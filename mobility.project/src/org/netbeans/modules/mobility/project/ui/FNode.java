/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.mobility.project.ui;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.openide.actions.CopyAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

class FNode extends FilterNode {

    private final Action[] actions;
    private final Image icon;
    final boolean origProp;

    FNode(Node original, Lookup lookup, Action[] acts, VisualClassPathItem it) {
        super(original, new ActionFilterChildren(original), lookup);
        actions = acts == null ? new Action[0] : acts;
        icon = ((ImageIcon) it.getIcon()).getImage();
        origProp = false;
    }

    FNode(Node original, Image it) {
        super(original, new ActionFilterChildren(original), null);
        origProp = true;
        actions = original.getActions(false);
        icon = it;
    }

    @Override
    public Action[] getActions(boolean context) {
        return actions == null ? 
            new Action[]{
                NodeActions.RemoveResourceAction.getStaticInstance(),
                SystemAction.get(CopyAction.class)
        }
            : actions;
    }

    @Override
    public Image getIcon(int i) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return icon;
    }

    @Override
    public boolean canDestroy() {
        return origProp == true ? super.canDestroy() : false;
    }

    @Override
    public boolean canRename() {
        return origProp == true ? super.canRename() : false;
    }

    @Override
    public boolean canCut() {
        return origProp == true ? super.canCut() : false;
    }

    @Override
    public boolean canCopy() {
        return origProp == true ? super.canCopy() : true;
    }

    private static class ActionFilterChildren extends FilterNode.Children {

        ActionFilterChildren(Node original) {
            super(original);
        }

        @Override
        protected Node[] createNodes(Node n) {
            return new Node[]{new FNode(n, n.getIcon(1))};
        }
    }
}
