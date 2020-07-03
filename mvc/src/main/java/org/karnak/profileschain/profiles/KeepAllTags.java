package org.karnak.profileschain.profiles;

import org.dcm4che6.data.DicomElement;
import org.karnak.data.profile.Policy;
import org.karnak.profileschain.action.Action;

public class KeepAllTags extends AbstractProfileItem {

    public KeepAllTags(String name, String codeName, Policy policy, ProfileItem parentProfile) {
        super(name, codeName, policy, parentProfile);
    }

    @Override
    public Action getAction(DicomElement dcmElem) {
        return Action.KEEP;
    }
}
