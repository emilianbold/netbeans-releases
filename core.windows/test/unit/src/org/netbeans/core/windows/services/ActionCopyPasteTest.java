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


package org.netbeans.core.windows.services;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;




/**
 *
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class ActionCopyPasteTest extends NbTestCase {
    
    ToolbarFolderNode toolbar1;
    ToolbarFolderNode toolbar2;
    MenuFolderNode menu1;
    MenuFolderNode menu2;
    DataObject actionToPaste;
    
    public ActionCopyPasteTest (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (ActionCopyPasteTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        toolbar1 = new ToolbarFolderNode( createFolder( "Toolbars", "tb1" ) );
        toolbar2 = new ToolbarFolderNode( createFolder( "Toolbars", "tb2" ) );

        menu1 = new MenuFolderNode( createFolder( "Menu", "menu1" ) );
        menu2 = new MenuFolderNode( createFolder( "Menu", "menu2" ) );
        
        createChildren( toolbar1.getDataObject().getPrimaryFile(), new Class[] { ActionA1.class, ActionA2.class } );
        createChildren( menu1.getDataObject().getPrimaryFile(), new Class[] { ActionA1.class, ActionA2.class } );

        createChildren( toolbar2.getDataObject().getPrimaryFile(), new Class[] { ActionB1.class, ActionB2.class } );
        createChildren( menu2.getDataObject().getPrimaryFile(), new Class[] { ActionB1.class, ActionB2.class } );
        
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    public void testDoNotPasteDuplicitActions() throws Exception {
        //check copy & paste for toolbar folders
        DataObject[] folderChildren = ((DataFolder)toolbar1.getDataObject()).getChildren();
        DataObject child1 = folderChildren[0];
        Transferable t = child1.getNodeDelegate().clipboardCopy();
        
        ArrayList list = new ArrayList();
        toolbar1.createPasteTypes( t, list );
        assertEquals( "Cannot paste an action if the toolbar already contains it.", 0, list.size() );
        
        list.clear();
        toolbar2.createPasteTypes( t, list );
        assertTrue( "Pasting to a different folder is ok.", list.size() > 0 );
        
        list.clear();
        menu1.createPasteTypes( t, list );
        assertEquals( "Cannot paste an action if the menu already contains it.", 0, list.size() );
        
        list.clear();
        menu2.createPasteTypes( t, list );
        assertTrue( "Pasting to a different menu is ok.", list.size() > 0 );

        //check copy & paste for menu folders
        folderChildren = ((DataFolder)menu1.getDataObject()).getChildren();
        child1 = folderChildren[0];
        t = child1.getNodeDelegate().clipboardCopy();

        list.clear();
        toolbar1.createPasteTypes( t, list );
        assertEquals( "Cannot paste an action if the toolbar already contains it.", 0, list.size() );
        
        list.clear();
        toolbar2.createPasteTypes( t, list );
        assertTrue( "Pasting to a different folder is ok.", list.size() > 0 );
        
        list.clear();
        menu1.createPasteTypes( t, list );
        assertEquals( "Cannot paste an action if the menu already contains it.", 0, list.size() );
        
        list.clear();
        menu2.createPasteTypes( t, list );
        assertTrue( "Pasting to a different menu is ok.", list.size() > 0 );
    }
    
    DataFolder createFolder( String parent, String folderName ) throws Exception {
        FileObject folderObj = Repository.getDefault().getDefaultFileSystem().findResource( parent+"/"+folderName );
        if( null != folderObj )
            folderObj.delete();

        FileObject parentFolder = Repository.getDefault().getDefaultFileSystem().findResource( parent );
        assertNotNull( parentFolder );
        parentFolder.createFolder( folderName );
        
        DataFolder res = DataFolder.findFolder( Repository.getDefault().getDefaultFileSystem().findResource( parent+"/"+folderName ) );
        assertNotNull( res );
        return res;
    }
    
    void createChildren( FileObject folder, Class[] actions ) throws Exception {
        for( int i=0; i<actions.length; i++ ) {
            folder.createData( actions[i].getName()+".instance" );
        }
    }
    
    public static class ActionA1 extends AbstractAction {
        public ActionA1() {
            super( "actiona1" );
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }
    }
    
    public static class ActionA2 extends AbstractAction {
        public ActionA2() {
            super( "actiona2" );
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }
    }

    public static class ActionB1 extends AbstractAction {
        public ActionB1() {
            super( "actionb1" );
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }
    }
    
    public static class ActionB2 extends AbstractAction {
        public ActionB2() {
            super( "actionb2" );
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }
    }
}
