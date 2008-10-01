/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.api.screen.display.injector;

import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.netbeans.modules.vmd.api.io.PopupUtil;
import org.netbeans.modules.vmd.api.model.DesignComponent;

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
            DesignComponent component = ScreenButtonInjectorPresenter.this.getComponent ();
            component.getDocument ().getTransactionManager ().writeAccess (new Runnable () {
                public void run () {
                    ScreenButtonInjectorPresenter.this.actionPerformed ();
                }
            });
            PopupUtil.hidePopup ();
        }

    }

    public static abstract class Static extends ScreenButtonInjectorPresenter {

        private String displayName;
        private Image image;
        private int order;

        public Static (String displayName, String imageResource, int order) {
            this.displayName = displayName;
            this.image = imageResource != null ? ImageUtilities.loadImage (imageResource) : null;
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
