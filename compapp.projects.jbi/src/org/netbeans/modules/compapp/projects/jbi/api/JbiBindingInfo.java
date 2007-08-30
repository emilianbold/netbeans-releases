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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.io.Serializable;
import java.net.URL;

/**
 * JBI binding info, a Binding Component may implment multiple bindings.
 *
 * @author tli
 */
public class JbiBindingInfo implements Serializable {

    /**
     * Name of the binding component, e.x., "sun-http-binding".
     */
    private String bcName;

    /**
     * Type of the binding, e.x., "http", or "soap".
     */
    private String bindingType;

    /**
     * DOCUMENT ME!
     */
    private String description;

    /**
     * DOCUMENT ME!
     */
    private URL icon;

    /**
     * DOCUMENT ME!
     */
    private String ns;

    /**
     * DOCUMENT ME!
     *
     * @param bcName
     * @param bindingType
     * @param icon
     * @param description 
     * @param ns 
     */
    public JbiBindingInfo(String bcName, String bindingType, URL icon, 
            String description, String ns) {
        this.bcName = bcName;
        this.bindingType = bindingType;
        this.icon = icon;
        this.description = description;
        this.ns = ns;
    }

    /**
     * Gets the binding component name, e.x., sun-file-binding, sun-http-binding.
     *
     * @return the binding compoent name
     */
    public String getBcName() {
        return this.bcName;
    }

    /**
     * Gets the binding type, e.x., file, http, or soap.
     *
     * @return the binding type
     */
    // RENAME ME: API change
    public String getBindingName() {
        return this.bindingType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the icon.
     */
    public URL getIcon() {
        return this.icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the binding component namespaces
     */
    // FIXME
    public String[] getNameSpaces() {
        return new String[] { this.ns };
    }
}
