/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.wildfly.ide.commands;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import org.netbeans.modules.javaee.wildfly.WildFlyDeploymentFactory;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <emmanuel.hugonnet@gmail.com>
 */
public class WildflyManagementAPI {

    private static Map<String, Object> clientConstants;

    private static Map<String, Object> modelDescriptionConstants;

    static Object createClient(WildFlyDeploymentFactory.WildFlyClassLoader cl, final String serverAddress, final int serverPort,
            final CallbackHandler handler) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.ModelControllerClient$Factory"); // NOI18N
        Method method = clazz.getDeclaredMethod("create", String.class, int.class, CallbackHandler.class
        );
        return method.invoke(
                null, serverAddress, serverPort, handler);
    }

    static void closeClient(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object client) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method = client.getClass().getMethod("close", new Class[]{});
        method.invoke(client, (Object[]) null);
    }

    // ModelNode
    static Object createDeploymentPathAddressAsModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class paClazz = cl.loadClass("org.jboss.as.controller.PathAddress"); // NOI18N
        Class peClazz = cl.loadClass("org.jboss.as.controller.PathElement"); // NOI18N

        Method peFactory = peClazz.getDeclaredMethod("pathElement",// NOI18N
                name != null ? new Class[]{String.class, String.class} : new Class[]{String.class});
        Object pe = peFactory.invoke(null,
                name != null ? new Object[]{getClientConstant(cl, "DEPLOYMENT"), name} : new Object[]{getClientConstant(cl, "DEPLOYMENT")});// NOI18N

        Object array = Array.newInstance(peClazz, 1);
        Array.set(array, 0, pe);
        Method paFactory = paClazz.getDeclaredMethod("pathAddress", array.getClass()); // NOI18N
        Object pa = paFactory.invoke(null, array);

        Method toModelNode = pa.getClass().getMethod("toModelNode", (Class<?>[]) null); // NOI18N
        return toModelNode.invoke(pa, (Object[]) null);
    }

    // ModelNode
    static Object createPathAddressAsModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl, LinkedHashMap<Object, Object> elements)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class paClazz = cl.loadClass("org.jboss.as.controller.PathAddress"); // NOI18N
        Class peClazz = cl.loadClass("org.jboss.as.controller.PathElement"); // NOI18N

        Method peFactory = peClazz.getDeclaredMethod("pathElement", new Class[]{String.class, String.class});
        Object array = Array.newInstance(peClazz, elements.size());
        int i = 0;
        for (Map.Entry<Object, Object> entry : elements.entrySet()) {
            Array.set(array, i, peFactory.invoke(null, new Object[]{entry.getKey(), entry.getValue()}));
            i++;
        }

        Method paFactory = paClazz.getDeclaredMethod("pathAddress", array.getClass()); // NOI18N
        Object pa = paFactory.invoke(null, array);

        Method toModelNode = pa.getClass().getMethod("toModelNode", (Class<?>[]) null); // NOI18N
        return toModelNode.invoke(pa, (Object[]) null);
    }

    // ModelNode
    static Object createOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object name, Object modelNode)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createOperation", new Class[]{String.class, modelClazz});
        return method.invoke(null, name, modelNode);
    }

    // ModelNode
    static Object createReadResourceOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, boolean recursive)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createReadResourceOperation", new Class[]{modelClazz, boolean.class});
        return method.invoke(null, modelNode, recursive);
    }

    // ModelNode
    static Object createRemoveOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createRemoveOperation", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    static Object createAddOperation(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("createAddOperation", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    static Object readResult(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("readResult", new Class[]{modelClazz});
        return method.invoke(null, modelNode);
    }

    // ModelNode
    static Object getModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object name) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("get", String.class);
        return method.invoke(modelNode, name);
    }

    // ModelNode
    static Object getModelNodeChildAtIndex(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, int index) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("get", int.class);
        return method.invoke(modelNode, index);
    }

    // ModelNode
    static Object getModelNodeChildAtPath(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object[] path) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("get", String[].class);
        Object array = Array.newInstance(String.class, path.length);
        for (int i = 0; i < path.length; i++) {
            Array.set(array, i, path[i]);
        }
        return method.invoke(modelNode, array);
    }
    
    // ModelNode
    static boolean modelNodeHasChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, String child) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("has", String.class);
        return (Boolean) method.invoke(modelNode, child);
    }
    
    // ModelNode
    static boolean modelNodeHasDefinedChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, String child) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("hasDefined", String.class);
        return (Boolean) method.invoke(modelNode, child);
    }

    // ModelNode
    static Object createModelNode(WildFlyDeploymentFactory.WildFlyClassLoader cl) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        return modelClazz.newInstance();
    }

    // ModelNode
    static Object setModelNodeChildString(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        assert value != null;
        Method method = modelNode.getClass().getMethod("set", String.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        assert value != null;
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = modelNode.getClass().getMethod("set", modelClazz);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, int value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("set", int.class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object setModelNodeChildEmptyList(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Method method = modelNode.getClass().getMethod("setEmptyList", (Class<?>[]) null);
        return method.invoke(modelNode, (Object[]) null);
    }

    // ModelNode
    static Object setModelNodeChildBytes(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, byte[] value) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Method method = modelNode.getClass().getMethod("set", byte[].class);
        return method.invoke(modelNode, value);
    }

    // ModelNode
    static Object addModelNodeChild(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode, Object toAddModelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = modelNode.getClass().getMethod("add", modelClazz);
        return method.invoke(modelNode, toAddModelNode);
    }

    static boolean modelNodeIsDefined(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("isDefined", (Class<?>[]) null);
        return (Boolean) method.invoke(modelNode, (Object[]) null);
    }

    static String modelNodeAsString(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asString", (Class<?>[]) null);
        return (String) method.invoke(modelNode, (Object[]) null);
    }

    static String modelNodeAsPropertyForName(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asProperty", (Class<?>[]) null);
        Object property = method.invoke(modelNode, (Object[]) null);
        method = property.getClass().getMethod("getName", (Class<?>[]) null);
        return (String) method.invoke(property, (Object[]) null);
    }

    static Object modelNodeAsPropertyForValue(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asProperty", (Class<?>[]) null);
        Object property = method.invoke(modelNode, (Object[]) null);
        method = property.getClass().getMethod("getValue", (Class<?>[]) null);
        return method.invoke(property, (Object[]) null);
    }

    // List<ModelNode>
    static List modelNodeAsList(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asList", (Class<?>[]) null);
        return (List) method.invoke(modelNode, (Object[]) null);
    }
    
    static List modelNodeAsPropertyList(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Method method = modelNode.getClass().getMethod("asPropertyList()", (Class<?>[]) null);
        return (List) method.invoke(modelNode, (Object[]) null);
    }

    static boolean isSuccessfulOutcome(WildFlyDeploymentFactory.WildFlyClassLoader cl, Object modelNode) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.Operations"); // NOI18N
        Class modelClazz = cl.loadClass("org.jboss.dmr.ModelNode"); // NOI18N
        Method method = clazz.getDeclaredMethod("isSuccessfulOutcome", modelClazz);
        return (Boolean) method.invoke(null, modelNode);
    }

    static Object getClientConstant(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name) throws ClassNotFoundException, IllegalAccessException {
        if (clientConstants == null) {
            clientConstants = new HashMap<String, Object>();
            Class clazz = cl.loadClass("org.jboss.as.controller.client.helpers.ClientConstants"); // NOI18N
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                int modifiers = f.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    clientConstants.put(f.getName(), f.get(null));
                }
            }
        }
        return clientConstants.get(name);
    }

    static Object getModelDescriptionConstant(WildFlyDeploymentFactory.WildFlyClassLoader cl, String name) throws ClassNotFoundException, IllegalAccessException {
        if (modelDescriptionConstants == null) {
            modelDescriptionConstants = new HashMap<String, Object>();
            Class clazz = cl.loadClass("org.jboss.as.controller.descriptions.ModelDescriptionConstants"); // NOI18N
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                int modifiers = f.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    modelDescriptionConstants.put(f.getName(), f.get(null));
                }
            }
        }
        return modelDescriptionConstants.get(name);
    }
}
