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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.IOption;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicOption implements IOption {

    private String displayName;

    private String name;
    private INode parent;
    private String toolTip;

    private String value;

    /** Creates a new instance of GUIOption */
    public BasicOption() {
    }

    public BasicOption(String name, String val) {
        this.name = name;
        this.value = val;
    }

    /**
     * get the display name of of element
     * 
     * @return display name
     */
    public String getDisplayName() {
        if (this.displayName == null) {
            return this.getName();
        }

        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    /**
     * get the parent element
     * 
     * @return parent
     */
    public INode getParent() {
        return parent;
    }

    /**
     * get the tooltip of of element
     * 
     * @return tooltip
     */
    public String getToolTip() {
        return this.toolTip;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * set the display name of the element
     * 
     * @param dName display name
     */
    public void setDisplayName(String dName) {
        this.displayName = dName;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * set parent parent element
     */
    public void setParent(INode parent) {
        this.parent = parent;
    }

    /**
     * set the tooltip of the element
     * 
     * @param tTip tool tip
     */
    public void setToolTip(String tTip) {
        this.toolTip = tTip;
    }

    public void setValue(String val) {
        this.value = val;
    }

    public String toString() {
        return this.getDisplayName();
    }
}

