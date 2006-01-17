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
package org.netbeans.jellytools.modules.debugger.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "Finish All" popup menu item in Sessions view.
 * @author Jiri.Skrivanek@sun.com
 * @see org.netbeans.jellytools.actions.Action
 * @see org.netbeans.jellytools.modules.debugger.SessionsOperator
 */
public class FinishAllAction extends Action {
    
    // "Finish All"
    private static final String popupPath = Bundle.getStringTrimmed(
                                "org.netbeans.modules.debugger.ui.models.Bundle",
                                "CTL_SessionAction_FinishAll_Label");
    
    /** Creates new FinishAllAction instance. */
    public FinishAllAction() {
        super(null, popupPath);
    }
}