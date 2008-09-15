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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.modules.bpel.design.actions.PasteModeAction;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

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
    private CopyAction actionCopy;
    private CutAction actionCut; 
    private PasteAction actionPaste; 

    public CopyPasteHandler(DesignView designView) {
        this.designView = designView;
        actionCopy = new CopyAction();
        actionCut = new CutAction();
        actionPaste = new PasteAction();

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
        fireUpdateActionEnabled();
    }

    public void exitPasteMode() {
        copiedPattern = null;
        currentPlaceholder = null;
        
        for (PlaceHolderManager m : managers) {
            m.clear();
            m.getDiagramView().removeMouseListener(this);
        }

        fireUpdateActionEnabled();
    }

    public boolean isActive() {
        return copiedPattern != null;
    }

    public Action getCopyAction() {
        return actionCopy;
    }

    public Action getCutAction() {
        return actionCut;
    }

    public Action getPasteAction() {
        return actionPaste;
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

    private void fireUpdateActionEnabled() {

        actionPaste.fireUpdateActionEnabled();


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

        private void fireUpdateActionEnabled() {
            
            
            
            firePropertyChange(NodeAction.PROP_ENABLED, 
                    new Boolean(!isEnabled()), 
                    new Boolean(isEnabled()));

        }
    }

    public class CopyAction extends CopyCutAction{
        public CopyAction(){
            super(true);
        }
        
    }
    public class CutAction extends CopyCutAction{
        public CutAction(){
            super(false);
        }
        
    }
    
    protected class CopyCutAction extends BpelNodeAction {

        private boolean copy;

        public CopyCutAction(boolean copy) {
            super(true);
            
            this.copy = copy;
            
            putValue(ACCELERATOR_KEY, 
                    KeyStroke.getKeyStroke(
                        copy ? KeyEvent.VK_C : KeyEvent.VK_X, 
                        KeyEvent.CTRL_DOWN_MASK));
        }

        protected boolean enable(BpelEntity[] bpelEntities) {
            if (!super.enable(bpelEntities)) {
                return false;
            }

            if (bpelEntities.length != 1) {
                return false;
            }

            return !(bpelEntities[0] instanceof org.netbeans.modules.bpel.model.api.Process);
        }

        public ActionType getType() {
            return copy ? ActionType.CLIPBOARD_COPY : ActionType.CLIPBOARD_CUT;
        }

        @Override
        protected String getBundleName() {
            return NbBundle.getMessage(CopyPasteHandler.class,
                    copy ? "ACT_ClipboardCopy" : "ACT_ClipboardCut"); //NOI18N
        }

        @Override
        protected void performAction(BpelEntity[] bpelEntities) {
            exitPasteMode();
            if (!enable(bpelEntities)) {
                return;
            }
            Pattern pattern = getPatternCopy(bpelEntities[0]);

            enterPasteMode(pattern);
        }



        private Pattern getPatternCopy(BpelEntity entity) {
            if (entity == null) {
                return null;
            }


            BpelEntity new_entity = copy ? entity.copy(new HashMap<UniqueId, UniqueId>()) : entity.cut();

            return designView.getModel().createPattern(new_entity);

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
