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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
abstract class CdiAnnotationAnalyzer extends TargetAnalyzer {
    
    private static final Logger LOG = Logger.getLogger( 
            CdiAnnotationAnalyzer.class.getName()); 
    
    CdiAnnotationAnalyzer(TypeElement element, CompilationInfo compInfo, 
            List<ErrorDescription> descriptions) 
    {
        init( element , compInfo );
        myDescriptions = descriptions;
        myCompInfo = compInfo;
    }
    
    @Override
    public boolean hasTarget() {
        try {
            return getHelper().runJavaSourceTask( new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return CdiAnnotationAnalyzer.super.hasTarget();
                }
            });
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null, e);
        }
        return true;
    }
    
    @Override
    public boolean hasRuntimeRetention() {
        try {
            return getHelper().runJavaSourceTask( new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return CdiAnnotationAnalyzer.super.hasRuntimeRetention();
                }
            });
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null, e);
        }
        return true;
    }
    
    @Override
    public Set<ElementType> getDeclaredTargetTypes() {
        try {
            return getHelper().runJavaSourceTask( new Callable<Set<ElementType>>() {

                @Override
                public Set<ElementType> call() throws Exception {
                    return CdiAnnotationAnalyzer.super.getDeclaredTargetTypes();
                }
            });
        }
        catch (IOException e) {
            LOG.log( Level.INFO , null, e);
        }
        return Collections.emptySet();
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#handleNoTarget()
     */
    @Override
    protected void handleNoTarget() {
        if ( getDescriptions()== null){
            return;
        }
        ErrorDescription description = CdiEditorAnalysisFactory.
            createError( getOriginalElement(), getCompilationInfo(), 
                    NbBundle.getMessage(ScopeAnalyzer.class, "ERR_NoTarget" ,   // NOI18N
                            getCdiMetaAnnotation()));      
        getDescriptions().add( description );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.RuntimeRetentionAnalyzer#handleNoRetention()
     */
    @Override
    protected void handleNoRetention() {
        if ( getDescriptions()== null){
            return;
        }
        ErrorDescription description = CdiEditorAnalysisFactory.
            createError( getOriginalElement(), getCompilationInfo(), 
                    NbBundle.getMessage(ScopeAnalyzer.class, "ERR_NoRetention", // NOI18N
                            getCdiMetaAnnotation()));      
        getDescriptions().add( description );
    }
    
    protected abstract String getCdiMetaAnnotation();
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.RuntimeRetentionAnalyzer#getElement()
     */
    @Override
    protected Element getElement() {
        /* This method requires redefinition : helper has its own
         * compilation controller. so the original Element is in the other 
         * Java model. As consequence one need the same element in the helper Java model.  
         */
        ElementHandle<Element> handle = ElementHandle.create(getOriginalElement());
        return handle.resolve(getHelper().getCompilationController());
    }
    
    protected Element getOriginalElement(){
        return super.getElement();
    }
    
    protected List<ErrorDescription> getDescriptions(){
        return myDescriptions;
    }
    
    protected CompilationInfo getCompilationInfo() {
        return myCompInfo;
    }
    
    private List<ErrorDescription> myDescriptions;
    private CompilationInfo myCompInfo;

}
