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

package org.netbeans.modules.soa.ui.tree;

/**
 * The SPI interface for constructing extensions to a tree. 
 * The extension model is intended to provide additional items in the tree. 
 * It doesn't allow to eliminate items, which are provided by main 
 * mapper model or by another extension models.
 * 
 * It is used by the BPEL mapper for building left and right trees.
 * 
 * An external code can provide an instance of such interface 
 * to show required tree.
 * 
 * @author nk160297
 */
public interface SoaTreeExtensionModel {

    public TreeStructureProvider getTreeStructureProvider();
    
    public TreeItemInfoProvider getTreeItemInfoProvider();
    
    public TreeItemActionsProvider getTreeItemActionsProvider();
    
}
