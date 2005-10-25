/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
import junit.framework.TestCase;
import org.netbeans.modules.form.FormDesigner;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.GandalfPersistenceManager;
import org.netbeans.modules.form.PersistenceException;
import org.netbeans.modules.form.RADComponent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class LayoutTestCase extends TestCase {
    
    private LayoutModel lm = null;
    private FormModel fm = null;
    private FormDesigner fd = null;
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
                    System.out.println("Invoking " + m.getName());
                    m.invoke(this, null);
                    
                    String methodCount = m.getName().substring(9);
                    
                    String currentLayout = getCurrentLayoutDump();
                    String expectedLayout = getExpectedLayoutDump(methodCount);

                    System.out.println("Comparing ... ");
                    System.out.println("EXPECTED: ");
                    System.out.println(expectedLayout);
                    System.out.println("");
                    System.out.println("CURRENT: ");
                    System.out.println(currentLayout);
                    System.out.println("");

                    boolean same = expectedLayout.equals(currentLayout);
                    if (!same) {
                        writeCurrentWrongLayout(currentLayout);
                    }

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
    
    private void loadForm(FileObject file) {
        GandalfPersistenceManager gpm = new GandalfPersistenceManager();
        List errors = new ArrayList();
        
        try {
//	    RADComponent.setIdCounter(getCounterId());
            fm = gpm.loadForm(file, file, null, errors);
        } catch (PersistenceException pe) {
            fail(pe.toString());
        }
        
        if (errors.size() > 0) {
            System.out.println("There were errors while loading the form: " + errors);
        }
        
        lm = fm.getLayoutModel();
        
        FakeLayoutMapper fakemapper = new FakeLayoutMapper(fm,
                contInterior,
                baselinePosition,
                prefPaddingInParent,
                compBounds,
                compMinSize,
                compPrefSize,
                hasExplicitPrefSize,
                prefPadding);
        ld = new LayoutDesigner(lm, fakemapper);
    }
    
    private String getCurrentLayoutDump() {
        return lm.dump(null);
    }
    
    private String getExpectedLayoutDump(String methodCount) throws IOException {        
        expectedLayoutFile = new File(url.getFile() + goldenFilesPath + className + "-ExpectedEndModel" + methodCount + ".txt").getCanonicalFile();
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
        
    private void writeCurrentWrongLayout(String dump) throws IOException {
        // will go to form/build/test/unit/results
        File file = new File(url.getFile() + "../results").getCanonicalFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file, "layoutModelDump.fail");
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
