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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.openide.filesystems.FileObject;

/**
 * Per web-module instance
 *
 * @author marekfukala
 */
public class TldLibrariesCache {

    //uri -> library map
    private final Map<String, TldLibrary> LIBRARIES = new HashMap<String, TldLibrary>();
    private static Collection<TldLibrary> DEFAULT_LIBRARIES;
    private JsfSupport support;

    public TldLibrariesCache(JsfSupport support) {
        this.support = support;

        //add default libraries
        for (TldLibrary lib : getDefaultLibraries()) {
            LIBRARIES.put(lib.getURI(), lib);
        }
    }

    public synchronized TldLibrary getLibrary(String namespace) throws TldLibraryException {
        TldLibrary lib = LIBRARIES.get(namespace);
        if(lib == null) {
            FileObject file = support.getBinariesIndex().getTldFile(namespace);
            if(file != null) {
                lib = TldLibrary.create(file);
                LIBRARIES.put(namespace, lib);
            }
        }
        return lib;
        
    }

     public static synchronized Collection<TldLibrary> getDefaultLibraries() {
        if (DEFAULT_LIBRARIES == null) {
            DEFAULT_LIBRARIES = new ArrayList<TldLibrary>();
            try {
                DEFAULT_LIBRARIES.add(
                        TldLibrary.create(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/netbeans/modules/web/jsf/editor/resources/composite.tld"))); //NOI18N
                DEFAULT_LIBRARIES.add(
                        TldLibrary.create(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/netbeans/modules/web/jsf/editor/resources/ui.tld"))); //NOI18N
            } catch (TldLibraryException ex) {
                //warn user, this should not happen
                Logger.global.warning(ex.getMessage());
            }
        }
        return DEFAULT_LIBRARIES;
    }

    private void dumpLibs() {
        System.out.println("Available TLD libraries:"); //NOI18N
        for (TldLibrary l : LIBRARIES.values()) {
            System.out.println(l.getDisplayName() + " (" + l.getURI() + "; " + (l.getDefinitionFile() != null ? l.getDefinitionFile().getPath() : "default library") + ")");
        }

    }
}
