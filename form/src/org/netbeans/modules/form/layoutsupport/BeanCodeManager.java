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
    private CodeExpression[] propertyExpressions;

    private int creationStyle;
    private boolean forceEmptyConstructor;

    private CreationDescriptor creationDesc;
    private CreationDescriptor.Creator currentCreator;

    private CodeStructure codeStructure;

    private CodeExpression beanExpression;
    private CodeGroup beanCode;

    private boolean isVariableSet;
    private int variableType;

    private boolean readingDone;

    // constructor for a new expression
    public BeanCodeManager(Class beanClass,
                           FormProperty[] beanProperties,
                           int creationStyle,
                           boolean forceEmptyCtor,
                           CodeStructure codeStructure,
                           int defaultVariableType,
                           CodeGroup beanCode)
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

        beanExpression = codeStructure.createDefaultExpression();

        readingDone = true;
        updateCode();
    }

    // constructor for reading the expression from code
    public BeanCodeManager(Class beanClass,
                           FormProperty[] beanProperties,
                           int creationStyle,
                           boolean forceEmptyCtor,
                           boolean allowChangesFiring,
                           CodeExpression beanExpression,
                           CodeGroup beanCode)
    {
        this.beanClass = beanClass;
        this.properties = beanProperties;
        this.creationStyle = creationStyle | CreationDescriptor.CHANGED_ONLY;
        this.forceEmptyConstructor = forceEmptyCtor;
        this.beanExpression = beanExpression;
        this.codeStructure = beanExpression.getCodeStructure();
        this.beanCode = beanCode;

        readingDone = false;

        CodeVariable var = beanExpression.getVariable();
        CodeStatement variableStatement = var != null ?
                                      var.getAssignment(beanExpression) : null;

        isVariableSet = variableStatement != null;
        variableType = var != null ? var.getType() : 0;

        // find creation descriptor
        creationDesc = CreationFactory.getDescriptor(beanClass);
        if (creationDesc != null) {
            // find creator, read creation code
            CodeExpression creationExpressions[] =
                beanExpression.getOrigin().getCreationParameters();
            Class[] paramTypes = new Class[creationExpressions.length];
            for (int i=0; i < creationExpressions.length; i++)
                paramTypes[i] = creationExpressions[i].getOrigin().getType();

            currentCreator = CreationFactory.findCreator(creationDesc, paramTypes);

            if (currentCreator != null) {
                String[] creatorPropNames = currentCreator.getPropertyNames();
                for (int i=0; i < creatorPropNames.length; i++) {
                    String propName = creatorPropNames[i];
                    for (int j=0; j < properties.length; j++)
                        if (properties[j].getName().equals(propName)) {
                            FormCodeSupport.readPropertyExpression(
                                                creationExpressions[i],
                                                properties[j],
                                                allowChangesFiring);
                            setPropertyExpression(j, creationExpressions[i]);
                            break;
                        }
                }
                beanExpression.setOrigin(
                    currentCreator.getCodeOrigin(creationExpressions));
            }
        }

        // read properties code
        Iterator it = CodeStructure.getStatementsIterator(beanExpression);
        while (it.hasNext()) {
            CodeStatement statement = (CodeStatement) it.next();
            for (int j=0; j < properties.length; j++) {
                FormProperty prop = properties[j];
                if (prop instanceof RADProperty) {
                    Method propMethod = ((RADProperty)prop)
                                .getPropertyDescriptor().getWriteMethod();
                    if (propMethod.equals(statement.getMetaObject())) {
                        CodeExpression propExp =
                            statement.getStatementParameters()[0];
                        FormCodeSupport.readPropertyExpression(
                                            propExp,
                                            prop,
                                            allowChangesFiring);
                        setPropertyExpression(j, propExp);
                        if (beanCode != null)
                            beanCode.addStatement(statement);
                        break;
                    }
                }
            }
        }

        if (beanCode != null && variableStatement != null)
            beanCode.addStatement(0, variableStatement);
    }

    public CodeExpression getCodeExpression() {
        return beanExpression;
    }

    // creates origin and statements according to state of properties
    public void updateCode() {
        if (!readingDone)
            return; // avoid interacting with reading

        CreationDescriptor.Creator newCreator =
            creationDesc != null && !forceEmptyConstructor ?
                creationDesc.findBestCreator(properties, creationStyle) :
                null;

        String[] creatorPropNames;
        CodeExpression[] creationExpressions;
        if (newCreator != null) {
            creatorPropNames = newCreator.getPropertyNames();
            creationExpressions =
                new CodeExpression[newCreator.getParameterCount()];
        }
        else {
            creatorPropNames = null;
            creationExpressions = CodeStructure.EMPTY_PARAMS;
        }

        boolean anyPropertyStatement = false;

        for (int i=0; i < properties.length; i++) {
            FormProperty property = properties[i];
            boolean removeStatement = !property.isChanged();

            if (newCreator != null) {
                String propName = property.getName();
                for (int j=0; j < creatorPropNames.length; j++)
                    if (creatorPropNames[j].equals(propName)) {
                        creationExpressions[j] = getPropertyExpression(i);
                        removeStatement = true;
                        break;
                    }
            }

            if (!(property instanceof RADProperty))
                continue;

            Method statementMethod = ((RADProperty)property)
                             .getPropertyDescriptor().getWriteMethod();
            CodeStatement[] existingStatements = CodeStructure
                             .getStatements(beanExpression, statementMethod);

            if (removeStatement) {
                for (int j=0; j < existingStatements.length; j++) {
                    CodeStatement toRemove = existingStatements[j];
                    CodeStructure.removeStatement(toRemove);
                    if (beanCode != null)
                        beanCode.remove(toRemove);
                }
            }
            else {
                anyPropertyStatement = true;
                if (existingStatements.length == 0) {
                    CodeStatement statement =
                        CodeStructure.createStatement(
                            beanExpression,
                            statementMethod,
                            new CodeExpression[] { getPropertyExpression(i) });

                    if (beanCode != null)
                        beanCode.addStatement(statement);
                }
            }
        }

        if (newCreator != null) {
            if (newCreator != currentCreator) { // creator has changed
                currentCreator = newCreator;
                beanExpression.setOrigin(newCreator.getCodeOrigin(
                                                        creationExpressions));
            }
        }
        else if (newCreator != currentCreator
                 || beanExpression.getOrigin() == null)
        {
            currentCreator = null;

            CodeExpressionOrigin origin = null;
            try { // use empty constructor
                Constructor ctor = beanClass.getConstructor(new Class[0]);
                origin = CodeStructure.createOrigin(ctor, new CodeExpression[0]);
            }
            catch (NoSuchMethodException ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    System.out.println("[WARNING] No default constructor for " // NOI18N
                                       + beanClass.getName());
                    ex.printStackTrace();
                    return;
                }
            }
            beanExpression.setOrigin(origin);
        }

        if (anyPropertyStatement) {
            if (!isVariableSet) {
                CodeVariable var =
                    codeStructure.createVariableForExpression(
                                      beanExpression, variableType, null);
                if (beanCode != null) {
                    beanCode.addStatement(0, var.getAssignment(beanExpression));
                }
                isVariableSet = true;
            }
        }
        else if (isVariableSet) {
            CodeVariable var = beanExpression.getVariable();
            if (var != null) {
                if (beanCode != null)
                    beanCode.remove(var.getAssignment(beanExpression));
                variableType = var.getType();
                codeStructure.removeExpressionUsingVariable(beanExpression);
            }
            isVariableSet = false;
        }
    }

    private CodeExpression getPropertyExpression(int index) {
        if (propertyExpressions == null)
            // we suppose the bean has a constant set of properties
            propertyExpressions = new CodeExpression[properties.length];

        CodeExpression expression = propertyExpressions[index];
        if (expression == null) {
            FormProperty prop = properties[index];
            expression = codeStructure.createExpression(
                                         FormCodeSupport.createOrigin(prop));
            propertyExpressions[index] = expression;
        }

        return expression;
    }

    private void setPropertyExpression(int index, CodeExpression propExp) {
        if (propertyExpressions == null)
            propertyExpressions = new CodeExpression[properties.length];
        propertyExpressions[index] = propExp;
    }
}
