/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.openide.util.actions.SystemAction;

/**
 * Save All action. It has only main menu representation.
 * @author Jiri.Skrivanek@sun.com
 */
public class SaveAllAction extends Action {
    
    /** "File|Save All" */
    private static final String saveAllMenu = 
                       Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File")
                       + "|"
                       + Bundle.getStringTrimmed("org.openide.actions.Bundle", "SaveAll");
    
    private static final String systemActionClassname = "org.openide.actions.SaveAllAction";
    
    /** Creates new SaveAllAction instance. */
    public SaveAllAction() {
        super(saveAllMenu, null, systemActionClassname);
    }
    
    /** Performs action through main menu and wait until action is not finished. */
    public void performMenu() {
        super.performMenu();
        waitFinished();
    }
    
    /** Performs action through API call and wait until action is not finished. */
    public void performAPI() {
        super.performAPI();
        waitFinished();
    }

    /** Waits until SaveAllAction is finished. Actually it waits until system
     * action is disabled. */
    private void waitFinished() {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object systemAction) {
                    return ((SystemAction)systemAction).isEnabled() ? null:Boolean.TRUE;
                }
                public String getDescription() {
                    return("Wait SaveAllAction is finished.");
                }
            });
            SystemAction saveAllAction = SystemAction.get(Class.forName(systemActionClassname));
            waiter.waitAction(saveAllAction);
        } catch(InterruptedException e) {
            throw new JemmyException("Waiting interrupted.", e);
        } catch (ClassNotFoundException e) {
            throw new JemmyException("Class not found.", e);
        }
    }
    
}