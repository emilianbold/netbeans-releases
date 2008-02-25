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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler.ServiceRootChildren;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
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
    LoggingHandler log;                
    
    public ServiceNodeHandlerTest(String testName) {
        super(testName);
    }
        
    protected @Override void setUp() throws Exception {
        super.setUp();
        log = new LoggingHandler();
        SUtil.getLogger().setLevel(Level.INFO);
        SUtil.getLogger().addHandler(log);
    }
    
    private void setUpSimpleSuite() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        EditableProperties ep = suite.getHelper().getProperties("nbproject/platform.properties");
        ep.setProperty(SuiteProperties.DISABLED_CLUSTERS_PROPERTY, CLUSTER_IDE + "," + CLUSTER_ENTERPRISE);
        suite.getHelper().putProperties("nbproject/platform.properties", ep);
        ProjectManager.getDefault().saveProject(suite);
         
        prj = TestBase.generateSuiteComponent(suite, "prj1");
        nodeHandler = prj.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
    }
    
     private void setUpStandaloneModule() throws Exception {
        prj = TestBase.generateStandaloneModule(getWorkDir(), "prj1");
        nodeHandler = prj.getLookup().lookup(ServiceNodeHandler.class);
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
        nodeHandler = prj1.getLookup().lookup(ServiceNodeHandler.class);
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
        
        nodeHandler = prj3.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
//        SuiteProjectTest.openSuite(suite);
        exploreNodes(nodeHandler.moduleChild);  
        exploreNodes(nodeHandler.allInContextChild);

        assertNodes("org.myservice",
                    "",
                    "org.myservice,org.myservice,impl,impl,"
                );
        
        nodeHandler = prj2.getLookup().lookup(ServiceNodeHandler.class);
        serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
        exploreNodes(nodeHandler.moduleChild);  
        exploreNodes(nodeHandler.allInContextChild);        
        Service service = (Service)((List) nodeHandler.moduleServiceMap.get("org.myservice")).get(0); 
        assertNotNull("prj2 contains org.myservice",service);
        service.getClasses().clear();
        service.write(prj2);
        assertNull("org.myservice is deleted",prj2.getSourceDirectory().getFileObject("META-INF/services/org.myservice"));
        
        //  test for NPE : service folder deleted #87049
        deleteServiceFolder(prj1);
        deleteServiceFolder(prj2);
        deleteServiceFolder(prj3);
        
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
        FileObject prjFo = nbRoot().getFileObject("openide.filesystems");
        FileObject prj2Fo = nbRoot().getFileObject("masterfs");
        deleteService(prjFo,"org.myservice");   
        deleteService(prj2Fo,"org.myservice");   
        try {
            NbModuleProject prj1 = (NbModuleProject) ProjectManager.getDefault().findProject(prjFo);
            NbModuleProject prj2 = (NbModuleProject) ProjectManager.getDefault().findProject(prj2Fo);
            OpenProjects.getDefault().open(new NbModuleProject[]{prj1,prj2}, false);
            nodeHandler = prj1.getLookup().lookup(ServiceNodeHandler.class);
            serviceRootNode = (ServiceNodeHandler.ServiceRootNode) nodeHandler.createServiceRootNode();
            exploreNodes(nodeHandler.moduleChild);  
            exploreNodes(nodeHandler.allInContextChild);
             
            assertNodes("org.myservice","","");
            writeServices(prj1,"org.myservice","impl");
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
    
    private Node[] exploreNodes(ServiceNodeHandler.ServiceRootChildren children) throws Exception {
        List<String> events = new ArrayList<String>();
        events.add(SUtil.LOG_COMPUTE_KEYS);
        events.add(SUtil.LOG_END_COMPUTE_KEYS);
        log.setEvents(events);
	    children.getNodes(true);
        log.waitToEvents();
        SUtil.log("Test.exploreNodes : node computed");
	    return children.getNodes(true);
    }
    
    private static void deleteServiceFolder(NbModuleProject prj) throws IOException {
        FileObject srcDir = prj.getSourceDirectory();
        // XXX huh?!
        FileObject serviceFo = FileUtil.createData(srcDir,"META-INF/services");
        final FileObject miFo = FileUtil.createData(srcDir,"META-INF");
        miFo.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                miFo.delete();
            }
        });
    }
    
    static class LoggingHandler extends Handler {
    private List events;
    private int index;
    private boolean bWait;  
    public void publish(LogRecord rec) {
       synchronized(this) {
           if (events != null && index < events.size() && rec.getMessage().equals(events.get(index))) {
               index++;
           }
           if (events != null && index == events.size() && bWait) {
               bWait = false;
               System.out.println("notify");
               notify();
               System.out.println("end not");
           }

       }
    }
    public synchronized void setEvents(List events) {
        this.events = events;
        index = 0;
        
    }
    public synchronized void waitToEvents() {
        if(events != null && index < events.size() ) {
            try {
                bWait = true;
                System.out.println("wait");
                wait();
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                                                                 ex.getMessage(),
                                                                 ex);
            }
        } else {
            System.out.println("no wait");
        }
    }
    public void flush() {
       
    }
    
    public void close() throws SecurityException {
    }
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
