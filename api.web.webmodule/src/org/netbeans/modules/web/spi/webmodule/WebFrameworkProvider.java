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

package org.netbeans.modules.web.spi.webmodule;

import java.io.File;
import java.util.Set;

import org.netbeans.modules.web.api.webmodule.*;
import org.openide.filesystems.FileObject;

/**
 * Through this class the IDE obtains information about the web framework and also calls
 * actions, which proceed the framework specific things.
 * @author Petr Pisl
 */


public abstract class WebFrameworkProvider {
    private String name;
    private String description;

    /**
     * The constructor for the provider.
     * @param name Name of the Web Framework. It's used for displaying in the new Web Application wizard and customizer.
     * @param description Basic description of the framework. It's used as tooltip in the Web Application wizard and customizer. 
     */
    public WebFrameworkProvider(String name, String description){
        this.name = name;
        this.description = description;
    }
    
    /**
     * Returns the name for the framework.
     * @return the name
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Returns the user description of the framework.
     * @return the description
     */
    public String getDescription(){
        return this.description;
    }
    
    /**
     * It is called from the IDE, when user create new web application 
     * or when the framework is added to already existing web application.
     * @param wm the WebModule, which is extended.
     * @return It's list of created files with the framework and should be opened in the editor.
     */
    public abstract Set extend(WebModule wm);
    
    /**
     * Through this method the IDE find out, whether a web module has this  framework.
     * @param wm The Web Module
     * @return True, if the Web Module has already the framework, otherwise else.
     */
    public abstract boolean isInWebModule (WebModule wm);
    
    /**
     * Obtaining all configuration files, which are offered with the framework. The files 
     * are then displayed under the configuration nodes in the logical view.
     * @param wm The Web Module for which the configuration files are returned.
     * @return Array of the configuration files, which will be displayed under the Configuration Node in the logical view.
     */
    public abstract File[] getConfigurationFiles(WebModule wm);
    
    /**
     * This method returns configuration panel for the framework. The configuration
     * panel is displayed in the New Web Application wizard (when user select the framework)
     * or when user adds the framework through project properties. A panel is also obtained for
     * customizing the framework. It can be two different panels, one for adding and one for customize
     * already added framework.
     * @param wm The Web Module for which the panel is returned.
     * @return The panel for configuration the framework.
     */
    public abstract FrameworkConfigurationPanel getConfigurationPanel(WebModule wm);
    
    
    /**
     * Returns the part of the request's URL that calls the web component. 
     * This path starts with a "/" character and includes either the servlet name 
     * or a path to the servlet/jsp. Includes as well the mapping, but does not include 
     * any extra path information or a query string. 
     * The method can return null. Then common servlet path is used. 
     * <br/>
     * JSF Example: There is index.jsp directly in the document base. Normaly the URL 
     * for accessing this page in browser should be http://server:port/contextpath/index.jsp. 
     * The servletpath is /index.jsp.
     * Because the index.jsp includes jsf tags, should be called with appropriate JSF servlet 
     * mapping. If the mapping is /faces/*, then the right url is 
     * http://server:port/contextpath/faces/index.jsp and this method should returns /faces/index.jsp.
     * @param fo arbitrary FileObject. Should be a jsp file.
     * @return A string that contains the servletPath including mapping as well. Can return null.
     * 
     */
    public String getServletPath(FileObject file){
        return null;
    }
}
