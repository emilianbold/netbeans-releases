/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl.semantic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.NodeVisitor;
import org.netbeans.modules.css.model.impl.semantic.box.TokenNodeModel;

/**
 *
 * @author marekfukala
 */
public class ModelBuilderNodeVisitor implements NodeVisitor {

    private Collection<NodeModel> models = new ArrayList<NodeModel>();
    
    private NodeModel currentModel;
    
    private PropertyModelId propertyModel;
    
    private Stack<NodeModel> current = new Stack<NodeModel>();

    public ModelBuilderNodeVisitor(PropertyModelId propertyModel) {
        this.propertyModel = propertyModel;
    }

    public Collection<NodeModel> getModels() {
        return models;
    }
    
    public <T> Collection<T> getModels(Class<T> type) {
        Collection<T> tmodels = new ArrayList<T>();
        for(NodeModel m : getModels()) {
            if(type.isAssignableFrom(m.getClass())) {
                tmodels.add(type.cast(m));
            }
        }
        return tmodels;
    }

    static String getModelClassNameForNodeName(String nodeName) {
        if (nodeName.length() == 0) {
            throw new IllegalArgumentException("Node name cannot be empty.");
        }

        StringBuilder sb = new StringBuilder();
        boolean nextCharToUpper = true;
        for (int i = 0; i < nodeName.length(); i++) {
            char c = nodeName.charAt(i);

            if (i == 0 && ( c == '@' || c == '!')) {
                //skip
                continue;
            }

            if (c == '-' || c == '_') {
                //remove the dash and underscore and make to following character uppercase
                nextCharToUpper = true;
            } else {
                if (nextCharToUpper) {
                    nextCharToUpper = false;
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();

    }

    private Class getModelClass(String modelClassName) {
        for (Class clazz : propertyModel.getModelClasses()) {
            if (clazz.getSimpleName().equals(modelClassName)) {
                return clazz;
            }
        }
        return null;
    }

    @Override
    public boolean visit(Node node) {
        //assuming model class elements cannot nest
        String nodeName = node.name();
        if(nodeName == null) {
            throw new IllegalArgumentException(String.format("Node %s has no name!", node.toString()));
        }
        
        String modelClassName = getModelClassNameForNodeName(nodeName);
        
        System.out.println("model class for node " + node + ": " + modelClassName);
        
        if (current.isEmpty()) {
            //first try custom model 
            currentModel = createModelInstance(node);
            
            
            //if no model created then use the class mechanism
            if(currentModel == null) {
                Class modelClass = getModelClass(modelClassName);
                if (modelClass != null) {
                    currentModel = createModelInstance(modelClass, node);
                }
            }
            
            if(currentModel != null) {
                current.push(currentModel);
                models.add(currentModel);
            }
            
            return true;
        } else {
            //we've already created the model so now lets fill it with some data
            return handleNode(modelClassName, node);
        }
    }

    @Override
    public void unvisit(Node node) {
        if (!current.isEmpty()) {
            current.pop();
        }
    }

    private boolean handleNode(String modelClassName, Node node) {
        //first try the custom models
        NodeModel nmodel = CustomModelFactory.Query.createModel(node);
        if (nmodel != null) {
            try {
                current.peek().setSubmodel(modelClassName, nmodel);
                current.push(nmodel);
            } catch (NoSuchFieldException ex) {
                //no-op
            } catch (IllegalArgumentException ex) {
                //no-op
            } catch (IllegalAccessException ex) {
                //no-op
            }
        } else {
            //1. find the corresponding field in the current node
            Class<? extends NodeModel> currentModelClass = current.peek().getClass();
            String submodelFieldName = NodeModel.getSubmodelFieldName(modelClassName);
            Class<?> constructorArgumentClass;
            Class<?> fieldType;
            if (node instanceof Node.ResolvedTokenNode) {
                constructorArgumentClass = Node.ResolvedTokenNode.class;
                //do not derive the model class type from the field type since there 
                //doesn't have to be any for the token nodes
                fieldType = TokenNodeModel.class;

            } else if (node instanceof Node.GroupNodeImpl) {
                constructorArgumentClass = Node.class;
                //get the model type from the field type
                try {
                    Field field = currentModelClass.getField(submodelFieldName);
                    fieldType = field.getType();
                } catch (NoSuchFieldException nsfe) {
                    //no such field in the class, lets use the modelClassName as the class name
                    fieldType = current.peek().getModelClassForSubNode(node.name());
                    if (fieldType == null) {
                        //no type info provided, give up
                        String msg = String.format(
                                "Processing node %s: Neither public field %s found in the model class %s "
                                + "nor the class provides a class for the node name %s", node, submodelFieldName, currentModelClass, node.name());
                        System.err.println(msg);
                        current.peek().setUnhandledChild(node);

                        return false; //do not process children nodes of the given node

                    }
                }
            } else {
                throw new IllegalStateException();
            }

            try {
                Constructor<?> constructor = fieldType.getConstructor(constructorArgumentClass);
                NodeModel modelInstance = (NodeModel) constructor.newInstance(node);

                current.peek().setSubmodel(modelClassName, modelInstance);

                current.push(modelInstance);

            } catch (Exception e) {
                /*
                 * NoSuchMethodException, InstantiationException,
                 * IllegalAccessException, InvocationTargetException,
                 * SecurityException
                 */
                throw new RuntimeException(e);

            }

        }

        return true; //proceede with the children nodes
    }

    private NodeModel createModelInstance(Node node) {
        //try custom model factories first
        return CustomModelFactory.Query.createModel(node);
    }

    private NodeModel createModelInstance(Class modelClass, Node node) {
        //custom model not create, use the class based model loading mechanism
        try {
            Constructor<NodeModel> constructor = modelClass.getConstructor(Node.class);
            return constructor.newInstance(node);
        } catch (Exception /*
                 * InstantiationException | IllegalAccessException |
                 * IllegalArgumentException | InvocationTargetException |
                 * NoSuchMethodException | SecurityException
                 */ ex) {
            throw new IllegalStateException(String.format("Cannot create an instance of class %s "
                    + "by invoking its constructor with Node argument", modelClass.getName()), ex);
        }
    }
}
