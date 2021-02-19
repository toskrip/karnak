package org.karnak.backend.service;

import org.dcm4che6.data.DicomObject;
import org.dcm4che6.data.Tag;
import org.dcm4che6.data.VR;
import org.karnak.backend.cache.PatientClient;
import org.karnak.backend.config.AppConfig;
import org.karnak.backend.model.profilepipe.PatientMetadata;
import org.karnak.backend.util.PatientClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.AttributeEditor;
import org.weasis.dicom.param.AttributeEditorContext;
import org.weasis.dicom.param.AttributeEditorContext.Abort;

public class BhutanEditor implements AttributeEditor {
  private static final Logger LOGGER = LoggerFactory.getLogger(BhutanEditor.class);

  private final PatientClient externalIdCache;

  public BhutanEditor() {
    externalIdCache = AppConfig.getInstance().getExternalIDCache();
  }

  @Override
  public void apply(DicomObject dcm, AttributeEditorContext context) {
    String newPatientID =
        PatientClientUtil.getPseudonym(new PatientMetadata(dcm, ""), externalIdCache);
    if (newPatientID == null || newPatientID.equals("")) {
      LOGGER.error(
          String.format(
              " The patient %s, %s is not present in the cache",
              dcm.getString(Tag.PatientName).orElse(null),
              dcm.getString(Tag.PatientID).orElse(null)));
      context.setAbort(Abort.FILE_EXCEPTION);
    } else {
      dcm.setString(Tag.PatientID, VR.LO, newPatientID);
    }
  }
}
