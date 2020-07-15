package org.karnak.profilepipe.profiles;

import org.dcm4che6.data.DicomElement;
import org.karnak.data.AppConfig;
import org.karnak.data.profile.ExceptedTag;
import org.karnak.data.profile.IncludedTag;
import org.karnak.profilepipe.action.Action;
import org.karnak.profilepipe.utils.TagActionMap;
import org.karnak.standard.ConfidentialityProfiles;

import java.util.List;

public class BasicProfile extends AbstractProfileItem {
    private final List<ProfileItem> listProfiles;
    private final TagActionMap actionMap;

    public BasicProfile(String name, String codeName, String action, List<IncludedTag> tags, List<ExceptedTag> exceptedTags) {
        super(name, codeName, action, tags, exceptedTags);
        ConfidentialityProfiles confidentialityProfiles = AppConfig.getInstance().getConfidentialityProfile();
        actionMap = confidentialityProfiles.getActionMap();
        listProfiles = confidentialityProfiles.getListProfiles();
    }

    @Override
    public Action getAction(DicomElement dcmElem) {
        int tag = dcmElem.tag();
        Action action = actionMap.get(tag);
        if (action == null) {
            for (ProfileItem p : listProfiles) {
                Action val = p.getAction(dcmElem);
                if(val != null){
                    return val;
                }
            }
            return null;
        }
        return action;
    }
}
