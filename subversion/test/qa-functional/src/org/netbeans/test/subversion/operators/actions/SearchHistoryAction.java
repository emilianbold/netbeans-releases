/*
 * SearchHistoryAction.java
 *
 * Created on 14 June 2006, 15:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.operators.actions;

import org.netbeans.jellytools.actions.Action;

/**
 *
 * @author peter
 */
public class SearchHistoryAction extends Action{
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "SEARCH HISTORY" menu item. */
    public static final String SEARCH_HISTORY_ITEM = "Search History...";
    
    /** Creates a new instance of SEARCH HISTORY */
    public SearchHistoryAction() {
        super("Versioning" + "|" + SEARCH_HISTORY_ITEM, SVN_ITEM + "|" + SEARCH_HISTORY_ITEM);
    }
}
