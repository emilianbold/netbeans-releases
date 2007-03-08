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
        File x = new File(src, "x");
        FileObject xfo = FileUtil.createData(x);
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
                return true;
            }
        }
        ActionProvider proxy = new LookupMergerImpl().merge(Lookups.fixed(new Actions(p), new MockActionProvider()));
        assertTrue(Arrays.asList(proxy.getSupportedActions()).contains(cmd));
        Lookup selection = Lookups.singleton(xfo);
        assertTrue(proxy.isActionEnabled(cmd, selection));
        proxy.invokeAction(cmd, selection);
        assertEquals("[build.xml:[twiddle]:{file=x}]", targetsRun.toString());
        assertFalse(ranMockAction[0]);
    }

}
