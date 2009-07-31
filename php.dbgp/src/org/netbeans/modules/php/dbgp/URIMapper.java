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
package org.netbeans.modules.php.dbgp;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.Pair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Converts remote (or also local) URI to local project File an vice versa
 * @author Radek Matous
 */
abstract class URIMapper {
    private static final Logger LOGGER = Logger.getLogger(URIMapper.class.getName());
    abstract File toSourceFile(URI remoteURI);
    abstract URI toWebServerURI(File localFile, boolean includeHostPart);

    URI toWebServerURI(File localFile) {
        return toWebServerURI(localFile, true);
    }

    static URIMapper.MultiMapper createMultiMapper(URI webServerURI, FileObject sourceFileObj,
            FileObject sourceRoot, List<Pair<String, String>> pathMapping) {
        //typicaly should be mappers called in this order:
        //- 1. mapper provided by user via project UI if any
        //- 2. base mapper
        //- 3. last resort mapper (one to one mapper)
        //TODO: we could also implement mapper with UI asking the user to add info for
        //mapping instead of lsat resort mapper implemented by one to one
        MultiMapper mergedMapper = new MultiMapper();
        for (Pair<String, String> pair : pathMapping) {
            //1. mapper provided by user via project UI if any
            pair = encodedPathMappingPair(pair);
            String uriPath = pair.first;
            String filePath = pair.second;
            if (uriPath.length() > 0 && filePath.length() > 0) {
                if (!uriPath.startsWith("file:")) {//NOI18N
                    if (!uriPath.startsWith("/")) {
                        uriPath = "file:/" + uriPath;//NOI18N
                    } else {
                        uriPath = "file:" + uriPath;//NOI18N
                    }
                }
                if (!uriPath.endsWith("/")) {//NOI18N
                    uriPath += "/";//NOI18N
                }
                URI remoteURI = URI.create(uriPath);
                File localFile = new File(filePath);
                FileObject localFo = FileUtil.toFileObject(localFile);
                if (localFo != null && localFo.isFolder()) {
                    URIMapper customMapper = URIMapper.createBasedInstance(remoteURI, localFile);
                    mergedMapper.addAsLastMapper(customMapper);
                }
            }
        }

        //2. base mapper that checks sourceFileObj && webServerURI to create webServerURIBase and  sourceFileObjBase
        //used for conversions
        URIMapper defaultMapper = createDefaultMapper(webServerURI, sourceFileObj, sourceRoot);
        if (defaultMapper != null) {
            mergedMapper.addAsLastMapper(defaultMapper);
        }
        //3. last resort just one to one mapper (should be called as last)
        mergedMapper.addAsLastMapper(createOneToOne());

        return mergedMapper;
    }
    static URIMapper createDefaultMapper(URI webServerURI, FileObject sourceFileObj, FileObject sourceRoot) {
        if (!"file".equals(webServerURI.getScheme())) {//NOI18N
            return null;
        }
        File webServerFile = new File(webServerURI);
        File sourceFile = FileUtil.toFile(sourceFileObj);
        String sourcePath = FileUtil.getRelativePath(sourceRoot, sourceFileObj);
        //debugged file must be part of the debugged project (for now)
        if (sourcePath != null) {
            if (sourceFile.isDirectory()) {
                //TODO: not sure about this (should be reviewed)
                sourceFile = new File(sourceFile, webServerFile.getName());
                if (!sourceFile.exists()) {
                    LOGGER.fine("No default path mapping: "+//NOI18N
                            "webServerURI: "+webServerURI.toString() + " sourceFile: " + sourceFile.getAbsolutePath());//NOI18N
                    return null;
                }
            }
            if (!sourceFile.equals(webServerFile)) {
                File sourceRootFile = FileUtil.toFile(sourceRoot);
                assert sourceRootFile != null;
                URI[] bases = findBases(webServerURI, sourceFile, sourceRootFile);
                if (bases != null) {
                    URI webServerBase = bases[0];
                    File sourceBase = new File(bases[1]);
                    assert webServerBase != null;
                    assert sourceBase != null;
                    return new BaseMapper(webServerBase, sourceBase);
                }
            }
        }
        //no decision how to map - must exist user's defined mapping
        LOGGER.fine("No default path mapping: " +//NOI18N
                "webServerURI: " + webServerURI.toString() + " sourceFile: " + sourceFile.getAbsolutePath());//NOI18N
        return null;
    }

