/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.ClassTree;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.fixes.MakeClassPublic;
import org.netbeans.modules.j2ee.ejbverification.fixes.RemoveModifier;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class LegalModifiers extends EJBVerificationRule {
    
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        
        if (ctx.getEjb() != null){
            Collection<ErrorDescription> problemsFounds = new LinkedList<ErrorDescription>();
            
            Set<Modifier> modifiers = ctx.getClazz().getModifiers();
            
            if (!modifiers.contains(Modifier.PUBLIC)){
                Fix fix = new MakeClassPublic(ctx.getFileObject(), ElementHandle.create(ctx.getClazz()));
                
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustBePublic"), fix);
                
                problemsFounds.add(err);
            }
            
            if (modifiers.contains(Modifier.FINAL)){
                Fix fix = new RemoveModifier(ctx.getFileObject(),
                        ElementHandle.create(ctx.getClazz()),
                        Modifier.FINAL);
                
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustNotBeFinal"), fix);
                
                problemsFounds.add(err);
            }
            
            if (modifiers.contains(Modifier.ABSTRACT)){
                if (isInterface(ctx)){
                    // no fix for interfaces, just a warning
                    ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                            NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustNotBeAbstract"));

                    problemsFounds.add(err);
                } else {
                    Fix fix = new RemoveModifier(ctx.getFileObject(),
                            ElementHandle.create(ctx.getClazz()),
                            Modifier.ABSTRACT);

                    ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                    NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustNotBeAbstract"), fix);

                    problemsFounds.add(err);
                }
            }
            
            return problemsFounds;
        }
        
        return null;
    }
    
    private boolean isInterface(EJBProblemContext ctx){
        ClassTree classTree = ctx.getComplilationInfo().getTrees().getTree(ctx.getClazz());
        return ctx.getComplilationInfo().getTreeUtilities().isInterface(classTree);
    }
    
}
