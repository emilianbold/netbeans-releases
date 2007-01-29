/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * BeanCreateInfo which creates a Tab with an embedded Grid Positioning
 * Panel
 *
 * @author Tor Norbye
 */
public class TabGridBeanCreateInfo implements BeanCreateInfo {
    public TabGridBeanCreateInfo() {
    }

    public String getBeanClassName() {
        return "org.netbeans.modules.visualweb.web.ui.dt.component.Tab";
    }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignContext context = bean.getDesignContext();
        DesignBean panel =
            context.createBean("org.netbeans.modules.visualweb.web.ui.dt.component.PanelGroup", bean, new Position());

        if (panel == null) {
            return Result.FAILURE;
        }

        // Force to block (div)
        DesignProperty property = panel.getProperty("block");

        if (property != null) {
            property.setValue(Boolean.TRUE);
        }

        // Style
        property = panel.getProperty("style");

        if (property != null) {
            String s =
                "-rave-layout: grid; position: relative; background-color: white; border: solid 1px gray; height: 200px;";
            String style = (String)property.getValue();

            if ((style != null) && (style.length() > 0)) {
                s = s + style;
            }

            property.setValue(s);
        }

        return Result.SUCCESS;
    }

    public String getDisplayName() {
        return DesignMessageUtil.getMessage(TabGridBeanCreateInfo.class, "tabPanel.name");
    }

    public String getDescription() {
        return DesignMessageUtil.getMessage(TabGridBeanCreateInfo.class, "tabPanel.tip");
    }

    public Image getLargeIcon() {
        return null;
    }

    // XXX Copied from java.beans.BeanInfo.
    public static java.awt.Image loadImage(final String resourceName) {
        try {
            //final Class c = getClass();
            final Class c = TabGridBeanCreateInfo.class;
            java.awt.image.ImageProducer ip =
                (java.awt.image.ImageProducer)java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                        public Object run() {
                            java.net.URL url;

                            if ((url = c.getResource(resourceName)) == null) {
                                return null;
                            } else {
                                try {
                                    return url.getContent();
                                } catch (java.io.IOException ioe) {
                                    return null;
                                }
                            }
                        }
                    });

            if (ip == null) {
                return null;
            }

            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();

            return tk.createImage(ip);
        } catch (Exception ex) {
            return null;
        }
    }

    public Image getSmallIcon() {
        String iconFileName_C16 = "Tab_C16";

        String name;
        name = iconFileName_C16;

        if (name == null) {
            return null;
        }

        Image image = loadImage(name + ".png");

        if (image == null) {
            image = loadImage(name + ".gif");
        }

        return image;

        //return null;
    }

    public String getHelpKey() {
        return null;
    }
}
