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
package org.netbeans.modules.edm.editor.graph.jgo;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;


import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GraphPort extends JGoPort implements IGraphPort {

    private boolean toggleLinkHighliting = false;
    JGoPen pen;

    /** Creates a new instance of GraphPort */
    public GraphPort() {
        super();
        this.setSelectable(false);
        this.setResizable(false);
    }

    public boolean doMouseEntered(int modifiers, Point dc, Point vc, JGoView view) {
        if (this.getNumLinks() == 0) {
            return false;
        }

        JGoListPosition pos = this.getFirstLinkPos();
        while (pos != null) {
            JGoLink link = this.getLinkAtPos(pos);
            if (link instanceof IGraphLink) {
                ((IGraphLink) link).startHighlighting();
            } else {
                if (pen == null) {
                    pen = link.getPen();
                }
                link.setPen(JGoPen.make(JGoPen.SOLID, 2, Color.ORANGE));
            }
            pos = this.getNextLinkPos(pos);
        }
        return true;
    }

    public boolean doMouseExited(int modifiers, Point dc, Point vc, JGoView view) {
        if (this.getNumLinks() == 0) {
            return false;
        }

        JGoListPosition pos = this.getFirstLinkPos();
        while (pos != null) {
            JGoLink link = this.getLinkAtPos(pos);
            if (link instanceof IGraphLink) {
                ((IGraphLink) link).stopHighlighting();
            } else {
                link.setPen(pen);
            }
            pos = this.getNextLinkPos(pos);
        }

        return true;
    }

    /**
     * @param modifiers which keys are depressed (see Event documentation)
     * @param dc the point of the click in document coordinates
     * @param vc the point of the click in view coordinates
     * @param view the view in which this event occured
     * @return true if handled here and the view doesn't need to try this object's parent
     *         to see if it's interested
     */
    public boolean doMouseDblClick(int modifiers, Point dc, Point vc, JGoView view) {
        if (toggleLinkHighliting) {
            doMouseExited(modifiers, dc, vc, view);
            toggleLinkHighliting = false;
            return true;
        }
        doMouseEntered(modifiers, dc, vc, view);
        toggleLinkHighliting = true;
        return true;
    }

    /**
     * Change the cursor at the port
     * 
     * @param flags
     */
    public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
        if ((isValidSource() || isValidDestination()) && getLayer() != null && getLayer().isModifiable()) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return true;
        }
        return false;
    }

    /**
     * get the data node for this graph port This is usually its parent
     * 
     * @return data node
     */
    public IGraphNode getDataNode() {
        JGoObject obj = this.getParent();

        while (obj != null && !(obj instanceof IGraphNode)) {
            obj = obj.getParent();
        }

        return (IGraphNode) obj;
    }

}

