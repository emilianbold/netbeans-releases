/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.codestructure;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class representing a code structure of one form.
 *
 * @author Tomas Pavek
 */

public class CodeStructure {

    public static final CodeElement[] EMPTY_PARAMS = new CodeElement[0];

    private Map namesToVariables = new HashMap(50);
    private Map elementsToVariables = new HashMap(50);

    // -------
    // elements

    // creates a new element from a constructor
    public CodeElement createElement(Constructor ctor, CodeElement[] params) {
        CodeElementOrigin origin =
                            new CodeSupport.ConstructorOrigin(ctor, params);
        return new DefaultCodeElement(this, origin);
    }

    // creates a new element from a method
    public CodeElement createElement(CodeElement parent,
                                     Method method,
                                     CodeElement[] params)
    {
        CodeElementOrigin origin =
                        new CodeSupport.MethodOrigin(parent, method, params);
        return new DefaultCodeElement(this, /*method.getReturnType(),*/ origin);
    }

    // creates a new element from a field
    public CodeElement createElement(CodeElement parent, Field field) {
        CodeElementOrigin origin = new CodeSupport.FieldOrigin(parent, field);
        return new DefaultCodeElement(this, /*field.getType(),*/ origin);
    }

    // creates a new element from a value
    public CodeElement createElement(Class type,
                                     Object value,
                                     String javaInitStr)
    {
        return new DefaultCodeElement(this, new CodeSupport.ValueOrigin(
                                                    type, value, javaInitStr));
    }

    // creates a new element of an arbitrary origin
    public CodeElement createElement(CodeElementOrigin origin) {
        return new DefaultCodeElement(this, origin);
    }

    // creates an element representing null value
    public CodeElement createNullElement(Class type) {
        return new DefaultCodeElement(this, new CodeSupport.ValueOrigin(
                                                    type, null, "null")); // NOI18N
    }

    // creates an element with no origin
    public CodeElement createDefaultElement() {
        return new DefaultCodeElement(this);
    }

    // removes element from the structure completely
    public static void removeElement(CodeElement element) {
        unregisterUsedCodeObject(element);
        unregisterUsingCodeObject(element);

        element.getCodeStructure().releaseVariable(element);
    }

    // --------
    // connections

    // creates a new method connection
    public static CodeConnection createConnection(CodeElement element,
                                                  Method m,
                                                  CodeElement[] params)
    {
        CodeConnection connection =
                         new CodeSupport.MethodConnection(element, m, params);
        registerUsingCodeObject(connection);
        return connection;
    }

    // creates a new field connection
    public static CodeConnection createConnection(CodeElement element,
                                                  Field f,
                                                  CodeElement assignEl)
    {
        CodeConnection connection =
                        new CodeSupport.FieldConnection(element, f, assignEl);
        registerUsingCodeObject(connection);
        return connection;
    }

    // creates a special connection for assigning element's variable
    static CodeConnection createVariableConnection(CodeElement element) {
        CodeConnection connection =
                         new CodeSupport.VariableAssignmentConnection(element);
        element.addUsingObject(
            connection, UsedCodeObject.DEFINING, CodeConnection.class);
        // [need to add as the first ??]
        return connection;
    }

    // removes connection from the structure completely
    public static void removeConnection(CodeConnection connection) {
        unregisterUsingCodeObject(connection);
    }

    public static void removeConnections(Iterator it) {
        List list = new ArrayList();
        while (it.hasNext())
            list.add(it.next());

        for (int i=0, n=list.size(); i < n; i++)
            unregisterUsingCodeObject((CodeConnection) list.get(i));
    }

    // returns Iterator of all connections of an element
    public static Iterator getConnectionsIterator(CodeElement element) {
        return element.getUsingObjectsIterator(UsedCodeObject.DEFINING,
                                               CodeConnection.class);
    }

