/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;

import org.netbeans.modules.sql.framework.ui.graph.IGraphLink;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoSelection;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GraphLink extends JGoLink implements IGraphLink {

    private Object obj;
    private JGoPen pen;
    private JGoBrush brush;

    /** Creates a new instance of GraphLink */
    public GraphLink(IGraphPort fromP, IGraphPort toP) {
        super((JGoPort) fromP, (JGoPort) toP);
        this.setOrthogonal(true);
        this.setAvoidsNodes(false);
        this.setRelinkable(false);
        this.setArrowHeads(false, true);

        this.setPen(JGoPen.lightGray);
        this.setBrush(JGoBrush.lightGray);
        this.pen = this.getPen();
        this.brush = this.getBrush();
    }

    public void setDefaultPen(JGoPen pen) {
        this.pen = pen;
        this.setPen(pen);
    }

    public void setDefaultBrush(JGoBrush brush) {
        this.brush = brush;
        this.setBrush(brush);
    }

    /**
     * get from port of this link
     * 
     * @return from port
     */
    public IGraphPort getFromGraphPort() {
        return (IGraphPort) this.getFromPort();
    }

    /**
     * get to port of this link
     * 
     * @return to port
     */
    public IGraphPort getToGraphPort() {
        return (IGraphPort) this.getToPort();
    }

    /**
     * get data object stored in this link
     * 
     * @return graph node
     */
    public Object getDataObject() {
        return obj;
    }

    /**
     * set the data object on this link
     * 
     * @param dObj data object
     */
    public void setDataObject(Object dObj) {
        this.obj = dObj;
    }

    /**
     * start highlighting this link
     */
    public void startHighlighting() {
        this.setPen(JGoPen.make(JGoPen.SOLID, 2, Color.ORANGE));
        this.setBrush(JGoBrush.makeStockBrush(Color.ORANGE));
    }

    /**
     * stop highlighting this link
     */
    public void stopHighlighting() {
        this.setPen(pen);
        this.setBrush(brush);
    }

    /**
     * set the link pen
     * 
     * @param pen pen
     */
    public void setGraphLinkPen(JGoPen newPen) {
        this.pen = newPen;
        this.setPen(pen);
    }

    /**
     * set the link brush
     * 
     * @param brush brush
     */
    public void setGraphLinkBrush(JGoBrush newBrush) {
        this.brush = newBrush;
        this.setBrush(newBrush);
    }

    public void gainedSelection(JGoSelection selection) {
        super.gainedSelection(selection);
        startHighlighting();
    }

    public void lostSelection(JGoSelection selection) {
        super.lostSelection(selection);
        stopHighlighting();
    }

    public int getMidPoint() {
        return midPoint;
    }

    private int midPoint = -1;

    private boolean isMidPointTaken(int newMidPoint) {
        IGraphPort fromPort = this.getFromGraphPort();
        IGraphNode fromNode = fromPort.getDataNode();
        java.util.List links = fromNode.getAllLinks();

        java.util.Iterator it = links.iterator();
        while (it.hasNext()) {
            GraphLink link = (GraphLink) it.next();
            if (link == this) {
                continue;
            }
            int p = link.getMidPoint();
            if (p == newMidPoint) {
                return true;
            }
        }

        return false;
    }

    protected int getMidOrthoPosition(int from, int to, boolean vertical) {
        int pp = from;
        midPoint = pp;

        while (isMidPointTaken(midPoint)) {
            midPoint = midPoint + 10;
        }

        return midPoint;
    }

}

