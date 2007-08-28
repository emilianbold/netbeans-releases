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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemFinder;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * The invocation semantics of a remote business method is very different
 * from that of a local business method.
 * For this reason, when a session bean has remote as well as local business method,
 * there should not be any method common to both the interfaces.
 *
 * Example below is an incorrect use case:
 * Remote public interface I1 { void foo();},
 * Local public interface I2 { void foo();},
 * Stateless public class Foo implements I1, I2 { ... }
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class BMnotPartOfRBIandLBI extends EJBVerificationRule {
    
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        if (ctx.getEjb() instanceof Session){
            Session session = (Session) ctx.getEjb();
            
            Collection<ExecutableElement> localMethods = null;
            Map<String, ExecutableElement> remoteMethods = new HashMap<String, ExecutableElement>();
            
            try {
                localMethods = getMethodsFromClasses(ctx.getComplilationInfo(),
                        session.getBusinessLocal());
                
                for (ExecutableElement method : getMethodsFromClasses(ctx.getComplilationInfo(),
                        session.getBusinessRemote())){
                    
                    remoteMethods.put(method.getSimpleName().toString(), method);
                }
                
                for (ExecutableElement localMethod : localMethods){
                    ExecutableElement sameNameRemoteMethod = remoteMethods.get(
                            localMethod.getSimpleName().toString());
                    
                    if (sameNameRemoteMethod != null){
                        if (JavaUtils.isMethodSignatureSame(ctx.getComplilationInfo(),
                                localMethod, sameNameRemoteMethod)){
                            ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                                    NbBundle.getMessage(BMnotPartOfRBIandLBI.class, "MSG_BMnotPartOfRBIandLBI"),
                                    Severity.WARNING);
                            
                            return Collections.singletonList(err);
                        }
                    }
                }
                
            } catch (VersionNotSupportedException ex) {
                EJBProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
        return null;
    }
    
    private Collection<ExecutableElement> getMethodsFromClasses(
            CompilationInfo cinfo, String classNames[]){
        
        Collection<ExecutableElement> methods = new LinkedList<ExecutableElement>();
        
        if (classNames != null) {
            for (String className : classNames) {
                TypeElement clazz = cinfo.getElements().getTypeElement(className);

                if (clazz != null) {
                    methods.addAll(ElementFilter.methodsIn(clazz.getEnclosedElements()));
                }
            }
        }

        
        return methods;
    }
}
