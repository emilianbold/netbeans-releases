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

package org.netbeans.modules.soa.mapper.common.basicmapper.tree;

import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mapper.common.IMapperNode;


/**
 * <p>
 *
 * Title: </p> IMapperTreeNode <p>
 *
 * Description: </p> An MapperNode that holds a TreePath to repersents the
 * mapper node on the tree.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author Un Seng Leong
 * @version 1.0
 */
public interface IMapperTreeNode
         extends IMapperNode {

    /**
     * Retrun the TreePath repersentation of the tree address
     *
     * @return the tree path of this tree address
     */
    public TreePath getPath();

    /**
     * Return true if this tree node repersents source tree path, false otherwise.
     *
     * @return true if this tree node repersents source tree path, false otherwise.
     */
    public boolean isSourceTreeNode();

    /**
     * Return true if this tree node repersents destination tree path, false otherwise.
     *
     * @return true if this tree node repersents destination tree path, false otherwise.
     */
    public boolean isDestTreeNode();
    
    /**
     * set true if link from this node needs to be shown as selected.
     *@param selected flag
     */
    public void setSelectedLink(boolean selected);
    
    /**
     * check if link from this node needs to be shown as selected.
     * @return true if links from this node needs to be shown as selected.
     */
    public boolean isSelectedLink();
    
    /**
     * set true if link from this node needs to be highlighted.
     *@param highlight flag
     */
    public void setHighlightLink(boolean highlight);
    
    /**
     * check if link from this node needs to be highlighted.
     * @return true if links from this node needs to be highlighted.
     */
    public boolean isHighlightLink();
}
