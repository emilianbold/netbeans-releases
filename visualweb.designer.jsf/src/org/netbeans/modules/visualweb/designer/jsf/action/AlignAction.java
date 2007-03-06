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


package org.netbeans.modules.visualweb.designer.jsf.action;

import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 * Action providing aligning.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performActionAt impl)
 *
 * @todo Implement it the way it works against context and not
 * against the selection (the old impl is still in place).
 */
public class AlignAction extends AbstractDesignBeanAction {

    /** Creates a new instance of AlignAction. */
    public AlignAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        String alignName = NbBundle.getMessage(AlignAction.class, "LBL_AlignAction");
        if (designBeans.length == 0) {
            return alignName;
        }

//        WebForm webform = WebForm.findWebFormForDesignContext(designBeans[0].getDesignContext());
        Designer designer = JsfSupportUtilities.getDesignerForDesignContext(designBeans[0].getDesignContext());
//        if (webform == null) {
        if (designer == null) {
            return alignName;
//        } else if (webform.getSelection().getNumSelected() == 1) {
        } else if (designer.getSelectedCount() == 1) {
            return AlignMenuModel.SNAP_TO_GRID.getDisplayName();
        } else {
            return alignName;
        }
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return false;
        }

        // XXX FIXME Cannot be an insync dependency.
        if (!LiveUnit.isTrayBean(designBeans[0])) {
//        if (designBeans[0] instanceof MarkupDesignBean && !WebForm.getHtmlDomProviderService().isTrayComponent(
//                WebForm.getHtmlDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBeans[0]))) {
//            WebForm webform = WebForm.findWebFormForDesignContext(designBeans[0].getDesignContext());
//            if (webform != null && webform.getSelection().getNumSelected() > 0) {
            Designer designer = JsfSupportUtilities.getDesignerForDesignContext(designBeans[0].getDesignContext());
            if (designer != null && designer.getSelectedCount() > 0) {
                return true;
            } else {
                return false;
            }
        } else {
             return false;
        }
    }

    protected void performAction(DesignBean[] designBeans) {
        // XXX Strange impl of the Actions.SubMenu(action, model, isPopup). If the model provides one item,
        // it doesn't call the performAt(0), but this method.
        new AlignMenuModel(designBeans).performActionAt(0);
    }

    protected JMenuItem getMenuPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.SubMenu(contextAwareAction, new AlignMenuModel(getDesignBeans(result)), false);
    }

    protected JMenuItem getPopupPresenter(Action contextAwareAction, Lookup.Result result) {
        return new Actions.SubMenu(contextAwareAction, new AlignMenuModel(getDesignBeans(result)), true);
    }


    /** Implementation of the actions submenu model. */
    private static class AlignMenuModel implements Actions.SubMenuModel {
        private static abstract class Alignment {
            private final Designer.Alignment designerAlignment;
            private final String displayName;

            public Alignment(Designer.Alignment designerAlignment, String displayName) {
                this.designerAlignment = designerAlignment;
                this.displayName = displayName;
            }
            
            public String getDisplayName() {
                return displayName;
            }
            
            public Designer.Alignment getDesignerAlignment() {
                return designerAlignment;
            }

//            protected abstract void performAction(WebForm webform);
            protected abstract void performAction(Designer designer);
        } // End of Alignment class.
        
        private static class SnapToGridAlignment extends Alignment {
            public SnapToGridAlignment(String displayName) {
                super(Designer.Alignment.SNAP_TO_GRID, displayName);
            }
            
//            protected void performAction(WebForm webform) {
//                snapToGrid(webform);
//            }
            protected void performAction(Designer designer) {
                if (designer != null) {
                    designer.snapToGrid();
                }
            }
        } // End of SnapToGridAlignment class.
        
        private static class SimpleAlignment extends Alignment {
            public SimpleAlignment(Designer.Alignment designerAlignment, String displayName) {
                super(designerAlignment, displayName);
            }
            
//            protected void performAction(WebForm webform) {
//                align(webform, alignmentType);
//            }
            protected void performAction(Designer designer) {
                if (designer != null) {
                    designer.align(getDesignerAlignment());
                }
            }
        } // End of SimpleAlignment class.
        
        // XXX Make an enum once moved to jdk5.0 sources.
        private static final Alignment SNAP_TO_GRID = new SnapToGridAlignment(NbBundle.getMessage(AlignAction.class, "LBL_SnapToGrid"));
        private static final Alignment TOP          = new SimpleAlignment(Designer.Alignment.TOP, NbBundle.getMessage(AlignAction.class, "LBL_Top"));
        private static final Alignment MIDDLE       = new SimpleAlignment(Designer.Alignment.MIDDLE, NbBundle.getMessage(AlignAction.class, "LBL_Middle"));
        private static final Alignment BOTTOM       = new SimpleAlignment(Designer.Alignment.BOTTOM, NbBundle.getMessage(AlignAction.class, "LBL_Bottom"));
        private static final Alignment LEFT         = new SimpleAlignment(Designer.Alignment.LEFT, NbBundle.getMessage(AlignAction.class, "LBL_Left"));
        private static final Alignment CENTER       = new SimpleAlignment(Designer.Alignment.CENTER, NbBundle.getMessage(AlignAction.class, "LBL_Center"));
        private static final Alignment RIGHT        = new SimpleAlignment(Designer.Alignment.RIGHT, NbBundle.getMessage(AlignAction.class, "LBL_Right"));
        
//        private static final Alignment[] alignments   = new Alignment[] {
//            SNAP_TO_GRID, TOP, MIDDLE, BOTTOM, LEFT, CENTER, RIGHT};
        private final Alignment[] alignments;
        
        private final DesignBean[] designBeans;
        
        public AlignMenuModel(DesignBean[] designBeans) {
            this.designBeans = designBeans;
            
//            WebForm webform = WebForm.findWebFormForDesignContext(designBeans[0].getDesignContext());
//            if (webform == null) {
            Designer designer = JsfSupportUtilities.getDesignerForDesignContext(designBeans[0].getDesignContext());
            if (designer == null) {
                this.alignments = new Alignment[0];
//            } else if (webform.getSelection().getNumSelected() == 1) {
            } else if (designer.getSelectedCount() == 1) {
                this.alignments = new Alignment[] {SNAP_TO_GRID};
            } else {
                this.alignments = new Alignment[] {SNAP_TO_GRID, TOP, MIDDLE, BOTTOM, LEFT, CENTER, RIGHT};
            }
        }
        
        
        public int getCount() {
            return alignments.length;
        }

        public String getLabel(int i) {
            return alignments[i].getDisplayName();
        }

        public HelpCtx getHelpCtx(int i) {
            // XXX Implement?
            return null;
        }

        public void performActionAt(int i) {
            if (designBeans.length == 0) {
                return;
            }
            
//            WebForm webform = WebForm.findWebFormForDesignContext(designBeans[0].getDesignContext());
//            if (webform == null) {
//                return;
//            }
            Designer designer = JsfSupportUtilities.getDesignerForDesignContext(designBeans[0].getDesignContext());
            if (designer == null) {
                return;
            }
            
//            alignments[i].performAction(webform);
            alignments[i].performAction(designer);
        }

        public void addChangeListener(ChangeListener changeListener) {
            // dummy, this model is not mutable.
        }

        public void removeChangeListener(ChangeListener changeListener) {
            // dummy, this model is not mutable.
        }
        
    } // End of AlignMenuModel.
    

