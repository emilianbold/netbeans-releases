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

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.openide.WizardDescriptor;

/**
 *
 * @author  mkuchtiak
 */
public class Utilities {
    
    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                Component component = panels[i - before.length + diff].getComponent();
                res[i] = component != null ? component.getName() : "...";
            }
        }
        return res;
    }
    
    public static void replaceInDocument(javax.swing.text.Document document, String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex){}
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static JTextComponent findTextFieldForLabel(JComponent component, String text) {
        JLabel label = findLabel(component, text);
        if(label != null) {
            Component comp = label.getLabelFor();
            if (comp!=null && (comp instanceof JTextComponent)) return (JTextComponent)comp;
        }
        return null;
    }

    private static JLabel findLabel(JComponent component, String text) {
        Component[] components = component.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                if (((JLabel) comp).getText().equals(text)) {
                    return (JLabel) comp;
                }
            } else if (comp instanceof JComponent){
                return findLabel((JComponent) comp, text);
            }
        }
        return null;
    }
    
    
    public static String[] createSteps(String[] before) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff)];
        for (int i = 0; i < res.length; i++) {
            res[i] = before[i];
        }
        return res;
    }
}
