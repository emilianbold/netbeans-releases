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

package org.netbeans.core.output2;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.netbeans.core.output2.ui.AbstractOutputWindow;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * An output window.  Note this class contains no logic of interest - all
 * events of interest are passed to the <code>Controller</code> which
 * manages this instance (and possibly others).
 * <p>
 * The mechanism for displaying/not displaying the tabbed pane is handled in
 * the superclass, which overrides addImpl() and remove() to automatically install
 * the tabbed pane if more than one view is added, and remove it if only one
 * is present - so it is enough to simply call add() and remove() with instances
 * of OutputTab and the management of tabs will be taken care of automatically.
 */
public class OutputWindow extends AbstractOutputWindow {
    private Controller controller;
    static OutputWindow DEFAULT = null;
    public static final String ICON_RESOURCE =
        "org/netbeans/core/resources/frames/output.png"; // NOI18N
        
    private MouseListener activateListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            //#83829
            requestActive();
        }
    };

    public OutputWindow() {
        this (new Controller());
        enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        putClientProperty ("dontActivate", Boolean.TRUE);
        getActionMap().put("PreviousViewAction", controller.prevTabAction);
        getActionMap().put("NextViewAction", controller.nextTabAction);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        pane.addMouseListener(activateListener);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        pane.removeMouseListener(activateListener);
    }
    

    protected void closeRequest(AbstractOutputTab tab) {
        controller.close (this, (OutputTab) tab, false);
    }

    OutputWindow (Controller controller) {
        if (Controller.LOG) Controller.log("Created an output window");
        this.controller = controller;
        setDisplayName (NbBundle.getMessage(OutputWindow.class, "LBL_OUTPUT")); //NOI18N
        // setting name to satisfy the accesible name requirement for window.
        setName (NbBundle.getMessage(OutputWindow.class, "LBL_OUTPUT")); //NOI18N
        
        setIcon(ImageUtilities.loadImage(ICON_RESOURCE)); // NOI18N
         // special title for sliding mode
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", getDisplayName()); //NOI18N 
    }
    
    public static synchronized OutputWindow findDefault() {
        if (DEFAULT == null) {
            //If settings file is correctly defined call of WindowManager.findTopComponent() will
            //call TestComponent00.getDefault() and it will set static field component.
            TopComponent tc = WindowManager.getDefault().findTopComponent("output"); // NOI18N
            if (tc != null) {
                if (!(tc instanceof OutputWindow)) {
                    //This should not happen. Possible only if some other module
                    //defines different settings file with the same name but different class.
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + OutputWindow.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    Logger.getLogger(OutputWindow.class.getName()).log(Level.WARNING, null, exc);
                    //Fallback to accessor reserved for window system.
                    OutputWindow.getDefault();
                }
            } else {
                OutputWindow.getDefault();
            }
        }
        return DEFAULT;
    }
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * OutputWindow instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of OutputWindow. */
    public static synchronized OutputWindow getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new OutputWindow();
        }
        return DEFAULT;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String preferredID() {
        return "output"; //NOI18N
    }

    public Object readResolve() throws java.io.ObjectStreamException {
        return getDefault();
    }
    
    @Override
    public String getToolTipText() {
        return getDisplayName();
    }

    Controller getController() {
        return controller;
    }

    @Override
    public void requestVisible () {
        if (Controller.LOG) {
            Controller.log("Request visible");
            Controller.logStack();
        }
        super.requestVisible();
    }
    
    void requestVisibleForNewTab() {
        if (Controller.LOG) Controller.log("Request visible for new tab");
        if (isOpened() && isShowing()) {
            if (!isActivated()) {
                super.requestVisible();
            }
        } else {
            if (Controller.LOG) Controller.log ("CALLING OPEN() ON OUTPUT WINDOW!");
            open();
            super.requestVisible();
            if (Boolean.TRUE.equals(getClientProperty("isSliding"))) { //NOI18N
                requestActiveForNewTab();
            } 
        }
    }
    
    public void processFocusEvent (FocusEvent fe) {
        super.processFocusEvent (fe);
        if (Boolean.TRUE.equals(getClientProperty("isSliding"))) { //NOI18N
            repaint(200);
        }
    }
    
    public void paintComponent (Graphics g) {
        super.paintComponent (g);
        if (hasFocus()) {
            Insets ins = getInsets();
            Color col = UIManager.getColor ("controlShadow"); //NOI18N
            //Draw *some* focus indication
            if (col == null) {
                col = java.awt.Color.GRAY;
            }
            g.setColor(col);
            g.drawRect (
                ins.left + 2,
                ins.top + 2,
                getWidth() - (ins.left + ins.right + 4),
                getHeight() - (ins.top + ins.bottom + 4));
        }
    }
    
    void requestActiveForNewTab() {
        requestActive();
    }
    
    @Override
    public void requestActive() {
        boolean act = isActivated();
        if (Controller.LOG) Controller.log("Request active");
        super.requestActive();
        if (!act) {
            requestFocus();
        }
    }  
    
    private boolean activated = false;
    @Override
    protected void componentActivated () {
        if (Controller.LOG) Controller.log("ComponentActivated");
        super.componentActivated();
        activated = true;
        controller.notifyActivated (this);
        requestFocus();
    }
    
    @Override
    protected void componentDeactivated() {
        if (Controller.LOG) Controller.log("ComponentDeactivated");
        super.componentDeactivated();
        activated = false;
    }
    
    protected void removed(AbstractOutputTab view) {
        if (Controller.LOG) Controller.log("Removed tab " + view);
        if (Controller.LOG) Controller.log ("Tab has been removed.  Notifying controller.");
        controller.notifyRemoved((OutputTab) view);
    }

    protected void selectionChanged(AbstractOutputTab former, AbstractOutputTab current) {
        controller.selectionChanged (this, (OutputTab) former, (OutputTab) current);
    }

    void lineClicked(OutputTab outputComponent, int line) {
        controller.lineClicked (this, outputComponent, line);
    }

    void postPopupMenu(OutputTab outputComponent, Point p, Component src) {
        controller.postPopupMenu (this, outputComponent, p, src);
    }

    void caretEnteredLine(OutputTab outputComponent, int line) {
        controller.caretEnteredLine(outputComponent, line);
    }

    void documentChanged(OutputTab comp) {
        controller.documentChanged (this, comp);
    }

    private HashSet<OutputTab> hiddenTabs = null;
    void putHiddenView (OutputTab comp) {
        if (hiddenTabs == null) {
            hiddenTabs = new HashSet<OutputTab>();
        }
        comp.putClientProperty("outputWindow", this); //NOI18N
        hiddenTabs.add(comp);
        if (comp.getParent() != null) {
            comp.getParent().remove(comp);
        }
    }

    void removeHiddenView (OutputTab comp) {
        hiddenTabs.remove(comp);
        comp.putClientProperty("outputWindow", null); //NOI18N
    }

    @Override
    public void setSelectedTab (AbstractOutputTab op) {
        if (op.getParent() == null && hiddenTabs.contains(op)) {
            removeHiddenView ((OutputTab) op);
            add(op);
        }
        super.setSelectedTab (op);
    }

    protected void updateSingletonName(String name) {
        String winName = NbBundle.getMessage(OutputWindow.class, "LBL_OUTPUT"); //NOI18N
        if (name != null) {
            String newName = NbBundle.getMessage(OutputWindow.class,
                "FMT_OUTPUT", new Object[] {winName, name}); //NOI18N
            if (newName.indexOf ("<html>") != -1) {
                newName = Utilities.replaceString(newName, "<html>", ""); //NOI18N
                setHtmlDisplayName("<html>" + newName); //NOI18N
            } else {
                setDisplayName(newName);
                setHtmlDisplayName(null);
            }
        } else {
            setDisplayName(winName);
            setHtmlDisplayName(null);
        }
    }


    OutputTab[] getHiddenTabs() {
        if (hiddenTabs != null && !hiddenTabs.isEmpty()) {
            OutputTab[] result = new OutputTab[hiddenTabs.size()];
            return hiddenTabs.toArray(result);
        }
        return new OutputTab[0];
    }

    OutputTab getTabForIO (NbIO io) {
        AbstractOutputTab[] views = getTabs();
        for (int i=0; i < views.length; i++) {
            if (((OutputTab) views[i]).getIO() == io) {
                return ((OutputTab) views[i]);
            }
        }
        OutputTab[] hidden = getHiddenTabs();
        for (int i=0; i < hidden.length; i++) {
            if (hidden[i].getIO() == io) {
                return hidden[i];
            }
        }
        return null;
    }

    void eventDispatched(IOEvent ioe) {
            if (Controller.LOG) Controller.log ("Event received: " + ioe);
            NbIO io = ioe.getIO();
            int command = ioe.getCommand();
            boolean value = ioe.getValue();
            Object data = ioe.getData();
            OutputTab comp = getTabForIO (io);
            if (command == IOEvent.CMD_DETACH) {
                if (!ioe.isConsumed()) {
                    //Can be used by ModuleInstall to dispose of the current output window if desired
                    ioe.consume();
                    DEFAULT = null;
                    return;
                }
            }
            if (Controller.LOG) Controller.log ("Passing command to controller " + ioe);
            controller.performCommand (this, comp, io, command, value, data);
            ioe.consume();
    }

    void hasSelectionChanged(OutputTab tab, boolean val) {
        controller.hasSelectionChanged(this, tab, val);
    }

    public boolean isActivated() {
        return activated;
    }

    void hasOutputListenersChanged(OutputTab tab, boolean hasOutputListeners) {
        controller.hasOutputListenersChanged(this, tab, hasOutputListeners);
    }

    void inputEof(OutputTab tab) {
        if (Controller.LOG) Controller.log ("Input EOF on " + this);
        controller.inputEof(tab);
    }

    void inputSent(OutputTab c, String txt) {
        if (Controller.LOG) Controller.log ("Notifying controller input sent " + txt);
        controller.notifyInput(this, c, txt);
    }
}
