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

package org.netbeans.modules.java.freeform.ui;

import java.io.File;
import junit.framework.TestCase;

public class SourceFoldersPanelTest extends TestCase {
    
    public SourceFoldersPanelTest(String name) {
        super(name);
    }
    
    public void testGetDefaultLabel() throws Exception {
        char sep = File.separatorChar;
        assertEquals("foo", SourceFoldersPanel.getDefaultLabel("foo", false));
        assertEquals("foo", SourceFoldersPanel.getDefaultLabel("foo", true));
        assertEquals("foo" + sep + "bar", SourceFoldersPanel.getDefaultLabel("foo/bar", false));
        assertEquals("foo", SourceFoldersPanel.getDefaultLabel("${project.dir}/foo", false));
        assertEquals(sep + "else" + sep + "where", SourceFoldersPanel.getDefaultLabel("/else/where", false));
        // #54428:
        assertEquals("Source Packages", SourceFoldersPanel.getDefaultLabel(".", false));
        assertEquals("Test Packages", SourceFoldersPanel.getDefaultLabel(".", true));
        assertEquals("Source Packages", SourceFoldersPanel.getDefaultLabel("${project.dir}", false));
        assertEquals("Test Packages", SourceFoldersPanel.getDefaultLabel("${project.dir}", true));
    }
    
}
