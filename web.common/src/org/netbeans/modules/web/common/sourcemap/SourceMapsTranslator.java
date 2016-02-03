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

package org.netbeans.modules.web.common.sourcemap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Translator of code locations, based on source maps.
 * An instance of this class caches all registered source maps translations in
 * both ways. Do not hold a strong reference to an instance of this class after
 * it's translations are not needed any more.
 * 
 * @author Antoine Vandecreme, Martin Entlicher
 */
public final class SourceMapsTranslator {
    
    private static final Logger LOG = Logger.getLogger(SourceMapsTranslator.class.getName());
    
    private static final String SOURCE_MAPPING_URL = "//# sourceMappingURL=";   // NOI18N
    
    private static final DirectMapping NO_MAPPING = new DirectMapping();
    private final Map<FileObject, DirectMapping> directMappings = new HashMap<>();
    private final Map<FileObject, InverseMapping> inverseMappings = new HashMap<>();
    
    public SourceMapsTranslator() {
    }
    
    public boolean registerTranslation(FileObject source, String sourceMapFileName) {
        DirectMapping dm = getMapping(source, sourceMapFileName);
        return dm != NO_MAPPING;
    }
    
    private DirectMapping getMapping(FileObject source, String sourceMapFileName) {
        DirectMapping dm;
        synchronized (directMappings) {
            dm = directMappings.get(source);
            if (dm != null) {
                if (sourceMapFileName == null) {
                    // return the cached one
                    return dm;
                } else {
                    // load the source map
                    dm = null;
                }
            }
            if (sourceMapFileName == null) {
                String lastLine = null;
                try {
                    for (String line : source.asLines()) {
                        lastLine = line;
                    }
                    if (lastLine != null && lastLine.startsWith(SOURCE_MAPPING_URL)) {
                        sourceMapFileName = lastLine.substring(SOURCE_MAPPING_URL.length()).trim();
                    } else {
                        dm = NO_MAPPING;
                    }
                } catch (IOException ioex) {
                    dm = NO_MAPPING;
                }
            }
            if (dm == null) {
                File sourceMapFile = new File(sourceMapFileName);
                FileObject fo;
                if (sourceMapFile.isAbsolute()) {
                    fo = FileUtil.toFileObject(FileUtil.normalizeFile(sourceMapFile));
                } else {
                    fo = source.getParent().getFileObject(sourceMapFileName);
                }
                if (fo == null) {
                    dm = NO_MAPPING;
                } else {
                    try {
                        dm = new DirectMapping(source.getParent(), SourceMap.parse(fo.asText()));
                    } catch (IOException | IllegalArgumentException ex) {
                        LOG.log(Level.INFO, "Could not read source map "+fo, ex);
                        dm = NO_MAPPING;
                    }
                }
            }
            directMappings.put(source, dm);
        }
        if (dm != NO_MAPPING) { // we created new mapping, register the inverse
            synchronized (inverseMappings) {
                for (String src : dm.sourceMap.getSources()) {
                    FileObject fo = dm.parentFolder.getFileObject(src);
                    inverseMappings.put(fo, new InverseMapping(src, source, dm.sourceMap));
                }
            }
        }
        return dm;
    }
    
    /**
     * Translate a location in the compiled file to the location in the source file.
     * Translation is based on a source map retrieved from the compiled file.
     * @param loc location in the compiled file
     * @return corresponding location in the source file, or the original passed
     *         location if the source map is not found, or does not provide the translation.
     */
    public Location getSourceLocation(Location loc) {
        return getSourceLocation(loc, null);
    }
    
    /**
     * Translate a location in the compiled file to the location in the source file.
     * Translation is based on the provided source map.
     * @param loc location in the compiled file
     * @param sourceMapFileName file name of the source map file
     * @return corresponding location in the source file, or the original passed
     *         location if the source map does not provide the translation.
     */
    public Location getSourceLocation(Location loc, String sourceMapFileName) {
        DirectMapping dm = getMapping(loc.getFile(), sourceMapFileName);
        Location mloc = dm.getMappedLocation(loc.getLine(), loc.getColumn());
        if (mloc != null) {
            return mloc;
        } else {
            return loc;
        }
    }
    
