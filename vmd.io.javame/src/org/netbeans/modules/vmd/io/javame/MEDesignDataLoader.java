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
package org.netbeans.modules.vmd.io.javame;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.pub.J2MEDataLoader;

/**
 * @author David Kaspar
 */
public final class MEDesignDataLoader extends J2MEDataLoader {

    static final long serialVersionUID = 7259154257404524113L;

    static final String EXT_JAVA = "java"; // NOI18N
    static final String EXT_DESIGN = "vmd"; // NOI18N

    public MEDesignDataLoader () {
        super ("org.netbeans.modules.vmd.io.javame.MEDesignDataObject"); // NOI18N
    }

    protected SystemAction[] defaultActions () {
        return new SystemAction[]{
                SystemAction.get (OpenAction.class),
                SystemAction.get (EditAction.class),
                SystemAction.get (SaveAction.class),
                SystemAction.get (FileSystemAction.class),
                null,
                SystemAction.get (ToolsAction.class),
                SystemAction.get (PropertiesAction.class)
        };
    }

    protected String defaultDisplayName () {
        return NbBundle.getMessage (MEDesignDataLoader.class, "DISP_DefaultName"); // NOI18N
    }

    protected FileObject findPrimaryFile (FileObject fileObject) {
        String ext = fileObject.getExt ();
        if (EXT_DESIGN.equals (ext))
            return FileUtil.findBrother (fileObject, EXT_JAVA);
        if (EXT_JAVA.equals (ext))
            if (FileUtil.findBrother (fileObject, EXT_DESIGN) != null)
                return fileObject;
        return null;
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, java.io.IOException {
        return new MEDesignDataObject (primaryFile, FileUtil.findBrother (primaryFile, EXT_DESIGN), this);
    }

    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject dataObject, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry (dataObject, primaryFile);
    }

    public MultiDataObject.Entry createSecondaryEntry (MultiDataObject dataObject, FileObject secondaryFile) {
        if (EXT_DESIGN.equals (secondaryFile.getExt ()))
            return new FileEntry (dataObject, secondaryFile);
        else
            return null;
    }

}
