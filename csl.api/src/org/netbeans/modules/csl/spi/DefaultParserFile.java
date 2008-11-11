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

package org.netbeans.modules.csl.spi;

import java.io.File;
import org.netbeans.modules.csl.api.ParserFile;
import org.netbeans.modules.csl.api.annotations.CheckForNull;
import org.netbeans.modules.csl.api.annotations.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class DefaultParserFile implements ParserFile {
    private FileObject fileObject;
    private String relative;
    private boolean platform;
    
    public DefaultParserFile(@NonNull FileObject fo, @CheckForNull String relative, boolean platform) {
        this.fileObject = fo;
        this.relative = relative;
        this.platform = platform;
    }
    
//    public DefaultParserFile(String path, String relative) {
//        this.path = path;
//        this.relative = relative;
//    }

    @NonNull
    public FileObject getFileObject() {
//        if (fileObject == null) {
//            assert path != null;
//            fileObject = FileUtil.toFileObject(new File(path));
//        }
        return fileObject;
    }

    public String getRelativePath() {
        return relative;
    }
    
    public String getNameExt() {
        final FileObject fo = getFileObject();
        return fo != null ? fo.getNameExt() : null;
    }
    
    public String getExtension() {
        final FileObject fo = getFileObject();
        return fo != null ? fo.getExt() : null;
    }
    
    @Override
    public String toString() {
        return getNameExt();
    }

    public boolean isPlatform() {
        return platform;
    }

    public File getFile() {
        return FileUtil.toFile(fileObject);
    }
}
