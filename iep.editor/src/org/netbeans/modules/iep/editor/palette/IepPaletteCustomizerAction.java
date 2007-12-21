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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.palette;

import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * IepPaletteCustomizerAction.java
 * 
 * Created on November 20, 2005, 1:54 PM
 * 
 * 
 * @author Bing Lu
 */
public class IepPaletteCustomizerAction extends CallableSystemAction {

    private static String name;
    
    public IepPaletteCustomizerAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    /** 
     * Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = NbBundle.getBundle(IepPaletteCustomizerAction.class).getString("ACT_OpenIepCustomizer"); // NOI18N
        
        return name;
    }

    /** 
     * Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /** 
     * This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     */
    public void performAction() {
        IepPaletteFactory.getPalette().showCustomizer();
        
    }

}
