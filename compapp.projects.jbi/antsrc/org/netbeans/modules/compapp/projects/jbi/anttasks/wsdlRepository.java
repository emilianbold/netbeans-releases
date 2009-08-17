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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.xml.namespace.QName;
import org.apache.tools.ant.BuildException;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.PtConnection;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileUtil;

/**
 * WSDL repoository of the JBI project
 *
 * @author tli
 * @author jqian
 */
public class wsdlRepository {
    
    private Project project;
    private Task task;
    
    private List<WSDLModel> wsdlModels = null;
    private List<WSDLModel> suWsdlModels = null;

    /**
     * A list of WSDL models defined in external SE SU projects.
     * WSDL Ports defined in these models are completely ignored from the
     * CompApp project build process point of view.
     * Only PortTypes defined in these models are useful.
     */
    private List<WSDLModel> externalSuWsdlModels = null;

    // mapping PortType QName to PortType
    private Map<String, PortType> portTypes = new HashMap<String, PortType>();
    
    // mapping Binding QName to Binding
    private Map<String, Binding> bindings = new HashMap<String, Binding>();
    
    // mapping PartnerLinkType QName to PartnerLinkType
    private Map<String, PartnerLinkType> partnerLinkTypes = new HashMap<String, PartnerLinkType>();
    
    // mapping Service QName to Service
    private Map<String, Service> services = new HashMap<String, Service>();
    
    // mapping Port QName to Port
    private Map<String, Port> ports = new HashMap<String, Port>();
    
    // mapping Port to BC name
    private Map<Port, String> port2BC = new HashMap<Port, String>();
    
    // mapping PortType QName string to PtConnection
    private Map<String, PtConnection> connections = new HashMap<String, PtConnection>();
    
    // mapping bc namespace to bc name
    private Map<String, String> bcNS2NameMap;

    // a list of external SU names
    private List<String> externalSuNames;

    private static final String WSDL_FILE_EXTENSION = "wsdl";
    //private static final WSDLFileFilter WSDL_FILE_FILTER = new WSDLFileFilter();
    
    private static QName SOAP_ADDRESS_QNAME =
            new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
    
    private String DUMMY_SOAP_LOCATION = "REPLACE_WITH_ACTUAL_URL";


    /**
     *
     * @param project           the compapp project as an Ant project
     * @param task              the Ant task
     * @param externalSUNames   a non-null list of names for external SU
     * @param bcNS2NameMap      a map mapping BC namespace to name
     */
    public wsdlRepository(Project project, Task task, 
            List<String> externalSuNames, Map<String, String> bcNS2NameMap) {
        
        this.project = project;
        this.task = task;
        this.externalSuNames = externalSuNames;
        this.bcNS2NameMap = bcNS2NameMap;
        
        wsdlModels = getAllWsdlModels(project);        
        
        initLists();

        //also look into all SE.jars
    }    
        
    public List<WSDLModel> getWsdlCollection() {
        return wsdlModels;
    }
            
    /**
     * Gets all the WSDL files from both SU projects and JBI project.
     */
    private List<File> getAllWsdlFiles(Project project) {
        List<File> ret = new ArrayList<File>();  
        
        String srcPath = project.getProperty("basedir") + 
                File.separator + "src" + File.separator;
        String serviceUnitsDirLoc = srcPath + JbiProjectConstants.FOLDER_JBISERVICEUNITS;
        String jbiAsaDirLoc = srcPath + JbiProjectConstants.FOLDER_JBIASA;
                
        File serviceUnitsDir = new File(serviceUnitsDirLoc);
        File jbiASADir = new File(jbiAsaDirLoc);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".wsdl");
            }
        };
        
        // Note that all files under src/jbiasa/ have been copied over to 
        // src/jbiServiceUnits/ at the beginning of jbi-build.
        // For all the WSDLs defined in JBI project, we want to use the  
        // original R/W copy under src/jbiasa/ instead of the R/O copy under 
        // src/jbiServiceUnits/.
        
        // jbiASAChildNames: jbiasa/*/*.wsdl relative to jbiasa/
        // (See IZ #148697)
        List<String> jbiASAChildNames = new ArrayList<String>();
//        for (File file : jbiASADir.listFiles()) {
//            jbiASAChildNames.add(file.getName());
//        }
        for (File file : MyFileUtil.listFiles(jbiASADir, filter, true)) {
            jbiASAChildNames.add(MyFileUtil.getRelativePath(jbiASADir, file));
        }

        // 02/12/08, add all CompApp wsdls first...
        // Add all the WSDLs defined in JBI project (under src/jbiasa/).
        ret.addAll(MyFileUtil.listFiles(jbiASADir, filter, true));

        // Add all WSDLs coming from SU projects
