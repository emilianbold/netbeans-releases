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
package org.netbeans.modules.websvc.core.jaxws.saas;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author rico
 */
public class RestResourceGenerator {

    public static final String RESOURCE_TEMPLATE = "Templates/WebServices/GenericResource.java"; //NOI18N

    public static final String SWDP_LIBRARY = "restlib"; //NOI18N

    public static final String RESTAPI_LIBRARY = "restapi"; //NOI18N
    private static final String [] CLASSPATHTYPES = new String[]{"javac.classpath"};

    private FileObject folder;
    private URL wsdlURL;
    private WsdlModel wsdlModel;
    private RestWrapperForSoapGenerator generator;

    public RestResourceGenerator(FileObject folder, URL wsdlURL) {
        this.folder = folder;
        this.wsdlURL = wsdlURL;
    }

    public void generate() {
        try {
            addSwdpLibrary(CLASSPATHTYPES);
            WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlURL);
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                public void modelCreated(WsdlModel model) {
                    wsdlModel = model;
                    FileObject targetFile = null;
                    List<WsdlService> services = wsdlModel.getServices();
                    for (WsdlService service : services) {
                        List<WsdlPort> ports = service.getPorts();
                        for (WsdlPort port : ports) {
                            targetFile = JavaSourceHelper.createJavaSource(RESOURCE_TEMPLATE, folder, folder.getName(), port.getName());
                            List<WSOperation> operations = port.getOperations();
                            for (WSOperation operation : operations) {
                                try {
                                    new RestWrapperForSoapGenerator(service, port, operation, targetFile).generate();
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ex);
                                }
                            }
                        }
                    }
                    try {
                        openFileInEditor(DataObject.find(targetFile)); //display in the editor
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    
    public static void openFileInEditor(DataObject dobj) {

        final OpenCookie openCookie = dobj.getCookie(OpenCookie.class);
        if (openCookie != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    openCookie.open();
                }
            }, 1000);
        } else {
            final EditorCookie ec = dobj.getCookie(EditorCookie.class);
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ec.open();
                }
            }, 1000);
        }
    }

    
      /**
     *  Add SWDP library for given source file on specified class path types.
     * 
     *  @param source source file object for which the libraries is added.
     *  @param classPathTypes types of class path to add ("javac.compile",...)
     */
    private void addSwdpLibrary(String[] classPathTypes) throws IOException {
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary == null) {
            return;
        }

        Library restapiLibrary = LibraryManager.getDefault().getLibrary(RESTAPI_LIBRARY);
        if (restapiLibrary == null) {
            return;
        }
        Project project = FileOwnerQuery.getOwner(folder);
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs == null || sgs.length < 1) {
            throw new IOException("Project has no Java sources"); //NOI18N

        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        for (String type : classPathTypes) {
            try {
                ProjectClassPathModifier.addLibraries(new Library[]{restapiLibrary, swdpLibrary}, sourceRoot, type);
            } catch (UnsupportedOperationException ex) {
                Logger.getLogger(getClass().getName()).info(type + " not supported.");
            }
        }
    }
}
