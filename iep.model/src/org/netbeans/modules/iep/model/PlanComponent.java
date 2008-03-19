package org.netbeans.modules.iep.model;

import java.util.List;

public interface PlanComponent extends Component {

	OperatorComponentContainer getOperatorComponentContainer();
	
	LinkComponentContainer getLinkComponentContainer();
	
	SchemaComponentContainer getSchemaComponentContainer();
	
        List<Import> getImports();
}
