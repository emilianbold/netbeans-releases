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

package org.netbeans.modules.form.layoutsupport;

import java.util.Iterator;
import java.lang.reflect.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

final class BeanCodeManager
{
    private Class beanClass;
    private FormProperty[] properties; // supposing bean has a constant set of properties
    private CodeElement[] propertyElements;

    private int creationStyle;
    private boolean forceEmptyConstructor;

    private CreationDescriptor creationDesc;
    private CreationDescriptor.Creator currentCreator;

    private CodeStructure codeStructure;

    private CodeElement beanElement;
    private CodeConnectionGroup beanCode;

    private boolean isVariableSet;
    private int variableType;

    private boolean readingDone;

    // constructor for a new element
    public BeanCodeManager(Class beanClass,
                           FormProperty[] beanProperties,
                           int creationStyle,
                           boolean forceEmptyCtor,
                           CodeStructure codeStructure,
                           int defaultVariableType,
                           CodeConnectionGroup beanCode)
    {
        this.beanClass = beanClass;
        this.properties = beanProperties;
        this.creationStyle = creationStyle | CreationDescriptor.CHANGED_ONLY;
        this.forceEmptyConstructor = forceEmptyCtor;
        this.codeStructure = codeStructure;
        this.variableType = defaultVariableType;
        this.beanCode = beanCode;

        isVariableSet = false;

        creationDesc = CreationFactory.getDescriptor(beanClass);

        beanElement = codeStructure.createDefaultElement();

        readingDone = true;
        updateCode();
    }

    // constructor for reading the element from code
    public BeanCodeManager(Class beanClass,
                           FormProperty[] beanProperties,
                           int creationStyle,
                           boolean forceEmptyCtor,
                           boolean allowChangesFiring,
                           CodeElement beanElement,
                           CodeConnectionGroup beanCode)
    {
        this.beanClass = beanClass;
        this.properties = beanProperties;
        this.creationStyle = creationStyle | CreationDescriptor.CHANGED_ONLY;
        this.forceEmptyConstructor = forceEmptyCtor;
        this.beanElement = beanElement;
        this.codeStructure = beanElement.getCodeStructure();
        this.beanCode = beanCode;

        readingDone = false;

        CodeElementVariable var = beanElement.getVariable();
        CodeConnection variableConnection =
                       var != null ? var.getAssignment(beanElement) : null;

        isVariableSet = variableConnection != null;
        variableType = var != null ? var.getType() : 0;

        // find creation descriptor
        creationDesc = CreationFactory.getDescriptor(beanClass);
        if (creationDesc != null) {
            // find creator, read creation code
            CodeElement creationElements[] =
                beanElement.getOrigin().getCreationParameters();
            Class[] paramTypes = new Class[creationElements.length];
            for (int i=0; i < creationElements.length; i++)
                paramTypes[i] = creationElements[i].getOrigin().getType();

            currentCreator = CreationFactory.findCreator(creationDesc, paramTypes);

            if (currentCreator != null) {
                String[] creatorPropNames = currentCreator.getPropertyNames();
                for (int i=0; i < creatorPropNames.length; i++) {
                    String propName = creatorPropNames[i];
                    for (int j=0; j < properties.length; j++)
                        if (properties[j].getName().equals(propName)) {
                            FormCodeSupport.readPropertyElement(
                                                creationElements[i],
                                                properties[j],
                                                allowChangesFiring);
                            setPropertyElement(j, creationElements[i]);
                            break;
                        }
                }
                beanElement.setOrigin(
                    currentCreator.getCodeOrigin(creationElements));
            }
        }

        // read properties code
        Iterator it = CodeStructure.getConnectionsIterator(beanElement);
        while (it.hasNext()) {
            CodeConnection connection = (CodeConnection) it.next();
            for (int j=0; j < properties.length; j++) {
                FormProperty prop = properties[j];
                if (prop instanceof RADProperty) {
                    Method propMethod = ((RADProperty)prop)
                                .getPropertyDescriptor().getWriteMethod();
                    if (propMethod.equals(connection.getConnectingObject())) {
                        CodeElement propElement =
                            connection.getConnectionParameters()[0];
                        FormCodeSupport.readPropertyElement(propElement,
                                                            prop,
                                                            allowChangesFiring);
                        setPropertyElement(j, propElement);
                        if (beanCode != null)
                            beanCode.addConnection(connection);
                        break;
                    }
                }
            }
        }

        if (beanCode != null && variableConnection != null)
            beanCode.addConnection(0, variableConnection);
    }

