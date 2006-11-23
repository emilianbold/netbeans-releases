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

package org.netbeans.modules.search;

import org.netbeans.modules.search.types.TextDetail;

/**
 * Interface used for displaying item text.
 *
 * @author Tim Boudreau
 */
public interface TextDisplayer {
    
    /**
     * Displays the given text and possibly highlights the given range of text.
     * 
     * @param  txt  text to be displayed
     * @param  mimeType  mime type of the text/file
     * @param  location  determines range of text that is to be highlighted
     *                   ({@code location.x} = start position,
     *                    {@code location.y} = end position);
     *                   if {@code null}, nothing is to be highlighted
     */
    public void setText(String txt,
                        String mimeType,
                        TextDetail location);
    
}