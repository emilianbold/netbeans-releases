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

import java.util.List;
import javax.swing.KeyStroke;

/**
 * Code templates settings are represented by map
 * of key=&lt;String&gt;code template name
 * and value=&lt;String&gt;code template string.
 * <br>
 * Instances of this class should be retrieved from the {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * for a given mime-type.
 * <br>
 * <font color="red">This class must NOT be extended by any API clients</font>
 *
 * @author Martin Roskanin
 */
public abstract class CodeTemplateSettings {

    /**
     * Construction prohibited for API clients.
     */
    public CodeTemplateSettings() {
        // Control instantiation of the allowed subclass only
        if (!"org.netbeans.modules.editor.settings.xxx".equals(getClass().getName())) {
            throw new IllegalStateException("Instantiation prohibited."); // NOI18N
        }
    }
    
    /**
     * Gets list of code template descriptions.
     *
     * @return non-modifiable list of the code template descriptions.
     */
    public abstract List/*<CodeTemplateDescription>*/ getCodeTemplateDescriptions();
    
    /**
     * Get the keystroke that expands the code templates abbreviations.
     *
     * @return non-null keystroke that expands the code template abbreviations
     *  into code templates.
     */
    public abstract KeyStroke getExpandKey();
    
}
