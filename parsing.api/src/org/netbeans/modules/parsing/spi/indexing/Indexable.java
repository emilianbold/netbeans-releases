/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.spi.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


/**
 * Represens a file to be procesed by an indexer.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Indexable {

    private final URI file;
    private final URI root;
    private final long lastModified;
    private String name;

    Indexable(final URI file, final URI root, final long lastModified) {
        assert root != null;
        assert file != null;
        assert root.isAbsolute();
        assert file.isAbsolute();
        this.file = file;
        this.root = root;
        this.lastModified = lastModified;
    }

    /**
     * Returns a relative path from root to the
     * represented file.
     * @return the relative URI
     */
    public URI getRelativePath () {
        return file.relativize(this.root);
    }

    /**
     * Returns a name of represented file.
     * @return a name
     */
    public String getName () {
        if (name == null) {
            String path = file.getPath();
            int index = path.lastIndexOf('/');  //NOI18N
            name = index < 0 ? path : path.substring(index+1);
        }
        return name;
    }

    /**
     * Returns absolute URI of the represente file
     * @return uri
     */
    public URI getURI () {
        return this.file;
    }

    /**
     * Returns a time when the file was last modified
     * @return A long value representing the time the file was last modified,
     * measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
     * or 0L if the file does not exist or if an I/O error occurs
     */
    public long getLastModified () {
        return this.lastModified;
    }

    /**
     * Returns {@link InputStream} of represented file.
     * The caller is responsible to correctly close the stream.
     * @return the {@link InputStream} to read the content
     * @throws java.io.IOException
     */
    public InputStream openInputStream () throws IOException {
        throw new UnsupportedOperationException("todo");
    }

}
