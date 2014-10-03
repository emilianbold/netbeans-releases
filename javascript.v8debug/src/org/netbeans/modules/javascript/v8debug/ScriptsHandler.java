/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.lib.v8debug.V8Script;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Entlicher
 */
public class ScriptsHandler {
    
    private static final Logger LOG = Logger.getLogger(ScriptsHandler.class.getName());

    private final Map<Long, V8Script> scriptsById = new HashMap<>();

    private final boolean doPathTranslation;
    @NullAllowed
    private final String localPathPrefix;
    private final char localPathSeparator;
    @NullAllowed
    private final FileObject localRoot;
    @NullAllowed
    private final String serverPathPrefix;
    private final char serverPathSeparator;
    
    ScriptsHandler(@NullAllowed String localPath,
                   @NullAllowed String serverPath) {
        if (localPath != null && serverPath != null) {
            this.doPathTranslation = true;
            this.localPathPrefix = stripSeparator(localPath);
            this.localPathSeparator = findSeparator(localPath);
            this.serverPathPrefix = stripSeparator(serverPath);
            this.serverPathSeparator = findSeparator(serverPath);
            this.localRoot = FileUtil.toFileObject(new File(localPath));
        } else {
            this.doPathTranslation = false;
            this.localPathPrefix = this.serverPathPrefix = null;
            this.localPathSeparator = this.serverPathSeparator = 0;
            this.localRoot = null;
        }
        LOG.log(Level.FINE,
                "ScriptsHandler: doPathTranslation = {0}, localPathPrefix = {1}, separator = {2}, serverPathPrefix = {3}, separator = {4}",
                new Object[]{doPathTranslation, localPathPrefix, localPathSeparator, serverPathPrefix, serverPathSeparator});
        
    }

    void add(V8Script script) {
        synchronized (scriptsById) {
            scriptsById.put(script.getId(), script);
        }
    }
    
    void add(V8Script[] scripts) {
        synchronized (scriptsById) {
            for (V8Script script : scripts) {
                scriptsById.put(script.getId(), script);
            }
        }
    }
    
    public V8Script getScript(long id) {
        synchronized (scriptsById) {
            return scriptsById.get(id);
        }
    }
    
    public Collection<V8Script> getScripts() {
        synchronized (scriptsById) {
            return new ArrayList<>(scriptsById.values());
        }
    }
    
    public boolean containsLocalFile(FileObject fo) {
        if (localRoot == null) {
            return true;
        }
        if (fo == null) {
            return false;
        }
        return FileUtil.isParentOf(localRoot, fo);
    }
    
    public String getLocalPath(@NonNull String serverPath) throws OutOfScope {
        if (!doPathTranslation) {
            return serverPath;
        } else {
            return translate(serverPath, serverPathPrefix, serverPathSeparator, localPathPrefix, localPathSeparator);
        }
    }
    
    public String getServerPath(@NonNull String localPath) throws OutOfScope {
        if (!doPathTranslation) {
            return localPath;
        } else {
            return translate(localPath, localPathPrefix, localPathSeparator, serverPathPrefix, serverPathSeparator);
        }
    }
    
    private static String translate(String path, String pathPrefix, char pathSeparator, String otherPathPrefix, char otherPathSeparator) throws OutOfScope {
        if (!path.startsWith(pathPrefix)) {
            throw new OutOfScope(path, pathPrefix);
        }
        int l = pathPrefix.length();
        if (!isRootPath(pathPrefix)) { // When the prefix is the root, do not do further checks.
            if (path.length() > l && !isSeparator(path.charAt(l))) {
                throw new OutOfScope(path, pathPrefix);
            }
        }
        while (path.length() > l && isSeparator(path.charAt(l))) {
            l++;
        }
        String otherPath = path.substring(l);
        if (pathSeparator != otherPathSeparator) {
            otherPath = otherPath.replace(pathSeparator, otherPathSeparator);
        }
        if (otherPath.isEmpty()) {
            return otherPathPrefix;
        } else {
            if (isRootPath(otherPathPrefix)) { // Do not append further slashes to the root
                return otherPathPrefix + otherPath;
            } else {
                return otherPathPrefix + otherPathSeparator + otherPath;
            }
        }
    }
    
    private static char findSeparator(String path) {
        if (path.indexOf('/') >= 0) {
            return '/';
        }
        if (path.indexOf('\\') >= 0) {
            return '\\';
        }
        return '/';
    }

    private static boolean isSeparator(char c) {
        return c == '/' || c == '\\';
    }
    
    private static String stripSeparator(String path) {
        if (isRootPath(path)) { // Do not remove slashes the root
            return path;
        }
        while (path.length() > 1 && (path.endsWith("/") || path.endsWith("\\"))) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    private static boolean isRootPath(String path) {
        if ("/".equals(path)) {
            return true;
        }
        if (path.length() == 4 && path.endsWith(":\\\\")) { // "C:\\"
            return true;
        }
        return false;
    }

    public static final class OutOfScope extends Exception {
        
        private OutOfScope(String path, String scope) {
            super(path);
        }
    }

}
