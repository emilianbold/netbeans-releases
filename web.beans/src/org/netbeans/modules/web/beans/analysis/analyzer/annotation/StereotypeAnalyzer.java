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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.beans.analysis.analyzer.annotation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractScopedAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class StereotypeAnalyzer extends AbstractScopedAnalyzer implements AnnotationAnalyzer {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.AnnotationElementAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    public void analyze( TypeElement element, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        boolean isStereotype = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.STEREOTYPE_FQN, compInfo);
        if ( !isStereotype ){
            return;
        }
        analyzeScope((Element)element, compInfo, descriptions);
        checkName( element, compInfo, descriptions);
        checkDefinition( element , compInfo, descriptions );
    }

    private void checkDefinition( TypeElement element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        StereotypeTargetAnalyzer analyzer = new StereotypeTargetAnalyzer(element, 
                compInfo, descriptions);
        if ( !analyzer.hasRuntimeRetention()){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, 
                    NbBundle.getMessage(StereotypeAnalyzer.class, 
                            INCORRECT_RUNTIME));
            descriptions.add( description );
        }
        if ( !analyzer.hasTarget()){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, 
                        NbBundle.getMessage(StereotypeAnalyzer.class, 
                                "ERR_IncorrectStereotypeTarget"));                // NOI18N
            descriptions.add( description );
        }
    }

    private void checkName( TypeElement element, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        AnnotationMirror named = AnnotationUtil.getAnnotationMirror(element, 
                AnnotationUtil.NAMED , compInfo);
        Map<? extends ExecutableElement, ? extends AnnotationValue> members = 
            named.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: 
            members.entrySet()) 
        {
            ExecutableElement member = entry.getKey();
            if ( member.getSimpleName().contentEquals(AnnotationUtil.VALUE)){ 
                ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, 
                    NbBundle.getMessage(StereotypeAnalyzer.class, 
                            "ERR_NonEmptyNamedStereotype"));            // NOI18N
                descriptions.add( description );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.AbstractScopedAnalyzer#checkScope(javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    protected void checkScope( TypeElement scopeElement, Element element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
    }

    private static class StereotypeTargetAnalyzer extends CdiAnnotationAnalyzer{

        StereotypeTargetAnalyzer( TypeElement element,
                CompilationInfo compInfo, List<ErrorDescription> descriptions )
        {
            super(element, compInfo, descriptions);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.CdiAnnotationAnalyzer#getCdiMetaAnnotation()
         */
        @Override
        protected String getCdiMetaAnnotation() {
            return AnnotationUtil.STEREOTYPE;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
         */
        @Override
        protected TargetVerifier getTargetVerifier() {
            return new StereotypeVerifier( getHelper() );
        }
        
    }
}
