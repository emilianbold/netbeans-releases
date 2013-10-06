/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 ** Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.permanentUI;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.test.permanentUI.utils.ProjectContext;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marian.Mirilovic@oracle.com
 */
public abstract class PermUITestCase extends JellyTestCase {

    protected static final char TREE_SEPARATOR = '|';
    protected static final boolean screen = false;
    protected static ProjectRootNode testProjectRootNode = null;
    private static ProjectsTabOperator pto = null;

    //context says what kind of project is open
    protected static ProjectContext context = ProjectContext.NONE;

    protected static boolean initialized = false;

    public PermUITestCase(String name) {
        super(name);
    }

    /**
     * Setup called before every test case.
     */
    @Override
    public void setUp() {
        try {
            System.setErr(new PrintStream(new File(getWorkDir(), getName() + ".error")));
            clearWorkDir();
            getWorkDir();
            if (!initialized) {
                initialize();
                initialized = true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("########  " + " CONTEXT -> " + context.toString() + " - " + getName() + "  #######");
    }

    public abstract void initialize() throws IOException;

    public void ref(Object o) {
        getRef().println(o);
    }

    public void ref(File f) {
        getRef().println("==>" + f.getName());
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String s = br.readLine();
            while (s != null) {
                getRef().println(s);
                s = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

    public void ref(FileObject fo) {
        if (fo.isValid()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
                getRef().println("==>" + fo.getName());
                String s = br.readLine();
                while (s != null) {
                    getRef().println(s);
                    s = br.readLine();
                }
            } catch (IOException ioe) {
                fail(ioe);
            }
        }
    }

    /**
     * Opens file in editor.
     *
     * @param project
     * @param treeSubPackagePathToFile
     * @param fileName
     */
    protected void openFile(String project, String treeSubPackagePathToFile, String fileName) {
        openProject(project);
        StringTokenizer st = new StringTokenizer(treeSubPackagePathToFile, TREE_SEPARATOR + "");
        if (st.countTokens() > 1) {
            String token = st.nextToken();

            String fullpath = token;
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                waitForChildNode(project, fullpath, token);
                fullpath += TREE_SEPARATOR + token;
            }
        }
        // last node
        waitForChildNode(project, treeSubPackagePathToFile, fileName);
        // end of fix of issue #51191

        Node node = new Node(testProjectRootNode, treeSubPackagePathToFile + TREE_SEPARATOR + fileName);
        //node.performPopupAction("Open");
        new OpenAction().performAPI(node);  //should be more stable then performing open action from popup
    }

    private void waitForChildNode(String project, String parentPath, String childName) {
        openProject(project);
        Node parent = new Node(testProjectRootNode, parentPath);
        final String finalFileName = childName;
        try {
            // wait for max. 3 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object parent) {
                    return ((Node) parent).isChildPresent(finalFileName) ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    protected void openProject(String projectName) {
        if (pto == null) {
            pto = ProjectsTabOperator.invoke();
        }

        if (testProjectRootNode == null) {
            try {
                openDataProjects(projectName);
                testProjectRootNode = pto.getProjectRootNode(projectName);
                testProjectRootNode.select();
            } catch (IOException ex) {
                throw new JemmyException("Open project [" + projectName + "] fails !!!", ex);
            }
        } else {
            log("Project is opened!");
        }
    }

/**    @Override
    protected void tearDown() throws Exception {
        getRef().flush();
        getRef().close();
        assertFile("Golden file differs ", getReferencFile(), getGoldenFile(), getWorkDir(), new LineDiff());
        //compareReferenceFiles();
        File diffFile = getDiffFile(getReferencFile().getAbsolutePath(), getWorkDir());
        System.out.println("+++++++++ Diff file [" + diffFile.getAbsolutePath() + "] exists=" + diffFile.exists());
        if (diffFile.exists()) {
            System.out.println("============= DIFF >>>> =======================================");
            FileReader fr = new FileReader(diffFile);
            int oneByte;
            while ((oneByte = fr.read()) != -1) {
                System.out.print((char) oneByte);
            }
            System.out.flush();
            System.out.println("============= <<<< DIFF =======================================");
        }

        System.out.println("Test " + getName() + " finished !");
    }
*/
    protected File getGoldenFile(String category, String fileName) {
        return new File(getDataDir() + File.separator + "permanentUI" + File.separator + category + File.separator + fileName + ".txt");
    }

    /**
     * Copy of NbTestCate#getDiffName
     *
     * @param pass
     * @param diff
     * @return diff file
     */
    protected File getDiffFile(String pass, File diff) {
        StringBuilder d = new StringBuilder();
        int i1, i2;

        d.append(diff.getAbsolutePath());
        i1 = pass.lastIndexOf('\\');
        i2 = pass.lastIndexOf('/');
        i1 = i1 > i2 ? i1 : i2;
        i1 = -1 == i1 ? 0 : i1 + 1;

        i2 = pass.lastIndexOf('.');
        i2 = -1 == i2 ? pass.length() : i2;

        if (0 < d.length()) {
            d.append("/");
        }

        d.append(pass.substring(i1, i2));
        d.append(".diff");
        return new File(d.toString());
    }

    private File getReferencFile() throws IOException {
        File refFile = new File(getWorkDir(), getName() + ".ref");
        return refFile;
    }

    protected ComponentChooser getCompChooser(final String className) {
        return new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().equals(className);
            }

            @Override
            public String getDescription() {
                return className;
            }
        };
    }

    /**
     * Take a screen shot.
     */
    protected void captureScreen() {
        if (screen) {
            try {
                String captureFile = getWorkDir().getAbsolutePath() + File.separator + "screen.png";
                PNGEncoder.captureScreen(captureFile, PNGEncoder.COLOR_MODE);
            } catch (Exception ex) {
                ex.printStackTrace(getLog());
            }
        }
    }

    protected class LogFiles {

        String pathToIdeLogFile;
        String pathToGoldenLogFile;
        String pathToDiffLogFile;
        PrintStream ideFileStream;
        PrintStream goldenFileStream;
        PrintStream diffFileStream;

        protected LogFiles() {
            pathToIdeLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
            pathToGoldenLogFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
            pathToDiffLogFile = getWorkDirPath() + File.separator + getName() + ".diff";
            ideFileStream = null;
            goldenFileStream = null;
        }

        protected PrintStream getIdeFileStream() throws FileNotFoundException {
            return ideFileStream = new PrintStream(pathToIdeLogFile);
        }

        protected PrintStream getGoldenFileStream() throws FileNotFoundException {
            return goldenFileStream = new PrintStream(pathToGoldenLogFile);
        }

        protected PrintStream getDiffFileStream() throws FileNotFoundException {
            return diffFileStream = new PrintStream(pathToDiffLogFile);
        }
    }

}
