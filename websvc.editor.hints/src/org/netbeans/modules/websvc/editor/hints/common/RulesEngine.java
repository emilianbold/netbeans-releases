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

package org.netbeans.modules.websvc.editor.hints.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor6;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class RulesEngine extends ElementKindVisitor6<Void, ProblemContext> {
    private ProblemContext ctx;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    
    @Override public Void visitTypeAsClass(TypeElement javaClass, ProblemContext ctx){
        // apply class-level rules
        for (Rule<TypeElement> rule : getClassRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(javaClass, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    if (problem != null){
                        problemsFound.add(problem);
                    }
                }
            }
        }
        
        // visit all enclosed elements
        for (Element enclosedClass : javaClass.getEnclosedElements()){
            enclosedClass.accept(this, ctx);
        }
        
        return null;
    }
    
    @Override public Void visitTypeAsInterface(TypeElement javaClass, ProblemContext ctx){
        return visitTypeAsClass(javaClass,ctx);
    }
    
    @Override public Void visitExecutableAsMethod(ExecutableElement operation, ProblemContext ctx){
        // apply operation-level rules
        for (Rule<ExecutableElement> rule : getOperationRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(operation, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    if (problem != null){
                        problemsFound.add(problem);
                    }
                }
            }
        }
        
         // visit all parameters
        for (VariableElement parameter : operation.getParameters()){
            parameter.accept(this, ctx);
        }
        
       return null;
    }
    
    @Override public Void visitVariableAsParameter(VariableElement parameter, ProblemContext ctx){
        // apply parameter-level rules
        for (Rule<VariableElement> rule : getParameterRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(parameter, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    if (problem != null){
                        problemsFound.add(problem);
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    protected abstract Collection<Rule<TypeElement>> getClassRules();
    protected abstract Collection<Rule<ExecutableElement>> getOperationRules();
    protected abstract Collection<Rule<VariableElement>> getParameterRules();
}
