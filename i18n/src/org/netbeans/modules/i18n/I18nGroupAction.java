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


package org.netbeans.modules.i18n;


import java.awt.event.ActionEvent;

import org.netbeans.modules.i18n.wizard.I18nWizardAction;
import org.netbeans.modules.i18n.wizard.I18nTestWizardAction;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;


/**
 * Abstract enclosing class for I18n group actions. Just for sharing static context.
 *
 * @author  Peter Zavadsky
 * @see I18nGroupAction.Menu
 * @see I18nGroupAction.Popup
 */
public abstract class I18nGroupAction extends SystemAction {

    /** Array of i18n actions. */
    protected static final SystemAction[] i18nActions = new SystemAction[] {
        SystemAction.get(I18nWizardAction.class),
        SystemAction.get(I18nTestWizardAction.class),
        SystemAction.get(I18nAction.class),
        SystemAction.get(InsertI18nStringAction.class)
    };

    /** Does nothing. Shouldn't be called. Implements superclass abstract method. */
    public void actionPerformed(ActionEvent ev) {
    }

    /** Gets localized name of action. Implements superclass abstract method. */
    public String getName() {
        return I18nUtil.getBundle().getString("LBL_I18nGroupActionName");
    }

    /** Gets icon resource. Overrides suprclass method. */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
    }

    /** Gets help context. Implements abstract superclass method. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(I18nGroupAction.class);
    }
    


}
