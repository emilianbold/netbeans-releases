/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.windows.services;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;

/**
 * @author Jaroslav Tulach, Jiri Rechtacek
 */
public class ActionCopyPasteTest extends NbTestCase {

    ToolbarFolderNode toolbar1;
    ToolbarFolderNode toolbar2;
    MenuFolderNode menu1;
    MenuFolderNode menu2;
    DataObject actionToPaste;
    
    public ActionCopyPasteTest(String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        // This magic call will load modules and fill content of default file system
        // where xml layers live - uaah sometimes I think I just live in another world
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        toolbar1 = new ToolbarFolderNode( createFolder( "Toolbars", "tb1" ) );
        toolbar2 = new ToolbarFolderNode( createFolder( "Toolbars", "tb2" ) );

        menu1 = new MenuFolderNode( createFolder( "Menu", "menu1" ) );
        menu2 = new MenuFolderNode( createFolder( "Menu", "menu2" ) );
        
        createChildren( toolbar1.getDataObject().getPrimaryFile(), new Class[] { ActionA1.class, ActionA2.class } );
        createChildren( menu1.getDataObject().getPrimaryFile(), new Class[] { ActionA1.class, ActionA2.class } );

        createChildren( toolbar2.getDataObject().getPrimaryFile(), new Class[] { ActionB1.class, ActionB2.class } );
        createChildren( menu2.getDataObject().getPrimaryFile(), new Class[] { ActionB1.class, ActionB2.class } );
        
    }

    protected boolean runInEQ () {
        return true;
    }
    
    public void testDoNotPasteDuplicateActions() throws Exception {
        //check copy & paste for toolbar folders
        DataObject[] folderChildren = ((DataFolder)toolbar1.getDataObject()).getChildren();
        DataObject child1 = folderChildren[0];
        Transferable t = child1.getNodeDelegate().clipboardCopy();
        PasteType[] types;
        
        types = toolbar1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the toolbar already contains it.", 0, types.length );
        
        types = toolbar2.getPasteTypes( t );
        assertTrue( "Pasting to a different folder is ok.", types.length > 0 );
        
        types = menu1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the menu already contains it.", 0, types.length );
        
        types = menu2.getPasteTypes( t );
        assertTrue( "Pasting to a different menu is ok.", types.length > 0 );

        //check copy & paste for menu folders
        folderChildren = ((DataFolder)menu1.getDataObject()).getChildren();
        child1 = folderChildren[0];
        t = child1.getNodeDelegate().clipboardCopy();

        types = toolbar1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the toolbar already contains it.", 0, types.length );
        
        types = toolbar2.getPasteTypes( t );
        assertTrue( "Pasting to a different folder is ok.", types.length > 0 );
        
        types = menu1.getPasteTypes( t );
        assertEquals( "Cannot paste an action if the menu already contains it.", 0, types.length );
        
        types = menu2.getPasteTypes( t );
        assertTrue( "Pasting to a different menu is ok.", types.length > 0 );
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
        
        public void actionPerformed(ActionEvent e) {}
    }
    
    public static class ActionA2 extends AbstractAction {
        public ActionA2() {
            super( "actiona2" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }

    public static class ActionB1 extends AbstractAction {
        public ActionB1() {
            super( "actionb1" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }
    
    public static class ActionB2 extends AbstractAction {
        public ActionB2() {
            super( "actionb2" );
        }
        
        public void actionPerformed(ActionEvent e) {}
    }
}
