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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.editor.settings.storage.*;


/**
 * Getters and setters for font & color editor profiles. Instances of this 
 * class should be registerred in {@MimeLookup} for particular mime types.
 *
 * @author Jan Jancura
 */
public abstract class FontColorSettingsFactory {

    /**
     * Gets all token font and colors for given scheme or null, if 
     * scheme does not exists. 
     * 
     * @param profile the name of profile
     *
     * @return token font and colors
     */
    public abstract Collection /*<AttributeSet>*/ getAllFontColors (String profile);
    
    /**
     * Gets default values for all font & colors for given profile, or null
     * if profile does not exist or if it does not have any defaults. 
     * 
     * @param profile the name of profile
     *
     * @return default values for all font & colors
     */
    public abstract Collection /*<AttributeSet>*/ getAllFontColorDefaults 
        (String profile);
    
    /**
     * Sets all token font and colors for given scheme. 
     * 
     * @param profile the name of profile
     * @param fontColors new colorings
     */
    public abstract void setAllFontColors (
        String profile,
        Collection /*<AttributeSet>*/ fontColors
    );
    
    /**
     * Sets default values for all token font and colors for given scheme. 
     * 
     * @param profile the name of profile
     * @param fontColors new colorings
     */
    public abstract void setAllFontColorsDefaults (
        String profile,
        Collection /*<AttributeSet>*/ fontColors
    );
}
