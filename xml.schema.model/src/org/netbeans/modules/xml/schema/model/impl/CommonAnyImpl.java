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
 * CommonAnyImpl.java
 *
 * Created on October 7, 2005, 8:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Any;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public abstract class CommonAnyImpl extends SchemaComponentImpl implements Any {
    
    /**
     * Creates a new instance of CommonAnyImpl
     */
    public CommonAnyImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
    
    /**
     *
     */
    public void setNamespace(String namespace) {
	setAttribute(NAMESPACE_PROPERTY, SchemaAttributes.NAMESPACE, namespace);
    }
    
    /**
     *
     */
    public void setProcessContents(ProcessContents pc) {
	setAttribute(PROCESS_CONTENTS_PROPERTY, SchemaAttributes.PROCESS_CONTENTS, pc);
    }
    
    public ProcessContents getDefaultProcessContents() {
        return ProcessContents.STRICT;
    }
	
    public ProcessContents getEffectiveProcessContents() {
        ProcessContents v = getProcessContents();
        return v == null ? getDefaultProcessContents() : v;
    }
    
    /**
     *
     */
    public ProcessContents getProcessContents() {
	String s = getAttribute(SchemaAttributes.PROCESS_CONTENTS);
	return s == null? null : Util.parse(ProcessContents.class, s);
    }
    
    public ProcessContents getProcessContentsEffective() {
        ProcessContents v = getProcessContents();
        return v == null ? getProcessContentsDefault() : v;
    }

    public ProcessContents getProcessContentsDefault() {
        return ProcessContents.STRICT;
    }

    /**
     *
     */
    public String getNamespace() {
	return getAttribute(SchemaAttributes.NAMESPACE);
	
    }
    
    public String getNamespaceDefault() {
        return "##any"; //NOI18N
    }

    public String getNameSpaceEffective() {
        String v = getNamespace();
        return v == null ? getNamespaceDefault() : v;
    }
}
