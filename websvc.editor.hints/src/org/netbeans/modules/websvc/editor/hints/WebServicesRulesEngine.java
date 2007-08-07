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

package org.netbeans.modules.websvc.editor.hints;

import java.util.Collection;
import java.util.LinkedList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.modules.websvc.editor.hints.common.Rule;
import org.netbeans.modules.websvc.editor.hints.common.RulesEngine;
import org.netbeans.modules.websvc.editor.hints.rules.*;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 */
public class WebServicesRulesEngine extends RulesEngine {
    private static final LinkedList<Rule<TypeElement>> classRules = new LinkedList<Rule<TypeElement>>();
    private static final LinkedList<Rule<ExecutableElement>> operationRules = new LinkedList<Rule<ExecutableElement>>();
    private static final LinkedList<Rule<VariableElement>> paramRules = new LinkedList<Rule<VariableElement>>();
    
    static{
        //class rules
        classRules.add(new NoOperations());
        classRules.add(new InvalidJSRAnnotations());
        classRules.add(new InvalidNameAttribute());
        classRules.add(new DefaultPackage());
        classRules.add(new InterfaceServiceName());
        classRules.add(new InterfaceEndpointInterface());
        classRules.add(new HandlerChainAndSoapMessageHandlers());
        classRules.add(new RPCStyleWrappedParameterStyle());
        //operation rules
        operationRules.add(new OnewayOperationReturnType());
        operationRules.add(new OnewayOperationParameterMode());
        operationRules.add(new OnewayOperationExceptions());
        operationRules.add(new InvalidExcludeAttribute());
        //parameters rules
        paramRules.add(new WebParamHolder());
    }
    
    protected Collection<Rule<TypeElement>> getClassRules() {
        return classRules;
    }

    protected Collection<Rule<ExecutableElement>> getOperationRules() {
        return operationRules;
    }

    protected Collection<Rule<VariableElement>> getParameterRules() {
        return paramRules;
    }

}
