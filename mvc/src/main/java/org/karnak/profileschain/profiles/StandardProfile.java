package org.karnak.profileschain.profiles;

import org.dcm4che6.data.DicomElement;
import org.karnak.data.AppConfig;
import org.karnak.data.profile.Policy;
import org.karnak.profileschain.action.Action;

import java.util.ArrayList;
import java.util.List;

public class StandardProfile extends AbstractProfileItem {

    private final List<ProfileItem> groupList;

    public StandardProfile(String name, String codeName, ProfileItem profileParent) {
        this(name, codeName, Type.BASIC_DICOM.getPolicy(), profileParent);
    }

    public StandardProfile(String name, String codeName, Policy policy, ProfileItem profileParent) {
        super(name, codeName, policy, profileParent);
        if (policy != Type.BASIC_DICOM.getPolicy()) {
            throw new IllegalStateException(String.format("The policy %s is not consistent with the profile %s!", policy, codeName));
        }
        BasicDicomProfile basicDicomProfile = AppConfig.getInstance().getStandardProfile();
        this.tagMap.putAll(basicDicomProfile.getActionMap());
        this.groupList = new ArrayList<>(basicDicomProfile.getGroupList());
    }

    @Override
    public Action getAction(DicomElement dcmElem) {
        int tag = dcmElem.tag();
        Action action = tagMap.get(tag);
        if (action == null) {
            for (ProfileItem p : groupList) {
                Action val = p.getAction(dcmElem);
                if(val != null){
                    return val;
                }
            }
            return this.getParentAction(dcmElem);
        }
        return action;
    }
}
