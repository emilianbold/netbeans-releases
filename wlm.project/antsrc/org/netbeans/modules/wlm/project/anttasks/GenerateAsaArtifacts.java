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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.project.anttasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.wlm.model.api.TEmail;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMModelFactory;
import org.netbeans.modules.wlm.model.api.WSDLReference;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * 
 * @author SUN Microsystems
 */
public class GenerateAsaArtifacts extends Task {

    
    private static final String WF_SUFFIX = "WF_SUFFIX";

    
    private static final String TASK_COMMON_END_POINT = "TaskCommon_EndPoint";

    private static final String TASK_COMMON_WSDL_URI = "TaskCommon_WSDL_URI";
    
    private static final String TASK_COMMON_INTERFACE = "TaskCommon_Interface";

    private static final String WFSE_WSDL_URI_PREFIX = "WFSE_WSDL_URI_Prefix";

    private static final String WFSE_WSDL_URI_CLIENT_SUFFIX = "WFSE_WSDL_URI_Client_Suffix";
    
   
    private static class Provide {
        private String interfaceName;

        private String serviceName;

        private String endPointName;
    }

    private static class Consumes {
        private String interfaceName;

        private String serviceName;

        private String endPointName;
        
        @Override
        public String toString() {
        	return interfaceName + ":" + serviceName + ":" + endPointName;
        }
        
        @Override
        public boolean equals(Object obj) {
        	boolean result = true;
        	if(!(obj instanceof Consumes)) {
        		return false;
        	}
        	
        	Consumes target = (Consumes) obj;
        	
        	result &= this.interfaceName != null ? this.interfaceName.equals(target.interfaceName) : target.interfaceName == null; 
        	result &= this.serviceName != null ? this.serviceName.equals(target.serviceName) : target.serviceName == null;
        	result &= this.endPointName != null ? this.endPointName.equals(target.endPointName) : target.endPointName == null;
        	
        	return result;
        }
        
        @Override
        public int hashCode() {
        	int code = super.hashCode();
        	if(interfaceName != null) {
        		code += interfaceName.hashCode();
        	}
        	
        	if(serviceName != null) {
        		code += serviceName.hashCode();
        	}
        	
        	if(endPointName != null) {
        		code += endPointName.hashCode();
        	}
        	
        	return code;
        }
        
        public boolean isValid() {
        	boolean valid = true;
        	
        	valid &= this.interfaceName != null;
        	valid &= this.serviceName != null;
        	valid &= this.endPointName != null;
        	
        	return valid;
        }
    }

    private String mSrcDirectoryLocation;

    private String mJbiDescriptorFileLocation;

    private String mProjectName;

    private Properties mProps;

    /** Creates a new instance of GenerateIEPASAArtifacts */
    public GenerateAsaArtifacts() {
        loadProps();
    }

    private void loadProps() {
        mProps = Util.getCommonProperties();

    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return mSrcDirectoryLocation;
    }

    /**
     * @param workflowMapFileLocation
     *            The workflowMapFileLocation to set.
     */
    public void setSrcDirectoryLocation(String workflowMapFileLocation) {
        mSrcDirectoryLocation = workflowMapFileLocation;
    }

    /**
     * @return Returns the portMapFileLocation.
     */
    public String getJbiDescriptorFileLocation() {
        return mJbiDescriptorFileLocation;
    }

    /**
     * @param portMapFileLocation
     *            The portMapFileLocation to set.
     */
    public void setJbiDescriptorFileLocation(String jbiDescriptorFileLocation) {
        mJbiDescriptorFileLocation = jbiDescriptorFileLocation;
    }

    public String getProjectName() {
        return mProjectName;
    }

    public void setProjectName(String projectName) {
        mProjectName = projectName;
    }

