/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.autoupdate.cli;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.*;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.InstalledUpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach
 */
public class ListModulesTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
    public ListModulesTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
//        clearWorkDir();
        
//        System.setProperty("netbeans.user", getWorkDirPath() + File.separator + "userdir");

        // initialize whole infra
        //Lookup.getDefault().lookup(ModuleInfo.class);
        Lkp.ic.setPairs(Collections.<AbstractLookup.Pair>emptyList());
        Lkp.ic.add(InstalledUpdateProvider.getDefault());
        //Lkp.getDefault().lookup(ModuleInfo.class);
        Lkp.ic.add(new ModuleOptions());
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ListModulesTest.class);
        
        return suite;
    }

    public void testAModuleIsPrinted() throws CommandException {
        Lkp.ic.add(new MyModule("my.cnb", 5, new SpecificationVersion("1.3"), true));
        Lkp.ic.add(new MyModule("my.snd.cnb", -1, new SpecificationVersion("5.3.1"), false));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        CommandLine.getDefault().process(new String[] { "--modules", "--list" }, System.in, os, err, new File("."));

        assertEquals("No errors", 0, err.size());

        String out = os.toString();
        if (out.indexOf("my.cnb") == -1) {
            fail("my.cnb should be there: " + out);
        }
        if (out.indexOf("my.snd.cnb") == -1) {
            fail("snd should be there: " + out);
        }
        if (out.indexOf("disabled") == -1) {
            fail("disabled should be there: " + out);
        }
        if (out.indexOf("enabled") == -1) {
            fail("enabled should be there: " + out);
        }
        if (out.indexOf("/5") == -1) {
            fail("5 should be there: " + out);
        }
        if (out.indexOf("1.3") == -1) {
            fail("1.3 should be there: " + out);
        }
        if (out.indexOf("5.3.1") == -1) {
            fail("5.3.1 should be there: " + out);
        }
    }
    
    public static final class Lkp extends AbstractLookup {
        public static InstanceContent ic = new InstanceContent();
        
        public Lkp() {
            super(ic);
        }
    }

    private static class MyModule extends ModuleInfo {

        private boolean enabled;
        private String cnb;
        private int r;
        private SpecificationVersion specV;

        MyModule(String cnb, int r, SpecificationVersion specV, boolean enabled) {
            this.cnb = cnb;
            this.r = r;
            this.specV = specV;
            this.enabled = enabled;
        }

        @Override
        public String getCodeNameBase() {
            return cnb;
        }

        @Override
        public int getCodeNameRelease() {
            return r;
        }

        @Override
        public String getCodeName() {
            if (r == -1) {
                return getCodeNameBase();
            }
            return getCodeNameBase() + '/' + r;
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            return specV;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public Object getAttribute(String attr) {
            return null;
        }

        @Override
        public Object getLocalizedAttribute(String attr) {
            return null;
        }

        @Override
        public Set getDependencies() {
            return new HashSet();
        }

        @Override
        public boolean owns(Class clazz) {
            return false;
        }

    }
}
