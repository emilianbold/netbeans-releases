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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.swingapp;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JRootPane;
import org.netbeans.modules.form.ComponentConverter;

/**
 *
 */
public class ComponentConverterImpl implements ComponentConverter {

    public Class getDesignClass(String componentClassName) {
        return null;
    }

    public Class getDesignClass(Class componentClass) {
        if (org.jdesktop.application.View.class.isAssignableFrom(componentClass)) {
            return FrameView.class;
        } else {
            return null;
        }
    }

    public static class FrameView extends org.jdesktop.application.FrameView {
        private JRootPane rootPane;

        public FrameView() {
            super(new org.jdesktop.application.Application() { protected void startup() {} });
        }

        public JRootPane getRootPane() {
            if (rootPane == null) {
                rootPane = new JRootPane();
            }
            return rootPane;
        }
    }

    public static class FrameViewBeanInfo extends SimpleBeanInfo {
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return new PropertyDescriptor[] {
                    new PropertyDescriptor("component", FrameView.class), // NOI18N
                    new PropertyDescriptor("menuBar", FrameView.class), // NOI18N
                    new PropertyDescriptor("toolBar", FrameView.class), // NOI18N
                    new PropertyDescriptor("statusBar", FrameView.class) // NOI18N
                };
            } catch (IntrospectionException ex) {
                Logger.getLogger(ComponentConverterImpl.class.getName()).log(Level.INFO, null, ex);
                return null;
            }
        }
    }
}
