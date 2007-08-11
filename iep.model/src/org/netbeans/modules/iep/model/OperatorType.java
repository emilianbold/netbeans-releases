package org.netbeans.modules.iep.model;

public enum OperatorType {
	
	
	OPERATOR_NONE("i18n.IEP.IOType.none"),
	OPERATOR_STREAM("i18n.IEP.IOType.stream"),
	OPERATOR_RELATION("i18n.IEP.IOType.relation"), 
	OPERATOR_TABLE("i18n.IEP.IOType.stream");
	
	private final String mType;
	
	private OperatorType(String type) {
		this.mType = type;
	}
	
	public static OperatorType getType(String type) {
		OperatorType t = null;
		if(type != null) {
			if(type.equals(OPERATOR_NONE.getType())) {
				t = OPERATOR_NONE;
			} else if(type.equals(OPERATOR_STREAM.getType())) {
				t = OPERATOR_STREAM;
			} else if(type.equals(OPERATOR_RELATION.getType())) {
				t = OPERATOR_RELATION;
			} else if(type.equals(OPERATOR_TABLE.getType())) {
				t = OPERATOR_TABLE;
			}
		}
		return t;
	}
	
	public String getType() {
		return this.mType;
	}
	
	public String toString() {
		return getType();
	}
}
