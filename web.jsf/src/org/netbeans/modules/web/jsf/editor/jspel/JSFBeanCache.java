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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JSFBeanCache {
    
    public static List /*<ManagedBean>*/ getBeans(WebModule wm){
        FileObject[] files = ConfigurationUtils.getFacesConfigFiles(wm);
        ArrayList beans = new ArrayList();
        
        for (int i = 0; i < files.length; i++) {
                FacesConfig facesConfig = ConfigurationUtils.getConfigModel(files[i], false).getRootComponent();
                Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
                for (Iterator<ManagedBean> it = managedBeans.iterator(); it.hasNext();) {
                    beans.add(it.next());   
                }
        }
        return beans;
    }
    
    
}
