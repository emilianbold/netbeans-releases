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

package org.netbeans.modules.form.layoutdesign;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.form.FormLAF;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.GandalfPersistenceManager;
import org.netbeans.modules.form.PersistenceException;
import org.openide.filesystems.FileObject;

public abstract class LayoutTestCase extends TestCase {

    private String testSwitch;

    protected LayoutModel lm = null;
    protected LayoutDesigner ld = null;
    
    protected URL url;
    
    protected FileObject startingFormFile;
    protected File expectedLayoutFile;
    
    protected HashMap contInterior = new HashMap();
    protected HashMap baselinePosition = new HashMap();
    
    protected HashMap prefPaddingInParent = new HashMap();
    protected HashMap prefPadding = new HashMap();
    protected HashMap compBounds = new HashMap();
    protected HashMap compMinSize = new HashMap();
    protected HashMap compPrefSize = new HashMap();
    protected HashMap hasExplicitPrefSize = new HashMap();
    
    protected LayoutComponent lc = null;
    
    protected String goldenFilesPath = "../../../test/unit/data/goldenfiles/";

    protected String className;
    
    public LayoutTestCase(String name) {
        super(name);
        String resName = LayoutTestCase.class.getName().replace('.', '/') + ".class";
        URL url = getClass().getClassLoader().getResource(resName);
        String urlStr = url.toExternalForm();
        try {
            this.url = new URL(urlStr.substring(0, urlStr.indexOf(resName)));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Tests the layout model by loading a form file, add/change some components there,
     * and then compare the results with golden files.
     * In case the dump does not match, it is saved into a file under
     * build/test/unit/results so it can be compared with the golden file manually.namename
     */
    public void testLayout() throws IOException {
        loadForm(startingFormFile);

        Method[] methods = this.getClass().getMethods();
        for (int i=0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().startsWith("doChanges")) {
                try {
                    String name = getClass().getName();
                    String simpleName = name.substring(name.lastIndexOf('.')+1);
                    System.out.println("Invoking " + simpleName + "." + m.getName());
                    m.invoke(this, null);
                    
                    String methodCount = m.getName().substring(9); // "doChanges".length()
                    
                    String currentLayout = getCurrentLayoutDump();
                    String expectedLayout = getExpectedLayoutDump(methodCount);
                    String lineSep = System.getProperty("line.separator"); // NOI18N
                    if (lineSep.length() > 1) {
                        expectedLayout = expectedLayout.replace(lineSep, "\n"); // NOI18N
                    }

                    System.out.print("Comparing ... ");

                    boolean same = expectedLayout.equals(currentLayout);
                    if (!same) {
                        System.out.println("failed");
                        System.out.println("EXPECTED: ");
                        System.out.println(expectedLayout);
                        System.out.println("");
                        System.out.println("CURRENT: ");
                        System.out.println(currentLayout);
                        writeCurrentWrongLayout(methodCount, currentLayout);
                    }
                    else System.out.println("OK");
                    System.out.println("");

                    assertTrue("Model dump in step " + methodCount + " gives different result than expected", same);
                    
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    fail("Error while invoking method: " + m);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                    fail("Error while invoking method: " + m);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                    fail("Error while invoking method: " + m);
                }
            }
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        testSwitch = System.getProperty(LayoutDesigner.TEST_SWITCH);
        System.setProperty(LayoutDesigner.TEST_SWITCH, "true"); // NOI18N
        hackFormLAF(true);
    }

    protected void tearDown() throws Exception {
        hackFormLAF(false);
        if (testSwitch != null)
            System.setProperty(LayoutDesigner.TEST_SWITCH, testSwitch);
        else
            System.getProperties().remove(LayoutDesigner.TEST_SWITCH);
        super.tearDown();
    }

    private void hackFormLAF(boolean b) {
        try {
            Field f = FormLAF.class.getDeclaredField("preview"); // NOI18N
            f.setAccessible(true);
            f.setBoolean(null, b);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadForm(final FileObject file) {
        FormModel fm = null;
        GandalfPersistenceManager gpm = new GandalfPersistenceManager();
        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            fm = gpm.loadForm(file, file, null, errors);
        } catch (PersistenceException pe) {
            fail(pe.toString());
        }

        if (errors.size() > 0) {
            System.out.println("There were errors while loading the form: ");
            for (Throwable er : errors) {
                er.printStackTrace();
            }
        }

        lm = fm.getLayoutModel();

        ld = new LayoutDesigner(lm, new FakeLayoutMapper(fm,
                                                         contInterior,
                                                         baselinePosition,
                                                         prefPaddingInParent,
                                                         compBounds,
                                                         compMinSize,
                                                         compPrefSize,
                                                         hasExplicitPrefSize,
                                                         prefPadding));
    }
    
    private String getCurrentLayoutDump() {
        return lm.dump(null);
    }
    
    private String getExpectedLayoutDump(String methodCount) throws IOException {        
        expectedLayoutFile = new File(url.getFile() + goldenFilesPath + getExpectedResultFileName(methodCount) + ".txt").getCanonicalFile();
        int length = (int) expectedLayoutFile.length();
        FileReader fr = null;
        try {
            fr = new FileReader(expectedLayoutFile);
            char[] buf = new char[length];
            fr.read(buf);
            return new String(buf);
        } catch (IOException ioe) {
            fail(ioe.toString());
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException io) {
                    fail(io.toString());
                }
            }
        }
        return null;
    }

    private String getExpectedResultFileName(String methodCount) {
        return className + "-ExpectedEndModel" + methodCount;
    }

    private void writeCurrentWrongLayout(String methodCount, String dump) throws IOException {
        // will go to form/build/test/unit/results
        File file = new File(url.getFile() + "../results").getCanonicalFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file, getExpectedResultFileName(methodCount)+".txt");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(dump);
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }
    
}
