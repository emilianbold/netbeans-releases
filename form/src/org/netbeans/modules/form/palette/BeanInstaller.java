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

package com.netbeans.developer.modules.loaders.form.palette;

import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.jar.*;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.netbeans.ide.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.developer.modules.loaders.form.FormLoaderSettings;

/** Bean Installer
*
* @author Petr Hamernik
*/
public final class BeanInstaller extends Object {

  /** Text resources */
  static final ResourceBundle bundle = NbBundle.getBundle(BeanInstaller.class);

  /** Last opened directory */
  private static String lastDirectory;
  
  /** Borders used in dialogs */
  static final Border hasFocusBorder;
  static final Border noFocusBorder;

  /** Initialize static fields */
  static {
    hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight"));
    noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    lastDirectory = null;
  }

  /** Extension of jar archive where to find module */
  static String JAR_EXT = ".jar";

  //==============================================================================
  // Installing new beans - user action.
  //==============================================================================
  
  /** Open the FileOpenDialog for the selection of Jar file and
  * install the selected module into the system.
  */
  public static void installBean() {
    String fileName = selectJarModule();

    if (fileName != null) {
      JarFileSystem jar = createJarForName(fileName);
      if (jar == null) {
        TopManager.getDefault().notify(new NotifyDescriptor.Message(
          bundle.getString("MSG_ErrorInFile"), NotifyDescriptor.ERROR_MESSAGE)
        );
      }
      else {
        boolean alreadyInstalled = false;
        Repository rep = TopManager.getDefault().getRepository();
        JarFileSystem jar2 = (JarFileSystem) rep.findFileSystem(jar.getSystemName());
        if (jar2 != null) {
          alreadyInstalled = true;
          jar = jar2;
        }
        
        LinkedList list = findJavaBeans(jar);
        
        /*
        BeanSelector sel = new BeanSelector(findJavaBeans(jar));
        sel.show();
        */
        if (list.size() == 0)
          return;

        if (!alreadyInstalled) {
          jar.setHidden(true);
          rep.addFileSystem(jar);
        }

        String pal = null; //selectPaletteCategory();
        if (pal != null)
          finishInstall(jar, list/*sel.getSelectedBeans()*/, pal);
/*        TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                                                    "Changes in the component palette will only take effect after Netbeans Developer have been restarted",
                                                                    NotifyDescriptor.INFORMATION_MESSAGE)
        ); */
                      
      }
    }
  }

  /** Scan all files with attributes in the given jar.
  * @return LinkedList of founded beans.
  */
  private static LinkedList findJavaBeans(JarFileSystem jar) {
    LinkedList foundJB = new LinkedList();

    // Looking for the beans
    Manifest manifest = jar.getManifest();
    Map entries = manifest.getEntries();

    Iterator it = entries.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();
      String value = ((Attributes) entries.get(key)).getValue("Java-Bean");
      if ((value != null) && value.equalsIgnoreCase("True")) {
        if (key.endsWith(".class")) {
          String wholeName = key.substring(0, key.length() - 6).replace('/', '.').replace('\\', '.');
          int lastDot = wholeName.lastIndexOf('.');
          String pack;
          String name;
          if (lastDot == -1) {
            pack = "";
            name = wholeName;
          }
          else {
            pack = wholeName.substring(0, lastDot);
            name = wholeName.substring(lastDot + 1);
          }
          FileObject fo = jar.find(pack, name, "class");
          if (fo != null) {
            foundJB.add(fo);
          }
        }
      }
    }
    return foundJB;
  }
  
  /** Finishing the instalation of the java beans.
  * @param jar JarFileSystem - the source of JBs
  * @param list Collection of FileObjects - selected JBs
  * @param pal palettecategory where to place beans.
  */
  private static void finishInstall(JarFileSystem jar, final Collection list, String pal) {
    FileObject root = TopManager.getDefault().getRepository().getDefaultFileSystem().getRoot();
    FileObject paletteFolder = root.getFileObject ("Palette");
    if (paletteFolder == null) {
      return;
    }

    if (pal == null) {
      pal = "Beans"; // defaul palette category
    }
    FileObject category = paletteFolder.getFileObject(pal);
    if (category == null) {
      return;
    }
    

    
/*    progress.setLabel(MessageFormat.format(progressLabel, new Object[] { "" }));
    progress.center();
    progress.show(); */
    
          ClassLoader loader = TopManager.getDefault().currentClassLoader();
          Iterator it = list.iterator();
          LinkedList paletteNodes = new LinkedList();

          while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            String name = fo.getPackageName('.');
            createInstance(category, name, null);
          }
