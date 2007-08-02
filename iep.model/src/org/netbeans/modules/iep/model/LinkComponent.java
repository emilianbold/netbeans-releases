package org.netbeans.modules.iep.model;

public interface LinkComponent extends Component {

	OperatorComponent getFrom();
	
	OperatorComponent getTo();
}
