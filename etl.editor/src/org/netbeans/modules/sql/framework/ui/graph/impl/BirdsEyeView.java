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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoViewListener;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BirdsEyeView extends JGoView {

    /** Creates a new instance of BirdsEyeView */
    public BirdsEyeView() {
        setHidingDisabledScrollbars(true);
        setInternalMouseActions(DnDConstants.ACTION_MOVE);
        setScale(1.0d / 4);
        this.setDocument(new GraphDocument());
        setMouseEnabled(true);
    }

    // when the Overview window is no longer needed, make sure the observed view doesn't
    // keep
    // any references to this view
    public void removeNotify() {
        removeListeners();
        myObserved = null;
        super.removeNotify();
    }

    private void removeListeners() {
        if (myObserved != null && myOverviewRect != null) {
            myObserved.getDocument().removeDocumentListener(this);
            myObserved.removeViewListener(myOverviewRect);
            myObserved.getCanvas().removeComponentListener(myOverviewRect);
        }
    }

    // call this with the view that this overview is supposed to observe
    public void setObserved(JGoView observed) {
        if (observed instanceof BirdsEyeView)
            return; // don't handle watching an overview, including yourself

        JGoView old = myObserved;
        if (old != observed) {
            removeListeners();
            myObserved = observed;
            if (myObserved != null) {
                if (myOverviewRect == null) {
                    myOverviewRect = new OverviewRectangle(myObserved.getViewPosition(), myObserved.getExtentSize());
                    addObjectAtTail(myOverviewRect);
                } else {
                    myOverviewRect.setBoundingRect(myObserved.getViewRect());
                }
                myObserved.getDocument().addDocumentListener(this);
                myObserved.addViewListener(myOverviewRect);
                myObserved.getCanvas().addComponentListener(myOverviewRect);
                firePropertyChange("observed", old, observed);
                updateView();
            }
        }
    }

    // this is the JGoView that's being watched/tracked
    public JGoView getObserved() {
        return myObserved;
    }

    // leave drag-and-drop enabled for autoscrolling,
    // but disallow dragging from other windows to here
    public boolean isDropFlavorAcceptable(DropTargetDragEvent e) {
        return false;
    }

    public int computeAcceptableDrop(DropTargetDragEvent e) {
        return DnDConstants.ACTION_NONE;
    }

    // this is the rectangle in this view that the user drags to
    // change the ViewPosition of the observed view
    public OverviewRectangle getOverviewRect() {
        return myOverviewRect;
    }

    // The rectangle shown and dragged around in the overview window.
    // This class also takes up the additional responsibilities of keeping
    // track of changes to the view.
    public class OverviewRectangle extends JGoRectangle implements JGoViewListener, ComponentListener {
        public OverviewRectangle(Point p, Dimension d) {
            super(p, d);
            // the pen is extra wide so that it can show up reasonably in the
            // scaled-down overview
            //setPen(JGoPen.make(JGoPen.SOLID, 8, new Color(0, 128, 128)));
            setPen(JGoPen.make(JGoPen.SOLID, 4, Color.GRAY));
            setResizable(false);
        }

        // make this JGoRectangle's position and size correspond to the
        // observed view's position and size in the document
        public void updateRectFromView() {
            if (getObserved() == null)
                return;
            if (myChanging)
                return;

            myChanging = true;
            setBoundingRect(getObserved().getViewPosition(), getObserved().getExtentSize());
            scrollRectToVisible(getBoundingRect());
            myChanging = false;
        }

        // don't let this rectangle go negative if the observed view doesn't allow it
        public void setBoundingRect(int left, int top, int width, int height) {
            if (getObserved() != null) {
                Point doctopleft = getObserved().getDocumentTopLeft();
                Dimension docsize = getObserved().getDocumentSize();
                if (left + width > doctopleft.x + docsize.width)
                    left = doctopleft.x + docsize.width - width;
                if (left < doctopleft.x)
                    left = doctopleft.x;
                if (top + height > doctopleft.y + docsize.height)
                    top = doctopleft.y + docsize.height - height;
                if (top < doctopleft.y)
                    top = doctopleft.y;
            }
            if (!isIncludingNegativeCoords()) {
                if (left < 0)
                    left = 0;
                if (top < 0)
                    top = 0;
            }
            super.setBoundingRect(left, top, width, height);
        }

        // handle any change in location (due to dragging) of this rectangle
        // by changing the observed view's viewPosition.
        // ignore any change caused by a change in the observed view
        protected void geometryChange(Rectangle prevRect) {
            if (getObserved() == null)
                return;
            if (myChanging)
                return;

            myChanging = true;
            getObserved().setViewPosition(getTopLeft());
            myChanging = false;
        }

        // selecting the overview rectangle shouldn't show any handles or do
        // anything else
        protected void gainedSelection(JGoSelection selection) {
        }

        protected void lostSelection(JGoSelection selection) {
        }

        // JGoViewListener
        // handle basic changes to the observed view's viewPosition or scale
        public void viewChanged(JGoViewEvent e) {
            switch (e.getHint()) {
                case JGoViewEvent.UPDATE_ALL:
                    // if the observed view's document changed, need to listen to the new
                    // one
                    if (getObserved() != null && getObserved().getDocument() != e.getObject()) {
                        if (e.getObject() instanceof JGoDocument) {
                            JGoDocument oldDoc = (JGoDocument) e.getObject();
                            oldDoc.removeDocumentListener((JGoView) e.getSource());
                        }
                        getObserved().getDocument().addDocumentListener((JGoView) e.getSource());
                    }
                    updateRectFromView();
                    break;
                case JGoViewEvent.POSITION_CHANGED:
                case JGoViewEvent.SCALE_CHANGED:
                    updateRectFromView();
                    break;
            }
        }

        // ComponentListener
        // handle changes in the view's (window) shape
        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentResized(ComponentEvent e) {
            updateRectFromView();
        }

        // Detect when a document or view change should cause a change to the
        // overview rectangle, rather than a user's drag. The former case should
        // not make further changes to the original view; only the user's
        // dragging should do so.
        private boolean myChanging = false;
    }

    // end of OverviewRectangle

    // ignore all document objects; only the OverviewRectangle can be
    // selected
    public JGoObject pickDocObject(Point pointToCheck, boolean selectableOnly) {
        if (getOverviewRect() != null && getOverviewRect().isPointInObj(pointToCheck)) {
            return getOverviewRect();
        }
        return null;
    }

    // don't allow anything to be selected by user's rubber band either
    public void selectInBox(Rectangle rect) {
    }

    // clicking somewhere other than in the overview rectangle will move
    // the view to be centered at that point
    public void doBackgroundClick(int modifiers, Point dc, Point vc) {
        if (getOverviewRect() != null) {
            Rectangle rect = getOverviewRect().getBoundingRect();
            getOverviewRect().setTopLeft(dc.x - rect.width / 2, dc.y - rect.height / 2);
        }
    }

    // make sure this is always looking at the same document as the observed's document
    public JGoDocument getDocument() {
        if (getObserved() != null) {
            return getObserved().getDocument();
        }
        return super.getDocument();
    }

    // ignore any view objects
    public Dimension getDocumentSize() {
        if (getDocument() != null) {
            return getDocument().getDocumentSize();
        }
        return new Dimension();
    }

    public boolean isIncludingNegativeCoords() {
        if (getObserved() != null) {
            return getObserved().isIncludingNegativeCoords();
        }
        return false;
    }

    // show tooltips, so users might get a clue about which object is which
    // even though the objects are so small
    public String getToolTipText(MouseEvent evt) {
        if (getObserved() == null)
            return null;

        Point p = new Point(evt.getPoint());
        convertViewToDoc(p);

        JGoObject obj = getObserved().pickDocObject(p, false);

        while (obj != null) {
            String tip = obj.getToolTipText();
            if (tip != null) {
                return tip;
            }
            obj = obj.getParent();
        }
        return null;
    }

    // State
    private JGoView myObserved = null;
    private OverviewRectangle myOverviewRect = null;

    @Override
    public void setSize(int arg0, int arg1) {
        super.setSize(150, 150);
}

    @Override
    public Dimension getSize() {
        return super.getSize();
    }   

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(10, 10);
    }
}

