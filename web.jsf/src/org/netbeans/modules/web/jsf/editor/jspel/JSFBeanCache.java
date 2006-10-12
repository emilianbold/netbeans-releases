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

package org.netbeans.modules.web.jsf.editor.jspel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.config.model.FacesConfig;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Petr Pisl
 */
public class JSFBeanCache {
    
    public static List /*<ManagedBean>*/ getBeans(WebModule wm){
        FileObject[] files = JSFConfigUtilities.getConfiFilesFO(wm.getDeploymentDescriptor());
        ArrayList beans = new ArrayList();
        
        for (int i = 0; i < files.length; i++) {
            try {
                DataObject dObject = DataObject.find(files[i]);
                if (dObject != null){
                    FacesConfig config = ((JSFConfigDataObject)dObject).getFacesConfig();
                    ManagedBean [] mb = config.getManagedBean();
                    for (int j = 0; j < mb.length; j++) {
                        beans.add(mb[j]);
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return beans;
    }
    
    
}
