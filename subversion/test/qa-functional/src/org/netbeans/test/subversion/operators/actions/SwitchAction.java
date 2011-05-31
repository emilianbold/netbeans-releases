/*
 * SwitchAction.java
 *
 * Created on 17 May 2006, 22:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.operators.actions;

import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author peter
 */
public class SwitchAction extends ActionNoBlock {
    
    /** "Team" menu item. */
    public static final String TEAM_ITEM = "Team";
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";

    /** "Switch" menu item. */
    public static final String SWITCH_MENU_ITEM = "Switch to...";
    
    /** "Switch" menu item. */
    public static final String SWITCH_POPUP_ITEM = "Switch to Copy...";
    
    /** Creates a new instance of SwitchAction */
    public SwitchAction() {
        super(TEAM_ITEM + "|" + SWITCH_MENU_ITEM, SVN_ITEM + "|" + SWITCH_POPUP_ITEM);
    }
    
}
