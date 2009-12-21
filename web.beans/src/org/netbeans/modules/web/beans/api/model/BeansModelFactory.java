/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.beans.api.model;

import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.impl.model.BeansModelImpl;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public final class BeansModelFactory {
    
    private BeansModelFactory(){
    }

    public static BeansModel createModel( BeansModelUnit unit ){
        return new BeansModelImpl(unit);
    }
    
    public static synchronized BeansModel getModel( WebModule module ){
        BeansModel model = MODELS.get( module );
        if ( model == null ){
            BeansModelUnit unit = getUnit( module , null );
            if ( unit == null ){
                return null;
            }
            model = createModel( unit );
            MODELS.put(module, model);
        }
        return model;
    }
    
    public static BeansModel getModel( ModelUnit unit ){
        return getModel( unit.getSourceFileObject() );
    }
    
    public static synchronized BeansModel getModel( FileObject origin ){
        if ( origin == null ){
            return null;
        }
        WebModule module = WebModule.getWebModule(origin);
        BeansModel model = MODELS.get( module );
        if ( model == null ){
            BeansModelUnit unit = getUnit( module , origin );
            if ( unit == null ){
                return null;
            }
            model = createModel( unit );
            MODELS.put(module, model);
        }
        return model;
    }
    
    private static BeansModelUnit getUnit( WebModule module, FileObject origin ) {
        if ( module == null ){
            return null;
        }
        FileObject fileObject = origin;
        if ( fileObject == null ){
            fileObject = getFileObject( module );
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            return null;
        }
        ClassPath boot = getClassPath( project , ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE );
        ClassPath src = getClassPath(project , ClassPath.SOURCE);
        return BeansModelUnit.create(boot, compile, src, module);
    }
    
    private static ClassPath getClassPath( Project project, String type ) {
        ClassPathProvider provider = project.getLookup().lookup( 
                ClassPathProvider.class);
        if ( provider == null ){
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if ( sources == null ){
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups( 
                JavaProjectConstants.SOURCES_TYPE_JAVA );
        ClassPath[] paths = new ClassPath[ sourceGroups.length];
        int i=0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
        }
        return ClassPathSupport.createProxyClassPath( paths );
    }
    
    private static FileObject getFileObject( WebModule module ) {
        FileObject fileObject = module.getDocumentBase();
        if ( fileObject != null ){
            return fileObject;
        }
        fileObject = module.getDeploymentDescriptor();
        if ( fileObject != null ){
            return fileObject;
        }
        fileObject = module.getWebInf();
        if ( fileObject != null ){
            return fileObject;
        }
        
        FileObject[] fileObjects = module.getJavaSources();
        if ( fileObjects!= null){
            for (FileObject source : fileObjects) {
                if ( source != null ){
                    return source;
                }
            }
        }
        return null;
    }
    
    private static final Map<WebModule, BeansModel> MODELS = 
        new WeakHashMap<WebModule, BeansModel>();
}
