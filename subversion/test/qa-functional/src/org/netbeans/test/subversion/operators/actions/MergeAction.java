/*
 * MergeAction.java
 *
 * Created on 16 May 2006, 15:04
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
public class MergeAction extends ActionNoBlock{
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Merge" menu item. */
    public static final String MERGE_ITEM = "Merge";
    
    /** Creates a new instance of MergeAction */
    public MergeAction() {
        super(SVN_ITEM + "|" + MERGE_ITEM, SVN_ITEM + "|" + MERGE_ITEM);
    }
}
