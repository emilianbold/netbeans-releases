/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
