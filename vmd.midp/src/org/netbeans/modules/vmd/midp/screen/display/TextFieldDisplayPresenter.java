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

import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class TextFieldDisplayPresenter extends ItemDisplayPresenter {
    
    private static final Border LABEL_BORDER = BorderFactory.createLineBorder(Color.GRAY);
    
    private JLabel label;

    public TextFieldDisplayPresenter() {
        label = new JLabel();
        label.setBorder(LABEL_BORDER);
        setContentComponent(label);
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        /*
         * Fix for IZ#147913 - Values of screen elements bound to dataset are displayed inconsistently
         */
        String text = null;
        if (MidpDatabindingSupport.getConnector(getComponent(), TextFieldCD.PROP_TEXT) != null) {
            text = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/screen/display/Bundle").getString("LBL_Databinding"); //NOI18N
        } else {
            text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(TextFieldCD.PROP_TEXT));
        }
        label.setText(text);
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors () {
        ArrayList<ScreenPropertyDescriptor> list = new ArrayList<ScreenPropertyDescriptor> (super.getPropertyDescriptors ());
        list.add (new ScreenPropertyDescriptor (getComponent (), label, new ScreenStringPropertyEditor (TextFieldCD.PROP_TEXT)));
        return list;
    }

}
