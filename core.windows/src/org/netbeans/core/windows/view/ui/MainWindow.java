/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.*;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.openide.awt.MenuBar;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.*;
import org.openide.windows.TopComponent;
import org.openide.awt.ToolbarPool;

import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.Constants;

import org.openide.LifecycleManager;
import org.openide.windows.WindowManager;

/** The MainWindow of IDE. Holds toolbars, main menu and also entire desktop
 * if in MDI user interface. Singleton.
 * This class is final only for performance reasons, can be unfinaled
 * if desired.
 *
 * @author Ian Formanek, Petr Hamernik
 */
public final class MainWindow extends JFrame {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1160791973145645501L;

    /** Desktop. */
    private Component desktop;
    
    /** Flag indicating main window is initialized. */ 
    private boolean inited;

    /** Constructs main window. */
    public MainWindow() {
    }
    

    /** Overrides superclass method, adds help context to the new root pane. */
    protected void setRootPane(JRootPane root) {
        super.setRootPane(root);
        if(root != null) {
            HelpCtx.setHelpIDString(
                    root, new HelpCtx(MainWindow.class).getHelpID());
        }
    }
    
    /** Initializes main window. */
    public void initializeComponents() {
        if(inited) {
            return;
        }
        inited = true;
        
        // initialize frame
        setIconImage(createIDEImage());
        
        initListeners();

        setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);

        getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(MainWindow.class).getString("ACSD_MainWindow"));

        setJMenuBar(createMenuBar());
    
        JComponent tb = getToolbarComponent();
        
        if (UIUtils.isWindowsLF()) {
            Color borderC = null;
            if (UIUtils.isXPLF()) {
                // install our XP color scheme
                // XXX - should be flexible, also for other LFs
                UIUtils.installXPColors();
                borderC = (Color)UIManager.get("nb_workplace_fill"); //NOI18N
            } else {
                borderC = tb.getBackground();
            }
            // decorate toolbars with extra botton border
            Border orig = tb.getBorder();
            tb.setBorder(new CompoundBorder(new MatteBorder(0, 0, 4, 0, borderC), orig));
        }

        getContentPane().add(tb, BorderLayout.NORTH);        
        getContentPane().add(StatusLine.createLabel(), BorderLayout.SOUTH);
    }
    
    private Image createIDEImage() {
        return Toolkit.getDefaultToolkit ().getImage (
                NbBundle.getLocalizedFile(
                        "org.netbeans.core.resources.frames.ide" // NOI18N
                            + (org.openide.util.Utilities.isLargeFrameIcons()
                                ? "32" : ""), // NOI18N
                        "gif", // NOI18N
                        Locale.getDefault(),
                        MainWindow.class.getClassLoader()
            )
        );
    }
    
    private void initListeners() {
        addWindowListener (new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    LifecycleManager.getDefault().exit();
                }

                public void windowActivated (WindowEvent evt) {
                   // #19685. Cancel foreigner popup when
                   // activated main window.
                   org.netbeans.core.windows.RegistryImpl.cancelMenu(MainWindow.this);
                }
            }
        );
    }

    /** Creates menu bar. */
    private static JMenuBar createMenuBar() {
        MenuBar menu = new MenuBar (null);
        menu.setBorderPainted(false);
        menu.waitFinished();
        
        return menu;
    }
    
    /** Creates toolbar component. */
    private static JComponent getToolbarComponent() {
        ToolbarPool tp = ToolbarPool.getDefault();
        tp.waitFinished();
        tp.setConfiguration("Standard"); // NOI18N
        
        return tp;
    }

    /** Packs main window, to set its border */
    private void initializeBounds() {
        Rectangle bounds;
        if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            bounds = WindowManagerImpl.getInstance().getMainWindowBoundsJoined();
        } else {
            bounds = WindowManagerImpl.getInstance().getMainWindowBoundsSeparated();
        }
        
        if(!bounds.isEmpty()) {
            setBounds(bounds);
            validate();
        }
    }

    /** Prepares main window, has to be called after {@link initializeComponents()}. */
    public void prepareWindow() {
        pack();
        
        initializeBounds();
    }

    /** Sets desktop component. */
    public void setDesktop(Component comp) {
        if(desktop == comp) {
            // XXX PENDING revise how to better manipulate with components
            // so there don't happen unneeded removals.
            if(desktop != null
            && !Arrays.asList(getContentPane().getComponents()).contains(desktop)) {
                getContentPane().add(desktop, BorderLayout.CENTER);
            }
            
            return;
        }

        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

        if(desktop != null) {
            getContentPane().remove(desktop);
        }
        
        desktop = comp;
        
        if(desktop != null) {
            getContentPane().add(desktop, BorderLayout.CENTER);
        }
         
        invalidate();
        validate();
        repaint();

        // XXX #37239 Preserve focus remains at the needed place.
        // Even if it has focus now (when you ask KeyboardFocusManager),
        // it is requested again, so the already scheduled requests
        // (caused by the above remove/add) are replaced.
        if(focusOwner != null) {
            focusOwner.requestFocus();
        }
    }

    // XXX PENDING used in DnD only.
    public Component getDesktop() {
        return desktop;
    }
    
    public boolean hasDesktop() {
        return desktop != null;
    }

    // XXX
    /** Gets bounds of main window without the dektop component. */
    public Rectangle getPureMainWindowBounds() {
        Rectangle bounds = getBounds();

        // XXX Substract the desktop height, we know the pure main window
        // is always at the top, the width is same.
        if(desktop != null) {
            Dimension desktopSize = desktop.getSize();
            bounds.height -= desktopSize.height;
        }
        
        return bounds;
    }

    private String getMainTitle() {
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        return NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title", buildNumber);
    }
    
    /** Updates the MainWindow's title */
    public void updateTitle() {
        setTitle(getMainTitle());
    }
    
//    public void updateTitle(String subTitle) {
//        setTitle(getMainTitle() + " - " + subTitle); // NOI18N
//    } // PENDING

}

