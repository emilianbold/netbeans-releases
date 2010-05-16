/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.listeners;

import java.io.File;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.Util;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sgenipudi
 */
public class POJOPalleteProviderFileListener implements FileChangeListener{
    private Map map = null;
    private String outputType = null;
    private Project project = null;
    private FileObject javaFo = null;

    public POJOPalleteProviderFileListener(FileObject fo, Map<String, Object> unmodifiableMap, String outputType, Project project) {
        this.map = unmodifiableMap;
        this.outputType = outputType;
        this.project = project;
        this.javaFo = fo;
        javaFo.addFileChangeListener(this);
    }
    
    
    public void fileFolderCreated(FileEvent arg0) {
        
    }

    public void fileDataCreated(FileEvent arg0) {        
    }
    
    public void fileChanged(FileEvent arg0) {
        if ( this.map != null) {
            FileObject fob = arg0.getFile();
            if ( fob != null) {
                if ( Util.isValidPOJOFile(fob)){
                    // Generate the WSDL 
                    // Save the POJO Node.
                    File pojoFile = new File((String) map.get(GeneratorUtil.POJO_FILE_LOCATION));
                    Boolean generateWSDL = (Boolean) map.get(GeneratorUtil.POJO_GENERATE_WSDL);
                    String wsdlLoc = null;
                    if (generateWSDL.booleanValue()) {
                        wsdlLoc = GeneratorUtil.generateWSDL(this.project, FileUtil.toFileObject(pojoFile.getParentFile()) , map,!outputType.equals(GeneratorUtil.VOID_CONST));
                    }
                    
                    POJOProvider pojo = new POJOProvider();
                    pojo.setClassName((String)map.get(GeneratorUtil.POJO_CLASS_NAME));
                    pojo.setPackage((String)map.get(GeneratorUtil.POJO_PACKAGE_NAME));
                    
                    if ( wsdlLoc == null) {
                        wsdlLoc = (String)map.get(GeneratorUtil.POJO_BC_WSDL_LOC);
                        File wsdlFile = new File(wsdlLoc);
                        wsdlLoc = FileUtil.getRelativePath(this.project.getProjectDirectory(), FileUtil.toFileObject(wsdlFile));
                        pojo.setUpdateWsdlDuringBuild(false);
                    }

                    pojo.setWsdlLocation(wsdlLoc);
                    Util.addPOJO2Model(project, pojo);                
                }
            }
        }
        uninitialize();
        
    }
    
    private void uninitialize() {
        this.map = null;
        this.outputType = null;
        this.project = null;
        javaFo.removeFileChangeListener(this);
        this.javaFo = null;

    }

    public void fileDeleted(FileEvent arg0) {
         uninitialize();
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void fileRenamed(FileRenameEvent arg0) {
         uninitialize();
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void fileAttributeChanged(FileAttributeEvent arg0) {
         uninitialize();
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
