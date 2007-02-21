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

package org.netbeans.modules.soa.mapper.basicmapper.tree;

import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.soa.mapper.common.IMapperAutoLayout;
import org.netbeans.modules.soa.mapper.common.IMapperView;

/**
 * <p>
 *
 * Title: </p> MapperTreeAutoLayout<p>
 *
 * Description: </p> MapperTreeAutoLayout calls repaint() on the component of
 * the tree view to perform the default auto layout function. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 * @version   1.0
 */

public class MapperTreeAutoLayout
     implements IMapperAutoLayout {

    /**
     * the instance of the tree view that this auto layout to perform
     */
    private IMapperTreeView mTreeView;

    /**
     * Creates a new BasicMapperAutoLayout object.
     *
     * @param treeView  Description of the Parameter
     */
    public MapperTreeAutoLayout(IMapperTreeView treeView) {
        mTreeView = treeView;
    }

    /**
     * Return the mapper view that this layout performs to.
     *
     * @return   the mapper view that this layout performs to.
     */
    public IMapperView getView() {
        return mTreeView;
    }

    /**
     * This method just calls repaint() on the JTree instance.
     */
    public void autoLayout() {
        mTreeView.getTree().repaint();
    }
}
