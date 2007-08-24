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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "application" element provides a mechanism to define the
 * various per-application-singleton implementation artifacts for
 * a particular web application that is utilizing 
 * JavaServer Faces.  For nested elements that are not specified, 
 * the JSF implementation must provide a suitable default.
 * 
 * @author Petr Pisl
 */
public interface Application extends JSFConfigComponent {
    /**
     * Property name of &lt;view-handler&gt; element.
     */ 
    public static final String VIEW_HANDLER = JSFConfigQNames.VIEW_HANDLER.getLocalName();
    
    /**
     * Property name of &lt;locale-config&gt; element.
     */ 
    public static final String LOCALE_CONFIG = JSFConfigQNames.LOCALE_CONFIG.getLocalName();

    List<ViewHandler> getViewHandlers();
    void addViewHandler(ViewHandler handler);
    void addViewHandler(int index, ViewHandler handler);
    void removeViewHandler(ViewHandler handler);
    
    List<LocaleConfig> getLocaleConfig();
    void addLocaleConfig(LocaleConfig locale);
    void addLocaleConfig(int index, LocaleConfig locale);
    void removeLocaleConfig(LocaleConfig locale);
}
