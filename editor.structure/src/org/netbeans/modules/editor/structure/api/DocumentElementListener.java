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
 * An implementation of EventListener allowing to listen o changes of a DocumentElement.
 *<br>
 * Allows to listen on following changes:
 * <ul>
 * <li>A child has been added into the element
 * <li>A child has been removed into the element
 * <li>Children of the element have been reordered
 * <li>Text content of the element has changed
 * <li>Attributes of the element has changed
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 * @see DocumentElement
 * @see DocumentElementEvent
 */
public interface DocumentElementListener extends EventListener {

    //note: there are no events like elementRenamed or elementPositionChanged
    //1.if the element is renamed then the parent disposes it and creates a new one.
    //2.the Positions objects changes its inner position representation itself.
    
    /** fired when a new child has been added into the element. */
    public void elementAdded(DocumentElementEvent e);
    
    /** fired when a child has been removed from the element. */
    public void elementRemoved(DocumentElementEvent e);

    /** fired when children of the element have been reordered. */
    public void childrenReordered(DocumentElementEvent e);
    
    /** fired when the element's text content has been changed. */
    public void contentChanged(DocumentElementEvent e);
    
    /** fired when attributes of the element have been changed (removed/added/value changed) */
    public void attributesChanged(DocumentElementEvent e);
   
}
