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
    
    Color getPaintColor() { return Color.blue; }

}
