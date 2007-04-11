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

package org.netbeans.spi.mobility.cfgfactory;

import java.util.List;
import java.util.Map;

/**
 * ProjectConfigurationFactory provides tree of categories and project configuration templates.<br>
 * User can select from the tree during each new project configuration creation.
 * Selected configuration template(s) then provide information for the newly created project configuration(s). 
 * If you want to plug a custom device database provider then this is your entry point.
 * @author Adam Sotona
 */
public interface ProjectConfigurationFactory {
    
    /**
     * Getter of the root category descriptor.
     * This descriptor describes the factory itself and works as its root node.
     * @return CategoryDescriptor root node descriptor
     */
    public CategoryDescriptor getRootCategory();
    
    /**
     * Abstract descriptor interface. do not implement directly.
     * Use CategoryDescriptor or ConfigurationTemplateDescriptor instead.
     */
    static abstract interface Descriptor {
        /**
         * Display name of the descriptor (category or cfg template name).
         * @return String display name
         */
        public String getDisplayName();
    }
    
    /**
     * Describes category node of the tree.
     * Each category can include multiple subcategories and/or configuration templates.
     */
    public static interface CategoryDescriptor extends Descriptor {
        /**
         * Getter for list of category children.
         * @return non-null list of Descriptors 
         */
        public List<Descriptor> getChildren();
    }
    
    /**
     * Describes Project Configuration Template.
     */
    public static interface ConfigurationTemplateDescriptor extends Descriptor {
        /**
         * Getter for the new project configuration name.
         * The project configuration name must be a valid Java identifier.  
         * @return String new project configuration name
         */
        public String getCfgName();
        /**
         * Getter for project properties that will be specific to the project configuration.
         * @return String-String Map of project properties or null
         */
        public Map<String, String> getProjectConfigurationProperties();
        /**
         * Getter for properties that will added to project properties without configuration prefix (for example relative references, etc...).
         * @return String-String Map of project properties or null
         */
        public Map<String, String> getProjectGlobalProperties();
        /**
         * Getter for properties that will added to project private properties without configuration prefix (for example absolute references, etc...).
         * @return String-String Map of private properties or null
         */
        public Map<String, String> getPrivateProperties();
    }
}
