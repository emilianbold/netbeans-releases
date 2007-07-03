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

package org.netbeans.modules.ruby.rhtml.editor;

import org.netbeans.modules.html.editor.options.HTMLOptions;
import org.netbeans.modules.ruby.rhtml.editor.RhtmlKit;
import java.util.MissingResourceException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
* Options for the RHTML editor kit
*
* @author Miloslav Metelka
* @author Tor Norbye
* @version 1.00
*/
public class RhtmlOptions extends HTMLOptions {

    public static final String RHTML = "rhtml"; // NOI18N

    private static final String HELP_ID = "editing.editor.rhtml"; // !!! NOI18N
                                        
    static final long serialVersionUID = 75289734362748537L;
   
    public RhtmlOptions() {
        super(RhtmlKit.class, RHTML);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(RhtmlOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
