/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
