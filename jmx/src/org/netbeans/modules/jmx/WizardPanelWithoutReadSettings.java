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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx;
import org.openide.WizardDescriptor;

/**
 * Class used to avoid the bad effect of the calls of readSettings method with
 * a generic file panel (It creates the file each time you click on next).
 * @author tl156378
 */
public class WizardPanelWithoutReadSettings extends FinishableDelegatedWizardPanel{

   public WizardPanelWithoutReadSettings(WizardDescriptor.Panel delegate,
            GenericWizardPanel finishDelegate) {
        super(delegate, finishDelegate);
   }

   /**
     * This method is called when a step is loaded.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
   public void readSettings(Object settings) {
        finishDelegate.storeSettings(settings);
        finishDelegate.readSettings(settings);
    }
}
