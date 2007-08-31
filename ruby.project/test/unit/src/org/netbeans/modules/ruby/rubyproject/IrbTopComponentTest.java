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

package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import javax.swing.JTextArea;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.netbeans.junit.NbTestCase;

public final class IrbTopComponentTest extends NbTestCase {

    public IrbTopComponentTest(String testName) {
        super(testName);
    }

    // sanity check to prevent regressions like issue #114387
    public void testBasics() throws Exception {
        final Ruby runtime = IrbTopComponent.getRuntime(new JTextArea());
        File jrubyHome = getXTestJRubyHome();
        File lib = new File(jrubyHome, "lib/ruby/1.8");
        RubyArray loadPath = (RubyArray) runtime.getLoadService().getLoadPath();
        loadPath.add(lib.getAbsolutePath());
        runtime.evalScript("require 'irb'; require 'irb/completion'");
    }

    private static File getXTestJRubyHome() {
        String destDir = System.getProperty("xtest.jruby.home");
        assertNotNull("xtest.jruby.home property has to be set when running within binary distribution", destDir);
        return new File(destDir);
    }
}