//            progress.setLabel(MessageFormat.format(progressLabel, new Object[] { fo.getName() }));
//            progress.inc();
            /*
            try {
              Object o = java.beans.Beans.instantiate(loader, fo.getPackageName('.'));
              if (o == null) {
                notifyNotFound(fo.getPackageName ('.'));
                break;
              }  
              Class cl = o.getClass ();
//              PaletteNode n = new PaletteNode (cl, fo.getPackageName ('.'));
//              n.getIcon(); // resolving Icon invokes loading class.
//              paletteNodes.addElement (n);
            }
            catch (ClassNotFoundException e) {
              if (e.getMessage () != null)
                notifyNotFound(e.getMessage());
              else
                notifyNotFound (e.getClass ().getName () + ": "+fo.getPackageName ('.'));
              break;
            }
            catch (NoClassDefFoundError e) {
              if (e.getMessage () != null)
                notifyNotFound(e.getMessage());
              else
                notifyNotFound (e.getClass ().getName () + ": "+fo.getPackageName ('.'));
              break;
            }
            catch (Exception e) {
              TopManager.getDefault().notifyException(e);
              break;
            }
          }
//          progress.setLabel(topBundle.getString ("CTL_UpdatingPalette"));
//          progress.inc ();
          /*
          Node[] addNodes = new Node[paletteNodes.size ()];
          paletteNodes.copyInto (addNodes);
          pal.add(addNodes);
          */
