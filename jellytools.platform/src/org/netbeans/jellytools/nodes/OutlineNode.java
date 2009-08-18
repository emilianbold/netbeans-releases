/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.jellytools.nodes;

import java.awt.Point;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.OutlineOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *  Handles nodes of the Outline component.
 *
 * Warning: Do not use yet!! Incomplete, under development and most probably still buggy!
 *
 * @author Vojtech.Sigler@sun.com
 */
public class OutlineNode {

    private OutlineOperator _outline;
    private TreePath _treePath;

    public OutlineNode(OutlineOperator irOutlineOp, TreePath irTreePath)
    {
        if (irOutlineOp == null)
            throw new IllegalArgumentException("OutlineOperator argument cannot be null.");

        if (irTreePath == null)
            throw new IllegalArgumentException("TreePath argument cannot be null.");
        
        _outline = irOutlineOp;
        _treePath = irTreePath;
    }

    public OutlineNode(OutlineNode irParentNode, String isPath)
    {
        _outline = irParentNode.getOutline();
        _treePath = getOutline().findPath(irParentNode.getTreePath(), isPath);
    }

    public OutlineOperator getOutline()
    {
        return _outline;
    }

    public TreePath getTreePath()
    {
        return _treePath;
    }

    public static TreePath findAndExpandPath(OutlineOperator irOOp, TreePath irTP)
    {
        return irTP;
    }

    public JPopupMenuOperator callPopup()
    {
        Point lrPopupPoint = getOutline().getLocationForPath(getTreePath());

        //y is for row, x for column
        return new JPopupMenuOperator(getOutline().callPopupOnCell(lrPopupPoint.y, lrPopupPoint.x));
    }

    public void expand()
    {
        getOutline().expandPath(getTreePath());
    }

}
