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


package org.netbeans.modules.refactoring.spi.impl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * Action which opens the Find Usages window.
 *
 * @author Jan Becicka
 */
public class FindUsagesOpenAction extends AbstractAction {

    /**
     * Creates an instance of this action.
     */
    public FindUsagesOpenAction() {
        String name = NbBundle.getMessage(
                FindUsagesOpenAction.class,
                "LBL_UsagesWindow");                          //NOI18N
        putValue(NAME, name);
        putValue("iconBase", "org/netbeans/modules/refactoring/api/resources/findusages.png"); // NOI18N
    }
    
    /**
     * Opens and activates the Find Usages window.
     *
     * @param  e  event that caused this action to be called
     */
    public void actionPerformed(ActionEvent e) {
        RefactoringPanelContainer resultView = RefactoringPanelContainer.getUsagesComponent();
        resultView.open();
        resultView.requestActive();
    }
}
