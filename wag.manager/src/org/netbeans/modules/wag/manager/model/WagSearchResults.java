/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.wag.manager.model;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author peterliu
 */
public class WagSearchResults extends WagItems<WagSearchResult> {

    public static final String WAG_HOME = System.getProperty("netbeans.user") +
            File.separator + "config" + File.separator + "WebApiGateway"; // NOI18N
    public static final String WAG_SEARCH_RESULTS = "WagSearchResults";     //NOI18N
    public static final String XML_EXT = "xml"; //NOI18N
    public static final String PROP_NAME = "searchResults";

    public String getDisplayName() {
        return NbBundle.getMessage(WagSearchResults.class, "Search_Node");
    }

    public String getDescription() {
        return NbBundle.getMessage(WagSearchResults.class, "Search_Node_Desc");
    }

    @Override
    public void refresh() {
        State oldState = state;
        state = State.INITIALIZED;
        fireChange(oldState, state);
    }

    protected Collection<WagSearchResult> loadItems() {
        return null;
    }

    protected String getPropName() {
        return PROP_NAME;
    }

    public static FileObject getWagHome() {
        File wagDir = new File(WAG_HOME);
        if (!wagDir.exists()) {
            wagDir.mkdirs();
        }
        return FileUtil.toFileObject(wagDir);
    }

    public static FileObject getWagSearchResultsFile() {
        FileObject fobj = getWagHome().getFileObject(WAG_SEARCH_RESULTS, XML_EXT);

        if (fobj == null) {
            try {
                fobj = getWagHome().createData(WAG_SEARCH_RESULTS, XML_EXT);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return fobj;
    }

    private void load() {
        FileObject fobj = getWagSearchResultsFile();

        if (fobj != null) {
            try {
                XMLDecoder d;
                d = new XMLDecoder(new BufferedInputStream(fobj.getInputStream()));
                Object result = (SortedSet<WagSearchResult>) d.readObject();
                d.close();

                this.items = (TreeSet<WagSearchResult>) result;
            } catch (Exception ex) {
                // simply ignore if the file is invalid and start fresh
            }
        }
    }

    private void save() {
        try {
            FileObject fobj = getWagSearchResultsFile();
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fobj.getOutputStream()));
            e.writeObject(items);
            e.close();
        } catch (FileAlreadyLockedException ex) {
            // ignore
        } catch (IOException ex) {
            // ignore
        }
    }
}
