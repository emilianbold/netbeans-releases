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

package org.netbeans.api.editor.settings;

import javax.swing.text.AttributeSet;

/**
 * Fonts and Colors settings are represented by map of 
 * key=&lt;String&gt;fonts and colors syntax name and value=AttributeSet.
 * <br>
 * The keys for returned AttributeSet are defined by {@link javax.swing.text.StyleConstants} and
 * {@link org.netbeans.api.editor.settings.EditorStyleConstants}
 * <br>
 * <br>
 * Supported keys for FontColorSettings are: 
 * <ol>
 *    <li> StyleConstants.FontFamily </li>
 *    <li> StyleConstants.FontSize </li>
 *    <li> StyleConstants.Bold </li>
 *    <li> StyleConstants.Italic </li>
 *    <li> StyleConstants.Foreground </li>
 *    <li> StyleConstants.Background </li>
 *    <li> StyleConstants.Underline </li>
 *    <li> StyleConstants.StrikeThrough </li>
 *    <li> and all attributes defined in {@link org.netbeans.api.editor.settings.EditorStyleConstants} </li>
 * </ol>
 * <br>
 * Instances of this class should be retrieved from the {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * for a given mime-type.
 * <br>
 * <font color="red">This class must NOT be extended by any API clients</font>
 *
 * @author Martin Roskanin
 */
public abstract class FontColorSettings {

    public static final String PROP_FONT_COLORS = "fontColors"; //NOI18N
    
    /**
     * Construction prohibited for API clients.
     */
    public FontColorSettings() {
        // Control instantiation of the allowed subclass only
        if (!"org.netbeans.modules.editor.settings.storage.FontColorSettingsImpl".equals(getClass().getName())) {
            throw new IllegalStateException("Instantiation prohibited. " + getClass().getName()); // NOI18N
        }
    }
    
    /**
     * Gets the font and colors. 
     * 
     * @param settingName font and colors setting name
     *
     * @return AttributeSet describing the font and colors. 
     */
    public abstract AttributeSet getFontColors(String settingName);

    /**
     * Gets the token font and colors. 
     * 
     * @param tokenName token name
     *
     * @return AttributeSet describing the font and colors
     */
    public abstract AttributeSet getTokenFontColors(String tokenName);
    
}
