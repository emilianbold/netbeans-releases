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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.*;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;

/**
 *
 * @author Peter Williams
 */
public class ServletMetadataReader implements MetadataModelAction<WebAppMetadata, Map<String, Object>> {

    /** Entry point to generate map from standard descriptor
     */
    public static Map<String, Object> readDescriptor(WebApp webApp) {
        return genProperties(webApp);
    }
    
    /** Entry point to generate map from annotation metadata
     */
    public Map<String, Object> run(WebAppMetadata metadata) throws Exception {
        return genProperties(metadata.getRoot());
    }
    
    /** Maps interesting fields from ejb-jar descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    private static Map<String, Object> genProperties(WebApp webApp) {
        Map<String, Object> data = new HashMap<String, Object>();
        Servlet [] servlets = webApp.getServlet();
        if(servlets != null) {
            for(Servlet servlet: servlets) {
                String servletName = servlet.getServletName();
                if(Utils.notEmpty(servletName)) {
                    Map<String, String> servletMap = new HashMap<String, String>();
                    data.put(servletName, servletMap);
                    servletMap.put(DDBinding.PROP_NAME, servletName);

                    RunAs runAs = servlet.getRunAs();
                    if(runAs != null) {
                        String roleName = runAs.getRoleName();
                        if(Utils.notEmpty(roleName)) {
                            servletMap.put(DDBinding.PROP_RUNAS_ROLE, roleName);
                        }
                    }
                }
            }
        }
        
        return data.size() > 0 ? data : null;
    }

}
