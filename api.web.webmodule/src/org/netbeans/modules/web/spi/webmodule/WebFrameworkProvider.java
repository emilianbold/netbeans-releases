/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.spi.webmodule;

import java.io.File;
import java.util.Set;

import org.netbeans.modules.web.api.webmodule.*;

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
     * @param customize It's false when the method is called before adding the framework to the web medule and true when the framework 
     * is already added.
     * @return The panel for configuration the framework.
     */
    public abstract FrameworkConfigurationPanel getConfigurationPanel(WebModule wm);
}
