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
package org.netbeans.modules.maven.grammar;

import hidden.org.codehaus.plexus.util.FileUtils;
import hidden.org.codehaus.plexus.util.IOUtil;
import hidden.org.codehaus.plexus.util.StringUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.store.FSDirectory;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class PluginIndexManager {

    private static final String INDEX_PATH = "maven-plugins-index"; //NOI18N
    private static IndexReader indexReader;
    private static String FIELD_ID = "id";
    private static String FIELD_GOALS = "gls";
    private static String FIELD_PREFIX = "prfx";
    private static String PREFIX_FIELD_GOAL = "mj_";
    final static int BUFFER = 2048;

    private static synchronized IndexSearcher getIndexSearcher() throws Exception {
        if (indexReader == null) {
            FSDirectory dir = FSDirectory.getDirectory(getDefaultIndexLocation());
            indexReader = IndexReader.open(dir);
        }
        //TODO shall the searcher be stored as field??
        return new IndexSearcher(indexReader);
    }

    public static Set<String> getPluginGoalNames(Set<String> groups) throws Exception {
        IndexSearcher searcher = getIndexSearcher();
        BooleanQuery bq = new BooleanQuery();
        for (String grp : groups) {
            PrefixQuery pq = new PrefixQuery(new Term(FIELD_ID, grp));
            bq.add(new BooleanClause(pq, BooleanClause.Occur.SHOULD));
        }
        Hits hits = searcher.search(bq);
        Iterator it = hits.iterator();
        TreeSet<String> toRet = new TreeSet<String>();
        while (it.hasNext()) {
            Hit hit = (Hit) it.next();
            Document doc = hit.getDocument();
            //TODO shall we somehow pick just one version fom a given plugin here? how?
            String prefix = doc.getField(FIELD_PREFIX).stringValue();
            String goals = doc.getField(FIELD_GOALS).stringValue();
            String[] gls = StringUtils.split(goals, " ");
            for (String goal : gls) {
                toRet.add(prefix + ":" + goal);
            }
        }
        return toRet;
    }


    private static int checkLocalVersion(File[] fls) {
        for (File fl : fls) {
            try {
                int intVersion = Integer.parseInt(fl.getName());
                return intVersion;
            } catch (NumberFormatException e) {
                Exceptions.printStackTrace(e);
            }
        }
        //if there is a folder, but not a number, return max value to be sure
        //we don't overwrite stuff..
        return fls.length > 0 ? Integer.MAX_VALUE : 0;
    }

    private static int checkZipVersion(File cacheDir) {
        InputStream is = null;
        try {
            is = PluginIndexManager.class.getClassLoader().getResourceAsStream("org/netbeans/modules/maven/grammar/pluginz.zip"); //NOI18N
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                File fl = new File(cacheDir, entry.getName());
                if (!fl.getParentFile().equals(cacheDir)) {
                    String version = fl.getParentFile().getName();
                    try {
                        int intVersion = Integer.parseInt(version);
                        return intVersion;
                    } catch (NumberFormatException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        } catch (IOException io) {
            Exceptions.printStackTrace(io);
        } finally {
            IOUtil.close(is);
        }
        return 0; //a fallback
    }

    private static File getDefaultIndexLocation() {
        String userdir = System.getProperty("netbeans.user"); //NOI18N
        File cacheDir;
        if (userdir != null) {
            cacheDir = new File(new File(new File(userdir, "var"), "cache"), INDEX_PATH);//NOI18N
        } else {
            File root = FileUtil.toFile(Repository.getDefault().getDefaultFileSystem().getRoot());
            cacheDir = new File(root, INDEX_PATH);//NOI18N
        }
        cacheDir.mkdirs();
        File[] fls = cacheDir.listFiles();
        if (fls == null || fls.length == 0) {
            //copy the preexisting index in module into place..
            InputStream is = null;
            try {
                is = PluginIndexManager.class.getClassLoader().getResourceAsStream("org/netbeans/modules/maven/grammar/pluginz.zip"); //NOI18N
                ZipInputStream zis = new ZipInputStream(is);
                unzip(zis, cacheDir);
            } finally {
                IOUtil.close(is);
            }
        } else {
            int zipped = checkZipVersion(cacheDir);
            int local = checkLocalVersion(fls);
            if (zipped > local && local > 0) {
                try {
                    FileUtils.deleteDirectory(new File(cacheDir, "" + local));
                    //copy the preexisting index in module into place..
                    InputStream is = null;
                    try {
                        is = PluginIndexManager.class.getClassLoader().getResourceAsStream("org/netbeans/modules/maven/grammar/pluginz.zip"); //NOI18N
                        ZipInputStream zis = new ZipInputStream(is);
                        unzip(zis, cacheDir);
                    } finally {
                        IOUtil.close(is);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        File[] files = cacheDir.listFiles();
        assert files != null && files.length == 1;
        cacheDir = files[0];
        return cacheDir;
    }

    private static void unzip(ZipInputStream zis, File cacheDir) {
        try {
            BufferedOutputStream dest = null;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                File fl = new File(cacheDir, entry.getName());
                fl.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(fl);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(zis);
        }
    }
}
