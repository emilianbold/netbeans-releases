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
package org.netbeans.modules.cnd.spi.codemodel.support;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;

/**
 *
 * @author Vladimir Kvashin
 */
public class SimpleCompilationDataBase implements CMCompilationDataBase {

    private final Map<URI, Entry> entries;
    
    public SimpleCompilationDataBase(Entry entry) {
        this(Arrays.asList(entry));
    }

    private SimpleCompilationDataBase(List<Entry> entries) {
        Map<URI, Entry> map = new HashMap<>();
        for (Entry entry : entries) {
            map.put(entry.getFile(), entry);
        }
        this.entries = Collections.unmodifiableMap(map);
    }

    @Override
    public Collection<Entry> getEntries() {
        return entries.values();
    }

    @Override
    public Entry getFileEntry(URI file) {
        return entries.get(file);
    }

    public static class Builder {

        private final List<Entry> list = new ArrayList<>();
        private String defaultCompileCommand = "";
        private File baseDir = null;

        public void addEntry(String relativePath) {
            addEntry(relativePath, null);
        }

        public void addEntry(String relativePath, String compileCommand) {
            File file = (baseDir == null || new File(relativePath).exists()) ? new File(relativePath) : new File(baseDir, relativePath);
            addEntry(file, compileCommand);
        }

        public void addEntry(File file) {
            list.add(createEntry(file.toURI(), null));
        }

        public void addEntry(File file, String compileCommand) {
            list.add(createEntry(file.toURI(), compileCommand));
        }

        public void addEntry(URI uri) {
            addEntry(uri, null);
        }

        public void addEntry(URI uri, String compileCommand) {
            list.add(createEntry(uri, compileCommand));
        }

        public void setBaseDir(File baseDir) {
            this.baseDir = baseDir;
        }

        public void setDefaultCompileCommand(String defaultCompileCommand) {
            this.defaultCompileCommand = defaultCompileCommand;
        }

        public SimpleCompilationDataBase createDataBase() {
            return new SimpleCompilationDataBase(list);
        }

        private String getDefaultCompileCommand() {
            return defaultCompileCommand;
        }

        private EntryImpl createEntry(URI file, String compileCommand) {
            if (compileCommand == null) {
                compileCommand= getDefaultCompileCommand();
            }
            return new EntryImpl(file, compileCommand == null ? new String[0] : compileCommand.split(" "));
        }
    }

    private static class EntryImpl implements CMCompilationDataBase.Entry {

        private final URI file;
        private final String[] compileArgs;
        

        private EntryImpl(URI file, String[] compileArgs) {
            this.file = file;
            this.compileArgs = compileArgs;
        }

        @Override
        public URI getFile() {
            return file;
        }


        @Override
        public String[] getCompileArgs() {
            return compileArgs;
        }

        @Override
        public String toString() {
            return "file=" + file + "\n compileArgs=" + Arrays.toString(compileArgs); // NOI18N
        }
    }
}
