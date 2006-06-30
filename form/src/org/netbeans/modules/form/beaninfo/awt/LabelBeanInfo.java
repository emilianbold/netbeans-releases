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

import java.awt.Label;
import java.beans.*;

import org.openide.util.NbBundle;

/** A BeanInfo for java.awt.Label.
*
* @author Ales Novak

*/
public class LabelBeanInfo extends ComponentBeanInfo.Support {

    public LabelBeanInfo() {
        super("label", java.awt.Label.class); // NOI18N
    }

    /** @return Propertydescriptors */
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            new PropertyDescriptor("alignment", Label.class), // NOI18N
            new PropertyDescriptor("text", Label.class), // NOI18N
        };
        pds[0].setPropertyEditorClass(LabelBeanInfo.AlignmentPropertyEditor.class);
        return pds;
    }

    public static class AlignmentPropertyEditor extends PropertyEditorSupport {
        String[] tags;

        /** @return tags */
        public synchronized String[] getTags() {
            if (tags == null) {
                tags = new String[] {
                    getString("LEFT"),
                    getString("CENTER"),
                    getString("RIGHT")
                };
            }
            return tags;
        }

        public void setAsText(String s) {
            Integer i;
            getTags();
            if (s.equals(tags[0])) i = new Integer(java.awt.Label.LEFT);
            else if (s.equals(tags[1])) i = new Integer(java.awt.Label.CENTER);
            else i = new Integer(java.awt.Label.RIGHT);
            setValue(i);
        }

        public String getAsText() {
            int i = ((Integer) getValue()).intValue();
            getTags();
            return tags[i == java.awt.Label.CENTER ? 1 : (i == java.awt.Label.LEFT ? 0 : 2)];
        }

        public String getJavaInitializationString () {
            int i = ((Integer) getValue()).intValue();
            switch (i) {
            case java.awt.Label.RIGHT :  return "java.awt.Label.RIGHT"; // NOI18N
            case java.awt.Label.LEFT :   return "java.awt.Label.LEFT"; // NOI18N
            default:
            case java.awt.Label.CENTER : return "java.awt.Label.CENTER"; // NOI18N
            }
        }
    }

    /** i18n */
    static String getString(String x) {
        return NbBundle.getMessage(LabelBeanInfo.class, x);
    }
}
