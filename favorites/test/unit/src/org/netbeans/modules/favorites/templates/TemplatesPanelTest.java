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
package org.netbeans.modules.favorites.templates;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * Tests creating/renaming/removing templates via TemplatesPanel.
 *
 * @author Jiri Rechtacek
 */
public class TemplatesPanelTest extends NbTestCase {
    File popural;
    FileObject templateFolder;
    DataFolder f;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public TemplatesPanelTest(String s) {
        super(s);
    }
    
    protected void setUp () {
        try {
            templateFolder = Repository.getDefault ().getDefaultFileSystem ().getRoot ().createFolder ("TestTemplates");
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
        assertNotNull ("TestTemplates folder exists on SFS", templateFolder);
        try {
            popural = getWorkDir ().createTempFile ("popural", "java");
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
        assertTrue ("popural.tmp exists", popural.exists ());
        
        f = DataFolder.findFolder (templateFolder);
        
        assertNotNull ("DataFolder found for FO " + templateFolder, f);
        
    }
    
    protected void tearDown() {
        try {
            FileLock l = templateFolder.lock ();
            templateFolder.delete (l);
            l.releaseLock ();
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
    }
    
    public void testNewTemplateFromFile () throws Exception {
        DataObject dobj = TemplatesPanel.createTemplateFromFile (popural, f);
        assertNotNull ("New DataObject found.", dobj);
        assertTrue ("Is template.", dobj.isTemplate ());
        assertEquals ("Template is in the preffered folder.", f, dobj.getFolder ());
    }
    
    public void testTwiceNewFromTemplate () throws Exception {
        testNewTemplateFromFile ();
        testNewTemplateFromFile ();
    }
    
    public void testDuplicateTemplate () {
        DataObject dobj = TemplatesPanel.createTemplateFromFile (popural, f);
        DataObject dupl = TemplatesPanel.createDuplicateFromNode (dobj.getNodeDelegate ());
        assertNotNull ("Duplicate DataObject found.", dobj);
        assertTrue ("Duplicate is template.", dobj.isTemplate ());
        assertEquals ("Template is in same folder as original.", dobj.getFolder (), dupl.getFolder ());
        assertTrue ("Name is derived from original.", dupl.getNodeDelegate ().getName ().startsWith (dobj.getNodeDelegate ().getName ()));
    }
    public void testIgnoresSimplefolders() throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        FileObject fo = FileUtil.createFolder(root, "Templates/SimpleFolder");
        fo.setAttribute("simple", Boolean.FALSE);
        Node n = TemplatesPanel.getTemplateRootNode();
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Empty: " + Arrays.asList(arr), 0, arr.length);
    }
}
