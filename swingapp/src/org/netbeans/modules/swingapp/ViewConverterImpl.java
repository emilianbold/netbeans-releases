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

package org.netbeans.modules.swingapp;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import org.netbeans.modules.form.ViewConverter;

/**
 *
 */
public class ViewConverterImpl implements ViewConverter {

    public boolean canVisualize(Class componentClass) {
        return org.jdesktop.application.View.class.isAssignableFrom(componentClass);
    }

    public Convert convert(Object component, boolean root, boolean designRestrictions) {
        if (root && component instanceof org.jdesktop.application.View) {
            return new ConvertResult(
                    designRestrictions ? new AppDesignView() : new AppPreview(),
                    null);
        } else {
            return null;
        }
    }

    public static class AppDesignView extends JRootPane {
        public void setComponent(JComponent component) {
            getContentPane().add(component, BorderLayout.CENTER);
        }

        public void setToolBar(JToolBar toolBar) {
            getContentPane().add(toolBar, BorderLayout.PAGE_START);
        }

        public void setStatusBar(JComponent statusBar) {
            getContentPane().add(statusBar, BorderLayout.PAGE_END);
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
