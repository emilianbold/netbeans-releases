/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


/*
 * Trace.java
 *
 * Created on January 11, 2007, 2:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.util;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Alexandr Scherbatiy
 */

public class Debug {
    
    public static final String BLANK = "  ";
    
    public static final boolean HIERARCHY_FLAG = false;
    
    public static void showLog(NbTestCase testCase){
	System.setOut(testCase.getLog());
    }
    
    public static void showNode(Node node){
	System.out.println("[Node]");
	showTreePath(node.tree(), node.getTreePath());
    }
    
    
    public static void showTreePath(JTreeOperator treeOperator, TreePath path){
	showTreePath("", treeOperator, path);
    }
    
    public static void showTreePath(String blank, JTreeOperator treeOperator, TreePath path){
	System.out.println(blank +  "[path] " + "\""  + path + "\"");
	
	for(int i=0; i < treeOperator.getChildCount(path); i++ ){
	    showTreePath(blank + BLANK, treeOperator, treeOperator.getChildPath(path, i));
	}
	
    }
    
    
    public static void showTree(JTreeOperator treeOperator){
	
	TreeModel model = treeOperator.getModel();
	System.out.println("[Tree]");
	showTree("", model,  model.getRoot());
    }
    
    public static void showTree(String blank, TreeModel model, Object node){
	System.out.println(blank + "\"" + node + "\"");
	
	for(int i=0; i < model.getChildCount(node); i++){
	    showTree(blank + BLANK, model, model.getChild(node, i));
	}
	
    }
    
    
    //==================      Show Components    ==============================================
    
    public static void show() {
	out("======================= Show Dump ======================= ");
	show(MainWindowOperator.getDefault().getSource());
	out("========================================================= ");
    }
    
    public static void show(Component component) {
	show(component, "", 0);
    }
    
    
    private static void show(Component component, String indent, int index) {
	
	System.out.println(indent + index + ": " + component);
	componentDescription(indent, component);
	
	if(HIERARCHY_FLAG) {
	    showClassHierarchy(component);
	}
	
	if (component instanceof Container) {
	    Component [] components = ((Container) component).getComponents();
	    
	    for (int i=0; i < components.length; i++) {
		show(components [i], "  " + indent, i);
	    }
	}
    }
    
    
    private static void  componentDescription(String blank, Component component) {
	
	String name = component.getName();
	
	//if (name!=null){
	System.out.println(blank + "   component name = \"" + component.getName() + "\"") ;
	//}
	
	if ( component instanceof JTabbedPane  ){
	    System.out.println(blank + " [JTabbedPane]");
	}else if ( component instanceof JTree ){
	    System.out.println(blank + " [JTree]");
	    //JTestTree.show(blank, ( JTree) component);
	}else if ( component instanceof JList ){
	    System.out.println(blank + " [JList]");
	} else if ( component instanceof JTable ){
	    System.out.println(blank + " [JTable]");
	}
	
    }
    
    
    
    public static void showClassHierarchy(Object obj){
	
	System.out.print("==================================================================");
	System.out.println("==================================================================");
	System.out.println("Class Hierarchy:");
	showClassHierarchy("", obj.getClass());
	System.out.println("String: " + obj.toString());
	System.out.print("==================================================================");
	System.out.println("==================================================================");
	
    }
    private static void showClassHierarchy(String blank,Class cls){
	if(!cls.getName().equals("java.lang.Object")){
	    System.out.println(blank + "class: " + cls.getName());
	    Class[] inter = cls.getInterfaces();
	    
	    for(int i=0; i< inter.length; i++){
		showInterfaceHierarchy(blank, inter[i]);
	    }
	    
	    showClassHierarchy(blank + " ", cls.getSuperclass());
	}
    }
    
    private static void showInterfaceHierarchy(String blank, Class inter){
	System.out.println(blank + "interface: " + inter.getName());
	Class[] interf = inter.getInterfaces();
	
	for(int i=0; i< interf.length; i++){
	    showInterfaceHierarchy(blank + " ", interf[i]);
	}
    }
    
    //==================      Out  Procedure    ==============================================
    
    private static void out(String text){
	System.out.println("[main]: " + text );
    }
    
}
