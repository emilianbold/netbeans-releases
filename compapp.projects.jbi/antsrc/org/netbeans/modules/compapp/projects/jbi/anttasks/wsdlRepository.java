/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.PtConnection;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.apache.tools.ant.Project;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;
import org.netbeans.modules.sun.manager.jbi.management.model.ComponentInformationParser;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
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
    
    private List<WSDLModel> wsdlModels = null;
    
    // mapping PortType QName to PortType
    private Map<String, PortType> portTypes = new HashMap<String, PortType>();
    
    // mapping Binding QName to Binding
    private Map<String, Binding> bindings = new HashMap();
    
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
    private Map<String, String> bcNsMap = new HashMap<String, String>(); 
    
//    private boolean repoLoaded = true;
    private Logger logger = Logger.getLogger(getClass().getName());    
    
    private static final String WSDL_FILE_EXTENSION = "wsdl";
    private static final WSDLFileFilter WSDL_FILE_FILTER = new WSDLFileFilter();
    
    
    public wsdlRepository(Project project) {
        
        this.project = project;
        
        wsdlModels = getAllWsdlModels(project);
        
        bcNsMap = buildBindingComponentMap(project);
        
        initLists();
        
        //also look into all SE.jars
    }
    
    // move me to some utility class
    /**
     * @param project 
     * @return a map mapping binding component namespace to binding component name.
     */
    public static Map<String, String> buildBindingComponentMap(Project project) {
        
        Map<String, String> bcMap = new HashMap<String, String>();
        
        String projPath = project.getProperty("basedir") + File.separator;
        String cnfDir = project.getProperty((JbiProjectProperties.META_INF));
        String bcInfo = projPath + cnfDir + "/BindingComponentInformation.xml";  // NOI18N
        File bcFile = new File(bcInfo);
        if (bcFile.exists()) {
            try {
                JBIComponentDocument compDoc = ComponentInformationParser.parse(bcFile);
                List<JBIComponentStatus> compList = compDoc.getJbiComponentList();
                for (JBIComponentStatus comp : compList) {
                    String compName = comp.getName();
                    List<String> nsList = comp.getNamespaceList();
                    for (String ns : nsList) {
                        bcMap.put(ns, compName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return bcMap;
    }
    
//    public boolean isLoaded() {
//        return repoLoaded;
//    }
    
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
                return name.endsWith(".wsdl");
            }
        };
        
        // Note that all files under src/jbiasa/ have been copied over to 
        // src/jbiServiceUnits/ at the beginning of jbi-build.
        // For all the WSDLs defined in JBI project, we want to use the  
        // original R/W copy under src/jbiasa/ instead of the R/O copy under 
        // src/jbiServiceUnits/.
        List<String> jbiASAChildNames = new ArrayList<String>();
        for (File file : jbiASADir.listFiles()) {
            jbiASAChildNames.add(file.getName());
        }
                
        // Add all WSDLs coming from SU projects
        for (File file : serviceUnitsDir.listFiles()) {
            String fileName = file.getName();
            // Skip <compapp>.wsdl and other wsdl files or directories 
            // defined under src/jbiasa/.
            if (!jbiASAChildNames.contains(fileName)) {
                ret.addAll(MyFileUtil.listFiles(file, filter, true));
            }
        }
        
        // Add all the WSDLs defined in JBI project (under src/jbiasa/).
        ret.addAll(MyFileUtil.listFiles(jbiASADir, filter, true));
        
        return ret;
    }
    
    private List<WSDLModel> getAllWsdlModels(Project project) {        
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        
        WSDLModelFactory wsdlModelFactory = WSDLModelFactory.getDefault();
        
        for (File file : getAllWsdlFiles(project)) {
            try {
                ModelSource ms = null;                
                try {
                    FileObject fo = FileUtil.toFileObject(file);
                    fo.refresh();
                    ms = Utilities.createModelSource(fo, false);
                } catch (Exception e) {      // from command line
                    Lookup lookup = Lookups.fixed(new Object[]{
                        file,
                        getDocument(file),
                        WSDLCatalogModel.getDefault()
                    });
                    ms = new ModelSource(lookup, false);
                }
                
                WSDLModel wm = wsdlModelFactory.createFreshModel(ms); 
                ret.add(wm);
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
        File wsdlFile = (File) lookup.lookup(File.class);
        if (wsdlFile == null) {
            FileObject wsdlFileObject = (FileObject) lookup.lookup(FileObject.class);
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
                        logger.log(Level.SEVERE, 
                                "The Partnerlink namespace URI in " + 
                                wsdlFilePath + " is \"" + pltNS + "\". " + 
                                "It should be changed to \"http://docs.oasis-open.org/wsbpel/2.0/plnktype\".");
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
        
        for (WSDLModel wsdlModel : wsdlModels) {
            // todo: 03/26/07, skip J2EE project concrete wsdls..
            if (true) {  // (!isJavaEEWsdl(doc)) {
                Definitions def = wsdlModel.getDefinitions();
                String tns = def.getTargetNamespace();
                
                // Collect services... (Serivce QName -> Service)
                for (Service s : def.getServices()) {
                    String skey = getQName(tns, s.getName());
                    if (services.get(skey) != null) {
                        System.out.println("Duplicate Service: " + skey);
                    } else {
                        services.put(skey, s);
                    }

                    // Collect ports... (Port QName -> Port)
                    for (Port p : s.getPorts()) {
                        String key = skey + "." + p.getName(); 
                        if (ports.get(key) != null) {
                            System.out.println("Duplicate Port: " + key);
                        } else {
                            ports.put(key, p);
                            
                            String ptkey = p.getBinding().get().getType().getQName().toString();
                            PtConnection ptCon = connections.get(ptkey);
                            if (ptCon != null) {
                                ptCon.addPort(p);
                            }

                            // Mapping port to binding component ID...
                            List<ExtensibilityElement> xts = p.getExtensibilityElements();
                            if (xts.size() > 0) {
                                ExtensibilityElement ee = xts.get(0);
                                String bcNs = ee.getQName().getNamespaceURI();
                                if (bcNs != null) {
                                    String bcName = bcNsMap.get(bcNs);
                                    if (bcName != null) {
                                        port2BC.put(p, bcName);
                                    } else {
                                        System.out.println("***WARNING: Missing WSDL extension plug-in for \"" + bcNs + "\".");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
        ArrayList rs = new ArrayList();
        if (r1 != null) rs.add(r1);
        if (r2 != null) rs.add(r2);
        return (Role[]) rs.toArray(new Role[] {});
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
}
