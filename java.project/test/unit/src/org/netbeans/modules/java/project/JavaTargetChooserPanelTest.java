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

package org.netbeans.modules.java.project;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jiri Rechtacek
 */
public class JavaTargetChooserPanelTest extends NbTestCase {
    FileObject root = null;

    public JavaTargetChooserPanelTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        super.setUp ();
        clearWorkDir ();
    }

    // tests #51135: checked existence of multiple dotted package name
    public void testCanUseFileName () throws Exception {
        File rootFile = getWorkDir ();
        assertNotNull ("WorkDir exists.", rootFile);
        root = FileUtil.toFileObject (rootFile);
        if (!root.canWrite ()) {
            fail ("Cannot create test folder.");
        }

        root = root.createFolder ("testCanUseFileName");

        assertNotNull (root + " exists.", FileUtil.toFile (root));
        assertTrue ("Package aaa.bbb.ccc can be created.", JavaTargetChooserPanel.canUseFileName (root, "", "aaa.bbb.ccc", "") == null);

        assertNotNull ("Package aaa.bbb.ccc was created.", root.createFolder ("aaa").createFolder ("bbb").createFolder ("ccc"));
        assertTrue ("Package aaa cannot be created.", JavaTargetChooserPanel.canUseFileName (root, "", "aaa", "") != null);
        assertTrue ("Package aaa.bbb cannot be created.", JavaTargetChooserPanel.canUseFileName (root, "", "aaa.bbb", "") != null);
        assertTrue ("Package aaa.bbb.ccc cannot be created.", JavaTargetChooserPanel.canUseFileName (root, "", "aaa.bbb.ccc", "") != null);
        assertTrue ("Package ddd can be created.", JavaTargetChooserPanel.canUseFileName (root, "", "ddd", "") == null);
    }
    
}
