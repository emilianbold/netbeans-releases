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

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InterceptorBindingAnalyzer implements AnnotationAnalyzer {
    
    private static final Logger LOG = Logger.getLogger( 
            InterceptorBindingAnalyzer.class.getName());

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AnnotationElementAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, CompilationInfo compInfo,
            List<ErrorDescription> descriptions , AtomicBoolean cancel )
    {
        if ( !AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.INTERCEPTOR_BINDING_FQN , compInfo))
        {
            return;
        }
        InterceptorTargetAnalyzer analyzer = new InterceptorTargetAnalyzer(
                element, compInfo, descriptions);
        if ( cancel.get() ){
            return;
        }
        if (!analyzer.hasRuntimeRetention()) {
            ErrorDescription description = CdiEditorAnalysisFactory
                    .createError(element, compInfo, NbBundle
                            .getMessage(InterceptorBindingAnalyzer.class,
                                    INCORRECT_RUNTIME));
            descriptions.add(description);
        }
        if ( cancel.get() ){
            return;
        }
        if (!analyzer.hasTarget()) {
            ErrorDescription description = CdiEditorAnalysisFactory
                    .createError(element, compInfo, NbBundle.getMessage(
                            InterceptorBindingAnalyzer.class,
                            "ERR_IncorrectInterceptorBindingTarget")); // NOI18N
            descriptions.add(description);
        }
        else {
            if ( cancel.get() ){
                return;
            }
            Set<ElementType> declaredTargetTypes = analyzer.getDeclaredTargetTypes();
            if ( cancel.get() ){
                return;
            }
            checkTransitiveInterceptorBindings( element, declaredTargetTypes, 
                    compInfo , descriptions);
        }
    }
    
    private void checkTransitiveInterceptorBindings( TypeElement element,
            Set<ElementType> declaredTargetTypes, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        if ( declaredTargetTypes== null || declaredTargetTypes.size()==1){
            return;
        }
        Project project = FileOwnerQuery.getOwner( compInfo.getFileObject() );
        if ( project == null ){
            return ;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        final ElementHandle<TypeElement> handle = ElementHandle.create( element);
        try {
            Collection<ElementHandle<Element>> bindingHandles = 
                metaModel.runReadAction( 
                    new MetadataModelAction<WebBeansModel, 
                    Collection<ElementHandle<Element>>>() 
            {
                @Override
                public Collection<ElementHandle<Element>> run( 
                        WebBeansModel model ) throws Exception 
                {
                    Set<ElementHandle<Element>> result = new 
                        HashSet<ElementHandle<Element>>();
                    TypeElement iBinding = handle.resolve( 
                            model.getCompilationController());
                    if ( iBinding == null ){
                        return result;
                    }
                    Collection<AnnotationMirror> interceptorBindings = 
                        model.getInterceptorBindings(iBinding);
                    for (AnnotationMirror annotation : interceptorBindings) {
                        Element interceptorBinding = annotation.
                            getAnnotationType().asElement();
                        ElementHandle<Element> iBindingHandle = 
                            ElementHandle.create( interceptorBinding);
                        result.add( iBindingHandle );
                        
                    }
                    return result;
                }
            });
            for (ElementHandle<Element> bindingHandle : bindingHandles) {
                Element binding = bindingHandle.resolve(compInfo);
                if ( !(binding instanceof  TypeElement) ){
                    continue;
                }
                InterceptorTargetAnalyzer analyzer = new InterceptorTargetAnalyzer(
                        (TypeElement)binding, compInfo, null );
                Set<ElementType> bindingTargetTypes = analyzer.getDeclaredTargetTypes();
                if ( bindingTargetTypes.size() == 1 && bindingTargetTypes.
                        contains(ElementType.TYPE))
                {
                    ErrorDescription description = CdiEditorAnalysisFactory
                        .createError(element, compInfo, NbBundle
                            .getMessage(InterceptorBindingAnalyzer.class,
                                    "ERR_IncorrectTransitiveInterceptorBinding",
                                    ((TypeElement)binding).getQualifiedName().toString()));
                    descriptions.add(description);
                }
                
            }
        }
        catch (MetadataModelException e) {
            LOG.log( Level.INFO , null , e);
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null , e);
        }
    }

    private static class InterceptorTargetAnalyzer extends CdiAnnotationAnalyzer {
        
        InterceptorTargetAnalyzer( TypeElement element , CompilationInfo info ,
                List<ErrorDescription> descriptions)
        {
            super( element, info , descriptions );
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.CdiAnnotationAnalyzer#getCdiMetaAnnotation()
         */
        @Override
        protected String getCdiMetaAnnotation() {
            return AnnotationUtil.INTERCEPTOR_BINDING;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
         */
        @Override
        protected TargetVerifier getTargetVerifier() {
            return InterceptorBindingVerifier.getInstance();
        }
        
    }

}
