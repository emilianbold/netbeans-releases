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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.CasaCustomizer;
import org.netbeans.modules.compapp.casaeditor.graph.CasaFactory;
import org.netbeans.modules.compapp.casaeditor.properties.LookAndFeelProperty;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author jsandusky
 */
public class LookAndFeelNode extends CasaNode {

    private static final Image ICON = ImageUtilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/LookAndFeelNode.png"); // NOI18N


    public LookAndFeelNode() {
        super();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(LookAndFeelProperty.class, "LBL_LookAndFeel");        // NOI18N

    }

    @Override
    protected void setupPropertySheet(Sheet sheet) {
        Sheet.Set genericPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.GENERIC_SET);
        Sheet.Set stylePropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.STYLE_SET);
        Sheet.Set colorPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.COLOR_SET);
        Sheet.Set fontPropertySet = getPropertySet(sheet, PropertyUtils.PropertiesGroups.FONT_SET);

        sheet.put(genericPropertySet);
        sheet.put(stylePropertySet);
        sheet.put(colorPropertySet);
        sheet.put(fontPropertySet);

        for (String key : CasaFactory.getCasaCustomizer().getStylesMapReference().keySet()) {
            stylePropertySet.put(
                    new PropertySupport.ReadWrite<Boolean>(
                    key, // NO18N
                    Boolean.class,
                    NbBundle.getMessage(getClass(), key),
                    Constants.EMPTY_STRING) {

                        public Boolean getValue() {
                            return (Boolean) CasaFactory.getCasaCustomizer().getValue(getName());
                        }

                        public void setValue(Boolean value) {
                            CasaFactory.getCasaCustomizer().setValue(getName(), value);
                        }

                        @Override
                        public void restoreDefaultValue() {
                            CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
                            String strValue = customizer.getDefaultStyles().get(getName());
                            customizer.setValue(getName(), Boolean.parseBoolean(strValue));
//                            if (customizer.getDefaultStyles().containsKey(getName())) {
//                                String strValue = customizer.getDefaultStyles().get(getName());
//                                customizer.setValue(getName(), customizer.getBoolean(strValue));
//                            }
                        }

                        @Override
                        public boolean supportsDefaultValue() {
                            return true;
                        }
                    });
        }

        for (String key : CasaFactory.getCasaCustomizer().getColorsMapReference().keySet()) {
            colorPropertySet.put(
                    new PropertySupport.ReadWrite<Color>(
                    key, // NO18N
                    Color.class,
                    NbBundle.getMessage(getClass(), key),
                    Constants.EMPTY_STRING) {

                        public Color getValue() {
                            return (Color) CasaFactory.getCasaCustomizer().getValue(getName());
                        }

                        public void setValue(Color value) {
                            CasaFactory.getCasaCustomizer().setValue(getName(), value);
                        }

                        @Override
                        public void restoreDefaultValue() {
                            CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
                            String strValue = customizer.getDefaultColors().get(getName());
                            customizer.setValue(getName(), new Color(Integer.parseInt(strValue)));
//                            if (customizer.getDefaultGradients().containsKey(getName())) {
//                                String strValue = customizer.getDefaultGradients().get(getName());
//                                customizer.setValue(getName(), customizer.getGradient(strValue));
//                            }
                        }

                        @Override
                        public boolean supportsDefaultValue() {
                            return true;
                        }
                    });
        }

        for (String key : CasaFactory.getCasaCustomizer().getFontsMapReference().keySet()) {
            fontPropertySet.put(
                    new PropertySupport.ReadWrite<Font>(
                    key, // NO18N
                    Font.class,
                    NbBundle.getMessage(getClass(), key),
                    Constants.EMPTY_STRING) {

                        public Font getValue() {
                            return (Font) CasaFactory.getCasaCustomizer().getValue(getName());
                        }

                        public void setValue(Font value) {
                            CasaFactory.getCasaCustomizer().setValue(getName(), value);
                        }

                        @Override
                        public void restoreDefaultValue() {
                            CasaCustomizer customizer = CasaFactory.getCasaCustomizer();
                            String strValue = customizer.getDefaultFonts().get(getName());
                            customizer.setValue(getName(), customizer.getFont(strValue));
                        }

                        @Override
                        public boolean supportsDefaultValue() {
                            return true;
                        }
                    });
        }
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
}
