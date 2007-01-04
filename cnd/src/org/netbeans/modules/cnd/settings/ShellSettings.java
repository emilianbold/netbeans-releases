/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.settings;

import java.util.ResourceBundle;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Settings for the C/C++/Fortran. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 */

public class ShellSettings extends SystemOption {

    /** The singleton instance */
    //static ShellSettings cppSettings;

    /** serial uid */
    static final long serialVersionUID = -2942465353463577336L;

    // Option labels
    public static final String PROP_DEFSHELLCOMMAND = "defaultShellCommand";//NOI18N
    public static final String PROP_SAVE_ALL        = "saveAll";        //NOI18N
    
    /** The resource bundle for the form editor */
    public static ResourceBundle bundle;

    /**
     *  Initialize each property.
     */
    protected void initialize() {
	super.initialize();
    }


    /** Return the signleton cppSettings */
    public static ShellSettings getDefault() {
	return (ShellSettings) findObject(ShellSettings.class, true);
    }


    /**
     * Get the display name.
     *
     *  @return value of OPTION_CPP_SETTINGS_NAME
     */
    public String displayName () {
	return getString("OPTION_SHELL_SETTINGS_NAME");		        //NOI18N
    }
    
    public HelpCtx getHelpCtx () {
	return new HelpCtx ("Welcome_opt_shell_settings");	        //NOI18N // FIXUP
    }

    /** 
    * Default Shell Command
    */
    public void setDefaultShellCommand(String dsc) {
        putProperty(PROP_DEFSHELLCOMMAND, dsc, true);
    }

    /**
    * Default Shell Command
    */
    public String getDefaultShellCommand() {
        String dsc = (String)getProperty(PROP_DEFSHELLCOMMAND);
        if (dsc == null) {
            return "/bin/sh"; // NOI18N
        } else {
            return dsc;
        }
    }

    /** Getter for the SaveAll property */
    public boolean getSaveAll () {
        Boolean dsc = (Boolean)getProperty(PROP_SAVE_ALL);
        if (dsc == null) {
            return true;
        }
        return dsc.booleanValue();
    }
    
    /** Setter for the SaveAll property */
    public void setSaveAll (boolean sa) {
        putProperty(PROP_SAVE_ALL, sa ? Boolean.TRUE : Boolean.FALSE, true);
    }

    /** @return localized string */
    static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ShellSettings.class);
	}
	return bundle.getString(s);
    }
}
