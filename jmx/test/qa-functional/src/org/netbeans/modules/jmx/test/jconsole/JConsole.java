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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
      
      OutputTabOperator oto = null;
      int maxToWait = 10;
      while(maxToWait > 0) {
        try{
            oto = new OutputTabOperator("JConsole");
            break;
        }catch(Exception e) {
            System.out.println("Output tab not yet displayed " + e.toString());
            maxToWait--;
        }
      }
      System.out.println("*********************** WAITING FOR TEXT JConsole started ************");
      
      maxToWait = 10;      
      while(maxToWait > 0) {
        try{
            oto.waitText("JConsole started");
            break;
        }catch(Exception e){
              System.out.println("JConsole not started, will wait again");
              maxToWait--;
        }
      }
      
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
