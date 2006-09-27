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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler.ServiceRootChildren;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
/**
 *
 * @author pzajac
 */
public class ServiceNodeHandlerTest extends  TestBase {
    NbModuleProject prj; 
    ServiceNodeHandler nodeHandler;
    ServiceNodeHandler.ServiceRootNode serviceRootNode;
                
    
    public ServiceNodeHandlerTest(java.lang.String testName) {
	super(testName);
    }
    
//    public static Test suite() {
//	TestSuite suite = new NbTestSuite();
//        suite.addTest(new ServiceNodeHandlerTest("testNbOrgModule"));
//	return suite;
//    }
    private void setUpSimpleSuite() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.DISABLED_CLUSTERS_PROPERTY, CLUSTER_IDE + "," + CLUSTER_ENTERPRISE);
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(suite);
         
        prj = TestBase.generateSuiteComponent(suite, "prj1");
        nodeHandler = (ServiceNodeHandler) prj.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
   
    }
    
     private void setUpStandaloneModule() throws Exception {
        prj = TestBase.generateStandaloneModule(getWorkDir(), "prj1");
        nodeHandler = (ServiceNodeHandler) prj.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
     }
    public void testSuiteWithManyProjects() throws IOException, Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
  
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.DISABLED_CLUSTERS_PROPERTY, CLUSTER_IDE + "," + CLUSTER_ENTERPRISE  + "," + CLUSTER_PLATFORM);
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(suite);
          
        NbModuleProject prj1 = TestBase.generateSuiteComponent(suite, "prj1");
        NbModuleProject prj2 = TestBase.generateSuiteComponent(suite, "prj2");
        NbModuleProject prj3 = TestBase.generateSuiteComponent(suite,"prj3");
        nodeHandler = (ServiceNodeHandler) prj1.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
//        SuiteProjectTest.openSuite(suite);
        exploreNodes(nodeHandler.moduleChild);  
        exploreNodes(nodeHandler.allInContextChild);

	assertNodes("org.myservice","","");
        
        writeServices(prj2,"org.myservice","impl");
        assertNodes("org.myservice",
                    "",
                    "org.myservice,org.myservice,impl,impl,"
                );
        
        nodeHandler = (ServiceNodeHandler) prj3.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
