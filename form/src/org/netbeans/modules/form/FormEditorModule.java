/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.form;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.modules.ModuleInstall;

import org.openidex.util.Utilities2;
import com.netbeans.developer.modules.loaders.form.actions.*;
import com.netbeans.developer.modules.loaders.form.palette.*;

/**
* Module installation class for Form Editor
*
* @author Ian Formanek
*/
public class FormEditorModule extends ModuleInstall {

  private static final String AWT_CATEGORY_NAME = NbBundle.getBundle (FormEditorModule.class).getString ("CTL_Palette_AWT");
  private static final String SWING_CATEGORY_NAME = NbBundle.getBundle (FormEditorModule.class).getString ("CTL_Palette_Swing");
  private static final String SWING2_CATEGORY_NAME = NbBundle.getBundle (FormEditorModule.class).getString ("CTL_Palette_Swing2");
  private static final String BEANS_CATEGORY_NAME = NbBundle.getBundle (FormEditorModule.class).getString ("CTL_Palette_Beans");
  private static final String LAYOUTS_CATEGORY_NAME = NbBundle.getBundle (FormEditorModule.class).getString ("CTL_Palette_Layouts");
  private static final String BORDERS_CATEGORY_NAME = NbBundle.getBundle (FormEditorModule.class).getString ("CTL_Palette_Borders");

  static final long serialVersionUID =1573432625099425394L;
  /** Module installed for the first time. */
  public void installed () {
  // ---------------------------------------------------------------------------
  // 1. create FormEditor actions
    installActions ();

  // ---------------------------------------------------------------------------
  // 2. copy FormEditor templates
    copyTemplates ();

  // ---------------------------------------------------------------------------
  // 3. create Component Palette under system
    createComponentPalette ();

    restored ();
  }

  /** Module installed again. */
  public void restored () {
    BeanInstaller.autoLoadBeans ();

    // register standard persistence managers
    PersistenceManager.registerManager (new TuborgPersistenceManager ());
    PersistenceManager.registerManager (new GandalfPersistenceManager ());
  }

  /** Module was uninstalled. */
  public void uninstalled () {
  // ---------------------------------------------------------------------------
  // 1. remove FormEditor actions
    uninstallActions ();

    // [PENDING - ask and delete ComponentPalette]
    // [PENDING - ask and delete Form templates]
  }
  
// -----------------------------------------------------------------------------
// Private methods
  
