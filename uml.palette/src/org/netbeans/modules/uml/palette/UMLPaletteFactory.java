/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.palette;
import org.netbeans.modules.uml.palette.actions.UMLPaletteActions;
import org.netbeans.modules.uml.palette.ui.ModelingPalette;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;


/**
 *
 * @author Thuy
 */

public final class UMLPaletteFactory {

    //private static String paletteFolder = null;
    private static Node paletteRootNode = null;
    private static PaletteController palette = null;
    
    
    public static PaletteController getPalette(Node rootNode) {
        
        if (palette == null || (rootNode != null && !rootNode.equals(paletteRootNode)) ) {
            paletteRootNode = rootNode;
            palette = PaletteFactory.createPalette(paletteRootNode, new UMLPaletteActions());
        }
        return palette;
    }
    
    public static PaletteController getPalette(String folderName) {
        //if (palette == null || (folderName != null && !folderName.equals(paletteFolder)) ) {
        String paletteFolder = folderName;
        
        //get the file object from folder
        FileObject fileObject = Repository.getDefault().getDefaultFileSystem().findResource(paletteFolder);
        
        // get DataFolder of fileObject
        DataFolder dFolder = DataFolder.findFolder(fileObject);
        
        //create a hierarchy of Nodes (root - categories - items)
        Node pRootnode = new ModelingPalette(dFolder.getNodeDelegate());
        
        // create common palette with the root node and customized actions for palette contextual menu
        palette = PaletteFactory.createPalette(pRootnode, new UMLPaletteActions());
        //}
        
        return palette;
    }
    
    public static PaletteController getPaletteController() {
        return palette;
    }
}

