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

package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.items.ChoiceGroupCD;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ChoiceGroupDisplayPresenter extends ItemDisplayPresenter {

//    private static final Border POPUP_BORDER = BorderFactory.createBevelBorder(BevelBorder.RAISED);
//    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
    private JPanel panel;

    public ChoiceGroupDisplayPresenter() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        setContentComponent(panel);
    }

    @Override
    public Collection<DesignComponent> getChildren() {
        PropertyValue elementsValue = getComponent().readProperty(ChoiceGroupCD.PROP_ELEMENTS);
        ArrayList<DesignComponent> elements = new ArrayList<DesignComponent>();
        Debug.collectAllComponentReferences(elementsValue, elements);

//        PropertyValue typeValue = getComponent().readProperty(ChoiceGroupCD.PROP_CHOICE_TYPE);
//        if (!PropertyValue.Kind.USERCODE.equals(typeValue.getKind())) {
//            int type = MidpTypes.getInteger(typeValue);
//            if (type == ChoiceSupport.VALUE_POPUP && elements.size() > 0) {
//                return elements.subList(0, 1);
//            }
//        }
        return elements;
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

//        PropertyValue value = getComponent().readProperty(ChoiceGroupCD.PROP_CHOICE_TYPE);
//        if (!PropertyValue.Kind.USERCODE.equals(value.getKind()) && MidpTypes.getInteger(value) == ChoiceSupport.VALUE_POPUP) {
//            panel.setBorder(POPUP_BORDER);
//        } else {
//            panel.setBorder(EMPTY_BORDER);
//        }

        panel.removeAll();
        for (DesignComponent item : getChildren()) {
            ChoiceElementDisplayPresenter presenter = item.getPresenter(ChoiceElementDisplayPresenter.class);
            if (presenter == null) {
                continue;
            }
            panel.add(presenter.getView());
            presenter.reload(deviceInfo);
        }
        panel.add(Box.createVerticalGlue());
    }
}
