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

package org.netbeans.modules.db.sql.loader;

import java.nio.charset.Charset;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Andrei Badea
 */
public class SQLDataObject extends MultiDataObject {

    private Lookup lookup;

    public SQLDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new SQLEditorSupport(this));
    }

    protected Node createNodeDelegate() {
        return new SQLNode(this);
    }

    public synchronized Lookup getLookup() {
        if (lookup == null) {
            lookup = new ProxyLookup(getCookieSet().getLookup(), Lookups.singleton(new FileEncodingQueryImpl()));
        }
        return lookup;
    }

    public boolean isConsole() {
        try {
            // the "console" files are stored in the SFS
            return "nbfs".equals(getPrimaryFile().getURL().getProtocol()); // NOI18N
        } catch (FileStateInvalidException e) {
            return false;
        }
    }

    void addCookie(Node.Cookie cookie) {
        getCookieSet().add(cookie);
    }

    void removeCookie(Node.Cookie cookie) {
        getCookieSet().remove(cookie);
    }

    private final class FileEncodingQueryImpl extends FileEncodingQueryImplementation {

        public Charset getEncoding(FileObject file) {
            // the "console" files are always in UTF-8
            if (isConsole()) {
                return Charset.forName("UTF-8"); // NOI18N
            }
            return null;
        }
    }
}