    public CodeElement getCodeElement() {
        return beanElement;
    }

    // creates origin and connections according to state of properties
    public void updateCode() {
        if (!readingDone)
            return; // avoid interacting with reading

        CreationDescriptor.Creator newCreator =
            creationDesc != null && !forceEmptyConstructor ?
                creationDesc.findBestCreator(properties, creationStyle) :
                null;

        String[] creatorPropNames;
        CodeElement[] creationElements;
        if (newCreator != null) {
            creatorPropNames = newCreator.getPropertyNames();
            creationElements = new CodeElement[newCreator.getParameterCount()];
        }
        else {
            creatorPropNames = null;
            creationElements = CodeStructure.EMPTY_PARAMS;
        }

        boolean anyPropertyConnection = false;

        for (int i=0; i < properties.length; i++) {
            FormProperty property = properties[i];
            boolean removeConnection = !property.isChanged();

            if (newCreator != null) {
                String propName = property.getName();
                for (int j=0; j < creatorPropNames.length; j++)
                    if (creatorPropNames[j].equals(propName)) {
                        creationElements[j] = getPropertyElement(i);
                        removeConnection = true;
                        break;
                    }
            }

            if (!(property instanceof RADProperty))
                continue;

            Method connectionMethod = ((RADProperty)property)
                             .getPropertyDescriptor().getWriteMethod();
            CodeConnection[] existingConnections = CodeStructure
                             .getConnections(beanElement, connectionMethod);

            if (removeConnection) {
                for (int j=0; j < existingConnections.length; j++) {
                    CodeConnection toRemove = existingConnections[j];
                    CodeStructure.removeConnection(toRemove);
                    if (beanCode != null)
                        beanCode.remove(toRemove);
                }
            }
            else {
                anyPropertyConnection = true;
                if (existingConnections.length == 0) {
                    CodeConnection connection =
                        CodeStructure.createConnection(
                            beanElement,
                            connectionMethod,
                            new CodeElement[] { getPropertyElement(i) });

                    if (beanCode != null)
                        beanCode.addConnection(connection);
                }
            }
        }

        if (newCreator != null) {
            if (newCreator != currentCreator) { // creator has changed
                currentCreator = newCreator;
                beanElement.setOrigin(newCreator.getCodeOrigin(creationElements));
            }
        }
        else if (newCreator != currentCreator
                 || beanElement.getOrigin() == null)
        {
            currentCreator = null;

            CodeElementOrigin origin = null;
            try { // use empty constructor
                Constructor ctor = beanClass.getConstructor(new Class[0]);
                origin = CodeStructure.createOrigin(ctor, new CodeElement[0]);
            }
            catch (NoSuchMethodException ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    System.out.println("[WARNING] No default constructor for " // NOI18N
                                       + beanClass.getName());
                    ex.printStackTrace();
                    return;
                }
            }
            beanElement.setOrigin(origin);
        }

        if (anyPropertyConnection) {
            if (!isVariableSet) {
                CodeElementVariable var =
                    codeStructure.createVariableForElement(
                                      beanElement, variableType, null);
                if (beanCode != null) {
                    beanCode.addConnection(0, var.getAssignment(beanElement));
                }
                isVariableSet = true;
            }
        }
        else if (isVariableSet) {
            CodeElementVariable var = beanElement.getVariable();
            if (var != null) {
                if (beanCode != null)
                    beanCode.remove(var.getAssignment(beanElement));
                variableType = var.getType();
                codeStructure.removeElementUsingVariable(beanElement);
            }
            isVariableSet = false;
        }
    }

    private CodeElement getPropertyElement(int index) {
        if (propertyElements == null)
            // we suppose the bean has a constant set of properties
            propertyElements = new CodeElement[properties.length];

        CodeElement element = propertyElements[index];
        if (element == null) {
            FormProperty prop = properties[index];
            element = codeStructure.createElement(
                                        FormCodeSupport.createOrigin(prop));
            propertyElements[index] = element;
        }

        return element;
    }

    private void setPropertyElement(int index, CodeElement propElement) {
        if (propertyElements == null)
            propertyElements = new CodeElement[properties.length];
        propertyElements[index] = propElement;
    }
}
