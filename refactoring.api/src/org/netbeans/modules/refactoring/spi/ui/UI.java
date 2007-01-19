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

package org.netbeans.modules.refactoring.spi.ui;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanel;
import org.netbeans.modules.refactoring.spi.impl.RefactoringPanelContainer;
import org.openide.windows.TopComponent;

/**
 * Various UI helper methods
 * @see RefactoringUI
 * @author Jan Becicka
 */
public final class UI {

    private UI() {
    }

    /**
     * Open Refactoring UI for specufied RefactoringUI
     */
    public static void openRefactoringUI(RefactoringUI ui) {
        new RefactoringPanel(ui);
    }
    
    /**
     * Open Refactoring UI for specufied RefactoringUI from specified TopComponent. 
     * callerTC will get focus when refactoring is finished.
     */
    public static void openRefactoringUI(RefactoringUI ui, TopComponent callerTC) {
        new RefactoringPanel(ui, callerTC);
    }

    /**
     * Open Refactoring UI for specufied RefactoringUI from specified TopComponent. 
     * callerTC will get focus when refactoring is finished.
     * @param callback this action will be called when user clicks refresh button
     * @param callerTC which component will get focus when refactoring is finished
     * @param ui this RefactoringUI will open
     */
    public static void openRefactoringUI(RefactoringUI ui, RefactoringSession callerTC, Action callback) {
        new RefactoringPanel(ui, callerTC, callback).setVisible(true);
    }
    
    /**
     * use this method from RefactoringElementImplementation.showPreview
     * @param component is set as a preview component of RefactoringPanel
     */ 
    public static boolean setComponentForRefactoringPreview(Component component) {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        RefactoringPanel refactoringPanel = null;
        if (activated instanceof RefactoringPanelContainer) {
            RefactoringPanelContainer panel = (RefactoringPanelContainer) activated;
            refactoringPanel = panel.getCurrentPanel();
        }
        if (refactoringPanel == null) {
            refactoringPanel = RefactoringPanelContainer.getRefactoringComponent().getCurrentPanel();
        }
        if (refactoringPanel == null) {
            refactoringPanel = RefactoringPanelContainer.getUsagesComponent().getCurrentPanel();
        }
        if (refactoringPanel == null) 
            return false;
        if (component == null) {
            if (refactoringPanel.splitPane.getRightComponent() == null)
                return false;
            component = new JLabel("<Preview not Available>", SwingConstants.CENTER);
        }
        refactoringPanel.splitPane.setRightComponent(component);
        return true;
        
    }
}
