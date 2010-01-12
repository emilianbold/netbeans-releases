/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.CompilationUnitTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;


/**
 *
 * @author Jan Lahoda
 */
public class ComputeOverriding {
    
    static final Logger LOG = Logger.getLogger(ComputeOverriding.class.getName());

    private final AtomicBoolean cancel;
    
    public ComputeOverriding(AtomicBoolean cancel) {
        this.cancel = cancel;
    }
    
    public static AnnotationType detectOverrides(CompilationInfo info, TypeElement type, ExecutableElement ee, List<ElementDescription> result) {
        final Map<Name, List<ExecutableElement>> name2Method = new HashMap<Name, List<ExecutableElement>>();
        
        sortOutMethods(info, name2Method, type, false);
        
        List<ExecutableElement> lee = name2Method.get(ee.getSimpleName());
        
        if (lee == null || lee.isEmpty()) {
            return null;
        }
        
        Set<ExecutableElement> seenMethods = new HashSet<ExecutableElement>();
        
        for (ExecutableElement overridee : lee) {
            if (info.getElements().overrides(ee, overridee, SourceUtils.getEnclosingTypeElement(ee))) {
                if (seenMethods.add(overridee)) {
                    result.add(new ElementDescription(info, overridee, false));
                }
            }
        }
        
        if (!result.isEmpty()) {
            for (ElementDescription ed : result) {
                if (!ed.getModifiers().contains(Modifier.ABSTRACT)) {
                    return AnnotationType.OVERRIDES;
                }
            }
            
            return AnnotationType.IMPLEMENTS;
        }
        
        return null;
    }
    
    public Map<ElementHandle<? extends Element>, List<ElementDescription>> process(CompilationInfo info) {
        IsOverriddenVisitor v = new IsOverriddenVisitor(info, cancel);
        CompilationUnitTree unit = info.getCompilationUnit();
        
        long startTime1 = System.currentTimeMillis();
        
        v.scan(unit, null);
        
        long endTime1 = System.currentTimeMillis();
        
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden Scanner", //NOI18N
                    new Object[] {info.getFileObject(), endTime1 - startTime1});
        
        Map<ElementHandle<? extends Element>, List<ElementDescription>> result = new HashMap<ElementHandle<? extends Element>, List<ElementDescription>>();
        
        for (ElementHandle<TypeElement> td : v.type2Declaration.keySet()) {
            if (isCanceled())
                return null;
            
            LOG.log(Level.FINE, "type: {0}", td.getQualifiedName()); //NOI18N
            
            final Map<Name, List<ExecutableElement>> name2Method = new HashMap<Name, List<ExecutableElement>>();
            
            TypeElement resolvedType = td.resolve(info);
            
            if (resolvedType == null)
                continue;
            
            sortOutMethods(info, name2Method, resolvedType, false);
            
            for (ElementHandle<ExecutableElement> methodHandle : v.type2Declaration.get(td)) {
                if (isCanceled())
                    return null;
                
                ExecutableElement ee = methodHandle.resolve(info);
                
                if (ee == null)
                    continue;
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "method: {0}", ee.toString()); //NOI18N
                }
                
                List<ExecutableElement> lee = name2Method.get(ee.getSimpleName());
                
                if (lee == null || lee.isEmpty()) {
                    continue;
                }
                
                Set<ExecutableElement> seenMethods = new HashSet<ExecutableElement>();
                List<ElementDescription> descriptions = new LinkedList<ElementDescription>();

                for (ExecutableElement overridee : lee) {
                    if (info.getElements().overrides(ee, overridee, SourceUtils.getEnclosingTypeElement(ee))) {
                        if (seenMethods.add(overridee)) {
                            descriptions.add(new ElementDescription(info, overridee, false));
                        }
                    }
                }

                result.put(methodHandle, descriptions);
            }
        }
        
        if (isCanceled())
            return null;
        else
            return result;
    }
    
    private synchronized boolean isCanceled() {
        return cancel.get();
    }
    
    private static void sortOutMethods(CompilationInfo info, Map<Name, List<ExecutableElement>> where, Element td, boolean current) {
        if (current) {
            Map<Name, List<ExecutableElement>> newlyAdded = new HashMap<Name, List<ExecutableElement>>();
            
            OUTTER: for (ExecutableElement ee : ElementFilter.methodsIn(td.getEnclosedElements())) {
                Name name = ee.getSimpleName();
                List<ExecutableElement> alreadySeen = where.get(name);
                
                if (alreadySeen != null) {
                    for (ExecutableElement seen : alreadySeen) {
                        if (info.getElements().overrides(seen, ee, (TypeElement) seen.getEnclosingElement())) {
                            continue OUTTER; //a method that overrides this one was already handled, ignore
                        }
                    }
                }
                
                List<ExecutableElement> lee = newlyAdded.get(name);
                
                if (lee == null) {
                    newlyAdded.put(name, lee = new ArrayList<ExecutableElement>());
                }
                
                lee.add(ee);
            }
            
            for (Map.Entry<Name, List<ExecutableElement>> e : newlyAdded.entrySet()) {
                List<ExecutableElement> lee = where.get(e.getKey());
                
                if (lee == null) {
                    where.put(e.getKey(), e.getValue());
                } else {
                    lee.addAll(e.getValue());
                }
            }
        }
        
        for (TypeMirror superType : info.getTypes().directSupertypes(td.asType())) {
            if (superType.getKind() == TypeKind.DECLARED) {
                sortOutMethods(info, where, ((DeclaredType) superType).asElement(), true);
            }
        }
    }
    
}
