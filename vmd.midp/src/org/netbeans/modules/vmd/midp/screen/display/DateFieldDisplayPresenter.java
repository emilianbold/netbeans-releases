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
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.DateFieldCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.items.StringItemCD;

/**
 *
 * @author David Kaspar
 */
public class DateFieldDisplayPresenter extends ItemDisplayPresenter {

    private JLabel label;

    public DateFieldDisplayPresenter() {
        label = new JLabel();
        setContentComponent(label);
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        PropertyValue inputModeValue = getComponent().readProperty(DateFieldCD.PROP_INPUT_MODE);
        PropertyValue dateValue = getComponent().readProperty(DateFieldCD.PROP_DATE);
        String text = NbBundle.getMessage(DateFieldDisplayPresenter.class, "DISP_user_input_mode"); // NOI18N
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        if (inputModeValue.getKind() == PropertyValue.Kind.VALUE) {
            int inputMode = MidpTypes.getInteger(inputModeValue);
            switch (inputMode) {
                case DateFieldCD.VALUE_DATE:
                    text = NbBundle.getMessage(DateFieldDisplayPresenter.class, "DISP_user_date"); // NOI18N
                    format = DateFormat.getDateInstance(DateFormat.MEDIUM);
                    break;
                case DateFieldCD.VALUE_DATE_TIME:
                    text = NbBundle.getMessage(DateFieldDisplayPresenter.class, "DISP_user_date_time"); // NOI18N
                    break;
                case DateFieldCD.VALUE_TIME:
                    text = NbBundle.getMessage(DateFieldDisplayPresenter.class, "DISP_user_time"); // NOI18N
                    format = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                    break;
            }
        }

        if (MidpDatabindingSupport.getConnector(getComponent(), DateFieldCD.PROP_DATE) != null) {
            text = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/screen/display/Bundle").getString("LBL_Databinding"); //NOI18N 
        } else if (dateValue.getKind() == PropertyValue.Kind.VALUE) {
            text = format.format(new Date(MidpTypes.getLong(dateValue)));
        }
        label.setText(text);
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ResourcePropertyEditor dateFieldPropertyEditor = new ResourcePropertyEditor(DateFieldCD.PROP_DATE, getComponent());
        List<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>();
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), label, dateFieldPropertyEditor));
        descriptors.addAll(super.getPropertyDescriptors());
        return descriptors;
    }
}
