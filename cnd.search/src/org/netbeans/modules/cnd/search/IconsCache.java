/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.search;

import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * NOT thread safe
 *
 */
public final class IconsCache {

    private static final IconsCache cache = new IconsCache();
    private final static int cacheLifetime = 1000 * 60 * 1; // 1 min
    private final Task cleanUpTask;
    private final HashMap<String, Image> map = new HashMap<String, Image>();
    private final FileObject root = FileUtil.createMemoryFileSystem().getRoot();

    private IconsCache() {
        cleanUpTask = RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        cache.map.clear();
                    }
                });
            }
        }, cacheLifetime);
    }

    public static Image getIcon(String name, int type) {
        cache.cleanUpTask.schedule(cacheLifetime);

        if (name.indexOf('.') < 0) {
            name = "noext"; // NOI18N
        }

        Image icon = cache.map.get(name + type);
        if (icon == null) {
            FileObject fo = createMemoryFile(name);
            try {
                DataObject dob = DataObject.find(fo);
                icon = dob.getNodeDelegate().getIcon(type);
                cache.map.put(name + type, icon);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (fo != null) {
                    try {
                        fo.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return icon;
    }

    private static FileObject createMemoryFile(String name) {
        FileObject fo = null;
        try {
            fo = FileUtil.createData(cache.root, name);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return fo;
    }
}
