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
 * CheckoutWizardOperator.java
 *
 * Created on 19/04/06 13:24
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

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

