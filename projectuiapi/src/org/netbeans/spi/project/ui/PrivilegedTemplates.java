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
 * List of templates which should be in the initial "privileged" list
 * when making a new file.
 * An instance should be placed in {@link org.netbeans.api.project.Project#getLookup}
 * to affect the privileged list for that project.
 * <p>
 * For more information about registering templates see overview of 
 * {@link org.netbeans.spi.project.ui.templates.support} package. 
 * @see org.netbeans.spi.project.ui.support.CommonProjectActions
 * @author Petr Hrebejk
 */
public interface PrivilegedTemplates {
    
    /**
     * Lists privileged templates.
     * @return full paths to privileged templates, e.g. <samp>Templates/Other/XmlFile.xml</samp>
     */
    public String[] getPrivilegedTemplates();
    
}
