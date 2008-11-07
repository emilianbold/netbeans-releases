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

package org.netbeans.modules.maven.jaxws;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.spi.customizer.ModelHandleUtils;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkuchtiak
 */
public class MavenJAXWSSupportIml implements JAXWSLightSupportImpl {
    Project prj;
    private List<JaxWsService> services = new LinkedList<JaxWsService>();
    public static final String CATALOG_PATH = "src/jax-ws-catalog.xml"; //NOI18N
    
    MavenJAXWSSupportIml(Project prj) {
        this.prj = prj;
    }
    
    public void addService(JaxWsService service) {
        services.add(service);
    }

    public List<JaxWsService> getServices() {
        return services;
    }

    public void removeService(JaxWsService service) {
        services.remove(service);
    }

    public JaxWsService getService(String implClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFromWSDL(JaxWsService service) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FileObject getLocalWsdlFolder(boolean createFolder) {
        String wsdlFolderPath = getWsdlDir();
        File wsdlDir = FileUtilities.resolveFilePath(FileUtil.toFile(prj.getProjectDirectory()), wsdlFolderPath);
        if (wsdlDir.exists()) {
            return FileUtil.toFileObject(wsdlDir);
        }
        else if (createFolder) {
            boolean created = wsdlDir.mkdirs();
            if (created) {
                return FileUtil.toFileObject(wsdlDir);
            }
        }
        return null;
    }

    public FileObject getBindingsFolder(boolean createFolder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getCatalog() {
        File catalogFile = FileUtilities.resolveFilePath(FileUtil.toFile(prj.getProjectDirectory()), CATALOG_PATH);
        try {
            return catalogFile.toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public FileObject getDeploymentDescriptorFolder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {        
            J2eeModuleProvider j2eeModuleProvider = prj.getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null) {
                webservicesMetadataModel = j2eeModuleProvider.getJ2eeModule().getMetadataModel(WebservicesMetadata.class);
            }
        }
        return webservicesMetadataModel;
    }
    
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    
    private String getWsdlDir() {
        Plugin jaxWsPlugin = null;
        try {
            ModelHandle mavenHandle = ModelHandleUtils.createModelHandle(prj);
            jaxWsPlugin = MavenModelUtils.getJaxWSPlugin(mavenHandle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (jaxWsPlugin != null) {
            String dirPath = PluginPropertyUtils.getPluginProperty(prj, "org.codehaus.mojo", "jaxws-maven-plugin", "wsdlDirectory", 
                    "wsimport");
            if (dirPath != null) {
                return dirPath;
            } else {
                return "src/wsdl"; //NOI18N
            }
        } else {
            return "src/wsdl"; //NOI18N
        }        
    }


}