//    // XXX Copied from before DesignerActions
//    /** Snap selection to grid */
//    private static void snapToGrid(WebForm webform) {
////        GridHandler handler = GridHandler.getInstance();
//        GridHandler handler = webform.getGridHandler();
//        DesignerPane editor = webform.getPane();
//        SelectionManager sm = webform.getSelection();
////        Iterator it = sm.iterator();
//        Element[] componentRootElements = sm.getSelectedComponentRootElements();
////        ModelViewMapper mapper = webform.getMapper();
//        boolean haveMoved = false;
////        Document doc = webform.getDocument();
//
////        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(AlignAction.class, "LBL_SnapToGrid")); // NOI18N
//        HtmlDomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(AlignAction.class, "LBL_SnapToGrid")); // NOI18N
//        try {
////            doc.writeLock(NbBundle.getMessage(AlignAction.class, "LBL_SnapToGrid")); // NOI18N
//
////            while (it.hasNext()) {
////                MarkupDesignBean bean = (MarkupDesignBean)it.next();
//            for (Element componentRootElement : componentRootElements) {
////                MarkupDesignBean bean = WebForm.getHtmlDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
////                CssBox box = mapper.findBox(bean);
//                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
//
//                if (box == null) {
//                    continue;
//                }
//
//                boolean canAlign = box.getBoxType().isAbsolutelyPositioned();
//
//                if (!canAlign) {
//                    continue;
//                }
//
//                int x = box.getAbsoluteX();
//                int y = box.getAbsoluteY();
//                handler.moveTo(editor, /*bean,*/ box, x, y, false);
//                haveMoved = true;
//            }
//        } finally {
////            doc.writeUnlock();
////            webform.getModel().writeUnlock(undoEvent);
//            webform.writeUnlock(writeLock);
//        }
//
//        if (!haveMoved) {
//            StatusDisplayer.getDefault().setStatusText(
//                    NbBundle.getMessage(AlignAction.class, "MSG_AlignAbsolute"));
//            UIManager.getLookAndFeel().provideErrorFeedback(webform.getPane());
//        }
//    }
//
//    // XXX Copied from the former DesignerActions
//    private static final int ALIGN_TOP    = 0;
//    private static final int ALIGN_MIDDLE = 1;
//    private static final int ALIGN_BOTTOM = 2;
//    private static final int ALIGN_LEFT   = 3;
//    private static final int ALIGN_CENTER = 4;
//    private static final int ALIGN_RIGHT  = 5;
//    
//    /** Align selection to the primary selection item */
//    private static void align(WebForm webform, int direction) {
//        // Primary
//        SelectionManager sm = webform.getSelection();
//
//        if (sm.isSelectionEmpty()) {
//            return;
//        }
//
//        sm.pickPrimary();
//
////        ModelViewMapper mapper = webform.getMapper();
////        CssBox primaryBox = mapper.findBox(sm.getPrimary());
//        CssBox primaryBox = ModelViewMapper.findBox(webform.getPane().getPageBox(), sm.getPrimary());
//
//        if (primaryBox == null) {
//            return;
//        }
//
//        boolean haveMoved = false;
////        Document doc = webform.getDocument();
//
////        UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(SelectionManager.class, "Align")); // NOI18N
//        HtmlDomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(SelectionManager.class, "Align")); // NOI18N
//        try {
////            doc.writeLock(NbBundle.getMessage(SelectionManager.class, "Align")); // NOI18N
//
////            GridHandler handler = GridHandler.getInstance();
//            GridHandler handler = webform.getGridHandler();
//            DesignerPane editor = webform.getPane();
//            boolean canAlign = primaryBox.getBoxType().isAbsolutelyPositioned();
//            int x = primaryBox.getAbsoluteX();
//            int y = primaryBox.getAbsoluteY();
//            int w = primaryBox.getWidth();
//            int h = primaryBox.getHeight();
////            Iterator it = sm.iterator();
////
////            while (canAlign && it.hasNext()) {
////                MarkupDesignBean bean = (MarkupDesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
////                MarkupDesignBean bean = WebForm.getHtmlDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
////                CssBox box = mapper.findBox(bean);
//                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
//
//                if (box == null) {
//                    continue;
//                }
//
//                // XXX Should I use isPositioned() instead? (e.g. are relative
//                // positioned boxes alignable?
//                if (!box.getBoxType().isAbsolutelyPositioned()) {
//                    continue;
//                }
//
//                haveMoved = true;
//
//                /*
//                 Element element = FacesSupport.getElement(fob.component);
//                 if (element == null) {
//                 continue;
//                 }
//                 */
//                switch (direction) {
//                case ALIGN_TOP:
//                    handler.moveTo(editor, /*bean,*/ box, box.getAbsoluteX(), y, true);
//
//                    break;
//
//                case ALIGN_MIDDLE:
//                    handler.moveTo(editor, /*bean,*/ box, box.getAbsoluteX(),
//                        (y + (h / 2)) - (box.getHeight() / 2), true);
//
//                    break;
//
//                case ALIGN_BOTTOM:
//                    handler.moveTo(editor, /*bean,*/ box, box.getAbsoluteX(),
//                        (y + h) - box.getHeight(), true);
//
//                    break;
//
//                case ALIGN_LEFT:
//                    handler.moveTo(editor, /*bean,*/ box, x, box.getAbsoluteY(), true);
//
//                    break;
//
//                case ALIGN_CENTER:
//                    handler.moveTo(editor, /*bean,*/ box, (x + (w / 2)) - (box.getWidth() / 2),
//                        box.getAbsoluteY(), true);
//
//                    break;
//
//                case ALIGN_RIGHT:
//                    handler.moveTo(editor, /*bean,*/ box, (x + w) - box.getWidth(), box.getAbsoluteY(),
//                        true);
//
//                    break;
//                }
//            }
//        } finally {
////            doc.writeUnlock();
////            webform.getModel().writeUnlock(undoEvent);
//            webform.writeUnlock(writeLock);
//        }
//
//        if (!haveMoved) {
//            StatusDisplayer.getDefault().setStatusText(
//                    NbBundle.getMessage(AlignAction.class,"MSG_AlignAbsolute"));
//            UIManager.getLookAndFeel().provideErrorFeedback(webform.getPane());
//        }
//    }
}
