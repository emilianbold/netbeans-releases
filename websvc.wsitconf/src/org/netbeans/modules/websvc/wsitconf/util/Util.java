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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.util;

import java.util.logging.Level;

import java.util.logging.Logger;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import javax.swing.JComponent;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class Util {

    public enum ProjectType {
        WEB, EJB, CLIENT, UNKNOWN
    };
    
    private static final Logger logger = Logger.getLogger(Util.class.getName());

    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector<Component> allComponents = new Vector<Component>();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return result.toArray(new SourceGroup[result.size()]);
    }

    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "No FileObject found for the following URL: " + urls[i]); //NOI18N
                }
            }
        }
        return result;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return Collections.emptyList();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    /* Used to validate number inputs
     */
    public static boolean isPositiveNumber(String s, boolean zeroAllowed, boolean allowEmptyValue) {
        Integer i = null;
        if ((s == null) || ("".equals(s))) {
            return allowEmptyValue ? true : false;
        }
        try {
            i = Integer.parseInt(s);
            if (i != null) {
                if (zeroAllowed) {
                    return i.intValue() >= 0;
                }
                return i.intValue() > 0;
            }
        } catch (NumberFormatException nfe) {}
        return false;
    }

    public static String getServerStoreLocation(Project project, boolean trust) {
        String storeLocation = null;
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            InstanceProperties ip = mp.getInstanceProperties();
            if ("".equals(ip.getProperty("LOCATION"))) {    //NOI18N
                return null;
            }
            
            J2eePlatform j2eePlatform = getJ2eePlatform(project);
            if (j2eePlatform != null) {
                File[] keyLocs = null;
                keyLocs = trust ? j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_TRUSTSTORE_CLIENT) :
                                  j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_KEYSTORE_CLIENT);
                if ((keyLocs != null) && (keyLocs.length > 0)) {
                    storeLocation = keyLocs[0].getAbsolutePath();
                }
            }
        }
        return storeLocation;
    }
    
    public static List<String> getAliases(String storePath, char[] password, String type) throws IOException {
        if ((storePath == null) || (type == null)) return null;
        FileInputStream iStream;
        try {
            File f = new File(storePath);
            if ((f == null) || (!f.exists())) {
                throw new IOException();
            }
            iStream = new FileInputStream(new File(storePath));
            java.security.KeyStore keyStore;
            keyStore = java.security.KeyStore.getInstance(type);
            keyStore.load(iStream, password);
            Enumeration<String> e = keyStore.aliases();
            ArrayList<String> arr = new ArrayList<String>(keyStore.size());
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                arr.add(key);
            }
            Collections.sort(arr);
            return arr;
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (KeyStoreException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (CertificateException ex) {
            logger.log(Level.INFO, null, ex);
        }
        return null;
    }

    public static final String getPassword(Project p) {
        J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            InstanceProperties ip = mp.getInstanceProperties();
            return ip.getProperty(InstanceProperties.PASSWORD_ATTR);
        }
        return "";
    }

    private static String getServerInstanceID(Project p) {
        if (p != null) {
            J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (mp != null) {
                return mp.getServerInstanceID();
            }
        }
        return null;
    }
    
    public static final boolean isWsitSupported(Project p) {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/xml/ws/policy/Policy.class"); // NOI18N
        
        if (wsimportFO == null) {
            J2eePlatform j2eePlatform = getJ2eePlatform(p);
            if (j2eePlatform != null) {
                return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT); //NOI18N
            }
        }
        return true;
    }

    public static J2eePlatform getJ2eePlatform(Project project) {
        String serverInstanceID = getServerInstanceID(project);
        if ((serverInstanceID != null) && (serverInstanceID.length() > 0)) {
            return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        }
        return null;
    }
    
    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                Object type = j2eeModule.getModuleType();
                double version = Double.parseDouble(j2eeModule.getModuleVersion());
                if (J2eeModule.EJB.equals(type) && (version > 2.1)) {
                    return true;
                };
                if (J2eeModule.WAR.equals(type) && (version > 2.4)) {
                    return true;
                }
                if (J2eeModule.CLIENT.equals(type) && (version > 1.4)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String getServerName(Project p) {
        String sID = getServerInstanceID(p);
        if (sID != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            return j2eePlatform.getDisplayName();
        }
        return null;
    }
        
    public static final boolean isTomcat(Project project) {
        String sID = getServerInstanceID(project);
        if (sID != null) {
            if ((sID != null) && (sID.toLowerCase().contains("tomcat"))) {     //NOI18N
                return true;
            }
        }
        return false;
    }
    
    public static final boolean isWebProject(Project project) {
        if (getProjectType(project) == ProjectType.WEB) {
            return true;
        }
        return false;
    }
    
    public static final ProjectType getProjectType(Project project) {
        ProjectType pt = ProjectType.UNKNOWN;
        if (project != null) {
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
            if (wm != null) {
                pt = ProjectType.WEB;
            } else if (em != null) {
                pt = ProjectType.EJB;
            }
        }
        return pt;
    }

    private static boolean isOperationInList(String operName, Collection<BindingOperation> operations) {
        Iterator<BindingOperation> i = operations.iterator();
        while (i.hasNext()) {
            BindingOperation bo = i.next();
            if ((bo != null) && (operName.equals(bo.getName()))) {
                return true;
            }
        }
        return false;
    }
    
    public static Collection<BindingOperation> refreshOperations(Binding binding, FileObject jc) {
        
        Collection<BindingOperation> operations = binding.getBindingOperations();
        if ((binding == null) || (jc == null)) {
            return operations;
        }
        
        PortType pt = getPortType(binding);
        
        // create operations and add them to the binding element
        List<String> bindingOperationNames = JavaWsdlMapper.getOperationNames(jc);
        for (String name : bindingOperationNames) {
            if (!isOperationInList(name, operations)) {
                generateOperation(binding, pt, name, jc);
            }
        }
        
        return binding.getBindingOperations();
    }

    public static BindingOperation generateOperation(Binding binding, PortType portType, String operationName, FileObject implClass) {
        WSDLModel model = binding.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        Definitions d = (Definitions) binding.getParent();

        BindingOperation bindingOperation;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            bindingOperation = wcf.createBindingOperation();
            bindingOperation.setName(operationName);
            binding.addBindingOperation(bindingOperation);

            // add input/output messages
            org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
            inputMsg.setName(operationName);
            d.addMessage(inputMsg);

            org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
            outMsg.setName(operationName + "Response");                  //NOI18N
            d.addMessage(outMsg);

            org.netbeans.modules.xml.wsdl.model.RequestResponseOperation oper = wcf.createRequestResponseOperation();
            oper.setName(operationName);
            portType.addOperation(oper);

            org.netbeans.modules.xml.wsdl.model.Input input = wcf.createInput();
            oper.setInput(input);
            input.setMessage(input.createReferenceTo(inputMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

            org.netbeans.modules.xml.wsdl.model.Output out = wcf.createOutput();
            oper.setOutput(out);
            out.setMessage(out.createReferenceTo(outMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

            org.netbeans.modules.xml.wsdl.model.BindingOutput bindingOutput = wcf.createBindingOutput();
            bindingOperation.setBindingOutput(bindingOutput);
            org.netbeans.modules.xml.wsdl.model.BindingInput bindingInput = wcf.createBindingInput();
            bindingOperation.setBindingInput(bindingInput);

            //add faults
            List<String> operationFaults = JavaWsdlMapper.getOperationFaults(implClass, operationName);
            for (String fault : operationFaults) {
                org.netbeans.modules.xml.wsdl.model.BindingFault bindingFault = wcf.createBindingFault();
                bindingOperation.addBindingFault(bindingFault);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
        return bindingOperation;
    }

    public static PortType getPortType(Binding binding) {
        Definitions d = (Definitions) binding.getParent();

        QName portTypeQName = binding.getType().getQName();
        PortType portType = null;
        
        Collection<PortType> portTypes = d.getPortTypes();
        Iterator<PortType> i = portTypes.iterator();
        while (i.hasNext()) {
            PortType pt = i.next();
            if (pt != null) {
                if (portTypeQName.getLocalPart().equals(pt.getName())) {
                    portType = pt;
                    break;
                }
            }
        }
        return portType;
    }
    
    public static FileObject getFOForModel(WSDLModel model) {
        if (model == null) return null;
        ModelSource ms = model.getModelSource();
        return Utilities.getFileObject(ms);
    }
        
}
