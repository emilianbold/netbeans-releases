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

import org.openide.nodes.Node;

/**
 * @author radval
 */
public interface IOperatorManager {

    /**
     * Get the operator view build from operator defined in xml
     * 
     * @return operator view
     */
    public IToolBar getOperatorView();

    /**
     * get operator canvas view
     * 
     * @return operator canvas view
     */

    public IGraphView getGraphView();

    /**
     * show operator selection dialog for a particular operator category
     * 
     * @param node operator category
     */
    public void show(Node node);

    /**
     * get operator xml info model which is defined in netbeans layer.xml file
     * 
     * @return operator xml info model
     */
    public IOperatorXmlInfoModel getOperatorXmlInfoModel();

    /**
     * Returns toolbar type
     * 
     * @return toolbar type
     */
    public int getToolbarType();
}

