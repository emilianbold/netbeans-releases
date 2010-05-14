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

package org.netbeans.modules.encoder.ui.basic;

import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

/**
 * The visitor that removes encoding information from every element declaration
 * and every type definition.
 *
 * @author Jun Xu
 */
public class RemoveEncodingVisitor extends DeepSchemaVisitor implements SchemaVisitor {
    
    private final SchemaModel mModel;
    private int mElementModified = 0;
    private int mComplexTypeModified = 0;
    private int mSimpleTypeModified = 0;
        
    public RemoveEncodingVisitor(SchemaModel rootModel) {
        mModel = rootModel;
    }
    
    public void visit(LocalElement localElement) {
        if (removeEncoding(localElement)) {
            mElementModified++;
        }
        super.visit(localElement);
    }

    public void visit(GlobalElement globalElement) {
        if (removeEncoding(globalElement)) {
            mElementModified++;
        }
        super.visit(globalElement);
    }
    
    public void visit(GlobalSimpleType globalSimpleType) {
        if (removeEncoding(globalSimpleType)) {
            mSimpleTypeModified++;
        }
        super.visit(globalSimpleType);
    }

    public void visit(LocalSimpleType localSimpleType) {
        if (removeEncoding(localSimpleType)) {
            mSimpleTypeModified++;
        }
        super.visit(localSimpleType);
    }

    public void visit(GlobalComplexType globalComplexType) {
        if (removeEncoding(globalComplexType)) {
            mComplexTypeModified++;
        }
        super.visit(globalComplexType);
    }

    public void visit(LocalComplexType localComplexType) {
        if (removeEncoding(localComplexType)) {
            mComplexTypeModified++;
        }
        super.visit(localComplexType);
    }

    public int getElementModified() {
        return mElementModified;
    }
    
    public int getComplexTypeModified() {
        return mComplexTypeModified;
    }
    
    public int getSimpleTypeModified() {
        return mSimpleTypeModified;
    }
    
    private boolean removeEncoding(SchemaComponent component) {
        if (!mModel.equals(component.getModel())) {
            return false;
        }
        Annotation anno = component.getAnnotation();
        if (anno == null) {
            return false;
        }
        boolean found = false;
        AppInfo[] appInfos = anno.getAppInfos().toArray(new AppInfo[0]);
        for (int i = 0; appInfos != null && i < appInfos.length; i++) {
            if (EncodingConst.URI.equals(appInfos[i].getURI())) {
                anno.removeAppInfo(appInfos[i]);
                return true;
            }
        }
        return false;
    }
}
