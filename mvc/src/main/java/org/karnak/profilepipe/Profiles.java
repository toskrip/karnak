package org.karnak.profilepipe;

import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dcm4che6.data.DicomElement;
import org.dcm4che6.data.DicomObject;
import org.dcm4che6.data.Tag;
import org.dcm4che6.data.VR;
import org.dcm4che6.img.op.MaskArea;
import org.dcm4che6.img.util.DicomObjectUtil;
import org.dcm4che6.util.TagUtils;
import org.karnak.api.PseudonymApi;
import org.karnak.api.rqbody.Fields;
import org.karnak.data.AppConfig;
import org.karnak.data.gateway.Destination;
import org.karnak.data.gateway.IdTypes;
import org.karnak.data.profile.Profile;
import org.karnak.data.profile.ProfileElement;
import org.karnak.expression.ExprConditionDestination;
import org.karnak.expression.ExpressionResult;
import org.karnak.profilepipe.action.*;
import org.karnak.profilepipe.profiles.AbstractProfileItem;
import org.karnak.profilepipe.profiles.ActionTags;
import org.karnak.profilepipe.profiles.ProfileItem;
import org.karnak.expression.ExprAction;
import org.karnak.profilepipe.utils.HMAC;
import org.karnak.util.SpecialCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.weasis.core.util.StringUtil;
import org.weasis.dicom.param.AttributeEditorContext;

public class Profiles {
    private final Logger LOGGER = LoggerFactory.getLogger(Profiles.class);

    private Profile profile;
    private final ArrayList<ProfileItem> profiles;
    private final Map<String, MaskArea> maskMap;
    private final HMAC hmac;

    public Profiles(Profile profile) {
        this.maskMap = new HashMap<>();
        this.hmac = AppConfig.getInstance().getHmac();
        this.profile = profile;
        this.profiles = createProfilesList();
    }

    public void addMaskMap(Map<? extends String, ? extends MaskArea> maskMap){
        this.maskMap.putAll(maskMap);
    }

    public MaskArea getMask(String key) {
        MaskArea mask = maskMap.get(key);
        if (mask == null) {
            mask = maskMap.get("*");
        }
        return mask;
    }

    public void addMask(String stationName, MaskArea maskArea){
        this.maskMap.put(stationName, maskArea);
    }

    public ArrayList<ProfileItem> createProfilesList() {
        if (profile != null) {
            final List<ProfileElement> listProfileElement = profile.getProfileElements();
            ArrayList<ProfileItem> profiles = new ArrayList<>();

            for (ProfileElement profileElement : listProfileElement) {
                AbstractProfileItem.Type t = AbstractProfileItem.Type.getType(profileElement.getCodename());
                if (t == null) {
                    LOGGER.error("Cannot find the profile codename: {}", profileElement.getCodename());
                } else {
                    Object instanceProfileItem;
                    try {
                        instanceProfileItem = t.getProfileClass()
                                .getConstructor(ProfileElement.class)
                                .newInstance(profileElement);
                        profiles.add((ProfileItem) instanceProfileItem);
                    } catch (Exception e) {
                        LOGGER.error("Cannot build the profile: {}", t.getProfileClass().getName(), e);
                    }
                }
            }
            profiles.sort(Comparator.comparing(ProfileItem::getPosition));
            profile.getMasks().forEach(
                m -> {
                    Color color = null;
                    if(StringUtil.hasText(m.getColor())) {
                        color = ActionTags.hexadecimal2Color(m.getColor());
                    }
                    List<Shape> shapeList = m.getRectangles().stream().map(Shape.class::cast).collect(Collectors.toList());
                    addMask(m.getStationName(), new MaskArea(shapeList, color));
                });
            return profiles;
        }
        return null;
    }

    public String getMainzellistePseudonym(DicomObject dcm, String externalPseudonym, IdTypes idTypes) throws IOException, InterruptedException {
        final String patientID = dcm.getString(Tag.PatientID).orElse(null);
        final String patientName = dcm.getString(Tag.PatientName).orElse(null);
        final String patientBirthDate = dcm.getString(Tag.PatientBirthDate).orElse(null);
        String patientSex = dcm.getString(Tag.PatientSex).orElse(null);
        if (!patientSex.equals("M") && !patientSex.equals("F") && !patientSex.equals("O")) {
           patientSex = "O";
        }
        // Issuer of patientID is recommended to make the patientID universally unique. Can be defined in profile if missing.
        final String issuerOfPatientID = dcm.getString(Tag.IssuerOfPatientID).orElse(profile.getDefaultissueropatientid());

        PseudonymApi pseudonymApi = new PseudonymApi(externalPseudonym);
        final Fields newPatientFields = new Fields(patientID, patientName, patientBirthDate, patientSex, issuerOfPatientID);

        return pseudonymApi.createPatient(newPatientFields, idTypes);
    }

