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
package org.netbeans.modules.j2ee.ejbverification.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.modules.j2ee.ejbverification.fixes.ExposeBusinessMethod;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * Offer a hint to expose a method in business interface
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class BusinessMethodExposed extends EJBVerificationRule {

    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        if (ctx.getEjb() instanceof Session) {
            Session session = (Session) ctx.getEjb();
            Collection<TypeElement> localInterfaces = new ArrayList<TypeElement>();
            Collection<TypeElement> remoteInterfaces = new ArrayList<TypeElement>();
            
            try {
                localInterfaces.addAll(resolveClasses(ctx.getComplilationInfo(),
                        session.getBusinessLocal()));
                
                remoteInterfaces.addAll(resolveClasses(ctx.getComplilationInfo(),
                        session.getBusinessRemote()));
                
            } catch (VersionNotSupportedException e) {
                assert false;
            }
            
            Collection<ExecutableElement> definedMethods = new ArrayList<ExecutableElement>();
            
            for (TypeElement iface : localInterfaces){
                definedMethods.addAll(ElementFilter.methodsIn(iface.getEnclosedElements()));
            }
            
            for (TypeElement iface : remoteInterfaces){
                definedMethods.addAll(ElementFilter.methodsIn(iface.getEnclosedElements()));
            }
            
            Map<String, ExecutableElement> definedMethodsByName = new HashMap<String, ExecutableElement>();
            
            for (ExecutableElement method : definedMethods){
                definedMethodsByName.put(method.getSimpleName().toString(), method);
            }
            
            // ----
            
            for (ExecutableElement method : ElementFilter.methodsIn(ctx.getClazz().getEnclosedElements())){
                if (isEligibleMethod(method)){
                    ExecutableElement potentialMatch = definedMethodsByName.get(method.getSimpleName().toString());
                    
                    if (potentialMatch != null && JavaUtils.isMethodSignatureSame(ctx.getComplilationInfo(),
                            method, potentialMatch)){
                        continue;
                    }
                    
                    ArrayList<Fix> fixes = new ArrayList<Fix>();
                    
                    for (TypeElement iface : localInterfaces){
                        Fix fix = new ExposeBusinessMethod(
                                ctx.getFileObject(),
                                ElementHandle.create(iface),
                                ElementHandle.create(method),
                                true);
                        
                        fixes.add(fix);
                    }
                    
                    for (TypeElement iface : remoteInterfaces){
                        Fix fix = new ExposeBusinessMethod(
                                ctx.getFileObject(),
                                ElementHandle.create(iface),
                                ElementHandle.create(method),
                                false);
                        
                        fixes.add(fix);
                    }
                    
                    ErrorDescription err = HintsUtils.createProblem(method, ctx.getComplilationInfo(),
                            NbBundle.getMessage(BusinessMethodExposed.class, "MSG_BusinessMethodExposed"),
                            Severity.HINT, fixes);
                    
                    return Collections.singletonList(err);
                }
            }
        }

        return null;
    }
    
    private Collection<TypeElement> resolveClasses(CompilationInfo info, String classNames[]){
        Collection<TypeElement> result = new ArrayList<TypeElement>();
        
        if (classNames != null) {
            for (String className : classNames) {
                TypeElement clazz = info.getElements().getTypeElement(className);

                if (clazz != null) {
                    result.add(clazz);
                }
            }
        }

        
        return result;
    }
    
    private boolean isEligibleMethod(ExecutableElement method){
        return method.getModifiers().contains(Modifier.PUBLIC) 
                && !method.getModifiers().contains(Modifier.STATIC);
    }
}
