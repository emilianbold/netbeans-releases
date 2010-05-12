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

package org.netbeans.modules.maven.grammar;

import hidden.org.codehaus.plexus.util.StringUtils;
import org.netbeans.modules.maven.indexer.api.PluginIndexManager;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.netbeans.modules.maven.grammar.spi.GrammarExtensionProvider;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * xml completion grammar based on xsd, additionally allowing more to be added.
 * @author Milos Kleint
 */
public class MavenProjectGrammar extends AbstractSchemaBasedGrammar {
    
    private static final String[] SCOPES = new String[] {
        "compile", //NOI18N
        "test", //NOI18N
        "runtime", //NOI18N
        "provided", //NOI18N
        "system" //NOI18N
    };

    private Set<String> groupCache;
    private RequestProcessor.Task groupTask;
    private Map<String, RequestProcessor.Task> artifactTasks = new HashMap<String, RequestProcessor.Task>();
    private Map<String, Set<String>> artifactCache = new HashMap<String, Set<String>>();
    private Map<String, RequestProcessor.Task> versionTasks = new HashMap<String, RequestProcessor.Task>();
    private Map<String, Set<String>> versionCache = new HashMap<String, Set<String>>();
    private final Object GROUP_LOCK = new Object();
    private final Object ARTIFACT_LOCK = new Object();
    private final Object VERSION_LOCK = new Object();


    public MavenProjectGrammar(GrammarEnvironment env) {
        super(env);
        groupTask = RequestProcessor.getDefault().create(new GroupTask());
        
    }
    
    protected InputStream getSchemaStream() {
        return getClass().getResourceAsStream("/org/netbeans/modules/maven/grammar/maven-4.0.0.xsd"); //NOI18N
    }
    
    @Override
    protected List<GrammarResult> getDynamicCompletion(String path, HintContext hintCtx, org.jdom.Element parent) {
        List<GrammarResult> result = null;
        if (path.endsWith("plugins/plugin/configuration") || //NOI18N
            path.endsWith("plugins/plugin/executions/execution/configuration")) { //NOI18N
            // assuming we have the configuration node as parent..
            // does not need to be true for complex stuff
            Node previous = path.indexOf("execution") > 0 //NOI18N
                ? hintCtx.getParentNode().getParentNode().getParentNode().getPreviousSibling()
                : hintCtx.getParentNode().getPreviousSibling();
            MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
            ArtifactInfoHolder info = findPluginInfo(previous, embedder, true);
            result = collectPluginParams(info, hintCtx);
            if (result == null) { //let the local processing geta changce
                               //once the index failed.
                Document pluginDoc = loadDocument(info, embedder);
                if (pluginDoc != null) {
                    result = collectPluginParams(pluginDoc, hintCtx);
                }
            }
        }

        GrammarExtensionProvider extProvider = Lookup.getDefault().lookup(GrammarExtensionProvider.class);
        if (extProvider != null) {
            List<GrammarResult> extResult = extProvider.getDynamicCompletion(path, hintCtx, parent);
            if (result == null) {
                result = extResult;
            } else {
                result.addAll(extResult);
            }
        }

        return result;
    }
    
    private ArtifactInfoHolder findArtifactInfo(Node previous) {
        ArtifactInfoHolder holder = new ArtifactInfoHolder();
        while (previous != null) {
            if (previous instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element el = (org.w3c.dom.Element)previous;
                NodeList lst = el.getChildNodes();
                if (lst.getLength() > 0) {
                    if ("artifactId".equals(el.getNodeName())) { //NOI18N
                        holder.setArtifactId(lst.item(0).getNodeValue());
                    }
                    if ("groupId".equals(el.getNodeName())) { //NOI18N
                        holder.setGroupId(lst.item(0).getNodeValue());
                    }
                    if ("version".equals(el.getNodeName())) { //NOI18N
                        holder.setVersion(lst.item(0).getNodeValue());
                    }
                }
            }
            previous = previous.getPreviousSibling();
        }
        return holder;
    }
    
