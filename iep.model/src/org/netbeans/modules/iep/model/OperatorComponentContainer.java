package org.netbeans.modules.iep.model;

import java.util.List;

public interface OperatorComponentContainer extends Component {

	List<OperatorComponent> getAllOperatorComponent();
	
	void addOperatorComponent(OperatorComponent operator);
	
	void removeOperatorComponent(OperatorComponent operator);
	
	OperatorComponent findChildComponent(String id);
}
