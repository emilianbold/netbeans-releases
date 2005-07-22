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

import org.netbeans.jellytools.JellyTestCase;

import org.netbeans.junit.NbTestSuite;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 *
 * @author an156382
 */
public class JConsole extends JellyTestCase {
    
    /** Creates a new instance of BundleKeys */
    public JConsole(String name) {
        super(name);
    }

    public static NbTestSuite suite() {

        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JConsole("startJConsole"));
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
    
    public void startJConsole() {
      MainWindowOperator mainWindow = MainWindowOperator.getDefault();
      // push "Open" toolbar button in "System" toolbar
      mainWindow.getToolbarButton(mainWindow.getToolbar("Management"), "Start JConsole Management Console").push();
      OutputTabOperator oto = new OutputTabOperator("JConsole");
      System.out.println("*********************** WAITING FOR TEXT JConsole started ************");
      oto.waitText("JConsole started");
      RuntimeTabOperator rto = new RuntimeTabOperator();
      
      Node node = new Node(rto.getRootNode(), "Processes|JConsole");
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