    private ArtifactInfoHolder findPluginInfo(Node previous, MavenEmbedder embedder, boolean checkLocalRepo) {
        ArtifactInfoHolder holder = findArtifactInfo(previous);
        if (holder.getGroupId() == null) {
            holder.setGroupId("org.apache.maven.plugins"); //NOI18N
        }
        if (checkLocalRepo && (holder.getVersion() == null || "LATEST".equals(holder.getVersion()) || "RELEASE".equals(holder.getVersion()))  //NOI18N
                && holder.getArtifactId() != null && holder.getGroupId() != null) { //NOI18N
            File lev1 = new File(embedder.getLocalRepository().getBasedir(), holder.getGroupId().replace('.', File.separatorChar));
            File dir = new File(lev1, holder.getArtifactId());
            File fil = new File(dir, "maven-metadata-local.xml"); //NOI18N
            if (fil.exists()) {
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                try {
                    Metadata data = reader.read(new InputStreamReader(new FileInputStream(fil)));
                    if (data.getVersion() != null) {
                        holder.setVersion(data.getVersion());
                    } else {
                        Versioning vers = data.getVersioning();
                        if (vers != null) {
                            if ("LATEST".equals(holder.getVersion())) { //NOI18N
                                holder.setVersion(vers.getLatest());
                            }
                            if ("RELEASE".equals(holder.getVersion())) { //NOI18N
                                holder.setVersion(vers.getRelease());
                            }
                        }
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (XmlPullParserException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (holder.getVersion() == null) {
            holder.setVersion("RELEASE"); //NOI18N
        }
        
        return holder;
    }
    
    private List<GrammarResult> collectPluginParams(Document pluginDoc, HintContext hintCtx) {
        Iterator it = pluginDoc.getRootElement().getDescendants(new Filter() {
            public boolean matches(Object object) {
                if (object instanceof Element) {
                    Element el = (Element)object;
                    if ("parameter".equals(el.getName()) && //NOI18N
                            el.getParentElement() != null && "parameters".equals(el.getParentElement().getName()) && //NOI18N
                            el.getParentElement().getParentElement() != null && "mojo".equals(el.getParentElement().getParentElement().getName())) { //NOI18N
                        return true;
                    }
                }
                return false;
            }
        });
        List<GrammarResult> toReturn = new ArrayList<GrammarResult>();
        Collection<String> params = new HashSet<String>();
        while (it.hasNext()) {
            Element el = (Element)it.next();
            String editable = el.getChildText("editable"); //NOI18N
            if ("true".equalsIgnoreCase(editable)) { //NOI18N
                String name = el.getChildText("name"); //NOI18N
                if (name.startsWith(hintCtx.getCurrentPrefix()) && !params.contains(name)) {
                    params.add(name);
                    toReturn.add(new MyElement(name));
                }
            }
        }
        return toReturn;
    }

    private List<GrammarResult> collectPluginParams(ArtifactInfoHolder info, HintContext hintCtx) {
        Set<PluginIndexManager.ParameterDetail> params;
        try {
            params = PluginIndexManager.getPluginParameters(info.getGroupId(), info.getArtifactId(), info.getVersion(), null);
            if (params == null) {
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        List<GrammarResult> toReturn = new ArrayList<GrammarResult>();

        for (PluginIndexManager.ParameterDetail plg : params) {
            if (plg.getName().startsWith(hintCtx.getCurrentPrefix())) {
                MyElement me = new MyElement(plg.getName());
                me.setDescription(plg.getHtmlDetails(true));
                toReturn.add(me);
            }
        }
        return toReturn;
    }


    @Override
    protected Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, Element el) {
        if (path.endsWith("executions/execution/goals/goal")) { //NOI18N
            Node previous;
            // HACK.. if currentPrefix is zero length, the context is th element, otherwise it's the content inside
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                 previous = virtualTextCtx.getParentNode().getParentNode().getParentNode();
            } else {
                previous = virtualTextCtx.getParentNode().getParentNode().getParentNode().getParentNode();
            }
            previous = previous.getPreviousSibling();
            MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
            ArtifactInfoHolder info = findPluginInfo(previous, embedder, true);
            Enumeration<GrammarResult> res = collectGoals(info, virtualTextCtx);
            if (res == null) {
                Document pluginDoc = loadDocument(info, embedder);
                if (pluginDoc != null) {
                    return collectGoals(pluginDoc, virtualTextCtx);
                }
            } else {
                return res;
            }
        }
        if (path.endsWith("executions/execution/phase")) { //NOI18N
            MavenEmbedder embedder = EmbedderFactory.getOnlineEmbedder();
            @SuppressWarnings("unchecked")
            List<String> phases = embedder.getLifecyclePhases();
            return super.createTextValueList(phases.toArray(new String[phases.size()]), virtualTextCtx);
        }
        if (path.endsWith("dependencies/dependency/version") || //NOI18N
            path.endsWith("plugins/plugin/version") || //NOI18N
            path.endsWith("extensions/extension/version") || //NOI18N
            path.endsWith("/project/parent/version")) { //NOI18N
            
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findPluginInfo(previous, null, false);
            if (hold.getGroupId() != null && hold.getArtifactId() != null) {
                Set<String> verStrings = getVersions(hold.getGroupId(), hold.getArtifactId());
                Collection<GrammarResult> elems = new ArrayList<GrammarResult>();
                for (String vers : verStrings) {
                    if (vers.startsWith(virtualTextCtx.getCurrentPrefix())) {
                        elems.add(new MyTextElement(vers, virtualTextCtx.getCurrentPrefix()));
                    }
                }
                
                return Collections.enumeration(elems);
            }
        }
        if (path.endsWith("dependencies/dependency/groupId") || //NOI18N
            path.endsWith("extensions/extension/groupId")) {    //NOI18N
                Set<String> elems = getGroupIds();
                ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
                for (String elem : elems) {
                    if (elem.startsWith(virtualTextCtx.getCurrentPrefix())) {
                        texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                    }
                }
                return Collections.enumeration(texts);
            
        }
        if (path.endsWith("plugins/plugin/groupId")) { //NOI18N
            
                Set<String> elems = RepositoryQueries.filterPluginGroupIds(virtualTextCtx.getCurrentPrefix());
//                elems.addAll(getRelevant(virtualTextCtx.getCurrentPrefix(), getCachedPluginGroupIds()));
                ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
                for (String elem : elems) {
                    texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                }
                return Collections.enumeration(texts);
           
        }
        if (path.endsWith("dependencies/dependency/artifactId") || //NOI18N
            path.endsWith("extensions/extension/artifactId")) {    //NOI18N
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findArtifactInfo(previous);
            if (hold.getGroupId() != null) {
                    Set<String> elems = getArtifactIds(hold.getGroupId());
                    ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
                    String currprefix = virtualTextCtx.getCurrentPrefix();
                    for (String elem : elems) {
                        if (elem.startsWith(currprefix)) {
                            texts.add(new MyTextElement(elem, currprefix));
                        }
                    }
                    return Collections.enumeration(texts);
               
            }
        }
        if (path.endsWith("plugins/plugin/artifactId")) { //NOI18N
            //poor mans solution, just check local repository for possible versions..
            // in future would be nice to include remote repositories somehow..
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findArtifactInfo(previous);
            if (hold.getGroupId() != null) {
                Set<String> elems = RepositoryQueries.filterPluginArtifactIds(hold.getGroupId(), virtualTextCtx.getCurrentPrefix());
                ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
                for (String elem : elems) {
                    texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                }
                return Collections.enumeration(texts);
            }
        }
        
        if (path.endsWith("dependencies/dependency/scope")) { //NOI18N
            return super.createTextValueList(SCOPES, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/releases/updatePolicy") || //NOI18N
            path.endsWith("repositories/repository/snapshots/updatePolicy") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/releases/updatePolicy") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/snapshots/updatePolicy")) { //NOI18N
            return super.createTextValueList(MavenSettingsGrammar.UPDATE_POLICIES, virtualTextCtx);
        }
        if (path.endsWith("repository/releases/checksumPolicy") || //NOI18N
            path.endsWith("repository/snapshots/checksumPolicy") || //NOI18N
            path.endsWith("pluginRepository/releases/checksumPolicy") || //NOI18N
            path.endsWith("pluginRepository/snapshots/checksumPolicy")) { //NOI18N
            return super.createTextValueList(MavenSettingsGrammar.CHECKSUM_POLICIES, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/layout") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/layout") || //NOI18N
            path.endsWith("distributionManagement/repository/layout")) { //NOI18N
            return super.createTextValueList(MavenSettingsGrammar.LAYOUTS, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/url") || //NOI18N
            path.endsWith("pluginRepositories/pluginRepository/url") || //NOI18N
            path.endsWith("distributionManagement/repository/url")) { //NOI18N
            
            List<String> repoIds = getRepoUrls();   
            return super.createTextValueList(repoIds.toArray(new String[0]),
                    virtualTextCtx);
        }
        
        if (path.endsWith("modules/module")) { //NOI18N
            FileObject fo = getEnvironment().getFileObject();
            if (fo != null) {
                File dir = FileUtil.toFile(fo).getParentFile();  
                String prefix = virtualTextCtx.getCurrentPrefix();
                boolean endingSlash = prefix.endsWith("/");
                String[] elms = StringUtils.split(prefix, "/");
                String lastElement = "";
                for (int i = 0; i < elms.length; i++) {
                    if ("..".equals(elms[i])) { //NOI18N
                        dir = dir != null ? dir.getParentFile() : null;
                    } else if (i < elms.length - (endingSlash ? 0 : 1)) {
                        dir = dir != null ? new File(dir, elms[i]) : null;
                    } else {
                        lastElement = elms[i];
                    }
                }
                prefix = lastElement != null ? lastElement : prefix;
                if (dir == null || !dir.exists() || !dir.isDirectory()) {
                    return null;
                }
                
                File[] modules = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                         return pathname.isDirectory() && new File(pathname, "pom.xml").exists(); //NOI18N
                    }
                });
                Collection<GrammarResult> elems = new ArrayList<GrammarResult>();
                for (int i = 0; i < modules.length; i++) {
                    if (modules[i].getName().startsWith(prefix)) {
                        elems.add(new MyTextElement(modules[i].getName(), prefix));
                    }
                }
                return Collections.enumeration(elems);
            }
        }
        return null;
    }

    private Set<String> getGroupIds() {
        Set<String> elems = null;
        synchronized (GROUP_LOCK) {
            if (groupCache != null) {
                elems = groupCache;
            } else {
                if (!groupTask.isFinished()) {
                    groupTask.run();
                }
            }
        }
        if (elems == null) {
            groupTask.waitFinished();
        }
        synchronized (GROUP_LOCK) {
            if (groupCache != null) {
                elems = groupCache;
            } else {
                //can it happen?
                elems = Collections.<String>emptySet();
            }
        }
        return elems;
    }
    
    private Set<String> getArtifactIds(String groupId) {
        Set<String> elems = null;
        RequestProcessor.Task tsk = null;
        synchronized (ARTIFACT_LOCK) {
            tsk = artifactTasks.get(groupId);
            if (tsk == null) {
                tsk = RequestProcessor.getDefault().create(new ArtifactTask(groupId));
                artifactTasks.put(groupId, tsk);
            }
            Set<String> c = artifactCache.get(groupId);
            if (c != null) {
                elems = c;
            } else {
                if (!tsk.isFinished()) {
                    tsk.run();
                }
            }
        }
        if (elems == null) {
            tsk.waitFinished();
        }
        synchronized (ARTIFACT_LOCK) {
            Set<String> c = artifactCache.get(groupId);
            elems = c != null ? c : Collections.<String>emptySet();
        }
        return elems;
    }
    
    private Set<String> getVersions(String groupId, String artifactId) {
        Set<String> elems = null;
        RequestProcessor.Task tsk = null;
        String id = groupId + ":" + artifactId; //NOI18N
        synchronized (VERSION_LOCK) {
            tsk = versionTasks.get(id);
            if (tsk == null) {
                tsk = RequestProcessor.getDefault().create(new VersionTask(groupId, artifactId));
                versionTasks.put(id, tsk);
            }
            Set<String> c = versionCache.get(id);
            if (c != null) {
                elems = c;
            } else {
                if (!tsk.isFinished()) {
                    tsk.run();
                }
            }
        }
        if (elems == null) {
            tsk.waitFinished();
        }
        synchronized (VERSION_LOCK) {
            Set<String> c = versionCache.get(id);
            elems = c != null ? c : Collections.<String>emptySet();
        }
        return elems;
    }
    
    
  /*Return repo url's*/
    private List<String> getRepoUrls() {
        List<String> repos = new ArrayList<String>();

        List<RepositoryInfo> ris = RepositoryPreferences.getInstance().getRepositoryInfos();
        for (RepositoryInfo ri : ris) {
            if(ri.getRepositoryUrl()!=null){
             repos.add(ri.getRepositoryUrl());
            }
        }

        return repos;

    }
    private Document loadDocument(ArtifactInfoHolder info, MavenEmbedder embedder) {
        if (info.getArtifactId() != null && info.getGroupId() != null && info.getVersion() != null) {
            Artifact art = embedder.createArtifact(info.getGroupId(), info.getArtifactId(), info.getVersion(), null, "jar"); //NOI18N
            String repopath = embedder.getLocalRepository().pathOf(art);
            File fil = new File(embedder.getLocalRepository().getBasedir(), repopath);
            if (fil.exists()) {
                try {
                    JarFile jf = new JarFile(fil);
                    JarEntry entry = jf.getJarEntry("META-INF/maven/plugin.xml"); //NOI18N
                    if (entry != null) {
                        InputStream str = jf.getInputStream(entry);
                        SAXBuilder builder = new SAXBuilder();
                        return builder.build(str);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (JDOMException ex) {
                    ex.printStackTrace();
                }
            }
            
        }
        return null;
    }

    private Enumeration<GrammarResult> collectGoals(Document pluginDoc, HintContext virtualTextCtx) {
        @SuppressWarnings("unchecked")
        Iterator<Element> it = pluginDoc.getRootElement().getDescendants(new Filter() {
            public boolean matches(Object object) {
                if (object instanceof Element) {
                    Element el = (Element)object;
                    if ("goal".equals(el.getName()) && //NOI18N
                            el.getParentElement() != null && "mojo".equals(el.getParentElement().getName())) { //NOI18N
                        return true;
                    }
                }
                return false;
            }
        });
        Collection<GrammarResult> toReturn = new ArrayList<GrammarResult>();
        while (it.hasNext()) {
            Element el = it.next();
            String name = el.getText();
            if (name.startsWith(virtualTextCtx.getCurrentPrefix())) {
               toReturn.add(new MyTextElement(name, virtualTextCtx.getCurrentPrefix()));
            }
        }
        return Collections.enumeration(toReturn);
    }

    private Enumeration<GrammarResult> collectGoals(ArtifactInfoHolder info, HintContext virtualTextCtx) {

        if (info.getGroupId() == null || info.getArtifactId() == null || info.getVersion() == null) {
            //#159317
            return null;
        }
        @SuppressWarnings("unchecked")
        Set<String> goals;
        try {
            goals = PluginIndexManager.getPluginGoals(info.getGroupId(), info.getArtifactId(), info.getVersion());
            if (goals == null) {
                // let the document/local repository based collectGoals() get a chance.
                return null;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        Collection<GrammarResult> toReturn = new ArrayList<GrammarResult>();
        for (String name : goals) {
            if (name.startsWith(virtualTextCtx.getCurrentPrefix())) {
               toReturn.add(new MyTextElement(name, virtualTextCtx.getCurrentPrefix()));
            }
        }
        return Collections.enumeration(toReturn);
    }

    
    
    private static class ArtifactInfoHolder  {
        private String artifactId;
        private String groupId;
        private String version;
        
        public String getArtifactId() {
            return artifactId;
        }
        
        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }
        
        public String getGroupId() {
            return groupId;
        }
        
        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
    }
    
    private class GroupTask implements Runnable {

        public void run() {
            Set<String> elems = RepositoryQueries.getGroups();
            synchronized (GROUP_LOCK) {
                MavenProjectGrammar.this.groupCache = elems;
            }
        }
    }
            
    private class ArtifactTask implements Runnable {
        private String groupId;

        ArtifactTask(String groupId) {
            this.groupId = groupId;
        }
        
        public void run() {
            Set<String> elems = RepositoryQueries.getArtifacts(groupId);
            synchronized (ARTIFACT_LOCK) {
                MavenProjectGrammar.this.artifactCache.put(groupId, elems);
            }
        }
    }

    private class VersionTask implements Runnable {
        private String groupId;
        private String artifactId;

        VersionTask(String groupId, String art) {
            this.groupId = groupId;
            artifactId = art;
        }
        
        public void run() {
            List<NBVersionInfo> infos = RepositoryQueries.getVersions(groupId, artifactId);
            Set<String> elems = new TreeSet<String>();
            for (NBVersionInfo inf : infos) {
                elems.add(inf.getVersion());
            }
            synchronized (VERSION_LOCK) {
                MavenProjectGrammar.this.versionCache.put(groupId + ":" + artifactId, elems); //NOI18N
            }
        }
        
    }
    
}
