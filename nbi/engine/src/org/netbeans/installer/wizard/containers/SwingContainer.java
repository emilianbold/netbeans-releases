/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.wizard.containers;

import org.netbeans.installer.utils.helper.swing.NbiButton;

/**
 * This is the specialization of the {@link WizardContainer} interface with regard 
 * to Swing UI mode - {@link UiMode#SWING}. It defines several methods which are
 * specific to the Swing-based containers.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public interface SwingContainer extends WizardContainer {
    /**
     * Returns the Swing implementation of the standard <code>Help</code> button.
     * 
     * @return <code>Help</code> button instance.
     */
    NbiButton getHelpButton();
    
    /**
     * Returns the Swing implementation of the standard <code>Back</code> button.
     * 
     * @return <code>Back</code> button instance.
     */
    NbiButton getBackButton();
    
    /**
     * Returns the Swing implementation of the standard <code>Next</code> button.
     * 
     * @return <code>Next</code> button instance.
     */
    NbiButton getNextButton();
    
    /**
     * Returns the Swing implementation of the standard <code>Cancel</code> button.
     * 
     * @return <code>Cancel</code> button instance.
     */
    NbiButton getCancelButton();
}