  private void installActions () {
    try {
      // install actions into menu
      Utilities2.createAction (InstallBeanAction.class, 
        DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Tools"), /* NO I18N */
        "UnmountFSAction", true, true, false, false /* NO I18N */
      );

      Utilities2.createAction (ComponentInspectorAction.class, 
        DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "View"), /* NO I18N */
        "HTMLViewAction", true, false, false, false /* NO I18N */
      );

      // install actions into toolbar
      DataFolder formFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Form"); /* NO I18N */
      DataFolder paletteFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Palette"); /* NO I18N */
  
      DataObject[] formInstances = {
        Utilities2.createAction (ComponentInspectorAction.class, formFolder),
        Utilities2.createAction (TestModeAction.class, formFolder),
        Utilities2.createAction (DesignModeAction.class, formFolder),
        Utilities2.createAction (ShowGridAction.class, formFolder),
      };
      formFolder.setOrder (formInstances);
  
      Utilities2.createAction (PaletteAction.class, paletteFolder);

      // install actions into action pool
      DataFolder formActions = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Form"); /* NO I18N */
      Utilities2.createAction (InstallBeanAction.class, formActions);
      Utilities2.createAction (ComponentInspectorAction.class, formActions);
      Utilities2.createAction (PaletteAction.class, formActions);
      Utilities2.createAction (CustomizeLayoutAction.class, formActions);
      Utilities2.createAction (DesignModeAction.class, formActions);
      Utilities2.createAction (EventsAction.class, formActions);
      Utilities2.createAction (GotoEditorAction.class, formActions);
      Utilities2.createAction (GotoFormAction.class, formActions);
      Utilities2.createAction (GotoInspectorAction.class, formActions);
      Utilities2.createAction (SelectLayoutAction.class, formActions);
      Utilities2.createAction (ShowGridAction.class, formActions);
      Utilities2.createAction (TestModeAction.class, formActions);

    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) { /* NO I18N */
        e.printStackTrace ();
      }
      // ignore failure to install
    }
  }

  private void uninstallActions () {
    try {
      // remove actions from menu
      Utilities2.removeAction (InstallBeanAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Tools")); /* NO I18N */
      Utilities2.removeAction (ComponentInspectorAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "View")); /* NO I18N */

      // remove actions from toolbar
      DataFolder formFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Form"); /* NO I18N */
      DataFolder paletteFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Palette"); /* NO I18N */
  
      Utilities2.removeAction (ComponentInspectorAction.class, formFolder);
      Utilities2.removeAction (TestModeAction.class, formFolder);
      Utilities2.removeAction (DesignModeAction.class, formFolder);
      Utilities2.removeAction (ShowGridAction.class, formFolder);
      Utilities2.removeAction (PaletteAction.class, paletteFolder);

      if (formFolder.getChildren ().length == 0) formFolder.delete ();
      if (paletteFolder.getChildren ().length == 0) paletteFolder.delete ();

      // remove actions from action pool
      DataFolder formActions = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Form"); /* NO I18N */
      Utilities2.removeAction (ComponentInspectorAction.class, formActions);
      Utilities2.removeAction (InstallBeanAction.class, formActions);
      Utilities2.removeAction (PaletteAction.class, formActions);
      Utilities2.removeAction (CustomizeLayoutAction.class, formActions);
      Utilities2.removeAction (DesignModeAction.class, formActions);
      Utilities2.removeAction (EventsAction.class, formActions);
      Utilities2.removeAction (GotoEditorAction.class, formActions);
      Utilities2.removeAction (GotoFormAction.class, formActions);
      Utilities2.removeAction (GotoInspectorAction.class, formActions);
      Utilities2.removeAction (SelectLayoutAction.class, formActions);
      Utilities2.removeAction (ShowGridAction.class, formActions);
      Utilities2.removeAction (TestModeAction.class, formActions);

      if (formActions.getChildren ().length == 0) formActions.delete ();

    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) { /* NO I18N */
        e.printStackTrace ();
      }
      // ignore failure to uninstall
    }
  }

  private void copyTemplates () {
    try {
      FileUtil.extractJar (
        TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/developer/modules/loaders/form/resources/templates.jar") /* NO I18N */
      );
    } catch (java.io.IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
  }

  private void createComponentPalette () {
    FileObject root = TopManager.getDefault ().getRepository ().getDefaultFileSystem ().getRoot ();
    FileObject paletteFolder;
    if ((paletteFolder = root.getFileObject ("Palette")) == null) { /* NO I18N */
      try {
        paletteFolder = root.createFolder ("Palette"); /* NO I18N */
      } catch (java.io.IOException e) {
        TopManager.getDefault ().notify (new NotifyDescriptor.Message (NbBundle.getBundle (FormEditorModule.class).getString ("ERR_CreatingPalette"), NotifyDescriptor.ERROR_MESSAGE)); /* NO I18N */
        return;
      }
    }
    DataFolder paletteDataFolder = DataFolder.findFolder (paletteFolder);

    FileObject awtCategory = null; DataFolder awtFolder;
    FileObject swingCategory = null; DataFolder swingFolder;
    FileObject swing2Category = null; DataFolder swing2Folder;
    FileObject beansCategory = null; DataFolder beansFolder;
    FileObject layoutsCategory = null; DataFolder layoutsFolder;
    FileObject bordersCategory = null; DataFolder bordersFolder;

    java.util.ArrayList categoryErrors = new java.util.ArrayList ();
    java.util.ArrayList componentErrors = new java.util.ArrayList ();

    // -----------------------------------------------------------------------------
    // Create AWT Category and components
    try {
      if ((awtCategory = paletteFolder.getFileObject (AWT_CATEGORY_NAME)) == null) 
        awtCategory = paletteFolder.createFolder (AWT_CATEGORY_NAME);
      createInstances (awtCategory, defaultAWTComponents, null, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (AWT_CATEGORY_NAME);
    }
    awtFolder = DataFolder.findFolder (awtCategory);
    
    // -----------------------------------------------------------------------------
    // Create Swing Category and components
    try {
      if ((swingCategory = paletteFolder.getFileObject (SWING_CATEGORY_NAME)) == null) 
        swingCategory = paletteFolder.createFolder (SWING_CATEGORY_NAME);
      createInstances (swingCategory, defaultSwingComponents, null, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (SWING_CATEGORY_NAME);
    }
    swingFolder = DataFolder.findFolder (swingCategory);

    // -----------------------------------------------------------------------------
    // Create Swing2 Category and components
    try {
      if ((swing2Category = paletteFolder.getFileObject (SWING2_CATEGORY_NAME)) == null) 
        swing2Category = paletteFolder.createFolder (SWING2_CATEGORY_NAME);
      createInstances (swing2Category, defaultSwing2Components, null, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (SWING2_CATEGORY_NAME);
    }
    swing2Folder = DataFolder.findFolder (swing2Category);

    // -----------------------------------------------------------------------------
    // Create Beans Category and components
    try {
      if ((beansCategory = paletteFolder.getFileObject (BEANS_CATEGORY_NAME)) == null) 
        beansCategory = paletteFolder.createFolder (BEANS_CATEGORY_NAME);
      createInstances (beansCategory, defaultBeansComponents, null, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (BEANS_CATEGORY_NAME);
    }
    beansFolder = DataFolder.findFolder (beansCategory);

    // -----------------------------------------------------------------------------
    // Create Layouts Category and components
    try {
      if ((layoutsCategory = paletteFolder.getFileObject (LAYOUTS_CATEGORY_NAME)) == null) 
        layoutsCategory = paletteFolder.createFolder (LAYOUTS_CATEGORY_NAME);
      createInstances (layoutsCategory, defaultLayoutsComponents, defaultLayoutsIcons, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (LAYOUTS_CATEGORY_NAME);
    }
    layoutsFolder = DataFolder.findFolder (layoutsCategory);

    // -----------------------------------------------------------------------------
    // Create Borders Category and components
    try {
      if ((bordersCategory = paletteFolder.getFileObject (BORDERS_CATEGORY_NAME)) == null) 
        bordersCategory = paletteFolder.createFolder (BORDERS_CATEGORY_NAME);
      createInstances (bordersCategory, defaultBorders, defaultBordersIcons, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (BORDERS_CATEGORY_NAME);
    }
    bordersFolder = DataFolder.findFolder (bordersCategory);

    try {
      paletteDataFolder.setOrder (new DataObject[] { awtFolder, swingFolder, swing2Folder, beansFolder, layoutsFolder, bordersFolder } );
    } catch (java.io.IOException e) {
    }

    if ((categoryErrors.size () != 0) || (componentErrors.size () != 0)) {
      TopManager.getDefault ().notify (new NotifyDescriptor.Message (NbBundle.getBundle (FormEditorModule.class).getString ("ERR_ProblemsCreatingPalette"), NotifyDescriptor.WARNING_MESSAGE)); /* NO I18N */
    }
  }

  private void createInstances (FileObject folder, String[] classNames, String[] iconNames, java.util.Collection componentErrors) {
    java.util.ArrayList orderList = new java.util.ArrayList (classNames.length);
    for (int i = 0; i < classNames.length; i++) {
      String fileName = formatName (classNames[i]);
      FileLock lock = null;
      try {
        if (folder.getFileObject (fileName+".instance") == null) { /* NO I18N */
          FileObject fo = folder.createData (fileName, "instance"); /* NO I18N */
          if ((iconNames != null) && (iconNames [i] != null)) {
            lock = fo.lock ();
            java.io.OutputStream os = fo.getOutputStream (lock);
            String ic = "icon="+iconNames[i]; /* NO I18N */
            os.write (ic.getBytes ());
          }
          DataObject obj = DataObject.find (fo);
          if (obj != null) {
            orderList.add (obj);
          }
        }
      } catch (java.io.IOException e) {
        componentErrors.add (fileName);
      } finally {
        if (lock != null) {
          lock.releaseLock ();
        }
      }
    }
    if (orderList.size () > 0) {
      DataFolder dataFolder = DataFolder.findFolder (folder);
      if (dataFolder != null) {
        try {
          dataFolder.setOrder ((DataObject[])orderList.toArray (new DataObject[orderList.size ()]));
        } catch (java.io.IOException e) {
          // ignore failure to set order
        }
      }
    }
  }

  private String formatName (String className) {
    return className.substring (className.lastIndexOf (".") + 1) + "[" + className.replace ('.', '-') + "]"; /* NO I18N */
  }
  
// -----------------------------------------------------------------------------
// Default Palette contents
  
  /** The default AWT Components */
  private final static String[] defaultAWTComponents = new String[] {
    "java.awt.Label", /* NO I18N */
    "java.awt.Button", /* NO I18N */
    "java.awt.TextField", /* NO I18N */
    "java.awt.TextArea", /* NO I18N */
    "java.awt.Checkbox", /* NO I18N */
    "java.awt.Choice", /* NO I18N */
    "java.awt.List", /* NO I18N */
    "java.awt.Scrollbar", /* NO I18N */
    "java.awt.ScrollPane", /* NO I18N */
    "java.awt.Panel", /* NO I18N */
    "java.awt.Canvas", /* NO I18N */
    "java.awt.MenuBar", /* NO I18N */
    "java.awt.PopupMenu", /* NO I18N */
  };

  /** The default Swing Components */
  private final static String[] defaultSwingComponents = new String[] {
    "javax.swing.JLabel", /* NO I18N */
    "javax.swing.JButton", /* NO I18N */
    "javax.swing.JCheckBox", /* NO I18N */
    "javax.swing.JRadioButton", /* NO I18N */
    "javax.swing.JComboBox", /* NO I18N */
    "javax.swing.JList", /* NO I18N */
    "javax.swing.JTextField", /* NO I18N */
    "javax.swing.JTextArea", /* NO I18N */
    "javax.swing.JToggleButton", /* NO I18N */
    "javax.swing.JPanel", /* NO I18N */
    "javax.swing.JTabbedPane", /* NO I18N */
    "javax.swing.JScrollBar", /* NO I18N */
    "javax.swing.JScrollPane", /* NO I18N */
    "javax.swing.JMenuBar", /* NO I18N */
    "javax.swing.JPopupMenu", /* NO I18N */
  };

  /** The default Swing Components - Swing2 category */
  private final static String[] defaultSwing2Components = new String[] {
    "javax.swing.JSlider", /* NO I18N */
    "javax.swing.JProgressBar", /* NO I18N */
    "javax.swing.JSplitPane", /* NO I18N */
    "javax.swing.JPasswordField", /* NO I18N */
    "javax.swing.JSeparator", /* NO I18N */
    "javax.swing.JTextPane", /* NO I18N */
    "javax.swing.JEditorPane", /* NO I18N */
    "javax.swing.JTree", /* NO I18N */
    "javax.swing.JTable", /* NO I18N */
    "javax.swing.JToolBar", /* NO I18N */
    "javax.swing.JInternalFrame", /* NO I18N */
    "javax.swing.JLayeredPane", /* NO I18N */
    "javax.swing.JDesktopPane", /* NO I18N */
    "javax.swing.JOptionPane", /* NO I18N */
  };


//  private final static String[] defaultDBComponents = new String[] {
//    "com.netbeans.sql.JDBCRowSet", /* NO I18N */
//    "com.netbeans.sql.components.DataNavigator", /* NO I18N */
//  };

  /** The default Swing Components - beans category */
  private final static String[] defaultBeansComponents = new String[] {
    // for future use.
  };

  /** The default Layout Components */
  private final static String[] defaultLayoutsComponents = new String[] {
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignFlowLayout", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignBorderLayout", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignGridLayout", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignCardLayout", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignAbsoluteLayout", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignGridBagLayout", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignBoxLayout", /* NO I18N */
  };

  /** The default Layout Components */
  private final static String[] defaultLayoutsIcons = new String[] {
    "/com/netbeans/developer/modules/loaders/form/resources/palette/flowLayout.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/borderLayout.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/gridLayout.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/cardLayout.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/absoluteLayout.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/gridBagLayout.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/boxLayout.gif", /* NO I18N */
  };
  
  /** The default Swing Borders */
  private final static String[] defaultBorders = new String[] {
    "com.netbeans.developerx.loaders.form.formeditor.border.EmptyBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.LineBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.MatteIconBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.MatteColorBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.TitledBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.EtchedBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.BevelBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.SoftBevelBorderInfo", /* NO I18N */
    "com.netbeans.developerx.loaders.form.formeditor.border.CompoundBorderInfo", /* NO I18N */
  };
  
  /** The default Swing Borders */
  private final static String[] defaultBordersIcons = new String[] {
    "/com/netbeans/developer/modules/loaders/form/resources/palette/border.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/lineBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/matteIconBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/matteColorBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/titledBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/etchedBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/bevelBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/softBevelBorder.gif", /* NO I18N */
    "/com/netbeans/developer/modules/loaders/form/resources/palette/compoundBorder.gif", /* NO I18N */
  };
}

/*
 * Log
 *  39   Gandalf   1.38        1/4/00   Ian Formanek    Proper Removal of 
 *       actions, I18Nzed
 *  38   Gandalf   1.37        1/4/00   Ian Formanek    Uses 
 *       Utilities2.createAction for Toolbars and action pool
 *  37   Gandalf   1.36        11/27/99 Patrik Knakal   
 *  36   Gandalf   1.35        11/25/99 Ian Formanek    Uses Utilities module
 *  35   Gandalf   1.34        10/27/99 Ian Formanek    Fixed bug 4596 - There 
 *       should be no separator in View menu between Component Inspector and 
 *       other windows.
 *  34   Gandalf   1.33        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  33   Gandalf   1.32        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  32   Gandalf   1.31        9/13/99  Ian Formanek    Fixed bug 3182 - 
 *       Property Editor for Border has two the same tabs.
 *  31   Gandalf   1.30        8/15/99  Ian Formanek    Removed 
 *       ListModelFormAware editor
 *  30   Gandalf   1.29        8/9/99   Ian Formanek    Used currentClassLoader 
 *       to fix problems with loading beans only present in repository
 *  29   Gandalf   1.28        7/31/99  Ian Formanek    Fixed registration of 
 *       BorderEditor
 *  28   Gandalf   1.27        7/15/99  Ian Formanek    Better cleanup when 
 *       uninstalling
 *  27   Gandalf   1.26        7/15/99  Ian Formanek    Installation of actions 
 *       into action pool, works better if actions already exist
 *  26   Gandalf   1.25        7/15/99  Ian Formanek    Items in 
 *       ComponentPalette are ordered
 *  25   Gandalf   1.24        7/11/99  Ian Formanek    Persistence managers 
 *       registered
 *  24   Gandalf   1.23        7/6/99   Ian Formanek    Installs menu and 
 *       toolbar actions...
 *  23   Gandalf   1.22        6/30/99  Ian Formanek    added registration of 
 *       ListModelFormAwareEditor
 *  22   Gandalf   1.21        6/22/99  Ian Formanek    Added Canvas to AWT 
 *       components
 *  21   Gandalf   1.20        6/10/99  Ian Formanek    copy templates on 
 *       install
 *  20   Gandalf   1.19        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  19   Gandalf   1.18        6/7/99   Ian Formanek    AutoLoad beans enabled 
 *       again
 *  18   Gandalf   1.17        6/4/99   Ian Formanek    
 *  17   Gandalf   1.16        5/30/99  Ian Formanek    Minor property editors 
 *       tweaks, fixed problem with empty border's icon
 *  16   Gandalf   1.15        5/14/99  Ian Formanek    
 *  15   Gandalf   1.14        5/14/99  Ian Formanek    
 *  14   Gandalf   1.13        5/11/99  Ian Formanek    Build 318 version
 *  13   Gandalf   1.12        5/4/99   Ian Formanek    Icons again
 *  12   Gandalf   1.11        4/26/99  Ian Formanek    
 *  11   Gandalf   1.10        4/23/99  Ian Formanek    Icons for layouts and 
 *       borders
 *  10   Gandalf   1.9         4/8/99   Ian Formanek    Removed BeanInfo init
 *  9    Gandalf   1.8         4/5/99   Ian Formanek    
 *  8    Gandalf   1.7         3/31/99  Ian Formanek    
 *  7    Gandalf   1.6         3/31/99  Ian Formanek    Fixed bug 1410 - Many 
 *       exceptions (see attachment) are thrown only during first startup after 
 *       installing.
 *  6    Gandalf   1.5         3/30/99  Ian Formanek    Creates default palette 
 *       on first installation
 *  5    Gandalf   1.4         3/30/99  Ian Formanek    
 *  4    Gandalf   1.3         3/27/99  Ian Formanek    
 *  3    Gandalf   1.2         3/26/99  Ian Formanek    
 *  2    Gandalf   1.1         3/22/99  Ian Formanek    
 *  1    Gandalf   1.0         3/22/99  Ian Formanek    
 * $
 */
