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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author S. Aubrecht
 */
public class WelcomeOptions {

    private static WelcomeOptions theInstance;
    
    private static final String PROP_SHOW_ON_STARTUP = "showOnStartup";
    private static final String PROP_FIRST_TIME_START = "firstTimeStart";
    
    private static final String FOLDER_NAME = "WelcomePage";
    
    private Logger log = Logger.getLogger( WelcomeOptions.class.getName() );

    /** Creates a new instance of WelcomeOptions */
    private WelcomeOptions() {
    }

    public static synchronized WelcomeOptions getDefault() {
        if( null == theInstance ) {
            theInstance = new WelcomeOptions();
        }
        return theInstance;
    }
 
    public void setShowOnStartup( boolean show ) {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( FOLDER_NAME );
        if( null != fo ) {
            try {
                fo.setAttribute( PROP_SHOW_ON_STARTUP, Boolean.valueOf(show) );
            } catch (IOException ex) {
                log.log( Level.INFO, ex.getMessage(), ex );
            }
        }
    }

    public boolean isShowOnStartup() {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( FOLDER_NAME );
        if( null != fo ) {
            Object val = fo.getAttribute( PROP_SHOW_ON_STARTUP );
            if( null != val && val instanceof Boolean ) {
                return ((Boolean)val).booleanValue();
            }
        }
        return true;
    }

    public void setFirstTimeStart( boolean firstTime ) {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( FOLDER_NAME );
        if( null != fo ) {
            try {
                fo.setAttribute( PROP_FIRST_TIME_START, Boolean.valueOf(firstTime) );
            } catch (IOException ex) {
                log.log( Level.INFO, ex.getMessage(), ex );
            }
        }
    }

    public boolean isFirstTimeStart() {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource( FOLDER_NAME );
        if( null != fo ) {
            Object val = fo.getAttribute( PROP_FIRST_TIME_START );
            if( null != val && val instanceof Boolean ) {
                return ((Boolean)val).booleanValue();
            }
        }
        return true;
    }
}
