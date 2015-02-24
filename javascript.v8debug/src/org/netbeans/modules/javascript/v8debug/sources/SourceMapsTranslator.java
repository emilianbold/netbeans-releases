/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.sources;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.common.sourcemap.Mapping;
import org.netbeans.modules.web.common.sourcemap.SourceMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Entlicher
 */
public class SourceMapsTranslator {
    
    private static final Logger LOG = Logger.getLogger(SourceMapsTranslator.class.getName());
    
    private static final String SOURCE_MAPPING_URL = "//# sourceMappingURL=";   // NOI18N
    private static final boolean USE_SOURCE_MAPS =
            Boolean.parseBoolean(System.getProperty("javascript.debugger.useSourceMaps", "true"));
    
    private static final Map<FileObject, SourceMapsTranslator> translatedFiles = new WeakHashMap<>();
    
    private final FileObject parentFolder;
    private final SourceMap sourceMap;
    
    private SourceMapsTranslator(FileObject parentFolder, SourceMap sourceMap) {
        this.parentFolder = parentFolder;
        this.sourceMap = sourceMap;
    }
    
    public static synchronized SourceMapsTranslator get(FileObject source) {
        if (!USE_SOURCE_MAPS) {
            return null;
        }
        SourceMapsTranslator smtr = translatedFiles.get(source);
        if (smtr != null) {
            return smtr;
        }
        String lastLine = null;
        try {
            for (String line : source.asLines()) {
                lastLine = line;
            }
        } catch (IOException ioex) {
            return null;
        }
        if (lastLine != null && lastLine.startsWith(SOURCE_MAPPING_URL)) {
            String sourceMapFileName = lastLine.substring(SOURCE_MAPPING_URL.length()).trim();
            File sourceMapFile = new File(sourceMapFileName);
            FileObject fo;
            if (sourceMapFile.isAbsolute()) {
                fo = FileUtil.toFileObject(FileUtil.normalizeFile(sourceMapFile));
            } else {
                fo = source.getParent().getFileObject(sourceMapFileName);
            }
            if (fo == null) {
                return null;
            }
            try {
                return new SourceMapsTranslator(source.getParent(), SourceMap.parse(fo.asText()));
            } catch (IOException | IllegalArgumentException ex) {
                LOG.log(Level.INFO, "Could not read source map "+fo, ex);
                return null;
            }
        } else {
            return null;
        }
    }

    public FileObject getTranslatedFile(int line) {
        Mapping mapping = sourceMap.findMapping(line);
        if (mapping == null) {
            return null;
        }
        int sourceIndex = mapping.getSourceIndex();
        String sourcePath = sourceMap.getSourcePath(sourceIndex);
        File sourceFile = new File(sourcePath);
        FileObject fo;
        if (sourceFile.isAbsolute()) {
            fo = FileUtil.toFileObject(FileUtil.normalizeFile(sourceFile));
        } else {
            fo = parentFolder.getFileObject(sourcePath);
        }
        return fo;
    }
    
    public int getTranslatedLine(int line) {
        Mapping mapping = sourceMap.findMapping(line);
        if (mapping == null) {
            return line;
        } else {
            return mapping.getOriginalLine();
        }
    }
    
}
