/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.jconsole;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.JButton;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 *
 * @author jfdenise
 */
public class J2SEProject extends JellyTestCase {
    private static String PROJECT_NAME="JMXTESTSampleProjectToTestJ2SEProjectIntegration";
    private static String ORIGINAL_TMP_FILE;
    
    static {
        //We need it to help tools.jar API to findout the local connector
        //This is an horrible hack! But no way to make it work without /var/tmp/ tmp file
        //Flushing env
        java.util.Enumeration e = System.getProperties().keys();
        java.util.Enumeration e2 = System.getProperties().elements();
        while(e.hasMoreElements())
            System.out.println(e.nextElement() + "=" + e2.nextElement());
            
        //java.io.tmpdir.default is defined on 4.2 ...
        String tmpFile = System.getProperty("java.io.tmpdir.default");
       
        if(tmpFile == null) {
            //This is for Windows platform, hoping it is set.
            tmpFile = System.getProperty("Env-TMP");
        }
           
        if(tmpFile == null) {
            if(!System.getProperty("os.name").startsWith("Win"))
                tmpFile = "/var/tmp";
            //else
            //We can't find the tmp dir. The test must fail
        }
        
        ORIGINAL_TMP_FILE =  tmpFile == null ? null :  tmpFile + File.separator;
        
        System.out.println("TMP FILE : " + ORIGINAL_TMP_FILE);
    }
    
    /** Creates a new instance of BundleKeys */
    public J2SEProject(String name) {
        super(name);
    }

    public static NbTestSuite suite() {

        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new J2SEProject("createSampleProject"));
        suite.addTest(new J2SEProject("runWithJConsole"));
        suite.addTest(new J2SEProject("debugWithJConsole"));
        suite.addTest(new J2SEProject("runWithRemoteManagement"));
        suite.addTest(new J2SEProject("debugWithRemoteManagement"));
       
