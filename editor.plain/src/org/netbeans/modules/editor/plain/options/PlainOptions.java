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
