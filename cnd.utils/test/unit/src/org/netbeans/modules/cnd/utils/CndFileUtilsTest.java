/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Test;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author vv159170
 */
public class CndFileUtilsTest {
    @Test
    public void testLocalUrlToFileObject() throws IOException {
        File temp = File.createTempFile("urlToFileObject", ".txt");
        Assert.assertNotNull(temp);
        // on windows it has ~ to have short names
        temp = FileUtil.normalizeFile(temp);
        temp.deleteOnExit();
        String fileExternalForm = temp.toURI().toURL().toExternalForm();
        String absPath = temp.getAbsolutePath();
        FileObject fo = FileUtil.toFileObject(temp);
        Assert.assertNotNull(fo);
        String foExternalForm = fo.getURL().toExternalForm();
        CharSequence url1 = CndFileUtils.fileObjectToUrl(fo);
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(url1));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(fileExternalForm));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(foExternalForm));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(absPath));
        // we can not concatenate abs path and protocol as is, because
        // if abs path contains space => it's invalid URI symbol
        String escapedPath = absPath.replaceAll(" ", "%20");
        if (!escapedPath.startsWith("/")) {
            escapedPath = "/" + escapedPath;
        }
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject("file:" + escapedPath));
        Assert.assertNull(CndFileUtils.urlToFileObject("file:/" + escapedPath));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject("file://" + escapedPath));
        temp.delete();
    }
}