        return suite;
    }

    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }

    public void setUp() {
    }

    public void tearDown() {
    }
    
    public void createSampleProject() {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Samples|Management");
        npwo.selectProject("Anagram Game Managed with JMX");
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        
        npnlso.txtProjectName().setText(PROJECT_NAME);
        npwo.finish();
        
    }
    
    public void runWithJConsole() {
        assertTmpDir();
        String ctxt = preLocalConnection();
        doItLocal("Run Main Project With Local Management", "run-management");
        postLocalConnection(ctxt);
    }
    
    public void debugWithJConsole() {
        assertTmpDir();
        String ctxt = preLocalConnection();
        doItLocal("Debug Main Project With Local Management", "debug-management");
        postLocalConnection(ctxt);
    }
    
    public void runWithRemoteManagement() {
       doItRemote("Run Main Project with Remote Management...", "run-management");      
    }
   
    public void debugWithRemoteManagement() {
       doItRemote("Debug Main Project with Remote Management...", "debug-management");      
    }
    
    private void assertTmpDir() {
        if(ORIGINAL_TMP_FILE == null)
            throw new IllegalArgumentException("TMP DIR is not set, check env");
    }
    
    private String preLocalConnection() {
        //Just in case we are not testing using XTest harness
        if(ORIGINAL_TMP_FILE == null) return null;
        String current = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", ORIGINAL_TMP_FILE);
        return current;
    }
    
    private void postLocalConnection(String ctxt) {
        if(ctxt == null) return;
        System.setProperty("java.io.tmpdir", ctxt);
    }
    
    private void doItLocal(String action, String target) {
       syncOnProject();
       activateLocalAction(action);
       try {
           Thread.sleep(2000);
       }catch(Exception e){}
       trackAndKillJConsole(target);
    }
    
    private void doItRemote(String action, String target) {
       syncOnProject();
       activateRemoteAction(action); 
       try {
           Thread.sleep(2000);
       }catch(Exception e){}
       trackAndKillJConsole(target);
    }
    
    private void syncOnProject() {
       //Sync on Project node
       ProjectsTabOperator pto = new ProjectsTabOperator();
       JTreeOperator tree = pto.tree();
       ProjectRootNode prn = pto.getProjectRootNode(PROJECT_NAME);
       System.out.println("FOUND NODE, WAITING 5 sec");
       try {
           Thread.sleep(5000);
       }catch(Exception e){}
    }
    
    private static JButton findOkButton(Component root) {
          if (root instanceof Container) {
                Component[] components = ((Container) root).getComponents();
                for (int i = 0; i < components.length; i++) {
                    JButton b = findOkButton(components[i]);
                    if(b != null) return b;
                }
          }
          if( root instanceof JButton) {
           JButton b = (JButton) root;
           String label = b.getText();
           System.out.println("Found button label : [" + b.getText() + "]");
           if(label.equals("OK")) return b;
          }
          
          return null;
    }
    
    private void activateRemoteAction(final String action) {
       //We must thread the call in order not to be locked by dialog
       Runnable r = new Runnable() {
           public void run() {
                JMenuBarOperator op = MainWindowOperator.getDefault().menuBar();
                op.pushMenu("Run|Remote Management|"+action);
           }
       };
       new Thread(r).start();
       
       //Control dialog
       DialogOperator dop = new DialogOperator();
       
       dop.waitTitle("Remote Management Configuration");

       System.out.println("FOUND DIALOG : " + dop.getTitle());
       Component[] comp = dop.getComponents();
       JButton b = findOkButton(comp[0]);
       JButtonOperator op = new JButtonOperator(b);
       op.clickMouse();
       //for(int i = 0; i < comp.length; i++) {
         //  System.out.println("Component name " + comp[i].getName());
       //}
    }
    
    private void activateLocalAction(String action) {
       MainWindowOperator mainWindow = MainWindowOperator.getDefault();
      // push "Open" toolbar button in "System" toolbar
      mainWindow.getToolbarButton(mainWindow.getToolbar("Management"), action).push();
      System.out.println("Pushed Action");
    }
    
    private void trackAndKillJConsole(String target) {
        //Access output and synchronize on it
        OutputTabOperator oto = null;
        int maxToWait = 10;
        while(maxToWait > 0) {
            try {
                oto = new OutputTabOperator(target);
                break;
            }catch(Exception e) {
                System.out.println("Output tab not yet displayed " + e.toString());
                maxToWait--;
            }
        }
        System.out.println("*********************** WAITING FOR TEXT Found manageable process ... ************");
        maxToWait = 10;
      while(maxToWait > 0) {
          try {
              System.out.println("Waiting for JConsole to start");
              oto.waitText("Found manageable process, connecting JConsole to process...");   
              break;
          }catch(Exception e){
              System.out.println("JConsole not started, will wait again");
              maxToWait--;
          }
      }
      System.out.println("*********************** TEXT FOUND ************");
      
      OutputTabOperator oto2 = null;
      maxToWait = 10;
      while(maxToWait > 0) {
            try {
                oto2 = new OutputTabOperator("-connect-jconsole");
                break;
            }catch(Exception e) {
                System.out.println("Output tab not yet displayed " + e.toString());
                maxToWait--;
            }
        }
      
      maxToWait = 10;
      while(maxToWait > 0) {
          try {
              System.out.println("Waiting for jconsole  -interval=4");
              oto2.waitText("jconsole  -interval=4");
              break;
          }catch(Exception e){
              System.out.println("JConsole not started, will wait again");
              maxToWait--;
          }
      }
      
      //Now we can kill
      //RuntimeTabOperator rto = RuntimeTabOperator.invoke();
      // or when Runtime pane is already opened
      RuntimeTabOperator rto = new RuntimeTabOperator();
      
      Node node = new Node(rto.getRootNode(), "Processes|anagrams (-connect-jconsole)");
      String[] child = node.getChildren();
      for(int i = 0; i < child.length; i++) {
        System.out.println(child[i]);
      }
      //Little tempo to kill once stabilized state
      try {
          Thread.sleep(2000);
      }catch(Exception e) {}
      node.callPopup().pushMenu("Terminate Process");
    }
}
