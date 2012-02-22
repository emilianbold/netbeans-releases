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
package org.netbeans.modules.css.lib.api.properties.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.lib.api.properties.NodeVisitor;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class ModelBuilderNodeVisitor<T extends NodeModel> implements NodeVisitor {

    private T model;
    private PropertyModelId propertyModel;
    private Stack<NodeModel> current = new Stack();

    public ModelBuilderNodeVisitor(PropertyModelId propertyModel) {
        this.propertyModel = propertyModel;
    }

    public T getModel() {
        return model;
    }

    static String getModelClassNameForNodeName(String nodeName) {
        if (nodeName == null) {
            throw new NullPointerException();
        }
        if (nodeName.length() == 0) {
            throw new IllegalArgumentException("Node name cannot be empty.");
        }

        StringBuilder sb = new StringBuilder();
        boolean nextCharToUpper = true;
        for (int i = 0; i < nodeName.length(); i++) {
            char c = nodeName.charAt(i);

            if (i == 0 && c == '@') {
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
    public void visit(Node node) {
        //assuming model class elements cannot nest
        String modelClassName = getModelClassNameForNodeName(node.getName());
        if (current.isEmpty()) {
            Class modelClass = getModelClass(modelClassName);
            if (modelClass != null) {
                createModelInstance(modelClass, node);
                current.push(this.model);
            }
        } else {
            //we've already created the model so now lets fill it with some data
            handleNode(modelClassName, node);
        }
    }

    @Override
    public void unvisit(Node node) {
        if (!current.isEmpty()) {
            current.pop();
        }
    }

    private void handleNode(String modelClassName, Node node) {
        //1. find the corresponding field in the current node
        String submodelFieldName = NodeModel.getSubmodelFieldName(modelClassName);
        Class<?> constructorArgumentClass;
        if (node instanceof Node.ResolvedTokenNode) {
            constructorArgumentClass = Node.ResolvedTokenNode.class;
        } else if (node instanceof Node.GroupNode) {
            constructorArgumentClass = Node.class;
        } else {
            throw new IllegalStateException();
        }

        Class<? extends NodeModel> currentModelClass = current.peek().getClass();
        try {
            Field field = currentModelClass.getField(submodelFieldName);
            Class<?> fieldType = field.getType();

            Constructor<?> constructor = fieldType.getConstructor(constructorArgumentClass);
            NodeModel modelInstance = (NodeModel) constructor.newInstance(node);

            current.peek().setSubmodel(modelClassName, modelInstance);

            current.push(modelInstance);

        } catch (NoSuchFieldException ex) {
            String msg = String.format("Processing node %s: No public field %s found in the model class %s", node, submodelFieldName, currentModelClass);
            throw new RuntimeException(msg, ex);
        } catch (Exception e) {
            /*
             * NoSuchMethodException, InstantiationException,
             * IllegalAccessException, InvocationTargetException,
             * SecurityException
             */
            throw new RuntimeException(e);

        }
    }

    private void createModelInstance(Class modelClass, Node node) {
        try {
            Constructor<T> constructor = modelClass.getConstructor(Node.class);
            this.model = constructor.newInstance(node);
        } catch (Exception /*
                 * InstantiationException | IllegalAccessException |
                 * IllegalArgumentException | InvocationTargetException |
                 * NoSuchMethodException | SecurityException
                 */ ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
