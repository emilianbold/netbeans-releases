package org.netbeans.modules.iep.model;

public interface LinkComponent extends Component {

	public static final String PROP_FROM = "from";
	
	public static final String PROP_TO = "to";
	
	
	OperatorComponent getFrom();
	
	void setFrom(OperatorComponent from);
	
	OperatorComponent getTo();
	
	void setTo(OperatorComponent to);
}
