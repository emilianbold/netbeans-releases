/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.awt.MenuBar;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.*;
import org.openide.windows.TopComponent;
import org.openide.awt.ToolbarPool;
import org.openide.ErrorManager;

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
    
    /** Inner panel which contains desktop component */
    private JPanel desktopPanel;
    
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
        //Optimization related to jdk bug 4393857 - on pre 1.5 jdk's an
        //extra repaint is caused by the search for an opaque component up
        //to the component root.  Post 1.5, root pane will automatically be
        //opaque.
        root.setOpaque(true);
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
    
        if (!Constants.NO_TOOLBARS) {
            JComponent tb = getToolbarComponent();
            getContentPane().add(tb, BorderLayout.NORTH);
        }
        
        if(!Constants.SWITCH_STATUSLINE_IN_MENUBAR) {
            if (Constants.CUSTOM_STATUS_LINE_PATH == null) {
                JLabel status = new StatusLine();
                // XXX #19910 Not to squeeze status line.
                status.setText(" "); // NOI18N
                status.setPreferredSize(new Dimension(0, status.getPreferredSize().height));

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(new JSeparator(), BorderLayout.NORTH);
                panel.add(status, BorderLayout.CENTER);
                panel.setName("statusLine"); //NOI18N
                getContentPane().add(panel, BorderLayout.SOUTH);
            } else { // custom status line provided
                JComponent status = getCustomStatusLine();
                if (status != null) {
                    getContentPane().add(status, BorderLayout.SOUTH);
                }
            }
        }
        
        // initialize desktop panel
        desktopPanel = new JPanel();
        desktopPanel.setBorder(getDesktopBorder());
        desktopPanel.setLayout(new BorderLayout());
        if (UIUtils.isXPLF()) {
            // install our XP color scheme
            // XXX - should be flexible, also for other LFs
            UIUtils.installXPColors();
            Color fillC = (Color)UIManager.get("nb_workplace_fill"); //NOI18N
            desktopPanel.setBackground(fillC);
        }
        getContentPane().add(desktopPanel, BorderLayout.CENTER);
        //#38810 start - focusing the main window in case it's not active and the menu is
        // selected..
        MenuSelectionManager.defaultManager().addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                MenuElement[] elems = MenuSelectionManager.defaultManager().getSelectedPath();
                if (elems != null && elems.length > 0) {
                    if (elems[0] == getJMenuBar()) {
                        if (!isActive()) {
                            toFront();
                        }
                    }
                }
            }
        });
        //#38810 end
    }
    
    private boolean hackFirst = Boolean.getBoolean("netbeans.winsys.flashhack"); //NOI18N
    /** Workaround for main window flashing during startup problem on Windows.
     * Depends on the main window being laid out twice during startup, so may
     * not be safe enough for producting use, but including for testing.  Can't
     * find any platform it causes a problem on thus far. */
    public void doLayout() {
        if (hackFirst) {
            hackFirst = false;
            return;
        }
        super.doLayout();
    }
    
    /** Creates and returns border for desktop which is visually aligned
     * with currently active LF */
    private static Border getDesktopBorder () {
        if (UIUtils.isXPLF()) {
            return new EmptyBorder(6, 5, 4, 6);
        } else if (UIUtils.isWindowsLF()) {
            return new EmptyBorder(4, 2, 1, 2);
        }
        return new EmptyBorder(1, 1, 1, 1);
    }
    
    static Image createIDEImage() {
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
        MenuBar menu = getCustomMenuBar();
        if (menu == null) {
             menu = new MenuBar (null);
        }
        menu.setBorderPainted(false);
        menu.waitFinished();
        
        if(Constants.SWITCH_STATUSLINE_IN_MENUBAR) {
            if (Constants.CUSTOM_STATUS_LINE_PATH == null) {
                JLabel status = new StatusLine();
                JPanel panel = new JPanel(new BorderLayout());
                JSeparator sep = new JSeparator(JSeparator.VERTICAL);
                Dimension d = sep.getPreferredSize();
                d.width += 6; // need a bit more padding...
                sep.setPreferredSize(d);
                panel.add(sep, BorderLayout.WEST);
                panel.add(status, BorderLayout.CENTER);
                panel.setName("statusLine"); //NOI18N
                menu.add(panel);
            } else {
                JComponent status = getCustomStatusLine();
                if (status != null) {
                    menu.add(status);
                }
            }
        }
        
        return menu;
    }

     /**
      * Tries to find custom menu bar component on system file system.
      * @return menu bar component or <code>null</code> if no menu bar
      *         component is found on system file system.
      */
     private static MenuBar getCustomMenuBar() {
         try {
             String fileName = Constants.CUSTOM_MENU_BAR_PATH;
             if (fileName == null) {
                 return null;
             }
             FileObject fo =
                 Repository.getDefault().getDefaultFileSystem().findResource(
                     fileName);
             if (fo != null) {
                 DataObject dobj = DataObject.find(fo);
                 InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
                 if (ic != null) {
                     return (MenuBar)ic.instanceCreate();
                 }
             }
         } catch (Exception e) {
             ErrorManager.getDefault().notify(e);
         }
         return null;
     }
    
     /**
      * Tries to find custom status line component on system file system.
      * @return status line component or <code>null</code> if no status line
      *         component is found on system file system.
      */
     private static JComponent getCustomStatusLine() {
         try {
             String fileName = Constants.CUSTOM_STATUS_LINE_PATH;
             if (fileName == null) {
                 return null;
             }
             FileObject fo =
                 Repository.getDefault().getDefaultFileSystem().findResource(
                     fileName);
             if (fo != null) {
                 DataObject dobj = DataObject.find(fo);
                 InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
                 if (ic != null) {
                     return (JComponent)ic.instanceCreate();
                 }
             }
         } catch (Exception e) {
             ErrorManager.getDefault().notify(e);
         }
         return null;
     }
     
    /** Creates toolbar component. */
    private static JComponent getToolbarComponent() {
        ToolbarPool tp = ToolbarPool.getDefault();
        tp.waitFinished();
//        ErrorManager.getDefault().getInstance(MainWindow.class.getName()).log("toolbar config name=" + WindowManagerImpl.getInstance().getToolbarConfigName());
//        tp.setConfiguration(WindowManagerImpl.getInstance().getToolbarConfigName()); // NOI18N
        
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
        }
    }
    
    /** Prepares main window, has to be called after {@link initializeComponents()}. */
    public void prepareWindow() {
        initializeBounds();
    }

    /** Sets desktop component. */
    public void setDesktop(Component comp) {
        if(desktop == comp) {
            // XXX PENDING revise how to better manipulate with components
            // so there don't happen unneeded removals.
            if(desktop != null
            && !Arrays.asList(desktopPanel.getComponents()).contains(desktop)) {
                desktopPanel.add(desktop, BorderLayout.CENTER);
            }
            return;
        }

        if(desktop != null) {
            desktopPanel.remove(desktop);
        }
        
        desktop = comp;
        
        if(desktop != null) {
            desktopPanel.add(desktop, BorderLayout.CENTER);
        }
        invalidate();
        validate();
        repaint();        
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

    public void setProjectName(String projectName) {
        updateTitle(projectName);
    }
    
    /** Updates the MainWindow's title */
    private void updateTitle(String projectName) {
        String buildNumber = System.getProperty("netbeans.buildnumber"); // NOI18N
        String subTitle;
        if(projectName == null) {
            subTitle = NbBundle.getMessage(MainWindow.class, "CTL_NoProject");
        } else {
            subTitle = projectName;
        }
        
        setTitle(NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title", buildNumber, subTitle));
    }

}

