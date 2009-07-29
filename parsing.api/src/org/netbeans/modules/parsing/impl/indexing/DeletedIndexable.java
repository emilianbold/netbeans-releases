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

package org.netbeans.modules.parsing.impl.indexing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tomas Zezula
 */
public final class DeletedIndexable implements IndexableImpl {

    private static final Logger LOG = Logger.getLogger(DeletedIndexable.class.getName());

    private final URL root;
    private final String relativePath;

    public DeletedIndexable (final URL root, final String relativePath) {
        assert root != null : "root must not be null"; //NOI18N
        assert relativePath != null : "relativePath must not be null"; //NOI18N
        this.root = root;
        this.relativePath = relativePath;
    }

//    public long getLastModified() {
//        return -1;
//    }
//
//    public String getName() {
//        int index = this.relativePath.lastIndexOf('/'); //NOI18N
//        return index == -1 ? relativePath : relativePath.substring(index+1);
//    }

    public String getRelativePath() {
        return relativePath;
    }

    public URL getURL() {
        try {
            return Util.resolveUrl(root, relativePath);
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    public String getMimeType() {
        throw new UnsupportedOperationException("Mimetype related operations are not supported by DeletedIndexable"); //NOI18N
    }

    public boolean isTypeOf(String mimeType) {
        throw new UnsupportedOperationException("Mimetype related operations are not supported by DeletedIndexable"); //NOI18N
    }

//    public InputStream openInputStream() throws IOException {
//        throw new IOException();
//    }

    @Override
    public String toString() {
        return "DeletedIndexable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + getURL() + "]"; //NOI18N
    }

}
