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


package org.netbeans.modules.form.palette;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.jar.*;
import java.beans.*;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import org.openide.*;
import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;

//import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.GlobalJarFileSystem;

/**
 * Bean Installer
 * @author Petr Hamernik
 */
public final class BeanInstaller
{
    /** Last opened directory */
    private static File lastDirectory;

    /** Extension of jar archive where to find module */

    static String JAR_EXT = ".jar"; // NOI18N

    /**
     * asks the user for a jar file, lets him choose available beans and
     * install them into the component palette
     */

    public static void installBean() {
        File jarFile = selectJarModule();
        if (jarFile == null)
            return;

        JarFileSystem jar = createJarForFile(jarFile);
        if (jar == null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                CPManager.getBundle().getString(
                    "MSG_ErrorInFile"), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
            return;
        }

        Collection beans = findJavaBeans(jar);

        if (beans.size() == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                CPManager.getBundle().getString(
                    "MSG_noBeansInJar"), NotifyDescriptor.INFORMATION_MESSAGE)); // NOI18N
            return;
        }

        BeanSelector sel = new BeanSelector(beans);
        DialogDescriptor desc = new DialogDescriptor(
            sel,
            CPManager.getBundle().getString("CTL_SelectJB"), // NOI18N
            true,
            null
            );
        desc.setHelpCtx(new HelpCtx(BeanInstaller.class.getName() + ".installBean")); // NOI18N

        DialogDisplayer.getDefault().createDialog(desc).show();
        if (desc.getValue() == NotifyDescriptor.OK_OPTION) {
            String cat = selectPaletteCategory();
            if (cat != null) {
                installBeans(jar, sel.getSelectedBeans(), cat);
            }
        }
    }

    public static void installBeans(Node[] nodes) {
        String cat = selectPaletteCategory();
        if (cat == null)
            return;

        ArrayList list = new ArrayList(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            DataObject dobj = (DataObject)
                               nodes[i].getCookie(DataObject.class);
            InstanceCookie ic = (InstanceCookie)
                                nodes[i].getCookie(InstanceCookie.class);

            if (dobj != null && ic != null) {
                FileObject fo = dobj.getPrimaryFile();
                if ("instance".equals(fo.getExt())
                        || "java".equals(fo.getExt())
                        || "class".equals(fo.getExt()))
                    list.add(ic);
                else
                    list.add(fo);
            }
        }
        installBeans(null, list, cat);
    }

    /**
     * searches the jar for beans
     * @return Collection of founded beans represented as FileObjects
     */

    private static Collection findJavaBeans(JarFileSystem jar) {
        LinkedList beans = new LinkedList();

        Manifest manifest = jar.getManifest();
        Map entries = manifest.getEntries();

        Iterator it = entries.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (!key.endsWith(".class") && !key.endsWith(".ser"))
                continue;

            String value = ((Attributes) entries.get(key)).getValue("Java-Bean"); // NOI18N
            if ((value == null) || ! value.equalsIgnoreCase("True")) // NOI18N
                continue;

            String exts[] = { ".class", ".ser" }; // NOI18N
            for (int i = 0; i < exts.length; i++) {
                String ext = exts[i];
                if (!key.endsWith(ext))
                    continue;
                String resourcePath = key.replace('\\', '/');
                FileObject fo = jar.findResource(resourcePath);
                if (fo != null) {
                    beans.add(fo);
                }
            }
        }
        return beans;
    }

    /**
     * installs selected beans from a specified jar into a palette category
     * @param jar JarFileSystem - the source of beans to be mounted, or null if
     * not necessary
     * @param list Collection of FileObjects - selected JBs
     * @param cat palettecategory where to place beans.
     */

    private static void installBeans(JarFileSystem jar, Collection beans, String cat) {
        if (jar != null) addJarFileSystem(jar);

        if (cat == null) {
            cat = "Beans"; // default palette category // NOI18N
        }

        FileSystem fs = Repository.getDefault().getDefaultFileSystem();

        FileObject root = fs.getRoot();
        FileObject paletteFolder = root.getFileObject("Palette"); // NOI18N
        if (paletteFolder == null) {
            return;
        }

        FileObject category = paletteFolder.getFileObject(cat);
        if (category == null) {
            try {
                category = paletteFolder.createFolder(cat);
            } catch (IOException e) {
                if (System.getProperty("netbeans.debug.exceptions") != null)
                    e.printStackTrace();
                /* ignore */
                return;
            }
        }

        final FileObject categoryFolder = category;
        final Iterator it = beans.iterator();
        final LinkedList paletteNodes = new LinkedList();

        try {
            fs.runAtomicAction(new FileSystem.AtomicAction () {
                public void run() {
                    while (it.hasNext()) {
                        Object obj = it.next();
                        String name = null;
                        if (obj instanceof FileObject) {
                            if ("class".equals(((FileObject)obj).getExt())) { // NOI18N
                                assert false : "XXX needs to be rewritten";
                                /*
                                name =((FileObject)obj).getPackageName('.');
                                if (name != null)
                                    createInstance(categoryFolder, name);
                                 */
                            }
                            else {
                                createShadow(categoryFolder, (FileObject)obj);
                            }
                        }
                        else if (obj instanceof InstanceCookie) {
                            name = ((InstanceCookie) obj).instanceName();
                            if (name != null)
                                createInstance(categoryFolder, name);
                        }
                    }
                }
            });
        }
        catch (IOException cannotHappen) {
        }
    }

    private static void addJarFileSystem(JarFileSystem jar) {
        boolean alreadyInstalled = false;
        if (jar != null) {
            Repository rep = Repository.getDefault();
            JarFileSystem jar2 =(JarFileSystem) rep.findFileSystem(jar.getSystemName());
            if (jar2 != null) {
                alreadyInstalled = true;
                jar = jar2;
            }

            if (!alreadyInstalled) {
                jar.setHidden(true);
                rep.addFileSystem(jar);
            }
        }
    }
    
    static void createShadow(FileObject folder, FileObject original) {
        try {
            DataObject originalDO = DataObject.find(original);
            //      System.out.println("createShadow: "+folder + ", : " + original +", : "+originalDO); // NOI18N
            if (originalDO != null) {
                DataShadow.create(DataFolder.findFolder(folder), originalDO);
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    static void createInstance(FileObject folder, String className) {
        // first check if the class is valid and can be loaded
        try {
            ClassLoader loader = (ClassLoader)org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
            Class.forName(className, true, loader);
        }
        catch (Throwable ex) {
            if (ex instanceof ThreadDeath)
                throw (ThreadDeath)ex;
            
            ErrorManager manager = ErrorManager.getDefault();
            
            String message = MessageFormat.format(
                CPManager.getBundle().getString("FMT_ERR_CannotLoadClass"), // NOI18N
                new Object [] { className });
                
            manager.annotate(ex, ErrorManager.WARNING, null, message, null, null);
            manager.notify(ex);
            
            return;
        }

        String fileName = formatName(className);
        try {
            if (folder.getFileObject(fileName+".instance") == null) { // NOI18N
                FileObject fo = folder.createData(fileName, "instance"); // NOI18N
                DataObject dobj = DataObject.find(fo);
                if (dobj != null) {
                    // enforce creation of node so that it is displayed
                    dobj.getNodeDelegate();
                }
            }
        }
        catch (java.io.IOException e) {
            if (System.getProperty("netbeans.debug.exceptions") != null)
                e.printStackTrace();
            /* ignore */
        }
        catch (Throwable t) {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath)t;
            else
                t.printStackTrace();
        }
    }

    private static String formatName(String className) {
        return className.replace('.', '-'); // NOI18N
    }

    /**
     * Opens dialog and lets user select category, where beans should be installed
     */

    private static String selectPaletteCategory() {
        CategorySelector sel = new CategorySelector();
        DialogDescriptor desc = new DialogDescriptor(
            sel,
            CPManager.getBundle().getString("CTL_SelectPalette"), // NOI18N
            true,
            null
            );
        desc.setHelpCtx(new HelpCtx(BeanInstaller.class.getName() + ".selectPaletteCategory")); // NOI18N

        DialogDisplayer.getDefault().createDialog(desc).show();
        if (desc.getValue() == NotifyDescriptor.OK_OPTION) {
            return sel.getSelectedCategory();
        } else {
            return null;
        }
    }

    /**
     * prompts user for the jar file
     * @return filename or null if operation was cancelled.
     */

    private static File selectJarModule() {
        JFileChooser chooser = new JFileChooser();
        final ResourceBundle bundle = CPManager.getBundle();

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return(f.isDirectory() || f.getName().endsWith(JAR_EXT));
            }
            public String getDescription() {
                return bundle.getString("CTL_JarArchivesMask"); // NOI18N
            }
        });

        if (lastDirectory == null)
        {
            try
            {
                File defDir = new File(System.getProperty("user.home"));  // NOI18N
                if (Utilities.isUnix())
                    lastDirectory = defDir;
                else if (Utilities.isWindows())
                {
                    do
                    {
                        defDir = defDir.getParentFile();
                    }
                    while (defDir != null && defDir.getParentFile() != null);
                    if (defDir != null)
                        lastDirectory = defDir;
                }
            }
            catch (Exception ex)
            {
                lastDirectory = null;
            }
        }
        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }

        chooser.setDialogTitle(bundle.getString("CTL_SelectJar"));
        while (chooser.showDialog(
                 org.openide.windows.WindowManager.getDefault().getMainWindow(),
                 bundle.getString("CTL_Select_Approve_Button"))
               == JFileChooser.APPROVE_OPTION)
        {
            File f = chooser.getSelectedFile();
            lastDirectory = chooser.getCurrentDirectory();
            if (f != null && f.isFile() && f.getName().endsWith(JAR_EXT)) {
                return f;
            }

            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                bundle.getString("MSG_noValidFile"), NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    }

    /**
     * @return jar FS for the given name or null if some problems occured
     */
    private static JarFileSystem createJarForFile(File jarFile) {
        try {
            JarFileSystem jar = new GlobalJarFileSystem();
            jar.setJarFile(jarFile);
            return jar;
        }
        catch (PropertyVetoException e) {
            if (System.getProperty("netbeans.debug.exceptions") != null)
                e.printStackTrace();
            return null;
        }
        catch (IOException e) {
            if (System.getProperty("netbeans.debug.exceptions") != null)
                e.printStackTrace();
            return null;
        }
    }

    // ----------

    private static class CategorySelector extends JPanel {
        private JList list;
        private String[] catNames;

        public CategorySelector() {
            Node[] catNodes = PaletteNode.getPaletteNode().getChildren().getNodes(true);

            ArrayList dispList = new ArrayList(catNodes.length);
            ArrayList nameList = new ArrayList(catNodes.length);
            for (int i=0; i < catNodes.length; i++) {
                dispList.add(catNodes[i].getDisplayName());
                nameList.add(catNodes[i].getName());
            }

            String[] catDisplayNames = new String[dispList.size()];
            dispList.toArray(catDisplayNames);
            catNames = new String[nameList.size()];
            nameList.toArray(catNames);

            list = new JList(catDisplayNames);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            setLayout(new BorderLayout(0, 5));
            
            JLabel categoryLabel = new JLabel(CPManager.getBundle().getString("CTL_PaletteCategories"));
            categoryLabel.setDisplayedMnemonic(CPManager.getBundle().getString("CTL_PaletteCategories_Mnemonic").charAt(0));
            categoryLabel.setLabelFor(list);
            list.getAccessibleContext().setAccessibleDescription(CPManager.getBundle().getString("ACSD_CTL_PaletteCategories"));
            getAccessibleContext().setAccessibleDescription(CPManager.getBundle().getString("ACSD_PaletteCategoriesSelector"));
            add(categoryLabel, BorderLayout.NORTH);
            add(new JScrollPane(list), BorderLayout.CENTER);
            setBorder(new EmptyBorder(12, 12, 0, 11));
        }

        public String getSelectedCategory() {
            int i = list.getSelectedIndex();
            return i >= 0 ? catNames[i] : null;
        }

        public Dimension getPreferredSize() {
            Dimension ret = super.getPreferredSize();
            ret.width = Math.max(ret.width, 350);
            ret.height = Math.max(ret.height, 250);
            return ret;
        }
    }

    private static class BeanSelector extends JPanel
    {
        static final long serialVersionUID = -6038414545631774041L;

        private JList list;

        public BeanSelector(Collection beans) {
            list = new JList(beans.toArray());
            list.setCellRenderer(new FileObjectRenderer());

            setBorder(new EmptyBorder(12, 12, 0, 11));
            setLayout(new BorderLayout(0, 2));
            JLabel label = new JLabel(CPManager.getBundle().getString("CTL_SelectBeans")); // NOI18N
            label.setLabelFor(list);
            label.setDisplayedMnemonic(CPManager.getBundle().getString("CTL_SelectBeans_Mnemonic").charAt(0));
            list.getAccessibleContext().setAccessibleDescription(CPManager.getBundle().getString("ACSD_CTL_SelectBeans"));
            getAccessibleContext().setAccessibleDescription(CPManager.getBundle().getString("ACSD_SelectBeansDialog"));
            add(label, BorderLayout.NORTH);
            add(new JScrollPane(list), BorderLayout.CENTER); // NOI18N
        }

        Collection getSelectedBeans() {
            Object[] sel = list.getSelectedValues();
            ArrayList al = new ArrayList(sel.length);
            for (int i = 0; i < sel.length; i++) {
                al.add(sel[i]);
            }
            return al;
        }

        public Dimension getPreferredSize() {
            Dimension ret = super.getPreferredSize();
            ret.width = Math.max(ret.width, 350);
            ret.height = Math.max(ret.height, 250);
            return ret;
        }
    }

    private static class FileObjectRenderer extends JLabel implements ListCellRenderer
    {
        static final long serialVersionUID = 832555965217675765L;

        private static final Border hasFocusBorder =
            new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        /** Creates a new NetbeansListCellRenderer */
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

            FileObject fo =(FileObject) value;

            setText(fo.getName());

            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);
            return this;
        }
    }
}
