/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.junit.output.ResultDisplayHandler.DisplayContents;
import org.netbeans.modules.junit.output.ResultDisplayHandler.ToggleViewAction;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 *
 * @author Marian Petras
 */
final class ResultDisplayHandler {
    
    /** */
    private static final String ID_TREE = "tree";                       //NOI18N
    /** */
    private static final String ID_OUTPUT = "output";                   //NOI18N
    
    /** */
    private MultiViewHandler handler;
    /** */
    private MultiViewPerspective treeViewHandle;
    /** */
    private MultiViewPerspective outputViewHandle;
    /** */
    private ResultPanelTree treePanel;
    /** */
    private ResultPanelOutput outputPanel;
    /** */
    private DisplayContents display;
    /** */
    private Collection/*<ChangeListener>*/ changeListeners;
    
    
    /** Creates a new instance of ResultView */
    ResultDisplayHandler() {
    }
    
    /**
     */
    TopComponent createReportDisplay() {
        AbstractResultViewDesc descTree
                = new ResultTreeViewDesc(ID_TREE,
                                         "LBL_resultTreeView");         //NOI18N
        AbstractResultViewDesc descOutput
                = new ResultOutputViewDesc(ID_OUTPUT,
                                           "LBL_resultOutputView");     //NOI18N
        TopComponent tc = MultiViewFactory.createMultiView(
                    new MultiViewDescription[] { descTree, descOutput },
                    descTree);
        setUpNavigation(tc);
                    
        handler = MultiViews.findMultiViewHandler(tc);
        
        MultiViewPerspective[] viewHandles = handler.getPerspectives();
        treeViewHandle = viewHandles[0];
        outputViewHandle = viewHandles[1];
        
        return tc;
    }
    
    /**
     */
    private void setUpNavigation(JComponent c) {
        final String actionName = "toggleView";                         //NOI18N
        
        final int keyModifiers = InputEvent.ALT_DOWN_MASK
                                 | InputEvent.SHIFT_DOWN_MASK;

        final InputMap inputMap = c.getInputMap(
                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,  keyModifiers),
                     actionName);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, keyModifiers),
                     actionName);
        
        c.getActionMap().put(actionName, new ToggleViewAction());
    }
    
    /**
     *
     */
    final class ToggleViewAction extends AbstractAction {
        
        /*
         * PENDING
         * This is a hack! The default mechanism of MultiView should be used!
         */
        
        /**
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            MultiViewPerspective currView = handler.getSelectedPerspective();
            
            if (currView == treeViewHandle) {
                handler.requestActive(outputViewHandle);
            } else if (currView == outputViewHandle) {
                handler.requestActive(treeViewHandle);
            } else {
                assert false;
            }
        }
        
    }
    
    /**
     */
    void displayMsg(String msg) {
        assert EventQueue.isDispatchThread();
        
        setDisplay(new DisplayContents(msg, null));
        
        //if (handler != null) {
        //    handler.requestActive(treeViewHandle);
        //}
}
    
    /**
     */
    void displayReport(final Report report) {
        assert EventQueue.isDispatchThread();
        
        setDisplay(new DisplayContents(null, report));
        
        //if (handler != null) {
        //    handler.requestActive(treeViewHandle);
        //}
    }
    
    /**
     */
    private void fireChange() {
        if (changeListeners != null) {
            ChangeEvent e = new ChangeEvent(this);
            for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                ((ChangeListener) i.next()).stateChanged(e);
            }
        }
    }
    
    /**
     */
    void addChangeListener(ChangeListener l) {
        String listenerName;
        if (l == null) {
            listenerName = "<null>";
        } else {
            listenerName = l.getClass().getName();
            int lastDotIndex = listenerName.lastIndexOf('.');
            if (lastDotIndex != -1) {
                listenerName = listenerName.substring(lastDotIndex + 1);
            }
        }
        if (l != null) {
            if (changeListeners == null) {
                changeListeners = new ArrayList(2);
            }
            changeListeners.add(l);
        }
    }
    
    /**
     */
    void removeChangeListener(ChangeListener l) {
        if ((l != null)
                && (changeListeners != null)
                && changeListeners.remove(l)
                && changeListeners.isEmpty()) {
            changeListeners = null;
        }
    }
    
    /**
     */
    DisplayContents getDisplay() {
        assert EventQueue.isDispatchThread();
        
        return display;
    }
    
    /**
     */
    private void setDisplay(DisplayContents display) {
        this.display = display;
        fireChange();
    }
    
    
    /**
     *
     */
    static final class DisplayContents {
       private String msg;
       private Report report;
       private DisplayContents(String msg, Report report) {
           this.msg = msg;
           this.report = report;
       }
       String getMessage() {
           return msg;
       }
       Report getReport() {
           return report;
       }
    }
    
    /**
     * Partial implementation of a class describing single view of the multiview
     * result window.
     *
     * @author  Marian Petras
     */
    private abstract static class AbstractResultViewDesc
                                            implements MultiViewDescription {
        
        /**
         * this view's ID part 
         * - ID of this view among all views of the result window
         *
         * @serial  this view's ID part - ID of this view among all views
         *          of the result window
         */
        private final String idPart;
        private transient String id;
        /**
         * bundle key for this view's display name
         *
         * @serial  bundle key for this view's display name
         *          - the corresponding entry must exist in the module's bundle
         */
        private final String displayNameKey;
        private transient String displayName;
        
        private AbstractResultViewDesc(String id,
                                       String displayNameKey) {
            this.idPart = id;
            this.displayNameKey = displayNameKey;
        }

        public String preferredID() {
            //PENDING:
            return idPart;
        }

        public String getDisplayName() {
            if (displayName == null) {
                try {
                    displayName = NbBundle.getMessage(ResultDisplayHandler.class,
                                                      displayNameKey);
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,
                                                     ex);
                    displayName = "??? (" + displayNameKey + ')';       //NOI18N
                }
            }
            return displayName;
        }

        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

    }
    
    /**
     * Descriptor of the tree view of the result window multiview.
     *
     * @author  Marian Petras
     */
    private final class ResultTreeViewDesc extends AbstractResultViewDesc
                                           implements Serializable {
        
        static final long serialVersionUID = -4369549644387784567L;
        
        private ResultTreeViewDesc(final String id,
                                   final String displayNameKey) {
            super(id, displayNameKey);
        }

        public java.awt.Image getIcon() {
            return null;
        }

        public MultiViewElement createElement() {
            return new ResultViewTree(ResultDisplayHandler.this);
        }
        
        public HelpCtx getHelpCtx() {
            //PENDING:
            return HelpCtx.DEFAULT_HELP;
        }

    }
    
    /**
     * Descriptor of the output view of the result window multiview.
     *
     * @author  Marian Petras
     */
    private final class ResultOutputViewDesc extends AbstractResultViewDesc
                                             implements Serializable {
        
        static final long serialVersionUID = -6007918504501341013L;
        
        private ResultOutputViewDesc(final String id,
                                     final String displayNameKey) {
            super(id, displayNameKey);
        }

        public java.awt.Image getIcon() {
            return null;
        }

        public MultiViewElement createElement() {
            return new ResultViewOutput(ResultDisplayHandler.this);
        }
        
        public HelpCtx getHelpCtx() {
            //PENDING:
            return HelpCtx.DEFAULT_HELP;
        }

    }
    
}
