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

package org.netbeans.modules.visualweb.gravy.model.project.components;

import org.netbeans.modules.visualweb.gravy.model.project.WebPage;
import org.netbeans.modules.visualweb.gravy.model.components.*;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;

import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.JemmyException;

import java.awt.Container;
import javax.swing.JLabel;
import java.util.Hashtable;

/**
 * Common class for all components on web page.
 */

public class Component {

    final static String OS_MAC = "Mac OS X";
    final static String OS_SOL = "SunOS";
    final static String OS_WIN = "Windows";
    final static String OS_LIN = "Linux";
    final String propName = "id";
    public WebPage Page;
    protected String Type = "";
    
    
    /**
     * Component's properties.
     */
    private Hashtable properties = new Hashtable();
            
    /**
     * Create component on web page using web component from palette.
     * @param webcomponent Web component from palette.
     * @param name Name of component.
     */
    public Component(WebComponent webcomponent, String name) {
        Type =  webcomponent.getID();
        properties.put(propName, name);
    }
    
    /**
     * Get name of component.
     * @return Name of component.
     */
    public String getName() {
        return ((String) getProperty(propName));
    }
    
    /**
     * Get type of component.
     * @return Type of component.
     */
    public String getType() {
        return Type;
    }

    /**
     * Return property value.
     * @param name Name of property.
     * @return Property value.
     */
    public String getProperty(String name) {
        return properties.get(name).toString();
    }
    
    /**
     * Set property value.
     * @param name Name of property.
     * @param value Value of property.
     */
    public void setProperty(String name, String value) {
        Page.open();
        DocumentOutlineOperator outline = new DocumentOutlineOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        JTreeOperator aotree = outline.getStructTreeOperator();
        try {
            aotree.selectPath(aotree.findPath(Page.getName() + "|page1|html1|body1|form1|" + getName()));
        }
        catch(Exception e) {
            throw new JemmyException("Path " + Page.getName() + "|page1|html1|body1|form1|" + getName() + " can't be selected!", e);
        }
        TestUtils.wait(1000);
        try {
            SheetTableOperator sheet = new SheetTableOperator();
            String class_0 = ((Container) sheet.getRenderedComponent(sheet.findCell("name:"+name, 0).y, 1)).getComponent(0).getClass().getName();
            String class_1 = ((Container) sheet.getRenderedComponent(sheet.findCell("name:"+name, 0).y, 1)).getComponent(1).getClass().getName();
            String propName = ((JLabel) sheet.getRenderedComponent(sheet.findCell("name:"+name, 0).y, 0)).getText();
            if (class_1.equals("org.openide.explorer.propertysheet.RendererFactory$StringRenderer")) sheet.setTextValue(propName, value);
            if (class_1.equals("org.openide.explorer.propertysheet.RendererFactory$CheckboxRenderer")) sheet.setCheckBoxValue(propName, value);
            if (class_1.equals("org.openide.explorer.propertysheet.RendererFactory$ComboboxRenderer") || 
               (class_0.indexOf("AquaComboBoxButton") != -1 && System.getProperty("os.name").equals(OS_MAC)) || 
               (class_0.indexOf("javax.swing.JButton") != -1 && System.getProperty("os.name").equals(OS_SOL)) || 
               (class_0.indexOf("WindowsComboBoxUI") != -1 && class_0.indexOf("ComboBoxButton") != -1 && System.getProperty("os.name").indexOf(OS_WIN) != -1) || 
               (class_0.indexOf("javax.swing.JButton") != -1 && System.getProperty("os.name").equals(OS_LIN))) sheet.setComboBoxValue(propName, value);
        }
        catch(Exception e) {
            throw new JemmyException("Property can't be set!", e);
        }
        properties.put(name, value);
    }
}
