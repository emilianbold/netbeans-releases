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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.AbstractCanvasLink.NodeYChangeListener;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasTreeToNodeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> BasicCanvasTreeToNodeLink<p>
 *
 * Description: </p> BasicCanvasTreeToNodeLink provides basic implementation of
 * ICanvasTreeToNodeLink <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 * @version   1.0
 */
public class BasicCanvasTreeToNodeLink
     extends AbstractCanvasLink
     implements ICanvasTreeToNodeLink {

    /**
     * the end canvas node
     */
    private ICanvasFieldNode mCanvasNode;

    /**
     * the node change listener
     */
    private NodeYChangeListener mYListener;

    
    /**
     * Creates a new BasicCanvasTreeToNodeLink object.
     *
     * @param link  the mapper link
     */
    public BasicCanvasTreeToNodeLink(IMapperLink link) {
        super(link);
        this.setArrowHeads(false, true);
        initializeArrowHeads(this);
        startPort = new BasicCanvasPort();
        startPort.setDraggable(false);
        startPort.setSelectable(false);
        startPort.setStyle(JGoPort.StyleHidden);
        this.setFromPort(startPort);
    }

    /**
     * Return the canvas field node repersetns the end point of this link.
     *
     * @return   the canvas node repersetns the end point of this link.
     */
    public ICanvasFieldNode getDestFieldNode() {
        return mCanvasNode;
    }

    /**
     * Return the tree node repersetns the start point of this link.
     *
     * @return   the tree node repersetns the start point of this link.
     */
    public IMapperTreeNode getSourceTreeAddress() {
        return (IMapperTreeNode) this.getMapperLink()
            .getStartNode();
    }

    /**
     * Set the canvas contains this canvas node.
     *
     * @param canvas  the canvas contains this canvas node.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        super.setMapperCanvas(canvas);
        mCanvasNode =
            canvas.findCanvasFieldNode(
            (IFieldNode) getMapperLink().getEndNode());

        if (mCanvasNode != null) {
            endPort = (JGoPort) mCanvasNode.getConnectPointObject();
            this.setToPort(endPort);
        }

        if (canvas instanceof BasicCanvasView) {
            startPort.setLeft(
                ((BasicCanvasView) canvas).getTreeLinkFromPortX());

            IMapperLink link = getMapperLink();
            startPort.setTop(
                link.getStartNode().getY() + NodeYChangeListener.Y_AXIS_DIFF
                + ((JGoView) canvas).getViewPosition().y);
            mYListener = new NodeYChangeListener(
                (JGoView) canvas,
                startPort);
            link.getStartNode()
                .addPropertyChangeListener(mYListener);
        }
        
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            boolean isHighlighting = new NetworkHighlightDetector(this).isHighlightDetected();
            new NetworkHighlightTraverser(isHighlighting).visit(this);
        }
    }
}
