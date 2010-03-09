/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public abstract class PhpElementImpl implements PhpElement {

    static enum SEPARATOR {
        SEMICOLON,
        COMMA,
        COLON,
        PIPE;

        @Override
        public String toString() {
            switch (this) {
                case SEMICOLON:
                    return ";";//NOI18N
                case COMMA:
                    return ",";//NOI18N
                case COLON:
                    return ":";//NOI18N
                case PIPE:
                    return "|";//NOI18N
                default:
                    assert false;
            }
            return super.toString();
        }
    }
    private static final String CLUSTER_URL = "cluster:"; // NOI18N
    private static String clusterUrl = null;
    private final String name;
    private final String in;
    private final String fileUrl;
    private final int offset;
    private final ElementQuery elementQuery;
    private FileObject fileObject;

    PhpElementImpl(final String name, final String in, final String fileUrl,
            final int offset, final ElementQuery elementQuery) {
        this.name = name;
        this.in = in;
        this.fileUrl = fileUrl;
        this.offset = offset;
        if (fileUrl != null && fileUrl.contains(" ")) {//NOI18N
            throw new IllegalArgumentException("fileURL may not contain spaces!");//NOI18N
        }
        this.elementQuery = elementQuery;
    }

    @Override
    public final String getFilenameUrl() {
        return fileUrl;
    }

    @Override
    public PhpModifiers getPhpModifiers() {
        return PhpModifiers.noModifiers();
    }

    @Override
    public final int getOffset() {
        return offset;
    }

    @Override
    public final Set<Modifier> getModifiers() {
        return getPhpModifiers().toModifiers();
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return new OffsetRange(offset, offset + getName().length());
    }

    @Override
    public ElementQuery getElementQuery() {
        return elementQuery;
    }

    @Override
    public final synchronized FileObject getFileObject() {
        String urlStr = fileUrl;
        if ((fileObject == null) && (fileUrl != null)) {
            fileObject = resolveFileObject(urlStr);
        }
        return fileObject;
    }

    public static FileObject resolveFileObject(final String urlStr) {
        String url = urlStr;
        if (url.startsWith(CLUSTER_URL)) {
            clusterUrl = getClusterUrl();
            url = clusterUrl + url.substring(CLUSTER_URL.length()); // NOI18N
        }
        return toFileObject(url);
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject toFileObject(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return URLMapper.findFileObject(url);
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }

        return null;
    }

    private static String getClusterUrl() {
        String retval = null;
        if (retval == null) {
            File f =
                    InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-php-editor.jar", null, false); // NOI18N

            if (f == null) {
                throw new RuntimeException("Can't find cluster");
            }

            f = new File(f.getParentFile().getParentFile().getAbsolutePath());

            try {
                f = f.getCanonicalFile();
                retval = f.toURI().toURL().toExternalForm();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return retval;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getIn() {
        return in;
    }

    public abstract String getSignature();

    @Override
    public final boolean signatureEquals(ElementHandle handle) {
        if (handle instanceof PhpElementImpl) {
            PhpElementImpl other = (PhpElementImpl) handle;
            return this.getSignature().equals(other.getSignature());
        }
        return false;
    }

    @Override
    public final boolean isPlatform() {
        FileObject fo = getFileObject();
        if (fo != null) {
            try {
                return Repository.getDefault().getDefaultFileSystem().equals(fo.getFileSystem());
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    @Override
    public final String getMimeType() {
        return FileUtils.PHP_MIME_TYPE;
    }

    @Override
    public final int getFlags() {
        return getPhpModifiers().toFlags();
    }

    @Override
    public final ElementKind getKind() {
        return getPhpElementKind().getElementKind();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhpElementImpl other = (PhpElementImpl) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.in == null) ? (other.in != null) : !this.in.equals(other.in)) {
            return false;
        }
        if (this.offset != other.offset) {
            return false;
        }
        if ((this.fileUrl == null) ? (other.fileUrl != null) : !this.fileUrl.equals(other.fileUrl)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 71 * hash + (this.in != null ? this.in.hashCode() : 0);
        hash = 71 * hash + (this.fileUrl != null ? this.fileUrl.hashCode() : 0);
        hash = 71 * hash + this.offset;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPhpElementKind().toString()).append(" ");//NOI18N
        if (this instanceof FullyQualifiedElement) {
            sb.append(((FullyQualifiedElement)this).getFullyQualifiedName().toString());
        } else {
            sb.append(getName());
        }
        return sb.toString();
    }


}
