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
import com.netbeans.developer.modules.loaders.form.actions.*;
import com.netbeans.developer.modules.loaders.form.palette.*;

/**
* Module installation class for Form Editor
*
* @author Ian Formanek
*/
public class FormEditorModule implements ModuleInstall {

  private static final String AWT_CATEGORY_NAME = "AWT";
  private static final String SWING_CATEGORY_NAME = "Swing";
  private static final String SWING2_CATEGORY_NAME = "Swing2";
  private static final String BEANS_CATEGORY_NAME = "Beans";
  private static final String LAYOUTS_CATEGORY_NAME = "Layouts";
  private static final String BORDERS_CATEGORY_NAME = "Borders";

  /** Module installed for the first time. */
  public void installed () {
//    System.out.println("FormEditorModule: installed");

  // -----------------------------------------------------------------------------
  // 1. create FormEditor actions
    installActions ();

  // -----------------------------------------------------------------------------
  // 2. copy FormEditor templates
    copyTemplates ();

  // -----------------------------------------------------------------------------
  // 3. create Component Palette under system
    createComponentPalette ();

    restored ();
  }

  /** Module installed again. */
  public void restored () {
    // [PENDING - ugly workaround so that borders editor works - ideally, a FormPropertyEditorManager would be used for finding border's properties editors]
    java.beans.PropertyEditorManager.registerEditor (javax.swing.border.Border.class, com.netbeans.developer.explorer.propertysheet.editors.BorderEditor.class);
    FormPropertyEditorManager.registerEditor (javax.swing.ListModel.class, com.netbeans.developer.modules.loaders.form.editors.ListModelFormAwareEditor.class);
    BeanInstaller.autoLoadBeans ();

    // register standard persistence managers
    PersistenceManager.registerManager (new TuborgPersistenceManager ());
    PersistenceManager.registerManager (new GandalfPersistenceManager ());
  }

  /** Module was uninstalled. */
  public void uninstalled () {
  // -----------------------------------------------------------------------------
  // 1. remove FormEditor actions
    uninstallActions ();

    // [PENDING - ask and delete ComponentPalette]
    // [PENDING - ask and delete Form templates]
  }

  /** Module is being closed. */
  public boolean closing () {
    return true; // agree to close
  }
  
// -----------------------------------------------------------------------------
// Private methods
  
