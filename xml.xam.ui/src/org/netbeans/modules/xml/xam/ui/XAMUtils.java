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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui;

import org.netbeans.modules.xml.xam.Component;
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
}
