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

package org.netbeans.modules.vmd.midp.screen.display.injector;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.injector.ScreenInjectorPresenter;
import org.netbeans.modules.vmd.api.io.PopupUtil;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public class ScreenBooleanInjectorPresenter extends ScreenInjectorPresenter {

    private String propertyName;
    private String displayName;
    private int order;

    public ScreenBooleanInjectorPresenter (String propertyName, String displayName, int order) {
        this.order = order;
        assert propertyName != null &&  displayName != null;
        this.propertyName = propertyName;
        this.displayName = displayName;
    }

    public boolean isEnabled () {
        return true;
    }

    public JComponent getViewComponent () {
        InjectorCheckBox checkBox = new InjectorCheckBox (displayName);
        PropertyValue propertyValue = getComponent ().readProperty (propertyName);
        if (propertyValue.getKind () == PropertyValue.Kind.VALUE)
            checkBox.setSelected (MidpTypes.getBoolean (propertyValue));
        else
            checkBox.setEnabled (false);
        return checkBox;
    }

    public Integer getOrder () {
        return order;
    }

    private class InjectorCheckBox extends JCheckBox implements ActionListener {

        public InjectorCheckBox (String text) {
            super (text);
            addActionListener (this);
        }

        public void actionPerformed (ActionEvent e) {
            final DesignComponent comp = ScreenBooleanInjectorPresenter.this.getComponent ();
            comp.getDocument ().getTransactionManager ().writeAccess (new Runnable () {
                public void run () {
                    comp.writeProperty (propertyName, MidpTypes.createBooleanValue (InjectorCheckBox.this.isSelected ()));
                }
            });
            PopupUtil.hidePopup ();
        }
    }

}
