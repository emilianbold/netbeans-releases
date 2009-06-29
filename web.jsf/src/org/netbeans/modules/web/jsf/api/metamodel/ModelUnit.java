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
package org.netbeans.modules.web.jsf.api.metamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import com.sun.swing.internal.plaf.synth.resources.synth;



/**
 * @author ads
 *
 */
public class ModelUnit {
    
    public  static final String META_INF = "META-INF";      // NOI18N
    private static final String WEB_INF = "WEB-INF";        // NOI18N
    public static final String FACES_CONFIG = "faces-config.xml";// NOI18N
    
    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, File mainFacesConfig)
    {
        return new ModelUnit(bootPath, compilePath, sourcePath, mainFacesConfig, 
                null);
    }
    
    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath)
    {
        return new ModelUnit(bootPath, compilePath, sourcePath, null);
    }
    
    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, FileObject[] configFiles )
    {
        return new ModelUnit(bootPath, compilePath, sourcePath, null,configFiles);
    }
    
    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, WebModule webModule )
    {
        return new ModelUnit(bootPath, compilePath, sourcePath, 
                webModule);
    }

    private ModelUnit(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, File mainFacesConfig, FileObject[] configFiles ) 
    {
        myBootPath= bootPath;
        myCompilePath = compilePath;
        mySourcePath = sourcePath;
        myMainFacesConfig =mainFacesConfig ;
        myConfigFiles = configFiles;
        myModule = null;
        getConfigFiles();
    }
    
    private ModelUnit(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath,  WebModule webModule ) 
    {
        myBootPath= bootPath;
        myCompilePath = compilePath;
        mySourcePath = sourcePath;
        if (webModule != null) {
            FileObject[] configs = ConfigurationUtils
                    .getFacesConfigFiles(webModule);
            if (configs != null && configs.length > 0) {
                myMainFacesConfig = FileUtil.toFile(configs[0]);
            }
            else {
                myMainFacesConfig = null;
            }
        }
        myModule = webModule;
        myConfigFiles = null;
        getConfigFiles();
    }
    
    
    public ClassPath getBootPath() {
        return myBootPath;
    }

    public ClassPath getCompilePath() {
        return myCompilePath;
    }

    public ClassPath getSourcePath() {
        return mySourcePath;
    }
    
    public synchronized FileObject getMainFacesConfig(){
        if ( myMainFacesConfig != null && !myMainFacesConfig.exists() ){
            myMainFacesConfig = null;
        }
        return myMainFacesConfig != null ? FileUtil.toFileObject(
                FileUtil.normalizeFile(myMainFacesConfig)) : null;
    }
    
    public synchronized List<FileObject> getConfigFiles(){
        FileObject[] objects = myModule == null ?  myConfigFiles :
                ConfigurationUtils.getFacesConfigFiles( myModule );
        
        Set<FileObject> configs;
        if ( objects != null ){
            configs = new HashSet<FileObject>( Arrays.asList( objects ));
        }
        else {
            configs = new HashSet<FileObject>();
        }
        
        if (myMainFacesConfig == null) {
            List<FileObject> list = getSourcePath().findAllResources( 
                    WEB_INF +"/" +FACES_CONFIG); // NOI18N
            if ( list != null && list.size() > 0 ){
                myMainFacesConfig =  FileUtil.toFile(list.get(0));
            }
        }
        if ( myMainFacesConfig!= null ){
            configs.add( FileUtil.toFileObject( 
                    FileUtil.normalizeFile(myMainFacesConfig) ));
        }
        String suffix = "."+FACES_CONFIG;
        for (FileObject root : getSourcePath().getRoots()) {
            FileObject metaInf = root.getFileObject(META_INF);
            if (metaInf != null) {
                FileObject[] children = metaInf.getChildren();
                for (FileObject fileObject : children) {
                    String name = fileObject.getNameExt();
                    if ( name.equals( FACES_CONFIG) || name.endsWith(suffix )){
                        configs.add( fileObject );
                    }
                }
            }
        }
        return new ArrayList<FileObject>(configs);
    }
    
    private final ClassPath myBootPath;
    private final ClassPath myCompilePath;
    private final ClassPath mySourcePath;
    private File myMainFacesConfig;
    private final WebModule myModule;
    private final FileObject[] myConfigFiles;

}
