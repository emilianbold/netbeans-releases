/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
