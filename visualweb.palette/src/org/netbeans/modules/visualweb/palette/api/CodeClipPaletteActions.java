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
/*
 * CodeClipPaletteActions.java
 *
 * Created on August 8, 2006, 5:35 PM
 *
 * Adds additional actions to the Netbeans Palette Actions.
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @date 08/20/2006
 */

package org.netbeans.modules.visualweb.palette.api;

import org.netbeans.modules.visualweb.palette.codeclips.CodeClipItemNode;
import org.netbeans.modules.visualweb.palette.codeclips.CodeClipUtilities;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.actions.NewAction;
import org.openide.actions.RenameAction;
import org.openide.loaders.DataFolder;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;


public class CodeClipPaletteActions extends PaletteActions {
    private String paletteFolderName;
    private CloneableEditor cloneableEditor;


    /*
     * Creates a new instance of FormPaletteProvider
     * @param paletteFolderName string that contains the palette folder name
     */
    public CodeClipPaletteActions(String paletteFolderName, CloneableEditor cloneableEditor) {
        this.paletteFolderName = paletteFolderName;
        this.cloneableEditor = cloneableEditor;
    }

    public Action[] getImportActions() {
        return new Action[] {
            //new ResetPaletteAction(paletteFolderName)
        };
    }

    public Action[] getCustomCategoryActions(Lookup category) {
        return new Action[] {
            new CreateCodeClipAction(category)
//            I can not use the NewAction because I would need to overwrite getTypes in the Category class written by Netbeans.
//             ((NewAction)SystemAction.get(NewAction.class))
        };
    }

    public Action[] getCustomItemActions(Lookup item) {
        return new Action[] {
            SystemAction.get(RenameAction.class).createContextAwareInstance(item),
            new EditCodeClipAction(item)
//            ((EditAction)SystemAction.get(EditAction.class)).createContextAwareInstance(item)
        };
    }

    public Action[] getCustomPaletteActions() {
        return new Action[] {
            SystemAction.get(NewAction.class)
        };
    }

    /*
     * Called when user double clicks.  Double click a palette item should
     * insert a new item.
     *
     * @param item The lookup for the item clicked.
     * @return action The action to be called when an item is double clicked.  In this case, insert action.
     */
    public Action getPreferredAction( Lookup item ) {
        return new CodeClipPaletteInsertAction(item, cloneableEditor);
    }


    /** No longer necessary since the Palette API now provides this in the manager.
     * Reset Palette
    private static class ResetPaletteAction extends AbstractAction   {
        String folderName;

        ResetPaletteAction(String folderName) {
            super( NbBundle.getMessage(CodeClipPaletteActions.class, "RESET"));
            this.folderName = folderName;
        }

        public void actionPerformed(ActionEvent e) {
            String msg;
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            FileObject paletteFileObject = fs.findResource( folderName );
            FileObject paletteParent = paletteFileObject.getParent();

            String nbUserDir = System.getProperty("netbeans.user");
            String separator = new File(nbUserDir).separator;
            File userPaletteFolder = new File(nbUserDir + separator + "config" + separator + paletteFileObject.getPath());
            
            if ( !userPaletteFolder.isDirectory() ){
                msg = NbBundle.getMessage(CodeClipPaletteActions.class, "MSG_NoUserModifications");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            
           msg = NbBundle.getMessage(CodeClipPaletteActions.class, "MSG_OkayToDelete");
//           Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.OK_CANCEL_OPTION));
           
            NotifyDescriptor d =new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.OK_CANCEL_OPTION);           
           
           if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION ){
               return;
           }
            
            boolean success = deleteDir(userPaletteFolder);
            if( !success ) {
                msg = NbBundle.getMessage(CodeClipPaletteActions.class, "MSG_ErrorUnableToDeleteUserPaletteFolder");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            //It is insufficient to refresh paletteFileObject since itself has been removed from the user dir.
            paletteParent.refresh();

        }
    }
     */
    
    
    
    
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    
    
    
    
    
    /**
     * Create a code clip
     */
    private static class CreateCodeClipAction extends AbstractAction {
        Lookup category;
        CreateCodeClipAction(Lookup category){
            super( NbBundle.getMessage(CodeClipPaletteActions.class, "ADD"));
            this.category = category;
        }
        public void actionPerformed(ActionEvent e) {
            DataFolder dataFolder = (DataFolder)category.lookup(DataFolder.class);
            CodeClipUtilities.createCodeClip(dataFolder);
        }
        
    }
    
    private static class EditCodeClipAction extends AbstractAction {
        Lookup item;
        
        EditCodeClipAction(Lookup item){
            super( NbBundle.getMessage(CodeClipPaletteActions.class, "EDIT"));
            this.item = item;
        }
        public void actionPerformed(ActionEvent e) {
            CodeClipItemNode  ccNode = (CodeClipItemNode) item.lookup(CodeClipItemNode.class);
            ccNode.edit();
            return;
            
        }
    }
    
    
    /**
     * Inserts a palette item
     */
    private static class CodeClipPaletteInsertAction extends AbstractAction {
        
        Lookup item;
        CloneableEditor cloneableEditor;
        
        CodeClipPaletteInsertAction(Lookup item, CloneableEditor cloneableEditor) {
            this.item = item;
            this.cloneableEditor = cloneableEditor;
        }
        
        public void actionPerformed(ActionEvent e) {
            CodeClipItemNode ccNode = (CodeClipItemNode) item.lookup(CodeClipItemNode.class);
            ccNode.drop((JTextComponent)cloneableEditor.getEditorPane());

            
        }
    }
    
}
