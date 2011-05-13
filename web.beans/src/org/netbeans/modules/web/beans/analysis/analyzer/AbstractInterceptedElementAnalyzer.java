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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.MetaModelSupport;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.spi.editor.hints.ErrorDescription;


/**
 * @author ads
 *
 */
public abstract class AbstractInterceptedElementAnalyzer {
    
    private final static Logger LOG = Logger.getLogger( 
            AbstractInterceptedElementAnalyzer.class.getName());

    protected boolean hasInterceptorBindings(Element element,
            CompilationInfo compInfo, List<ErrorDescription> descriptions )
    {
        Project project = FileOwnerQuery.getOwner( compInfo.getFileObject() );
        if ( project == null ){
            return false;
        }
        MetaModelSupport support = new MetaModelSupport(project);
        MetadataModel<WebBeansModel> metaModel = support.getMetaModel();
        final ElementHandle<Element> handle = ElementHandle.create( element);
        try {
            return metaModel.runReadAction( 
                    new MetadataModelAction<WebBeansModel, Boolean>() 
            {
                @Override
                public Boolean run( WebBeansModel model ) throws Exception {
                    Element element = handle.resolve( model.getCompilationController());
                    if ( element == null ){
                        return false;
                    }
                    Collection<AnnotationMirror> interceptorBindings = 
                        model.getInterceptorBindings( element );
                    List<? extends AnnotationMirror> annotations = 
                        model.getCompilationController().getElements().
                        getAllAnnotationMirrors(element);
                    Set<AnnotationMirror> set = new HashSet<AnnotationMirror>(
                            interceptorBindings);
                    set.retainAll( annotations );
                    return !set.isEmpty();
                }
            });
        }
        catch (MetadataModelException e) {
            LOG.log( Level.INFO , null , e);
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null , e);
        }
        return false;
    }
}
