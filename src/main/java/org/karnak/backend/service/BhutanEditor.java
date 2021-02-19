/*
 * Copyright (c) 2021 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
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
