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
package org.netbeans.modules.soa.ui.nodes;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Here is the abstract base class for factories which use Reflection
 * approach to find constructor to create a new Node. 
 *
 * It's implied here that the Node has a constructors with at least one
 * from two possible parameters set. The cases are following:
 *  -- Object reference, Lookup lookup
 *  -- Object reference, Children children, Lookup lookup
 *
 * The Generics parameter KeyType represents the type of key which 
 * should be used by the factory. 
 * It usually is a kind of enumeration class. 
 * 
 * This factory is intended to create nodes without any children!
 * The standard approach is when another node factory responsible 
 * for construction nodes' hierarchy but it use this factory to 
 * create nodes themselves. 
 *
 * @author nk160297
 */
public class ReflectionNodeFactory<KeyType> implements NodeFactory<KeyType> {
    
    protected Map<KeyType, Class<? extends Node>> key2Class;
    
    protected ReflectionNodeFactory(int initialSize) {
        key2Class = new HashMap<KeyType,  Class<? extends Node>>(initialSize);
    }
    
    /**
     * See base class comment.
     */
    public Node createNode(KeyType nodeType, Object reference, Lookup lookup) {
        assert nodeType != null && reference != null;
        //
        return createNode(nodeType, reference, null, lookup);
    }
    
    /**
     * See base class comment.
     */
    public Node createNode(KeyType nodeType, Object reference, 
            Children children, Lookup lookup) {
        assert nodeType != null && reference != null;
        //
        Class<? extends Node> nodeClass = key2Class.get(nodeType);
        //
        if (nodeClass == null) {
            return null;
        }
        //
        Node newNode = Node.EMPTY;
        try {
            //
            // Here the reflection is used intensively
            // Try to find constructors with 2 and 3 parameters at first
            Constructor<? extends Node> constr2Params = null;
            Constructor<? extends Node> constr3Params = null;
            //
            Class[] params2 = new Class[] {reference.getClass(), Lookup.class};
            Class[] params3 = new Class[] {reference.getClass(), Children.class, Lookup.class};
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
                            new Object[] {reference, lookup});
                } else if (constr3Params != null) {
                    // Call the constructor with children parameter with
                    // the Children.LEAF value
                    newNode = constr3Params.newInstance(
                            new Object[] {reference, Children.LEAF, lookup});
                } else {
                    throw new Exception("The " + nodeClass.getName() +  // NOI18N
                            " class doesn't have the requred constructor.");  // NOI18N
                }
            } else {
                if (constr3Params != null) {
                    // Call the constructor with children parameter
                    // This is the normal branch
                    newNode = constr3Params.newInstance(
                            new Object[] {reference, children, lookup});
                } else if (constr2Params != null) {
                    // Call the constructor without children parameter
                    newNode = constr2Params.newInstance(
                            new Object[] {reference, lookup});
                } else {
                    throw new Exception("The " + nodeClass.getName() +  // NOI18N
                            " class doesn't have the requred constructor.");  // NOI18N
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        //
        return newNode;
    }
    
    public Class<? extends Node> getNodeClass(KeyType nodeType) {
        return key2Class.get(nodeType);
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
        //
        for (int index = 0; index < source.length; index++) {
            if (!target[index].isAssignableFrom(source[index])) {
                return false;
            }
        }
        //
        return true;
    }

}
