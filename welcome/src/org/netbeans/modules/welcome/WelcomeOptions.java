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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public class WelcomeOptions {

    private static WelcomeOptions theInstance;
    
    private static final String PROP_SHOW_ON_STARTUP = "showOnStartup";
    private static final String PROP_LAST_ACTIVE_TAB = "lastActiveTab";
    private static final String PROP_START_COUNTER = "startCounter";
    
    private PropertyChangeSupport propSupport;
    
    /** Creates a new instance of WelcomeOptions */
    private WelcomeOptions() {
    }

    private Preferences prefs() {
        return NbPreferences.forModule(WelcomeOptions.class);
    }

    public static synchronized WelcomeOptions getDefault() {
        if( null == theInstance ) {
            theInstance = new WelcomeOptions();
        }
        return theInstance;
    }
 
    public void setShowOnStartup( boolean show ) {
        boolean oldVal = isShowOnStartup();
        prefs().putBoolean(PROP_SHOW_ON_STARTUP, show);
        if( null != propSupport )
            propSupport.firePropertyChange( PROP_SHOW_ON_STARTUP, oldVal, show );
    }

    public boolean isShowOnStartup() {
        return prefs().getBoolean(PROP_SHOW_ON_STARTUP, true);
    }

    public void setLastActiveTab( int tabIndex ) {
        int oldVal = getLastActiveTab();
        prefs().putInt(PROP_LAST_ACTIVE_TAB, tabIndex);
        if( null != propSupport )
            propSupport.firePropertyChange( PROP_LAST_ACTIVE_TAB, oldVal, tabIndex );
    }

    public int getLastActiveTab() {
        return prefs().getInt(PROP_LAST_ACTIVE_TAB, -1);
    }
    
    public boolean isSecondStart() {
        return prefs().getInt(PROP_START_COUNTER, -1) == 2;
    }
    
    public void incrementStartCounter() {
        int count = prefs().getInt(PROP_START_COUNTER, 0) + 1;
        if( count > 3 )
            return; //we're just interested in the first and second start so don't bother any more then
        prefs().putInt( PROP_START_COUNTER, count );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        if( null == propSupport )
            propSupport = new PropertyChangeSupport( this );
        propSupport.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        if( null == propSupport )
            return;
        propSupport.removePropertyChangeListener( l );
    }
}
