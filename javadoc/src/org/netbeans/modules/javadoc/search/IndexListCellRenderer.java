/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.javadoc.search;

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

/* 
 * Log
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/23/99  Petr Hrebejk    HTML doc view & sort 
 *       modes added
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 