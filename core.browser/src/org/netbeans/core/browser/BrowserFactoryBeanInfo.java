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

package org.netbeans.core.browser;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.*;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.util.Exceptions;


public class BrowserFactoryBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(BrowserFactory.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] properties;
        try {
            properties = new PropertyDescriptor [] {
                                new PropertyDescriptor(BrowserFactory.PROP_EXTRA_BROWSER, BrowserFactory.class)
                             };

            properties[0].setDisplayName("Extra Browser");
            properties[0].setShortDescription ("Extra browser for web development");
            properties[0].setPreferred(true);
            properties[0].setPropertyEditorClass(EBPropertyEditor.class);


        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
        return properties;
    }


    public static class EBPropertyEditor implements PropertyEditor {

        HtmlBrowser.Factory extraBrowser = BrowserFactory._getExtraBrowser();

        public EBPropertyEditor() {

        }

        public void setValue(Object value) {
            if( value instanceof HtmlBrowser.Factory ) {
                extraBrowser = (Factory) value;
            } else {
                extraBrowser = null;
            }
        }

        public Object getValue() {
            return extraBrowser;
        }

        public boolean isPaintable() {
            return false;
        }

        public void paintValue(Graphics gfx, Rectangle box) {
        }

        public String getJavaInitializationString() {
            return null;
        }

        public String getAsText() {
            return "";
        }

        public void setAsText(String text) throws IllegalArgumentException {
        }

        public String[] getTags() {
            return null;
        }

        private EmbeddedBrowserEditor editor;
        public Component getCustomEditor() {
            if( null == editor ) {
                editor = new EmbeddedBrowserEditor(this);
            }
            return editor;
        }

        public boolean supportsCustomEditor() {
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
