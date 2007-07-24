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

package org.netbeans.modules.compapp.projects.jbi.ui.deployInfo;

/**
 * JBI deployInfo component object used in the table model
 *
 * @author Tientien Li
 */
public class ComponentObject {
    private String type; // engine or binding
    private String status; // installed, started, stop
    private String name; // component name
    private String description; // component description

    /**
     * Creates a new ComponentObject object.
     *
     * @param type DOCUMENT ME!
     * @param status DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param description DOCUMENT ME!
     */
    public ComponentObject(
        String type, String status, String name, String description) {
        super();
        this.type = type;
        this.status = status;
        this.name = name;
        this.description = description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getType() {
        return this.type;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return this.name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return this.description;
    }
}
