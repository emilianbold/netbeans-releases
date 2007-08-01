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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentFactory;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapComponentFactoryImpl implements TMapComponentFactory {
    
    private TMapModelImpl myModel;
    private ThreadLocal<TMapComponentBuildVisitor> myBuilder;
    
    public TMapComponentFactoryImpl(TMapModelImpl model) {
        this.myModel = model;
        myBuilder = new ThreadLocal<TMapComponentBuildVisitor>();
    }

    public TransformMap createTransformMap() {
        return new TransformMapImpl(getModel());
    }

    public Service createService() {
        return new ServiceImpl(getModel());
    }

    public Operation createOperation() {
        return new OperationImpl(getModel());
    }

    public Invoke createInvoke() {
        return new InvokeImpl(getModel());
    }

    public Transform createTransform() {
        return new TransformImpl(getModel());
    }

    public Param createParam() {
        return new ParamImpl(getModel());
    }

    public TMapComponent create(Element child, TMapComponent parent) {
        TMapComponentBuildVisitor visitor = getBuilder();
        return visitor.createSubComponent( parent , child  );
    }

    private TMapComponentBuildVisitor getBuilder() {
        TMapComponentBuildVisitor visitor = myBuilder.get();
        if ( visitor == null ) {
            visitor = new TMapComponentBuildVisitor( getModel());
            myBuilder.set( visitor );
        }
        visitor.init();
        return visitor;
    }

    private TMapModelImpl getModel() {
        return myModel;
    }
}
