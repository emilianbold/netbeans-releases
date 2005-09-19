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
package org.netbeans.modules.favorites.templates;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

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
}
