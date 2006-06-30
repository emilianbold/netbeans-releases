/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.*;
import junit.framework.TestCase;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.GandalfPersistenceManager;
import org.netbeans.modules.form.PersistenceException;
import org.openide.filesystems.FileObject;

public abstract class LayoutTestCase extends TestCase {

    private String testSwitch;

    protected LayoutModel lm = null;
    protected LayoutDesigner ld = null;
    
    protected URL url = getClass().getClassLoader().getResource("");
    
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
    
    protected String goldenFilesPath = "../../../../test/unit/data/goldenfiles/";

    protected String className;
    
    public LayoutTestCase(String name) {
        super(name);
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
                    // Equiv. to Tiger's code: currentLayout.replace("\n", System.getProperty("line.separator"));
                    currentLayout = Pattern.compile("\n").matcher(currentLayout) // NOI18N
                        .replaceAll(System.getProperty("line.separator")); // NOI18N
                    String expectedLayout = getExpectedLayoutDump(methodCount);

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
    }

    protected void tearDown() throws Exception {
        if (testSwitch != null)
            System.setProperty(LayoutDesigner.TEST_SWITCH, testSwitch);
        else
            System.getProperties().remove(LayoutDesigner.TEST_SWITCH);
        super.tearDown();
    }

    private void loadForm(FileObject file) {
        FormModel fm = null;
        GandalfPersistenceManager gpm = new GandalfPersistenceManager();
        List errors = new ArrayList();
        try {
            fm = gpm.loadForm(file, file, null, errors);
        } catch (PersistenceException pe) {
            fail(pe.toString());
        }
        
        if (errors.size() > 0) {
            System.out.println("There were errors while loading the form: " + errors);
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
