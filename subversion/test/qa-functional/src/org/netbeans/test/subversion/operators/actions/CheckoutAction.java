/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    public static final String VERSIONING_ITEM = "Versioning";
    
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";

    /** "Checkout..." menu item. */
    public static final String CHECKOUT_ITEM = "Checkout...";
    
    /** Creates a new instance of CheckoutAction */
    public CheckoutAction() {
        super(VERSIONING_ITEM + "|" + SVN_ITEM + "|" + CHECKOUT_ITEM, null);
    }
    
}
