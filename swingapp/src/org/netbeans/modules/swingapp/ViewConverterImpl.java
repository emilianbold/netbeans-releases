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

package org.netbeans.modules.swingapp;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import org.netbeans.modules.form.ViewConverter;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.form.ViewConverter.class)
public class ViewConverterImpl implements ViewConverter {

    public boolean canVisualize(Class componentClass) {
        return isViewClass(componentClass);
    }

    public Convert convert(Object component, boolean root, boolean designRestrictions) {
        if (root && component != null && isViewClass(component.getClass())) {
            return new ConvertResult(
                    designRestrictions ? new AppDesignView() : new AppPreview(),
                    null);
        } else {
            return null;
        }
    }

    private static boolean isViewClass(Class cls) {
        while (cls != null) {
            if (cls.getName().equals(org.jdesktop.application.View.class.getName())) {
                return true;
            } else {
                cls = cls.getSuperclass();
            }
        }
        return false;
    }

    public static class AppDesignView extends JRootPane {
        private JComponent component;
        private JToolBar toolBar;
        private JComponent statusBar;

        public void setComponent(JComponent component) {
            Container contentPane = getContentPane();
            if (this.component != null && this.component.getParent() == contentPane) {
                contentPane.remove(this.component);
            }
            this.component = component;
            if (component != null) {
                contentPane.add(component, BorderLayout.CENTER);
            }
        }

        public void setToolBar(JToolBar toolBar) {
            Container contentPane = getContentPane();
            if (this.toolBar != null && this.toolBar.getParent() == contentPane) {
                contentPane.remove(this.toolBar);
            }
            this.toolBar = toolBar;
            if (toolBar != null) {
                contentPane.add(toolBar, BorderLayout.PAGE_START);
            }
        }

        public void setStatusBar(JComponent statusBar) {
            Container contentPane = getContentPane();
            if (this.statusBar != null && this.statusBar.getParent() == contentPane) {
                contentPane.remove(this.statusBar);
            }
            this.statusBar = statusBar;
            if (statusBar != null) {
                contentPane.add(statusBar, BorderLayout.PAGE_END);
            }
        }
    }

    public static class AppPreview extends JFrame {
        public void setComponent(JComponent component) {
            getContentPane().add(component);
        }

        public void setMenuBar(JMenuBar menuBar) {
            setJMenuBar(menuBar);
        }

        public void setToolBar(JToolBar toolBar) {
            getContentPane().add(toolBar, BorderLayout.PAGE_START);
        }

        public void setStatusBar(JComponent statusBar) {
            getContentPane().add(statusBar, BorderLayout.PAGE_END);
        }
    }

    private static class ConvertResult implements ViewConverter.Convert {
        private Object converted;
        private Object enclosed;
        ConvertResult(Object converted, Object enclosed) {
            this.converted = converted;
            this.enclosed = enclosed;
        }
        public Object getConverted() {
            return converted;
        }
        public Object getEnclosed() {
            return enclosed;
        }
    }
}
