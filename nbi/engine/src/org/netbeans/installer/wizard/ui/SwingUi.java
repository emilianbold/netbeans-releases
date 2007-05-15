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

package org.netbeans.installer.wizard.ui;

import javax.swing.JComponent;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiPanel;

/**
 * This class represents the UI of a {@link WizardComponent} for the 
 * {@link UiMode#SWING} UI mode.
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public abstract class SwingUi extends NbiPanel {
    /**
     * Returns the title of the component. The way the title is displayed is
     * dependent on the container. A frame could expose the title in the windows 
     * heading, for example.
     * 
     * @return Title of the component, or <code>null</code> if the component does 
     *      not have a title.
     */
    public abstract String getTitle();
    
    /**
     * Returns the description of the component. The way the description is 
     * displayed is dependent on the container.
     * 
     * @return Description of the component, or <code>null</code> if the component 
     *      does not have a description.
     */
    public abstract String getDescription();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Help</code> button. The expected behavior would 
     * be to display a help dialog which describes the required user input for the 
     * current component.
     */
    public abstract void evaluateHelpButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Back</code> button. The expected behavior would 
     * be to call the {@link Wizard#previous()} method.
     */
    public abstract void evaluateBackButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Next</code> button. The expected behavior would 
     * be to call the {@link Wizard#next()} method.
     */
    public abstract void evaluateNextButtonClick();
    
    /**
     * Hook, allowing the component's UI to execute some custom logic when the user 
     * activates the standard <code>Cancel</code> button. The expected behavior 
     * would be to cancel the wizard execution.
     */
    public abstract void evaluateCancelButtonClick();
    
    /**
     * Returns the button which should be activated when the user presses the 
     * <code>Enter</code> key.
     * 
     * @return Default handler for the <code>Enter</code> key.
     */
    public abstract NbiButton getDefaultEnterButton();
    
    /**
     * Returns the button which should be activated when the user presses the 
     * <code>Escape</code> key.
     * 
     * @return Default handler for the <code>Escape</code> key.
     */
    public abstract NbiButton getDefaultEscapeButton();
    
    /**
     * Returns the Swing component which should have focus when this UI is shown.
     * 
     * @return Default focus owner for this UI.
     */
    public abstract JComponent getDefaultFocusOwner();
}
