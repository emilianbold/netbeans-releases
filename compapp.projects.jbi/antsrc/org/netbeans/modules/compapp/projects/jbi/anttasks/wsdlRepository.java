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

import java.util.logging.Level;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.PtConnection;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentDocument;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentStatus;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.ComponentInformationParser;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.xml.sax.SAXException;
import org.apache.tools.ant.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.util.*;
import java.util.logging.Logger;
import java.io.*;
import org.netbeans.modules.compapp.projects.jbi.util.MyFileUtil;

/**
 * WSDL repoository of the JBI project
 *
 * @author tli
 * @author jqian
 */
public class wsdlRepository {
    public static final String WSDL_NAME = "PortmapEditorGenerated";
    public static final String WSDL_TARGET_NAMESPACE = "ICAN_jbi_portmap";
    public static final String WSDL_XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final String WSDL_SOAP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String WSDL_SLINK_NAMESPACE = "http://schemas.xmlsoap.org/ws/2002/07/service-link";
    
    public static final String PORTMAP_WSDL_URI = "portmap.wsdl";
    
    //todo: 12/06/05 Later, these extensibility node should be loaded in from layer..
    /*
    private static final String[] protocols = {
        "soap", "http://schemas.xmlsoap.org/wsdl/soap/",
        "jdbc", "http://schemas.stc.com/jbi/wsdl-extensions/jdbc/",
        "smtp", "http://schemas.sun.com/jbi/wsdl-extensions/smtp/",
        "jms",  "http://schemas.sun.com/jbi/wsdl-extensions/jms/",
        "file", "http://schemas.sun.com/jbi/wsdl-extensions/file/",
    };
     */
    
    private List<WSDLModel> wsdls = null;
    private Map portTypes = new HashMap();
    private Map bindings = new HashMap();
    private Map serviceLTs = new HashMap();
    private Map services = new HashMap();
    private Map ports = new HashMap();
    private Map portsBC = new HashMap();
    private Map connections = new HashMap();
    private Map<String, String> bindingComponents = new HashMap<String, String>(); // mapping bc namespace to bc ID
    private WSDLModel portmapWsdl = null;
    private String portmapWsdlFileLoc = null;
    private boolean repoLoaded = true;
    private Logger logger = Logger.getLogger(getClass().getName());
    
    private Project project;
    
    private static final String WSDL_FILE_EXTENSION = "wsdl";
    private static WSDLFileFilter wsdlFileFilter = new WSDLFileFilter();
    
//    private JarCatalogModel acm = new JarCatalogModel();
    
    public wsdlRepository(Project project) {
        
        this.project = project;
        
        wsdls = new ArrayList<WSDLModel> ();
//        List mJars = getSeJarPathLocations(project);
//        String projPath = project.getProperty("basedir") + File.separator;
        
        try {
            //List<WSDLModel> cs = getAllWsdlDocumentsFormSEJars(mJars);
            List<WSDLModel> cs = getAllWsdlDocuments(project);
            if(cs != null) {
                wsdls.addAll(cs);
            }
            /*
            String srcDir = project.getProperty((JbiProjectProperties.SRC_DIR));
            if ((srcDir != null) && (srcDir.length()>0)) {
                portmapWsdlFileLoc = projPath + srcDir + "/portmap.wsdl";
                File portmapFile = new File(portmapWsdlFileLoc);
                if (! portmapFile.exists()) {
                    createPortmapWsdl();
                }
                File sdir = new File(projPath + srcDir);
                Collection cs2 = getWSDLDocumentInFolder(sdir);
                if (cs2 != null) {
                    wsdls.addAll(cs2);
                }
            }*/
            
            // Load Binding Component information            
            bindingComponents = buildBindingComponentMap(project);
            
            initLists();
            
        } catch (Exception ex) {
            System.out.println("...Ex: "+ex);
            ex.printStackTrace();
        }
        
        //also look into all SE.jars
    }
    
