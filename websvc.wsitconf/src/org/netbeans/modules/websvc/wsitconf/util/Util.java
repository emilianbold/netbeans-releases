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

import java.awt.Component;
import java.util.Arrays;
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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
//import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class Util {

    public enum ProjectType {
        WEB, EJB, CLIENT, UNKNOWN
    };
    
    private static final ErrorManager err =
            ErrorManager.getDefault().getInstance("org.netbeans.modules.websvc.wsitconf");   // NOI18N

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
    public static void getAllComponents( Component[] components, Collection allComponents ) {
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
        Vector allComponents = new Vector();
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
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }

    private static Set/*<SourceGroup>*/ getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set testGroups = new HashSet();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(severity, new IllegalStateException(
                       "No FileObject found for the following URL: " + urls[i])); //NOI18N
                }
            }
        }
        return result;
    }
    
    private static List/*<SourceGroup>*/ getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList();
        }
        List result = new ArrayList();
        List sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = (FileObject) sourceRoots.get(i);
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

//    public static void addProblemsToEnd(Problem[] where, Problem what) {
//        where[0] = addProblemsToEnd(where[0], what);
//    }
//    
//    public static Problem addProblemsToEnd(Problem where, Problem what) {
//        if (where == null) {
//            return what;
//        }
//        if (what != null) {
//            Problem tail = where;
//
//            while (tail.getNext() != null) {
//                tail = tail.getNext();
//            }
//            tail.setNext(what);
//        }
//        return where;
//    }

    /** Finds all WS projects affected by the change of FileObject 'fo' */
    public static Collection/*WebServicesSupport*/ getRelevantWSModules(FileObject fo) {
        
        Project affectedProject = FileOwnerQuery.getOwner(fo);
        Collection wsmodules = new ArrayList();
        Collection projects = new ArrayList();
        
        if (affectedProject != null) {
            // first check if the project which directly contains fo is relevant
            JAXWSSupport wsmod = JAXWSSupport.getJAXWSSupport(affectedProject.getProjectDirectory());
            if (wsmod != null) {
                projects.add(affectedProject);
            }
            
            
            projects.add(affectedProject);for (Project project : OpenProjects.getDefault().getOpenProjects()){
                
                Object isJ2eeApp = project.getLookup().lookup(J2eeAppProvider.class);
                if (isJ2eeApp != null) {
                    J2eeAppProvider j2eeApp = (J2eeAppProvider)isJ2eeApp;
                    J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();
                
                    if (j2eeModules != null) {
                        J2eeModuleProvider affectedPrjProvider =
                                (J2eeModuleProvider)affectedProject.getLookup().lookup(J2eeModuleProvider.class);
                        if (affectedPrjProvider != null) {
                            if (Arrays.asList(j2eeModules).contains(affectedPrjProvider)) {
                                for (int i=0; i<j2eeModules.length; i++) {
                                    FileObject[] sourceRoots = j2eeModules[i].getSourceRoots();
                                    if (sourceRoots != null && sourceRoots.length > 0){
                                        FileObject srcRoot = sourceRoots[0];
                                        Project p = FileOwnerQuery.getOwner(srcRoot);
                                        if ((p != null) && (!projects.contains(p))) {
                                            projects.add(p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Object obj = project.getLookup().lookup(SubprojectProvider.class);
                    if ((obj != null) && (obj instanceof SubprojectProvider)) {
                        Set subprojects = ((SubprojectProvider)obj).getSubprojects();
                        if (subprojects.contains(affectedProject)) {
                            JAXWSSupport ws = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
                            if (ws != null) {
                                if (!projects.contains(project)) { // include each project only once
                                    projects.add(project);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        for (int j=0; j < projects.size(); j++) {
            Project prj = (Project)((ArrayList)projects).get(j);
            JAXWSSupport websvc = JAXWSSupport.getJAXWSSupport(prj.getProjectDirectory());
            wsmodules.add(websvc);
        }
        
        err.log("Affected ws modules: " + wsmodules);
        return wsmodules;
    }

    public static Enumeration<String> getAliases(String storePath, char[] password, String type) throws IOException {
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
            return keyStore.aliases();
        } catch (FileNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); 
        } catch (KeyStoreException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); 
        } catch (NoSuchAlgorithmException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); 
        } catch (CertificateException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); 
        }
        return null;
    }

    public static final boolean isWsitSupported(Project p) {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/xml/ws/policy/Policy.class"); // NOI18N
        
        if (wsimportFO != null) {
            return true;
        }
        
        J2eeModuleProvider mp = (J2eeModuleProvider)p.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            String sID = mp.getServerInstanceID();
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT); //NOI18N
        } else {
            return true;
        }
    }

    public static final boolean isTomcat(Project project) {
        if (project != null) {
            J2eeModuleProvider mp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            if (mp != null) {
                String id = mp.getServerID();
                String instid = mp.getServerInstanceID();
                if ((instid != null) && (instid.toLowerCase().contains("tomcat"))) {     //NOI18N
                    return true;
                }
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
        
        WSDLModel model = binding.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        
        WSDLComponentFactory wcf = model.getFactory();
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
        // create operations and add them to the binding element
        List<String> bindingOperationNames = JavaWsdlMapper.getOperationNames(jc);
        for (String name : bindingOperationNames) {
            if (!isOperationInList(name, operations)) {
                org.netbeans.modules.xml.wsdl.model.BindingOperation bindingOperation = wcf.createBindingOperation();
                bindingOperation.setName(name);
                binding.addBindingOperation(bindingOperation);

                // add input/output messages
                org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
                inputMsg.setName(name);
                d.addMessage(inputMsg);

                org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
                outMsg.setName(name + "Response");                  //NOI18N
                d.addMessage(outMsg);

                org.netbeans.modules.xml.wsdl.model.RequestResponseOperation oper = wcf.createRequestResponseOperation();
                oper.setName(name);
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
                List<String> operationFaults = JavaWsdlMapper.getOperationFaults(jc, name);
                for (String fault : operationFaults) {
                    org.netbeans.modules.xml.wsdl.model.BindingFault bindingFault = wcf.createBindingFault();
                    bindingOperation.addBindingFault(bindingFault);
                }
            }
        }
        
        if (!isTransaction) {
            model.endTransaction();
        }
        
        return binding.getBindingOperations();
    }
    
}
