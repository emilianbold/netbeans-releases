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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.io.File;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author  tom
 */
public class PanelSourceFoldersTest extends NbTestCase {

    public PanelSourceFoldersTest (java.lang.String testName) {
        super(testName);
    }

    public void testCheckValidity () throws Exception {

        File root = getWorkDir();
        File projectDir = new File (root, "project");
        File test = new File (root,  "tests");
        test.mkdir();
        File src = new File (root, "src");
        src.mkdir();
        File badSrcDir = new File (root, "badSrc");
        File badSrcDir2 = new File (test, "src");
        badSrcDir2.mkdir();
        File badProjectDir = new File (root, "badPrjDir");
        badProjectDir.mkdir();
        badProjectDir.setReadOnly();
        
        assertNotNull("Empty name", PanelProjectLocationExtSrc.checkValidity ("",projectDir.getAbsolutePath()));
        assertNotNull("Read Only WorkDir", PanelProjectLocationExtSrc.checkValidity ("",badProjectDir.getAbsolutePath()));
        assertNotNull("Non Existent Sources", PanelSourceFolders.checkValidity (projectDir, new File[] {badSrcDir} , new File[] {test}));
        assertFalse("Sources == Tests",  FolderList.isValidRoot (src, new File[] {src},projectDir));
        assertFalse("Tests under Sources", FolderList.isValidRoot (new File (src, "Tests"),new File[] {src},projectDir));
        assertFalse("Sources under Tests", FolderList.isValidRoot (badSrcDir2, new File[] {test},projectDir));
        assertNull ("Valid data", PanelSourceFolders.checkValidity (projectDir, new File[]{src}, new File[]{test}));
    }
}