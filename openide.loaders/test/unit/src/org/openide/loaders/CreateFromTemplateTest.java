/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/** Checks the ability to create data object from template.
 * (only for investing bug #38421, could be removed if needed)
 * @author Jiri Rechtacek
 */
public class CreateFromTemplateTest extends NbTestCase {
    
    public CreateFromTemplateTest (String name) {
        super(name);
    }
    
    public void testCreateExecutorFromTemplate () throws Exception {
        String folderName = "/Templates/Services/Executor";
        FileObject data = org.openide.filesystems.FileUtil.createData (
            Repository.getDefault ().getDefaultFileSystem ().getRoot (), 
            folderName + "/" + "X.xml"
        );
        data.setAttribute ("template", Boolean.TRUE);
        FileObject fo = data.getParent ();
        assertNotNull ("FileObject " + folderName + " found on DefaultFileSystem.", fo);
        DataFolder f = DataFolder.findFolder (fo);
        assertNotNull ("Folder " + folderName + " found on DefaultFileSystem.", f);
        DataObject[] executors = f.getChildren ();
        assertTrue ("Templates for Executor found.", executors.length > 0);
        DataObject executor = executors[0];
//        System.out.println("do Executors before:");
//        for (int i = 0; i < executors.length; i++) {
//            System.out.println(">>> " + i + " -- " + executors[i].getName ());
//        }
//        System.out.println("done.");
        assertNotNull ("Executor found.", executor);
        String newExecutorName = "NewExecutor" + Double.toString (Math.random ());
        executor.createFromTemplate (f, newExecutorName);
        executors = f.getChildren ();
        boolean found = false;
//        System.out.println("do Executors after:");
        for (int i = 0; i < executors.length && !found; i++) {
//            System.out.println(">>> " + i + " -- " + executors[i].getName ());
            found = newExecutorName.equals (executors[i].getName ());
        }
//        System.out.println("done.");
        assertTrue (newExecutorName + " was created on right place.", found);
    }
    
}