    static URIMapper createOneToOne() {
        return new URIMapper() {
            private Map<File, File> can2AbsFile = new HashMap<File, File>();

            @Override
            File toSourceFile(URI remoteURI) {
                File retval = new File(remoteURI);
                File absFile = can2AbsFile.get(retval);
                return (absFile != null) ? absFile : retval;
            }

            @Override
            URI toWebServerURI(File localFile, boolean includeHostPart) {
                File canonicalFile = null;
                try {
                    canonicalFile = localFile.getCanonicalFile();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (!localFile.equals(canonicalFile)) {
                    can2AbsFile.put(canonicalFile, localFile);
                    localFile = canonicalFile;
                }
                return toURI(localFile, includeHostPart);
            }

        };
    }

    static URIMapper createBasedInstance(URI baseRemoteURI, File baseLocalFolder) {
        return new BaseMapper(baseRemoteURI, baseLocalFolder);
    }

    private static URI[] findBases(URI webServerURI, File sourceFile, File sourceRoot) {
        File baseFile = sourceFile;
        boolean nullRetVal = true;
        List<String> pathFragments = new ArrayList<String>();
        Collections.addAll(pathFragments, webServerURI.getPath().split("/"));//NOI18N
        Collections.reverse(pathFragments);
        for (String path : pathFragments) {
            if (baseFile != null && path.equals(baseFile.getName()) && !baseFile.equals(sourceRoot)) {
                nullRetVal = false;
                if (baseFile.equals(sourceRoot)) {
                    break;
                }
                baseFile = baseFile.getParentFile();
            } else {
                break;
            }
        }
        if (nullRetVal) {
            return null;
        }
        assert baseFile.isDirectory();
        int basePathLen = webServerURI.getPath().length() -
                (sourceFile.getAbsolutePath().length() - baseFile.getAbsolutePath().length());
        String basePath = webServerURI.getPath().substring(0, basePathLen);
        URI baseURI = createURI(webServerURI.getScheme(), webServerURI.getHost(),
               basePath, webServerURI.getFragment(),
                true, true);
        return new URI[]{baseURI, baseFile.toURI()};
    }

    private static class BaseMapper extends URIMapper {

        private static final String FILE_SCHEME = "file";
        private URI baseWebServerURI;
        private URI baseSourceURI;
        private File baseSourceFolder;

        BaseMapper(URI baseWebServerURI, File baseSourceFolder) {
            if (!baseSourceFolder.exists()) {
                throw new IllegalArgumentException();
            }
            if (!baseSourceFolder.isDirectory()) {
                throw new IllegalArgumentException();
            }

            this.baseSourceFolder = baseSourceFolder;
            this.baseWebServerURI = baseWebServerURI;
            boolean isLoggable = LOGGER.isLoggable(Level.FINE);
            if (isLoggable) {
                if (!FILE_SCHEME.equals(baseWebServerURI.getScheme())) {
                    LOGGER.fine("Unexpected scheme: "+baseWebServerURI.toString());//NOI18N
                }
                if (baseWebServerURI.getPath() == null) {
                    LOGGER.fine("URI.getPath() == null: "+baseWebServerURI.toString());//NOI18N
                }
                if (baseWebServerURI.getPath() == null) {
                    LOGGER.fine("URI.getPath() == null: "+baseWebServerURI.toString());//NOI18N
                } else if (!baseWebServerURI.getPath().endsWith("/")) {
                    LOGGER.fine("Not \"/\" at the end of URI.getPath(): "+baseWebServerURI.toString());//NOI18N
                }
                if (!baseWebServerURI.isAbsolute()) {
                    LOGGER.fine("URI not absolute: "+baseWebServerURI.toString());//NOI18N
                }
            }
            assert FILE_SCHEME.equals(baseWebServerURI.getScheme());
            assert baseWebServerURI.getPath() != null;//NOI18N
            assert baseWebServerURI.getPath().endsWith("/") : baseWebServerURI.getPath();//NOI18N
            assert baseWebServerURI.isAbsolute();
            this.baseSourceURI = baseSourceFolder.toURI();
            assert baseSourceURI.isAbsolute();
        }

        @Override
        File toSourceFile(URI webServerURI) {
            URI relativizedURI = baseWebServerURI.relativize(webServerURI);
            if (!relativizedURI.isAbsolute()) {
                assert FILE_SCHEME.equals(webServerURI.getScheme());
                return new File(baseSourceURI.resolve(relativizedURI));
            }
            return null;
        }

        @Override
        URI toWebServerURI(File sourceFile, boolean includeHostPart) {
            if (sourceFile.equals(baseSourceFolder)) {
                return baseWebServerURI;
            } else {
                URI relativizedURI = baseSourceURI.relativize(sourceFile.toURI());
                if (!relativizedURI.isAbsolute()) {
                    URI retval = baseWebServerURI.resolve(relativizedURI);
                    retval = createURI(retval.getScheme(), retval.getHost(),
                            retval.getPath(), retval.getFragment(),
                            true, false);
                    return retval;
                }
            }
            return null;
        }
    }

    static class MultiMapper extends URIMapper {
        private LinkedList<URIMapper> mappers = new LinkedList<URIMapper>();

        MultiMapper addAsFirstMapper(URIMapper mapper) {
            mappers.addFirst(mapper);
            return this;
        }

        MultiMapper addAsLastMapper(URIMapper mapper) {
            mappers.addLast(mapper);
            return this;
        }

        @Override
        File toSourceFile(URI remoteURI) {
            if ("file".equals(remoteURI.getScheme())) {//NOI18N
                for (URIMapper mapperInstance : mappers) {
                    File sourceFile = mapperInstance.toSourceFile(remoteURI);
                    if (sourceFile != null) {
                        return sourceFile;
                    }
                }
            }
            return null;
        }

        @Override
        URI toWebServerURI(File localFile, boolean includeHostPart) {
            for (URIMapper mapperInstance : mappers) {
                URI toWebServerURI = mapperInstance.toWebServerURI(localFile, includeHostPart);
                if (toWebServerURI != null) {
                    return toWebServerURI;
                }
            }
            return null;
        }
    }

    private static URI createURI(String scheme, String host, String path, String fragment,
            boolean includeHostPart, boolean pathEndsWithSlash) {
        if (pathEndsWithSlash && !path.endsWith("/")) {//NOI18N
            path = path + "/"; //NOI18N
        }
        if (host == null && includeHostPart) {
            host = ""; //NOI18N
        }
        try {
            return new URI(scheme, host, path, fragment);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static URI toURI(File webServerBase, boolean includeHostPart) {
        URI webServerBaseURI = webServerBase.toURI();
        return createURI(webServerBaseURI.getScheme(), webServerBaseURI.getHost(),
                webServerBaseURI.getPath(),webServerBaseURI.getFragment(),
                includeHostPart, webServerBase.exists() && webServerBase.isDirectory());
    }

    private static Pair<String, String> encodedPathMappingPair(Pair<String, String> pathMapping)  {
        String resName = pathMapping.first;
        resName = resName.replace('\\', '/');//NOI18N
        final String[] elements = resName.split("/"); // NOI18N
        final StringBuilder sb = new StringBuilder(200);
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            boolean skip = false;
            if (i == 0 && element.length() == 2 && element.charAt(1) == ':') {//NOI18N
                skip = true;
            }
            if (!skip) {
                try {
                    element = URLEncoder.encode(element, "UTF-8"); // NOI18N
                    element = element.replace("+", "%20"); // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            sb.append(element);
            if (i < elements.length - 1) {
                sb.append('/');
            }
        }
        return Pair.of(sb.toString(), pathMapping.second);//NOI18N
    }

}
