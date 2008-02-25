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
package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;


/**
 * 
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2Configuration implements DeploymentConfiguration, XpathListener {

    private J2eeModule j2eeModule;
    private String contextPath;

    /**
     * 
     * @param j2eeModule 
     */
    public Hk2Configuration (DeployableObject dObj) {
        throw new UnsupportedOperationException("JSR-88 support is deprecated.");
    }

    /**
     * 
     * @param j2eeModule 
     */
    public Hk2Configuration (J2eeModule j2eeModule) {
        this.j2eeModule = j2eeModule;
    }

    /**
     * 
     * @param file 
     */
    public void init(File[] configFiles) {
//        if(configFiles != null && configFiles.length > 0) {
//            try {
//                File file = configFiles[0];
//                FileObject folder = FileUtil.toFileObject(file.getParentFile());
//                if (folder == null) {
//                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The parent folder does not exist!"); // NOI18N
//                    return;
//                }
//                PrintWriter pw = null;
//                FileLock lock = null;
//                try {
//                    String name = file.getName();
//                    FileObject fo = folder.getFileObject(name);
//                    if (fo == null) {
//                        fo = folder.createData(name);
//                    }
//                    lock = fo.lock();
//                    pw = new PrintWriter(new OutputStreamWriter(fo.getOutputStream(lock)));
//                    pw.println("<MyServer path=\"/mypath\"/>"); // NOI18N
//                } finally {
//                    if (pw != null) {
//                        pw.close();
//                    }
//                    if (lock != null) {
//                        lock.releaseLock();
//                    }
//                }
//            } catch (IOException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            }
//        }

        // web.xml represented as DDBean model
//        DDBeanRoot root = j2eeModule.getDDBeanRoot();
//        if (root != null) {
//            // here we will listen to resource reference changes
//            root.addXpathListener("/web-app/resource-ref", this); // NOI18N
//        }
    }

    /**
     * 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public String getContextPath() {
        // TODO: replace this with reading the context path from the server specific DD
        return this.contextPath;
    }

    /**
     * 
     * @param contextPath 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public void setContextPath(String contextPath) {
        // TODO: here put the code that will store the context path in the server specific DD
        this.contextPath = contextPath;
    }

    // XpathListener implementation -------------------------------------------

    /**
     * 
     * @param xpe 
     */
    public void fireXpathEvent(XpathEvent xpe) {
        DDBean eventDDBean = xpe.getBean();
        if ("/web-app/resource-ref".equals(eventDDBean.getXpath())) { // NIO18N
            // new resource reference added
            if (xpe.isAddEvent()) {
                String[] name = eventDDBean.getText("res-ref-name"); // NOI18N
                String[] type = eventDDBean.getText("res-type");     // NOI18N
                String[] auth = eventDDBean.getText("res-auth");     // NOI18N
                // TODO: take appropriate steps here
            }
        }
    }

    // JSR-88 methods ---------------------------------------------------------

    /**
     * 
     * @return 
     */
    public DeployableObject getDeployableObject () {
//        System.out.println("in getDeployableObject" +deplObj);
//        return deplObj;
        throw new UnsupportedOperationException("Support for JSR-88 DeployableObject is deprecated.");
    }

    /**
     * 
     * @param os 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public void save(OutputStream os) throws ConfigurationException {   
    }

    /**
     * 
     * @param dDBeanRoot 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public DConfigBeanRoot getDConfigBeanRoot (DDBeanRoot dDBeanRoot) 
            throws ConfigurationException {
        return null;
    }

    /**
     * 
     * @param dConfigBeanRoot 
     * @throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException 
     */
    public void removeDConfigBean (DConfigBeanRoot dConfigBeanRoot) 
            throws BeanNotFoundException {
    }

    /**
     * 
     * @param is 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public void restore (InputStream is) 
            throws ConfigurationException {
    }

    /**
     * 
     * @param is 
     * @param dDBeanRoot 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public DConfigBeanRoot restoreDConfigBean (InputStream is, DDBeanRoot dDBeanRoot) 
            throws ConfigurationException {
        return null;
    }

    /**
     * 
     * @param os 
     * @param dConfigBeanRoot 
     * @throws javax.enterprise.deploy.spi.exceptions.ConfigurationException 
     */
    public void saveDConfigBean (OutputStream os, DConfigBeanRoot dConfigBeanRoot) 
            throws ConfigurationException {
    }
}
