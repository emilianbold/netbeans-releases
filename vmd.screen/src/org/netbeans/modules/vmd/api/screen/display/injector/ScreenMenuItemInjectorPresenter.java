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
 *
 */

package org.netbeans.modules.vmd.api.screen.display.injector;

import org.netbeans.modules.vmd.api.io.PopupUtil;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public abstract class ScreenMenuItemInjectorPresenter extends ScreenInjectorPresenter {

    private String displayName;
    private int order;

    public ScreenMenuItemInjectorPresenter (String displayName, int order) {
        assert displayName != null;
        this.displayName = displayName;
        this.order = order;
    }

    public JComponent getViewComponent () {
        return new InjectorMenuItem (displayName);
    }

    public Integer getOrder () {
        return order;
    }

    protected abstract void actionPerformed ();


    private class InjectorMenuItem extends JMenuItem implements ActionListener {

        public InjectorMenuItem (String displayName) {
            super (displayName);
            // TODO - implement active aiming
            setRolloverEnabled (true);
            addActionListener (this);
        }

        public void actionPerformed (ActionEvent e) {
            DesignComponent component = ScreenMenuItemInjectorPresenter.this.getComponent ();
            component.getDocument ().getTransactionManager ().writeAccess (new Runnable () {
                public void run () {
                    ScreenMenuItemInjectorPresenter.this.actionPerformed ();
                }
            });
            PopupUtil.hidePopup ();
        }

    }

}
