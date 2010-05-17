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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;
import static org.netbeans.modules.j2ee.jpa.model.JPAAnnotations.*;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class LegalCombinationOfAnnotations extends JPAClassRule {
    //TODO: Add more rules
    private static Collection<IllegalCombination> illegalClassAnnotationCombinations = Arrays.asList(
            new IllegalCombination(Collections.singleton(ENTITY), Arrays.asList(EMBEDDABLE, MAPPED_SUPERCLASS)),
            new IllegalCombination(Collections.singleton(TABLE), Collections.singleton(MAPPED_SUPERCLASS))
            );
    private static Collection<IllegalCombination> illegalAttrAnnotationCombinations = Arrays.asList(
            );
    
    public LegalCombinationOfAnnotations() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        
        Collection<String> annotationsOnClass = ModelUtils.extractAnnotationNames(subject);
        
        for (IllegalCombination ic : illegalClassAnnotationCombinations){
            ic.check(ctx, subject, problemsFound, annotationsOnClass);
        }
        
        for (Element elem : subject.getEnclosedElements()){
            Collection<String> annotationsOnElement = ModelUtils.extractAnnotationNames(elem);
            
            for (IllegalCombination ic : illegalAttrAnnotationCombinations){
                ic.check(ctx, elem, problemsFound, annotationsOnElement);
            }
        }
        
        return problemsFound.toArray(new ErrorDescription[problemsFound.size()]);
    }
    
    private static class IllegalCombination{
        private Collection<String> set1;
        private Collection<String> set2;
        
        IllegalCombination(Collection<String> set1, Collection<String> set2){
            this.set1 = set1;
            this.set2 = set2;
        }
        
        void check(ProblemContext ctx,
                Element elem,
                Collection<ErrorDescription> errorList,
                Collection<String> annotations){
            
            for (String ann : annotations){
                if (set1.contains(ann)){
                    for (String forbiddenAnn : set2){
                        if (annotations.contains(forbiddenAnn)){
                            ErrorDescription error = createProblem(elem, ctx,
                                    NbBundle.getMessage(LegalCombinationOfAnnotations.class,
                                    "MSG_IllegalAnnotationCombination",
                                    ModelUtils.shortAnnotationName(ann),
                                    ModelUtils.shortAnnotationName(forbiddenAnn)));
                            
                            errorList.add(error);
                        }
                    }
                }
            }
        }
    }
}
