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
