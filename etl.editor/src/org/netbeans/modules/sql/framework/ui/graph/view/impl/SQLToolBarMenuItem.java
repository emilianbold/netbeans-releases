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
package org.netbeans.modules.sql.framework.ui.graph.view.impl;

import java.awt.datatransfer.Transferable;

import javax.swing.SwingConstants;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;


/**
 * Represents a SQLToolBar menu item. Generally for displaying Operator Icon, names and
 * allowing user to drag and drop new Operator.
 * 
 * @author Girish Patil
 * @version $Revision$
 */

public class SQLToolBarMenuItem extends BaseDragableMenuItem {
    private IOperatorXmlInfo mItem;

    public SQLToolBarMenuItem(IOperatorXmlInfo item) {
        super();
        this.setIcon(item.getIcon());
        this.setText(item.getDisplayName());
        this.setHorizontalAlignment(SwingConstants.LEFT);
        this.setVisible(item.isChecked());
        mItem = item;
    }

    public IOperatorXmlInfo getItemObject() {
        return mItem;
    }

    public Object getTransferableObject() {
        return mItem;
    }

    protected Transferable getTransferable() {
        return mItem.getTransferable();
    }
}

