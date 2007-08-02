package org.netbeans.modules.iep.model;

public enum OperatorType {
	
	OPERATOR_STREAM("i18n.IEP.IOType.stream"),
	OPERATOR_RELATION("i18n.IEP.IOType.relation"), 
	OPERATOR_TABLE("i18n.IEP.IOType.stream");
	
	private final String mType;
	
	private OperatorType(String type) {
		this.mType = type;
	}
}
