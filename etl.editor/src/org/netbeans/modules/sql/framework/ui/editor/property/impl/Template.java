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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.editor.property.IElement;
import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyGroup;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplate;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class Template implements ITemplate {

    private String displayName;
    private String name;
    private INode parent;
    private ArrayList propertyGroupList = new ArrayList();

    private String toolTip;

    /** Creates a new instance of Template */
    public Template() {
    }

    /**
     * add a element in the node
     * 
     * @param element element to add
     */
    public void add(IElement element) {
        element.setParent(this);
        addPropertyGroup((IPropertyGroup) element);
    }

    public void addPropertyGroup(IPropertyGroup propertyG) {
        propertyGroupList.add(propertyG);
    }

    /**
     * get the count of template in this templte group
     * 
     * @return child count
     */
    public int getChildCount() {
        return propertyGroupList.size();
    }

    /**
     * get the display name of of element
     * 
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
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

    public IPropertyGroup getPropertyGroup(String groupName) {
        Iterator it = propertyGroupList.iterator();
        while (it.hasNext()) {
            IPropertyGroup pg = (IPropertyGroup) it.next();
            if (pg.getName().equals(groupName)) {
                return pg;
            }
        }
        return null;
    }

    public List getPropertyGroupList() {
        return propertyGroupList;
    }

    /**
     * get the tooltip of of element
     * 
     * @return tooltip
     */
    public String getToolTip() {
        return this.toolTip;
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
}

