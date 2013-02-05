/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Cache of temporary read-only copies of remote CSS style sheets.
 *
 * @author Jan Stola
 */
public class RemoteStyleSheetCache {
    /** The default instance of this class. */
    private static final RemoteStyleSheetCache DEFAULT = new RemoteStyleSheetCache();
    /**
     * The cache itself: a mapping between {@code StyleSheetBody}
     * and the corresponding temporary read-only copy of the stylesheet.
     */
    private final Map<StyleSheetBody, FileObject> cache = new HashMap<StyleSheetBody, FileObject>();

    /**
     * Returns the default instance of this class.
     * 
     * @return the default instance of this class.
     */
    public static RemoteStyleSheetCache getDefault() {
        return DEFAULT;
    }

    /**
     * Creates a new {@code RemoteStyleSheetCache}.
     */
    private RemoteStyleSheetCache() {
    }

    /**
     * Returns a temporary read-only copy of the stylesheet that corresponds
     * to the given {@code StyleSheetBody}.
     * 
     * @param body identification of the stylesheet.
     * @return temporary read-only copy of the stylesheet that corresponds
     * to the given {@code StyleSheetBody}.
     */
    public FileObject getFileObject(StyleSheetBody body) {
        synchronized (this) {
            FileObject fob = cache.get(body);
            if (fob == null) {
                String styleSheetText = body.getText();
                PrintWriter pw = null;
                try {
                    File file = File.createTempFile("download", ".css"); // NOI18N
                    file.deleteOnExit();
                    pw = new PrintWriter(file);
                    pw.println(styleSheetText);
                    pw.flush();
                    file.setReadOnly();
                    fob = FileUtil.toFileObject(file);
                    cache.put(body, fob);
                } catch (IOException ioex) {
                    Logger.getLogger(RemoteStyleSheetCache.class.getName()).log(Level.INFO, null, ioex);
                } finally {
                    if (pw != null) {
                        pw.close();
                    }
                }
            }
            return fob;
        }
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        synchronized (this) {
            cache.clear();
        }
    }
    
}
