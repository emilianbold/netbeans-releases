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

package org.netbeans.modules.encoder.coco.ui.action;

import org.netbeans.modules.encoder.ui.basic.EncodingConst;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

/**
 * The visitor that applies COBOL Copybook encoding to every element declaration.
 *
 * @author Jun Xu
 */
public class ApplyCocoEncodingVisitor extends DeepSchemaVisitor implements SchemaVisitor {
    
    private final SchemaModel mModel;
    private int mElementModified = 0;
        
    public ApplyCocoEncodingVisitor(SchemaModel rootModel) {
        mModel = rootModel;
    }
    
    public void visit(LocalElement localElement) {
        applyCocoEncoding(localElement);
        super.visit(localElement);
    }

    public void visit(GlobalElement globalElement) {
        applyCocoEncoding(globalElement);
        super.visit(globalElement);
    }
    
    public int getElementModified() {
        return mElementModified;
    }
    
    private void applyCocoEncoding(Element elem) {
        if (!mModel.equals(elem.getModel())) {
            return;
        }
        boolean addAnnotation = false;
        boolean found = false;
        Annotation anno = elem.getAnnotation();
        if (anno == null) {
            anno = elem.getModel().getFactory().createAnnotation();
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
            AppInfo appInfo = elem.getModel().getFactory().createAppInfo();
            appInfo.setURI(EncodingConst.URI);
            anno.addAppInfo(appInfo);
            if (addAnnotation) {
                elem.setAnnotation(anno);
            }
            mElementModified++;
        }
    }
}
