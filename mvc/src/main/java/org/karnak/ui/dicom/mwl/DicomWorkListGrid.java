package org.karnak.ui.dicom.mwl;


import java.util.Optional;

import org.dcm4che6.data.DicomElement;
import org.dcm4che6.data.DicomObject;
import org.dcm4che6.img.DicomImageUtils;
import org.dcm4che6.util.TagUtils;
import org.weasis.dicom.op.CFind;
import org.weasis.dicom.param.DicomParam;
import org.weasis.dicom.tool.ModalityWorklist;

import com.vaadin.flow.component.grid.Grid;

public class DicomWorkListGrid extends Grid<DicomObject> {

	private static final long serialVersionUID = 1L;

	
	public DicomWorkListGrid() {
		init();
		buildColumns();
	}
	
	
	private void init() {
		setSelectionMode(SelectionMode.SINGLE);
		
	}
	
	private void buildColumns() {
        DicomParam[] COLS = { CFind.PatientName, CFind.PatientID, CFind.PatientBirthDate, CFind.PatientSex,
                CFind.AccessionNumber, ModalityWorklist.ScheduledProcedureStepDescription, ModalityWorklist.Modality,
                ModalityWorklist.ScheduledStationName };

            for (DicomParam p : COLS) {
                addColumn(p);
            }
	}

    private void addColumn(DicomParam p) {
        int tag = p.getTag();
        int[] pSeq = p.getParentSeqTags();
        if (pSeq == null || pSeq.length == 0) {
        	addColumn(a -> a.get(tag).orElse(null))
        	.setHeader(TagUtils.toString(tag)) // TODO set name
        	.setSortable(true)
        	.setKey(String.valueOf(tag));
        } else {
            addColumn(a -> {
                DicomObject parent = a;
                for (int k = 0; k < pSeq.length; k++) {
                    DicomObject pn = getNestedDataset( parent, pSeq[k], 0);
                    if (pn == null) {
                        break;
                    }
                    parent = pn;
                }
                return parent.get(tag).orElse(null);
            })
            .setHeader(TagUtils.toString(tag))  // TODO set name
            .setSortable(true)
        	.setKey(String.valueOf(tag));
        }
    }
    

    public static DicomObject getNestedDataset(DicomObject dicom, int tagSeq, int index) {
        if (dicom != null) {
            Optional<DicomElement> item = dicom.get(tagSeq);
            if (item.isPresent() && !item.get().isEmpty()) {
                return item.get().getItem(index);
            }
        }
        return null;
    }
	
}
