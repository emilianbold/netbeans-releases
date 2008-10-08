/*
 * RevertAction.java
 *
 * Created on 18 May 2006, 17:01
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
public class RevertAction extends ActionNoBlock{

    /** "Versioning" menu item. */
    public static final String VERSIONING_MENU_ITEM = "Versioning";

    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";

    /** "Revert" popup menu item. */
    public static final String REVERT_MENU_ITEM = "Revert Modifications...";

    /** "Revert" popup menu item. */
    public static final String REVERT_POPUP_ITEM = "Revert Modifications...";
    
    /** Creates a new instance of RevertAction */
    public RevertAction() {
        super(VERSIONING_MENU_ITEM + "|" + SVN_ITEM + "|" + REVERT_MENU_ITEM, SVN_ITEM + "|" + REVERT_POPUP_ITEM);
    }
    
}
