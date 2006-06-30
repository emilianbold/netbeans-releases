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
