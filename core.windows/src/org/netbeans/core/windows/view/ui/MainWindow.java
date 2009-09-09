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

package org.netbeans.core.windows.view.ui;


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.border.*;
import javax.swing.event.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration;
import org.openide.LifecycleManager;
import org.openide.awt.*;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.*;

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
    private static JMenuBar mainMenuBar;

    /** Desktop. */
    private Component desktop;
    
    /** Inner panel which contains desktop component */
    private JPanel desktopPanel;
    
    private static JPanel innerIconsPanel;
    
    /** Flag indicating main window is initialized. */ 
    private boolean inited;

    private Lookup.Result <SaveCookie> saveResult;
    private Lookup.Result <DataObject> dobResult;
    private LookupListener saveListener;
    

    /** Constructs main window. */
    public MainWindow() {
        //make all menu heavyweight by default
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        //make all popups heavyweight
        PopupFactory pf = PopupFactory.getSharedInstance();
        if( !(pf instanceof HeavyWeightPopupFactory) ) {
            PopupFactory.setSharedInstance(new HeavyWeightPopupFactory());
        }
        if( "Aqua".equals(UIManager.getLookAndFeel().getID())
                && null == System.getProperty("apple.awt.brushMetalLook") ) //NOI18N
            getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE); //NOI18N
    }
    
    /** Overrides superclass method, adds help context to the new root pane. */
    @Override
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
                @Override
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

    public static void init() {
        if (mainMenuBar == null) {
            mainMenuBar = createMenuBar();
            ToolbarPool.getDefault().waitFinished();
        }
    }
    
    /** Initializes main window. */
    public void initializeComponents() {
        if(inited) {
            return;
        }
        inited = true;

        init();
        
        // initialize frame
        initFrameIcons(this);
        
        initListeners();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle(MainWindow.class).getString("ACSD_MainWindow"));

        setJMenuBar(mainMenuBar);
    
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

                    if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                        statusLinePanel.setBorder( BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("NbBrushedMetal.darkShadow")), //NOI18N
                                BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("NbBrushedMetal.lightShadow") ) ) ); //NOI18N
                    }
                }
                
                // status line should add some pixels on the left side
                statusLinePanel.setBorder(BorderFactory.createCompoundBorder(
                        statusLinePanel.getBorder(),
                        BorderFactory.createEmptyBorder (0, 0, 0, magicConstant)));
                
                statusLinePanel.add(new JSeparator(), BorderLayout.NORTH);
                statusLinePanel.add(status, BorderLayout.CENTER);
                
                decoratePanel (statusLinePanel, false);
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
        if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            //Show a "save dot" in the close button if a modified file is
            //being edited
            //Show the icon of the edited file in the window titlebar like
            //other mac apps
            saveResult = Utilities.actionsGlobalContext().lookupResult (SaveCookie.class);
            dobResult = Utilities.actionsGlobalContext().lookupResult (DataObject.class);
            if( null != saveResult && null != dobResult ) {
                saveListener = new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        if (ev.getSource() == saveResult) {
                            boolean modified = saveResult.allItems().size() > 0;
                            getRootPane().putClientProperty ("Window.documentModified", //NOI18N
                                    modified ? Boolean.TRUE : Boolean.FALSE);
                        } else if (ev.getSource() == dobResult) {
                            int count = dobResult.allItems().size();
                            switch (count) {
                                case 1 :
                                    DataObject dob = dobResult.allInstances().iterator().next();
                                    FileObject file = dob.getPrimaryFile();
                                    File f = FileUtil.toFile(file);
                                    if (f != null) {
                                        getRootPane().putClientProperty("Window.documentFile", f); //NOI18N
                                        break;
                                    }
                                    //Fall through
                                case 0 :
                                    //Fall through
                                default :
                                    getRootPane().putClientProperty("Window.documentFile", null); //NOI18N
                            }
                        }
                    }

                };
                saveResult.addLookupListener(saveListener);
                dobResult.addLookupListener(saveListener);
            }
        }
    }

    
    private static void decoratePanel (JPanel panel, boolean safeAccess) {
        assert safeAccess || SwingUtilities.isEventDispatchThread () : "Must run in AWT queue.";
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
                    decoratePanel (decoratingPanel, false);
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
    
    private static final String ICON_16 = "org/netbeans/core/startup/frame.gif"; // NOI18N
    private static final String ICON_32 = "org/netbeans/core/startup/frame32.gif"; // NOI18N
    private static final String ICON_48 = "org/netbeans/core/startup/frame48.gif"; // NOI18N
    
    private static Image createIDEImage() {
        return ImageUtilities.loadImage(ICON_16, true);
    }
    
    private static List<Image> createIDEImages() {
        List<Image> l = new ArrayList<Image>();
        l.add(ImageUtilities.loadImage(ICON_16, true));
        l.add(ImageUtilities.loadImage(ICON_32, true));
        l.add(ImageUtilities.loadImage(ICON_48, true));
        return l;
    }
    
    static void initFrameIcons (Frame f) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("java.awt.Window");
        } catch (ClassNotFoundException ex) {
            //This cannot happen because without AWT classes we would not get here.
        }
        Method m = null;
        try {
            m = clazz.getMethod("setIconImages", new Class [] {List.class});
        } catch (NoSuchMethodException ex) {
            //Method not available so we are on JDK 5. Use setIconImage.
        }
        if (m != null) {
            List<Image> l;
            l = createIDEImages();
            try {
                m.invoke(f, new Object [] {l});
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MainWindow.class.getName()).log
                (Level.INFO, "Cannot invoke setIconImages", ex); //NOI18N
                f.setIconImage(createIDEImage());
            } catch (InvocationTargetException ex) {
                Logger.getLogger(MainWindow.class.getName()).log
                (Level.INFO, "Cannot invoke setIconImages", ex); //NOI18N
                f.setIconImage(createIDEImage());
            }
        } else {
            f.setIconImage(createIDEImage());
        }
    }
    
    private void initListeners() {
        addWindowListener (new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    LifecycleManager.getDefault().exit();
                }

                @Override
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
                
                decoratePanel (statusLinePanel, true);
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
             FileObject fo = FileUtil.getConfigFile(fileName);
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
             FileObject fo = FileUtil.getConfigFile(fileName);
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

    @Override
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

    @Override
    public void paint(Graphics g) {
        // As a safeguard, always release the dummy graphic when we get a paint
        if (waitingForPaintDummyGraphic != null) {
            releaseWaitingForPaintDummyGraphic();
            // Since the release did not occur before the getGraphics() call,
            // I need to get the actual graphics now that I've released
            g = getGraphics();
        }
        super.paint(g);
        Logger.getLogger(MainWindow.class.getName()).log(Level.FINE, 
                "Paint method of main window invoked normally."); //NOI18N

        WindowManagerImpl.getInstance().mainWindowPainted();
    }

    /** Overrides parent version to return fake dummy graphic in certain time
     * during startup
     */
    @Override
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
    private boolean isUndecorated = true;
    private int windowDecorationStyle = JRootPane.FRAME;

    
    public void setFullScreenMode( boolean fullScreenMode ) {
        if( isFullScreenMode == fullScreenMode || isSwitchingFullScreenMode ) {
            return;
        }
        isSwitchingFullScreenMode = true;
        if( !isFullScreenMode ) {
            restoreExtendedState = getExtendedState();
            restoreBounds = getBounds();
            isUndecorated = isUndecorated();
            windowDecorationStyle = getRootPane().getWindowDecorationStyle();
        }
        isFullScreenMode = fullScreenMode;
        if( Utilities.isWindows() )
            setVisible( false );
        else 
            WindowManagerImpl.getInstance().setVisible(false);
        
        dispose();
        
        setUndecorated( isFullScreenMode || isUndecorated );
        // Added to support Custom Look and Feel with Decorations
        getRootPane().setWindowDecorationStyle( isFullScreenMode ? JRootPane.NONE : windowDecorationStyle );

        final String toolbarConfigName = ToolbarPool.getDefault().getConfiguration();
        if( null != toolbarConfigName ) {
            ToolbarConfiguration tc = ToolbarConfiguration.findConfiguration( toolbarConfigName );
            if( null != tc )
                tc.rebuildMenu();
        }
        getToolbarComponent().setVisible( !isFullScreenMode );
        final boolean updateBounds = ( !isFullScreenMode );//&& restoreExtendedState != JFrame.MAXIMIZED_BOTH );

        GraphicsDevice device = null;
        Graphics gc = getGraphics();
        if( gc instanceof Graphics2D ) {
            GraphicsConfiguration conf = ((Graphics2D)gc).getDeviceConfiguration();
            if( null != conf )
                device = conf.getDevice();
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
                    ToolbarPool.getDefault().setConfiguration( toolbarConfigName );
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
                    ToolbarPool.getDefault().setConfiguration( toolbarConfigName );
                    isSwitchingFullScreenMode = false;
                }
            });
        }
    }
    
    public boolean isFullScreenMode() {
        return isFullScreenMode;
    }

    private static class HeavyWeightPopupFactory extends PopupFactory {

        @Override
        public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
            return new HeavyWeightPopup(owner, contents, x, y);
        }
    }

    private static class HeavyWeightPopup extends Popup {
        public HeavyWeightPopup(Component owner, Component contents, int x, int y) {
            super( owner, contents, x, y);
        }
    }
}

