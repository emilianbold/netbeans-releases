/*
 * CopyAction.java
 *
 * Created on 16 May 2006, 10:57
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
public class CopyAction extends ActionNoBlock{
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Copy..." menu item. */
    public static final String COPY_ITEM = "Copy To...";
    
    /** Creates a new instance of CopyAction */
    public CopyAction() {
        super("Versioning" + "|" + COPY_ITEM, SVN_ITEM + "|" + COPY_ITEM);
    }
    
}