//        for (File file : serviceUnitsDir.listFiles()) {
//            String fileName = file.getName();
//            // Skip <compapp>.wsdl and other wsdl files or directories 
//            // defined under src/jbiasa/.
//            if (!jbiASAChildNames.contains(fileName)) {
//                ret.addAll(MyFileUtil.listFiles(file, filter, true));
//            }
//        }
        for (File file : MyFileUtil.listFiles(serviceUnitsDir, filter, true)) {
            // relativePath: jbiServiceUnits/*/*.wsdl relative to jbiServiceUnits
            String relativePath = MyFileUtil.getRelativePath(serviceUnitsDir, file);
            if (!jbiASAChildNames.contains(relativePath)) {
                ret.add(file);
            }
        }

        return ret;
    }
    
    private List<WSDLModel> getAllWsdlModels(Project project) {        
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        suWsdlModels = new ArrayList<WSDLModel>();
        externalSuWsdlModels = new ArrayList<WSDLModel>();

        WSDLModelFactory wsdlModelFactory = WSDLModelFactory.getDefault();
        
        for (File wsdlFile : getAllWsdlFiles(project)) {

            try {
                ModelSource ms = null;                
                try {
                    FileObject fo = FileUtil.toFileObject(wsdlFile);
                    fo.refresh();
                    ms = Utilities.createModelSource(fo, false);
                } catch (Exception e) {      // from command line
                    Lookup lookup = Lookups.fixed(new Object[]{
                        wsdlFile,
                        getDocument(wsdlFile),
                        WSDLCatalogModel.getDefault()
                    });
                    ms = new ModelSource(lookup, false);
                }
                
                WSDLModel wm = wsdlModelFactory.createFreshModel(ms); 
                ret.add(wm);

                if (isWSDLDefinedInSUProject(wsdlFile)) { // NOI18N
                    suWsdlModels.add(wm);

                    // keep track of WSDLs from external SE SUs
                    if (isWSDLDefinedInExternalSUProject(wsdlFile)) {
                        externalSuWsdlModels.add(wm);
                    }
                }
            } catch (CatalogModelException ex) {
                ex.printStackTrace();
            }            
        }
        
        return ret;
    }    
        
    /**
     * Implementation of CatalogModel
     * @param file
     * @return
     * @throws org.netbeans.modules.xml.xam.locator.CatalogModelException 
     */
    protected Document getDocument(File file) throws CatalogModelException{
        Document result = null;
        
        try {
            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
            result = new PlainDocument();
            result.remove(0, result.getLength());
            fis.read(buffer);
            fis.close();
            String str = new String(buffer);
            result.insertString(0,str,null);            
        } catch (Exception ex) {
            throw new CatalogModelException(file.getAbsolutePath() + " not found.");
        }
        
        return result;
    }
    
    public static String getWsdlFilePath(WSDLModel doc) {
        String wsdlFilePath = null;
        Lookup lookup = doc.getModelSource().getLookup();
        File wsdlFile = lookup.lookup(File.class);
        if (wsdlFile == null) {
            FileObject wsdlFileObject = lookup.lookup(FileObject.class);
            wsdlFile = FileUtil.toFile(wsdlFileObject);
        }
        wsdlFilePath = wsdlFile.getPath();
        return wsdlFilePath;
    }

    public boolean isJavaEEWsdl(WSDLModel doc) {
        String wsdlFilePath = getWsdlFilePath(doc).toUpperCase().replace('\\', '/');  // NOI18N

        // todo: 03/26/07, need to handle Catalog WSDLs..
        int idx = wsdlFilePath.indexOf("META-INF"); // NOI18N
        if (idx < 0) {
            return false;
        }

        boolean inJavaEEProject = (wsdlFilePath.indexOf("META-INF/CATALOGDATA/") < 0);  // NOI18N
        return  inJavaEEProject;
    }
    
    public void initLists() {
        /*
        todo: need a flag to indicat the source wsdls is editable or not...
        subproject wsdls should not be editable.. may be shared by other JBI projects
        jbiproject wsdls should be editable..
         */
        for (WSDLModel wsdlModel : wsdlModels) {
            String wsdlFilePath = getWsdlFilePath(wsdlModel);
            Definitions def = wsdlModel.getDefinitions();
            if (def == null) {
                task.log("ERROR: Malformed WSDL file: " + wsdlFilePath);
            }
            String tns = def.getTargetNamespace();

            // Collect portTypes... (PortType QName -> PortType)
            for (PortType pt : def.getPortTypes()) {
                String key = getQName(tns, pt.getName());
                if (portTypes.get(key) != null) {
                    System.out.println("Duplicate PortType: " + key);
                } else {
                    portTypes.put(key, pt);
                    PtConnection con = new PtConnection(key);
                    connections.put(key, con);
                }
            }

            // For external SE SUs, we only collect PortTypes (for connection
            // generation purpose). Skip Bindings and PartnerLinkTypes are
            // skipped on purpose.
            if (externalSuWsdlModels.contains(wsdlModel)) {
                continue;
            }
            
            // Collect bindings... (Binding QName -> Binding)
            for (Binding b : def.getBindings()) {
                String key = getQName(tns, b.getName());
                if (bindings.get(key) != null) {
                    System.out.println("Duplicate Binding: " + key);
                } else {
                    bindings.put(key, b);
                }
            }
            
            // Collect partnerLinkTypes... (PartnerLinkType QName -> ParterLinkType)
            for (ExtensibilityElement ee : def.getExtensibilityElements()) {
                if (ee.getQName().getLocalPart().equals("partnerLinkType")) {
                    String pltNS = ee.getQName().getNamespaceURI();
                    if (!pltNS.equals("http://docs.oasis-open.org/wsbpel/2.0/plnktype")) {
                        task.log("The Partnerlink namespace URI in " + 
                                wsdlFilePath + " is \"" + pltNS + "\". " + 
                                "It should be changed to \"http://docs.oasis-open.org/wsbpel/2.0/plnktype\".",
                                Project.MSG_ERR);
                        return;
                    }
                    PartnerLinkType plt = (PartnerLinkType) ee;
                    String key = getQName(tns, plt.getName());
                    if (partnerLinkTypes.get(key) != null) {
                        System.out.println("Duplicate ParnerLinkType: " + key);
                    } else {
                        partnerLinkTypes.put(key, plt);
                    }
                }
            }
        }

        // todo: 10/17/08, added to handle large # of SU wsdls with unwanted ports...
        String STR_SkipPorts = project.getProperty("skip.su.ports");  // NOI18N
        boolean skipSuPorts = (STR_SkipPorts != null) && (STR_SkipPorts.equalsIgnoreCase("true"));

        for (WSDLModel wsdlModel : wsdlModels) {
            // todo: 03/26/07, skip J2EE project concrete wsdls..
            if (true) {  // (!isJavaEEWsdl(doc)) {

                // Skip WSDL Ports defined in external SE SUs
                if (externalSuWsdlModels.contains(wsdlModel)) {
                    continue;
                }
                
                Definitions def = wsdlModel.getDefinitions();
                String tns = def.getTargetNamespace();
                
                // Collect services... (Serivce QName -> Service)
                for (Service s : def.getServices()) {
                    String sQName = getQName(tns, s.getName());
                    if (services.get(sQName) != null) {
                        System.out.println("Duplicate Service: " + sQName);
                    } else {
                        services.put(sQName, s);
                    }

                    // Collect ports... (ServiceQName + Port Name -> Port)
                    for (Port p : s.getPorts()) {
                        String key = sQName + "." + p.getName(); 
                        if (ports.get(key) != null) {
                            System.out.println("Duplicate Port: " + key);
                        } else if (skipSuPorts && isPortFromSU(p)) {
                           // skip this port...
                        } else {
                            // Mapping port to binding component ID...
                            List<ExtensibilityElement> xts = p.getExtensibilityElements();
                            if (xts.size() > 0) {
                                ExtensibilityElement ee = xts.get(0);
                                
                                // Ignore dummy soap port
                                QName eeQName = ee.getQName();                                
                                if (SOAP_ADDRESS_QNAME.equals(eeQName)) {
                                    String location = ee.getAttribute("location");
                                    if (DUMMY_SOAP_LOCATION.equals(location)) {
                                        task.log("INFO: WSDL Port with dummy SOAP address \"REPLACE_WITH_ACTUAL_URL\" is ignored: " +
                                                sQName + ":" + p.getName());
                                        continue;
                                    }
                                }
                                
                                String bcNs = ee.getQName().getNamespaceURI();
                                if (bcNs != null) {
                                    String bcName = bcNS2NameMap.get(bcNs);
                                    if (bcName != null) {
                                        port2BC.put(p, bcName);
                                    } else {
                                        task.log("WARNING: Missing WSDL extension plug-in for \"" + bcNs + 
                                                "\" or missing binding component definition in the config file.", 
                                                Project.MSG_WARN);
                                    }
                                }
                            }
                            
                            ports.put(key, p);
                            
                            Binding binding = p.getBinding().get();
                            if (binding == null) {
                                throw new BuildException(
                                    "ERROR: Missing binding for WSDL port " + key);
                            }
                            String ptQName = binding.getType().getQName().toString();
                            PtConnection ptCon = connections.get(ptQName);
                            if (ptCon != null) {
                                ptCon.addPort(p);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks whether the given WSDL Port is defined in a SU project.
     */
    private boolean isPortFromSU(Port p) {
        return suWsdlModels.contains(p.getModel());
    }

    public boolean isDefinedInExternalSU(WSDLModel model) {
        return externalSuWsdlModels.contains(model);
    }
    
    public String getBindingComponentName(Port p) {
        return port2BC.get(p);
    }
        
    private String getQName(String namespace, String name) {
        if (namespace == null) {
            return name;
        }
        return "{" + namespace + "}" + name; 
    }    
    
    /**
     * @return a map mapping PortType QName to PtConnection
     */
    public Map<String, PtConnection> getConnections() {
        return connections;
    }
    
    public PtConnection getPtConnection(String pt) {
        return connections.get(pt);
    }
    
    private Role[] getRoles(PartnerLinkType plt) {
        Role r1 = plt.getRole1();
        Role r2 = plt.getRole2();
        List<Role> rs = new ArrayList<Role>();
        if (r1 != null) rs.add(r1);
        if (r2 != null) rs.add(r2);
        return rs.toArray(new Role[] {});
    }
    
    public PortType getPartnerLinkPortType(String pname) {
        PartnerLinkType plt = partnerLinkTypes.get(pname);
        if (plt == null) {
            return null;
        }
        
        Role[] rs = getRoles(plt);
        if ((rs == null) || (rs.length < 1)) {
            return null;
        }
        
        // todo: need to handle multiple roles...
        Role r = rs[0];
        return r.getPortType().get();
    }
    
    public PortType getPartnerLinkPortType(String pname, String rname) {
        PartnerLinkType plt = partnerLinkTypes.get(pname);
        if (plt == null) {
            return null;
        }
        
        Role[] rs = getRoles(plt);
        if ((rs == null) || (rs.length < 1)) {
            return null;
        }
        
        for (int i=0; i<rs.length; i++) {
            Role r = rs[i];
            if (r.getName().equalsIgnoreCase(rname)) {
                return r.getPortType().get();
            }
        }
        
        return null;
    }
    
    public Map<String, PortType> getPortTypes() {
        return portTypes;
    }
    
    public Map<String, Binding> getBindings() {
        return bindings;
    }
    
    public Map<String, PartnerLinkType> getServiceLTs() {
        return partnerLinkTypes;
    }
    
    public Map<String, Service> getServices() {
        return services;
    }
    
    public Map<String, Port> getPorts() {
        return ports;
    }
    
    static class WSDLFileFilter implements FileFilter {        
        public boolean accept(File pathname) {
            boolean result = false;
            if(pathname.isDirectory()) {
                return true;
            }
            
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');
            if(dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex +1);
            }
            
            if(fileExtension != null && fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION)) {
                result = true;
            }
            
            return result;
        }
    }

    /**
     * Checks whether the given WSDL file is defined in an external SU project.
     *
     * @param wsdlFile  a WSDL file
     *
     * @return  <code>true</code> if the given WSDL file is defined in an
     *          external SU project; <code>false</code> if defined in the
     *          CompApp project or an internal SU project.
     */
    private boolean isWSDLDefinedInExternalSUProject(File wsdlFile) {
        String wsdlFilePath = wsdlFile.getAbsolutePath().replaceAll("\\\\", "/");

        String suProjectName = null;

        Pattern pattern = Pattern.compile("/src/jbiServiceUnits/(.*?)/");
        Matcher matcher = pattern.matcher(wsdlFilePath);

        if (matcher.find()) {
            suProjectName = matcher.group(1);
            return externalSuNames.contains(suProjectName);
        }

        return false;
    }

    /**
     * Checks whether the given WSDL file is defined in a SU project.
     *
     * @param wsdlFile  a WSDL file
     *
     * @return  <code>true</code> if the given WSDL file is defined in a SU
     *          project; <code>false</code> if defined in the CompApp project.
     */
    private static boolean isWSDLDefinedInSUProject(File wsdlFile) {
        String wsdlFilePath = wsdlFile.getAbsolutePath().replaceAll("\\\\", "/");
        return wsdlFilePath.contains("/src/jbiServiceUnits/");
    }
}
