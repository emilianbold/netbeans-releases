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
package org.netbeans.modules.bpel.design;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.bpel.design.actions.DesignModeAction;
import org.netbeans.modules.bpel.design.actions.PasteModeAction;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class CopyPasteHandler implements MouseListener {

    private DesignView designView;
    private Pattern copiedPattern;
    private PlaceHolderManager[] managers;
    private PlaceHolder currentPlaceholder;

    public CopyPasteHandler(DesignView designView) {
        this.designView = designView;
        managers = new PlaceHolderManager[]{
            designView.getConsumersView().getPlaceholderManager(),
            designView.getProcessView().getPlaceholderManager(),
            designView.getProvidersView().getPlaceholderManager()
        };
    }

    public void enterPasteMode(Pattern pattern) {
        copiedPattern = pattern;
        currentPlaceholder = null;
        for (PlaceHolderManager m : managers) {
            m.init(pattern);
            m.getDiagramView().addMouseListener(this);
        }
        tabNextPlaceholder(true);
    }

    public void exitPasteMode() {
        copiedPattern = null;
        currentPlaceholder = null;
        for (PlaceHolderManager m : managers) {
            m.clear();
            m.getDiagramView().removeMouseListener(this);
        }

    }

    public boolean isActive() {
        return copiedPattern != null;
    }

    public CopyAction getCopyAction() {
        return new CopyAction();
    }

    public CutAction getCutAction() {
        return new CutAction();
    }

    public PasteAction getPasteAction() {
        return new PasteAction();
    }

    public void tabNextPlaceholder(boolean forward) {

        PlaceHolder next;
        ArrayList<PlaceHolder> placeholders = new ArrayList<PlaceHolder>();

        for (PlaceHolderManager m : managers) {

            List<PlaceHolder> viewPhs = m.getPlaceHolders();
            Collections.sort(viewPhs, new Comparator<PlaceHolder>() {

                public int compare(PlaceHolder o1, PlaceHolder o2) {
                    Rectangle r1 = o1.getShape().getBounds();
                    Rectangle r2 = o2.getShape().getBounds();
                    return ((r1.x * r1.x) + (r1.y * r1.y)) -
                            ((r2.x * r2.x) + (r2.y * r2.y));

                }
            });

            placeholders.addAll(viewPhs);
        }

        if (placeholders.isEmpty()) {
            return;
        }

        if (currentPlaceholder == null) {
            next = placeholders.get(forward ? 0 : placeholders.size() - 1);
        } else {
            int pos = placeholders.indexOf(currentPlaceholder);
            pos = forward ? (pos + 1) : (pos - 1);
            if (pos < 0) {
                pos = placeholders.size() - 1;
            } else if (pos >= placeholders.size()) {
                pos = 0;
            }
            next = placeholders.get(pos);
        }

        for (PlaceHolderManager m : managers) {
            m.setCurrentPlaceholder(next);
        }

        currentPlaceholder = next;
        //find the view owning current placeholder and scroll it to make this placeholder visible
        if (currentPlaceholder != null) {
            for (PlaceHolderManager m : managers) {
                if (m.getPlaceHolders().contains(currentPlaceholder)) {
                    m.getDiagramView().scrollPlaceholderToView(currentPlaceholder);
                    break;
                }
            }

        }

    }

    class PasteAction extends PasteModeAction {

        public PasteAction() {
            super(designView);
        }
        private static final long serialVersionUID = 1L;

        public boolean isEnabled() {
            if (!super.isEnabled()) {
                return false;
            }


            return currentPlaceholder != null;
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }


            currentPlaceholder.drop();

            exitPasteMode();

        }
    }

    public class CutAction extends DesignModeAction {

        public CutAction() {
            super(designView);
        }

        public boolean isEnabled() {
            if (!super.isEnabled()) {
                return false;
            }

            Pattern selPattern = designView.getSelectionModel().getSelectedPattern();
            return selPattern != null && !(selPattern instanceof ProcessPattern);
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }

            enterPasteMode(getPatternCopy(designView.getSelectionModel().getSelectedPattern()));

        }

        private Pattern getPatternCopy(Pattern pattern) {
            if (pattern == null) {
                return null;
            }
            Pattern copiedPattern = null;
            BpelEntity entity = pattern.getOMReference();
            if (entity == null) {
                return null;
            }

            copiedPattern = designView.getModel().createPattern(entity.cut());


            return copiedPattern;
        }
    }

    public class CopyAction extends DesignModeAction {

        public CopyAction() {
            super(designView);
        }

        public boolean isEnabled() {
            if (!super.isEnabled()) {
                return false;
            }

            Pattern selPattern = designView.getSelectionModel().getSelectedPattern();
            return selPattern != null && !(selPattern instanceof ProcessPattern);
        }

        public void actionPerformed(ActionEvent e) {
            //            if (getModel().isReadOnly()) {
//                return;
//            }
            if (!isEnabled()) {
                return;
            }

            Pattern copiedPattern = getPatternCopy(designView.getSelectionModel().getSelectedPattern());
            enterPasteMode(copiedPattern);

        }

        private Pattern getPatternCopy(Pattern pattern) {
            if (pattern == null) {
                return null;
            }
            Pattern copiedPattern = null;
            BpelEntity entity = pattern.getOMReference();
            if (entity == null) {
                return null;
            }

            copiedPattern = designView.getModel().createPattern(entity.copy(new HashMap<UniqueId, UniqueId>()));

            return copiedPattern;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.getComponent() instanceof DiagramView) {
            DiagramView view = (DiagramView) e.getComponent();
            List<PlaceHolder> placeholders =
                    view.getPlaceholderManager().getPlaceHolders();
            FPoint pt = view.convertScreenToDiagram(new Point(e.getX(), e.getY()));        
            for (PlaceHolder p : placeholders) {
                if (p.contains(pt.x, pt.y)) {
                    p.drop();
                    break;
                }
            }
            exitPasteMode();

        }

    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }
}
