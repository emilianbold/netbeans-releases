/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.soa.pojo.util;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.soa.pojo.model.api.OperationMetadata;
import org.netbeans.modules.soa.pojo.model.api.PortTypeMetadata;
import org.netbeans.modules.soa.pojo.model.api.WSDLMetadata;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sgenipudi
 */
public class WSDLUtil {

    ApacheResolverHelper apacheResolverHelper = new ApacheResolverHelper();

    public WSDLModel getWSDLModel(FileObject fo) {
        ModelSource modelSource =
                Utilities.getModelSource(fo, true);
        WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(modelSource);
        if (wsdlModel != null && wsdlModel.getState() == Model.State.NOT_WELL_FORMED) {
            return null;
        }
        return wsdlModel;

    }

    public WSDLModel getWSDLModel(String url, Project project, WizardDescriptor wizDesc) {
        URI uri;
        WSDLModel wsdlModel = null;
        try {
            
            //File tempFile = File.createTempFile("WSDL", "cache");
            SourceGroup[] sg = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            FileObject projectDir = project.getProjectDirectory();
            String tempDir ="temp"+System.currentTimeMillis();//NOI18N
            String tempDirName = "build/"+tempDir;//NOI18N
            File buildFolder = new File(FileUtil.toFile(projectDir), "build");//NOI18N
            File tempFileFolder = new File(buildFolder,tempDir);
            File tempFolder = new File(FileUtil.toFile(projectDir), tempDirName);//NOI18N
            tempFolder.mkdirs();
            
            FileObject tempFO = projectDir.getFileObject(tempDirName); //FileUtil.toFileObject(tempFolder);            
            wizDesc.putProperty(GeneratorUtil.POJO_TEMP_FOLDER, tempFileFolder );
            
            FileObject WSDLFileFO = null;
            try {
                WSDLFileFO = 
                        Retriever.getDefault().retrieveResource(tempFO , new URI(url));
                
            } catch (Exception e) {//ignore all exceptions 
                e.printStackTrace();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(e));
                return null;
            } 
            
            ModelSource modelSource =
                    Utilities.getModelSource(WSDLFileFO, true);
            wsdlModel = WSDLModelFactory.getDefault().getModel(modelSource);
            try {

                tempFO.lock().releaseLock();
                tempFO.delete();
                tempFileFolder.delete();
            }catch (Exception ex) {
                
            }
            if (wsdlModel != null && wsdlModel.getState() == Model.State.NOT_WELL_FORMED) {
                return null;
            }
        } catch (Exception ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(ex));
            return null;
        }

        return wsdlModel;

    }

    public static WSDLMetadata getInterfaceNames(WSDLModel wsdlModel) {
        WSDLMetadata result = new WSDLMetadata();

        Definitions def = wsdlModel.getDefinitions();
        Collection<PortType> colPortTypes = def.getPortTypes();
        for (PortType pt : colPortTypes) {
            PortTypeMetadata ptMD = new PortTypeMetadata();
            ptMD.setPortType(new QName(def.getTargetNamespace(),pt.getName()));
            Collection<Operation> colOp = pt.getOperations();
            for (Operation op : colOp) {
                OperationMetadata opn = new OperationMetadata(ptMD);
                opn.setOperationName(op.getName());
                if ( op.getOutput() != null) {
                    opn.setOutputName(op.getOutput().getName());
                    opn.setOutMessageName(op.getOutput().getMessage().getQName());                    
                }
                
                if ( op.getInput() != null) {
                    opn.setInputName(op.getInput().getName());
                    opn.setInputMessageName(op.getInput().getMessage().getQName());
                }
                
                ptMD.getOperationMetadataList().add(opn);
            }
            result.getPortTypeMetadaList().add(ptMD);            
        }

        return result;
    }
}
