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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.visualweb.insync.live.DesignBeanNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;

/**
 * <p>This deals with handling of refactoring actions on the VW JSP files.</p>
 */
public class FacesRefactoringActionsProvider extends ActionsImplementationProvider {

    /**
     * <p>This returns true for VW JSP files indicating that Refactor:Rename 
     * operation is supported.</p>
     */
    @Override
    public boolean canRename(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        // Get the Node being renamed
        Node node = nodes.iterator().next();
        if (node != null) {
            // Get the DataObject
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                // Get the primary FileObject
                FileObject fileObject = dataObject.getPrimaryFile();
                // Is this a VW Page?
                if (FacesRefactoringUtils.isVisualWebJspFile(fileObject)) {
                    // Yes. It can be Renamed 
                    return true;
                }
                // Check for non special folders under web folder
            }
        }
        return false;
    }
    
    /**
     * <p>This implements the invocation of Rename refactoring for VW JSP files.</p>
     */
    @Override
    public void doRename(final Lookup lookup) {
        // First check can rename
        if (canRename(lookup)) {
        	Runnable task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] fileObjects) {
                    String newName = getNewName(lookup.lookup(Dictionary.class));
                    return new FacesRenameRefactoringUI(fileObjects[0], newName);
                }                
        	};
        	FacesRefactoringUtils.invokeAfterScanFinished(task, (String) RefactoringActionsFactory.renameAction().getValue(Action.NAME));
        }
    }
    
    /**
     * <p>This returns true for VW JSP files indicating that Refactor:Move 
     * operation is supported.</p>
     */
    @Override
    public boolean canMove(Lookup lookup) {
        // Get the Node being renamed
        Node node = lookup.lookup(Node.class);

        if (node != null &&
                // The DesignBeanNodes in outline also have the JsfJspDataObject
                // in the lookup. However drag and drop of those shouls not
                // refactoring
                !(node instanceof DesignBeanNode)) {
            // Get the DataObject
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                // Get the primary FileObject
                FileObject fileObject = dataObject.getPrimaryFile();
                // Is this a VW Page?
                if (FacesRefactoringUtils.isVisualWebJspFile(fileObject)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * <p>This implements the invocation of Move refactoring for VW JSP files.</p>
     */
    @Override
    public void doMove(Lookup lookup) {
        // First check can move
        if (canMove(lookup)) {
            final Dictionary dictionary = lookup.lookup(Dictionary.class);
            Runnable task = new NodeToFileObjectTask(lookup.lookupAll(Node.class)) {
                @Override
                protected RefactoringUI createRefactoringUI(FileObject[] fileObjects) {
                    // are other parameters specified e.g. due to drag and drop or copy paste
                    PasteType pasteType = getPaste(dictionary);
                    FileObject targetFolder=getTarget(dictionary);
                    if (fileObjects.length == 1) {
                        return new FacesMoveRefactoringUI(fileObjects[0], targetFolder, pasteType);
                    } else {
                        // TODO handle multiple
                        throw new UnsupportedOperationException("Cannot move multiple files/folders yet!"); // NOI18N TODO 
                    }
                }
                
            };
            FacesRefactoringUtils.invokeAfterScanFinished(task, (String) RefactoringActionsFactory.moveAction().getValue(Action.NAME));
        }
    }
    
    private FileObject getTarget(Dictionary dict) {
        if (dict==null)
            return null;
        Node n = (Node) dict.get("target"); //NOI18N
        if (n==null)
            return null;
        DataObject dob = n.getCookie(DataObject.class);
        if (dob!=null)
            return dob.getPrimaryFile();
        return null;
    }
    
    private PasteType getPaste(Dictionary dict) {
        if (dict==null) 
            return null;
        Transferable orig = (Transferable) dict.get("transferable"); // NOI18N
        if (orig==null)
            return null;
        Node n = (Node) dict.get("target"); // NOI18N
        if (n==null)
            return null;
        PasteType[] pt = n.getPasteTypes(orig); // NOI18N
        if (pt.length==1) {
            return null;
        }
        return pt[1];
    }
    
    private static String getNewName(Dictionary dict) {
        if (dict==null) 
            return null;
        return (String) dict.get("name"); //NOI18N
    }
    
    public static abstract class NodeToFileObjectTask implements Runnable {
        private Collection<? extends Node> nodes;
        
        public NodeToFileObjectTask(Collection<? extends Node> nodes) {
            this.nodes = nodes;
        }
        
        public void run() {
            List<FileObject> fileObjects = new ArrayList<FileObject>(nodes.size());
            for (Node node:nodes) {
                DataObject dataObject = node.getLookup().lookup(DataObject.class);
                if (dataObject!=null) {
                    FileObject primaryFileObject = dataObject.getPrimaryFile();
                    if (primaryFileObject != null) {
                        fileObjects.add(primaryFileObject);
                    }
                }
            }
            UI.openRefactoringUI(createRefactoringUI(fileObjects.toArray(new FileObject[fileObjects.size()])));
        }

        protected abstract RefactoringUI createRefactoringUI(FileObject[] selectedElement);
    }    

}
