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

package org.netbeans.modules.encoder.hl7.aip.action;

import org.netbeans.modules.encoder.ui.basic.EncodingConst;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

/**
 * The visitor that applies HL7 encoding to every complex type or simple
 * type definition.
 *
 * @author Jun Xu
 */
public class ApplyHL7EncodingVisitor extends DeepSchemaVisitor implements SchemaVisitor {
    
    private final SchemaModel mModel;
    private int mSimpleTypeModified = 0;
    private int mComplexTypeModified = 0;
    private int mElementModified = 0;
        
    public ApplyHL7EncodingVisitor(SchemaModel rootModel) {
        mModel = rootModel;
    }
    
    public void visit(GlobalElement globalElement) {
        if (applyHL7Encoding(globalElement)) {
            mElementModified++;
        }
        super.visit(globalElement);
    }
    
    public void visit(LocalSimpleType localSimpleType) {
        if (applyHL7Encoding(localSimpleType)) {
            mSimpleTypeModified++;
        }
        super.visit(localSimpleType);
    }

    public void visit(GlobalSimpleType globalSimpleType) {
        if (applyHL7Encoding(globalSimpleType)) {
            mSimpleTypeModified++;
        }
        super.visit(globalSimpleType);
    }

    public void visit(GlobalComplexType globalComplexType) {
        if (applyHL7Encoding(globalComplexType)) {
            mComplexTypeModified++;
        }
        super.visit(globalComplexType);
    }

    public void visit(LocalComplexType localComplexType) {
        if (applyHL7Encoding(localComplexType)) {
            mComplexTypeModified++;
        }
        super.visit(localComplexType);
    }
    
    public int getComplexTypeModified() {
        return mComplexTypeModified;
    }
    
    public int getSimpleTypeModified() {
        return mSimpleTypeModified;
    }
    
    public int getElementModified() {
        return mElementModified;
    }
    
    private boolean applyHL7Encoding(SchemaComponent component) {
        if (!mModel.equals(component.getModel())) {
            return false;
        }
        boolean addAnnotation = false;
        boolean found = false;
        Annotation anno = component.getAnnotation();
        if (anno == null) {
            anno = component.getModel().getFactory().createAnnotation();
            addAnnotation = true;
        } else {
            for (AppInfo appInfo : anno.getAppInfos()) {
                if (EncodingConst.URI.equals(appInfo.getURI())) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            AppInfo appInfo = component.getModel().getFactory().createAppInfo();
            appInfo.setURI(EncodingConst.URI);
            anno.addAppInfo(appInfo);
            if (addAnnotation) {
                component.setAnnotation(anno);
            }
            return true;
        }
        return false;
    }
}
