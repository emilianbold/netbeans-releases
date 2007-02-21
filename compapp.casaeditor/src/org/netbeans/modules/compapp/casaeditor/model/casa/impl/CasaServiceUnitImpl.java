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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Consumes;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Endpoint;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIComponent;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.jbi.GenericExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Provides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaBindings;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoints;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPorts;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public abstract class CasaServiceUnitImpl extends CasaComponentImpl 
        implements CasaServiceUnit {
    
    public CasaServiceUnitImpl(CasaModel model, Element element) {
        super(model, element);
    }    
        
    public String getName() {
        return getAttribute(CasaAttribute.NAME);
    }

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, CasaAttribute.NAME, name);        
    }    
        
    public String getUnitName() {
        return getAttribute(CasaAttribute.UNIT_NAME);
    }

    public void setUnitName(String unitName) {
        setAttribute(UNIT_NAME_PROPERTY, CasaAttribute.UNIT_NAME, unitName);        
    }    
    
    public String getDescription() {
        return getAttribute(CasaAttribute.DESCRIPTION);
    }

    public void setDescription(String description) {
        setAttribute(DESCRIPTION_PROPERTY, CasaAttribute.DESCRIPTION, description);        
    }
        
    public String getComponentName() {
        return getAttribute(CasaAttribute.COMPONENT_NAME);
    }

    public void setComponentName(String componentName) {
        setAttribute(COMPONENT_NAME_PROPERTY, CasaAttribute.COMPONENT_NAME, componentName);        
    }    
        
    public String getArtifactsZip() {
        return getAttribute(CasaAttribute.ARTIFACTS_ZIP);
    }

    public void setArtifactsZip(String artifactsZip) {
        setAttribute(ARTIFACTS_ZIP_PROPERTY, CasaAttribute.ARTIFACTS_ZIP, artifactsZip);        
    }    
}