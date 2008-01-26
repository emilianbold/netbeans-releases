/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.test.xslt.lib;

import java.awt.Point;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author ca@netbeans.org
 */

public class EditorTreeOperator extends JTreeOperator {
    protected JTreeOperator opTree;
    
    /** Creates a new instance of EditorTreeOperator */
    public EditorTreeOperator(JComponentOperator opContainer, int treeIndex) {
        super((JTree) Helpers.getComponentOperator(opContainer, "org.netbeans.modules.soa.mapper.basicmapper.tree.AbstractMapperTree$TreeView", treeIndex).getSource());
    }
    
    public Point prepareNodeForClick(String strPath) {
        TreePath treePath = findPath(strPath);
        Point point = getPointToClick(treePath);
        
        selectPath(treePath);
        Helpers.waitNoEvent();
        
        return point;
    }
    
}
