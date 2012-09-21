/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
/**
 * JComboBox with auto completion feature.
 *
 * @author marekfukala
 */

package org.netbeans.modules.css.visual;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class AutocompleteJComboBox extends JComboBox {

    private static final Logger LOGGER = Logger.getLogger(AutocompleteJComboBox.class.getSimpleName());
    
    private boolean popupCancelled;

    public AutocompleteJComboBox(ComboBoxModel model, ObjectToStringConverter objectToStringConverter) {
        super(model);
        initialize(objectToStringConverter);
    }

    private void initialize(ObjectToStringConverter converter) {
        AutoCompleteDecorator.decorate(this, converter);

        //At least on Mac and Windows XP L&F, the decorated JComboBox doesn't not fire
        //ActionEvent when the value is confirmed.
        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                popupCancelled = false;
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (!popupCancelled) {
                    superFireActionEvent();
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                popupCancelled = false;
            }
        });

    }

    @Override
    protected void fireActionEvent() {
        LOGGER.log(Level.FINE, "fireActionEvent()", new Exception());
        LOGGER.log(Level.FINE, "popup opened: {0}", isPopupVisible());
            
        //On the contrary to the ActionEvent not being fired upon entering the value,
        //each keystroke causes an ActionEvent to be fired!!!
        //So filter out such action events and fire the action event only when the popup is closed
        //and it has not been cancelled.
    }

    private void superFireActionEvent() {
        //really fire the action event once the edited value is confirmed
        super.fireActionEvent();
    }

}
