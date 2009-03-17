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

package org.netbeans.modules.xml.xam.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 * Common utilities for XAM user interface module.
 *
 * @author Nam Nguyen
 * @author Nathan Fiedler
 */
public class XAMUtils {

    /**
     * Retrieve the XAM component for the given node.
     *
     * @param  node  node from which to acquire component.
     * @return  the model component, or null if none.
     */
    public static Component getComponent(Node node) {
        GetComponentCookie cake = (GetComponentCookie) node.getCookie(
                GetComponentCookie.class);
        Component component = null;
        try {
            if (cake != null) {
                component = cake.getComponent();
            }
        } catch (IllegalStateException ise) {
            // Happens if the component is no longer in the model.
            // Ignore this here since the caller will deal with it.
        }
        if (component == null) {
            component = (Component) node.getLookup().lookup(Component.class);
        }
        return component;
    }

    /**
     * Retrieve the cookie for showing the component in the editor.
     *
     * @param  comp   component to be shown.
     * @param  view   the desired view in which to show the component.
     * @return  the cookie to view the component.
     */
    public static ViewComponentCookie getViewCookie(Component comp,
            ViewComponentCookie.View view) {
        if (comp == null) {
            return null;
        }
        try {
            Model model = comp.getModel();
            if (model != null) {
                FileObject fobj = (FileObject) model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    DataObject dobj = DataObject.find(fobj);
                    if (dobj != null) {
                        ViewComponentCookie cake = (ViewComponentCookie) dobj.
                                getCookie(ViewComponentCookie.class);
                        if (cake != null && cake.canView(view, comp)) {
                            return cake;
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }

    /**
     * Determine if the given model is writable, which requires that the
     * source file also be writable.
     *
     * @param  model  the model to be tested.
     * @return  true if model source is editable and the source file is writable.
     */
    public static boolean isWritable(Model model) {
        if (model != null) {
            ModelSource ms = model.getModelSource();
            if (ms.isEditable()) {
                FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
                if (fo != null) {
                    return fo.canWrite();
                }
            }
        }
        return false;
    }

    /**
     * A wrapper listener. It guarantees that the event processing
     * will be executed in the Event Dispatch thread.
     *
     * WARNING! Hold the instance somewhere if you are going to wrap it
     * with the WeakListener. Otherwise it will be garbage collected.
     */
    public static class AwtPropertyChangeListener implements PropertyChangeListener {

        private PropertyChangeListener mListener;

        public AwtPropertyChangeListener(PropertyChangeListener listener) {
            mListener = listener;
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.propertyChange(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.propertyChange(evt);
                    }
                });
            }
        }

    }

    /**
     * A wrapper listener. It guarantees that the event processing
     * will be executed in the Event Dispatch thread.
     * 
     * WARNING! Hold the instance somewhere if you are going to wrap it
     * with the WeakListener. Otherwise it will be garbage collected.
     */
    public static class AwtComponentListener implements ComponentListener {

        private ComponentListener mListener;

        public AwtComponentListener(ComponentListener listener) {
            mListener = listener;
        }

        public void valueChanged(final ComponentEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.valueChanged(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.valueChanged(evt);
                    }
                });
            }
        }

        public void childrenAdded(final ComponentEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.childrenAdded(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.childrenAdded(evt);
                    }
                });
            }
        }

        public void childrenDeleted(final ComponentEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                mListener.childrenAdded(evt);
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mListener.childrenAdded(evt);
                    }
                });
            }
        }

    }

}
