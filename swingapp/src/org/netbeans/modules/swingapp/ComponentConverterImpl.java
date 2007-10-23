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
import org.netbeans.modules.form.FormUtils;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class ComponentConverterImpl implements ComponentConverter {

    public Class getDesignClass(String componentClassName, FileObject fileFromProject) {
        return null;
    }

    public Class getDesignClass(Class componentClass, FileObject fileFromProject) {
        if (isViewClass(componentClass)) {
            try {
                // Our FrameView class is registered to be loaded by system
                // classloader also including the project classpath.
                return FormUtils.loadClass(FrameView.class.getName(), fileFromProject);
            } catch (Exception ex) {
                Logger.getLogger(ComponentConverterImpl.class.getName()).log(Level.INFO, null, ex);
            } catch (LinkageError ex) {
                Logger.getLogger(ComponentConverterImpl.class.getName()).log(Level.INFO, null, ex);
            }
        }
        return null;
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

    public static class FrameView extends org.jdesktop.application.FrameView {
        private JRootPane rootPane;

        public FrameView() {
            // Ideally we would use the actual application class from the project,
            // but it may not be compiled or instantiable. We should not call
            // Application.getInstance() either - that would make the wrong
            // instance remembered in Application's static field.
            super(new PlaceholderApp());
        }

        @Override
        public JRootPane getRootPane() {
            if (rootPane == null) {
                rootPane = new JRootPane();
            }
            return rootPane;
        }
    }

    public static class FrameViewBeanInfo extends SimpleBeanInfo {
        @Override
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

    private static class PlaceholderApp extends org.jdesktop.application.Application {
        protected void startup() {
        }
    }
}
