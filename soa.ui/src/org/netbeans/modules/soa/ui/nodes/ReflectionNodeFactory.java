/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
