/*
 * HTMLPaletteCustomizerAction.java
 *
 * Created on October 27, 2005, 10:49 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.core.palette;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author lk155162
 */
public class JSPPaletteCustomizerAction extends CallableSystemAction {

    private static String name;
    
    public JSPPaletteCustomizerAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = NbBundle.getBundle(JSPPaletteCustomizerAction.class).getString("ACT_OpenJSPCustomizer"); // NOI18N
        
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return null;
    }

    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     */
    public void performAction() {
        try {
            JSPPaletteFactory.getPalette().showCustomizer();
        }
        catch (IOException ioe) {
            Logger.getLogger("global").log(Level.WARNING, null, ioe);
        }
    }

}
