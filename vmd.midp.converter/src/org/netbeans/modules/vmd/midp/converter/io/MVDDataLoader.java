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
 *
 */
package org.netbeans.modules.vmd.midp.converter.io;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.pub.J2MEDataLoader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;

/**
 * @author David Kaspar
 */
public final class MVDDataLoader extends J2MEDataLoader {

    static final long serialVersionUID = -1;

    static final String EXT_JAVA = "java"; // NOI18N
    static final String EXT_DESIGN = "mvd"; // NOI18N

    public MVDDataLoader () {
        super ("org.netbeans.modules.vmd.midp.converter.io.MVDDataObject"); // NOI18N
    }

    protected String defaultDisplayName () {
        return NbBundle.getMessage (MVDDataLoader.class, "DISP_DefaultName"); // NOI18N
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

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws java.io.IOException {
        return new MVDDataObject (primaryFile, FileUtil.findBrother (primaryFile, EXT_DESIGN), this);
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
