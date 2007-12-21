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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class AbstractTransformationUC implements TransformationUC {
    private List<TransformationDesc> descs;
    private XsltMapModel model;
    
    public AbstractTransformationUC(XsltMapModel model) {
        this.model = model;
    }

    public XsltMapModel getXsltMapModel() {
        return model;
    }

    public void addTransformationDesc(TransformationDesc desc) {
        if (desc == null) {
            return;
        }

        if (descs == null ) {
            descs = new ArrayList<TransformationDesc>();
        } 
        descs.add(desc);
    }
    
    public void setTransformationDescs(List<TransformationDesc> descs) {
        this.descs = descs;
    }

    public List<TransformationDesc> getTransformationDescs() {
        return descs;
    }
    
    public InputTransformationDesc getInputTransformationDesc() {
        return (InputTransformationDesc) getTransformationDesc(
                TransformationDescType.INPUT);
    }
    
    
    public OutputTransformationDesc getOutputTransformationDesc() {
        return (OutputTransformationDesc) getTransformationDesc(
                TransformationDescType.OUTPUT);
    }
    
    /**
     * @return Returns first transformation desc with type tDescType
     */ 
    private TransformationDesc getTransformationDesc(TransformationDescType tDescType) {
        if (tDescType == null) {
            return null;
        }
        
        List<TransformationDesc> tDescs = getTransformationDescs();
        if (tDescs == null) {
            return null;
        }
        
        for (TransformationDesc tDesc : descs) {
            if (tDescType.equals(tDesc.getType())) {
                return tDesc;
            }
        }
        return null;
    }
}
