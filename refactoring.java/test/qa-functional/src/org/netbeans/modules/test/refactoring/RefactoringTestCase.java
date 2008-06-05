/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.test.refactoring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.diff.LineDiff;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public abstract class RefactoringTestCase extends NbTestCase {

    public static final char treeSeparator = '|';
    
    /**
     * The distance from the root of preview tree. Nodes located 
     * closer to the root then this values will be sorted before dumping 
     * to ref file
     */
    public static int sortLevel = 2;
    private PrintStream jemmyError;
    private PrintStream jemmyOutput;

    public RefactoringTestCase(String name) {
        super(name);
    }
    
    public void ref(String text) {
        getRef().print(text);
    }
    
    public void ref(Object o) {
        getRef().println(o);
    }

    protected void browseChildren(TreeModel model, Object parent, int level) {
        Object invoke = getPreviewItemLabel(parent);
        for (int i = 0; i < level; i++) ref("    ");
        ref(invoke.toString() + "\n");

        int childs = model.getChildCount(parent);
        ArrayList<Object> al = new ArrayList<Object>(childs);  //storing childs for sorting        
        
        for (int i = 0; i < childs; i++) {
            Object child = model.getChild(parent, i);
            al.add(child);
        }        
        if ((level+1) <= sortLevel) {
            sortChilds(al);
        }        

        while(!al.isEmpty()) {            
            Object child = al.remove(0);
            browseChildren(model, child, level + 1);                    
        }
        
    }
        
    protected  void openFile(String treeSubPackagePathToFile, String fileName) {
        // debug info, to be removed
        //this.treeSubPackagePathToFile = treeSubPackagePathToFile;
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(getProjectName());
        prn.select();        
        StringTokenizer st = new StringTokenizer(treeSubPackagePathToFile, treeSeparator + "");
        String token = "";
        String oldtoken = "";
        if (st.countTokens() > 1) {
            token = st.nextToken();
            
            String fullpath = token;
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                waitForChildNode(fullpath, token);
                fullpath += treeSeparator + token;
            }
        }
        // last node
        waitForChildNode(treeSubPackagePathToFile, fileName);
        // end of fix of issue #51191

        Node node = new Node(prn, treeSubPackagePathToFile + treeSeparator + fileName);
        //node.performPopupAction("Open");
        new OpenAction().performAPI(node);  //should be more stable then performing open action from popup

    }
    
    public String getFileForSelectedNode(JTree tree) {
        TreePath selectionPath = tree.getSelectionPath();
        Object pathComponent = selectionPath.getPathComponent(2);
        return (String) getPreviewItemLabel(pathComponent);
    }

    protected Object getPreviewItemLabel(Object parent)  {
        try {
            Method method = parent.getClass().getDeclaredMethod("getLabel", null);
            method.setAccessible(true);
            Object invoke = method.invoke(parent, null);
            return invoke;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);            
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RefactoringTestCase.class.getName()).log(Level.SEVERE, null, ex);
        }
        fail("Error in reflection");
        return null;
    }
        
    private void sortChilds(List<Object> al) {
        final HashMap<Object,String> hashMap = new HashMap<Object, String>();
        for (Object object : al) {
            hashMap.put(object,(String) getPreviewItemLabel(object));
        }
                        
        Collections.<Object>sort(al,new Comparator() {

            public int compare(Object o1, Object o2) {
                return hashMap.get(o1).compareTo(hashMap.get(o2));
            }
        });
    }

    private void waitForChildNode(String parentPath, String childName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getProjectName());
        prn.select();
        Node parent = new Node(prn, parentPath);      
        final String finalFileName = childName;
        try {
            // wait for max. 3 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            new Waiter(new Waitable() {

                public Object actionProduced(Object parent) {
                    return ((Node) parent).isChildPresent(finalFileName) ? Boolean.TRUE : null;
                }

                public String getDescription() {
                    return ("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
    
    protected void openSourceFile(String dir, String srcName) {
        openFile(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir")+treeSeparator+dir, srcName);
    }
    
    @Override
    protected void setUp() throws Exception {        
        jemmyOutput = new PrintStream(new File(getWorkDir(), getName() + ".jemmy"));
        jemmyError = new PrintStream(new File(getWorkDir(), getName() + ".error"));        
        //JemmyProperties.setCurrentOutput(new TestOut(System.in, jemmyOutput , jemmyError));
        JemmyProperties.setCurrentOutput(new TestOut(System.in, jemmyOutput , System.out));
        System.out.println("Test "+getName()+" started");        
    }

    @Override
    protected void tearDown() throws Exception {        
        getRef().close();
        jemmyOutput.close();
        jemmyError.close();
        System.out.println();
        assertFile("Golden file differs ", new File(getWorkDir(),getName()+".ref"), getGoldenFile(), getWorkDir(), new LineDiff());
        System.out.println("Test "+getName()+" finished");
        
    }

    public abstract String getProjectName();
        
}

