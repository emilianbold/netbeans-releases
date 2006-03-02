/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
