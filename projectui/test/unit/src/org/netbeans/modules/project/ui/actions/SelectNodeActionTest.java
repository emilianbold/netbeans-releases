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

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport.ChangeableLookup;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class SelectNodeActionTest extends NbTestCase {
    
    private ChangeableLookup contextLookup;
    private FileObject scratch;
    private FileObject test;
    private DataObject testDO;
    
    public SelectNodeActionTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        contextLookup = new ChangeableLookup(new Object[0]);
        scratch = TestUtil.makeScratchDir(this);
        test =  scratch.createData("test", "txt");
        testDO = DataObject.find(test);
        TestUtil.setLookup(new Object[] {
            new ContextGlobalProviderImpl(),
        });
    }

    public void testEnabledUpdated() throws Exception {
        Action a = SelectNodeAction.inProjects();
        
        assertFalse(a.isEnabled());
        contextLookup.change(new Object[] {testDO});
        assertTrue(a.isEnabled());
        contextLookup.change(new Object[] {});
        assertFalse(a.isEnabled());
        contextLookup.change(new Object[] {testDO});
        assertTrue(a.isEnabled());
        contextLookup.change(new Object[] {test});
        assertTrue(a.isEnabled());
        contextLookup.change(new Object[] {testDO});
        assertTrue(a.isEnabled());
        contextLookup.change(new Object[] {test});
        assertTrue(a.isEnabled());
        contextLookup.change(new Object[] {});
        assertFalse(a.isEnabled());
    }
    
    public boolean runInEQ() {
        return true;
    }
    
    private final class ContextGlobalProviderImpl implements ContextGlobalProvider {
        
        public Lookup createGlobalContext() {
            return contextLookup;
        }
        
    }
}
