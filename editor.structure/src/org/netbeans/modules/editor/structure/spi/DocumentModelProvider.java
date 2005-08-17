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

package org.netbeans.modules.editor.structure.spi;

import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentChange;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelModificationTransaction;
import org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelTransactionCancelledException;
import org.netbeans.modules.editor.structure.api.DocumentModelException;

/**
 * The DocumentModelProvider is responsible for parsing the text document
 * and updating the tree structure of elements held by DocumentModel. 
 * The provider accomplished this by adding a 'requests' to add or remove 
 * an element into the transaction. For more detailed information see 
 * {@link org.netbeans.modules.editor.structure.api.DocumentModel.DocumentModelModificationTransaction}
 * <br>
 * When the model decides that there is a need to update the structure of the
 * elements (after there is a document change(s)) it calls the provider's 
 * update method. The model passes an instance of DocumentModelModificationTransaction,
 * the model itself and an array of changes made in the text document.
 * Then it's up to the provider implementation what parts of the document will
 * be reparsed and how the elements will be tailored. In principal the
 * provider can add or remove elements from the model. This is accomplished by
 * adding change requests into the transaction. Once the provider finishes its
 * work the model commits the transaction and the model structure is definitively
 * updated. 
 * <br>
 * When the text document is changed during the call of the update method, the
 * model cancels the transaction so next attempt to add something into the transaction
 * will cause DocumentModelTransactionCancelledException to be thrown. 
 * The provider should not catch this exception - it is a legal mechanism, how
 * to break no more valid model update.
 *
 * Instancies of this class are registered via layer mechanism into the 
 * IDE and associated with a particullar mime-types.
 *
 * The registration are read from the following folder in the system FS:
 * <pre>
 *     Editors/&lt;mime-type&gt;/DocumentModel
 * </pre>
 *
 * @author Marek Fukala
 * @version 1.0
 * @see org.netbeans.modules.editor.structure.api.DocumentModel
 *
 */
public interface DocumentModelProvider {
    
    /** Method called by the DocumentModel when the model content needs to be updated.
     * For more info see the overall description of this class.
     * @param trans the transaction the provider puts the change requests into
     * @param model the DocumentModel instance
     * @param changes the array of changes made in the document from the last model update
     *
     * @throws DocumentModelTransactionCancelledException when the transaction passed
     * into the provider has been cancelled.
     *
     */
    public void updateModel(DocumentModelModificationTransaction trans, 
            DocumentModel model, DocumentChange[] changes) throws DocumentModelException, DocumentModelTransactionCancelledException;
    
}
