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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.awt.Actions;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.api.fileinfo.NonRecursiveFolder;

import javax.swing.*;
import javax.swing.text.Document;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.openide.filesystems.FileSystem;

/**
 * Utilities for Versioning SPI classes. 
 * 
 * @author Maros Sandor
 */
public class Utils {
    
    /**
     * Request processor for long running tasks.
     */
    private static final RequestProcessor vcsBlockingRequestProcessor = new RequestProcessor("Versioning long tasks", 1);

    /**
     * Keeps the nb masterfilesystem
     */
    private static FileSystem filesystem;

    /**
     * Constructs a VCSContext out of a Lookup, basically taking all Nodes inside. 
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become the root files of this context.
     * 
     * @param lookup a lookup
     * @return VCSContext containing nodes from Lookup
     */ 
    public static VCSContext contextForLookup(Lookup lookup) {
        Lookup.Result<Node> result = lookup.lookup(new Lookup.Template<Node>(Node.class));
        Collection<? extends Node> nodes = result.allInstances();
        return VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]));
    }
        
    public static VCSContext contextForFileObjects(Set<? extends FileObject> files) {
        Set<File> roots = new HashSet<File>(files.size());
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            roots.add(new FlatFolder(FileUtil.toFile(folder).getAbsolutePath()));
        } else {
            for (FileObject fo : files) {
                roots.add(FileUtil.toFile(fo));
            }
        }
        return Accessor.VCSContextAccessor.createContextForFiles(roots, files);
    }
    
    /**
     * Tests for ancestor/child file relationsip.
     * 
     * @param ancestor supposed ancestor of the file
     * @param file a file
     * @return true if ancestor is an ancestor folder of file OR both parameters are equal, false otherwise
     */
    public static boolean isAncestorOrEqual(File ancestor, File file) {
        if (VersioningSupport.isFlat(ancestor)) {
            return ancestor.equals(file) || ancestor.equals(file.getParentFile()) && !file.isDirectory();
        }
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(ancestor)) return true;
        }
        return false;
    }

    /**
     * Creates a menu item from an action.
     * 
     * @param action an action
     * @return JMenuItem
     */
    public static JMenuItem toMenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, false);
        }
        return item;
    }

    public static File getTempFolder() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));   // NOI18N
        for (;;) {
            File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
            if (!dir.exists() && dir.mkdirs()) {
                dir.deleteOnExit();
                return FileUtil.normalizeFile(dir);
            }
        }
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        deleteRecursively(file, Level.WARNING);
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     * @param level log level
     */
    public static void deleteRecursively(File file, Level level) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return;
        try {
            fo.delete();
        } catch (IOException e) {
            Logger.getLogger(Utils.class.getName()).log(level, "", e);
        }
    }

    public static boolean isLocalHistory(VersioningSystem system) {
        return system.getProperty(VersioningSystem.PROP_LOCALHISTORY_VCS) != null;
    }
    
    public static Reader getDocumentReader(final Document doc) {
        final String[] str = new String[1];
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    e.printStackTrace();
                }
            }
        };
        doc.render(run);
        return new StringReader(str[0]);
    }
    
    /**
     * Creates a task that will run in the Versioning RequestProcessor (with has throughput of 1). The runnable may take long
     * to execute (connet through network, etc).
     * 
     * @param runnable Runnable to run
     * @return RequestProcessor.Task created task
     */
    public static RequestProcessor.Task createTask(Runnable runnable) {
        return vcsBlockingRequestProcessor.create(runnable);
    }

    public static String getDisplayName(VersioningSystem system) {
        return (String) system.getProperty(VersioningSystem.PROP_DISPLAY_NAME);
    }

    public static String getMenuLabel(VersioningSystem system) {
        return (String) system.getProperty(VersioningSystem.PROP_MENU_LABEL);
    }
    
    public static JSeparator createJSeparator() {
        JMenu menu = new JMenu();
        menu.addSeparator();
        return (JSeparator)menu.getPopupMenu().getComponent(0);
    }

    static FileSystem getRootFilesystem() {
        if(filesystem == null) {
            try {
                String userDir = System.getProperty("netbeans.user"); // NOI18N
                FileObject fo = FileUtil.toFileObject(new File(userDir));
                filesystem = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                VersioningManager.LOG.log(Level.WARNING, null, ex);
            }
        }
        return filesystem;
    }
}
