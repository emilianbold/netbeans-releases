package org.netbeans.modules.iep.model;

import java.util.List;

public interface LinkComponentContainer extends Component {

	List<LinkComponent> getAllLinkComponents();
	
	void addLinkComponent(LinkComponent link);
	
	void removeLinkComponent(LinkComponent link);
}
