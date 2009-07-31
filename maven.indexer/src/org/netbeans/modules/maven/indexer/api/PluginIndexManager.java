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
package org.netbeans.modules.maven.indexer.api;

import hidden.org.codehaus.plexus.util.Base64;
import hidden.org.codehaus.plexus.util.FileUtils;
import hidden.org.codehaus.plexus.util.IOUtil;
import hidden.org.codehaus.plexus.util.StringUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class PluginIndexManager {

    private static final String ZIP_LOCATION = "org/netbeans/modules/maven/indexer/pluginz.zip"; //NOI18N

    private static final String INDEX_PATH = "maven-plugins-index"; //NOI18N
    private static IndexReader indexReader;
    
    /**
     * groupId + "|" + artifactId + "|" + version;
     */
    private static String FIELD_ID = "id";//NOI18N
    /**
     * 2.0.x or similar name of maven core. a document has either this or id field.
     */
    private static String FIELD_MVN_VERSION = "mvn";//NOI18N
    /**
     * space separated list of goal names
     */
    private static String FIELD_GOALS = "gls";//NOI18N
    /**
     * goal prefix
     */
    private static String FIELD_PREFIX = "prfx";//NOI18N
    
    /**
     * | is the separator
     * [0] - name
     * [1] - editable
     * [2] - required
     * [3] - expression or "null"
     * [4] - default value or "null"
     */
    private static String PREFIX_FIELD_GOAL = "mj_";//NOI18N
    /**
     * space separated list of lifecycles/packagings
     */
    private static String FIELD_CYCLES = "ccls";//NOI18N
    private static String PREFIX_FIELD_CYCLE = "ccl_";//NOI18N


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
            String[] gls = StringUtils.split(goals, " ");//NOI18N
            for (String goal : gls) {
                toRet.add(prefix + ":" + goal); //NOI18N
            }
        }
        return toRet;
    }

    public static Set<String> getPluginGoals(String groupId, String artifactId, String version) throws Exception {
        assert groupId != null && artifactId != null && version != null;
        IndexSearcher searcher = getIndexSearcher();
        String id = groupId + "|" + artifactId + "|" + version; //NOI18N
        TermQuery tq = new TermQuery(new Term(FIELD_ID, id));
        Hits hits = searcher.search(tq);
        if (hits.length() == 0) {
            return null;
        }
        Iterator it = hits.iterator();
        TreeSet<String> toRet = new TreeSet<String>();
        while (it.hasNext()) { //well should be just one anyway..
            Hit hit = (Hit) it.next();
            Document doc = hit.getDocument();
            String goals = doc.getField(FIELD_GOALS).stringValue();
            String[] gls = StringUtils.split(goals, " "); //NOI18N
            for (String goal : gls) {
                toRet.add(goal);
            }
        }
        return toRet;
    }

    public static Set<String> getPluginParameterNames(String groupId, String artifactId, String version, String mojo) throws Exception {
        assert groupId != null && artifactId != null && version != null;
        IndexSearcher searcher = getIndexSearcher();
        String id = groupId + "|" + artifactId + "|" + version; //NOI18N
        TermQuery tq = new TermQuery(new Term(FIELD_ID, id));
        Hits hits = searcher.search(tq);
        if (hits.length() == 0) {
            return null;
        }
        Iterator it = hits.iterator();
        TreeSet<String> toRet = new TreeSet<String>();
        while (it.hasNext()) { //well should be just one anyway..
            Hit hit = (Hit) it.next();
            Document doc = hit.getDocument();
            String goals = doc.getField(FIELD_GOALS).stringValue();
            String[] gls = StringUtils.split(goals, " "); //NOI18N
            for (String goal : gls) {
                if (mojo == null || mojo.equals(goal)) {
                    String params = doc.getField(PREFIX_FIELD_GOAL + goal).stringValue();
                    String[] lines = StringUtils.split(params, "\n"); //NOI18N
                    for (String line : lines) {
                        String[] paramDet = StringUtils.split(line, "|"); //NOI18N
                        String name = paramDet[0];
                        String editable = paramDet[1];
                        if ("true".equals(editable)) { //NOI18N
                            toRet.add(name);
                        }
                    }
                }
            }
        }
        return toRet;

    }

    /**
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param mojo
     * @return null if not found, else a set of parameter information objects
     * @throws java.lang.Exception
     */
    public static Set<ParameterDetail> getPluginParameters(String groupId, String artifactId, String version, String mojo) throws Exception {
        assert groupId != null && artifactId != null && version != null;
        IndexSearcher searcher = getIndexSearcher();
        String id = groupId + "|" + artifactId + "|" + version; //NOI18N
        TermQuery tq = new TermQuery(new Term(FIELD_ID, id));
        Hits hits = searcher.search(tq);
        if (hits.length() == 0) {
            return null;
        }
        Iterator it = hits.iterator();
        TreeSet<ParameterDetail> toRet = new TreeSet<ParameterDetail>(new PComparator());
        while (it.hasNext()) { //well should be just one anyway..
            Hit hit = (Hit) it.next();
            Document doc = hit.getDocument();
            String goals = doc.getField(FIELD_GOALS).stringValue();
            String[] gls = StringUtils.split(goals, " "); //NOI18N
            for (String goal : gls) {
                if (mojo == null || mojo.equals(goal)) {
                    String params = doc.getField(PREFIX_FIELD_GOAL + goal).stringValue();
                    String[] lines = StringUtils.split(params, "\n"); //NOI18N
                    for (String line : lines) {
                        String[] paramDet = StringUtils.split(line, "|"); //NOI18N
                        String name = paramDet[0];
                        String editable = paramDet[1];
                        String required = paramDet[2];
                        boolean req = "true".equals(required); //NOI18N
                        if ("true".equals(editable)) { //NOI18N
                            String expr = paramDet[3];
                            if (expr != null && "null".equals(expr)) { //NOI18N
                                expr = null;
                            }
                            String defVal = paramDet[4];
                            if (defVal != null && "null".equals(defVal)) { //NOI18N
                                defVal = null;
                            }
                            String desc;
                            if (paramDet.length > 5) {
                                desc = paramDet[5];
                                byte[] dec = Base64.decodeBase64(desc.getBytes());
                                desc =  new String(dec, "UTF-8"); //NOI18N
                            } else {
                                desc = null;
                            }
                            ParameterDetail pm = new ParameterDetail(name, expr, defVal, req, desc);
                            toRet.add(pm);
                        }
                    }
                }
            }
        }
        return toRet;
    }


    /**
     * find the plugins which are behind the given goal prefix.
     * @param prefix
     * @return A string composed of groupId and artifactId and version separated by "|"
     * @throws java.lang.Exception
     */
    public static Set<String> getPluginsForGoalPrefix(String prefix) throws Exception {
        assert prefix != null;
        IndexSearcher searcher = getIndexSearcher();
        TermQuery tq = new TermQuery(new Term(FIELD_PREFIX, prefix));
        Hits hits = searcher.search(tq);
        if (hits.length() == 0) {
            return null;
        }
        Iterator it = hits.iterator();
        TreeSet<String> toRet = new TreeSet<String>();
        while (it.hasNext()) { //well should be just one anyway..
            Hit hit = (Hit) it.next();
            Document doc = hit.getDocument();
            String id = doc.getField(FIELD_ID).stringValue();
            toRet.add(id);
        }
        return toRet;
    }

    /**
     * find the phase associations for the given packaging
     * @param packaging
     * @param mvnVersion
     * @param extensionPlugins
     * @return key= phase name, value - Set of Strings, where Strings are in format groupId:artifactId:mojo
     * @throws java.lang.Exception
     */
    public static Map<String, List<String>> getLifecyclePlugins(String packaging, String mvnVersion, String[] extensionPlugins) throws Exception {
        assert packaging != null;
        IndexSearcher searcher = getIndexSearcher();
        BooleanQuery bq = new BooleanQuery();
        TermQuery tq = new TermQuery(new Term(FIELD_CYCLES, packaging));
        bq.add(tq, BooleanClause.Occur.MUST);
        if (mvnVersion == null) {
            mvnVersion = "2.0.9"; //oh well we need something.. //NOI18N
        }
        BooleanQuery bq2 = new BooleanQuery();
        tq = new TermQuery(new Term(FIELD_MVN_VERSION, mvnVersion));
        bq2.add(tq, BooleanClause.Occur.SHOULD);

        for (String ext : extensionPlugins) {
            tq = new TermQuery(new Term(FIELD_ID, ext));
            bq2.add(tq, BooleanClause.Occur.SHOULD);
        }
        bq.add(bq2, BooleanClause.Occur.SHOULD); //why doesn't MUST work?

        Hits hits = searcher.search(bq);
        if (hits.length() == 0) {
            return null;
        }
        Iterator it = hits.iterator();
        LinkedHashMap<String, List<String>> toRet = new LinkedHashMap<String, List<String>>();
        while (it.hasNext()) { //well should be just one anyway..
            Hit hit = (Hit) it.next();
            Document doc = hit.getDocument();
            Field prefixed = doc.getField(PREFIX_FIELD_CYCLE + packaging);
            if (prefixed != null) {
                String mapping = prefixed.stringValue();
                String[] phases = StringUtils.split(mapping, "\n"); //NOI18N
                for (String phase : phases) {
                    String[] ph = StringUtils.split(phase, "="); //NOI18N
                    String[] plugins = StringUtils.split(ph[1], ","); //NOI18N
                    List<String> plgs = new ArrayList<String>(Arrays.asList(plugins));
                    toRet.put(ph[0], plgs);
                }
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
            is = PluginIndexManager.class.getClassLoader().getResourceAsStream(ZIP_LOCATION); //NOI18N
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
            File root = FileUtil.toFile(FileUtil.getConfigRoot());
            cacheDir = new File(root, INDEX_PATH);//NOI18N
        }
        cacheDir.mkdirs();
        File[] fls = cacheDir.listFiles();
        if (fls == null || fls.length == 0) {
            //copy the preexisting index in module into place..
            InputStream is = null;
            try {
                is = PluginIndexManager.class.getClassLoader().getResourceAsStream(ZIP_LOCATION); //NOI18N
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
                        is = PluginIndexManager.class.getClassLoader().getResourceAsStream(ZIP_LOCATION); //NOI18N
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

    private static class PComparator implements Comparator<ParameterDetail> {
        public int compare(ParameterDetail o1, ParameterDetail o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * Detailed information about a given parameter
     */
    public static class ParameterDetail {
        private String name;
        private String expression;
        private String defaultValue;
        private boolean required;
        private String description;

        public ParameterDetail(String name, String expression, String defaultValue, boolean required, String description) {
            this.name = name;
            this.expression = expression;
            this.defaultValue = defaultValue;
            this.required = required;
            this.description = description;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public String getExpression() {
            return expression;
        }

        public String getName() {
            return name;
        }

        public boolean isRequired() {
            return required;
        }

        public String getHtmlDetails(boolean includeName) {
            return "<html><body>" + (includeName ? ("<h4>" + NbBundle.getMessage(PluginIndexManager.class, "TXT_LBL_PARAMETER") + getName() + "</h4>") : "") +
            "<b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_Expression") + "</b>" +  (getExpression() != null ? ("${" + getExpression() + "}") : NbBundle.getMessage(PluginIndexManager.class, "LBL_Undefined")) + "<br>" +
            "<b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_DefaultValue") + "</b>" + (getDefaultValue() != null ? getDefaultValue() : NbBundle.getMessage(PluginIndexManager.class, "LBL_Undefined"))  +
            "<br><b>" + NbBundle.getMessage(PluginIndexManager.class, "LBL_Description") + "</b><br>"+ getDescription() + "</body></html>";
        }


    }

}
