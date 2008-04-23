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

import java.util.StringTokenizer;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.test.refactoring.operators.FindUsagesAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesClassOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class RefactoringTestCase extends JellyTestCase {

    public static String projectName = "RefactoringTest";
    public static final char treeSeparator = '|';

    public RefactoringTestCase(String name) {
        super(name);
    }

    public void testBasic() {
        final String fileName = "FindUsagesClass";        
        openSourceFile("fu",fileName);
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(12, 19);
        new FindUsagesAction().performPopup(editor);
        new EventTool().waitNoEvent(1000);
        FindUsagesClassOperator findUsagesClassOperator = new FindUsagesClassOperator();
        findUsagesClassOperator.getFind().push();
        
    }

    protected  void openFile(String treeSubPackagePathToFile, String fileName) {
        // debug info, to be removed
        //this.treeSubPackagePathToFile = treeSubPackagePathToFile;
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();        
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
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

    private void waitForChildNode(String parentPath, String childName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
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
    
    public static void main(String[] args) {
        TestRunner.run(RefactoringTestCase.class);
    }
}
