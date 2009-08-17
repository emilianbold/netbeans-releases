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
package org.netbeans.modules.web.jsf.editor.tld;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * Per web-module instance
 *
 * @author marekfukala
 */
public class TldClassPathSupport implements PropertyChangeListener {

    private final ClassPath cp;
    //uri -> library map
    private final Map<String, TldLibrary> LIBRARIES = new HashMap<String, TldLibrary>();
    private boolean cache_valid = false;

    public TldClassPathSupport(ClassPath cp) {
        this.cp = cp;
        cp.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        cache_valid = false;
    }

    public synchronized Map<String, TldLibrary> getLibraries() {
        if (!cache_valid) {
            LIBRARIES.clear();
            for (FileObject cpRoot : cp.getRoots()) {
                Collection<TldLibrary> libs = TldLibraryGlobalCache.getDefault().getLibraries(cpRoot);
                for(TldLibrary lib : libs) {
                    LIBRARIES.put(lib.getURI(), lib);
                }
            }

            //add default libraries
            for(TldLibrary lib : TldLibraryGlobalCache.getDefault().getDefaultLibraries()) {
                LIBRARIES.put(lib.getURI(), lib);
            }

            cache_valid = true;
            dumpLibs();
        }
        return LIBRARIES;
    }

    public ClassPath getClassPath() {
        return cp;
    }

    private void dumpLibs() {
        System.out.println("Available TLD libraries:"); //NOI18N
        for (TldLibrary l : getLibraries().values()) {
            System.out.println(l.getDisplayName() + " (" + l.getURI() + "; "+ (l.getDefinitionFile() != null ? l.getDefinitionFile().getPath() : "default library") +")");
        }

    }
}
