/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.modules.ModuleInstall;

import org.openidex.util.Utilities2;
import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.*;

import java.beans.*;
import java.io.File;

/**
 * Module installation class for Form Editor
 *
 * @author Ian Formanek
 */
public class FormEditorModule extends ModuleInstall {

    private transient Node lastProjectDesktop;
    private transient PropertyChangeListener projectListener = null;

    static final long serialVersionUID =1573432625099425394L;
    
    /** Module installed for the first time. */
    public void installed() {
        restored();
    }

    // XXX(-tdt) hack around failure of loading TimerBean caused by package
    // renaming com.netbeans => org.netbeans AND the need to preserve user's
    // system settings
    
    private static void timerBeanHack() {
        TopManager.getDefault().getRepository().addRepositoryListener(
            new RepositoryListener() {
                public void fileSystemRemoved (RepositoryEvent ev) {}
                public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {}

                public void fileSystemAdded (RepositoryEvent ev) {
                    FileSystem fs = ev.getFileSystem();
                    if (! (fs instanceof JarFileSystem))
                        return;
                    JarFileSystem jarfs = (JarFileSystem) fs;

                    try {
                        // XXX this should never happen, but sometimes it kdjf
                        // does. WHY?
                        if (null == jarfs.getJarFile())
                            return;
                        
                        String jarpath = jarfs.getJarFile().getCanonicalPath();
                        if (! jarpath.endsWith(File.separator + "beans"
                                               + File.separator + "TimerBean.jar"))
                            return;
                        File timerbean = new File(
                            System.getProperty("netbeans.home")
                            + File.separator + "beans"
                            + File.separator + "TimerBean.jar");
                        if (jarpath.equals(timerbean.getCanonicalPath()))
                            return;
                        
                        jarfs.setJarFile(timerbean);
                    }
                    catch (java.io.IOException ex) { /* ignore */ }
                    catch (PropertyVetoException ex) { /* ignore */ }
                }
            });
    }
    
    /** Module installed again. */
    public void restored() {
        Beans.setDesignTime(true);
        
        timerBeanHack();
        
        lastProjectDesktop = TopManager.getDefault().getPlaces().nodes().projectDesktop();
        if (projectListener == null) {
            projectListener = new ProjectChangeListener();
            TopManager.getDefault().addPropertyChangeListener(projectListener);
        }

        // register standard persistence managers
        PersistenceManager.registerManager(new TuborgPersistenceManager());
        PersistenceManager.registerManager(new GandalfPersistenceManager());

        // XXX(-tdt) JDK "forgets" to provide a PropertyEditor for char and Character

        PropertyEditor charEditor;

        charEditor = PropertyEditorManager.findEditor(Character.TYPE);
        if (charEditor == null)
            FormPropertyEditorManager.registerEditor(
                Character.TYPE,
                org.netbeans.modules.form.editors.CharacterEditor.class);

        charEditor = PropertyEditorManager.findEditor(Character.class);
        if (charEditor == null)
            FormPropertyEditorManager.registerEditor(
                Character.class,
                org.netbeans.modules.form.editors.CharacterEditor.class);

        FormPropertyEditorManager.registerEditor(
            javax.swing.KeyStroke.class,
            org.netbeans.modules.form.editors.KeyStrokeEditor.class);
    }

    /** Module was uninstalled. */
    public void uninstalled() {
        if (projectListener != null) {
            TopManager.getDefault().removePropertyChangeListener(projectListener);
            projectListener = null;
        }
    }

    // -------------------------------------------------------------------------
    // Listener class responding to project changes and updating Component Palette.

