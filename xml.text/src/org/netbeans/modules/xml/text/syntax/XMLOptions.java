/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import java.util.*;

/**
 * Options for the xml editor kit
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class XMLOptions extends AbstractBaseOptions {
    /** Serial Version UID */
    private static final long serialVersionUID = 2347735706857337892L;

    
    //
    // init
    //

    /** */
    public XMLOptions () {
        super (XMLKit.class, "xml"); // NOI18N
    }

    
    // remap old XMLTokenContext to new XMLDefaultTokenContext
    private static final String[][] TRANSLATE_COLORS = {
        { "xml-comment", "xml-block-comment" },
        { "xml-ref", "xml-character" },
        { "xml-string", "xml-value" },
        { "xml-attribute", "xml-argument" },
        { "xml-symbol", "xml-operator" },
//        { "xml-tag", "xml-tag" },
        { "xml-keyword", "xml-sgml-declaration" },
        { "xml-plain", "xml-text"},
    };
    
    /**
     * Get coloring, possibly remap setting from previous versions
     * to new one.
     */
    public synchronized Map getColoringMap() {
        Map colors = super.getColoringMap();
        
        // get old customized colors and map them to new token IDs
        // the map will contain only such old colors that was customized AFAIK
        // because current initializer does not create them
        
        for (int i = 0; i<TRANSLATE_COLORS.length; i++) {
            String oldKey = TRANSLATE_COLORS[i][0];
            Object color = colors.get(oldKey);
            if (color != null) {
                colors.remove(oldKey);
                String newKey = TRANSLATE_COLORS[i][1];
                colors.put(newKey, color);
            }
        }

        // do not save it explicitly if the user will do a customization
        // it get saved automatically (i.e.old keys removal will apply)
        
        return colors;
    }
}
