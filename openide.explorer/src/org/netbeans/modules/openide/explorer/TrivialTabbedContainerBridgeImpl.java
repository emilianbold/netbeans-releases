/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.openide.explorer;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import java.awt.*;

/**
 * Trivial implementation of TabbedContainerBridge for use with unit tests, etc.
 * Does not actually support changing tabs, this is just so things link and do not
 * throw NPEs.
 * <p>
 * Given sufficient interest, a JTabbedPane implementation could be provided,
 * though there are some non-trivial difficulties getting a JTabbedPane to show
 * the same component for all tabs, and the technique that worked on 1.4 does not
 * work on 1.5.
 *
 */
public class TrivialTabbedContainerBridgeImpl extends TabbedContainerBridge {
    public TrivialTabbedContainerBridgeImpl() {

    }

    public JComponent createTabbedContainer() {
        JPanel result = new JPanel();
        result.setLayout (new BorderLayout());
        result.putClientProperty ("titles", new String[0]);
        result.putClientProperty ("items", new Object[0]);
        return result;
    }

    public void setInnerComponent(JComponent container, JComponent inner) {
        if (container.getComponentCount() > 0) {
            container.removeAll();
        }
        container.add (inner, BorderLayout.CENTER);
    }

    public JComponent getInnerComponent(JComponent jc) {
        JComponent result = null;
        if (jc.getComponentCount() > 0 && jc.getComponent(0) instanceof JComponent) {
            result = (JComponent) jc.getComponent(0);
        }
        return result;
    }

    public Object[] getItems(JComponent jc) {
        return new Object[0];
    }

    public void setItems(JComponent jc, Object[] objects, String[] titles) {
        jc.putClientProperty ("items", objects);
        jc.putClientProperty ("titles", titles);
    }

    public void attachSelectionListener(JComponent jc, ChangeListener listener) {
        //do nothing
    }

    public void detachSelectionListener(JComponent jc, ChangeListener listener) {
        //do nothing
    }

    public Object getSelectedItem(JComponent jc) {
        Object[] items = (Object[]) jc.getClientProperty ("items");
        if (items != null && items.length > 0) {
            return items[0];
        }
        return null;
    }

    public void setSelectedItem(JComponent jc, Object selection) {
        //do nothing
    }

    public boolean setSelectionByName(JComponent jc, String tabname) {
        return false;
    }

    public String getCurrentSelectedTabName(JComponent jc) {
        String[] titles = (String[]) jc.getClientProperty("titles");
        if (titles != null && titles.length > 0) {
            return titles[0];
        }
        return ""; //NOI18N
    }
}
