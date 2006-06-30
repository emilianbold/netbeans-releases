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

package org.netbeans.modules.javadoc.search;

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;

/** Just sets the right icon to IndexItem

 @author Petr Hrebejk
*/
class IndexListCellRenderer extends DefaultListCellRenderer {

    static final long serialVersionUID =543071118545614229L;
    public Component getListCellRendererComponent( JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        JLabel cr = (JLabel)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

        cr.setIcon( DocSearchIcons.getIcon( ((DocIndexItem)value).getIconIndex() ) );

        try {
            if (  ((DocIndexItem)value).getURL() == null )
                setForeground (java.awt.SystemColor.textInactiveText);
        }
        catch ( java.net.MalformedURLException e ) {
            setForeground (java.awt.SystemColor.textInactiveText);
        }
        return cr;
    }
}
