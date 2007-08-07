/*
 * HTMLPaletteCustomizerAction.java
 *
 * Created on October 27, 2005, 10:49 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.visualweb.designer.jsf.palette;

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
public class JSF1_1PaletteCustomizerAction extends CallableSystemAction {

    private static String name;
    
    public JSF1_1PaletteCustomizerAction () {
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
            name = NbBundle.getBundle(JSF1_1PaletteCustomizerAction.class).getString("ACT_OpenJSF1_1Customizer"); // NOI18N
        
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
            PaletteControllerFactory.getJsfPaletteController_1_4().showCustomizer();

    }

}
