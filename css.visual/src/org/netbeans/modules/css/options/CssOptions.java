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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * CssOptions.java
 *
 * Created on December 9, 2004, 5:02 PM
 */

package org.netbeans.modules.css.options;

import org.netbeans.modules.css.editor.CssEditorKit;
import java.util.MissingResourceException;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Options for the CSS Editor
 * @author Winston Prakash
 * @version 1.0
 */
public class CssOptions extends BaseOptions {
    private static final String HELP_ID = "editing.editor.css"; // NOI18N

    /**
     * CSS option editor gets the locale for the Coloring Token Names displayed
     * in the Option Editor
     */
    public static final LocaleSupport.Localizer localizer = new LocaleSupport.Localizer() {
        public String getString(String key) {
            try {
                return NbBundle.getBundle(CssOptions.class).getString(key);
            } catch(MissingResourceException mre) {
                return null;
            }
        }
    };

    /** Creates a new instance of CssOptions */
    public CssOptions() {
        super(CssEditorKit.class, "CSS"); //NOI18N
        LocaleSupport.addLocalizer(localizer);
    }

//    protected Class getDefaultIndentEngineClass() {
//        return CssIndentEngine.class;
//    }

    /** Set the display name that will appear in the option editor*/
    public String displayName() {
        return  NbBundle.getMessage(CssOptions.class, "OPTIONS_CSS");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }

}
