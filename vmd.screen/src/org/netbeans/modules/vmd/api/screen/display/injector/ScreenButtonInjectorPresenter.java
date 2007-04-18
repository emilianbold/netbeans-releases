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

import org.openide.util.Utilities;
import org.netbeans.modules.vmd.api.io.PopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public abstract class ScreenButtonInjectorPresenter extends ScreenInjectorPresenter {

    public ScreenButtonInjectorPresenter () {
    }

    protected abstract String getDisplayName ();
    
    protected abstract Image getIcon ();

    public abstract boolean isEnabled ();

    protected abstract void actionPerformed ();

    public final JComponent getViewComponent () {
        return new InjectorButton (getIcon ());
    }

    private class InjectorButton extends JButton implements ActionListener {

        public InjectorButton (Image image) {
            super (getDisplayName (), image != null ? new ImageIcon (image) : null);
            setEnabled (isEnabled ());
            addActionListener (this);
        }

        public void actionPerformed (ActionEvent e) {
            ScreenButtonInjectorPresenter.this.actionPerformed ();
            PopupUtil.hidePopup ();
        }

    }

    public static abstract class Static extends ScreenButtonInjectorPresenter {

        private String displayName;
        private Image image;
        private int order;

        public Static (String displayName, String imageResource, int order) {
            this.displayName = displayName;
            this.image = imageResource != null ? Utilities.loadImage (imageResource) : null;
            this.order = order;
        }

        public String getDisplayName () {
            return displayName;
        }

        public Image getIcon () {
            return image;
        }

        public boolean isEnabled () {
            return true;
        }

        public Integer getOrder () {
            return order;
        }

    }

}
