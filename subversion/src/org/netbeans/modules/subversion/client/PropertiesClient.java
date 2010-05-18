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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion.client;

import java.io.*;
import java.util.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.parser.ParserSvnInfo;
import org.netbeans.modules.subversion.client.parser.SvnWcUtils;
import org.netbeans.modules.subversion.config.KVFile;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Implements properties access that is not supported
 * by svnClientAdapter library. It access <tt>.svn</tt>
 * metadata directly:
 *
 * <pre>
 *    trunk/
 *        .svn/
 *            dir-props            (KV file format)
 *            dir-props-base       (KV file format)
 *            props/
 *               filename.svn-base         (KV file format)
 *               filename_newprop.svn-base (KV file format)
 *            props-base/
 *               filename.svn-base         (KV file format)
 *        filename
 *        filename_newprop
 * </pre>
 *
 * <b>The implemetation should be moved into svnClientAdpater
 * library!</b>
 *
 * @author Petr Kuzel
 */
public final class PropertiesClient {

    private final File file;

    /** Creates a new instance of PropertiesClient */
    public PropertiesClient(File file) {
        assert file != null;
        this.file = file;
    }

    /**
     * Loads BASE properties for given file.
     * @return property map&lt;String, byte[]> never null
     */
    public Map<String, byte[]> getBaseProperties() throws IOException {
        File store;
        try {
            store = getPropertyFile(true);
        } catch (SVNClientException ex) {
            throw new IOException(ex.getMessage());
        }
        if (store != null && store.isFile()) {
            KVFile kv = new KVFile(store);
            return kv.getNormalizedMap();
        } else {
            return new HashMap<String, byte[]>();
        }
    }

    /**
     * Loads (locally modified) properties for given file.
     * @return property map&lt;String, byte[]> never null
     */
    public Map<String, byte[]> getProperties() throws IOException {
        File store;
        try {
            store = getPropertyFile(false);
            if (store == null) {
                // if no changes are made, the props.work does not exist
                // so return the base prop-file - see #
                store = getPropertyFile(true);
            }
        } catch (SVNClientException ex) {
            throw new IOException(ex.getMessage());
        }
        if (store != null && store.isFile()) {
            KVFile kv = new KVFile(store);
            return kv.getNormalizedMap();
        } else {
            return new HashMap<String, byte[]>();
        }
    }

    private File getPropertyFile(boolean base) throws SVNClientException {
        SvnClient client = Subversion.getInstance().getClient(false);
        ISVNInfo info = null;
        try {
            info = client.getInfoFromWorkingCopy(file);
        } catch (SVNClientException ex) {
            throw ex;
        }
        if(info instanceof ParserSvnInfo) {
            if(base) {
                return ((ParserSvnInfo) info).getBasePropertyFile();
            } else {
                return ((ParserSvnInfo) info).getPropertyFile();
            }
        } else {
            return SvnWcUtils.getPropertiesFile(file, base);
        }
    }

    /** Not implemented. */
    public Map getProperties(int revision) throws IOException {
        throw new UnsupportedOperationException();
    }
}
