/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.libraries.spi;

import org.netbeans.modules.javascript.libraries.api.*;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class JSLibrarySharabilityQueryImpl implements SharabilityQueryImplementation {

    private final SharabilityQueryImplementation baseImpl;
    private final Collection<String> excludedPaths;
    private final JavaScriptLibrarySupport librarySupport;
    
    public JSLibrarySharabilityQueryImpl(JavaScriptLibrarySupport support, SharabilityQueryImplementation baseImpl) {
        assert !(baseImpl instanceof JSLibrarySharabilityQueryImpl);
        
        this.librarySupport = support;
        this.baseImpl = baseImpl;
        excludedPaths = new LinkedHashSet<String>();
    }
    
    public void addUnsharablePath(String relativePath) {
        excludedPaths.add(relativePath);
    }
    public void removeUnsharablePath(String relativePath) {
        excludedPaths.remove(relativePath);
    }

    public void reset() {
        excludedPaths.clear();
    }
    
    public int getSharability(File file) {
        int result = (baseImpl != null) ? baseImpl.getSharability(file) : SharabilityQuery.SHARABLE;
        if (excludedPaths.size() == 0 || result == SharabilityQuery.NOT_SHARABLE) {
            return result;
        } else {
            String filePath = file.getAbsolutePath();
            String webRoot = librarySupport.getJavaScriptLibrarySourcePath();
            
            for (String path : excludedPaths) {
                File excludedFile = new File(new File(webRoot), path);
                String excludedAbsolutePath = excludedFile.getAbsolutePath();
                
                if (contains(filePath, excludedAbsolutePath, false)) {
                    return SharabilityQuery.NOT_SHARABLE;
                } else if (contains(filePath, excludedAbsolutePath, true)) {
                    return SharabilityQuery.MIXED;
                }
            }
            
            return result;
        }
    }
    
    /**
     * XXX copied from org.netbeans.spi.project.support.ant.SharabilityQueryImpl
     * 
     * Check whether a file path matches something in the supplied list.
     * @param a file path to test
     * @param item a reference file path
     * @param reverse if true, check if the file is an ancestor of some item; if false,
     *                check if some item is an ancestor of the file
     * @return true if the file matches some item
     */
    private static boolean contains(String path, String item, boolean reverse) {        
        if (path.equals(item)) {
            return true;
        } else {
            if (reverse ? item.startsWith(path + File.separator) : path.startsWith(item + File.separator)) {
                return true;
            }
        }
        return false;
    }
    
}
