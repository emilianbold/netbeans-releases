/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.hints;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.openide.modules.ModuleInstall;



/**
 * Module installation class for HintsModule.
 *
 * @author Jan Lahoda
 */
public final class HintsModule extends ModuleInstall {

    public void restored () {
        //create HintsOperator (which registers some listeners)
        HintsOperator.getDefault();
    }
    
}
