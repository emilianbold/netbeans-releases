/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
