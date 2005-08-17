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


package org.netbeans.modules.editor.structure.api;

import java.util.EventListener;


/**
 * An implementation of EventListener allowing to listen o changes of the DocumentModel.
 * This listner is very similar to the {@link DocumentElementListener} but in contrast with it 
 * it allows to listen on the entire model, not only on a particullar element.
 *<br>
 * Allows to listen on following changes:
 * <ul>
 * <li>A new element has been added into the model
 * <li>An element has been removed from the model
 * <li>Content of an element has been changed
 * <li>Attributes of an element has changed
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 *
 * @see DocumentElement
 * @see DocumentElementEvent
 * @see DocumentElementListener
 *
 */
public interface DocumentModelListener extends EventListener {
    
    /** fired when a new element has been added into the model. */
    public void documentElementAdded(DocumentElement de);
    
    /** fired when an existing element has been removed from the model. */
    public void documentElementRemoved(DocumentElement de);
    
    /** fired when an element's text content has been changed. */
    public void documentElementChanged(DocumentElement de);
    
    /** fired when attributes of an element have been changed (removed/added/value changed) */
    public void documentElementAttributesChanged(DocumentElement de);
    
}
