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
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
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
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;


/**
 * @author ads
 *
 */
public abstract class AbstractScopedAnalyzer  {
    
    private static final Logger LOG = Logger.getLogger( 
            AbstractScopedAnalyzer.class.getName() );  

    public MetadataModel<WebBeansModel> analyzeScope( Element element, 
            CompilationInfo compInfo, List<ErrorDescription> descriptions , 
            AtomicBoolean cancel )
    {
        Project project = FileOwnerQuery.getOwner( compInfo.getFileObject() );
        if ( project == null ){
            return null;
        }
        if ( cancel.get() ){
            return null;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        final ElementHandle<Element> handle = ElementHandle.create( element);
        try {
            String scope = metaModel.runReadAction( 
                    new MetadataModelAction<WebBeansModel, String>() 
            {

                @Override
                public String run( WebBeansModel model ) throws Exception {
                    Element element = handle.resolve( model.getCompilationController());
                    if ( element == null ){
                        return null;
                    }
                    return model.getScope( element );
                }
            });
            if ( cancel.get() ){
                return metaModel;
            }
            TypeElement scopeElement = compInfo.getElements().getTypeElement( scope );
            if ( scopeElement == null ){
                return metaModel;
            }
            checkScope( scopeElement , element , compInfo, descriptions , cancel);
        }
        catch (MetadataModelException e) {
            if ( !informCdiException(e, element, compInfo, descriptions) ){
                LOG.log( Level.INFO , null , e);
            }
        }
        catch (IOException e) {
            if ( !informCdiException(e, element, compInfo, descriptions) ){
                LOG.log( Level.INFO , null , e);
            }
        }
        return metaModel;
    }
    
    protected abstract void checkScope( TypeElement scopeElement, Element element, 
            CompilationInfo compInfo, List<ErrorDescription> descriptions , 
            AtomicBoolean cancel );
    
    protected boolean hasTypeVarParameter(TypeMirror type ){
        if ( type.getKind() == TypeKind.TYPEVAR){
            return true;
        }
        if ( type instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = 
                ((DeclaredType)type).getTypeArguments();
            for (TypeMirror typeArg : typeArguments) {
                if ( hasTypeVarParameter(typeArg)){
                    return true;
                }
            }
        }
        else if ( type instanceof ArrayType ){
            return hasTypeVarParameter(((ArrayType)type).getComponentType());
        }
        return false;
    }

    private boolean informCdiException(Exception exception , Element element, 
            CompilationInfo compInfo, List<ErrorDescription> descriptions)
    {
        Throwable cause = exception.getCause();
        if ( cause instanceof CdiException ){
            ErrorDescription description = CdiEditorAnalysisFactory.
                createError( element, compInfo, 
                    cause.getMessage());
            descriptions.add( description );
            return true;
        }
        return false;
    }
}
