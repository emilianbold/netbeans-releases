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
package org.openide.text;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;

/**
 * ActiveEditorDrop with artificial DataFlavor. Drag and drop initiator sometimes needs
 * to be notified about a target component, where the drop operation was performed. 
 * Initiator should implement this interface and use the required artificial DataFlavor.
 * Component that will support drop operation of the ActiveEditorDrop should call handleTransfer
 * method.
 * <br>
 * Sample usage of the client: <br>
 *   <pre>
 *   private class MyDrop extends StringSelection implements ActiveEditorDrop {
 *       
 *       public MyDrop(String text){
 *           super(text); //NOI18N
 *       }
 *       
 *       public boolean isDataFlavorSupported(DataFlavor f) {
 *           return super.isDataFlavorSupported(f) || ActiveEditorDrop.FLAVOR == f;
 *       }
 *       
 *       public final DataFlavor[] getTransferDataFlavors() {
 *           DataFlavor delegatorFlavor[] = super.getTransferDataFlavors();
 *           int delegatorFlavorLength = delegatorFlavor.length;
 *           DataFlavor newArray[] = new DataFlavor[delegatorFlavorLength + 1];
 *           System.arraycopy(delegatorFlavor, 0, newArray, 0, delegatorFlavorLength);
 *           newArray[delegatorFlavorLength] = ActiveEditorDrop.FLAVOR;
 *           return newArray;
 *       }
 *       
 *       public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
 *           if (flavor == ActiveEditorDrop.FLAVOR) {
 *               return this;
 *           }
 *           return super.getTransferData(flavor);
 *       }
 *       
 *       public boolean handleTransfer(java.awt.Component targetComponent) {
 *          // your implementation
 *       }
 *   }
 *   </pre>
 *
 * @author Martin Roskanin
 * @since org.openide.text 6.5 
 */
public interface ActiveEditorDrop {
    
    /**
     * Active editor DataFlavor used for communication between DragSource and DragTarget.
     * This DataFlavor should be used for case where target component is instance
     * of JTextComponent.
     */
    static final DataFlavor FLAVOR = 
            new DataFlavor("text/active_editor_flavor", "Active Editor Flavor"); //NOI18N

    /**
     * A method called from the drop target that supports the artificial DataFlavor.
     * @param targetComponent a Component where drop operation occured.
     * @return true if implementor allowed a drop operation into the targetComponent
     */
    abstract boolean handleTransfer(Component targetComponent);
    
}
