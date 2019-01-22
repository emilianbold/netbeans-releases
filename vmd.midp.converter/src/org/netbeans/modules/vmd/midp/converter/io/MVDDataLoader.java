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
 */package org.netbeans.modules.vmd.midp.converter.io;

import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.modules.mobility.editor.pub.J2MEDataLoader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;

/**
 * 
 */
public final class MVDDataLoader extends J2MEDataLoader {

    static final long serialVersionUID = -1;

    static final String EXT_JAVA = "java"; // NOI18N
    static final String EXT_DESIGN = "mvd"; // NOI18N

    public MVDDataLoader () {
        super ("org.netbeans.modules.vmd.midp.converter.io.MVDDataObject"); // NOI18N
    }

    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (MVDDataLoader.class, "DISP_DefaultName"); // NOI18N
    }

    @Override
    protected FileObject findPrimaryFile (FileObject fileObject) {
        String ext = fileObject.getExt ();
        if (EXT_DESIGN.equals (ext))
            return FileUtil.findBrother (fileObject, EXT_JAVA);
        if (EXT_JAVA.equals (ext))
            if (FileUtil.findBrother (fileObject, EXT_DESIGN) != null)
                return fileObject;
        return null;
    }

    @Override
    protected MultiDataObject createMultiObject (FileObject primaryFile) throws java.io.IOException {
        return new MVDDataObject (primaryFile, FileUtil.findBrother (primaryFile, EXT_DESIGN), this);
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject dataObject, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry (dataObject, primaryFile);
    }

    @Override
    public MultiDataObject.Entry createSecondaryEntry (MultiDataObject dataObject, FileObject secondaryFile) {
        if (EXT_DESIGN.equals (secondaryFile.getExt ()))
            return new FileEntry (dataObject, secondaryFile);
        else
            return null;
    }

}
