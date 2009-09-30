/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private ArrayList<IPropertyGroup> propertyGroupList = new ArrayList<IPropertyGroup>();

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

    public List<IPropertyGroup>  getPropertyGroupList() {
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

