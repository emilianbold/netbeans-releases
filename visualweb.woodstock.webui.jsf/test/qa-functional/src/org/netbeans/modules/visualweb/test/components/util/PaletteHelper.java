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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * PaletteHelper.java
 *
 * Created on February 20, 2008, 9:00 AM
 */
package org.netbeans.modules.visualweb.test.components.util;

import java.awt.Point;
import java.util.Properties;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;

/**
 *
 * @author Martin.Schovanek@sun.com
 */
public enum PaletteHelper {

    ADVANCED(Const.JSF_BUNDLE, Const.JSF_ADVACED_PREFIX, Const.PALETTE_PREFIX + "Advanced"),
    ADVANCED_DATA_PROVIDERS(Const.DATA_BUNDLE, Const.DATA_PREFIX, Const.PALETTE_PREFIX + "AdvancedDataProviders"),
    BASIC(Const.WS_BUNDLE, Const.WS_PREFIX, Const.PALETTE_PREFIX + "Basic"),
    COMPOSITE(Const.WS_BUNDLE, Const.WS_PREFIX, Const.PALETTE_PREFIX + "Composite"),
    CONVERTERS(Const.JSF_BUNDLE, Const.JSF_CONVERTERS_PREFIX, Const.PALETTE_PREFIX + "Converters"),
    DATA_PROVIDERS(Const.DATA_BUNDLE, Const.DATA_PREFIX, Const.PALETTE_PREFIX + "DataProviders"),
    LAYOUT(Const.WS_BUNDLE, Const.WS_PREFIX, Const.PALETTE_PREFIX + "Layout"),
    STANDARD(Const.JSF_BUNDLE, Const.JSF_STANDARD_PREFIX, Const.PALETTE_PREFIX + "Standard"),
    VALIDATORS(Const.JSF_BUNDLE, Const.JSF_VALIDATORS_PREFIX, Const.PALETTE_PREFIX + "Validators");
    
    // Resouurce Bundle name
    private final String bundle;
    // components keys prefix
    private final String compPrefix;
    // palete display name
    private final String palette;

    PaletteHelper(String bundle, String prefix, String paletteKey) {
        this.bundle = bundle;
        this.compPrefix = prefix;
        this.palette = getString(bundle, paletteKey);
    }
    
    /**
     * Appends the specified component on [x,y] position and sets approriate text
     * properties.  
     * 
     * @param componentId 'compPrefix' + this value should be a valid 'bundle' key.
     * @param x x coordinate
     * @param y y coordinate
     * @param properties String[n][2] array, where n is nuber of properties,
     * [n][0] - is property name and [n][1] - is it value. 
     */
    public void addComponent(String componentId, int x, int y, String properties) {
        String component = getString(bundle, compPrefix + componentId);
        log("** Adding component " + palette + " > " + component);
        DesignerPaneOperator designerOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator paletteOp = new PaletteContainerOperator(palette);
        paletteOp.addComponent(component, designerOp, new Point(x, y));
        if (properties != null) {
            Properties prop = ComponentUtils.parseProperties(properties);
            SheetTableOperator sheet = new SheetTableOperator();
            for (Object key : prop.keySet()) {
                String val = prop.getProperty((String) key);
                log("** Setting property: " + key + "=" + val);
                sheet.setTextValue((String) key, val);
            }
        }
    }

    private String getString(String bundle, String key) {
        String str = null;
        try {
            str = Bundle.getStringTrimmed(bundle, key);
        } catch (JemmyException je) {
            throw new JemmyException("Key: "+key+" was not found in: "+bundle, je);
        }
        return str;
    }

    private void log(String msg) {
        System.err.println(msg);
    }

    private class Const {

        static final String DATA_BUNDLE =
                "org.netbeans.modules.visualweb.dataprovider.designtime.resources.Bundle";
        static final String DATA_PREFIX = "NAME_com-sun-webui-jsf-component-";
        static final String JSF_BUNDLE =
                "org.netbeans.modules.visualweb.jsfsupport.designtime.resources.Bundle";
        static final String JSF_ADVACED_PREFIX =
                "NAME_org-netbeans-modules-visualweb-faces-dt-component-";
        static final String JSF_CONVERTERS_PREFIX =
                "NAME_org-netbeans-modules-visualweb-faces-dt-converter-";
        static final String JSF_STANDARD_PREFIX =
                "NAME_org-netbeans-modules-visualweb-faces-dt-component-html-";
        static final String JSF_VALIDATORS_PREFIX =
                "NAME_org-netbeans-modules-visualweb-faces-dt-validator-";
        static final String PALETTE_PREFIX = "CreatorDesignerPalette5/";
        static final String WS_BUNDLE =
                "org.netbeans.modules.visualweb.woodstock.webui.jsf.designtime.resources.Bundle";
        static final String WS_PREFIX = "NAME_com-sun-webui-jsf-component-";
    };
}
    


