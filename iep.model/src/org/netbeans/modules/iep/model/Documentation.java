package org.netbeans.modules.iep.model;

public interface Documentation extends IEPComponent {
	public static final String CONTENT_PROPERTY = "content";
	
	void setTextContent(String content);
	
	String getTextContent();
}
