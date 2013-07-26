/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.apt.utils;

import java.util.Iterator;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.SupportAPIAccessor;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTIncludeUtils {
    
    private APTIncludeUtils() {
    }
    
    /** 
     * finds file relatively to the baseFile 
     * caller must check that resolved path is not the same as base file
     * to prevent recursive inclusions 
     */
    public static ResolvedPath resolveFilePath(FileSystem fs, String inclString, CharSequence baseFile) {
        if (baseFile != null) {
            String folder = CndPathUtilities.getDirName(baseFile.toString());
            String absolutePath = folder + CndFileUtils.getFileSeparatorChar(fs) + inclString;
            if (isExistingFile(fs, absolutePath)) {
                absolutePath = normalize(fs, absolutePath);
                folder = normalize(fs, folder);
                return new ResolvedPath(fs, FilePathCache.getManager().getString(folder), absolutePath, true, 0);
            }
        }
        return null;
    }
    
    public static ResolvedPath resolveAbsFilePath(FileSystem fs, String absFile) {
        if (APTTraceFlags.APT_ABSOLUTE_INCLUDES) {
            if (CndPathUtilities.isPathAbsolute(absFile) && isExistingFile(fs, absFile) ) {
                absFile = normalize(fs, absFile);
                String parent = CndPathUtilities.getDirName(absFile);
                return new ResolvedPath(fs, FilePathCache.getManager().getString(parent), absFile, false, 0);
            }
        }   
        return null;
    }    

    private static String normalize(FileSystem fs, String path) {
        return CndFileUtils.normalizeAbsolutePath(fs, path);
    }

    private static boolean isExistingFile(FileSystem fs, String filePath) {
        return CndFileUtils.isExistingFile(fs, filePath);
    }
        
    /**
     * Resolves file path in a given search paths
     */
    public static interface FilePathResolver {
        
        ResolvedPath resolve(Iterator<IncludeDirEntry> searchPaths, String anIncludedFile, int dirOffset);                
    }
    
    /**
     * Abstract path resolver iterates over search paths and provides some convenient methods
     */
    public static abstract class AbstractPathResolver implements FilePathResolver {

        @Override
        public ResolvedPath resolve(Iterator<IncludeDirEntry> searchPaths, String anIncludedFile, int dirOffset) {
            SupportAPIAccessor accessor = SupportAPIAccessor.get();
            while( searchPaths.hasNext() ) {
                IncludeDirEntry dirPrefix = searchPaths.next();
                if (accessor.isExistingDirectory(dirPrefix)) {
                    FileSystem fs = dirPrefix.getFileSystem();
                    char fileSeparatorChar = CndFileUtils.getFileSeparatorChar(fs);
                    String includedFile = anIncludedFile.replace('/', fileSeparatorChar);
                    CharSequence prefix = dirPrefix.getAsSharedCharSequence();
                    int len = prefix.length();
                    String absolutePath;
                    if (len > 0 && prefix.charAt(len - 1) == fileSeparatorChar) {
                        absolutePath = prefix + includedFile;
                    } else {
                        absolutePath = CharSequenceUtils.toString(prefix, fileSeparatorChar, includedFile);
                    }
                    ResolvedPath resolvedPath = resolvePrepared(dirPrefix, prefix, includedFile, absolutePath, dirOffset);
                    if (resolvedPath != null) {
                        return resolvedPath;
                    }
                }
                dirOffset++;
            }
            return null;
        }
        
        protected abstract ResolvedPath resolvePrepared(IncludeDirEntry dirPrefix, CharSequence prefix, String includedFile, String absolutePath, int dirOffset);
        
        protected String normilize(FileSystem fs, String path) {
            return APTIncludeUtils.normalize(fs, path);
        }
        
        protected boolean isExistingFile(FileSystem fs, String filePath) {
            return APTIncludeUtils.isExistingFile(fs, filePath);
        }
    }
    
    /**
     * Default path resolver just checks if file exists and returns resolved path if so.
     */
    public static class DefaultPathResolver extends AbstractPathResolver {        
        
        @Override
        protected ResolvedPath resolvePrepared(IncludeDirEntry dirPrefix, CharSequence prefix, String includedFile, String absolutePath, int dirOffset) {
            FileSystem fs = dirPrefix.getFileSystem();
            if (isExistingFile(fs, absolutePath)) {
                return new ResolvedPath(fs, prefix, normalize(fs, absolutePath), false, dirOffset);
            }
            return null;
        }    
    }
    
    /**
     * This resolver provides a hack for some frameworks on MAC OS.
     */
    public static class FrameworksPathResolver extends DefaultPathResolver {

        @Override
        protected ResolvedPath resolvePrepared(IncludeDirEntry dirPrefix, CharSequence prefix, String includedFile, String absolutePath, int dirOffset) {
            ResolvedPath result = super.resolvePrepared(dirPrefix, prefix, includedFile, absolutePath, dirOffset);
            if (result == null && dirPrefix.isFramework()) {
                FileSystem fs = dirPrefix.getFileSystem();
                
                // 1. Old hack which tries to handle different frameworks (GLUT, Qt, ...)
                int i = includedFile.indexOf('/'); // NOI18N
                if (i > 0) {
                    // possible it is framework include (see IZ#160043)
                    // #include <GLUT/glut.h>
                    // header is located in the /System/Library/Frameworks/GLUT.framework/Headers
                    // system path is /System/Library/Frameworks
                    // So convert framework path
                    absolutePath = dirPrefix.getPath()+"/"+includedFile.substring(0,i)+".framework/Headers"+includedFile.substring(i); // NOI18N
                    if (isExistingFile(fs, absolutePath)) {
                        return new ResolvedPath(fs, dirPrefix.getAsSharedCharSequence(), normalize(fs, absolutePath), false, dirOffset);
                    }
                }                  
            }
            return result;
        }
    }
    
    /**
     * This resolver provides a hack for resolving QT headers on MAC OS.
     * 
     * In fact it is a way better to parse generated qt makefile and extract paths from it,
     * but in this case Qt project will be all red until first build.
     */
    public static class QtPathResolver extends FrameworksPathResolver {
        
        protected final MakeConfiguration projectConfiguration;

        public QtPathResolver(MakeConfiguration projectConfiguration) {
            this.projectConfiguration = projectConfiguration;
        }        

        @Override
        protected ResolvedPath resolvePrepared(IncludeDirEntry dirPrefix, CharSequence prefix, String includedFile, String absolutePath, int dirOffset) {
            ResolvedPath result = super.resolvePrepared(dirPrefix, prefix, includedFile, absolutePath, dirOffset);
            if (result == null && projectConfiguration != null && dirPrefix.isFramework()) {
                FileSystem fs = dirPrefix.getFileSystem();
                
                // New hack because in Qt4/5 one can include headers without paths (#include <QApplication>), 
                // but on MAC OS search paths from qmake do not contain such headers. Qmake includes special augmented
                // paths only into Makefile during build. Here we should include these special paths                
                QmakeConfiguration qmakeConf = projectConfiguration.getQmakeConfiguration();
                if (qmakeConf != null) {                                       
                    for (QtModuleDescriptor module : QtModules.values()) {
                        if (module.isEnabled(qmakeConf)) {
                            absolutePath = dirPrefix.getPath()+"/"+module.getName()+".framework/Headers/"+includedFile; // NOI18N
                            if (isExistingFile(fs, absolutePath)) {
                                return new ResolvedPath(fs, dirPrefix.getAsSharedCharSequence(), normalize(fs, absolutePath), false, dirOffset);
                            }                            
                        }
                    }
                }
            }
            return result;
        }
        
        /*
        ************************************************************************
        *   List of Qt modules
        ************************************************************************
        */
        
        private static interface QtModuleDescriptor {
            
            String getName();
            
            boolean isEnabled(QmakeConfiguration conf);
        }
        
        private static enum QtModules implements QtModuleDescriptor {
            
            QtCore {

                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isCoreEnabled().getValue();
                }
                
            },
            
            QtGui {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isGuiEnabled().getValue();
                }                
                
            },
            
            QtWidgets {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isWidgetsEnabled().getValue();
                }                
                
            },
            
            QtMultimedia {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtNetwork {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isNetworkEnabled().getValue();
                }                
                
            },
            
            QtOpenGL {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isOpenglEnabled().getValue();
                }                
                       
            },
            
            QtOpenVG {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                       
            },
            
            QtScript {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtScriptTools {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtSql {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isSqlEnabled().getValue();
                }                
                
            },
            
            QtSvg {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isSvgEnabled().getValue();
                }                
                
            },
            
            QtWebKit {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isWebkitEnabled().getValue();
                }                
                
            },
            
            QtXml {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isXmlEnabled().getValue();
                }                
                
            },
            
            QtXmlPatterns {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtDeclarative {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            Phonon {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isPhononEnabled().getValue();
                }                
                
            },
            
            Qt3Support {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return conf.isQt3SupportEnabled().getValue();
                }                
                
            },
            
            QtDesigner {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtUiTools {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtHelp {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtTest {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QAxContainer {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QAxServer {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            },
            
            QtDBus {
                
                @Override
                public boolean isEnabled(QmakeConfiguration conf) {
                    return false;
                }                
                
            };

            @Override
            public String getName() {
                return name();
            }
        }
    }
}