//        SuiteProjectTest.openSuite(suite);
        exploreNodes(nodeHandler.moduleChild);  
        exploreNodes(nodeHandler.allInContextChild);

        assertNodes("org.myservice",
                    "",
                    "org.myservice,org.myservice,impl,impl,"
                );
        
    }    
    private static void writeServices(NbModuleProject prj,String serviceType,String classes) throws IOException {
             FileObject servicesFolder = SUtil.getServicesFolder(prj,true); 
             FileObject fo = FileUtil.createData(servicesFolder,serviceType);
             System.out.println("writeServices:" + fo.getPath());
  	     FileLock lock = fo.lock(); 
	     OutputStream os = fo.getOutputStream(lock); 
	     PrintStream ps = new PrintStream(os);
	     ps.println(classes);
	     ps.close();
	     os.close();
             lock.releaseLock();
    }     
    /** use case 1
     * register some services in allInSuiteMap
     * explore node with services
     * create META-INF folder
     * create a service (file too)
     * check if all the nodes were updadtes
     * add a service to file (check it)
     * delete a service from file
     * delete file with services
     */
    
    public void testAModuleInSuite() throws Exception {
        FileLock lock = null;
	try {
	    setUpSimpleSuite();
            lock = doSingleModule();  
	} finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
    public void testStandaloneModule() throws Exception {
        FileLock lock = null;
	try {
	    setUpStandaloneModule();
            lock = doSingleModule();  
	} finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
    private void deleteService(FileObject prjRoot,String name) throws IOException {
       FileObject fo = prjRoot.getFileObject("src/META-INF/services/" + name);
       if (fo != null) {
           fo.delete();
       }
    }    
    public void testNbOrgModule() throws Exception {
        FileObject prjFo = nbroot.getFileObject("openide/fs");
        FileObject prj2Fo = nbroot.getFileObject("openide/masterfs");
        deleteService(prjFo,"org.myservice");   
        deleteService(prj2Fo,"org.myservice");   
        try {
            NbModuleProject prj = (NbModuleProject) ProjectManager.getDefault().findProject(prjFo);
            NbModuleProject prj2 = (NbModuleProject) ProjectManager.getDefault().findProject(prj2Fo);
            OpenProjects.getDefault().open(new NbModuleProject[]{prj,prj2}, false);
            nodeHandler = (ServiceNodeHandler) prj.getLookup().lookup(ServiceNodeHandler.class);
            serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
            exploreNodes(nodeHandler.moduleChild);  
            exploreNodes(nodeHandler.allInContextChild);
             
            assertNodes("org.myservice","","");
            writeServices(prj,"org.myservice","impl");
            assertNodes("org.myservice",
                        "org.myservice,org.myservice,impl,impl,",
                        "org.myservice,<b>org.myservice</b>,impl,<b>impl</b>,"
                    );
            // try modify different project
            OpenProjects.getDefault().open(new NbModuleProject[]{prj2}, false);
            writeServices(prj2,"org.myservice","impl2");
            assertNodes("org.myservice",
                        "org.myservice,org.myservice,impl,impl,",
                         "org.myservice,<b>org.myservice</b>,impl,<b>impl</b>,impl2,impl2,");
            
        } finally {
            deleteService(prjFo,"org.myservice");   
            deleteService(prj2Fo,"org.myservice");   
        }
    }
    
    private FileLock doSingleModule() throws IOException, Exception {
        FileLock lock;
        
        assertTrue("testing equals of ServiceNodeHandler",nodeHandler.equals(nodeHandler));
        
	    //
	    // setup the project
        FileObject servicesFolder = SUtil.getServicesFolder(prj,true); 
        servicesFolder.delete();
        servicesFolder = SUtil.getServicesFolder(prj,true);

        exploreNodes(nodeHandler.moduleChild);  
        exploreNodes(nodeHandler.allInContextChild);
        assertNotNull(nodeHandler.moduleServiceMap);
        String serviceType = "org.openide.filesystems.Repository";
        assertNodes(serviceType,"","org.openide.filesystems.Repository,org.openide.filesystems.Repository,org.netbeans.core.startup.NbRepository,org.netbeans.core.startup.NbRepository,");
        FileObject fo = servicesFolder.createData(serviceType); 
        //printNodes();
        assertNodes(serviceType,
                     "org.openide.filesystems.Repository,org.openide.filesystems.Repository,",
                     "org.openide.filesystems.Repository,<b>org.openide.filesystems.Repository</b>,org.netbeans.core.startup.NbRepository,org.netbeans.core.startup.NbRepository,");
    
        lock = fo.lock(); 
        OutputStream os = fo.getOutputStream(lock); 
        PrintStream ps = new PrintStream(os);
        ps.println("org.nic.Repository");
        ps.close();
        os.close();
        assertNodes(serviceType,"org.openide.filesystems.Repository,org.openide.filesystems.Repository,org.nic.Repository,org.nic.Repository,",
		     "org.openide.filesystems.Repository,<b>org.openide.filesystems.Repository</b>,org.netbeans.core.startup.NbRepository,org.netbeans.core.startup.NbRepository,org.nic.Repository,<b>org.nic.Repository</b>,");

        os = fo.getOutputStream(lock); 
        ps = new PrintStream(os);
        ps.println("org.nic.Repository");
        ps.println("-org.netbeans.core.startup.NbRepository");
        ps.close();
        os.close();
        assertNodes(serviceType,"org.openide.filesystems.Repository,org.openide.filesystems.Repository,-org.netbeans.core.startup.NbRepository,-org.netbeans.core.startup.NbRepository,org.nic.Repository,org.nic.Repository,"
                     ,"org.openide.filesystems.Repository,<b>org.openide.filesystems.Repository</b>,org.netbeans.core.startup.NbRepository,<s>org.netbeans.core.startup.NbRepository</s>,org.nic.Repository,<b>org.nic.Repository</b>,");
				 

        os = fo.getOutputStream(lock);
        ps = new PrintStream(os);
        ps.println("org.nic2.ErrorManager");
        ps.close();
        os.close();
        assertNodes(serviceType,"org.openide.filesystems.Repository,org.openide.filesystems.Repository,org.nic2.ErrorManager,org.nic2.ErrorManager,",
	     "org.openide.filesystems.Repository,<b>org.openide.filesystems.Repository</b>,org.netbeans.core.startup.NbRepository,org.netbeans.core.startup.NbRepository,org.nic2.ErrorManager,<b>org.nic2.ErrorManager</b>,");
        fo.delete(lock);
        assertNodes(serviceType,"","org.openide.filesystems.Repository,org.openide.filesystems.Repository,org.netbeans.core.startup.NbRepository,org.netbeans.core.startup.NbRepository,");  
        return lock;
    }

    
    /** @param serviceType - service name (file name in META-INF/xx)
     *  @param modulesNode nodes in services for current module - 
     *             "service.name,service.displayName,class.name,class.displayName,...."
     *  @param modulesInContextNode nodes in services for all modules - 
     *             "service.name,service.displayName,class.name,class.displayName,...."
     */
    private void assertNodes(String serviceType,String moduleNodes,String moduleInContextNodes) {
	ServiceNodeHandler.ServiceRootChildren children = nodeHandler.moduleChild ;
	log("module nodes");
	assertEquals(moduleNodes,printChildren(serviceType,children));
	children = nodeHandler.allInContextChild;
	log("module in context nodes");
	assertEquals(moduleInContextNodes,printChildren(serviceType,children));
    }

    private String printChildren(final String serviceType,final ServiceRootChildren serviceRootChildren) {
	StringBuffer buff = new StringBuffer();
	Node nodes[] = null; //serviceRootChildren.getNodes(true);
	final Node nodesRef[][] = new Node[][]{null};
  
        nodesRef[0] = serviceRootChildren.getNodes(true);
	nodes = nodesRef[0];
        for (int i = 0 ; i < nodes.length; i++) {
            if (nodes[i].getName().equals(serviceType)) {
                printNode(nodes[i],buff);
            }
	}
        log(buff.toString());
	return buff.toString();
    }

    private void printNodes(Node[] nodes,StringBuffer buff) {
        for (int i = 0 ; i < nodes.length; i++) {
	    printNode(nodes[i],buff);
	}
    }

    private void printNode(Node node,StringBuffer buff) {
	buff.append(node.getName() + ",");
	buff.append(node.getHtmlDisplayName() + ",");
	Node nodes[] = node.getChildren().getNodes(true);
	printNodes(nodes,buff);
    }
    
    public void log (String string) {
	System.out.println(string);
	super.log(string);
    }

    private Node[] exploreNodes(ServiceNodeHandler.ServiceRootChildren children) throws Exception {
	while(true) {
	    children.getNodes(true);
	    synchronized(nodeHandler) {
		if (children.fullyComputed) {
		    break;
		}
                Thread.currentThread().sleep(100);
	    }
	}
	return children.getNodes(true);
    }
    
//    public static ServiceNodeHandler.ServiceRootNode getServiceNodeRoot(Project prj) {
//        ModuleLogicalView mlv = (ModuleLogicalView)prj.getLookup().lookup(ModuleLogicalView.class);
//        Node mlvNode = mlv.createLogicalView();         registerFileObjectListener(); 
//        Node nodes[] = mlvNode.getChildren().getNodes(true);
//        for (int nIt = 0 ; nIt < nodes.length ; nIt++ ) {
//            Node node = nodes[nIt];
//            System.out.println(node.getName());
//            if (node.getName().equals("important.files")) {
//                nodes = node.getChildren().getNodes(true);
//                System.out.println(nodes.length);
//                for (int mIt = 0 ; mIt < nodes.length ; mIt++ ) {
//                    node = nodes[mIt];
//                    System.out.println(node.getName());
//                    if (node.getName().equals(ServiceNodeHandler.ROOT_NODE_NAME)) {
//                        return (ServiceNodeHandler.ServiceRootNode) node; 
//                    }
//                }
//            }
//        }
//        return null;
//    }
}
