/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.completion;

import java.awt.Color;

import org.netbeans.modules.xml.api.model.*;

import javax.swing.text.JTextComponent;

/**
 * Represents value option (attribute one or element content one).
 * <p>
 * It takes advatage of replacent text vs. display name. Providers
 * should use shorted display name for list values. e.g. for
 * <code>&lt;example enums="one two three fo|"</code>
 * provider can return nodeValue <code>"one two three four"</code>
 * and display name <code>"four"</code> to denote that it actually
 * completed only the suffix.
 * 
 * @author  sands
 * @author  Petr Kuzel
 */
class ValueResultItem extends XMLResultItem {

    private final String replacementText;

    public ValueResultItem(GrammarResult res) {
        super(res.getNodeValue(), res.getDisplayName());
        foreground = Color.magenta;
        selectionForeground = Color.magenta.darker();
        replacementText = res.getNodeValue();
    }

    public String getReplacementText(int modifiers) {
        return replacementText;
    }

}
