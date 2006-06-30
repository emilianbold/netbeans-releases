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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
