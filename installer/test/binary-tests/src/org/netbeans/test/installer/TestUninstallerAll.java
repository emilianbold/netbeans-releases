/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * $Id$
 *
 */
package org.netbeans.test.installer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Mikhail Vaysman
 */
public class TestUninstallerAll extends NbTestCase {

    public TestUninstallerAll() {
        super("Uninstaller test");
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(TestUninstallerAll.class);

        return suite;
    }

    public void testUninstaller() {
        TestData data = new TestData(Logger.getLogger("global"));

        try {
            data.setWorkDir(new File(System.getProperty("xtest.tmpdir")));
        } catch (IOException ex) {
            NbTestCase.fail("Can not get WorkDir");
        }

        //data.setInstallerType(installerType);

        System.setProperty("nbi.dont.use.system.exit", "true");
        System.setProperty("nbi.utils.log.to.console", "false");
        System.setProperty("servicetag.allow.register", "false");
        System.setProperty("user.home", data.getWorkDirCanonicalPath());


        Utils.phaseFive(data);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
