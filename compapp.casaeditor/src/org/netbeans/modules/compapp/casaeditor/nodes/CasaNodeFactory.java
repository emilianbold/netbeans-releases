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
package org.netbeans.modules.compapp.casaeditor.nodes;

import java.util.List;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.openide.util.lookup.InstanceContent;


public class CasaNodeFactory {
    
    private CasaDataObject mDataObject;
    private CasaWrapperModel mModel;
    
    
    public CasaNodeFactory(CasaDataObject dataObject, CasaWrapperModel model) {
        mDataObject = dataObject;
        mModel = model;
    }
    
    
    public CasaWrapperModel getCasaModel() {
        return mModel;
    }
    
    public InstanceContent createInstanceContent() {
        InstanceContent content = new InstanceContent();
        content.add(mDataObject);
        content.add(mModel);
        return content;
    }
    
    public CasaNode createModelNode(CasaWrapperModel model) {
        assert model != null;
        return new CasaRootNode(model, this);
    }
    
    public CasaNode createNode_connectionList(List<CasaConnection> data) {
        return new ConnectionsNode(data, this);
    }
    
    public CasaNode createNode_consumesList(List<CasaConsumes> data) {
        return new ConsumesListNode(data, this);
    }
    
    public CasaNode createNode_providesList(List<CasaProvides> data) {
        return new ProvidesListNode(data, this);
    }
    
    public CasaNode createNode_suList(List<CasaServiceEngineServiceUnit> data) {
        return new ServiceEnginesNode(data, this);
    }
    
    public CasaNode createNode_portList(List<CasaPort> data) {
        return new WSDLEndpointsNode(data, this);
    }
    
    public CasaNode createNodeFor(CasaComponent component) {
        CasaNodeCreationVisitor visitor = new CasaNodeCreationVisitor(this);
        component.accept(visitor);
        return visitor.getNode();
    }
    

    /**
     * Checks if the classes from source array are assignable to the
     * corresponding classes from target array.
     * Both arrays has to have the same quantity of elements.
     */
    private boolean isAssignable(Class<?>[] source, Class<?>[] target) {
        if (source == null || target == null || source.length != target.length) {
            return false;
        }
        for (int index = 0; index < source.length; index++) {
            if (!target[index].isAssignableFrom(source[index])) {
                return false;
            }
        }
        return true;
    }
}
