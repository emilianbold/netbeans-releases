/*
 * CommitAction.java
 *
 * Created on 15 May 2006, 18:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.operators.actions;

import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author peter
 */
public class CommitAction extends ActionNoBlock {
    
    /** "Mercurial" menu item. */
    public static final String HG_ITEM = "Mercurial";
            
    /** "Commit" menu item. */
    public static final String COMMIT_ITEM = "Commit";
    
    /** Creates a new instance of CommitAction */
    public CommitAction() {
        super(HG_ITEM + "|" + COMMIT_ITEM, HG_ITEM + "|" + COMMIT_ITEM);
    }
    
}
