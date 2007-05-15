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

import org.netbeans.installer.wizard.ui.WizardUi;

/**
 * This interface represents the container for the UI of a {@link WizardComponent}.
 * Each {@link Wizard} "owns" an instance of this class and uses it to initialize 
 * the UI of its active component.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public interface WizardContainer {
    /**
     * Shows or hides the container. The behavior of this method is 
     * component-specific. A frame would probably map this method directly, while
     * a console-mode container could draw itself or clear the screen.
     * 
     * @param visible Whether to show the container - <code>true</code>, or hide 
     * it - <code>false</code>.
     */
    void setVisible(final boolean visible);
    
    /**
     * Updates the container with a new UI. This method is usually called by the 
     * wizard when the active component changes - the wizard wants to display its 
     * UI.
     * 
     * @param ui UI which needs to be shown.
     */
    void updateWizardUi(final WizardUi ui);
}
