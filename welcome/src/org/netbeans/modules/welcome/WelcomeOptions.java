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

package org.netbeans.modules.welcome;

import org.openide.options.SystemOption;

/**
 *
 * @author S. Aubrecht
 */
public class WelcomeOptions extends SystemOption {

    static final long serialVersionUID = 1L;

    private static final String PROP_SHOW_ON_STARTUP = "showOnStartup";
    private static final String PROP_FIRST_TIME_START = "firstTimeStart";

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

    public void setFirstTimeStart( boolean firstTime ) {
        putProperty( PROP_FIRST_TIME_START, Boolean.valueOf(firstTime), true );
    }

    public boolean isFirstTimeStart() {
        Object res = getProperty( PROP_FIRST_TIME_START );
        if( null != res && res instanceof Boolean ) {
            return ((Boolean)res).booleanValue();
        }
        return true;
    }
}