    /**
     * Translate a location in the source file to the location in the compiled file.
     * Translation is based on an inverse application of source maps already registered.
     * @param loc location in the source file
     * @return corresponding location in the compiled file, or the original passed
     *         location if no registered source map provides the appropriate translation.
     */
    public Location getCompiledLocation(Location loc) {
        InverseMapping im;
        synchronized (inverseMappings) {
            im = inverseMappings.get(loc.getFile());
        }
        if (im == null) {
            return loc;
        }
        Location mloc = im.getMappedLocation(loc.getLine(), loc.getColumn());
        if (mloc != null) {
            return mloc;
        } else {
            return loc;
        }
    }
    
    public List<FileObject> getSourceFiles(FileObject compiledFile) {
        DirectMapping dm = getMapping(compiledFile, null);
        List<String> sourcePaths = dm.sourceMap.getSources();
        List<FileObject> sourceFiles = new ArrayList<>(sourcePaths.size());
        for (String sp : sourcePaths) {
            FileObject sourceFile = Location.getSourceFile(sp, dm.parentFolder);
            if (sourceFile != null) {
                sourceFiles.add(sourceFile);
            }
        }
        return sourceFiles;
    }
    
    
    private static class DirectMapping {

        private final FileObject parentFolder;
        private final SourceMap sourceMap;
        
        DirectMapping() {
            this.parentFolder = null;
            this.sourceMap = null;
        }

        DirectMapping(FileObject parentFolder, SourceMap sourceMap) {
            this.parentFolder = parentFolder;
            this.sourceMap = sourceMap;
        }
        
        Location getMappedLocation(int line, int column) {
            if (sourceMap == null) {
                return null;
            }
            Mapping mapping = sourceMap.findMapping(line, column);
            if (mapping == null) {
                return null;
            } else {
                return new Location(sourceMap, mapping, parentFolder);
            }
        }
    }
    
    private static class InverseMapping {
        
        private final String sourceName;
        private final FileObject source;
        private final SourceMap sourceMap;

        private InverseMapping(String sourceName, FileObject source, SourceMap sourceMap) {
            this.sourceName = sourceName;
            this.source = source;
            this.sourceMap = sourceMap;
        }

        private Location getMappedLocation(int line, int column) {
            Mapping mapping = sourceMap.findInverseMapping(sourceName, line, column);
            if (mapping == null) {
                return null;
            } else {
                return new Location(source, mapping.getOriginalLine(), mapping.getOriginalColumn());
            }
        }
        
    }
    
    public static final class Location {
        
        private FileObject file;
        private int line;
        private int column;
        
        private SourceMap sourceMap;
        private Mapping mapping;
        private FileObject parentFolder;
        
        public Location(FileObject file, int line, int column) {
            this.file = file;
            this.line = line;
            this.column = column;
        }
        
        Location(SourceMap sourceMap, Mapping mapping, FileObject parentFolder) {
            this.sourceMap = sourceMap;
            this.mapping = mapping;
            this.parentFolder = parentFolder;
            this.line = -1;
            this.column = -1;
        }
        
        static FileObject getSourceFile(String sourcePath, FileObject parentFolder) {
            File sourceFile = new File(sourcePath);
            FileObject fo;
            if (sourceFile.isAbsolute()) {
                fo = FileUtil.toFileObject(FileUtil.normalizeFile(sourceFile));
            } else {
                fo = parentFolder.getFileObject(sourcePath);
            }
            return fo;
        }
        
        public FileObject getFile() {
            if (file == null) {
                int sourceIndex = mapping.getSourceIndex();
                String sourcePath = sourceMap.getSourcePath(sourceIndex);
                file = getSourceFile(sourcePath, parentFolder);
            }
            return file;
        }
        
        public int getLine() {
            if (line < 0) {
                line = mapping.getOriginalLine();
            }
            return line;
        }
        
        public int getColumn() {
            if (column < 0) {
                column = mapping.getOriginalColumn();
            }
            return column;
        }
    }
    
}