    @Override
    public void execute() throws BuildException {
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {            
            Thread.currentThread().setContextClassLoader(XformGenerator.class.getClassLoader());      
        final String wfSuffix = mProps.getProperty(WF_SUFFIX);
        File srcFolder = new File(mSrcDirectoryLocation);
        if (!srcFolder.exists()) {
            throw new BuildException("Folder " + mSrcDirectoryLocation + " does not exit.");
        }
        // Find all .wf file
        String[] allwfs = srcFolder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // TODO Auto-generated method stub
                if (name.endsWith(wfSuffix)) {
                    return true;
                }
                return false;
            }
        });

        Map<String, String> prefixTable = new HashMap<String, String>(); // keyed by prefix
        Map<String, String> nsTable = new HashMap<String, String>(); // keyed by name space
        List<Provide> provides = new ArrayList<Provide>();
        List<Consumes> consumes = new ArrayList<Consumes>();
        
        if (allwfs != null && allwfs.length > 0) {
            int nsIndex = 0;
            try {
                for (int i = 0; i < allwfs.length; i++) {
                    String wfFileName = new File(srcFolder, allwfs[i]).getAbsolutePath();
                    FileObject workFlowFileObj = FileUtil.toFileObject(new File(wfFileName));
                    ModelSource wlmModelSource = Utilities.getModelSource(workFlowFileObj, true);
                    WLMModel wlmModel = WLMModelFactory.getDefault().getModel(wlmModelSource);
                    TTask task = wlmModel.getTask();
                    String wfns = task.getTargetNamespace();
                    String wfnsprefix = "ns" + nsIndex;
                    nsIndex++;
                    prefixTable.put(wfnsprefix, wfns);
                    nsTable.put(wfns, wfnsprefix);

                    Provide provide = new Provide();
                    provides.add(provide);

                    
                    provide.endPointName = task.getOperationAsString();

                    WSDLReference<PortType> portType = task.getPortType ();
                    String bpelwsdlns = portType.getQName().getNamespaceURI();
                    String bpelwsdlnsprefix = nsTable.get(bpelwsdlns);
                    if (bpelwsdlnsprefix == null) {
                        bpelwsdlnsprefix = "ns" + nsIndex;
                        nsIndex++;
                        prefixTable.put(bpelwsdlnsprefix, bpelwsdlns);
                        nsTable.put(bpelwsdlns, bpelwsdlnsprefix);
                    } 

                    provide.interfaceName = bpelwsdlnsprefix + ":" + portType.getQName().getLocalPart();
                    provide.serviceName = provide.interfaceName;
                    nsIndex = createClientAPIProvides(prefixTable, nsTable, provides, task.getName(), nsIndex);
                    
                    
                    //generate consumes entry based on notification
                    //List<Notification> notifications = task.getTaskNotifications();
                    List<TNotification> notifications = task.getNotifications();
                    Iterator<TNotification> it = notifications.iterator();
                    while(it.hasNext()) {
                    	TNotification notification = it.next();
                    	
                    	nsIndex = createNotificationConsumes(prefixTable, 
                    						   nsTable, 
                    						   consumes,
                    						   task,
                    						   notification,
                    						   nsIndex);
                    }
                }
                FileOutputStream fos = null;
                try {
                    // Generate jbi.xml
                    // <?xml version='1.0'?>
                    // <jbi version="1.0"
                    // xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    // xmlns="http://java.sun.com/xml/ns/jbi"
                    // xsi:schemaLocation="http://java.sun.com/xml/ns/jbi jbi.xsd"
                    // xmlns:ns0=${ns1} ... xmlns:nsN=${nsN} >
                    // <services binding-component="false">
                    // <provides interface-name=port-type service-name=partner-link
                    // endpoint-name=role-name/>
                    // <consumes interface-name=port-type service-name=partner-link
                    // endpoint-name=role-name link-type="standard"/>
                    // </services>
                    // </jbi>
                    StringBuffer sb = new StringBuffer();
                    sb.append("<!--start of generated code -->\n");
                    sb.append("<jbi version=\"1.0\"\n");
                    sb.append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
                    sb.append("        xmlns=\"http://java.sun.com/xml/ns/jbi\"\n");
                    sb
                            .append("        xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi jbi.xsd\"\n");
                    for (int i = 0, I = nsTable.size(); i < I; i++) {
                        String ns = "ns" + i;
                        sb.append("        xmlns:" + ns + "=\"" + prefixTable.get(ns) + "\"");
                        if (i < I - 1) {
                            sb.append("\n");
                        }
                    }
                    sb.append(">\n");
                    sb.append("    <services binding-component=\"false\">\n");
                    // Generate all <provides> first
                    for (int i = 0, I = provides.size(); i < I; i++) {
                        Provide provide = (Provide) provides.get(i);
                        sb.append("        <provides interface-name=\"" + provide.interfaceName);
                        sb.append("\" service-name=\"" + provide.serviceName);
                        sb.append("\" endpoint-name=\"" + provide.endPointName);
                        sb.append("\"/>\n");

                    }
                    
                    //then generate all <consumes>
                    for (int i = 0, I = consumes.size(); i < I; i++) {
                    	Consumes consume = (Consumes) consumes.get(i);
                        sb.append("        <consumes interface-name=\"" + consume.interfaceName);
                        sb.append("\" service-name=\"" + consume.serviceName);
                        sb.append("\" endpoint-name=\"" + consume.endPointName);
                        sb.append("\"/>\n");

                    }
                    sb.append("    </services>\n");
                    sb.append(" </jbi>\n");
                    sb.append("<!--end of generated code -->\n");

                    String content = sb.toString();
                    fos = new FileOutputStream(mJbiDescriptorFileLocation);
                    IOUtil.copy(content.getBytes("UTF-8"), fos);
                } catch (Exception e) {
                    e.printStackTrace ();
                    throw e;
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace ();
                throw new BuildException(e.getMessage(), e);
            }
        }
        }catch (Exception e) {
            throw new BuildException (e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }

        // FileOutputStream fos = null;
        // try {
        // List workflowEntryList = WorkflowMapReader.parse(workflowMapFile);
        //            
        // // Populate prefixTable
        // int nsIndex = 1;
        // for (int i = 0, I = workflowEntryList.size(); i < I; i++) {
        // WorkflowMapEntry entry = (WorkflowMapEntry)workflowEntryList.get(i);
        // String ns = entry.getPartnerLink().getNamespaceURI();
        // if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
        // nsTable.put(ns, "ns" + nsIndex);
        // prefixTable.put("ns" + nsIndex, ns);
        // nsIndex++;
        // }
        //                
        // ns = entry.getPortType().getNamespaceURI();
        // if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
        // nsTable.put(ns, "ns" + nsIndex);
        // prefixTable.put("ns" + nsIndex, ns);
        // nsIndex++;
        // }
        //
        // if (entry.getType() != WorkflowMapEntry.REQUEST_REPLY_SERVICE) {
        // ns = entry.getOutPartnerLink().getNamespaceURI();
        // if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
        // nsTable.put(ns, "ns" + nsIndex);
        // prefixTable.put("ns" + nsIndex, ns);
        // nsIndex++;
        // }
        //
        // ns = entry.getOutPortType().getNamespaceURI();
        // if (ns != null && !ns.trim().equals("") && !nsTable.containsKey(ns)) {
        // nsTable.put(ns, "ns" + nsIndex);
        // prefixTable.put("ns" + nsIndex, ns);
        // nsIndex++;
        // }
        // }
        // }
        //            
        //            

        // try {

        // for (int i = 0, I = workflowEntryList.size(); i < I; i++) {
        // WorkflowMapEntry xme = (WorkflowMapEntry)workflowEntryList.get(i);
        // sb.append(" <provides interface-name=\"" + getDottedQName(xme.getPortType(), nsTable));
        // sb.append("\" service-name=\"" + getDottedQName(xme.getPartnerLink(), nsTable));
        // sb.append("\" endpoint-name=\"" + xme.getRoleName());
        // sb.append("\"/>\n");
        // }
        // // Generate all <consumes> second
        // for (int i = 0, I = workflowEntryList.size(); i < I; i++) {
        // WorkflowMapEntry xme = (WorkflowMapEntry)workflowEntryList.get(i);
        // if (!xme.getType().equals(WorkflowMapEntry.REQUEST_REPLY_SERVICE)) {
        // sb.append(" <consumes interface-name=\"" + getDottedQName(xme.getOutPortType(),
        // nsTable));
        // sb.append("\" service-name=\"" + getDottedQName(xme.getOutPartnerLink(), nsTable));
        // sb.append("\" endpoint-name=\"" + xme.getOutRoleName());
        // sb.append("\" link-type=\"standard\"/>\n");
        // }
        // }

        // } catch (Exception e) {
        // throw new BuildException(e.getMessage());
        // }
    }

    private int createClientAPIProvides(Map prefixTable, Map nsTable, List provides,
            String taskName, int nsIndex) {
            
        String clientwsdlns = mProps.getProperty(TASK_COMMON_WSDL_URI);
        String clientwsdlnsprefix = null;
        Provide provide = null;
        if ((clientwsdlnsprefix = (String)nsTable.get(clientwsdlns)) == null) {
            clientwsdlnsprefix = "ns" + nsIndex;
            nsIndex++;
            prefixTable.put(clientwsdlnsprefix, clientwsdlns);
            nsTable.put(clientwsdlns, clientwsdlnsprefix);
    
            provide = new Provide();
            provides.add(provide);
    
            provide.endPointName = mProps.getProperty(TASK_COMMON_END_POINT);
            provide.interfaceName = clientwsdlnsprefix + ":"
                    + mProps.getProperty(TASK_COMMON_INTERFACE);
            provide.serviceName = clientwsdlnsprefix + ":" + mProps.getProperty(Util.TASK_SERVICE_NAME);
        }
        
//        String dynamicClientwsdlns = mProps.getProperty(WFSE_WSDL_URI_PREFIX) + "/" + mProjectName + mProps.getProperty(WFSE_WSDL_URI_CLIENT_SUFFIX);
//        String dynamicClientwsdlnsprefix = null;
//        
//        if ((dynamicClientwsdlnsprefix = (String)nsTable.get(dynamicClientwsdlns)) == null) {
//            dynamicClientwsdlnsprefix = "ns" + nsIndex;
//            nsIndex++;
//            prefixTable.put(dynamicClientwsdlnsprefix, dynamicClientwsdlns);
//            nsTable.put(dynamicClientwsdlns, dynamicClientwsdlnsprefix);
//    
//            provide = new Provide();
//            provides.add(provide);
//            provide.endPointName = Util.getTaskSpecificClientEndpointName(this.mProjectName); 
//            	
//            provide.interfaceName = dynamicClientwsdlnsprefix + ":" + Util.getTaskSpecificClientPortTypeName(taskName);
//                    
//            provide.serviceName = dynamicClientwsdlnsprefix + ":" + Util.getTaskSpecificClientServiceName();
//        }
        
        return nsIndex;
        // TODO Auto-generated method stub

    }

    private int createNotificationConsumes(Map<String, String> prefixTable, 
    									   Map<String, String> nsTable, 
    									   List<Consumes> consumesList,
    									   TTask task,
    									   TNotification notification,
    									   int nsIndex) throws Exception {
    
        if (notification.getEmail() != null) {
            String wfns = notification.getEmail().getPortType().getQName().getNamespaceURI();
            String wfnsprefix =  (String) nsTable.get(wfns);
            if(wfnsprefix == null) {
        	wfnsprefix = "ns" + nsIndex;
        	prefixTable.put(wfnsprefix, wfns);
            nsTable.put(wfns, wfnsprefix);
            nsIndex++;
            }
         
        Consumes consumes = new Consumes();
        
        TEmail email = notification.getEmail();
        
        consumes.interfaceName = wfnsprefix + ":" + email.getPortType().getQName().getLocalPart();
        consumes.endPointName = "emailPort";
	consumes.serviceName = wfnsprefix + ":" + "NotificationHandler";
        
        if(consumes.isValid()) {
	        if(!consumesList.contains(consumes)) {
	        	consumesList.add(consumes);
	        }
        } else {
        	throw new BuildException("Can not generate valid <consumes> entry for " + consumes.toString());
        }
        }
        
        return nsIndex;
    }
    
    private static String getDottedQName(QName qn, Map nsTable) {
        String ns = qn.getNamespaceURI();
        String prefix = (String) nsTable.get(ns);
        if (prefix == null) {
            return qn.getLocalPart();
        }
        return prefix + ":" + qn.getLocalPart();
    }

    public static void main(String[] args) throws MalformedURLException {
        GenerateAsaArtifacts tsk = new GenerateAsaArtifacts();
        tsk.setJbiDescriptorFileLocation("c:/work/test/jbi.xml");
        tsk
                .setSrcDirectoryLocation("c:/Alaska/root/sharedlibrary/workflowmodel/test/data/samples/approvePurchase");
        tsk.execute();

        // String urlStr =
        // "jar:file:/D:/darkhorse/nbbuild/netbeans/BusinessIntegration/modules/ext/workflowpro/anttask.jar!/com/sun/jbi/ui/devtool/projects/workflowpro/anttasks/jbi_gen.properties";
        // String urlStr =
        // "jar:file:/C:/work/test/anttask.jar!/com/sun/jbi/ui/devtool/projects/workflowpro/anttasks/jbi_gen.properties";
        // String urlStr = "file:/C:/work/test/anttask.jar";
        //        
        //       
        // URL url = new URL (urlStr);
        // String file = url.getFile();
        // System.out.println(new File (file).exists());

        // try {
        // FileInputStream fileInputStream = new FileInputStream(url.getFile());
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

}
