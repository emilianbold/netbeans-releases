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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 *
 * @author Alexander Simon
 */
public class SourceRootContainer {
    private final Map<CharSequence,Integer> projectRoots = new ConcurrentHashMap<CharSequence,Integer>();
    
    public SourceRootContainer() {
    }
    
    public boolean isMySource(String includePath){
        if (projectRoots.containsKey(DefaultCache.getManager().getString(includePath))){
            return true;
        }
        while (true){
            int i = includePath.lastIndexOf('\\');
            if (i <= 0) {
                i = includePath.lastIndexOf('/');
            }
            if (i <= 0) {
                return false;
            }
            includePath = includePath.substring(0,i);
            Integer val = projectRoots.get(DefaultCache.getManager().getString(includePath));
            if (val != null) {
                if (val > Integer.MAX_VALUE/4) {
                    return true;
                }
            }
        }
    }
    
    public void fixFolder(String path){
        if (path != null) {
            projectRoots.put(FilePathCache.getManager().getString(path), Integer.MAX_VALUE / 2);
        }
    }
    
    public void addSources(List<NativeFileItem> items){
        for( NativeFileItem nativeFileItem : items ) {
            addFile(nativeFileItem.getFile());
        }
    }
    
    private void addFile(File file){
        File parentFile = CndFileUtils.normalizeFile(file).getParentFile();
        String path = parentFile.getAbsolutePath();
        addPath(path);
        String canonicalPath;
        try {
            canonicalPath = parentFile.getCanonicalPath();
            if (!path.equals(canonicalPath)) {
                addPath(canonicalPath);
            }
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
    }
    
    private void addPath(final String path) {
        CharSequence added = FilePathCache.getManager().getString(path);
        Integer integer = projectRoots.get(added);
        if (integer == null) {
            projectRoots.put(added, 1);
        } else {
            projectRoots.put(added, integer + 1);
        }
    }
    
//    public void removeSources(List<NativeFileItem> items){
//        for( NativeFileItem nativeFileItem : items ) {
//            removeFile(nativeFileItem.getFile());
//        }
//    }
//
//    private void removeFile(File file){
//        String path = FileUtil.normalizeFile(file).getParent();
//        Integer integer = projectRoots.get(path);
//        if (integer != null) {
//            if (integer.intValue() > 1) {
//                projectRoots.put(path, integer - 1);
//            } else {
//                projectRoots.remove(path);
//            }
//        }
//    }
}
