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
