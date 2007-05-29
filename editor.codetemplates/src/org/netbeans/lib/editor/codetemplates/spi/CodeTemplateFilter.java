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

package org.netbeans.lib.editor.codetemplates.spi;

import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;

/**
 * Filter accepting code templates being displayed in a code completion popup.
 * It is also used for editor hints (code templates) over a text selection.
 *
 * @author Dusan Balek
 */
public interface CodeTemplateFilter {
  
    /**
     * Accept or reject the given code template.
     * 
     * @param template non-null template to accept or reject.
     * @return true to accept the given code template or false to reject it.
     */
    boolean accept(CodeTemplate template);
    
    /**
     * Factory for producing of the code template filters.
     * <br/>
     * It should be registered in the MimeLookup for a given mime-type.
     */
    public interface Factory {
        
        /**
         * Create code template filter for the given context.
         * 
         * @param component non-null component for which the filter is being created.
         * @param offset &gt;=0 offset for which the filter is being created.
         * @return non-null code template filter instance.
         */
        CodeTemplateFilter createFilter(JTextComponent component, int offset);
        
    }
}
