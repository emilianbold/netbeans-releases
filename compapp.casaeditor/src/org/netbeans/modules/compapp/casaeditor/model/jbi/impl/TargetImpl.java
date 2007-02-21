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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.visitor.JBIVisitor;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Target;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class TargetImpl extends JBIComponentImpl implements Target {
    
    /** Creates a new instance of TargetImpl */
    public TargetImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public TargetImpl(JBIModel model) {
        this(model, createElementNS(model, JBIQNames.TARGET));
    }
    
    public void accept(JBIVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getArtifactsZip() {
        return getChildElementText(JBIQNames.ARTIFACTS_ZIP.getQName());
    }
    
    public void setArtifactsZip(String artifactsZip) {
        setChildElementText(ARTIFACTS_ZIP_PROPERTY, artifactsZip, JBIQNames.ARTIFACTS_ZIP.getQName());
    }
    
    public String getComponentName() {
        return getChildElementText(JBIQNames.COMPONENT_NAME.getQName());
    }
    
    public void setComponentName(String componentName) {
        setChildElementText(COMPONENT_NAME_PROPERTY, componentName, JBIQNames.COMPONENT_NAME.getQName());
    }
    
    public String toString() {        
        StringBuilder sb = new StringBuilder();        
        sb.append("Target: [artifacts-zip=\"");
        sb.append(getArtifactsZip());
        sb.append("\" component-name=\"");
        sb.append(getComponentName());
        sb.append("\"]");        
        return sb.toString();
    }
}
