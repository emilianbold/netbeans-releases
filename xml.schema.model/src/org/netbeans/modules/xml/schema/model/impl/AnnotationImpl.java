/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

/*
 * AnnotationImpl.java
 *
 * Created on October 7, 2005, 8:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class AnnotationImpl extends SchemaComponentImpl implements Annotation {
    
    protected AnnotationImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.ANNOTATION, model));
    }
    /**
     * Creates a new instance of AnnotationImpl
     */
    public AnnotationImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Annotation.class;
	}
    
    /**
     *
     */
    public void removeDocumentation(Documentation documentation) {
	removeChild(DOCUMENTATION_PROPERTY, documentation);
    }
    
    /**
     *
     */
    public void addDocumentation(Documentation documentation) {
	appendChild(DOCUMENTATION_PROPERTY, documentation);
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
    
    /**
     *
     */
    public java.util.Collection<Documentation> getDocumentations() {
	return getChildren(Documentation.class);
    }

    public void removeAppInfo(AppInfo appInfo) {
        removeChild(Annotation.APPINFO_PROPERTY, appInfo);
    }

    public void addAppInfo(AppInfo appInfo) {
        appendChild(Annotation.APPINFO_PROPERTY, appInfo);
    }

    public Collection<AppInfo> getAppInfos() {
	return getChildren(AppInfo.class);
    }
}
