/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.List;
import java.util.HashMap;
import org.netbeans.editor.Settings;
import org.openide.options.ContextSystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.text.PrintSettings;
import org.netbeans.editor.EditorState;

/**
* Root node for all available editor options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class AllOptions extends ContextSystemOption {

    static final long serialVersionUID =-5703125420292694573L;

    // Initialize global options
    BaseOptions baseOptions = (BaseOptions)BaseOptions.findObject(BaseOptions.class, true);

    public AllOptions() {
        // Add the initializer for the base options. It will not be removed
        Settings.addInitializer(baseOptions.getSettingsInitializer(),
            Settings.OPTION_LEVEL);
    }
    
    /** Initialization of the options contains adding listener
     * watching for adding/removing child options to <code>AllOptions</code>
     * and <code>org.openide.text.PrintSettings</code>
     * and adding standard (java, html, plain) child options.
     */
    public void init() {
        refreshContextListeners();
    }
    
    private void refreshContextListeners() {
        PrintSettings ps = (PrintSettings) PrintSettings.findObject(PrintSettings.class, true);
        // Start listening on AllOptions and PrintSettings
        ContextOptionsListener.processExistingAndListen(ps);
        ContextOptionsListener.processExistingAndListen(this);
    }

    public String displayName() {
        return NbBundle.getBundle(AllOptions.class).getString("OPTIONS_all"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (AllOptions.class);
    }

    public List getKeyBindingList() {
        return baseOptions.getKeyBindingList();
    }

    public void setKeyBindingList(List list) {
        baseOptions.setKeyBindingList(list);
    }

    public HashMap getEditorState() {
        return EditorState.getStateObject();
    }
    
    public void setEditorState( HashMap stateObject ) {
        EditorState.setStateObject( stateObject );
    }
    
    public boolean isGlobal() {
        return true;
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        refreshContextListeners(); // beanContext was changed in super
    }

}
