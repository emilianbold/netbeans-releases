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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.beaninfo.awt;

import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import java.awt.Scrollbar;

/** A BeanInfo for java.awt.ScrollBar.
*
* @author Ales Novak
*/
public class ScrollbarBeanInfo extends ComponentBeanInfo.Support {

    public ScrollbarBeanInfo() {
        super("scrollbar", java.awt.Scrollbar.class); // NOI18N
    }

    /** @return Propertydescriptors */
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            new PropertyDescriptor("unitIncrement", Scrollbar.class), // NOI18N
            new PropertyDescriptor("minimum", Scrollbar.class), // NOI18N
            new PropertyDescriptor("maximum", Scrollbar.class), // NOI18N
            new PropertyDescriptor("value", Scrollbar.class), // NOI18N
            new PropertyDescriptor("blockIncrement", Scrollbar.class), // NOI18N
            new PropertyDescriptor("orientation", Scrollbar.class), // NOI18N
            new PropertyDescriptor("visibleAmount", Scrollbar.class), // NOI18N
        };
        pds[5].setPropertyEditorClass(ScrollbarBeanInfo.OrientationPropertyEditor.class);
        return pds;
    }

    /** orientation PropertyEditor */
    public static class OrientationPropertyEditor extends PropertyEditorSupport {
        String[] tags;
        
        /** @return tags */
        public synchronized String[] getTags() {
            if (tags == null) {
                ResourceBundle rb = NbBundle.getBundle(ScrollbarBeanInfo.class);
                tags = new String[] {
                    rb.getString("HORIZONTAL"),
                    rb.getString("VERTICAL"),
                };
            }
            return tags;
        }

        public void setAsText(String s) {
            Integer i;
            getTags();
            if (s.equals(tags[0])) i = new Integer(Scrollbar.HORIZONTAL);
            else i = new Integer(Scrollbar.VERTICAL);
            setValue(i);
        }

        public String getAsText() {
            int i = ((Integer) getValue()).intValue();
            getTags();
            return i == Scrollbar.VERTICAL ? tags[1] : tags[0];
        }

        public String getJavaInitializationString() {
            int i = ((Integer) getValue()).intValue();
            return i == Scrollbar.VERTICAL ? "java.awt.Scrollbar.VERTICAL" : "java.awt.Scrollbar.HORIZONTAL"; // NOI18N
        }
    }
}
