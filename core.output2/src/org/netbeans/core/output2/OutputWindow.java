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

package org.netbeans.core.output2;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.util.HashSet;
import javax.swing.UIManager;
import org.netbeans.core.output2.ui.AbstractOutputTab;
import org.netbeans.core.output2.ui.AbstractOutputWindow;
import org.openide.ErrorManager;
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
        "org/netbeans/core/resources/frames/output.gif"; // NOI18N
        

    public OutputWindow() {
        this (new Controller());
        enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        putClientProperty ("dontActivate", Boolean.TRUE);
        getActionMap().put("PreviousViewAction", controller.prevTabAction);
        getActionMap().put("NextViewAction", controller.nextTabAction);
    }

    protected void closeRequest(AbstractOutputTab tab) {
        controller.close (this, (OutputTab) tab, false);
    }

    OutputWindow (Controller controller) {
        if (Controller.log) Controller.log("Created an output window");
        this.controller = controller;
        setDisplayName (NbBundle.getMessage(OutputWindow.class, "LBL_OUTPUT")); //NOI18N
        setIcon(Utilities.loadImage(ICON_RESOURCE)); // NOI18N
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
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
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

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    public String preferredID() {
        return "output"; //NOI18N
    }

    public Object readResolve() throws java.io.ObjectStreamException {
        return getDefault();
    }
    
    public String getToolTipText() {
        return getDisplayName();
    }

    Controller getController() {
        return controller;
    }

    public void requestVisible () {
        if (Controller.log) {
            Controller.log("Request visible");
            Controller.logStack();
        }
        super.requestVisible();
    }
    
    void requestVisibleForNewTab() {
        if (Controller.log) Controller.log("Request visible for new tab");
        if (isOpened() && isShowing()) {
            if (!isActivated()) {
                super.requestVisible();
            }
        } else {
            if (Controller.log) Controller.log ("CALLING OPEN() ON OUTPUT WINDOW!");
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
            if (col == null) col = Color.GRAY;
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
    
    public void requestActive() {
        boolean activated = isActivated();
        if (Controller.log) Controller.log("Request active");
        super.requestActive();
        if (!activated) {
            requestFocus();
        }
    }  
    
    private boolean activated = false;
    protected void componentActivated () {
        if (Controller.log) Controller.log("ComponentActivated");
        super.componentActivated();
        activated = true;
        controller.notifyActivated (this);
        requestFocus();
    }
    
    protected void componentDeactivated() {
        if (Controller.log) Controller.log("ComponentDeactivated");
        super.componentDeactivated();
        activated = false;
    }
    
    protected void removed(AbstractOutputTab view) {
        if (Controller.log) Controller.log("Removed tab " + view);
        if (Controller.log) Controller.log ("Tab has been removed.  Notifying controller.");
        controller.notifyRemoved((OutputTab) view);
    }

    protected void selectionChanged(AbstractOutputTab former, AbstractOutputTab current) {
        controller.selectionChanged (this, (OutputTab) former, (OutputTab) current);
    }

    public void lineClicked(OutputTab outputComponent, int line) {
        controller.lineClicked (this, outputComponent, line);
    }

    public void postPopupMenu(OutputTab outputComponent, Point p, Component src) {
        controller.postPopupMenu (this, outputComponent, p, src);
    }

    public void caretEnteredLine(OutputTab outputComponent, int line) {
        controller.caretEnteredLine(outputComponent, line);
    }

    public void documentChanged(OutputTab comp) {
        controller.documentChanged (this, comp);
    }

    private HashSet hiddenTabs = null;
    public void putHiddenView (OutputTab comp) {
        if (hiddenTabs == null) {
            hiddenTabs = new HashSet();
        }
        comp.putClientProperty("outputWindow", this); //NOI18N
        hiddenTabs.add(comp);
        if (comp.getParent() != null) {
            comp.getParent().remove(comp);
        }
    }

    public void removeHiddenView (OutputTab comp) {
        hiddenTabs.remove(comp);
        comp.putClientProperty("outputWindow", null); //NOI18N
    }

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
            String newName = hackHtml(NbBundle.getMessage(OutputWindow.class,
                "FMT_OUTPUT", new Object[] {winName, name})); //NOI18N
            setDisplayName(newName);
        } else {
            setDisplayName(winName);
        }
    }

    private static String hackHtml (String name) {
        //XXX only until TopComponent.getHtmlDisplayName() in place
        if (name.indexOf ("<html>") != -1) {
            name = Utilities.replaceString(name, "<html>", ""); //NOI18N
            return "<html>" + name; //NOI18N
        } else {
            return name;
        }
    }

    public OutputTab[] getHiddenTabs() {
        if (hiddenTabs != null && !hiddenTabs.isEmpty()) {
            OutputTab[] result = new OutputTab[hiddenTabs.size()];
            result = (OutputTab[]) hiddenTabs.toArray(result);
            return result;
        }
        return new OutputTab[0];
    }

    public OutputTab getTabForIO (NbIO io) {
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

    public void eventDispatched(IOEvent ioe) {
            if (Controller.log) Controller.log ("Event received: " + ioe);
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
            if (Controller.log) Controller.log ("Passing command to controller " + ioe);
            controller.performCommand (this, comp, io, command, value, data);
            ioe.consume();
    }

    public void hasSelectionChanged(OutputTab tab, boolean val) {
        controller.hasSelectionChanged(this, tab, val);
    }

    public boolean isActivated() {
        return activated;
    }

    public void hasOutputListenersChanged(OutputTab tab, boolean hasOutputListeners) {
        controller.hasOutputListenersChanged(this, tab, hasOutputListeners);
    }

    public void inputEof(OutputTab tab) {
        if (Controller.log) Controller.log ("Input EOF on " + this);
        controller.inputEof(tab);
    }

    public void inputSent(OutputTab c, String txt) {
        if (Controller.log) Controller.log ("Notifying controller input sent " + txt);
        controller.notifyInput(this, c, txt);
    }
}