    // move me to some utility class
    public static Map<String, String> buildBindingComponentMap(final Project project)
    throws ParserConfigurationException, SAXException, IOException {
        
        Map<String, String> bcMap = new HashMap<String, String>();
        
        String projPath = project.getProperty("basedir") + File.separator;
        String cnfDir = project.getProperty((JbiProjectProperties.META_INF));
        String bcInfo = projPath + cnfDir + "/BindingComponentInformation.xml";  // NOI18N
        File bcFile = new File(bcInfo);
        if (bcFile.exists()) {
            JBIComponentDocument compDoc = ComponentInformationParser.parse(bcFile);
            List compList = compDoc.getJbiComponentList();
            Iterator iterator = compList.iterator();
            JBIComponentStatus component = null;
            
            while ((iterator != null) && (iterator.hasNext() == true)) {
                component = (JBIComponentStatus) iterator.next();
                List nsList = component.getNamespaceList();
                for (int i = 0; i < nsList.size(); i++) {
                    String ns = (String)nsList.get(i);
                    bcMap.put(ns, component.getName());
                }
            }
        }
        
        return bcMap;
    }
    
    public boolean isLoaded() {
        return repoLoaded;
    }
    
    public List<WSDLModel> getWsdlCollection() {
        return wsdls;
    }
    
    /*
    private ModelSource loadModel(InputStream modelIn, String baseURI, String key, boolean editable) {
        System.out.println("wsdlRepository.loadModel: baseURI= " + baseURI + " key=" + key);
     
        ModelSource model = null;
     
        try {
            Document d = new PlainDocument();
     
            String text;  // NOI18N
            BufferedReader reader = new BufferedReader(new InputStreamReader(modelIn, "UTF-8"));
            String line;
            int pos = 0;
            while ((line=reader.readLine()) != null) {
                text = line + "\n" ; // NOI18N
                d.insertString(pos, text, null);
                pos += text.length();
            }
            reader.close();
     
            //Document d = new PlainDocument();
            //d.insertString(0, text, null);
            // Lookup adding cm and d
            File fakeFile = new File(key);
            Lookup lookup = Lookups.fixed(new Object[]{d, acm, fakeFile, org.netbeans.modules.xml.xam.dom.ReadOnlyAccess.Provider.getInstance()});
            model = new ModelSource(lookup, editable);
            // System.out.println("Model created: "+baseURI+", "+model);
            acm.addModelSource(new URI(baseURI), model);
        } catch(Exception ex) {
            System.out.println(ex);
            /*
            logger.log(Level.SEVERE, "exception loading  wsdl: "+ baseURI, ex);
            //log and show to user
            logErr("exception loading wsdl: "+ baseURI  + ex.getMessage());
            //StatusDisplayer.getDefault().setStatusText(ex.getMessage());
     *.
            model = null;
        }
     
        return model;
    }
     */
    
    private Iterator reverseIterator(Iterator  it) {
        List list = new ArrayList();
        for (; it.hasNext();) {
            list.add(it.next());
        }
        Collections.reverse(list);
        return list.iterator();
    }
    
