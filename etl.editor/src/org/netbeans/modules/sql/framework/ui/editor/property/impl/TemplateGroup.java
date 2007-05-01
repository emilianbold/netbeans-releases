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

import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.sql.framework.ui.editor.property.IElement;
import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplate;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplateGroup;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TemplateGroup implements ITemplateGroup {

    private String displayName;
    private HashMap templateMap = new HashMap();
    private String toolTip;

    /** Creates a new instance of TemplateGroup */
    public TemplateGroup() {
    }

    /**
     * add a element in the node
     * 
     * @param element element to add
     */
    public void add(IElement element) {
        element.setParent(this);
        addTemplate((ITemplate) element);
    }

    public void addTemplate(ITemplate template) {
        templateMap.put(template.getName(), template);
    }

    /**
     * get the count of template in this templte group
     * 
     * @return child count
     */
    public int getChildCount() {
        return templateMap.size();
    }

    /**
     * get the display name of of element
     * 
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * get the name of of element
     * 
     * @return name
     */
    public String getName() {
        return null;
    }

    /**
     * get the parent element
     * 
     * @return parent
     */
    public INode getParent() {
        return null;
    }

    public Map getTemplates() {
        return templateMap;
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

    /**
     * set the name of the element
     * 
     * @param name name
     */
    public void setName(String name) {
    }

    /**
     * set parent parent element
     */
    public void setParent(INode parent) {
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

