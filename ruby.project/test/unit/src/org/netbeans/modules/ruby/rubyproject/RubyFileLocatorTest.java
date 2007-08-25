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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class RubyFileLocatorTest extends RubyProjectTestBase {

    public RubyFileLocatorTest(String testName) {
        super(testName);
    }

    public void testRelativeToProjectDir() throws Exception { // # 112254
        RubyProject project = createTestProject("RubyProject1");
        assertNotNull(project);
        FileObject dirFO = project.getProjectDirectory();
        assertNotNull(dirFO);
        File dir = new File(FileUtil.toFile(dirFO), "./test/unit/http_phone/");
        dir.mkdirs();
        File testFile = new File(dir, "asterisk_cmd_test.rb");
        testFile.createNewFile();
        RubyFileLocator rfl = new RubyFileLocator(null, project);
        assertNotNull(rfl.find("./test/unit/http_phone/asterisk_cmd_test.rb"));
    }
}