    private String getExtIDInDicom(DicomObject dcm, Destination destination) {
        if (destination.getIdTypes().equals(IdTypes.ADD_EXTID)) {
            String cleanTag = destination.getTag().replaceAll("[(),]", "").toUpperCase();
            final String tagValue = dcm.getString(TagUtils.intFromHexString(cleanTag)).orElse(null);
            if (tagValue != null && destination.getDelimiter() != null && destination.getPosition() != null) {
                String delimiterSpec = SpecialCharacter.escapeSpecialRegexChars(destination.getDelimiter());
                try {
                    return tagValue.split(delimiterSpec)[destination.getPosition()];
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOGGER.error("Can not split the external pseudonym", e);
                    return null;
                }
            } else if (tagValue != null) {
                return tagValue;
            }
        }
        return null;
    }

    public void applyAction(DicomObject dcm, DicomObject dcmCopy, String patientID, ProfileItem profilePassedInSequence, ActionItem actionPassedInSequence, AttributeEditorContext context) {
        for (Iterator<DicomElement> iterator = dcm.iterator(); iterator.hasNext(); ) {
            final DicomElement dcmEl = iterator.next();
            final ExprConditionDestination exprConditionDestination = new ExprConditionDestination(dcmEl.tag(), dcmEl.vr(), dcm, dcmCopy);

            ActionItem currentAction = null;
            ProfileItem currentProfile = null;
            for (ProfileItem profile : profiles) {
                currentProfile = profile;

                if(profile.getCondition() == null){
                    currentAction = profile.getAction(dcm, dcmCopy, dcmEl, patientID);
                } else {
                    boolean conditionIsOk = (Boolean) ExpressionResult.get(profile.getCondition(), exprConditionDestination, Boolean.class);
                    if (conditionIsOk) {
                        currentAction = profile.getAction(dcm, dcmCopy, dcmEl, patientID);
                    }
                }

                if (currentAction != null) {
                    break;
                }

                if (profile.equals(profilePassedInSequence)){
                    currentAction = actionPassedInSequence;
                    break;
                }
            }

            if ( (!(Remove.class.isInstance(currentAction)) || !(ReplaceNull.class.isInstance(currentAction))) && dcmEl.vr() == VR.SQ) {
                final ProfileItem finalCurrentProfile = currentProfile;
                final ActionItem finalCurrentAction = currentAction;
                dcmEl.itemStream().forEach(d -> applyAction(d, dcmCopy, patientID, finalCurrentProfile, finalCurrentAction, context));
            } else {
                if (currentAction != null) {
                    try {
                        currentAction.execute(dcm, dcmEl.tag(), iterator, patientID);
                    } catch (final Exception e) {
                        LOGGER.error("Cannot execute the currentAction {} for tag: {}", currentAction,  TagUtils.toString(dcmEl.tag()), e);
                    }
                }
            }
        }
    }

