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
import java.io.IOException;
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
    
    protected HashMap idToNameMap = new HashMap();
    
    protected LayoutComponent lc = null;
    
    public LayoutTestCase(String name) {
        super(name);
    }
    
    
    /**
     * Tests the layout model by loading a form file, add/change some components there,
     * and then compare the results with golden files.
     * In case the dump does not match, it is saved into a file under
     * build/test/unit/results so it can be compared with the golden file manually.namename
     */
    public void testLayout() {
		
        loadForm(startingFormFile);
        doChanges(lm);
        
        String currentLayout = getCurrentLayoutDump();
        String expectedLayout = getExpectedLayoutDump();
        
        System.out.println("Comparing ... ");
        System.out.println("EXPECTED: ");
        System.out.println(expectedLayout);
        System.out.println("");
        System.out.println("CURRENT: ");
        System.out.println(currentLayout);
        System.out.println("");
        
        assertEquals(expectedLayout, currentLayout);
    }
    
    private void loadForm(FileObject file) {
        GandalfPersistenceManager gpm = new GandalfPersistenceManager();
        List errors = new ArrayList();
        
        try {
	    RADComponent.setIdCounter(getCounterId());
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
        return lm.dump(idToNameMap);
    }
    
    private String getExpectedLayoutDump() {
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
    
    protected abstract void doChanges(LayoutModel model);
    
    protected abstract int getCounterId();
    
}
