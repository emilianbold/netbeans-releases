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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui;


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration;
import org.openide.LifecycleManager;
import org.openide.awt.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.windows.TopComponent;
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
    
    private static JPanel innerIconsPanel;
    
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
        //Optimization related to jdk bug 4939857 - on pre 1.5 jdk's an
        //extra repaint is caused by the search for an opaque component up
        //to the component root.  Post 1.5, root pane will automatically be
        //opaque.
        root.setOpaque(true);
        if (Utilities.isWindows()) {
            // use glass pane that will not cause repaint/revalidate of parent when set visible
            // is called (when setting wait cursor in ModuleActions) #40689
            JComponent c = new JPanel() {
                public void setVisible(boolean flag) {
                    if (flag != isVisible ()) {
                        super.setVisible(flag);
                    }
                }
            };
            c.setName(root.getName()+".nbGlassPane");  // NOI18N
            c.setVisible(false);
            ((JPanel)c).setOpaque(false);
            root.setGlassPane(c);
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

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

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
                // text in line should be shifted for 4pix.
                status.setBorder (BorderFactory.createEmptyBorder (0, 4, 0, 0));

                JPanel statusLinePanel = new JPanel(new BorderLayout());
                int magicConstant = 0;
                if (Utilities.isMac()) {
                    // on mac there is window resize component in the right most bottom area.
                    // it paints over our icons..
                    magicConstant = 12;
                }
                
                // status line should add some pixels on the left side
                statusLinePanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder (0, 0, 0, magicConstant), 
                        statusLinePanel.getBorder ()));
                
                statusLinePanel.add(new JSeparator(), BorderLayout.NORTH);
                statusLinePanel.add(status, BorderLayout.CENTER);
                
                decoratePanel (statusLinePanel);
                statusLinePanel.setName("statusLine"); //NOI18N
                getContentPane().add (statusLinePanel, BorderLayout.SOUTH);
            } else { // custom status line provided
                JComponent status = getCustomStatusLine();
                if (status != null) {
                    getContentPane().add(status, BorderLayout.SOUTH);
                }
            }
        }

        getContentPane().add(getDesktopPanel(), BorderLayout.CENTER);
        
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
        setTitle(NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title_No_Project", System.getProperty("netbeans.buildnumber")));
    }
    
    private static void decoratePanel (JPanel panel) {
        assert SwingUtilities.isEventDispatchThread () : "Must run in AWT queue.";
        if (innerIconsPanel != null) {
            panel.remove (innerIconsPanel);
        }
        innerIconsPanel = getStatusLineElements (panel);
        if (innerIconsPanel != null) {
            panel.add (innerIconsPanel, BorderLayout.EAST);
        }
    }
    
    private static Lookup.Result<StatusLineElementProvider> result;
    
    // package-private because StatusLineElementProviderTest
    static JPanel getStatusLineElements (JPanel panel) {
        // bugfix #56375, don't duplicate the listeners
        if (result == null) {            
            result = Lookup.getDefault ().lookup (
                    new Lookup.Template<StatusLineElementProvider> (StatusLineElementProvider.class));
            result.addLookupListener (new StatusLineElementsListener (panel));
        }
        Collection<? extends StatusLineElementProvider> c = result.allInstances ();
        if (c == null || c.isEmpty ()) {
            return null;
        }
        Iterator<? extends StatusLineElementProvider> it = c.iterator ();
        JPanel icons = new JPanel (new FlowLayout (FlowLayout.RIGHT, 0, 0));
        icons.setBorder (BorderFactory.createEmptyBorder (1, 0, 0, 2));
        boolean some = false;
        while (it.hasNext ()) {
            StatusLineElementProvider o = it.next ();
            Component comp = o.getStatusLineElement ();
            if (comp != null) {
                some = true;
                icons.add (comp);
            }
        }
        return some ? icons : null;
    }
    
    static private class StatusLineElementsListener implements LookupListener {
        private JPanel decoratingPanel;
        StatusLineElementsListener (JPanel decoratingPanel) {
            this.decoratingPanel = decoratingPanel;
        }
        public void resultChanged (LookupEvent ev) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    decoratePanel (decoratingPanel);
                }
            });
        }
    }
    
    /** Creates and returns border for desktop which is visually aligned
     * with currently active LF */
    private static Border getDesktopBorder () {
        Border b = (Border) UIManager.get ("nb.desktop.splitpane.border");
        if (b != null) {
            return b;
        } else {
            return new EmptyBorder(1, 1, 1, 1);
        }
    }
    
    private static final String ICON_SMALL = "org/netbeans/core/startup/frame.gif"; // NOI18N
    private static final String ICON_BIG = "org/netbeans/core/startup/frame32.gif"; // NOI18N
    static Image createIDEImage() {
        return Utilities.loadImage(Utilities.isLargeFrameIcons() ? ICON_BIG : ICON_SMALL, true);
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
        JMenuBar menu = getCustomMenuBar();
        if (menu == null) {
             menu = new MenuBar (null);
        }
        menu.setBorderPainted(false);
        if (menu instanceof MenuBar) {
            ((MenuBar)menu).waitFinished();
        }
        
        if(Constants.SWITCH_STATUSLINE_IN_MENUBAR) {
            if (Constants.CUSTOM_STATUS_LINE_PATH == null) {
                JLabel status = new StatusLine();
                JSeparator sep = new JSeparator(JSeparator.VERTICAL);
                Dimension d = sep.getPreferredSize();
                d.width += 6; // need a bit more padding...
                sep.setPreferredSize(d);
                JPanel statusLinePanel = new JPanel(new BorderLayout());
                statusLinePanel.add(sep, BorderLayout.WEST);
                statusLinePanel.add(status, BorderLayout.CENTER);
                
                decoratePanel (statusLinePanel);
                statusLinePanel.setName("statusLine"); //NOI18N
                menu.add(statusLinePanel);
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
     private static JMenuBar getCustomMenuBar() {
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
                     return (JMenuBar)ic.instanceCreate();
                 }
             }
         } catch (Exception e) {
             Exceptions.printStackTrace(e);
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
             Exceptions.printStackTrace(e);
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

    private Rectangle forcedBounds = null;
    /** Packs main window, to set its border */
    private void initializeBounds() {
        Rectangle bounds;
        if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            bounds = WindowManagerImpl.getInstance().getMainWindowBoundsJoined();
        } else {
            bounds = WindowManagerImpl.getInstance().getMainWindowBoundsSeparated();
        }
        if( null != forcedBounds ) {
            bounds = new Rectangle( forcedBounds );
            setPreferredSize( bounds.getSize() );
            forcedBounds = null;
        }
        
        if(!bounds.isEmpty()) {
            setBounds(bounds);
        }
    }
    
    /**
     * don't allow smaller bounds than the one constructed from preffered sizes, making sure everything is visible when
     * in SDI. #40063
     */
    public void setBounds(Rectangle rect) {
        Rectangle bounds = rect;
        if (bounds != null) {
            if (bounds.height < getPreferredSize().height) {
                bounds = new Rectangle(bounds.x, bounds.y, bounds.width, getPreferredSize().height);
            }
        }
        super.setBounds(bounds);
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
            && !Arrays.asList(getDesktopPanel().getComponents()).contains(desktop)) {
                getDesktopPanel().add(desktop, BorderLayout.CENTER);
            }
            return;
        }

        if(desktop != null) {
            getDesktopPanel().remove(desktop);
        }
        
        desktop = comp;
        
        if(desktop != null) {
            getDesktopPanel().add(desktop, BorderLayout.CENTER);
        } 
        invalidate();
        validate();
        // use #24291 hack only on Win OS
        if( isOlderJDK && !System.getProperty("os.name").startsWith("Windows") ) {
            releaseWaitingForPaintDummyGraphic();
        }

        repaint();
    }

    // XXX PENDING used in DnD only.
    public Component getDesktop() {
        return desktop;
    }
    
    public boolean hasDesktop() {
        return desktop != null;
    }
    
    /** #112408: Single access point for desktopPanel to ensure it's never null */
    private JPanel getDesktopPanel () {
        if (desktopPanel == null) {
            // initialize desktop panel
            desktopPanel = new JPanel();
            desktopPanel.setBorder(getDesktopBorder());
            desktopPanel.setLayout(new BorderLayout());
        }
        return desktopPanel;
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

    // [dafe] Start of #24291 hacky fix, to prevent from main window flicking on
    // JDK 1.5.x and older. Can be freely deleted when we will drop JDK 1.5.x
    // support in future

    private Image waitingForPaintDummyImage;
    private Graphics waitingForPaintDummyGraphic;
    boolean isOlderJDK = System.getProperty("java.version").startsWith("1.5");

    public void setVisible (boolean flag) {
        // The setVisible will cause a PaintEvent to be queued up, as a LOW_PRIORITY one
        // As the painting of my child components occurs, they cause painting of their own
        // When the PaintEvent queued from the setVisible is finally processed, it assumes
        // nothing has been displayed and redraws the whole window.
        // So we make it such that, UNTIL there is the repaint is dispatched, return a graphics
        // which goes nowhere.
        if (flag && isOlderJDK) {
            waitingForPaintDummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            waitingForPaintDummyGraphic = waitingForPaintDummyImage.getGraphics();
        }
        super.setVisible(flag);
    }

    public void paint(Graphics g) {
        // As a safeguard, always release the dummy graphic when we get a paint
        if (waitingForPaintDummyGraphic != null) {
            releaseWaitingForPaintDummyGraphic();
            // Since the release did not occur before the getGraphics() call,
            // I need to get the actual graphics now that I've released
            g = getGraphics();
        }
        super.paint(g);
    }

    /** Overrides parent version to return fake dummy graphic in certain time
     * during startup
     */
    public Graphics getGraphics () {
        // Return the dummy graphics that paint nowhere, until we receive a paint() 
        if (waitingForPaintDummyGraphic != null) {
            // If we are the PaintEvent we are waiting for is being dispatched
            // we better return the correct graphics.
            AWTEvent event = EventQueue.getCurrentEvent();
            if (event == null || (event.getID() != PaintEvent.PAINT && event.getSource() != this)) {
                return waitingForPaintDummyGraphic;
	        }
	        releaseWaitingForPaintDummyGraphic();
        }
        return super.getGraphics();
    }

    private void releaseWaitingForPaintDummyGraphic () {
        if (waitingForPaintDummyGraphic != null) {
            waitingForPaintDummyGraphic.dispose();
            waitingForPaintDummyGraphic = null;
            waitingForPaintDummyImage = null;
        }
    }

    // end of #24291 hacky fix

    // Full Screen Mode
    private boolean isFullScreenMode = false;
    private Rectangle restoreBounds;
    private int restoreExtendedState = JFrame.NORMAL;
    private boolean isSwitchingFullScreenMode = false;
    
    public void setFullScreenMode( boolean fullScreenMode ) {
        if( isFullScreenMode == fullScreenMode || isSwitchingFullScreenMode ) {
            return;
        }
        isSwitchingFullScreenMode = true;
        final TopComponent activatedTc = WindowManager.getDefault().getRegistry().getActivated();
        if( !isFullScreenMode ) {
            restoreExtendedState = getExtendedState();
            restoreBounds = getBounds();
        }
        isFullScreenMode = fullScreenMode;
        if( Utilities.isWindows() )
            setVisible( false );
        else 
            WindowManagerImpl.getInstance().setVisible(false);
        
        dispose();
        
        setUndecorated( isFullScreenMode );

        String toolbarConfigName = ToolbarPool.getDefault().getConfiguration();
        if( null != toolbarConfigName ) {
            ToolbarConfiguration tc = ToolbarConfiguration.findConfiguration( toolbarConfigName );
            if( null != tc )
                tc.rebuildMenu();
        }
        getToolbarComponent().setVisible( !isFullScreenMode );
        final boolean updateBounds = ( !isFullScreenMode );//&& restoreExtendedState != JFrame.MAXIMIZED_BOTH );

        GraphicsDevice device = null;
        if( getGraphics() instanceof Graphics2D ) {
            device = ((Graphics2D)getGraphics()).getDeviceConfiguration().getDevice();
        }
        if( null != device && device.isFullScreenSupported() ) {
            device.setFullScreenWindow( isFullScreenMode ? this : null );
        } else {
            setExtendedState( isFullScreenMode ? JFrame.MAXIMIZED_BOTH : restoreExtendedState );
        }
        
        if( updateBounds || (isFullScreenMode() && !Utilities.isWindows()) ) {
            if( updateBounds ) {
                forcedBounds = restoreBounds;
            } else {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                forcedBounds = ge.getMaximumWindowBounds();
            }
        }
        if( Utilities.isWindows() ) {
            setVisible( true );
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    invalidate();
                    validate();
                    repaint();
                    if( updateBounds ) {
                        setPreferredSize( restoreBounds.getSize() );
                        setBounds( restoreBounds );
                    }
                    isSwitchingFullScreenMode = false;
                }
            });
        } else {
            WindowManagerImpl.getInstance().setVisible(true);
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    invalidate();
                    validate();
                    repaint();
                    isSwitchingFullScreenMode = false;
                }
            });
        }
    }
    
    public boolean isFullScreenMode() {
        return isFullScreenMode;
    }
}

