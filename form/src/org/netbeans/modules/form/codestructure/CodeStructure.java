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

    private static UsingCodeObject globalUsingObject;

    private Map namesToVariables = new HashMap(50);
    private Map elementsToVariables = new HashMap(50);

    private int defaultVariableAccessType = CodeElementVariable.PRIVATE;

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

    // prevents element from being removed automatically from structure when
    // not used (by any UsingCodeObject)
    public void registerElement(CodeElement element) {
        if (globalUsingObject == null)
            globalUsingObject = new GlobalUsingObject();

        element.addUsingObject(globalUsingObject,
                               UsedCodeObject.USING,
                               CodeStructure.class);
    }

    // removes element from the structure completely
    public static void removeElement(CodeElement element) {
        unregisterUsedCodeObject(element);
        unregisterUsingCodeObject(element);

        element.getCodeStructure().removeElementUsingVariable(element);
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

    private static class GlobalUsingObject implements UsingCodeObject {
        public void usageRegistered(UsedCodeObject usedObject) {
        }
        public boolean usedObjectRemoved(UsedCodeObject usedObject) {
            return true;
        }
        public Iterator getUsedObjectsIterator() {
            return null;
        }
    }

    // -------
    // variables

    public CodeElementVariable createVariable(int varType,
                                              Class declaredType,
                                              String varName)
    {
        if (getVariable(varName) != null)
            return null; // variable already exists, cannot create new one

        if (varType < 0 || varName == null)
            throw new IllegalArgumentException();

        CodeElementVariable var = new Variable(varType, declaredType, varName);
        namesToVariables.put(varName, var);
        return var;
    }

    public boolean renameVariable(String oldName, String newName) {
        Variable var = (Variable) namesToVariables.get(oldName);
        if (var == null || newName == null
                || newName.equals(var.getName())
                || namesToVariables.get(newName) != null)
            return false;

        namesToVariables.remove(oldName);
        var.name = newName;
        namesToVariables.put(newName, var);

        return true;
    }

    public CodeElementVariable releaseVariable(String name) {
        Variable var = (Variable) namesToVariables.remove(name);
        if (var == null)
            return null; // there is no such variable

        Map elementsMap = var.elementsMap;
        if (elementsMap == null)
            return var;

        Iterator it = elementsMap.values().iterator();
        while (it.hasNext())
            elementsToVariables.remove(it.next());

        return var;
    }

    public boolean isVariableNameReserved(String name) {
        return namesToVariables.get(name) != null;
    }

    public CodeElementVariable createVariableForElement(CodeElement element,
                                                        int varType,
                                                        String varName)
    {
        if (element == null)
            throw new IllegalArgumentException();

        if (getVariable(element) != null)
            return null; // variable already exists, cannot create new one

        if (varType < 0)
            throw new IllegalArgumentException();

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

        Variable var = new Variable(varType,
                                    element.getOrigin().getType(),
                                    varName);
        var.addCodeElement(element,
                           createVariableAssignment(var, element));

        namesToVariables.put(varName, var);
        elementsToVariables.put(element, var);

        return var;
    }

    public void addElementUsingVariable(CodeElementVariable var,
                                        CodeElement element)
    {
        if (element == null)
            return;
        // [should we check also element type ??]

        if (var.getAssignment(element) != null)
            return; // element already attached

        // check if this variable can have multiple expressions attached
        int mask = CodeElementVariable.LOCAL
                   | CodeElementVariable.EXPLICIT_DECLARATION;
        if ((var.getType() & mask) == CodeElementVariable.LOCAL
             && var.getAttachedElements().size() > 0)
        {
            // local variable without a standalone declaration can be used
            // only for one expression
            throw new IllegalStateException(
                      "Standalone local variable declaration required"); // NOI18N
        }

        ((Variable)var).addCodeElement(element,
                                       createVariableAssignment(var, element));

        elementsToVariables.put(element, var);
    }

    public void removeElementUsingVariable(CodeElement element) {
        if (element == null)
            return;

        Variable var = (Variable) elementsToVariables.remove(element);
        if (var == null)
            return;

        var.removeCodeElement(element);
    }

    public CodeElementVariable getVariable(String name) {
        return (Variable) namesToVariables.get(name);
    }

    public CodeElementVariable getVariable(CodeElement element) {
        return (Variable) elementsToVariables.get(element);
    }

    public Iterator getVariablesIterator(int type, int typeMask,
                                         Class declaredType)
    {
        return new VariablesIterator(type, typeMask, declaredType);
    }

    public Collection getAllVariables() {
        return Collections.unmodifiableCollection(namesToVariables.values());
    }

    // ---------

    protected Map getNamesToVariablesMap() {
        return namesToVariables;
    }

    protected Map getElementsToVariables() {
        return elementsToVariables;
    }

    private CodeConnection createVariableAssignment(CodeElementVariable var,
                                                    CodeElement element)
    {
        CodeConnection connection =
            new CodeSupport.AssignVariableConnection(var, element);

        // important: assignment connection does not register usage of code
        // elements (assigned element, parameters) - so it does not hold
        // the elements in the structure

        return connection;
    }

    // --------
    // inner classes

    final class Variable implements CodeElementVariable {
        private int type;
        private Class declaredType;
        private String name;
        private Map elementsMap;
        private CodeConnection declarationConnection;

        Variable(int type, Class declaredType, String name) {
            this.type = type;
            this.declaredType = declaredType;
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Class getDeclaredType() {
            return declaredType;
        }

        public Collection getAttachedElements() {
            return elementsMap != null ?
                     Collections.unmodifiableCollection(elementsMap.values()) :
                     Collections.EMPTY_LIST;
        }

        public CodeConnection getDeclaration() {
            if (declarationConnection == null)
                declarationConnection =
                    new CodeSupport.DeclareVariableConnection(this);
            return declarationConnection;
        }

        public CodeConnection getAssignment(CodeElement element) {
            return elementsMap != null ?
                   (CodeConnection) elementsMap.get(element) : null;
        }

        // -------

        void addCodeElement(CodeElement element, CodeConnection connection) {
            if (elementsMap == null)
                elementsMap = new HashMap();
            elementsMap.put(element, connection);
        }

        void removeCodeElement(CodeElement element) {
            if (elementsMap != null)
                elementsMap.remove(element);
        }

        int getDefaultAccessType() {
            return defaultVariableAccessType;
        }
    }

    final class VariablesIterator implements Iterator {
        private int type;
        private int typeMask;
        private Class declaredType;

        private Iterator subIterator;

        private CodeElementVariable currentVar;

        public VariablesIterator(int type, int typeMask, Class declaredType) {
            this.type = type;
            this.typeMask = typeMask;
            this.declaredType = declaredType;

            subIterator = namesToVariables.values().iterator();
        }

        public boolean hasNext() {
            if (currentVar != null)
                return true;

            while (subIterator.hasNext()) {
                CodeElementVariable var = (CodeElementVariable) subIterator.next();
                if ((type < 0
                        || (type & typeMask) == (var.getType() & typeMask))
                    &&
                    (declaredType == null
                        || declaredType.equals(var.getDeclaredType())))
                {
                    currentVar = var;
                    return true;
                }
            }

            return false;
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();

            CodeElementVariable var = currentVar;
            currentVar = null;
            return var;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
