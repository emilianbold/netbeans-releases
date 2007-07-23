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

package org.netbeans.modules.editor.structure.api;

/**
 * Implementations of this interface may register itself into the DocumentModel 
 * and then listen to changes of the model state.
 * 
 *<br>
 * Allows to listen on following changes of the model state:
 * <ul>
 * <li>The underlaying document has changed.
 * <li>DocumentModel started to scan the underlying document for changes. 
 * (The old model data are available until next step is reached.)
 * <li>The document model update started. 
 * Model is locked for reading since this event. 
 * <li>The document model update finished. 
 * New model data are accessible now.
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 * @since 1.14
 *
 * @see DocumentModel
 * @see DocumentModelListener
 * @see DocumentElement
 * @see DocumentElementListener
 * 
 */
public interface DocumentModelStateListener {

     /** Called when the underlying javax.swing.Document has changed. */
    public void sourceChanged();
    
    /** Indicates the model started to scan the underlying document for changes 
     * happened since last scan and update of the model.
     * The old model elements can be still accessed.
     */
    public void scanningStarted();
    
    /** Called when the DocumentModel update has started. 
     * The model elements are locked for reading until the updateFinished() method 
     * notifies that the model update finished.
     */
    public void updateStarted();
    
    /** Called when the DocumentModel update has finished. 
     * New model data are available now.
     */
    public void updateFinished();
    
}
