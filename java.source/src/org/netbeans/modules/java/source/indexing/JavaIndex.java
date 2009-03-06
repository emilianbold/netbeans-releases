/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class JavaIndex {

    public static final String NAME = "java";
    public static final int    VERSION = 12;

    public static File getIndex(Context c) {
        return FileUtil.toFile(c.getIndexFolder());
    }

    public static File getIndex(URL url) throws IOException {
        return new File(FileUtil.toFile(CacheFolder.getDataFolder(url)), NAME + "/" + VERSION);
    }

    public static File getClassFolder(Context c) {
        return getClassFolder(c, false);
    }
    
    public static File getClassFolder(Context c, boolean onlyIfExists) {
        return processCandidate(new File(getIndex(c), "classes"), onlyIfExists);
    }

    public static File getClassFolder(File root) throws IOException {
        return getClassFolder(root.toURI().toURL()); //XXX
    }

    public static File getClassFolder(URL url) throws IOException {
        return getClassFolder(url, false);
    }

    public static File getClassFolder(URL url, boolean onlyIfExists) throws IOException {
        return processCandidate(new File(getIndex(url), "classes"), onlyIfExists);
    }

    private static File processCandidate(File result, boolean onlyIfExists) {
        if (onlyIfExists) {
            if (!result.exists()) {
                return null;
            } else {
                return result;
            }
        }
        result.mkdirs();
        return result;
    }
}
