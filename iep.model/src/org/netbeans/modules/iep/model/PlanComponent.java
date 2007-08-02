package org.netbeans.modules.iep.model;

public interface PlanComponent extends Component {

	OperatorComponentContainer getOperatorComponentContainer();
	
	LinkComponentContainer getLinkComponentContainer();
	
	SchemaComponentContainer getSchemaComponentContainer();
	
}
