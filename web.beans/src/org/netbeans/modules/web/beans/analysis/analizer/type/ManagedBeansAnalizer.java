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
package org.netbeans.modules.web.beans.analysis.analizer.type;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analizer.ClassElementAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class ManagedBeansAnalizer implements ClassAnalyzer {
    
    private static final String INJECT = "javax.inject.Inject";                      // NOI18N

    private static final String DECORATOR = "javax.decorator.Decorator";              // NOI18N

    private static final String EXTENSION = "javax.enterprise.inject.spi.Extension";  //NOI18N
    
    private static final Logger LOG = Logger.getLogger( 
            ManagedBeansAnalizer.class.getName() );  

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.ClassElementAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        Project project = FileOwnerQuery.getOwner( compInfo.getFileObject() );
        if ( project == null ){
            return ;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        final ElementHandle<TypeElement> handle = ElementHandle.create( element);
        try {
            boolean cdiManaged = metaModel.runReadAction( 
                    new MetadataModelAction<WebBeansModel, Boolean>() 
            {

                @Override
                public Boolean run( WebBeansModel model ) throws Exception {
                    TypeElement clazz = handle.resolve( model.getCompilationController());
                    if ( clazz == null ){
                        return false;
                    }
                    List<AnnotationMirror> qualifiers = model.getQualifiers( clazz,  true );
                    return qualifiers.size() > 0 ;
                }
            });
            if ( cdiManaged ){
                checkCtor( element , compInfo , descriptions );
                checkInner( element, parent , compInfo ,descriptions );
                checkAbstract( element , compInfo ,descriptions );
                checkImplementsExtension( element , compInfo ,descriptions );
            }
        }
        catch (MetadataModelException e) {
            LOG.log( Level.INFO , null , e);
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null , e);
        }
    }

    private void checkImplementsExtension( TypeElement element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        TypeElement extension = compInfo.getElements().getTypeElement(EXTENSION);
        if ( extension == null ){
            return;
        }
        TypeMirror elementType = element.asType();
        if ( compInfo.getTypes().isSubtype( elementType,  extension.asType())){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification( Severity.WARNING, element, compInfo, 
                        NbBundle.getMessage( ManagedBeansAnalizer.class, 
                                "WARN_QualifiedElementExtension"));
            descriptions.add( description );
        }
    }

    private void checkAbstract( TypeElement element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        TypeElement decorator = compInfo.getElements().getTypeElement(DECORATOR);
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.ABSTRACT )){
            if (  decorator != null ){
                List<? extends AnnotationMirror> annotations = 
                    compInfo.getElements().getAllAnnotationMirrors( element );
                for (AnnotationMirror annotationMirror : annotations) {
                    Element annotation = compInfo.getTypes().asElement( 
                            annotationMirror.getAnnotationType());
                    if ( decorator.equals( annotation )){
                        return;
                    }
                }
            }
            // element is abstract and has no Decorator annotation
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification( Severity.WARNING, element, compInfo, 
                        NbBundle.getMessage(ManagedBeansAnalizer.class, 
                                "WARN_QualifierAbstractClass"));
            descriptions.add( description );    
        }        
    }

    private void checkInner( TypeElement element, TypeElement parent,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        if ( parent == null ){
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( !modifiers.contains( Modifier.STATIC )){
            ErrorDescription description = CdiEditorAnalysisFactory.
            createError( element, compInfo, NbBundle.getMessage(
                    ManagedBeansAnalizer.class, "ERR_NonStaticInnerType"));
            descriptions.add( description );    
        }
    }

    private void checkCtor( TypeElement element, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        TypeElement inject = compInfo.getElements().getTypeElement(INJECT);
        List<ExecutableElement> ctors = ElementFilter.constructorsIn( 
                element.getEnclosedElements());
        for (ExecutableElement ctor : ctors) {
            Set<Modifier> modifiers = ctor.getModifiers();
            if ( modifiers.contains( Modifier.PRIVATE )){
                continue;
            }
            List<? extends VariableElement> parameters = ctor.getParameters();
            if ( parameters.size() ==0 ){
                return;
            }
            if ( inject == null){
                continue;
            }
            List<? extends AnnotationMirror> annotations = compInfo.
                getElements().getAllAnnotationMirrors(ctor);
            for (AnnotationMirror annotationMirror : annotations) {
                Element annotation = compInfo.getTypes().asElement( 
                        annotationMirror.getAnnotationType());
                if ( inject.equals( annotation )){
                    return;
                }
            }
        }
        // there is no non-private ctors without params or annotated with @Inject
        ErrorDescription description = CdiEditorAnalysisFactory.
            createNotification( Severity.WARNING, element, compInfo, 
                    NbBundle.getMessage(ManagedBeansAnalizer.class, 
                            "WARN_QualifierNoCtorClass"));
        descriptions.add( description );
    }

}
