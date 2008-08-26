
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.websphere6.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;

import org.netbeans.modules.j2ee.websphere6.config.sync.WarSynchronizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.j2ee.websphere6.util.WSUtil;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Arathi
 * @author Petr Hejl
 */
public class WSWarModuleConfiguration extends WSModuleConfiguration
        implements ContextRootConfiguration, DeploymentPlanConfiguration, PropertyChangeListener {

    public static final String WEB_APP_ID = "WebApp"; // NOI18N

    private static final String WEB_APP_TAG_OPEN = "<web-app"; // NOI18N

    private static final String WEB_APP_TAG_CLOSE = ">"; // NOI18N

    private final File wsWebBndFile;

    private final File wsWebExtFile;

    private WSWebBnd wsWebBnd;

    private WSWebExt wsWebExt;

    private String contextRoot;

    private DataObject [] dataObjects;

    public DataObject [] getDataObject() {
        return dataObjects.clone();
    }

    /**
     * Creates a new instance of WarDeploymentConfiguration.
     */
    public WSWarModuleConfiguration(J2eeModule j2eeModule) {
        super(j2eeModule);

        wsWebBndFile = j2eeModule.getDeploymentConfigurationFile("ibm-web-bnd.xmi"); // NOI18N
        wsWebExtFile = j2eeModule.getDeploymentConfigurationFile("ibm-web-ext.xmi"); // NOI18N

        getWSWebBnd();
        getWSWebExt();

        // TODO this could allow not fully constructed instance to escape
        try {
            DataObject webBndDO = DataObject.find(FileUtil.toFileObject(wsWebBndFile));
            webBndDO.addPropertyChangeListener(this);

            DataObject webExtDO = DataObject.find(FileUtil.toFileObject(wsWebExtFile));
            webExtDO.addPropertyChangeListener(this);

            dataObjects = new DataObject[] {webBndDO, webExtDO};
        } catch (DataObjectNotFoundException donfe) {
            dataObjects = new DataObject[] {};
            Exceptions.printStackTrace(donfe);
        }

        // FIXME we have to create id attribute in webapp element of web.xml
        File webInfFile = j2eeModule.getDeploymentConfigurationFile("WEB-INF/web.xml");
        if (webInfFile.exists()) {
            String contents = WSUtil.readFile(webInfFile);
            String id = "id=\"" + WEB_APP_ID + "\""; // NOI18N
            if (contents != null && contents.indexOf(id) == -1) {
                int startIndex = contents.indexOf(WEB_APP_TAG_OPEN);
                if (startIndex != -1) {
                    String afterTageOpen = contents.substring(startIndex + WEB_APP_TAG_OPEN.length());
                    String tag = afterTageOpen.substring(0, afterTageOpen.indexOf(WEB_APP_TAG_CLOSE));
                    if (tag.indexOf(id) == -1) {
                        StringBuilder replacement = new StringBuilder(WEB_APP_TAG_OPEN);
                        replacement.append(" ").append(id);
                        if (tag.length() != 0) {
                            replacement.append(" \n        ");
                        }
                        WSUtil.writeFile(webInfFile , contents.replaceFirst(WEB_APP_TAG_OPEN,
                                replacement.toString()));
                    }
                }
            }
            final WarSynchronizer webSync = new WarSynchronizer(webInfFile, wsWebBndFile);
            webSync.addSyncFile(webInfFile);
        }
    }

        /**
     * Return WSWebBnd graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     *
     * @return WSWebBnd graph or null if the ibm-web-bnd.xml file is not parseable.
     */
    public final synchronized WSWebBnd getWSWebBnd() {
        if (wsWebBnd == null) {
            try {
                if (wsWebBndFile.exists()) {

                    // load configuration if already exists
                    try {
                        wsWebBnd = new WSWebBnd(wsWebBndFile, false);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                        // websphere.xml is not parseable, do nothing
                    }
                } else {
                    // create websphere.xml if it does not exist yet
                    wsWebBnd = new WSWebBnd();
                    wsWebBnd.setDefaults();
                    writefile(wsWebBndFile, wsWebBnd);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return wsWebBnd;
    }


      /**
     * Return WSWebBnd graph. If it was not created yet, load it from the file
     * and cache it. If the file does not exist, generate it.
     *
     *
     * @return WSWebBnd graph or null if the ibm-web-bnd.xml file is not parseable.
     */
    public final synchronized WSWebExt getWSWebExt() {
        if (wsWebExt == null) {
            try {
                if (wsWebExtFile.exists()) {

                    // load configuration if already exists
                    try {
                        wsWebExt = new WSWebExt(wsWebExtFile, false);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (RuntimeException re) {
                    // websphere.xml is not parseable, do nothing
                    }
                } else {
                    // create websphere.xml if it does not exist yet
                    wsWebExt = new WSWebExt();
                    wsWebExt.setDefaults();
                    writefile(wsWebExtFile, wsWebExt);
                }
            } catch (ConfigurationException ce) {
                Exceptions.printStackTrace(ce);
            }
        }
        return wsWebExt;
    }

    // Unfortunately the deployment plan format seems to be well kept secret
    // If we found the format we can use it instead of this (would be much better)
    // SOLUTION could be - save application.xml here and get it in deployment manager - creating the ear
    public void save(OutputStream os) throws ConfigurationException {
        // TODO I18N messages, escape char entities
        try {
            FileObject file = getJ2eeModule().getArchive();
            if (file == null) {
                throw new ConfigurationException("Unknow war name to deploy");
            }
            String name = file.getNameExt();
            StringBuffer appXml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if (J2eeModule.J2EE_13.equals(getJ2eeModule().getModuleVersion())) {
                appXml.append("<application xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" id=\"Application_ID\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\">");
            } else {
                appXml.append("<application xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" id=\"Application_ID\" version=\"1.4\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\">");
            }
            appXml.append("<display-name>").append(name.replace('.', '_')).append("</display-name>");
            appXml.append("<module>");
            appXml.append("<web>");
            appXml.append("<web-uri>").append(name).append("</web-uri>");
            appXml.append("<context-root>").append(contextRoot).append("</context-root>");
            appXml.append("</web>");
            appXml.append("</module>");
            appXml.append("</application>");
            os.write(appXml.toString().getBytes("UTF-8")); // NOI18N
        } catch (IOException ex) {
		ex.printStackTrace();
            throw new ConfigurationException("Unknow war name to deploy");
        }
    }


    // TODO: this contextPath fix code will be removed, as soon as it will
    // be moved to the web project
    private boolean isCorrectCP(String contextPath) {
        boolean correct=true;
        if (!contextPath.equals("") && !contextPath.startsWith("/")) correct=false; //NOI18N
        else if (contextPath.endsWith("/")) correct=false; //NOI18N
        else if (contextPath.indexOf("//")>=0) correct=false; //NOI18N
        return correct;
    }


    public String getContextRoot() throws ConfigurationException {
        return contextRoot;
    }

     /**
     * Set context path.
     */
    public void setContextRoot(String contextPath)  {
        // TODO: this contextPath fix code will be removed, as soon as it will
        // be moved to the web project
        if (!isCorrectCP(contextPath)) {
            String ctxRoot = contextPath;
            java.util.StringTokenizer tok = new java.util.StringTokenizer(contextPath,"/"); //NOI18N
            StringBuffer buf = new StringBuffer(); //NOI18N
            while (tok.hasMoreTokens()) {
                buf.append("/"+tok.nextToken()); //NOI18N
            }
            ctxRoot = buf.toString();
            NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(WSWarModuleConfiguration.class, "MSG_invalidCP", contextPath),
                    NotifyDescriptor.Message.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            contextPath = ctxRoot;
        }
        contextRoot = contextPath;
    }




    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED) &&
                evt.getNewValue() == Boolean.FALSE) {
            // dataobject has been modified, ibmWebApp graph is out of sync
            synchronized (this) { 
                wsWebBnd = null;
                wsWebExt = null;
            }
        }
    }

}