//        }
//        finally {
//          progress.setVisible (false);
//          progress.dispose();
//        }
//      }
//    };
/*    if (sync) {
      task.run();
    }
    else {
      RequestProcessor.postRequest(task);
    }*/
  }

  private static void createInstance(FileObject folder, String className, String iconName) {
    String fileName = formatName(className);
    FileLock lock = null;
    try {
      if (folder.getFileObject(fileName+".instance") == null) {
        FileObject fo = folder.createData(fileName, "instance");
        if (iconName != null) {
          lock = fo.lock ();
          java.io.OutputStream os = fo.getOutputStream (lock);
          String ic = "icon="+iconName;
          os.write (ic.getBytes ());
        }
      }
    }
    catch (java.io.IOException e) {
      e.printStackTrace();
    }
    finally {
      if (lock != null) {
        lock.releaseLock ();
      }
    }
  }

  private static String formatName (String className) {
    String ret = className.substring(className.lastIndexOf (".") + 1) + "[" + className.replace ('.', '-') + "]";
    return ret;
  }

  /*
  static void notifyNotFound(String what) {
    String message = new MessageFormat(bundle.getString("ERR_ClassNotFound")).format(new Object[] { what });
    TopManager.getDefault().notify(new NotifyDescriptor.Exception(new Exception(), message));
  } */

  /** Opens dialog and lets user select category, where beans should be installed
  *
  private static PaletteCategory selectPaletteCategory() {
    PaletteSelector sel = new PaletteSelector();
    sel.show();
    return sel.getSelectedCategory();
  }*/

  /** This method open java.awt.FileDialog and selects
  * the jar file with the module.
  *
  * @return filename or null if operation was cancelled.
  */
  private static String selectJarModule() {
    FileDialog openDlg = new FileDialog(TopManager.getDefault().getWindowManager().getMainWindow(),
                                        bundle.getString("CTL_SelectJar"),
                                        FileDialog.LOAD);

    openDlg.setFilenameFilter(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.endsWith(JAR_EXT));
      }
    });

    if (lastDirectory != null)
      openDlg.setDirectory(lastDirectory);

    openDlg.setFile("*"+JAR_EXT);
    openDlg.setVisible(true);

    String fileName = openDlg.getFile();

    if ((fileName != null) && (fileName.endsWith(JAR_EXT))) {
      lastDirectory = openDlg.getDirectory();
      return lastDirectory + fileName;
    }
    else
      return null;
  }

  /**
  * @return jar FS for the given name or null if some problems occured
  */
  private static JarFileSystem createJarForName(String name) {
    try {
      JarFileSystem jar = new JarFileSystem();
      jar.setJarFile(new File(name));
      return jar;
    }
    catch (PropertyVetoException e) {
      return null;
    }
    catch (IOException e) {
      return null;
    }
  }

  //==============================================================================
  // Auto loading beans on startup
  //==============================================================================

  /** Auto loading all jars - beans */
  public static void autoLoadBeans() {
    FormLoaderSettings settings = new FormLoaderSettings();
    java.util.HashMap loadedBeans = settings.getLoadedBeans();

    File globalFolder = new File(System.getProperty("netbeans.home") + File.separator + "beans");
    try {
      globalFolder = new File(globalFolder.getCanonicalPath());
    }
    catch (IOException e) { }

    File localFolder = new File(System.getProperty("netbeans.user") + File.separator + "beans");
    try {
      localFolder = new File(localFolder.getCanonicalPath());
    }
    catch (IOException e) { }
    
    autoLoadFolder(globalFolder, loadedBeans);
    if (!globalFolder.equals(localFolder))
      autoLoadFolder(localFolder, loadedBeans);

    settings.setLoadedBeans(loadedBeans);
    
    java.awt.EventQueue.invokeLater (
      new Runnable () {
        public void run () {
          ComponentPalette.getDefault ().updatePalette ();
        }
      }
    );
  }

  /** Loads the beans stored in the given folder.
  * @param folder - where to find jars
  * @param loadedBeans - names of jars already loaded (in previous NB sessions)
  */
  private static void autoLoadFolder(File folder, HashMap loadedBeans) {
    if (!folder.exists())
      return;
    
    final String[] list = folder.list();
    final String base = folder.getAbsolutePath() + File.separator;
    Properties details = new Properties();
    try {
      details.load(new FileInputStream(base + "beans.properties"));
    }
    catch (IOException e) {
    }

    String[] categories = ComponentPalette.getDefault ().getPaletteCategories();
    for (int i = 0; i < list.length; i++) {
      if (list[i].endsWith(JAR_EXT) && (loadedBeans.get(list[i]) == null)) {
        String withoutExt = list[i].substring(0, list[i].length() - JAR_EXT.length());
        String categoryName = details.getProperty(withoutExt, withoutExt);
        if (autoLoadJar(base + list[i], categoryName, details.getProperty(withoutExt + ".beans"))) {
          loadedBeans.put(list[i], list[i]);
        }
      }
    }
  }

  /** Loaded beans from the jar.
  * @param name - name of the jar File
  * @param palette - category where to place the beans
  * @param selection - the selection of beans which should be installed.
  *       May be null - then all beans are loaded.
  */
  private static boolean autoLoadJar(String name, String paletteCategory, String selection) {
    JarFileSystem jar = createJarForName(name);
    Vector v = null;
    if (jar == null) {
/*      TopManager.getDefault().notify(
        new NotifyDescriptor.Message(topBundle.getString("MSG_ErrorInFile"),
                                     NotifyDescriptor.ERROR_MESSAGE)
        ); */
      return false;
    }
    else {
      JarFileSystem jar2 = (JarFileSystem) TopManager.getDefault ().getRepository ().findFileSystem (jar.getSystemName());
      if (jar2 != null)
        jar = jar2;

      LinkedList list = findJavaBeans(jar);
      if (selection != null) {
        Vector dest = new Vector();
        StringTokenizer tok = new StringTokenizer(selection, ", ", false);
        while (tok.hasMoreTokens()) {
          String token = tok.nextToken();
          String clName = token;
          String clPack = "";
          
          int lastDot = token.lastIndexOf('.');
          if ((lastDot != -1) && (!token.endsWith("."))) {
            clName = token.substring(lastDot + 1);
            clPack = token.substring(0, lastDot);
          }
          FileObject fo = jar.find(clPack, clName, "class");
          if (fo != null) {
            Iterator it = list.iterator ();
            while (it.hasNext()) {
              FileObject fo2 = (FileObject) it.next();
              if (fo.equals(fo2)) {
                dest.addElement(fo);
              }
            }
          }
        }
        v = dest;
      }
      finishInstall(jar, v, paletteCategory);
      return true;
    }
  }
  
  
  //==============================================================================
  // Inner classes
  //==============================================================================

  /*
  static class PaletteSelector extends CoronaDialog {
    private JList list;
    private ButtonBarButton okButton;
    private ButtonBarButton cancelButton;
    private Node[] categories;
    private boolean stamped = false;

    /** Creates a new ExceptionBox for given exception descriptor. *
    public PaletteSelector() {
      super(null);

      setDefaultCloseOperation (javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
      addWindowListener (new java.awt.event.WindowAdapter () {
          public void windowClosing (java.awt.event.WindowEvent evt) {
            closeDlg (false);
          }
        }
      );
      
      // attach cancel also to Escape key
      getRootPane().registerKeyboardAction(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            closeDlg (false);
          }
        },
        javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
        javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
      );
   

      setTitle(topBundle.getString("CTL_SelectPalette"));
      okButton = new ButtonBarButton(topBundle.getString("CTL_Select"));
      cancelButton = new ButtonBarButton(topBundle.getString("CTL_Cancel"));
      getButtonBar().setButtons(new ButtonBarButton[0],
                                new ButtonBarButton[] { okButton, cancelButton });

      okButton.setDefault(true);
      okButton.setEnabled(false);
      cancelButton.setEnabled(true);

      categories = PaletteContext.getPaletteContext().getPaletteCategories();

      String[] str = new String[categories.length];
      for (int i = 0; i < categories.length; i++) {
        str[i] = categories[i].getDisplayName();
      }
      list = new JList(str);
      list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      list.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent evt) {
          okButton.setEnabled(!list.isSelectionEmpty());
        }
      });

      list.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            closeDlg(true);
          }
        }
      });

      BorderLayout layout = new BorderLayout();
      layout.setVgap(5);
      layout.setHgap(5);
      getCustomPane().setLayout(layout);
      getCustomPane().add(new JLabel(topBundle.getString("CTL_PaletteCategories")), "North");
      getCustomPane().add(new JScrollPane (list), "Center");
      getCustomPane().setBorder(new EmptyBorder(5, 5, 5, 5));
      center();
    }

    /** Called when user presses a button on the ButtonBar.
    * @param evt The button press event.
    *
    protected void buttonPressed(ButtonBar.ButtonBarEvent evt) {
      closeDlg(evt.getButton() == okButton);
    }

    void closeDlg(boolean ok) {
      stamped = ok;
      setVisible (false);
      dispose();
    }

    public PaletteCategory getSelectedCategory() {
      if (! stamped) return null;
      int index = list.getSelectedIndex();
      if (index == -1)
        return null; // PaletteContext.getPaletteContext().getDefaultCategory();
      else {
        return (PaletteCategory) categories[index];
      }
    }

    public Dimension getPreferredSize() {
      Dimension ret = super.getPreferredSize();
      ret.width = Math.max(ret.width, 350);
      ret.height = Math.max(ret.height, 250);
      return ret;
    }
  }  */

  /** dialog which allows to select found beans *
  public static class BeanSelector extends CoronaDialog {
    private ButtonBarButton installButton;
    private ButtonBarButton cancelButton;
    private JList list;
    private Vector selected;

    /** Creates a new ExceptionBox for given exception descriptor. *
    public BeanSelector(Vector fileObjects) {
      super(null);
      
      setDefaultCloseOperation (javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
      addWindowListener (new java.awt.event.WindowAdapter () {
          public void windowClosing (java.awt.event.WindowEvent evt) {
            closeDlg (false);
          }
        }
      );
      
      // attach cancel also to Escape key
      getRootPane().registerKeyboardAction(
        new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
            closeDlg (false);
          }
        },
        javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
        javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
      );
   
      setTitle(topBundle.getString("CTL_SelectJB"));
      selected = new Vector();
      cancelButton = new ButtonBarButton(commonBundle.getString("CancelButton"));
      installButton = new ButtonBarButton(topBundle.getString("CTL_Install"));
      getButtonBar().setButtons(new ButtonBarButton[0],
                                new ButtonBarButton[] { installButton, cancelButton });

      installButton.setDefault(true);
      installButton.setEnabled(false);

      list = new JList(fileObjects);
      list.setCellRenderer(new FileObjectRenderer());

      list.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent evt) {
          installButton.setEnabled(!list.isSelectionEmpty());
        }
      });

      list.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() == 2) {
            closeDlg(true);
          }
        }
      });

      BorderLayout layout = new BorderLayout();
      layout.setVgap(5);
      layout.setHgap(5);
      getCustomPane().setLayout(layout);
      getCustomPane().add(new JLabel(topBundle.getString("CTL_SelectBeans")), "North");
      getCustomPane().add(new JScrollPane (list), "Center");
      getCustomPane().setBorder(new EmptyBorder(5, 5, 5, 5));
      center();
    }

    /** Called when user presses a button on the ButtonBar.
    * @param evt The button press event.
    *
    protected void buttonPressed(ButtonBar.ButtonBarEvent evt) {
      closeDlg(installButton.equals(evt.getButton()));
    }

    void closeDlg(boolean ok) {
      if (ok) {
        Object[] arr = list.getSelectedValues();
        for (int i = 0; i < arr.length; i++)
          selected.addElement(arr[i]);
      }
      setVisible (false);
      dispose();
    }

    Vector getSelectedBeans() {
      return selected;
    }

    public Dimension getPreferredSize() {
      Dimension ret = super.getPreferredSize();
      ret.width = Math.max(ret.width, 350);
      ret.height = Math.max(ret.height, 250);
      return ret;
    }
    }  */
  
  /*
  static class FileObjectRenderer extends JLabel implements ListCellRenderer {
    /** Creates a new NetbeansListCellRenderer *
    public FileObjectRenderer() {
      setOpaque(true);
      setBorder(noFocusBorder);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
      if (!(value instanceof FileObject))
        return this;

      FileObject fo = (FileObject) value;

      setText(fo.getName());
      if (isSelected){
        setBackground(UIManager.getColor("List.selectionBackground"));
        setForeground(UIManager.getColor("List.selectionForeground"));
      }
      else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);
      return this;
    }
  }*/
}

/*
 * Log
 *  3    Gandalf   1.2         6/4/99   Ian Formanek    First cut of 
 *       autoLoadBeans
 *  2    Gandalf   1.1         5/17/99  Petr Hamernik   very simple version of 
 *       Beans installer
 *  1    Gandalf   1.0         5/17/99  Petr Hamernik   
 * $
 */
