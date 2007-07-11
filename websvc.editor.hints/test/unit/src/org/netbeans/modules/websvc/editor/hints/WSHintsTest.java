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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.editor.hints;

import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.rules.*;

/**
 *
 * @author Ajit
 */
public class WSHintsTest extends WSHintsTestBase {
    
    public WSHintsTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(WSHintsTest.class);
        return suite;
    }
    
    // class level hints
    public final void testDefaultPackage() throws IOException {
        Rule<TypeElement> instance = new DefaultPackage();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testHandlerChainAndSoapMessageHandlers() throws IOException {
        Rule<TypeElement> instance = new HandlerChainAndSoapMessageHandlers();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInterfaceEndpointInterface() throws IOException {
        Rule<TypeElement> instance = new InterfaceEndpointInterface();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInterfaceServiceName() throws IOException {
        Rule<TypeElement> instance = new InterfaceServiceName();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidJSRAnnotations() throws IOException {
        Rule<TypeElement> instance = new InvalidJSRAnnotations();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidNameAttribute() throws IOException {
        Rule<TypeElement> instance = new InvalidNameAttribute();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testRPCStyleWrappedParameterStyle() throws IOException {
        Rule<TypeElement> instance = new RPCStyleWrappedParameterStyle();
        getRulesEngine().getClassRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    // operation level hints
    public final void testOnewayOperationReturnType() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationReturnType();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testOnewayOperationExceptions() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationExceptions();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testOnewayOperationParameterMode() throws IOException {
        Rule<ExecutableElement> instance = new OnewayOperationParameterMode();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    public final void testInvalidExcludeAttribute() throws IOException {
        Rule<ExecutableElement> instance = new InvalidExcludeAttribute();
        getRulesEngine().getOperationRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }

    // parameter level hints
    public final void testWebParamHolder() throws IOException {
        Rule<VariableElement> instance = new WebParamHolder();
        getRulesEngine().getParameterRules().add(instance);
        testRule(instance,instance.getClass().getSimpleName().concat("Test.java"));
    }
}