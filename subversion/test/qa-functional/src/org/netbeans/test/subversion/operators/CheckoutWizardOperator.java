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
 * CheckoutWizardOperator.java
 *
 * Created on 19/04/06 13:24
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.CheckoutAction;

/**
 * Class implementing all necessary methods for handling "CheckoutWizardOperator" NbDialog.
 *
 *
 * @author peter
 * @version 1.0
 */
public class CheckoutWizardOperator extends WizardOperator {

    /**
     * Creates new CheckoutWizardOperator that can handle it.
     */
    public CheckoutWizardOperator() {
        super("Checkout"); //NO I18N
    }

    /** Invokes new wizard and returns instance of CheckoutWizardOperator.
     * @return  instance of CheckoutWizardOperator
     */
    public static CheckoutWizardOperator invoke() {
        new CheckoutAction().perform();
        return new CheckoutWizardOperator();
    }
}

