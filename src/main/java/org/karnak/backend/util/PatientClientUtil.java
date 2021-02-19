/*
 * Copyright (c) 2020-2021 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.backend.util;

import org.karnak.backend.cache.PatientClient;
import org.karnak.backend.cache.PseudonymPatient;
import org.karnak.backend.model.profilepipe.PatientMetadata;

public class PatientClientUtil {

  private PatientClientUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static String getPseudonym(PatientMetadata patientMetadata, PatientClient cache) {
    if (cache != null) {
      final String key = generateKey(patientMetadata);
      final PseudonymPatient patient = cache.get(key);
      if (patient != null && patientMetadata.compareCachedPatient(patient)) {
        return patient.getPseudonym();
      }
    }
    return null;
  }

  public static String generateKey(String patientID, String patientName) {
    return patientID.concat(patientName);
  }

  public static String generateKey(PseudonymPatient patient) {
    String patientID = patient.getPatientId();
    String patientName = patient.getPatientName();
    return generateKey(patientID, patientName);
  }

  public static String generateKey(PatientMetadata patientMetadata) {
    String patientID = patientMetadata.getPatientID();
    String patientName = patientMetadata.getPatientName();
    return generateKey(patientID, patientName);
  }
}
