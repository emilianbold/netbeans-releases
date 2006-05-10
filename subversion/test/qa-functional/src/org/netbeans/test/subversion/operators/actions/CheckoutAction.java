/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CheckoutAction.java
 *
 * Created on 20 April 2006, 15:23
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
public class CheckoutAction extends ActionNoBlock {
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Checkout..." menu item. */
    public static final String CHECKOUT_ITEM = "Checkout...";
    
    /** Creates a new instance of CheckoutAction */
    public CheckoutAction() {
        super(SVN_ITEM + "|" + CHECKOUT_ITEM, null);
    }
    
}
