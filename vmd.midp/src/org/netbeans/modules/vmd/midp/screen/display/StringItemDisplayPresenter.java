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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.items.StringItemCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class StringItemDisplayPresenter extends ItemDisplayPresenter {

    private WrappedLabel label;
    //private int preferedHeight =-1;

    public StringItemDisplayPresenter() {
        label = label = new WrappedLabel(){

            @Override
            protected int getLabelWidth() {
                return (int)getView().getSize().getWidth();
            }

        };
        setContentComponent(label);
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        PropertyValue value = getComponent().readProperty(ItemCD.PROP_APPEARANCE_MODE);        
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            int appearanceMode = MidpTypes.getInteger(value);
            label.setBorder(appearanceMode == ItemCD.VALUE_BUTTON ? BorderFactory.createRaisedBevelBorder() : null);
            label.setForeground(appearanceMode == ItemCD.VALUE_HYPERLINK ? Color.BLUE : UIManager.getDefaults().getColor("Label.foreground")); // NOI18N
        }
        String text = null;
        if (MidpDatabindingSupport.getConnector(getComponent(), StringItemCD.PROP_TEXT) != null) {
            text = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/midp/screen/display/Bundle").getString("LBL_Databinding"); //NOI18N 
        } else {
            text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(StringItemCD.PROP_TEXT));
        }
        label.setText(text);

        value = getComponent().readProperty(StringItemCD.PROP_FONT);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            DesignComponent font = value.getComponent();
            label.setFont(ScreenSupport.getFont(deviceInfo, font));
        }

        int width = Integer.parseInt( getComponent().readProperty(
                ItemCD.PROP_PREFERRED_WIDTH).getPrimitiveValue().toString());
        label.setPreferedWidth(width);

        label.repaint();
        label.revalidate();

        int prefHeight = Integer.parseInt(getComponent().readProperty(
                ItemCD.PROP_PREFERRED_HEIGHT).getPrimitiveValue().toString());
        if ( prefHeight != -1 ){
            Dimension dimension = getView().getPreferredSize();
            /*if ( preferedHeight == -1){
                preferedHeight = (int)dimension.getHeight();
            }*/
            getView().setPreferredSize( new Dimension ((int)dimension.getWidth(),
                    prefHeight));
        }
        else {
            /*if (preferedHeight != -1) {
            Dimension dimension = getView().getPreferredSize();
            getView().setPreferredSize(new Dimension((int) dimension.getWidth(),
                    preferedHeight));*/
            getView().setPreferredSize(null);
        }
        getView().revalidate();
        getView().invalidate();
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ArrayList<ScreenPropertyDescriptor> list = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        list.add(new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(StringItemCD.PROP_TEXT)));
        return list;
    }
}
