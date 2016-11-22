/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.source;

import com.sun.jdi.StringReference;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public final class Source {
    
    public static final String URL_PROTOCOL = "truffle-scripts"; // NOI18N
    static final String ATTR_URI = "com.oracle.truffle InternalURI"; // NOI18N
    
    private static final Map<JPDADebugger, Map<Long, Source>> KNOWN_SOURCES = new WeakHashMap<>();

    private final StringReference codeRef;
    private final String name;
    private final URI uri;          // The original source URI
    private final URL url;          // The source
    private final long hash;
    private String content;
    
    private Source(String name, URI uri, long hash, StringReference codeRef) {
        this.name = name;
        this.codeRef = codeRef;
        URL url = null;
        if (uri == null || !"file".equalsIgnoreCase(uri.getScheme())) {
            try {
                url = SourceFilesCache.getDefault().getSourceFile(name, hash, uri, getContent());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (url == null) {
            try {
                url = uri.toURL();
            } catch (MalformedURLException muex) {
                Exceptions.printStackTrace(muex);
            }
        }
        this.url = url;
        this.uri = uri;
        this.hash = hash;
    }
    
    public static Source getExistingSource(JPDADebugger debugger, long id) {
        synchronized (KNOWN_SOURCES) {
            Map<Long, Source> dbgSources = KNOWN_SOURCES.get(debugger);
            if (dbgSources != null) {
                Source src = dbgSources.get(id);
                if (src != null) {
                    return src;
                }
            }
        }
        return null;
    }
    
    /**
     * Find an existing Source instance for the given FileObject.
     * Currently, this method returns sources only for non-file sources.
     * @param fo
     * @return a Source, or <code>null</code>.
     *
    public static Source get(JPDADebugger debugger, FileObject fo) {
        URI uri = fo.toURI();
        if (!URL_PROTOCOL.equals(uri.getScheme())) {
            return null;
        }
        String path = uri.getPath();
        int hashEnd = path.indexOf('/');
        String hashStr = path.substring(0, hashEnd);
        long id = Long.parseUnsignedLong(hashStr, 16);
        return getExistingSource(debugger, id);
    }*/
    
    public static URI getTruffleInternalURI(FileObject fo) {
        return (URI) fo.getAttribute(ATTR_URI);
    }
    
    public static Source getSource(JPDADebugger debugger, long id,
                                   String name,
                                   String path,
                                   URI uri,
                                   StringReference codeRef) {
        synchronized (KNOWN_SOURCES) {
            Map<Long, Source> dbgSources = KNOWN_SOURCES.get(debugger);
            if (dbgSources != null) {
                Source src = dbgSources.get(id);
                if (src != null) {
                    return src;
                }
            }
        }
        return getTheSource(debugger, id, name, path, uri, codeRef);
    }
    
    private static Source getTheSource(JPDADebugger debugger, long id,
                                       String name,
                                       String path,
                                       URI uri,
                                       StringReference codeRef) {
        
        Source src = new Source(name, uri, id, codeRef);
        synchronized (KNOWN_SOURCES) {
            Map<Long, Source> dbgSources = KNOWN_SOURCES.get(debugger);
            if (dbgSources == null) {
                dbgSources = new HashMap<>();
                KNOWN_SOURCES.put(debugger, dbgSources);
            }
            dbgSources.put(id, src);
        }
        return src;
    }
    
    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public long getHash() {
        return hash;
    }

    public String getContent() {
        synchronized (this) {
            if (content == null) {
                try {
                    content = StringReferenceWrapper.value(codeRef);
                } catch (InternalExceptionWrapper |
                         VMDisconnectedExceptionWrapper |
                         ObjectCollectedExceptionWrapper ex) {
                    content = ex.getLocalizedMessage();
                }
            }
            return content;
        }
    }
    
}
