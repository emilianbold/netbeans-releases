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
    // private String id; // 9bfbff60-467d-11d9-9669-0800200c9a66
    private String type; // engine or binding
    private String status; // installed, started, stop
    private String name; // component name
    private String description; // component description
    private boolean enabled; // to deploy or not

    /**
     * Creates a new ComponentObject object.
     */
    public ComponentObject() {
        super();
    }

    /**
     * Creates a new ComponentObject object.
     *
     * @param type DOCUMENT ME!
     * @param status DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param description DOCUMENT ME!
     * @param enabled DOCUMENT ME!
     */
    public ComponentObject(
        String type, String status, String name, String description, boolean enabled
    ) {
        super();
        this.type = type;
        this.status = status;

        //this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.name = name;
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
     * @param type DOCUMENT ME!
     */
    public void setType(String type) {
        this.type = type;
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
     * @param status DOCUMENT ME!
     */
    public void setStatus(String status) {
        this.status = status;
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
     * @param name DOCUMENT ME!
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * DOCUMENT ME!
     *
     * @param description DOCUMENT ME!
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getEnabled() {
        return this.enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param enabled DOCUMENT ME!
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
