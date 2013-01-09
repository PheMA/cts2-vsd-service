package edu.mayo.cts2.framework.plugin.service.mat.profile;

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetChange;
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
		ValueSetChange changeSet = changeSetRepository.findOne(changeSetUri);
		changeSetRepository.save(changeSet);
	}

}
