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

package org.netbeans.modules.subversion.remote.client;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.client.parser.ParserSvnInfo;
import org.netbeans.modules.subversion.remote.client.parser.SvnWcUtils;
import org.netbeans.modules.subversion.remote.config.KVFile;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

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
 * <strong>Works also with 1.7+ working copies, however does not access metadata but calls methods on 
 * {@link org.tigris.subversion.svnclientadapter.ISVNClientAdapter} instead. 
 * Performance comes into question here - that's because svn props are not displayed in Local_changes mode.</strong>
 *
 * 
 */
public final class PropertiesClient {

    private final VCSFileProxy file;

    /** Creates a new instance of PropertiesClient */
    public PropertiesClient(VCSFileProxy file) {
        assert file != null;
        this.file = file;
    }

    /**
     * Loads BASE properties for given file.
     * @return property map&lt;String, byte[]> never null
     */
    public Map<String, byte[]> getBaseProperties (boolean contactServer) throws IOException {
        // XXX: refactor code, join with getProperties()
        if (hasOldMetadata(file)) {
            VCSFileProxy store;
            try {
                store = getPropertyFile(true);
            } catch (SVNClientException ex) {
                throw new IOException(ex.getMessage());
            }
            if (store != null && store.isFile()) {
                KVFile kv = new KVFile(store);
                return kv.getNormalizedMap();
            } else {
                return new HashMap<>();
            }
        } else {
            Map<String, byte[]> map = new HashMap<>();
            try {
                if (contactServer) {
                    SvnClient client = Subversion.getInstance().getClient(file);
                    if (client != null) {
                        ISVNInfo info = SvnUtils.getInfoFromWorkingCopy(client, file);
                        if (info != null && (info.getUrl() != null || info.getCopyUrl() != null) && info.getRevision() != null
                                && info.getRevision().getNumber() > -1) {
                            ISVNProperty[] props = client.getProperties(info.getCopyUrl() == null ? info.getUrl() : info.getCopyUrl(),
                                    SVNRevision.getRevision(info.getRevision().toString()),
                                    SVNRevision.getRevision(info.getRevision().toString()),
                                    false);
                            for (ISVNProperty prop : props) {
                                map.put(prop.getName(), prop.getData());
                            }
                        }
                    }
                } else {
                    return getProperties();
                }
                return map;
            } catch (SVNClientException ex) {
                return map;
            } catch (ParseException ex) {
                return map;
            }
        }
    }

    /**
     * Loads (locally modified) properties for given file.
     * @return property map&lt;String, byte[]> never null
     */
    public Map<String, byte[]> getProperties() throws IOException {
        if (hasOldMetadata(file)) {
            VCSFileProxy store;
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
                return new HashMap<>();
            }
        } else {
            try {
                SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                Map<String, byte[]> map = new HashMap<>();
                if (client != null) {
                    ISVNProperty[] props = client.getProperties(file);
                    for (ISVNProperty prop : props) {
                        map.put(prop.getName(), prop.getData());
                    }
                }
                return map;
            } catch (SVNClientException ex) {
                return new HashMap<>();
            }
        }
    }

    private VCSFileProxy getPropertyFile(boolean base) throws SVNClientException {
        SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
        ISVNInfo info = null;
        try {
            info = SvnUtils.getInfoFromWorkingCopy(client, file);
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

    private static boolean hasOldMetadata (VCSFileProxy file) {
        if (file.isDirectory()) {
            if (VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(file, SvnUtils.SVN_ENTRIES_DIR))) {
                return true;
            }
        }
        VCSFileProxy parent = file.getParentFile();
        return parent != null &&
               VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(parent, SvnUtils.SVN_ENTRIES_DIR)) &&
               !VCSFileProxy.createFileProxy(parent, SvnUtils.SVN_WC_DB).exists();
    }
}