    // Use file-based wsdls
    private List<WSDLModel> getAllWsdlDocuments(Project project) {
        String projPath = project.getProperty("basedir") + File.separator;
        String serviceUnitsDirLoc = projPath + "src" + File.separator + "jbiServiceUnits";
        String jbiAsaDirLoc = projPath + "src" + File.separator + "jbiAsa";
        
        // Examine all WSDLs (from both SU projects and JBI project)
        File serviceUnitsDir = new File(serviceUnitsDirLoc);
        File jbiASADir = new File(jbiAsaDirLoc);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".wsdl");
            }
        };
        String casaWSDLFileName = getCasaWSDLFileName();
        List<File> wsdlFiles = MyFileUtil.listFiles(serviceUnitsDir, filter, true);
        for (File file : wsdlFiles) {
            if (file.getName().equals(casaWSDLFileName)) {
                // Use the original casa wsdl file under src/jbiasa/.
                wsdlFiles.remove(file);
                break;
            }
        }
        wsdlFiles.addAll(MyFileUtil.listFiles(jbiASADir, filter, true));
        
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
               
        WSDLCatalogModel catalogModel = WSDLCatalogModel.getDefault();
        
        for (File file : wsdlFiles) {
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
                        catalogModel
                    });
                    ms = new ModelSource(lookup, false);
                }
                
                WSDLModel wm = WSDLModelFactory.getDefault().createFreshModel(ms); //getModel(ms);
                ret.add(wm);
            } catch (CatalogModelException ex) {
                ex.printStackTrace();
            }            
        }
        
        return ret;
    }
    
    
    private String getCasaWSDLFileName() {        
        String projName = project.getProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
        return projName + ".wsdl";
    }
    
    /**
     * Implementation of CatalogModel
     * @param file
     * @return
     */
    protected Document getDocument(File file) throws CatalogModelException{
        Document result = null;
        
        if (result != null) return result;
        try {
            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
//                result = new org.netbeans.editor.BaseDocument(
//                        org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
            result = new javax.swing.text.PlainDocument();
            result.remove(0, result.getLength());
            fis.read(buffer);
            fis.close();
            String str = new String(buffer);
            result.insertString(0,str,null);
            
        } catch (Exception dObjEx) {
            throw new CatalogModelException(file.getAbsolutePath()+" File Not Found");
        }
        
        return result;
    }
    
    /*
    private List<WSDLModel> getAllWsdlDocumentFormSEJars(List seJarPathLocations) throws IOException, SecurityException, SAXException, FileNotFoundException, ParserConfigurationException {
        ArrayList wsdls = new ArrayList<WSDLModel>();
        String seJarPath = null;
        ZipEntry entry = null;
        InputStream inputStream = null;
        ArrayList xwsdls = new ArrayList();
        long lastTime = 0;
        for (Iterator iterator = reverseIterator(seJarPathLocations.iterator()); iterator.hasNext();) {
            seJarPath = (String) iterator.next();
            if(seJarPath != null) {
                // System.out.println("Proc: "+seJarPath);
                File file = new File(seJarPath);
                if(file.exists()) {
                    JarFile seJarFile = new JarFile(seJarPath);
                    Enumeration enumeration = seJarFile.entries();
                    while(enumeration.hasMoreElements()) {
                        entry = (ZipEntry) enumeration.nextElement();
                        if(entry != null) {
                            String fileName = entry.getName().toLowerCase();
     
                            if (fileName.endsWith("wsdl") || fileName.endsWith("xsd")) {
                                inputStream = seJarFile.getInputStream(entry);
                                //ModelSource ms = loadModel(inputStream, entry.getName(), false);
                                //ModelSource ms = loadModel(inputStream, "JBIPRJ"+System.currentTimeMillis(), false); // NOI18N
                                long time = System.currentTimeMillis();
                                if (time <= lastTime) {
                                    time = lastTime+1;
                                }
                                lastTime = time;
                                String baseURI = entry.getName();
                                String uniqueFileName = file.toURI().toString() + "/" + entry.getName() + "/" + time;
                                ModelSource ms = loadModel(inputStream, baseURI, uniqueFileName, false); // NOI18N
                                inputStream.close();
                                inputStream = null;
                                if (fileName.endsWith("wsdl")) {
                                    WSDLModel wm = WSDLModelFactory.getDefault().getModel(ms);
                                    assert wm.getModelSource() == ms;
                                    xwsdls.add(wm);
                                }
                            }
                        }
                    }
                    //seJarFile.close();
                } else {
                    repoLoaded = false;
                }
            }
        }
     
        return xwsdls;
    }
     */

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
        int size = wsdls.size();
        for (int i=0; i<size; i++) {
            WSDLModel doc = (WSDLModel) wsdls.get(i);
            /*
            Lookup lookup = doc.getModelSource().getLookup();
            File wsdlFile = (File) lookup.lookup(File.class);
            if (wsdlFile == null) {
                FileObject wsdlFileObject = (FileObject) lookup.lookup(FileObject.class);
                wsdlFile = FileUtil.toFile(wsdlFileObject);
            }
            String wsdlFilePath = wsdlFile.getPath();
            */
            String wsdlFilePath = getWsdlFilePath(doc);

            Definitions def = doc.getDefinitions();
            String tns = def.getTargetNamespace();
            //System.out.println("WSDL-"+i+": "+doc.getModelSource().toString());
//            acm.addNSModelSource(tns, doc.getModelSource());
            
            // Collect portTypes...
            Collection pts = def.getPortTypes();
            for (Iterator it=pts.iterator(); it.hasNext(); ) {
                PortType pt = (PortType) it.next();
                String key = getQName(tns, pt.getName());
                Object opt = portTypes.get(key);
                if (opt != null) {
                    System.out.println("Duplicate PT: "+pt.getName()+", "+key);
                } else {
                    //System.out.println("\tPType: "+key);
                    portTypes.put(key, pt);
                    PtConnection con = new PtConnection(key);
                    connections.put(key, con);
                }
            }
            
            // Collect bindings...
            Collection bs = def.getBindings();
            for (Iterator it=bs.iterator(); it.hasNext(); ) {
                Binding b = (Binding) it.next();
                String key = getQName(tns, b.getName());
                Object opt = bindings.get(key);
                if (opt != null) {
                    System.out.println("Duplicate Binding: "+b.getName()+", "+key);
                } else {
                    //System.out.println("\tBinding: "+key);
                    bindings.put(key, b);
                }
            }
            
            // Collect serviceLinkTypes...
            List<ExtensibilityElement> xts = 
                    def.getExtensibilityElements();
            for (int k=0, sz=xts.size(); k<sz; k++) {
                ExtensibilityElement exm = xts.get(k);
                if (exm.getQName().getLocalPart().equals("partnerLinkType")) {
                    String pltNS = exm.getQName().getNamespaceURI();
                    if (!pltNS.equals("http://docs.oasis-open.org/wsbpel/2.0/plnktype")) {
                        logger.log(Level.SEVERE, 
                                "The Partnerlink namespace URI in " + 
                                wsdlFilePath + " is \"" + pltNS + "\". " + 
                                "It should be changed to \"http://docs.oasis-open.org/wsbpel/2.0/plnktype\".");
                        return;
                    }
                    PartnerLinkType slt = (PartnerLinkType) exm;
                    String key = getQName(tns, slt.getName());
                    Object opt = serviceLTs.get(key);
                    if (opt != null) {
                        System.out.println("Duplicate SLT: "+slt.getName()+", "+tns);
                    } else {
                        serviceLTs.put(key, slt);
                        //System.out.println("\tPartnerLinkType: "+key);
                    }
                }
            }
        }
        
        for (int i=0; i<size; i++) {
            WSDLModel doc = (WSDLModel) wsdls.get(i);

            // todo: 03/26/07, skip J2EE project concrete wsdls..
            if (!isJavaEEWsdl(doc)) {
                Definitions def = doc.getDefinitions();
                String tns = def.getTargetNamespace();
                // Collect ports...
                Collection ss = def.getServices();
                for (Iterator it=ss.iterator(); it.hasNext(); ) {
                    Service s = (Service) it.next();
                    String skey = getQName(tns, s.getName());
                    Object sopt = services.get(skey);
                    if (sopt != null) {
                        System.out.println("Duplicate Service: "+s.getName()+", "+tns);
                    } else {
                        services.put(skey, s);
                        //System.out.println("\tService: "+skey);
                    }

                    Collection ps = s.getPorts();
                    for (Iterator it2=ps.iterator(); it2.hasNext(); ) {
                        Port p = (Port) it2.next();
                        String key = getQName(tns, s.getName())+"."+p.getName(); // NOI18N
                        //System.out.println("\tPortKey: "+key);
                        Object opt = ports.get(key);
                        if (opt != null) {
                            System.out.println("Duplicate Port: "+key);
                        } else {
                            ports.put(key, p);
                            String ptkey = p.getBinding().get().getType().getQName().toString();
                            //System.out.println("\tPort: "+ptkey);
                            Object con = connections.get(ptkey);
                            if (con != null) {
                                ((PtConnection) con).addPort(p);
                            }


                            // find the associated binding component ID...
                            List<ExtensibilityElement> xts = p.getExtensibilityElements();
                            if (xts.size() > 0) {
                                ExtensibilityElement ex = xts.get(0);
                                String qns = ex.getQName().getNamespaceURI();
                                if (qns != null) {
                                    Object bc = bindingComponents.get(qns);
                                    if (bc != null) {
                                        portsBC.put(p, bc);
                                        //System.out.println("\tBinding: "+qns);
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
        Object bc = portsBC.get(p);
        if (bc != null) {
            return (String) bc;
        }
        
        return null;
    }
    
    private String getNSName(String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            return qname.substring(0, i);
        }
        
        return null;
    }
    
    public String getQName(String namespace, String name) {
        if (namespace == null) {
            return name;
        }
        return "{"+namespace+"}"+name; // NOI18N
    }
    
    
    public Map getConnections() {
        return connections;
    }
    
    public PtConnection getPtConnection(String pt) {
        Object con = connections.get(pt);
        if (con != null) {
            return (PtConnection) con;
        }
        
        return null;
    }
    
    private Role[] getRoles(PartnerLinkType slt) {
        Role r1 = slt.getRole1();
        Role r2 = slt.getRole2();
        ArrayList rs = new ArrayList();
        if (r1 != null) rs.add(r1);
        if (r2 != null) rs.add(r2);
        return (Role[]) rs.toArray(new Role[] {});
    }
    
    public PortType getPartnerLinkPortType(String pname) {
        Object plto = serviceLTs.get(pname);
        if (plto == null) {
            return null;
        }
        
        PartnerLinkType slt = (PartnerLinkType) plto;
        Role[] rs = getRoles(slt);
        if ((rs == null) || (rs.length < 1)) {
            return null;
        }
        
        // todo: need to handle multiple roles...
        Role r = rs[0];
        return r.getPortType().get();
    }
    
    public PortType getPartnerLinkPortType(String pname, String rname) {
        Object plto = serviceLTs.get(pname);
        if (plto == null) {
            return null;
        }
        
        PartnerLinkType slt = (PartnerLinkType) plto;
        Role[] rs = getRoles(slt);
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
    
    public WSDLModel getPortmapWsdl() {
        if (portmapWsdl == null) {
            // create a new one...
            createPortmapWsdl();
        }
        return portmapWsdl;
    }
    
    private void serializeWsdl(WSDLModel wsdl, Writer writer) {
        try {
            ModelSource ms = wsdl.getModelSource();
            if (ms == null) return;
            
            Lookup lookup = ms.getLookup();
            PlainDocument d = (PlainDocument)lookup.lookup(PlainDocument.class);
            String txt = d.getText(0, d.getLength());
            
            writer.write(txt);
        } catch (Exception ex) {
            // create failed..
        }
    }
    
    public String getPortmapWsdlString() {
        if (portmapWsdl == null) {
            return null;
        }
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bs);
        serializeWsdl(portmapWsdl, osw);
        return bs.toString();
    }
    
    private void createPortmapWsdl() {
        try {
            // todo: 07/12/06 no editable model support yet..
            /*
            ByteArrayInputStream bin = new ByteArrayInputStream(emptyWsdl.getBytes());
            ModelSource ms = loadModel(bin, PORTMAP_WSDL_URI, true);
            WSDLModel portmapWsdl = WSDLModelFactory.getDefault().getModel(ms);
            Definitions definitions = portmapWsdl.getDefinitions();
            definitions.getModel().startTransaction();
            //definitions.setName(WSDL_NAME);
            definitions.setTargetNamespace(WSDL_TARGET_NAMESPACE);
            definitions.setAnyAttribute(new QName("http://schemas.xmlsoap.org/wsdl", "tns", "xmlns"), WSDL_TARGET_NAMESPACE);
            definitions.setAnyAttribute(new QName("http://schemas.xmlsoap.org/wsdl", "slink", "xmlns"), WSDL_SLINK_NAMESPACE);
            definitions.getModel().endTransaction();
             
            FileWriter fWriter = new FileWriter(new File(portmapWsdlFileLoc));
            serializeWsdl(portmapWsdl, fWriter);
            fWriter.close();
             */
        } catch (Exception ex) {
            // create failed..
            System.out.println("CreatePortmapWsdl: "+ex);  // NOI18N
            ex.printStackTrace();
        }
    }
    
    public void savePortmapWsdl() {
        if (portmapWsdl == null) {
            return;
        }
        
        try {
            FileWriter fWriter = new FileWriter(new File(portmapWsdlFileLoc));
            serializeWsdl(portmapWsdl, fWriter);
            fWriter.close();
        } catch (Exception ex) {
            //...
        }
    }
    /*
    private List getSeJarPathLocations(Project project) {
        List seJarPathLocations = new ArrayList();
        List locationList = new ArrayList();
        String projPath = project.getProperty("basedir") + File.separator;
        String jars = project.getProperty((JbiProjectProperties.JBI_CONTENT_ADDITIONAL));
        // This should be OS-agnostic so that we can use the exact same CompApp project on different OS’s
        StringTokenizer st = new StringTokenizer(jars, ";"); // File.pathSeparator);
        
        while (st.hasMoreTokens()) {
            String jn = st.nextToken();
            if ((jn.indexOf(':') < 0)  && (!jn.startsWith("/"))) {  // i.e., relative path
                jn = projPath + jn;
            }
            
            locationList.add(jn);
            seJarPathLocations.add( // projPath+ // jn);
        }
        
        return seJarPathLocations;
    }*/
    
    /*
    private Collection getWSDLDocumentInFolder(File rootFolder) throws Exception {
        ArrayList  wsdls = new ArrayList();
        File[] children = rootFolder.listFiles(wsdlFileFilter);
        if (children == null) {
            return wsdls; // no children...
        }
        for(int i = 0; i < children.length; i++) {
            File child = children[i];
            if(child.isDirectory()) {
                //do not look into sub folder for now
//				Collection matchingWsdls = getMatchingWSDLDocumentInFolder(child, targetNamespace);
//				if(matchingWsdls != null) {
//					wsdls.addAll(matchingWsdls);
//				}
            } else {
     
                InputStream in = new FileInputStream(child.getAbsolutePath());
     
                //todo: use the relative path as URI..
                ModelSource ms = loadModel(in, child.getName(), "JBIPRJ"+System.currentTimeMillis(), false);
                WSDLModel wsdl = WSDLModelFactory.getDefault().getModel(ms);
                if (child.getName().equalsIgnoreCase("portmap.wsdl")) {
                    portmapWsdl = wsdl;
                }
                if(wsdl != null) {
                    wsdls.add(wsdl);
                }
            }
        }
     
        return wsdls;
    }
     */
    
    private SourceGroup[] getSourceGroups(Project project) {
        /*
        Sources sources = ProjectUtils.getSources(project);
         
        if(sources != null) {
            SourceGroup[] groups = sources.getSourceGroups(JbiProject.SOURCES_TYPE_JBI /* Sources.TYPE_GENERIC *.);
            if ((groups == null) || (groups.length < 1)) {
                groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            return groups;
        }
         */
        return null;
    }
    
    public Map getPortTypes() {
        return portTypes;
    }
    
    public Map getBindings() {
        return bindings;
    }
    
    public Map getServiceLTs() {
        return serviceLTs;
    }
    
    public Map getServices() {
        return services;
    }
    
    public Map getPorts() {
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
