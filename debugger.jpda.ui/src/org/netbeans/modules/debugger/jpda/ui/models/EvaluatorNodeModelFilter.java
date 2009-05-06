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
package org.netbeans.modules.debugger.jpda.ui.models;


import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.debugger.jpda.ui.CodeEvaluator;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

public class EvaluatorNodeModelFilter implements ExtendedNodeModelFilter {

    private Collection<ModelListener> listeners = new HashSet<ModelListener>();

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }

    private void fireNodeChanged (Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event;
//        event = new ModelEvent.NodeChanged(this, node,
//                ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
//                ModelEvent.NodeChanged.ICON_MASK |
//                ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK |
//                ModelEvent.NodeChanged.CHILDREN_MASK |
//                ModelEvent.NodeChanged.EXPANSION_MASK);
        event = new ModelEvent.NodeChanged(this, node, ModelEvent.NodeChanged.CHILDREN_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }

    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
    }

    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof EvaluatorTreeModel.SpecialNode) {
            return ((EvaluatorTreeModel.SpecialNode)node).getIconBase();
        }
        if (node == CodeEvaluator.getResult()) {
            return "org/netbeans/modules/debugger/jpda/resources/evaluator_result_16.png"; // NOI18N
        }
        if (shouldIgnore(node)) {
            return WatchesNodeModel.WATCH;
        }
        return original.getIconBaseWithExtension(node);
    }

    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof EvaluatorTreeModel.SpecialNode) {
            return ((EvaluatorTreeModel.SpecialNode)node).getIconBase();
        }
        if (node == CodeEvaluator.getResult()) {
            return "org/netbeans/modules/debugger/jpda/resources/evaluator_result_16.png"; // NOI18N
        }
        if (shouldIgnore(node)) {
            return WatchesNodeModel.WATCH;
        }
        return original.getIconBase(node);
    }

    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof EvaluatorTreeModel.SpecialNode) {
            return ((EvaluatorTreeModel.SpecialNode)node).getDisplayName();
        }
        if (node == CodeEvaluator.getResult()) {
            String str = CodeEvaluator.getExpressionText();
            if (str != null) {
                return str;
            }
        }
        if (shouldIgnore(node)) {
            return "";
        }
        return original.getDisplayName(node);
    }

    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof EvaluatorTreeModel.SpecialNode) {
            return ((EvaluatorTreeModel.SpecialNode)node).getShortDescription();
        }
        if (node == CodeEvaluator.getResult()) {
            String str = CodeEvaluator.getExpressionText();
            if (str != null) {
                StringBuffer buf = new StringBuffer();
                buf.append("<html>");
                str = str.replaceAll ("&", "&amp;");
                str = str.replaceAll ("<", "&lt;");
                str = str.replaceAll (">", "&gt;");
                str = str.replaceAll ("\n", "<br/>");
                str = str.replaceAll ("\r", "");
                buf.append(str);
                buf.append("</html>");
                return buf.toString();
            }
        }
        if (shouldIgnore(node)) {
            return "";
        }
        return original.getShortDescription(node);
    }

    private boolean shouldIgnore(Object node) {
        String name = node.getClass().getSimpleName();
        return "AbstractVariable".equals(name) || "AbstractObjectVariable".equals(name); // [TODO]
    }

}
