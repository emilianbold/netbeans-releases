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

package org.netbeans.bluej.options;

import java.io.File;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * 
 * @author Milos Kleint
 */
public class BlueJSettings extends SystemOption {
    public static final String PROP_HOME = "home"; // NOI18N
    
    private static final long serialVersionUID = -4857548488373437L;
    
    protected void initialize() {
        super.initialize();
        //TODO try to guess the correct locations..
    }
    
    public String displayName() {
        return NbBundle.getMessage(BlueJSettings.class, "LBL_Settings"); //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public static BlueJSettings getDefault() {
        return (BlueJSettings) findObject(BlueJSettings.class, true);
    }
    
    public File getHome() {
        return (File)getProperty(PROP_HOME);
    }
    
    public void setHome(File home) {
        putProperty(PROP_HOME, home, true);
    }    
    
    
}
