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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * List of templates which should be in the initial "privileged" list
 * when creating a new file. An instance should be placed in the
 * <code>j2ee/webtier/templates</code> folder in a module layer file
 * to affect the privileged list for that project.
 *
 * @author Petr Pisl
 */
public interface WebPrivilegedTemplates {

    /**
     * Returns the list of templates which should be added in the initial "privileged" list
     * when created a new file.
     *
     * @param  webModule the web module to return the templates for.
     *         For example, it can be used to find out whether the web module is extended
     *         by a framework and then appropriate templates for the framework can be offered
     *         in the list of privileged list of templates.
     *
     * @return full paths to privileged templates, e.g. <samp>Templates/Other/XmlFile.xml</samp>; never null.
     */
    public String[] getPrivilegedTemplates(WebModule webModule);
}
