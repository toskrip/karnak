package org.karnak.backend.model.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.dcm4che6.data.DicomElement;
import org.dcm4che6.data.DicomObject;
import org.dcm4che6.data.VR;
import org.karnak.backend.data.entity.ArgumentEntity;
import org.karnak.backend.model.profilepipe.HMAC;
import org.karnak.backend.util.ShiftRangeDate;

public class DefaultDummy extends AbstractAction {

  List<ArgumentEntity> argumentEntities;
  ShiftRangeDate shiftRangeDate;

    public DefaultDummy(String symbol) {
      super(symbol);
      argumentEntities = new ArrayList<>();
      argumentEntities.add(new ArgumentEntity("max_days", "365"));
      argumentEntities.add(new ArgumentEntity("max_seconds", "86400"));
      shiftRangeDate = new ShiftRangeDate();
    }

    public DefaultDummy(String symbol, String dummyValue) {
        super(symbol, dummyValue);
    }

    @Override
    public void execute(DicomObject dcm, int tag, Iterator<DicomElement> iterator, HMAC hmac) {
        Optional<DicomElement> dcmItem = dcm.get(tag);
        DicomElement dcmEl = dcmItem.orElseThrow();
        VR vr = dcmEl.vr();
        String defaultDummyValue = switch (vr) {
            case AE, CS, LO, LT, PN, SH, ST, UN, UT, UC, UR -> "UNKNOWN";
          case DS, IS -> "0";
          case AS, DA, DT, TM -> shiftRangeDate.shift(dcm, dcmEl, argumentEntities, hmac);
          case UI -> hmac.uidHash(dcm.getString(tag).orElse(null));
            default -> null;
        };
        ActionItem replace = new Replace(symbol, defaultDummyValue);
        replace.execute(dcm, tag, iterator, hmac);
    }
}
