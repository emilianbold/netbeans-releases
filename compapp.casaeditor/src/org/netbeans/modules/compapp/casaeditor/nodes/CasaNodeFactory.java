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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaConnectionImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaConnectionsImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaConsumesImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaServiceEngineServiceUnitImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaPortImpl;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaProvidesImpl;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 * It's implied here that the Node has a constructors with at least one
 * from two possible parameters set. The cases are following:
 *  -- Assign reference, Lookup lookup
 *  -- Assign reference, Children children, Lookup lookup
 *
 * @author nk160297
 */
public class CasaNodeFactory {
    
    private CasaWrapperModel mModel;
    private Lookup mLookup;
    
    private Map<Class<? extends CasaComponent>, Class<? extends Node>> constant2Class;
    
    
    public CasaNodeFactory(CasaDataObject dataObject, CasaWrapperModel model) {
        mModel = model;
        
        mLookup = Lookups.fixed(new Object[] {
            dataObject, 
            model
        });
        
        constant2Class = new HashMap<Class<? extends CasaComponent>, Class<? extends Node>>();
        constant2Class.put(CasaServiceEngineServiceUnitImpl.class,     ServiceUnitNode.class);
        constant2Class.put(CasaPortImpl.class,        WSDLEndpointNode.class);
        constant2Class.put(CasaConnectionsImpl.class,     ConnectionsNode.class);
        constant2Class.put(CasaConnectionImpl.class,      ConnectionNode.class);
        constant2Class.put(CasaProvidesImpl.class,        ProvidesNode.class);
        constant2Class.put(CasaConsumesImpl.class,        ConsumesNode.class);
    }
    
    
    public Node createNode(CasaWrapperModel model) {
        assert model != null;
        return new CasaRootNode(model, mLookup);
    }
    
    public Node createNode(CasaComponent component) {
        assert component != null;
        return createNode(component, null);
    }

    public Node createNode(CasaComponent component, Children children) {
        assert component != null;
        Class<? extends Node> nodeClass = constant2Class.get(component.getClass());
        if (nodeClass == null) {
            return null;
        }
        Node newNode = Node.EMPTY;
        try {
            //
            // Here the reflection is used intensively
            // Try to find constructors with 2 and 3 parameters at first
            Constructor<? extends Node> constr2Params = null;
            Constructor<? extends Node> constr3Params = null;
            //
            Class[] params2 = new Class[] {component.getClass(), Lookup.class};
            Class[] params3 = new Class[] {component.getClass(), Children.class, Lookup.class};
            //
            Constructor[] constArr = nodeClass.getConstructors();
            for (Constructor constr : constArr) {
                Class<?>[] paramClassArr = constr.getParameterTypes();
                if (constr2Params == null &&
                        isAssignable(params2, paramClassArr)) {
                    constr2Params = constr;
                }
                if (constr3Params == null &&
                        isAssignable(params3, paramClassArr)) {
                    constr3Params = constr;
                }
            }
            //
            if (children == null)  {
                if (constr2Params != null) {
                    // Call the constructor without children parameter
                    // This is the normal branch
                    newNode = constr2Params.newInstance(
                            new Object[] {component, mLookup});
                } else if (constr3Params != null) {
                    // Call the constructor with children parameter with
                    // the Children.LEAF value
                    newNode = constr3Params.newInstance(
                            new Object[] {component, Children.LEAF, mLookup});
                } else {
                    throw new Exception("The " + nodeClass.getName() +  // NOI18N
                            " class doesn't have the requred constructor.");  // NOI18N
                }
            } else {
                if (constr3Params != null) {
                    // Call the constructor with children parameter
                    // This is the normal branch
                    newNode = constr3Params.newInstance(
                            new Object[] {component, children, mLookup});
                } else if (constr2Params != null) {
                    // Call the constructor without children parameter
                    newNode = constr2Params.newInstance(
                            new Object[] {component, mLookup});
                } else {
                    throw new Exception("The " + nodeClass.getName() +  // NOI18N
                            " class doesn't have the requred constructor.");  // NOI18N
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return newNode;
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
