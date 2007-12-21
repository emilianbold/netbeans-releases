package org.netbeans.modules.reportgenerator.api.impl;

import org.netbeans.modules.reportgenerator.api.ReportAttribute;

public class ReportAttributeImpl implements ReportAttribute {

	private String mName;
	
	private Object mValue;
	
	public String getName() {
		return this.mName;
	}

	public Object getValue() {
		return this.mValue;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public void setValue(Object value) {
		this.mValue = value;
	}

}
