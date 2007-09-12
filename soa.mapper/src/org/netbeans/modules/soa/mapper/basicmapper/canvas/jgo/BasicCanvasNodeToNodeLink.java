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
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToNodeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> BasicCanvasNodeToNodeLink<p>
 *
 * Description: </p> BasicCanvasNodeToNodeLink provides basic implemanation of
 * ICanvasNodeToNodeLink. <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public class BasicCanvasNodeToNodeLink
     extends AbstractCanvasLink
     implements ICanvasNodeToNodeLink {

    /**
     * the end canvas node
     */
    private ICanvasFieldNode mDestCanvasNode;

    /**
     * the start canvas node
     */
    private ICanvasFieldNode mSourceCanvasNode;

    
    /**
     * Creates a new BasicCanvasNodeToNodeLink object.
     *
     * @param link  the mapper link
     */
    public BasicCanvasNodeToNodeLink(IMapperLink link) {
        super(link);
        this.setArrowHeads(false, true);
        initializeArrowHeads(this);
    }

    /**
     * Return the canvas field node repersetns the start point of this link.
     *
     * @return   the canvas field node repersetns the start point of this link.
     */
    public ICanvasFieldNode getDestFieldNode() {
        return mDestCanvasNode;
    }

    /**
     * Return the canvas field node repersetns the end point of this link.
     *
     * @return   the canvas node repersetns the end point of this link.
     */
    public ICanvasFieldNode getSourceFieldNode() {
        return mSourceCanvasNode;
    }

    /**
     * Set the canvas contains this canvas link.
     *
     * @param canvas  the canvas contains this canvas link.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        super.setMapperCanvas(canvas);

        mSourceCanvasNode =
            canvas.findCanvasFieldNode(
            (IFieldNode) getMapperLink().getStartNode());

        mDestCanvasNode =
            canvas.findCanvasFieldNode(
            (IFieldNode) getMapperLink().getEndNode());

        if ((mSourceCanvasNode != null) && (mDestCanvasNode != null)) {
            startPort = (JGoPort) mSourceCanvasNode.getConnectPointObject();
            endPort = (JGoPort) mDestCanvasNode.getConnectPointObject();
            this.setFromPort(startPort);
            this.setToPort(endPort);
        }
        
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            boolean isHighlighting = new NetworkHighlightDetector(this).isHighlightDetected();
            new NetworkHighlightTraverser(isHighlighting).visit(this);
        }
    }
}
