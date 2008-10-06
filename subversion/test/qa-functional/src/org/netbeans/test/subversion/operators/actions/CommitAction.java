/*
 * CommitAction.java
 *
 * Created on 15 May 2006, 18:03
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
public class CommitAction extends ActionNoBlock {
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Commit" menu item. */
    public static final String COMMIT_ITEM = "Commit";
    
    /** Creates a new instance of CommitAction */
    public CommitAction() {
        super("Versioning" + "|" + COMMIT_ITEM, SVN_ITEM + "|" + COMMIT_ITEM);
    }
    
}
