/*
 * ImportAction.java
 *
 * Created on 10 May 2006, 11:46
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
public class ImportAction extends ActionNoBlock {
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Checkout..." menu item. */
    public static final String IMPORT_ITEM = "Import";
    
    /** Creates a new instance of ImportAction */
    public ImportAction() {
        super(SVN_ITEM + "|" + IMPORT_ITEM, SVN_ITEM + "|" + IMPORT_ITEM);
    }
    
}