    private class ProjectChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent ev) {
            if (TopManager.PROP_PLACES.equals(ev.getPropertyName())) {
                Node projectDesktop = TopManager.getDefault().getPlaces().nodes().projectDesktop();
                if (projectDesktop != lastProjectDesktop) {
                    lastProjectDesktop = projectDesktop;
                    BeanInstaller.autoLoadBeans();
                    ComponentPalette.getDefault().updatePalette();
                }
            }
        }
    }

    static String[] getDefaultAWTComponents() {
        return defaultAWTComponents;
    }
    static String[] getDefaultAWTIcons() {
        return defaultAWTIcons;
    }

    static String[] getDefaultSwingComponents() {
        return defaultSwingComponents;
    }
    static String[] getDefaultSwingIcons() {
        return defaultSwingIcons;
    }
    
    static String[] getDefaultSwing2Components() {
        return defaultSwing2Components;
    }
    static String[] getDefaultSwing2Icons() {
        return defaultSwing2Icons;
    }
    
    static String[] getDefaultLayoutsComponents() {
        return defaultLayoutsComponents;
    }
    static String[] getDefaultLayoutsIcons() {
        return defaultLayoutsIcons;
    }
    
    static String[] getDefaultBorders() {
        return defaultBorders;
    }
    static String[] getDefaultBordersIcons() {
        return defaultBordersIcons;
    }
    
    // -----------------------------------------------------------------------------
    // Default Palette contents

    /** The default AWT Components */
    private final static String[] defaultAWTComponents = new String[] {
        "java.awt.Label", // NOI18N
        "java.awt.Button", // NOI18N
        "java.awt.TextField", // NOI18N
        "java.awt.TextArea", // NOI18N
        "java.awt.Checkbox", // NOI18N
        "java.awt.Choice", // NOI18N
        "java.awt.List", // NOI18N
        "java.awt.Scrollbar", // NOI18N
        "java.awt.ScrollPane", // NOI18N
        "java.awt.Panel", // NOI18N
        "java.awt.Canvas", // NOI18N
        "java.awt.MenuBar", // NOI18N
        "java.awt.PopupMenu", // NOI18N
    };

    /** The default AWT icons */
    private final static String[] defaultAWTIcons = new String[] {
        "/org/netbeans/beaninfo/awt/label.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/button.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/textfield.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/textarea.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/checkbox.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/choice.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/list.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/scrollbar.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/scrollpane.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/panel.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/canvas.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/menubar.gif", // NOI18N
        "/org/netbeans/beaninfo/awt/popupmenu.gif", // NOI18N
    };
    
    /** The default Swing Components */
    private final static String[] defaultSwingComponents = new String[] {
        "javax.swing.JLabel", // NOI18N
        "javax.swing.JButton", // NOI18N
        "javax.swing.JCheckBox", // NOI18N
        "javax.swing.JRadioButton", // NOI18N
        "javax.swing.JComboBox", // NOI18N
        "javax.swing.JList", // NOI18N
        "javax.swing.JTextField", // NOI18N
        "javax.swing.JTextArea", // NOI18N
        "javax.swing.JToggleButton", // NOI18N
        "javax.swing.JPanel", // NOI18N
        "javax.swing.JTabbedPane", // NOI18N
        "javax.swing.JScrollBar", // NOI18N
        "javax.swing.JScrollPane", // NOI18N
        "javax.swing.JMenuBar", // NOI18N
        "javax.swing.JPopupMenu", // NOI18N
    };

    /** The default Swing icons */
    private final static String[] defaultSwingIcons = new String[] {
        "/javax/swing/beaninfo/images/JLabelColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JButtonColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JCheckBoxColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JRadioButtonColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JComboBoxColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JListColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JTextFieldColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JTextAreaColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JToggleButtonColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JPanelColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JTabbedPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JScrollBarColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JScrollPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JMenuBarColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JPopupMenuColor16.gif" // NOI18N
    };

    /** The default Swing Components - Swing2 category */
    private final static String[] defaultSwing2Components = new String[] {
        "javax.swing.JSlider", // NOI18N
        "javax.swing.JProgressBar", // NOI18N
        "javax.swing.JSplitPane", // NOI18N
        "javax.swing.JPasswordField", // NOI18N
        "javax.swing.JSeparator", // NOI18N
        "javax.swing.JTextPane", // NOI18N
        "javax.swing.JEditorPane", // NOI18N
        "javax.swing.JTree", // NOI18N
        "javax.swing.JTable", // NOI18N
        "javax.swing.JToolBar", // NOI18N
        "javax.swing.JInternalFrame", // NOI18N
        "javax.swing.JLayeredPane", // NOI18N
        "javax.swing.JDesktopPane", // NOI18N
        "javax.swing.JOptionPane", // NOI18N
    };

    /** The default Swing icons - Swing2 category */
    private final static String[] defaultSwing2Icons = new String[] {
        "/javax/swing/beaninfo/images/JSliderColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JProgressBarColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JSplitPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JPasswordFieldColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JSeparatorColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JTextPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JEditorPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JTreeColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JTableColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JToolBarColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JInternalFrameColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JLayeredPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JDesktopPaneColor16.gif", // NOI18N
        "/javax/swing/beaninfo/images/JOptionPaneColor16.gif" // NOI18N
    };

    //  private final static String[] defaultDBComponents = new String[] {
    //    "org.netbeans.lib.sql.JDBCRowSet", // NOI18N
    //    "org.netbeans.lib.sql.components.DataNavigator", // NOI18N
    //  };

    /** The default Swing Components - beans category */
    private final static String[] defaultBeansComponents = new String[] {
        // for future use.
    };

    /** The default Layout Components */
    private final static String[] defaultLayoutsComponents = new String[] {
        "org.netbeans.modules.form.compat2.layouts.DesignFlowLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBorderLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignCardLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout", // NOI18N
    };

    /** The default Layout Components */
    private final static String[] defaultLayoutsIcons = new String[] {
        "/org/netbeans/modules/form/resources/palette/flowLayout.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/borderLayout.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/gridLayout.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/cardLayout.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/absoluteLayout.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/gridBagLayout.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/boxLayout.gif", // NOI18N
    };

    /** The default Swing Borders */
    private final static String[] defaultBorders = new String[] {
        "org.netbeans.modules.form.compat2.border.EmptyBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.LineBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.MatteIconBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.MatteColorBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.TitledBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.EtchedBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.BevelBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.SoftBevelBorderInfo", // NOI18N
        "org.netbeans.modules.form.compat2.border.CompoundBorderInfo", // NOI18N
    };

    /** The default Swing Borders */
    private final static String[] defaultBordersIcons = new String[] {
        "/org/netbeans/modules/form/resources/palette/border.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/lineBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/matteIconBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/matteColorBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/titledBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/etchedBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/bevelBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/softBevelBorder.gif", // NOI18N
        "/org/netbeans/modules/form/resources/palette/compoundBorder.gif", // NOI18N
    };
}