    public void apply(DicomObject dcm, Destination destination, AttributeEditorContext context) {
        final String SOPinstanceUID = dcm.getString(Tag.SOPInstanceUID).orElse(null);
        final String IssuerOfPatientID = dcm.getString(Tag.IssuerOfPatientID).orElse(null);
        final String PatientID = dcm.getString(Tag.PatientID).orElse(null);
        final String stringExtIDInDicom = getExtIDInDicom(dcm, destination);
        final IdTypes idTypes = destination.getIdTypes();

        MDC.put("SOPInstanceUID", SOPinstanceUID);
        MDC.put("issuerOfPatientID", IssuerOfPatientID);
        MDC.put("PatientID", PatientID);

        String pseudonym = null;
        if (destination.getSavePseudonym() != null && destination.getSavePseudonym() == false) {
            pseudonym = stringExtIDInDicom;
            if (pseudonym == null) {
                throw new IllegalStateException("Cannot get a pseudonym in a DICOM tag");
            }
        } else {
            try {
                pseudonym = getMainzellistePseudonym(dcm, stringExtIDInDicom, idTypes);
            } catch (Exception e) {
                LOGGER.error("Cannot get a pseudonym with Mainzelliste API {}", e);
                throw new IllegalStateException("Cannot get a pseudonym with Mainzelliste API");
            }
        }

        String profilesCodeName = String.join(
                "-" , profiles.stream().map(profile -> profile.getCodeName()).collect(Collectors.toList())
        );
        BigInteger patientValue = generatePatientID(pseudonym, profilesCodeName);
        String newPatientName = !idTypes.equals(IdTypes.PID) && destination.getPseudonymAsPatientName() == true ?
                pseudonym : patientValue.toString(16).toUpperCase();
        String newPatientID = patientValue.toString();

        DicomObject dcmCopy = DicomObject.newDicomObject();
        DicomObjectUtil.copyDataset(dcm, dcmCopy);
        final String PatientIDProfile = HMAC.generatePatientIDProfile(PatientID, destination);

        // Apply clean pixel data
        Optional<DicomElement> pix = dcm.get(Tag.PixelData);
        if (pix.isPresent() && !profile.getMasks().isEmpty() && profiles.stream()
            .anyMatch(p -> CleanPixelData.class.isInstance(p.getAction(dcm, dcmCopy, pix.get(), PatientIDProfile)))) {
            String sopClassUID = dcm.getString(Tag.SOPClassUID)
                .orElseThrow(() -> new IllegalStateException("DICOM Object does not contain sopClassUID"));
            MaskArea mask = getMask(dcm.getString(Tag.StationName).orElse(null));
            // A mask must be applied with all the US and Secondary Capture sopClassUID, and with BurnedInAnnotation
            if (sopClassUID.startsWith("1.2.840.10008.5.1.4.1.1.6") || sopClassUID.startsWith("1.2.840.10008.5.1.4.1.1.7")
                || sopClassUID.startsWith("1.2.840.10008.5.1.4.1.1.3") || "YES"
                .equalsIgnoreCase(dcm.getString(Tag.BurnedInAnnotation).orElse(null))) {
                if (mask == null) {
                    throw new IllegalStateException("Cannot clean pixel data to sopClassUID " + sopClassUID);
                } else {
                    context.setMaskArea(mask);
                }
            }
        }

        applyAction(dcm, dcmCopy, PatientIDProfile, null, null, context);

        setDefaultDeidentTagValue(dcm, newPatientID, newPatientName, profilesCodeName, pseudonym);
        MDC.clear();
    }

    public void setDefaultDeidentTagValue(DicomObject dcm, String patientID, String patientName, String profilePipeCodeName, String pseudonym){
        final String profileFilename = profile.getName();
        final ArrayList<ExprAction> defaultDeidentTagValue = new ArrayList<>();
        defaultDeidentTagValue.add(new ExprAction(Tag.PatientID, VR.LO, patientID));
        defaultDeidentTagValue.add(new ExprAction(Tag.PatientName, VR.PN, patientName));
        defaultDeidentTagValue.add(new ExprAction(Tag.PatientIdentityRemoved, VR.CS, "YES"));
        // 0012,0063 -> module patient
        // A description or label of the mechanism or method use to remove the Patient's identity
        defaultDeidentTagValue.add(new ExprAction(Tag.DeidentificationMethod, VR.LO, profilePipeCodeName));
        defaultDeidentTagValue.add(new ExprAction(Tag.ClinicalTrialSponsorName, VR.LO, profilePipeCodeName));
        defaultDeidentTagValue.add(new ExprAction(Tag.ClinicalTrialProtocolID, VR.LO, profileFilename));
        defaultDeidentTagValue.add(new ExprAction(Tag.ClinicalTrialSubjectID, VR.LO, pseudonym));
        defaultDeidentTagValue.add(new ExprAction(Tag.ClinicalTrialProtocolName, VR.LO, (String) null));
        defaultDeidentTagValue.add(new ExprAction(Tag.ClinicalTrialSiteID, VR.LO, (String) null));
        defaultDeidentTagValue.add(new ExprAction(Tag.ClinicalTrialSiteName, VR.LO, (String) null));

        defaultDeidentTagValue.forEach(newElem -> {
            final ActionItem add = new Add("A", newElem.getTag(), newElem.getVr(), newElem.getStringValue());
            add.execute(dcm, newElem.getTag(), null, patientID);
        });
    }


    public BigInteger generatePatientID(String pseudonym, String profiles) {
        byte[] bytes = new byte[16];
        System.arraycopy(hmac.byteHash(pseudonym + profiles), 0, bytes, 0, 16);
        return new BigInteger(1, bytes);
    }
}
