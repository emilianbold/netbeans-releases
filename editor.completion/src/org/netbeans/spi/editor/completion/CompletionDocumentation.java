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

package org.netbeans.spi.editor.completion;

import java.net.URL;
import javax.swing.Action;

/**
 * The interface of an item that can be displayed in the documentation popup.
 *
 * @author Dusan Balek
 * @version 1.01
 */

public interface CompletionDocumentation {
        
    /**
     * Returns a HTML text dispaleyd in the documentation popup.
     */
    public String getText();
    
    /**
     * Returns a URL of the item's external representation that can be displayed
     * in an external browser or <code>null</code> if the item has no external
     * representation. 
     */
    public URL getURL();
    
    /**
     * Returns a documentation item representing an object linked from the item's 
     * HTML text.
     */
    public CompletionDocumentation resolveLink(String link);
    
    /**
     * Returns an action that opens the item's source representation in the editor
     * or <code>null</code> if the item has no source representation. 
     */    
    public Action getGotoSourceAction();
}
