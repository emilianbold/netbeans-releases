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

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class AppInfoImpl extends SchemaComponentImpl implements AppInfo {
    
    public AppInfoImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.APPINFO, model));
    }
    
    public AppInfoImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

    public void setURI(String uri) {
        setAttribute(SOURCE_PROPERTY, SchemaAttributes.SOURCE, uri);
    }

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public String getURI() {
        return getAttribute(SchemaAttributes.SOURCE);
    }

    public Element getAppInfoElement() {
        return Element.class.cast(getPeer().cloneNode(true));
    }

    public void setAppInfoElement(Element content) {
        super.updatePeer(CONTENT_PROPERTY, content);
    }

    public Class<? extends SchemaComponent> getComponentType() {
        return AppInfo.class;
    }
}
