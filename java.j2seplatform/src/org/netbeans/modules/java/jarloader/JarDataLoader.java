/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.jarloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.RenameAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Recognizes JAR files and shows their contents.
 * @author Jesse Glick
 */
public final class JarDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 1L;

    public JarDataLoader() {
        super("org.netbeans.modules.java.jarloader.JarDataObject"); // NOI18N
    }

    protected @Override String defaultDisplayName() {
        return NbBundle.getMessage(JarDataLoader.class, "LBL_loaderName");
    }

    /** @see FileUtil */
    private static byte[] ZIP_HEADER_1 = {0x50, 0x4b, 0x03, 0x04};
    private static byte[] ZIP_HEADER_2 = {0x50, 0x4b, 0x05, 0x06};
    protected @Override FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder() || fo.isVirtual()) {
            return null;
        }
        // Similar to FileUtil.isArchiveFile, but does not fall back to checking for a dot in the name.
        try {
            InputStream in = fo.getInputStream();
            try {
                byte[] buffer = new byte[4];
                int len = in.read(buffer, 0, 4);
                if (len == 4 && Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer)) {
                    return fo;
                }
            } finally {
                in.close();
            }
        } catch (IOException ioe) {
            Logger.getLogger(JarDataLoader.class.getName()).log(Level.INFO, "Scanning " + fo, ioe);
        }
        return null;
    }

    protected @Override SystemAction[] defaultActions() {
        return new SystemAction[] {
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(CutAction.class),
            SystemAction.get(CopyAction.class),
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            SystemAction.get(RenameAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            SystemAction.get(PropertiesAction.class),
        };
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new JarDataObject(primaryFile, this);
    }

}
