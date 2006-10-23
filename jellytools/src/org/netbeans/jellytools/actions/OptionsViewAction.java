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

/** Used to call "Tools|Options" main menu item or
 * "org.netbeans.core.actions.OptionsAction". If called on MAC it uses IDE API to
 * open Options.
 * @see Action
 */
public class OptionsViewAction extends Action {
    private static final String menu =
        Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                "Menu/Tools") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", 
                                "Options");

    /** Creates new instance. */    
    public OptionsViewAction() {
        super(menu, null, "org.netbeans.modules.options.OptionsWindowAction");
    }
    
    /** performs action through main menu. If called on MAC it uses IDE API to
     * open Options.
     */
    public void performMenu() {
        if(System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) { // NOI18N
            performAPI();
        } else {
            super.performMenu();
        }
    }
}
