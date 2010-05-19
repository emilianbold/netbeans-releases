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

/*
 * Created on Jul 6, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.wsdleditorapi.generator;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ActionHelper {

    /**
     * Show and select the Node which represents the given component.
     *
     * @param  comp  model component to select.
     */
    public static void selectNode(WSDLComponent comp) {
        if (comp == null) return;
        DataObject dobj = getDataObject(comp);
        if (dobj != null) {
            ViewComponentCookie cookie = dobj.getCookie(
                    ViewComponentCookie.class);
            if (cookie != null) {
                // Do not switch views, use the currently showing view.
                cookie.view(ViewComponentCookie.View.CURRENT, comp,
                        (Object[]) null);
            }
        }
    }
    
    
    public static void selectNode(SchemaComponent comp, WSDLModel model) {
        if (comp == null || model == null) return;
        DataObject dobj = getDataObject(model);
        if (dobj != null) {
            ViewComponentCookie cookie = dobj.getCookie(
                    ViewComponentCookie.class);
            if (cookie != null) {
                // Do not switch views, use the currently showing view.
                cookie.view(ViewComponentCookie.View.CURRENT, comp,
                        (Object[]) null);
            }
        }
    }
    
    public static DataObject getDataObject(Component comp) {
        try {
            Model model = comp.getModel();
            if (model != null) {
                FileObject fobj = model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }

    public static DataObject getDataObject(Model model) {
        try {
            if (model != null) {
                FileObject fobj = model.getModelSource().
                getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }
	
}
