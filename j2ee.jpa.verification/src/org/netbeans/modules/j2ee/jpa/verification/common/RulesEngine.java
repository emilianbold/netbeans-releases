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

package org.netbeans.modules.j2ee.jpa.verification.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementKindVisitor6;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class RulesEngine extends ElementKindVisitor6<Void, ProblemContext> {
    private ProblemContext ctx;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();

    @Override public Void visitTypeAsClass(TypeElement javaClass, ProblemContext ctx){
        // visit all enclosed classes recursively
        for (TypeElement enclosedClass : ElementFilter.typesIn(javaClass.getEnclosedElements())){
            visitTypeAsClass(enclosedClass, ctx);
        }
        
        // apply class-level rules
        for (Rule<TypeElement> rule : getClassRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(javaClass, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    problemsFound.add(problem);
                }
            }
        }
        
        return null;
    }
    
    public List<ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    protected abstract Collection<Rule<TypeElement>> getClassRules();
}
