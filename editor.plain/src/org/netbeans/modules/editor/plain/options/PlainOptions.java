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

package org.netbeans.modules.editor.plain.options;

import java.util.MissingResourceException;
import org.netbeans.modules.editor.plain.PlainKit;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.editor.options.BaseOptions;

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class PlainOptions extends BaseOptions {

    public static final String PLAIN = "plain"; // NOI18N

    static final long serialVersionUID =-7082075147378689853L;

    private static final String HELP_ID = "editing.editor.plain"; // !!! NOI18N
    
    static final String[] PLAIN_PROP_NAMES = BaseOptions.BASE_PROP_NAMES;
    
    public PlainOptions() {
        this(PlainKit.class, PLAIN);
    }

    public PlainOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(PlainOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
