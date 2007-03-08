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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test for merging action providers.
 * @author Jesse Glick
 */
public class LookupMergerImplTest extends NbTestCase {

    private static List<String> targetsRun = new ArrayList<String>();
    static {
        Actions.TARGET_RUNNER = new Actions.TargetRunner() {
            public void runTarget(FileObject scriptFile, String[] targetNameArray, Properties props) {
                targetsRun.add(scriptFile.getNameExt() + ":" + Arrays.toString(targetNameArray) + ":" + new TreeMap<Object,Object>(props));
            }
        };
    }

    /**
     * Create test.
     * @param name test name
     */
    public LookupMergerImplTest(String name) {
        super(name);
    }

    /**
     * Clear everything up.
     * @throws Exception for whatever reason
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        targetsRun.clear();
        clearWorkDir();
    }

    /**
     * Test that natures can add action behaviors, but not to the exclusion of the default impl.
     * @throws Exception for various reasons
     */
    public void testActionBindingFromNatures() throws Exception {
        File base = getWorkDir();
        File src = new File(base, "src");
        File x1 = new File(src, "x1");
        FileObject x1fo = FileUtil.createData(x1);
        File x2 = new File(src, "x2");
        FileObject x2fo = FileUtil.createData(x2);
        File y1 = new File(src, "y1");
        FileObject y1fo = FileUtil.createData(y1);
        File y2 = new File(src, "y2");
        FileObject y2fo = FileUtil.createData(y2);
        File buildXml = new File(base, "build.xml");
        FileUtil.createData(buildXml);
        AntProjectHelper helper = FreeformProjectGenerator.createProject(base, base, getName(), buildXml);
        FreeformProject p = (FreeformProject) ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        FreeformProjectGenerator.TargetMapping tm = new FreeformProjectGenerator.TargetMapping();
        final String cmd = "twiddle-file";
        tm.name = cmd;
        tm.targets = Arrays.asList("twiddle");
        FreeformProjectGenerator.TargetMapping.Context context = new FreeformProjectGenerator.TargetMapping.Context();
        tm.context = context;
        context.folder = "src";
        context.format = "relative-path";
        context.property = "file";
        context.pattern = "^x";
        context.separator = null;
        FreeformProjectGenerator.putTargetMappings(helper, Arrays.asList(tm));
        final boolean[] ranMockAction = {false};
        class MockActionProvider implements ActionProvider { // similar to JavaActions
            public String[] getSupportedActions() {
                return new String[] {cmd};
            }
            public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
                ranMockAction[0] = true;
            }
            public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
                FileObject f = context.lookup(FileObject.class);
                return f != null && !f.getNameExt().contains("2");
            }
        }
        ActionProvider proxy = new LookupMergerImpl().merge(Lookups.fixed(new MockActionProvider(), new Actions(p)));
        assertTrue(Arrays.asList(proxy.getSupportedActions()).contains(cmd));
        Lookup selection = Lookups.singleton(x1fo);
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[build.xml:[twiddle]:{file=x1}]", targetsRun.toString());
        assertFalse(ranMockAction[0]);
        targetsRun.clear();
        selection = Lookups.singleton(x2fo);
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[build.xml:[twiddle]:{file=x2}]", targetsRun.toString());
        assertFalse(ranMockAction[0]);
        targetsRun.clear();
        selection = Lookups.singleton(y1fo);
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[]", targetsRun.toString());
        assertTrue(ranMockAction[0]);
        selection = Lookups.singleton(y2fo);
        assertFalse(proxy.isActionEnabled(cmd, selection));
    }

}
