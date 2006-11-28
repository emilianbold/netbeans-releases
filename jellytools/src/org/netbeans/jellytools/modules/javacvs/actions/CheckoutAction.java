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
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "Versioning|CVS|Checkout..." main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class CheckoutAction extends ActionNoBlock {
    /** "Versioning" menu item. */
    public static final String VERSIONING_ITEM = Bundle.getStringTrimmed(
           "org.netbeans.modules.versioning.Bundle", "Menu/Window/Versioning");
    /** "CVS" menu item. */
    public static final String CVS_ITEM = Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle",
                "CTL_MenuItem_CVSCommands_Label");
    /** "Checkout..." menu item. */
    public static final String CHECKOUT_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.checkout.Bundle", "CTL_MenuItem_Checkout_Label");
    
    /** Creates new CheckoutAction instance. */
    public CheckoutAction() {
        super(VERSIONING_ITEM+"|"+CVS_ITEM+"|"+CHECKOUT_ITEM, null);
    }
}

