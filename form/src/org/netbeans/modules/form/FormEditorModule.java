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

import com.netbeans.ide.TopManager;
import com.netbeans.ide.NotifyDescriptor;
import com.netbeans.ide.filesystems.FileObject;
import com.netbeans.ide.loaders.DataObject;
import com.netbeans.ide.loaders.DataFolder;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.modules.ModuleInstall;

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
  // 1. create Component Palette under system
    createComponentPalette ();
  }

  /** Module installed again. */
  public void restored () {
//    System.out.println("FormEditorModule: restored");
  }

  /** Module was uninstalled. */
  public void uninstalled () {
    // [PENDING - ask and delete ComponentPalette]
  }

  /** Module is being closed. */
  public boolean closing () {
    return true; // agree to close
  }
  
// -----------------------------------------------------------------------------
// Private methods
  
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
      createInstances (awtCategory, defaultAWTComponents, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (AWT_CATEGORY_NAME);
    }
    awtFolder = DataFolder.findFolder (awtCategory);
    
    // -----------------------------------------------------------------------------
    // Create Swing Category and components
    try {
      if ((swingCategory = paletteFolder.getFileObject (SWING_CATEGORY_NAME)) == null) 
        swingCategory = paletteFolder.createFolder (SWING_CATEGORY_NAME);
      createInstances (swingCategory, defaultSwingComponents, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (SWING_CATEGORY_NAME);
    }
    swingFolder = DataFolder.findFolder (swingCategory);

    // -----------------------------------------------------------------------------
    // Create Swing2 Category and components
    try {
      if ((swing2Category = paletteFolder.getFileObject (SWING2_CATEGORY_NAME)) == null) 
        swing2Category = paletteFolder.createFolder (SWING2_CATEGORY_NAME);
      createInstances (swing2Category, defaultSwing2Components, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (SWING2_CATEGORY_NAME);
    }
    swing2Folder = DataFolder.findFolder (swing2Category);

    // -----------------------------------------------------------------------------
    // Create Beans Category and components
    try {
      if ((beansCategory = paletteFolder.getFileObject (BEANS_CATEGORY_NAME)) == null) 
        beansCategory = paletteFolder.createFolder (BEANS_CATEGORY_NAME);
      createInstances (beansCategory, defaultBeansComponents, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (BEANS_CATEGORY_NAME);
    }
    beansFolder = DataFolder.findFolder (beansCategory);

    // -----------------------------------------------------------------------------
    // Create Layouts Category and components
    try {
      if ((layoutsCategory = paletteFolder.getFileObject (LAYOUTS_CATEGORY_NAME)) == null) 
        layoutsCategory = paletteFolder.createFolder (LAYOUTS_CATEGORY_NAME);
      createInstances (layoutsCategory, defaultLayoutsComponents, componentErrors);
    } catch (java.io.IOException e) {
      categoryErrors.add (LAYOUTS_CATEGORY_NAME);
    }
    layoutsFolder = DataFolder.findFolder (layoutsCategory);

    // -----------------------------------------------------------------------------
    // Create Borders Category and components
    try {
      if ((bordersCategory = paletteFolder.getFileObject (BORDERS_CATEGORY_NAME)) == null) 
        bordersCategory = paletteFolder.createFolder (BORDERS_CATEGORY_NAME);
      createInstances (bordersCategory, defaultBorders, componentErrors);
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

  private void createInstances (FileObject folder, String[] classNames, java.util.Collection componentErrors) {
    for (int i = 0; i < classNames.length; i++) {
      String fileName = formatName (classNames[i]);
      try {
        if (folder.getFileObject (fileName+".instance") == null)
          folder.createData (fileName, "instance");
      } catch (java.io.IOException e) {
        componentErrors.add (fileName);
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
  
}

/*
 * Log
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
