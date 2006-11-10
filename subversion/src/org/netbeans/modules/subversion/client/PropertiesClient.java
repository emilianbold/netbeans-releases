/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.client;

import java.io.*;
import java.util.*;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.parser.ParserSvnInfo;
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
            return normalize(kv.getMap());
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
        } catch (SVNClientException ex) {
            throw new IOException(ex.getMessage());
        }
        if (store != null && store.isFile()) {
            KVFile kv = new KVFile(store);
            return normalize(kv.getMap());
        } else {
            return new HashMap<String, byte[]>();
        }
    }

    private File getPropertyFile(boolean base) throws SVNClientException {
        // XXX realy not sure if this is the best way ...
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
            throw new SVNClientException("Unexpected value:" + info + " should be from type " + ParserSvnInfo.class);
        }         
    }
    
    private Map<String, byte[]> normalize(Map map) {
        Map<String, byte[]> ret = new HashMap<String, byte[]>(map.size());
        Iterator<Map.Entry> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry next = it.next();
            // getKey().toString() == the normalization
            ret.put(next.getKey().toString(), (byte[]) next.getValue());
        }
        return ret;
    }

    /** Not implemented. */
    public Map getProperties(int revision) throws IOException {
        throw new UnsupportedOperationException();
    }
}