    // returns all connections of an element in an array
    public static CodeConnection[] getConnections(CodeElement element) {
        ArrayList list = new ArrayList();
        Iterator it = getConnectionsIterator(element);
        while (it.hasNext())
            list.add(it.next());

        return (CodeConnection[]) list.toArray(new CodeConnection[list.size()]);
    }

    // returns all connections which use given (or equal) meta object
    public static CodeConnection[] getConnections(CodeElement element,
                                                  Object metaObject)
    {
        ArrayList list = new ArrayList();
        Iterator it = getConnectionsIterator(element);
        while (it.hasNext()) {
            CodeConnection connection = (CodeConnection) it.next();
            if (metaObject.equals(connection.getConnectingObject()))
                list.add(connection);
        }
        return (CodeConnection[]) list.toArray(new CodeConnection[list.size()]);
    }

    // --------
    // connection group

    public CodeConnectionGroup createConnectionGroup(/*CodeElement baseElement*/) {
        return new CodeSupport.DefaultConnectionGroup();
    }

    // --------
    // origins

    public static CodeElementOrigin createOrigin(Constructor ctor,
                                                 CodeElement[] params)
    {
        return new CodeSupport.ConstructorOrigin(ctor, params);
    }

    public static CodeElementOrigin createOrigin(CodeElement parent,
                                                 Method m,
                                                 CodeElement[] params)
    {
        return new CodeSupport.MethodOrigin(parent, m, params);
    }

    public static CodeElementOrigin createOrigin(CodeElement parent, Field f) {
        return new CodeSupport.FieldOrigin(parent, f);
    }

    public static CodeElementOrigin createOrigin(Class type,
                                                 Object value,
                                                 String javaStr)
    {
        return new CodeSupport.ValueOrigin(type, value, javaStr);
    }

    // -------
    // managing references between code objects

    // Registers usage of elements used by a connection.
    static void registerUsingCodeObject(CodeConnection connection) {
        CodeElement parent = connection.getParentElement();
        if (parent != null)
            parent.addUsingObject(
                connection, UsedCodeObject.DEFINING, CodeConnection.class);

        CodeElement[] params = connection.getConnectionParameters();
        if (params != null)
            for (int i=0; i < params.length; i++)
                params[i].addUsingObject(
                    connection, UsedCodeObject.USING, CodeConnection.class);
    }

    // Registers usage of elements used by the origin of an element.
    static void registerUsingCodeObject(CodeElement element) {
        CodeElementOrigin origin = element.getOrigin();
        CodeElement parent = origin.getParentElement();

        if (parent != null)
            parent.addUsingObject(
                element, UsedCodeObject.DEFINING, CodeElement.class);

        CodeElement[] params = origin.getCreationParameters();
        if (params != null)
            for (int i=0; i < params.length; i++)
                params[i].addUsingObject(
                    element, UsedCodeObject.USING, CodeElement.class);
    }

    // Unregisters usage of all object used by a using object.
    static void unregisterUsingCodeObject(UsingCodeObject usingObject) {
        Iterator it = usingObject.getUsedObjectsIterator();
        while (it.hasNext()) {
            UsedCodeObject usedObject = (UsedCodeObject) it.next();
            if (!usedObject.removeUsingObject(usingObject)) {
                // usedObject is no more used, so it should be removed
                if (usedObject instanceof UsingCodeObject)
                    unregisterUsingCodeObject((UsingCodeObject)usedObject);
            }
        }
    }

    // Unregisters usage of just one object used by a using object.
    static void unregisterObjectUsage(UsingCodeObject usingObject,
                                      UsedCodeObject usedObject)
    {
        if (!usedObject.removeUsingObject(usingObject)) {
            // usedObject is no more used, so it should be removed
            if (usedObject instanceof UsingCodeObject)
                unregisterUsingCodeObject((UsingCodeObject)usedObject);
        }
    }

