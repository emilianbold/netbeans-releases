/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome;

import org.openide.options.SystemOption;

/**
 *
 * @author S. Aubrecht
 */
public class WelcomeOptions extends SystemOption {

    static final long serialVersionUID = 1L;

    private static final String PROP_SHOW_ON_STARTUP = "showOnStartup";

    /** Creates a new instance of WelcomeOptions */
    public WelcomeOptions() {
    }

    public static WelcomeOptions getDefault() {
        return (WelcomeOptions) findObject( WelcomeOptions.class, true );
    }
 
    public String displayName() {
        return "Welcome Page Settings";
    }

    public void setShowOnStartup( boolean show ) {
        putProperty( PROP_SHOW_ON_STARTUP, Boolean.valueOf(show), true );
    }

    public boolean isShowOnStartup() {
        Object res = getProperty( PROP_SHOW_ON_STARTUP );
        if( null != res && res instanceof Boolean ) {
            return ((Boolean)res).booleanValue();
        }
        return true;
    }
}