  private void installActions () {
    try {
      // install actions into menu
      createAction (InstallBeanAction.class, 
        DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Tools"), 
        "UnmountFSAction", true, true, false, false
      );

      createAction (ComponentInspectorAction.class, 
        DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "View"), 
        "HTMLViewAction", true, false, true, false
      );

      // install actions into toolbar
      DataFolder formFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Form");
      DataFolder paletteFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Palette");
  
      DataObject[] formInstances = {
        InstanceDataObject.create (formFolder, Utilities.getShortClassName (ComponentInspectorAction.class), ComponentInspectorAction.class.getName ()),
        InstanceDataObject.create (formFolder, Utilities.getShortClassName (TestModeAction.class), TestModeAction.class.getName ()),
        InstanceDataObject.create (formFolder, Utilities.getShortClassName (DesignModeAction.class), DesignModeAction.class.getName ()),
        InstanceDataObject.create (formFolder, Utilities.getShortClassName (ShowGridAction.class), ShowGridAction.class.getName ()),
      };
      formFolder.setOrder (formInstances);
  
      InstanceDataObject.create (paletteFolder, Utilities.getShortClassName (PaletteAction.class), PaletteAction.class.getName ());

      // install actions into action pool
      DataFolder formActions = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Form");
      InstanceDataObject.create (formActions, "InstallBeanAction", InstallBeanAction.class.getName ());
      InstanceDataObject.create (formActions, "ComponentInspectorAction", ComponentInspectorAction.class.getName ());
      InstanceDataObject.create (formActions, "PaletteAction", PaletteAction.class.getName ());
      InstanceDataObject.create (formActions, "CustomizeLayoutAction", CustomizeLayoutAction.class.getName ());
      InstanceDataObject.create (formActions, "DesignModeAction", DesignModeAction.class.getName ());
      InstanceDataObject.create (formActions, "EventsAction", EventsAction.class.getName ());
      InstanceDataObject.create (formActions, "GotoEditorAction", GotoEditorAction.class.getName ());
      InstanceDataObject.create (formActions, "GotoFormAction", GotoFormAction.class.getName ());
      InstanceDataObject.create (formActions, "GotoInspectorAction", GotoInspectorAction.class.getName ());
      InstanceDataObject.create (formActions, "SelectLayoutAction", SelectLayoutAction.class.getName ());
      InstanceDataObject.create (formActions, "ShowGridAction", ShowGridAction.class.getName ());
      InstanceDataObject.create (formActions, "TestModeAction", TestModeAction.class.getName ());

    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) {
        e.printStackTrace ();
      }
      // ignore failure to install
    }
  }

  private void uninstallActions () {
    try {
      // remove actions from menu
      removeAction (InstallBeanAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Tools"));
      removeAction (ComponentInspectorAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "View"));

      // remove actions from toolbar
      DataFolder formFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Form");
      DataFolder paletteFolder = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().toolbars (), "Palette");
  
      InstanceDataObject.remove (formFolder, Utilities.getShortClassName (ComponentInspectorAction.class), ComponentInspectorAction.class.getName ());
      InstanceDataObject.remove (formFolder, Utilities.getShortClassName (TestModeAction.class), TestModeAction.class.getName ());
      InstanceDataObject.remove (formFolder, Utilities.getShortClassName (DesignModeAction.class), DesignModeAction.class.getName ());
      InstanceDataObject.remove (formFolder, Utilities.getShortClassName (ShowGridAction.class), ShowGridAction.class.getName ());
      InstanceDataObject.remove (paletteFolder, Utilities.getShortClassName (PaletteAction.class), PaletteAction.class.getName ());

      // remove actions from action pool
      DataFolder formActions = DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Form");
      removeAction (ComponentInspectorAction.class, formActions);
      removeAction (InstallBeanAction.class, formActions);
      removeAction (PaletteAction.class, formActions);
      removeAction (CustomizeLayoutAction.class, formActions);
      removeAction (DesignModeAction.class, formActions);
      removeAction (EventsAction.class, formActions);
      removeAction (GotoEditorAction.class, formActions);
      removeAction (GotoFormAction.class, formActions);
      removeAction (GotoInspectorAction.class, formActions);
      removeAction (SelectLayoutAction.class, formActions);
      removeAction (ShowGridAction.class, formActions);
      removeAction (TestModeAction.class, formActions);

    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) {
        e.printStackTrace ();
      }
      // ignore failure to uninstall
    }
  }

  private void createAction (Class actionClass, DataFolder folder, String relativeTo, boolean after, boolean skipSeparator, boolean separatorBefore, boolean separatorAfter) 
  throws java.io.IOException {
    String actionShortName = Utilities.getShortClassName (actionClass);
    String actionName = actionClass.getName ();

    if (InstanceDataObject.find (folder, actionShortName, actionName) != null) return;

    DataObject[] children = folder.getChildren ();
    int indexToUse = -1;
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof InstanceDataObject && children[i].getPrimaryFile ().getName ().indexOf (relativeTo) != -1) {
        indexToUse = i;
        break;
      }
    }
    InstanceDataObject actionInstance = InstanceDataObject.create (folder, actionShortName, actionName);

    if (indexToUse != -1) {
      if (after) {
        indexToUse += 1;
        if (skipSeparator) {
          if ((indexToUse < children.length) && (children[indexToUse].getPrimaryFile ().getName ().indexOf ("JSeparator") != -1)) {
            indexToUse += 1;
          }
        }
      } else {
        if (skipSeparator) {
          if ((indexToUse > 0) && (children[indexToUse - 1].getPrimaryFile ().getName ().indexOf ("JSeparator") != -1)) {
            indexToUse -= 1;
          }
        }
      }

      InstanceDataObject beforeSeparator = separatorBefore ? InstanceDataObject.create (folder, "Separator1-"+actionShortName, "javax.swing.JSeparator") : null;
      InstanceDataObject afterSeparator = separatorAfter ? InstanceDataObject.create (folder, "Separator2-"+actionShortName, "javax.swing.JSeparator") : null;

      int itemsAdded = 0;
      if (separatorBefore) itemsAdded ++;
      if (separatorAfter) itemsAdded ++;
      int currentIndex = indexToUse;

      DataObject[] newOrder = new DataObject [children.length + 1 + itemsAdded];
      System.arraycopy (children, 0, newOrder, 0, indexToUse);

      if (separatorBefore) newOrder[currentIndex++] = beforeSeparator;
      newOrder[currentIndex++] = actionInstance;
      if (separatorAfter) newOrder[currentIndex++] = afterSeparator;

      System.arraycopy (children, indexToUse, newOrder, indexToUse + 1 + itemsAdded, children.length - indexToUse);
      folder.setOrder (newOrder);
    }
  }

  private void removeAction (Class actionClass, DataFolder folder) throws java.io.IOException {
    String actionShortName = Utilities.getShortClassName (actionClass);
    InstanceDataObject.remove (folder, actionShortName, actionClass.getName ());
    try {
      InstanceDataObject.remove (folder, "Separator1-"+actionShortName, "javax.swing.JSeparator");
      InstanceDataObject.remove (folder, "Separator2-"+actionShortName, "javax.swing.JSeparator");
    } catch (Exception e) {
      // these do not have to exist, wo we will catch the exception silently
    }
  }

  private void copyTemplates () {
    try {
      FileUtil.extractJar (
        TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/developer/modules/loaders/form/resources/templates.jar")
      );
    } catch (java.io.IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
  }

  private void createComponentPalette () {
    FileObject root = TopManager.getDefault ().getRepository ().getDefaultFileSystem ().getRoot ();
    FileObject paletteFolder;
    if ((paletteFolder = root.getFileObject ("Palette")) == null) {
      try {
        paletteFolder = root.createFolder ("Palette");
      } catch (java.io.IOException e) {
        TopManager.getDefault ().notify (new NotifyDescriptor.Message (NbBundle.getBundle (FormEditorModule.class).getString ("ERR_CreatingPalette"), NotifyDescriptor.ERROR_MESSAGE));
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
      TopManager.getDefault ().notify (new NotifyDescriptor.Message (NbBundle.getBundle (FormEditorModule.class).getString ("ERR_ProblemsCreatingPalette"), NotifyDescriptor.WARNING_MESSAGE));
    }
  }

  private void createInstances (FileObject folder, String[] classNames, String[] iconNames, java.util.Collection componentErrors) {
    java.util.ArrayList orderList = new java.util.ArrayList (classNames.length);
    for (int i = 0; i < classNames.length; i++) {
      String fileName = formatName (classNames[i]);
      FileLock lock = null;
      try {
        if (folder.getFileObject (fileName+".instance") == null) {
          FileObject fo = folder.createData (fileName, "instance");
          if ((iconNames != null) && (iconNames [i] != null)) {
            lock = fo.lock ();
            java.io.OutputStream os = fo.getOutputStream (lock);
            String ic = "icon="+iconNames[i];
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
    return className.substring (className.lastIndexOf (".") + 1) + "[" + className.replace ('.', '-') + "]";
  }
  
// -----------------------------------------------------------------------------
// Default Palette contents
  
  /** The default AWT Components */
  private final static String[] defaultAWTComponents = new String[] {
    "java.awt.Label",
    "java.awt.Button",
    "java.awt.TextField",
    "java.awt.TextArea",
    "java.awt.Checkbox",
    "java.awt.Choice",
    "java.awt.List",
    "java.awt.Scrollbar",
    "java.awt.ScrollPane",
    "java.awt.Panel",
    "java.awt.Canvas",
    "java.awt.MenuBar",
    "java.awt.PopupMenu",
  };

  /** The default Swing Components */
  private final static String[] defaultSwingComponents = new String[] {
    "javax.swing.JLabel",
    "javax.swing.JButton",
    "javax.swing.JCheckBox",
    "javax.swing.JRadioButton",
    "javax.swing.JComboBox",
    "javax.swing.JList",
    "javax.swing.JTextField",
    "javax.swing.JTextArea",
    "javax.swing.JToggleButton",
    "javax.swing.JPanel",
    "javax.swing.JTabbedPane",
    "javax.swing.JScrollBar",
    "javax.swing.JScrollPane",
    "javax.swing.JMenuBar",
    "javax.swing.JPopupMenu",
  };

  /** The default Swing Components - Swing2 category */
  private final static String[] defaultSwing2Components = new String[] {
    "javax.swing.JSlider",
    "javax.swing.JProgressBar",
    "javax.swing.JSplitPane",
    "javax.swing.JPasswordField",
    "javax.swing.JSeparator",
    "javax.swing.JTextPane",
    "javax.swing.JEditorPane",
    "javax.swing.JTree",
    "javax.swing.JTable",
    "javax.swing.JToolBar",
    "javax.swing.JInternalFrame",
    "javax.swing.JLayeredPane",
    "javax.swing.JDesktopPane",
    "javax.swing.JOptionPane",
  };

  /*
  private final static String[] defaultDBComponents = new String[] {
    "com.netbeans.sql.JDBCRowSet",
    "com.netbeans.sql.components.DataNavigator",
  };*/

  /** The default Swing Components - beans category */
  private final static String[] defaultBeansComponents = new String[] {
    // for future use.
  };

  /** The default Layout Components */
  private final static String[] defaultLayoutsComponents = new String[] {
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignFlowLayout",
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignBorderLayout",
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignGridLayout",
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignCardLayout",
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignAbsoluteLayout",
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignGridBagLayout",
    "com.netbeans.developerx.loaders.form.formeditor.layouts.DesignBoxLayout",
  };

  /** The default Layout Components */
  private final static String[] defaultLayoutsIcons = new String[] {
    "/com/netbeans/developer/modules/loaders/form/resources/palette/flowLayout.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/borderLayout.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/gridLayout.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/cardLayout.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/absoluteLayout.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/gridBagLayout.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/boxLayout.gif",
  };
  
  /** The default Swing Borders */
  private final static String[] defaultBorders = new String[] {
    "com.netbeans.developerx.loaders.form.formeditor.border.EmptyBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.LineBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.MatteIconBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.MatteColorBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.TitledBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.EtchedBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.BevelBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.SoftBevelBorderInfo",
    "com.netbeans.developerx.loaders.form.formeditor.border.CompoundBorderInfo",
  };
  
  /** The default Swing Borders */
  private final static String[] defaultBordersIcons = new String[] {
    "/com/netbeans/developer/modules/loaders/form/resources/palette/border.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/lineBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/matteIconBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/matteColorBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/titledBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/etchedBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/bevelBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/softBevelBorder.gif",
    "/com/netbeans/developer/modules/loaders/form/resources/palette/compoundBorder.gif",
  };
}

/*
 * Log
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
