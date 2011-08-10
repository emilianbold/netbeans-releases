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
package org.netbeans.modules.web.beans.analysis;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class CdiAnalysisResult {
    
    private static final String BEANS_XML = "beans.xml";      // NOI18N
    private static final String META_INF = "META-INF";        // NOI18N

    public CdiAnalysisResult( CompilationInfo info ){
        myInfo = info;
        myProblems = new LinkedList<ErrorDescription>();
    }

    public void addError( Element subject, String message ) {
        addNotification( Severity.ERROR, subject, message);
    }
    
    public void addNotification( Severity severity,
            Element element, String message )
    {
        ErrorDescription description = CdiEditorAnalysisFactory.
            createNotification( severity, element, myInfo , 
            message);
        if ( description == null ){
            return;
        }
        getProblems().add( description );              
    }

    public CompilationInfo getInfo() {
        return myInfo;
    }
    
    public List<ErrorDescription> getProblems(){
        return myProblems;
    }
    
    public void requireCdiEnabled( Element element ){
        if ( isCdiRequired ){
            return;
        }
        isCdiRequired = true;
        if ( !isCdiEnabled() ) {
            addError(element, NbBundle.getMessage(CdiAnalysisResult.class, 
                "ERR_RequireWebBeans"));        // NOI18N
        }
    }
    
    protected boolean isCdiEnabled(){
        FileObject fileObject = getInfo().getFileObject();
        if ( fileObject ==null ){
            return true;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            return true;
        }
        FileObject parent = getBeansTargetFolder(project, false);
        return parent!= null && parent.getFileObject(BEANS_XML)!=null;
    }
    
    private FileObject getBeansTargetFolder(Project project, boolean create) {
        // TODO: for fix hint <code>create</code> flag will be used to create folder if it is absent
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            return wm.getWebInf();
        } 
        else {
            EjbJar ejbs[] = EjbJar.getEjbJars(project);
            if (ejbs.length > 0) {
                return ejbs[0].getMetaInf();
            } else {
                Car cars[] = Car.getCars(project);
                if (cars.length > 0) {
                    return cars[0].getMetaInf();
                } 
            }
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if ( sourceGroups.length >0 ){
            return sourceGroups[0].getRootFolder().getFileObject( META_INF );
        }
        return null;
    }
    
    private CompilationInfo myInfo ;
    private List<ErrorDescription> myProblems;
    private boolean isCdiRequired;
}
