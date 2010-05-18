/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
/*
 * CodeClipDragAndDropHandler.java
 *
 * Created on August 2, 2006, 11:52 AM
 *
 * The drag and drop handler for palettes with codeclips.
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 */

package org.netbeans.modules.visualweb.palette.api;

import org.netbeans.modules.visualweb.palette.codeclips.CodeClipUtilities;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.MissingResourceException;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;



public class CodeClipDragAndDropHandler extends DragAndDropHandler{

    /** Creates a new instance of CodeClipDragAndDropHandler */
    public CodeClipDragAndDropHandler() {
        // #147511 To be able DnD text from source into palette.
        super(true);
    }

    /**
     * Add your own custom DataFlavor as need to suppor drag-over a different
     * parts of editor area.
     *
     * @param t Item's default Transferable.
     * @param item Palette item's Lookup.
     *
     */
    public void customize(ExTransferable exTransferable, Lookup lookup) {
        //This is only called when dragging items off palette.
    }

    /**
     * @param targetCategory Lookup of the category under the drop cursor.
     * @param flavors Supported DataFlavors.
     * @param dndAction Drop action type.
     *
     * @return True if the given category can accept the item being dragged.
     */
    public boolean canDrop( Lookup targetCategory, DataFlavor[] flavors, int dndAction ) {
        for( int i=0; i<flavors.length; i++ ) {
            if( PaletteController.ITEM_DATA_FLAVOR.equals( flavors[i] ) ) {
                return true;
            } else if (java.awt.datatransfer.DataFlavor.stringFlavor.equals(flavors[i])){
                return true;
            }
        }
        return false;
    }

//    /**
//     * Perform the drop operation and add the dragged item into the given category.
//     *
//     * @param targetCategory Lookup of the category that accepts the drop.
//     * @param item Transferable holding the item being dragged.
//     * @param dndAction Drag'n'drop action type.
//     * @param dropIndex Zero-based position where the dragged item should be dropped.
//     *
//     * @return True if the drop has been successful, false otherwise.
//     */
//    public boolean doDrop( Lookup targetCategory, Transferable item, int dndAction, int dropIndex ) {
//        String body;
//
//        if( item.isDataFlavorSupported( PaletteController.ITEM_DATA_FLAVOR ) ) {
//            return super.doDrop( targetCategory, item, dndAction, dropIndex );
//        }
//
//        try {
//            body = (String)item.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            return false;
//        } catch (UnsupportedFlavorException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            return false;
//        }
//
//        DataFolder categoryDatafolder = (DataFolder)targetCategory.lookup(DataFolder.class);
//        FileObject folderObject  = ((DataObject)categoryDatafolder).getPrimaryFile();
//        String localizingBundle = "org.netbeans.modules.visualweb.palette.codeclips.Bundle";
//
//        try{
////            String displayNameString = "CLIP";
//            String displayNameString = NbBundle.getMessage(CodeClipDragAndDropHandler.class, "CLIP");
//            //This causes a problem if it tries to find "Clip" during the drop.
//            CodeClipUtilities.createCodeClipFile(folderObject, body, displayNameString, localizingBundle, null);
//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ex);
//        } catch (MissingResourceException mre ) {
//            ErrorManager.getDefault().notify(mre);
//        }
//
//        return true;
//    }
    
    
    
}
