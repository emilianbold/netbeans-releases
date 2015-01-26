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
package org.netbeans.modules.cnd.codemodel.bridge.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import static org.netbeans.modules.cnd.api.project.NativeFileItem.Language.C;
import static org.netbeans.modules.cnd.api.project.NativeFileItem.Language.CPP;
import static org.netbeans.modules.cnd.api.project.NativeFileItem.Language.FORTRAN;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class NativeProjectCompilationDataBase implements CMCompilationDataBase {

    private final NativeProject nativeProject;
    private final Map<URI, Entry> entries;

    public NativeProjectCompilationDataBase(NativeProject nativeProject) {
        this.nativeProject = nativeProject;
        Map<URI, Entry> map = new HashMap<>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case FORTRAN:
                        EntryImpl entry = new EntryImpl(item);
                        map.put(entry.getFile(), entry);
                        break;
                    default:
                        break;
                }
            }
        }
        entries = Collections.unmodifiableMap(map);
    }

    @Override
    public Collection<Entry> getEntries() {
        return entries.values();
    }

    @Override
    public Entry getFileEntry(URI file) {
        return entries.get(file);
    }


    private class EntryImpl implements CMCompilationDataBase.Entry {

        private final NativeFileItem nativeFileItem;

        public EntryImpl(NativeFileItem nativeFileItem) {
            this.nativeFileItem = nativeFileItem;
        }

        @Override
        public URI getFile() {
            return nativeFileItem.getFileObject().toURI();
        }
      
        @Override
        public String[] getCompileArgs() {
            List<String> args = new ArrayList<>();
            for (FSPath fSPath : nativeFileItem.getSystemIncludePaths()) {
                args.add("-isystem");
                args.add(fSPath.getPath());
            }
            //TODO: fix macros
            for (String def : nativeFileItem.getSystemMacroDefinitions()) {
                args.add("-D" + def);
            }
            for (FSPath fSPath : nativeFileItem.getUserIncludePaths()) {
                FileObject fileObject = fSPath.getFileObject();
                if (fileObject != null && fileObject.isData()) {
                    args.add("-include");
                    args.add(fSPath.getPath());
                } else {
                    args.add("-I");
                    args.add(fSPath.getPath());
                }
            }
            for (String def : nativeFileItem.getUserMacroDefinitions()) {
                args.add("-D" + def);
            }            
            return args.toArray(new String[0]);
        }
        
    }
}
