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
import java.util.List;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * In EJB 3.0, it is recommended that a bean class implements its business interface.
 * That way, user does not have to worry about method matching,
 * compiler will take care of that...
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class BeanImplementsBI extends EJBVerificationRule {
    
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        if (ctx.getEjb() instanceof Session){
            Collection<String> businessInterFaces = new ArrayList<String>();
            
            processAnnotation(businessInterFaces, ctx.getClazz(), EJBAPIAnnotations.LOCAL);
            processAnnotation(businessInterFaces, ctx.getClazz(), EJBAPIAnnotations.REMOTE);
          
            if (businessInterFaces.size() > 0){
                Collection<String> implementedInterfaces = new TreeSet<String>();
                
                for (TypeMirror interfaceType : ctx.getClazz().getInterfaces()){
                    String iface = JavaUtils.extractClassNameFromType(interfaceType);
                    
                    if (iface != null){
                        implementedInterfaces.add(iface);
                    }
                }
                
                for (String businessInterface : businessInterFaces){
                    if (!implementedInterfaces.contains(businessInterface)){
                        ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                                NbBundle.getMessage(BeanImplementsBI.class, "MSG_BeanImplementsBI"),
                                Severity.WARNING);
                        
                        return Collections.singletonList(err);
                    }
                }
            }
        }
        
        return null;
    }
    
    
    private void processAnnotation(Collection<String> businessInterFaces,
            TypeElement clazz, String annotClass){
          AnnotationMirror annLocal = JavaUtils.findAnnotation(clazz, EJBAPIAnnotations.LOCAL);
            AnnotationValue value = JavaUtils.getAnnotationAttrValue(annLocal, EJBAPIAnnotations.VALUE);
            
            if (value != null){
                for (AnnotationValue ifaceAttr : (List<? extends AnnotationValue>)value.getValue()){
                    String className = JavaUtils.extractClassNameFromType((TypeMirror)ifaceAttr.getValue());
                    businessInterFaces.add(className);
                }
            }
    }
}
