/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.editor.ext.CompletionQuery;

/**
* Java completion cell renderer. It delegates to ResultItems.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class CsmCellRenderer implements ListCellRenderer {

    private static ListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public CsmCellRenderer() {
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if( value instanceof CompletionQuery.ResultItem ) {
            return ((CompletionQuery.ResultItem)value).getPaintComponent( list, isSelected, cellHasFocus );
        } else {
            return defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
        }
    }
    
}
