/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.palette;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Pavel Benes
 */
public final class ImportPaletteFolderAction extends SystemAction {
    private static final String ACTION_ID = "svg_palette_import"; //NOI18N
    
    private final String m_name;
    
    public ImportPaletteFolderAction() {
        m_name = AbstractSVGAction.getMessage(AbstractSVGAction.LBL_ID_PREFIX + ACTION_ID);
        String hint  = AbstractSVGAction.getMessage(AbstractSVGAction.HINT_ID_PREFIX + ACTION_ID);
        
        putValue(Action.SHORT_DESCRIPTION, hint);
    }

    public String getName() {
        return m_name;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public void actionPerformed(ActionEvent e) {
        ImportPaletteFolderPanel panel = new ImportPaletteFolderPanel();
        DialogDescriptor         dd    = new DialogDescriptor(panel, NbBundle.getMessage(SVGPaletteFactory.class, "TITLE_ImportPaletteFolder"));

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);

        if (dd.getValue() == DialogDescriptor.OK_OPTION){
            final File folderFile = panel.getFolder();
            if ( folderFile.exists() && folderFile.isDirectory() ) {
                SceneManager.log(Level.INFO, "Importing palette folder " + folderFile.getPath()); //NOI18N
                String categoryName = panel.getDisplayName();

                try {
                    DataFolder paletteRootFolder   = SVGPaletteFactory.getPalette().getRoot().lookup(DataFolder.class);
                    FileObject paletteRoot         = paletteRootFolder.getPrimaryFile();
                    final String     folderName    = convertCategoryToFolderName(paletteRoot, categoryName);
                    final FileObject paletteFolder = paletteRoot.createFolder(folderName);
                    if (!folderName.equals(categoryName)) {
                        paletteFolder.setAttribute( "categoryName", categoryName ); //NOI18N
                    }
                    paletteFolder.setAttribute( "isExpanded", "true"); //NOI18N

                    Thread th = new Thread() {
                        public void run() {
                            for ( File child : folderFile.listFiles()) {
                                if ( child.isFile() && SVGDataObject.isSVGFile(child)) {
                                    SceneManager.log(Level.FINE, "Creating palette item for " + child.getPath()); //NOI18N
                                    OutputStream out  = null;
                                    FileLock     lock = null;
                                    try {
                                        String fileName      = child.getName();
                                        String paletteFoName = fileName.replace('.', '_') + ".svgPaletteItem";

                                        SVGPaletteItemData data = new SVGPaletteItemData( fileName, folderName, child.getCanonicalPath());
                                        SVGPaletteItemData.set( paletteFolder.getPath() + "/" + paletteFoName, data);

                                        FileObject fo = paletteFolder.createData( paletteFoName);

                                        lock = fo.lock();
                                        out  = fo.getOutputStream(lock);
                                        data.serialize(out);
                                        SceneManager.log(Level.FINE, "Palette item created."); //NOI18N
                                    } catch( Exception ex) {
                                        SceneManager.error("Palette item creation failed.", ex); //NOI18N
                                    } finally {
                                        if (out != null) {
                                            try {
                                                out.close();
                                            } catch (IOException ex) {
                                                SceneManager.error("Couldn not close stream.", ex); //NOI18N
                                            }
                                        }
                                        if ( lock != null) {
                                            lock.releaseLock();
                                        }
                                    }
                                }
                            }

                            SceneManager.log(Level.INFO, "Palette folder imported."); //NOI18N
                        }
                    };
                    th.setPriority(Thread.MIN_PRIORITY);
                    th.setDaemon(true);
                    th.start();
                } catch( IOException ex) {
                    SceneManager.error("Palette folder creation failed.", ex); //NOI18N
                }
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(ImportPaletteFolderAction.class, "ERROR_NotDirectory", folderFile), //NOI18N
                        NotifyDescriptor.Message.WARNING_MESSAGE));
            }
        }
    }
    
    /** Converts category name to name that can be used as name of folder
     * for the category (restricted even to package name).
     */ 
    protected static String convertCategoryToFolderName( FileObject paletteFO, String name) {
        if (name == null || "".equals(name)) // NOI18N
            return null;

        int i;
        int n = name.length();
        StringBuffer nameBuff = new StringBuffer(n);

        char ch = name.charAt(0);
        if (Character.isJavaIdentifierStart(ch)) {
            nameBuff.append(ch);
            i = 1;
        }
        else {
            nameBuff.append('_');
            i = 0;
        }

        while (i < n) {
            ch = name.charAt(i);
            if (Character.isJavaIdentifierPart(ch))
                nameBuff.append(ch);
            i++;
        }

        String fName = nameBuff.toString();
        if ("_".equals(fName)) // NOI18N
            fName = "Category"; // NOI18N

        // having the base name, make sure it is not used yet
        String freeName = null;
        boolean nameOK = false;

        for (i=0; !nameOK; i++) {
            freeName = i > 0 ? fName + "_" + i : fName; // NOI18N

            if (Utilities.isWindows()) {
                nameOK = true;
                java.util.Enumeration en = paletteFO.getChildren(false);
                while (en.hasMoreElements()) {
                    FileObject fo = (FileObject)en.nextElement();
                    String fn = fo.getName();
                    String fe = fo.getExt();

                    // case-insensitive on Windows
                    if ((fe == null || "".equals(fe)) && fn.equalsIgnoreCase(freeName)) { // NOI18N
                        nameOK = false;
                        break;
                    }
                }
            }
            else nameOK = paletteFO.getFileObject(freeName) == null;
        }
        return freeName;
    }
}
