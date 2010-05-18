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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.faces.event.ActionEvent;

import com.sun.rave.faces.event.Action;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.Constants;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;


/**
 * BeanCreateInfo which creates a Group Panel with Grid Positioning
 *
 * @author Tor Norbye
 */
public class AbsGridBeanCreateInfo implements BeanCreateInfo {
    public AbsGridBeanCreateInfo() {
    }

    public String getBeanClassName() {
        return "org.netbeans.modules.visualweb.web.ui.dt.component.PanelGroup";
    }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignContext context = bean.getDesignContext();

        // Force to block (div)
        DesignProperty property = bean.getProperty("block");

        if (property != null) {
            property.setValue(Boolean.TRUE);
        }

        // Style
        property = bean.getProperty("style");

        if (property != null) {
            String s =
                "-rave-layout: grid; position: relative; background-color: white; border: solid 1px gray; width: 200px; height: 200px;";
            String style = (String)property.getValue();

            if ((style != null) && (style.length() > 0)) {
                s = s + style;
            }

            property.setValue(s);
        }

        return Result.SUCCESS;
    }

    public String getDisplayName() {
        return DesignMessageUtil.getMessage(AbsGridBeanCreateInfo.class, "absGrid.name");
    }

    public String getDescription() {
        return DesignMessageUtil.getMessage(AbsGridBeanCreateInfo.class, "absGrid.tip");
    }

    public Image getLargeIcon() {
        return null;
    }

    public Image getSmallIcon() {
        String iconFileName_C16 = "AbsGrid_C16";

        String name;
        name = iconFileName_C16;

        if (name == null) {
            return null;
        }

        Image image = TabGridBeanCreateInfo.loadImage(name + ".png");

        if (image == null) {
            image = TabGridBeanCreateInfo.loadImage(name + ".gif");
        }

        return image;

        //return null;
    }

    public String getHelpKey() {
        return null;
    }
}
