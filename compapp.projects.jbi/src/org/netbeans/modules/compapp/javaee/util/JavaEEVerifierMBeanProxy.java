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

package org.netbeans.modules.compapp.javaee.util;

import com.sun.jbi.ui.common.FileTransferManager;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import org.netbeans.modules.compapp.projects.jbi.AdministrationServiceHelper;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.sun.manager.jbi.management.JBIClassLoader;
import org.netbeans.modules.sun.manager.jbi.management.connectors.HTTPServerConnector;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.NbBundle;

/**
 *
 * @author gpatil
 */
public class JavaEEVerifierMBeanProxy {
    private static final String MB_VERIFY_OPERATION = "verifyServiceUnit"; //NOI18N
    private static String sJbiJmxDomain = null;
    private static String TARGET = "server" ; //NOI18N
    private static String getJbiJmxDomain()
    {
        if(sJbiJmxDomain == null)
            sJbiJmxDomain = System.getProperty("jbi.jmx.domain", "com.sun.jbi"); //NOI18N
        return sJbiJmxDomain;
    }
    
    private static ObjectName getAppVerifierMBeanObjectName() throws MalformedObjectNameException {
        String mbeanRegisteredName = (new StringBuilder()).append(
                getJbiJmxDomain()).append(":").append("ServiceName").append("=") //NOI18N
                .append("JavaEEVerifier").append(",").append("ComponentType") //NOI18N
                .append("=").append("System").toString(); //NOI18N
       return new ObjectName(mbeanRegisteredName);        
    }
        
    private static MBeanServerConnection getMBeanServerConnection(
            String hostName, String port, String userName, String password,
            JBIClassLoader jbiClassLoader)
            throws MalformedURLException, IOException, MalformedObjectNameException {
        HTTPServerConnector connector = new HTTPServerConnector(
                hostName, port, userName, password, jbiClassLoader);
        return connector.getConnection();
    }
    
    private static MBeanServerConnection getMBeanServerConnection(
            ServerInstance si) throws MalformedURLException, IOException, 
            MalformedObjectNameException {
            return getMBeanServerConnection(si.getHostName(),
                    si.getAdminPort(),
                    si.getUserName(),
                    si.getPassword(), 
                    new JBIClassLoader(si));
    }

    private static boolean isRemoteHost(String hostname) {
        boolean ret = false;
        try {
            InetAddress addr = InetAddress.getByName(hostname);
            if (!addr.isLoopbackAddress()){
                InetAddress[] a = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
                boolean local = false;
                for (InetAddress locAddr: a){
                    if (locAddr.equals(addr)){
                        local = true;
                        break;
                    }
                }
                if (!local){
                    ret = true;
                }
            }
        } catch (UnknownHostException e) {
            ret = true;
        }

        return ret;
    }
    
    /**
     * 
     * @param ftpManager Ftp connection to MBean server
     * @param localFilePath
     * @return [0] - uploaded path in the server. [1] upload id
     * @throws java.lang.Exception
     */
    private static Object[] uploadFile(FileTransferManager ftpManager, 
            String localFilePath) throws Exception {
        File uploadFile = new File(localFilePath);
        Object mLastUploadId = null;
        Object[] ret = new Object[2];
       
        String uploadedFilePath = ftpManager.uploadArchive(uploadFile);
        mLastUploadId = ftpManager.getLastUploadId();
        ret[0] = uploadedFilePath;
        ret[1] = mLastUploadId;
        return ret;
    }    
        
    public static List<JavaEEVerifierReportItem> verifyApplication(
            AntProjectHelper aph, String saUrl, String suName) 
            throws MalformedObjectNameException, MalformedURLException, 
            InstanceNotFoundException, ReflectionException, IOException, 
            MBeanException, IntrospectionException, Exception{
        
        List<JavaEEVerifierReportItem> ret = 
                new ArrayList<JavaEEVerifierReportItem>();
        
        ObjectName mbeanName = getAppVerifierMBeanObjectName();
        String[] sig = new String[] {
            "String", "String", "String"  //NOI18N
        };
                
        String siStr = aph.getStandardPropertyEvaluator().getProperty(
                JbiProjectProperties.J2EE_SERVER_INSTANCE);
        String nbUsrDir = System.getProperty("netbeans.user");  // NOI18N         
        ServerInstance si = AdministrationServiceHelper.getServerInstance(
                nbUsrDir, siStr);
        boolean remoteHost = isRemoteHost(si.getHostName());
        
        MBeanServerConnection conn = getMBeanServerConnection(si);
        
        if (conn == null){
            String msg = NbBundle.getMessage(JavaEEVerifierMBeanProxy.class, 
                    "msg_unable_connect_mbean");//NOI18N
            throw new IOException(msg);
        }
        
        //Thorws instanceNotFoundException
        MBeanInfo info = conn.getMBeanInfo(mbeanName);
        
        Object tabuli = null;
        
        if (remoteHost){
            FileTransferManager ftpManager = new FileTransferManager(conn);
            Object[] up = uploadFile(ftpManager, saUrl);
            
            String[] param = new String[] {
                ((String)up[0]), suName, TARGET
            };
            
            tabuli = conn.invoke(mbeanName, MB_VERIFY_OPERATION, param,sig);
            ftpManager.removeUploadedArchive(up[1]);
        } else {
            String[] param = new String[] {
                //"C:\\temp\\CompositeApp1.zip", "EJBModule2.jar", "server"
                //"C:\\User\\gmp_xfr\\CompositeApp1.zip", "EJBModule2.jar", "server"
                saUrl, suName, TARGET
            };
            
            tabuli = conn.invoke(mbeanName, MB_VERIFY_OPERATION, param,sig);
        }
        
        if (tabuli instanceof TabularData){
            TabularData td = (TabularData) tabuli;
            CompositeData cd = null;
            Collection vls = td.values();
            Iterator itr = vls.iterator();
            JavaEEVerifierReportItem ri = null;

            while (itr.hasNext()){
                cd = (CompositeData) itr.next();
                ri = new JavaEEVerifierReportItem();
                ri.setFileName((String) cd.get(JavaEEVerifierReportItem.KEY_FL_NAME));
                ri.setJndiName((String) cd.get(JavaEEVerifierReportItem.KEY_JNDI_NAME));
                ri.setExpectedClass((String) cd.get(JavaEEVerifierReportItem.KEY_EXPECTED_CLASS));
                ri.setMessage((String) cd.get(JavaEEVerifierReportItem.KEY_MSG));
                ri.setReferencingClass((String) cd.get(JavaEEVerifierReportItem.KEY_REFERENCING_CLASS));
                ri.setReferencingEjb((String) cd.get(JavaEEVerifierReportItem.KEY_REFERENCING_EJB));
                Integer i = (Integer) cd.get(JavaEEVerifierReportItem.KEY_STATUS);
                if (i != null){
                    ri.setStatus("" + i.intValue());//NOI18N
                } else {
                    ri.setStatus("0"); //NOI18N
                }
                ret.add(ri);
            }
        }
        
        return ret;
    }
}
