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
 * JBI binding info, a Binding Component may implment multiple bindings
 *
 * @author tli
 */
public class JbiBindingInfo implements Serializable {

    /**
     * DOCUMENT ME!
     */
    String bcName;

    /**
     * DOCUMENT ME!
     */
    String bindingName;

    /**
     * DOCUMENT ME!
     */
    String description;

    /**
     * DOCUMENT ME!
     */
    URL icon;

    /**
     * DOCUMENT ME!
     */
    String[] ns;

    /**
     *
     */
    public JbiBindingInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * DOCUMENT ME!
     *
     * @param bcName
     * @param bindingName
     * @param icon
     */
    public JbiBindingInfo(String bcName, String bindingName, URL icon, String description, String[] ns) {
        super();
        this.bcName = bcName;
        this.bindingName = bindingName;
        this.icon = icon;
        this.description = description;
        this.ns = ns;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the BC name.
     */
    public String getBcName() {
        return this.bcName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the Binding name.
     */
    public String getBindingName() {
        return this.bindingName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the icon.
     */
    public URL getIcon() {
        return this.icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the BC name.
     */
    public String[] getNameSpaces() {
        return this.ns;
    }

}
