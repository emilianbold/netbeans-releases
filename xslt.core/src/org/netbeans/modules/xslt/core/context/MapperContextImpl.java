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
package org.netbeans.modules.xslt.core.context;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeListener;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperContextImpl implements MapperContext {

    private Transform myTransformContextComponent;
    private XslModel myXslModel;
    private AXIComponent mySourceComponent;
    private AXIComponent myTargetComponent;
//    private MapperContextChangeSupport changeSupport;

    public MapperContextImpl(XslModel xslModel, TMapModel model) {
        assert model != null;
        
        this.myTransformContextComponent = null;
        this.myXslModel = xslModel;
        this.mySourceComponent = null;
        this.myTargetComponent = null;
// TODO a        
//        this.changeSupport = new MapperContextChangeSupport();
//        
//        xsltMapModel.addPropertyChangeListener(this);
    }

    // TODO m
    public MapperContextImpl(Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        
        this.myTransformContextComponent = transform;
        this.myXslModel = xslModel;
        this.mySourceComponent = sourceComponent;
        this.myTargetComponent = targetComponent;
//        this.changeSupport = new MapperContextChangeSupport();
//        
//        assocTransformDesc.getModel().addPropertyChangeListener(this);
    }

////    public MapperContextImpl(Operation operation, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
////        this.myTransformContextComponent = operation;
////        this.myXslModel = xslModel;
////        this.mySourceComponent = sourceComponent;
////        this.myTargetComponent = targetComponent;
//////        this.changeSupport = new MapperContextChangeSupport();
//////        
//////        assocTransformDesc.getModel().addPropertyChangeListener(this);
////    }

    public XslModel getXSLModel() {
        return myXslModel;
    }

    public AXIComponent getTargetType() {
        return myTargetComponent;
    }

    public AXIComponent getSourceType() {
        return mySourceComponent;
    }

    public void addMapperContextChangeListener(MapperContextChangeListener listener) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeMapperContextChangeListener(MapperContextChangeListener listener) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

}
