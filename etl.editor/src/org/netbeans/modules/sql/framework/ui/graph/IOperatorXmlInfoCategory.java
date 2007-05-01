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

package org.netbeans.modules.sql.framework.ui.graph;

import java.util.ArrayList;

import javax.swing.Icon;

/**
 * @author radval
 */
public interface IOperatorXmlInfoCategory {

    /**
     * get the name of the operator (not I18n)
     * 
     * @return name
     */
    public String getName();

    /**
     * get display name of the operator I18N
     * 
     * @return display name
     */
    public String getDisplayName();

    /**
     * get tool tip for the operator
     * 
     * @return tool tip
     */
    public String getToolTip();

    /**
     * Get the icon for this operator
     */
    public Icon getIcon();

    /**
     * get the operator list for this category
     * 
     * @return operator list
     */
    public ArrayList getOperatorList();

    /**
     * Returns Toolbar type for this category of operators. See IOperatorXmlInfoModel for
     * Toolbar types.
     * 
     * @return toobar type this category of operators belong.
     */
    public int getToolbarType();

}

