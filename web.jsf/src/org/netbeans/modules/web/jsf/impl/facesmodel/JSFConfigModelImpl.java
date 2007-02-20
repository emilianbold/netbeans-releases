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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigModelImpl extends AbstractDocumentModel<JSFConfigComponent> implements JSFConfigModel {
    
    private static final Logger LOGGER = Logger.getLogger(JSFConfigModelImpl.class.getName());
    
    private FacesConfig facesConfig;
    private final JSFConfigComponentFactory componentFactory;
    
    /** Creates a new instance of JSFConfigModelImpl */
    public JSFConfigModelImpl(ModelSource source) {
        super(source);
        componentFactory = new JSFConfigComponentFactoryImpl(this);
    }

    public JSFConfigComponent createRootComponent(Element root) {
        FacesConfig newFacesConfig = (FacesConfig) getFactory().create(root, null);
        if (newFacesConfig != null) {
            facesConfig = newFacesConfig;
        }
        return newFacesConfig;
    }

    protected ComponentUpdater<JSFConfigComponent> getComponentUpdater() {
        return null;
    }

    public FacesConfig getRootComponent() {
        LOGGER.fine("getRootComponent()");
        return facesConfig;
    }

    public JSFConfigComponent createComponent(JSFConfigComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public JSFConfigComponentFactory getFactory() {
        return componentFactory;
    }
    
    public JSFVersion getVersion() {
        String namespaceURI = getRootComponent().getPeer().getNamespaceURI();
        JSFVersion version = JSFVersion.JSF_1_1;
        if (namespaceURI != null){
            version = JSFVersion.JSF_1_2;
        }
        return version;
    }

}
