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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.openide.util.actions.SystemAction;

/** Used to call "File|Save All" main menu item or
 * "org.openide.actions.SaveAllAction".
 * <br>
 * After action is performed it waits until action is disabled which should
 * means the action is finished.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class SaveAllAction extends Action {

    /** "File|Save All" */
    private static final String saveAllMenu = 
                       Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File")
                       + "|"
                       + Bundle.getStringTrimmed("org.openide.loaders.Bundle", "SaveAll");
    
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