    // This method just notifies all objects using given used object that
    // the used object is removed from the structure.
    static void unregisterUsedCodeObject(UsedCodeObject usedObject) {
        List usingObjects = new ArrayList();
        Iterator it = usedObject.getUsingObjectsIterator(0, null);
        while (it.hasNext())
            usingObjects.add(it.next());

        it = usingObjects.iterator();
        while (it.hasNext()) {
            UsingCodeObject usingObject = (UsingCodeObject) it.next();
            if (!usingObject.usedObjectRemoved(usedObject)) {
                // usingObject cannot exist without removed usedObject
                if (usingObject instanceof UsedCodeObject)
                    unregisterUsedCodeObject((UsedCodeObject)usingObject);
                unregisterUsingCodeObject(usingObject);
            }
        }
    }

    // -------
    // variables

    public CodeElementVariable createVariable(CodeElement element,
                                              int varType,
                                              String varName)
    {
        Variable var = (Variable) elementsToVariables.get(element);
        if (var != null) {
            var.type = varType;
            if (varName == null || var.name.equals(varName))
                return var;

            elementsToVariables.remove(element);
            namesToVariables.remove(var.name);
        }

        int n = 0;
        String baseName;
        if (varName != null) { // a valid name provided
            baseName = varName; // try it without a suffix first
        }
        else { // derive default name from class type, add "1" as suffix
            String typeName = element.getOrigin().getType().getName();
            int i = typeName.lastIndexOf('$');
            if (i < 0)
                i = typeName.lastIndexOf('.');
            baseName = Character.toLowerCase(typeName.charAt(i+1))
                       + typeName.substring(i+2);
            varName = baseName + (++n);
        }

        // find a free name
        while (namesToVariables.get(varName) != null)
            varName = baseName + (++n);

        if (var == null)
            var = new Variable(element, varType, varName,
                               createVariableConnection(element));
        else var.name = varName;

        namesToVariables.put(varName, var);
        elementsToVariables.put(element, var);

        return var;
    }

    public CodeElementVariable getVariable(CodeElement element) {
        return (Variable) elementsToVariables.get(element);
    }

//    public String getVariableName(CodeElement element) {
//        Variable var = (Variable) elementsToVariables.get(element);
//        return var != null ? var.name : null;
//    }

//    public int getVariableType(CodeElement element) {
//        Variable var = (Variable) elementsToVariables.get(element);
//        return var != null ? var.type : 0;
//    }

//    public CodeConnection getVariableConnection(CodeElement element) {
//        Variable var = (Variable) elementsToVariables.get(element);
//        return var != null ? var.assignmentConnection : null;
//    }

    public boolean isVariableNameReserved(String name) {
        return namesToVariables.get(name) != null;
    }

    public CodeElementVariable releaseVariable(String name) {
        Variable var = (Variable) namesToVariables.remove(name);
        if (var != null) {
            removeConnection(var.assignmentConnection);
            elementsToVariables.remove(var.element);
        }
        return var;
    }

    public CodeElementVariable releaseVariable(CodeElement element) {
        Variable var = (Variable) elementsToVariables.remove(element);
        if (var != null) {
            removeConnection(var.assignmentConnection);
            namesToVariables.remove(var.name);
        }
        return var;
    }

    protected Map getNamesToVariablesMap() {
        return namesToVariables;
    }

    protected Map getElementsToVariables() {
        return elementsToVariables;
    }

    final class Variable implements CodeElementVariable {
        private CodeElement element;
        private int type;
        private String name;
        private CodeConnection assignmentConnection;

        Variable(CodeElement element,
                 int type, String name,
                 CodeConnection assignmentConnection)
        {
            this.element = element;
            this.type = type;
            this.name = name;
            this.assignmentConnection = assignmentConnection;
        }

        public CodeElement getCodeElement() {
            return element;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public CodeConnection getAssignmentConnection() {
            return assignmentConnection;
        }
    }
}
