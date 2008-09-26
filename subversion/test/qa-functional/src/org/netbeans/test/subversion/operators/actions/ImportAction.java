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
    
    /** "Versioning" menu item. */
    public static final String VERSIONING_ITEM = "Versioning";
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Import..." menu item. */
    public static final String IMPORT_ITEM = "Import into Repository...";
    
    /** Creates a new instance of ImportAction */
    public ImportAction() {
        super(VERSIONING_ITEM + "|" + SVN_ITEM + "|" + IMPORT_ITEM, VERSIONING_ITEM + "|" + IMPORT_ITEM);
    }
    
}
