package edu.mayo.cts2.framework.plugin.service.mat.profile;

import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSet;
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSetMember;
import edu.mayo.cts2.framework.plugin.service.mat.model.util.MatChangeSetUtils;
import edu.mayo.cts2.framework.plugin.service.mat.repository.ChangeSetRepository;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.model.core.ChangeDescription;
import edu.mayo.cts2.framework.model.updates.ChangeableResource;

import javax.annotation.Resource;

@Component
public class StateChangeCallback {

	@Resource
	ChangeSetRepository changeSetRepository;

	public void resourceAdded(ChangeableResource changeable) {
		ChangeDescription changeDescription =
		  changeable.getChangeableElementGroup().getChangeDescription();

		String changeSetUri = changeDescription.
		  getContainingChangeSet();

		this.addToChangeSet(changeSetUri, changeable);
	}

	public void resourceUpdated(ChangeableResource changeable) {
		ChangeDescription changeDescription =
		  changeable.getChangeableElementGroup().getChangeDescription();

		String changeSetUri = changeDescription.
		  getContainingChangeSet();

		this.addToChangeSet(changeSetUri, changeable);
	}

	public void resourceDeleted(ChangeableResource changeable, String changeSetUri) {

		this.addToChangeSet(changeSetUri, changeable);
	}

	protected void addToChangeSet(String changeSetUri, ChangeableResource changeable){
		MatChangeSet changeSet = changeSetRepository.findOne(changeSetUri);
		long count = changeSet.getEntryCount();
		count ++;
		changeable.setEntryOrder(count);
		changeSet.setEntryCount(count);
		if (changeable instanceof MatChangeSetMember)
			changeSet.addMember((MatChangeSetMember)changeable);
		changeSetRepository.save(changeSet);
	}

}
