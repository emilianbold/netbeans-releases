/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Any.java
 *
 * Created on September 29, 2005, 7:27 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents common features of any. 
 * @author Chris Webster
 */
public interface Any {
	
	public static final String NAMESPACE_PROPERTY = "namespace";
	public static final String PROCESS_CONTENTS_PROPERTY = "processContents";
	
	// ##any, ##other (xs:anyURI | ##targetNamespace, ##local
	String getNamespace();
	void setNamespace(String namespace);
	String getNamespaceDefault();
        String getNameSpaceEffective();
        
	ProcessContents getProcessContents();
	void setProcessContents(ProcessContents pc);
	ProcessContents getProcessContentsDefault();
	ProcessContents getProcessContentsEffective();
        
	enum ProcessContents {
		SKIP("skip"), LAX("lax"), STRICT("strict");
                private String value;
                ProcessContents(String s) {
                    value = s;
                }
                public String toString() {
                    return value;
                }
	}
}
