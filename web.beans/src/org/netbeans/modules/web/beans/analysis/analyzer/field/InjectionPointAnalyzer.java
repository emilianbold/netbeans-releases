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
package org.netbeans.modules.web.beans.analysis.analyzer.field;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResultKind;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;


/**
 * @author ads
 *
 */
public class InjectionPointAnalyzer implements FieldAnalyzer {
    
    private static final Logger LOG = Logger.getLogger( 
            InjectionPointAnalyzer.class.getName() );  

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.FieldElementAnalyzer.FieldAnalyzer#analyze(javax.lang.model.element.VariableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    public void analyze( final VariableElement element, TypeMirror elementType,
            TypeElement parent, final CompilationInfo compInfo,
            final List<ErrorDescription> descriptions )
    {
        Project project = FileOwnerQuery.getOwner( compInfo.getFileObject() );
        if ( project == null ){
            return ;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        final ElementHandle<VariableElement> handle = ElementHandle.create( element);
        try {
            metaModel.runReadAction( 
                    new MetadataModelAction<WebBeansModel, Void>() 
            {

                @Override
                public Void run( WebBeansModel model ) throws Exception {
                    VariableElement var = handle.resolve( 
                            model.getCompilationController());
                    if ( var == null ){
                        return null;
                    }
                    if ( model.isInjectionPoint( var ) ){
                        DependencyInjectionResult result = model.lookupInjectables( var,  null);
                        checkResult(result, element , compInfo , descriptions );
                    }
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            if ( informInjectionPointDefError(e, element, compInfo, 
                    descriptions))
            {
                LOG.log( Level.INFO , null , e);
            }
        }
        catch (IOException e) {
            if ( informInjectionPointDefError(e, element, compInfo, 
                    descriptions))
            {
                LOG.log( Level.INFO , null , e);
            }
        }
    }

    private void checkResult( DependencyInjectionResult result ,
            VariableElement var, CompilationInfo compInfo,
            List<ErrorDescription> descriptions )
    {
        if ( result instanceof DependencyInjectionResult.Error ){
            ResultKind kind = result.getKind();
            Severity severity = Severity.WARNING;
            if ( kind == DependencyInjectionResult.ResultKind.DEFINITION_ERROR){
                severity = Severity.ERROR;
            }
            String message = ((DependencyInjectionResult.Error)result).getMessage();
            ErrorDescription description = CdiEditorAnalysisFactory.
                createNotification(severity, var , compInfo, message);
            descriptions.add( description );
        }
    }

    private boolean informInjectionPointDefError(Exception exception , Element element, 
            CompilationInfo compInfo, List<ErrorDescription> descriptions)
    {
        Throwable cause = exception.getCause();
        if ( cause instanceof InjectionPointDefinitionError ){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, 
                    cause.getMessage());
            descriptions.add( description );
            return true;
        }
        return false;
    }
}
