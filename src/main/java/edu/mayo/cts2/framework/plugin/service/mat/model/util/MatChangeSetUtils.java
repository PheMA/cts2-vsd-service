package edu.mayo.cts2.framework.plugin.service.mat.model.util;

import edu.mayo.cts2.framework.model.core.ChangeSetElementGroup;
import edu.mayo.cts2.framework.model.core.OpaqueData;
import edu.mayo.cts2.framework.model.core.SourceReference;
import edu.mayo.cts2.framework.model.updates.ChangeSet;
import edu.mayo.cts2.framework.model.updates.ChangeableResource;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSet;
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSetMember;

public class MatChangeSetUtils {

  public static ChangeSet transformToChangeSet(MatChangeSet matChangeSet) {
    if (matChangeSet != null) {
      ChangeSet changeSet = new ChangeSet();
      changeSet.setChangeSetURI(matChangeSet.getId());
      changeSet.setOfficialEffectiveDate(matChangeSet.getOfficialEffectiveDate());
      changeSet.setCreationDate(matChangeSet.getCreationDate());
      changeSet.setCloseDate(matChangeSet.getCloseDate());
	  ChangeSetElementGroup eg = new ChangeSetElementGroup();
	  SourceReference creator = new SourceReference();
      creator.setContent(matChangeSet.getCreator());
      eg.setCreator(creator);
      OpaqueData od = ModelUtils.createOpaqueData(matChangeSet.getChangeInstructions());
      eg.setChangeInstructions(od);
      changeSet.setChangeSetElementGroup(eg);
      changeSet.setState(matChangeSet.getState());
      return changeSet;
    }

    return null;
  }

  public static MatChangeSet transformToMatChangeSet(ChangeSet changeSet) {
    MatChangeSet matChangeSet = new MatChangeSet(changeSet.getChangeSetURI());
    matChangeSet.setOfficialEffectiveDate(changeSet.getOfficialEffectiveDate());
    matChangeSet.setCreationDate(changeSet.getCreationDate());
    matChangeSet.setCloseDate(changeSet.getCloseDate());
    ChangeSetElementGroup eg = changeSet.getChangeSetElementGroup();
    if (eg != null) {
      matChangeSet.setCreator(eg.getCreator().getContent());
      matChangeSet.setChangeInstructions(eg.getChangeInstructions().getValue().toString());
    }
    matChangeSet.setState(changeSet.getState());
    return matChangeSet;
  }

}
