/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui;

/**
 * List of template types supported by a project when making a new file.
 * An instance should be placed in {@link org.netbeans.api.project.Project#getLookup}
 * to affect the recommended template list for that project.
 * @author Petr Hrebejk
 */
public interface RecommendedTemplates {
    
    /**
     * Lists supported template types.
     * @return types of supported templates (should match template file attribute names)
     */
    public String[] getRecommendedTypes();
    
}
