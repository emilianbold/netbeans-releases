/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.clearcase.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTextFieldOperator;
//import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.junit.ide.ProjectSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author peter
 */
public class TestKit {

    public static String MOCK_LOCATION = "/tmp/vob";
    public static String REAL_LOCATION = "";
    public static String VIEW_LOCATION = "M:/view_mine/vobisko/peter/test";
    public static String CC_TEST_SWITCH = "org.netbeans.modules.clearcase.client.mockup.vobRoot";
    
    public final static String MODIFIED_STATUS = "[Modified]";
    public final static String NEW_STATUS = "[New; ]";
    public final static String IGNORED_STATUS = "[Ignored; ]";
    public final static String UPTODATE_STATUS = "";
    
    public final static String MODIFIED_COLOR = "#0000FF";
    public final static String NEW_COLOR = "#008000";
    public final static String CONFLICT_COLOR = "#FF0000";
    public final static String IGNORED_COLOR = "#999999";

    public static boolean isMocked() {

        String cc_test_switch = System.getProperty(CC_TEST_SWITCH);
        if (cc_test_switch != null || !cc_test_switch.equals("")) {
            return true;
        }
        return false;
    }

    public static File prepareProject(String prj_category, String prj_type, String prj_name) throws Exception {
		//create temporary folder for test
		String folder = "work" + File.separator + "w" + System.currentTimeMillis();
		File file = new File("/tmp", folder); // NOI18N
		file.mkdirs();

		//PseudoVersioned project
		NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
		npwo.selectCategory(prj_category);
		npwo.selectProject(prj_type);
		npwo.next();
		NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
		new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath());
		new JTextFieldOperator(npnlso, 0).setText(prj_name);
		new NewProjectWizardOperator().finish();

		ProjectSupport.waitScanFinished();//AndQueueEmpty(); // test fails if there is waitForScanAndQueueEmpty()...

		return file;
    }

    public static void deleteFolder(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] files = dir.list();
            for (String file : files) {
                deleteFolder(new File(dir, file));
            }
        }
        dir.delete();
    }

    public static void closeProject(String projectName) {
        try {
            Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
            rootNode.performPopupActionNoBlock("Close");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
        } finally {
            new ProjectsTabOperator().tree().clearSelection();
        }
    }

    public static int execute(String[] cmds, String[] env, File dir) {
        int value = -1;
        FileOutputStream fos = null;
        StreamHandler shFile = null;
        StreamHandler shError = null;
        StreamHandler shOutput = null;
        File tmpOutput = null;
        
        try {
            tmpOutput = new File("/tmp" + File.separator + "output.txt");
            value = -1;
            fos = new FileOutputStream(tmpOutput);
            Process p = Runtime.getRuntime().exec(cmds, env, dir);
            shError = new StreamHandler(p.getErrorStream(), System.err);
            shOutput = new StreamHandler(p.getInputStream(), fos);
            shError.start();
            shOutput.start();
            value = p.waitFor();
            shError.join();
            shOutput.join();

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return value;
    }
    
    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null)
            return -1;
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            Arrays.sort(expected);
            Arrays.sort(actual);
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
            return result;
        }
        return result;
    }
    
    public static String getColor(String nodeHtmlDisplayName) {
        
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1)
            return "";
        int hashPos = nodeHtmlDisplayName.indexOf('#');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
        hashPos = nodeHtmlDisplayName.indexOf('"');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
        return nodeHtmlDisplayName;
    }
    
    public static String getStatus(String nodeHtmlDisplayName) {
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1)
            return "";
        String status;
        int pos1 = nodeHtmlDisplayName.indexOf('[');
        int pos2 = nodeHtmlDisplayName.indexOf(']');
        if ((pos1 != -1) && (pos2 != -1))
            status = nodeHtmlDisplayName.substring(pos1, pos2 + 1);
        else
            status = "";
        return status;
    }

    static class StreamHandler extends Thread {

        InputStream in;
        OutputStream out;

        /** Creates a new instance of StreamHandler */
        public StreamHandler(InputStream in, OutputStream out) {
            this.in = new BufferedInputStream(in);
            this.out = new BufferedOutputStream(out);
        }

        public void run() {
            try {
                try {
                    try {
                        int i;
                        while ((i = in.read()) != -1) {
                            out.write(i);
                        }
                    } finally {
                        in.close();
                    }
                    out.flush();
                } finally {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
