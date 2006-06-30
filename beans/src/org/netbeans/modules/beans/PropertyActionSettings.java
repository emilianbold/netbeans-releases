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

package org.netbeans.modules.beans;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Options for setting properties of generating property patterns from fields
 *
 * @author  Petr Suchomel
 */
public class PropertyActionSettings extends SystemOption {

    public static final String GENERATE_UNDERSCORED= "_"; // NOI18N
    public static final String GENERATE_WITH_THIS  = "this."; // NOI18N
    /** define value for property style of generated property variable */
    public static final String PROP_STYLE   = "prop_style"; // NOI18N
    static final String PROP_NAME_STYLE = "propStyle"; // NOI18N

    private static final long serialVersionUID =45122597471838193L;

    private boolean initializing;

    /** inicialize object
     */
    protected void initialize () {
        super.initialize ();
        try {
            initializing = true;
            if( getProperty (PROP_STYLE) == null )
                setPropStyle(PropertyActionSettings.GENERATE_WITH_THIS);
        } finally {
            initializing = false;
        }
    }

    /** Human readable class name
     * @return readable name
     */
    public String displayName () {
        return NbBundle.getMessage(PropertyActionSettings.class, "PROP_Option_Menu");
    }

    /** Default instance of this system option, for the convenience of associated classes.
     * @return itself, only one instance
     */
    public static PropertyActionSettings getDefault() {
        return (PropertyActionSettings)PropertyActionSettings.findObject(PropertyActionSettings.class,true);
    }

    /** Return setting for generating indexed property
     * @return setting for indexed property
     */
    public String getPropStyle () {
        return (String) getProperty (PROP_STYLE);
    }

    /** Set setting for generating indexed property
     * @param indexed setting for indexed property
     */
    public void setPropStyle (String style ) {
        putProperty (PROP_STYLE, style, false);
        if (!(initializing || isReadExternal()))
            firePropertyChange(PROP_NAME_STYLE, null, null);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PropertyActionSettings.class.getName());
    }
}
