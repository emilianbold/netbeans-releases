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
 * when making a new file. An instance should be placed in 
 * <pre>j2ee/webtier/templates</pre> folder in a module XML layer file
 * to affect the privileged list for that project.
 * The instances are asked for the list of templates when a new project 
 * is created or opened and also they are asked, when a web framework is added 
 * or removed to the project. 
 * 
 * @author Petr Pisl
 */
public interface WebPrivilegedTemplates {

    /**
     * List of templates which should be added in the initial "privileged" list
     * when making a new file. 
     * @param webModule the WebModule, which is in the project. It can be used 
     * for example to find out whether the web module is extended by a framework
     * and then appropriate templates for the framework can be offered 
     * in the list of privileged list of templates.
     * @return full paths to privileged templates, e.g. <samp>Templates/Other/XmlFile.xml</samp>
     */
    public String[] getPrivilegedTemplates(WebModule webModule);
}
