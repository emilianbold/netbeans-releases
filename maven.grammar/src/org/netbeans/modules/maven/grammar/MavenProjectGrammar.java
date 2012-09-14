/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar;
import org.netbeans.modules.maven.grammar.spi.GrammarExtensionProvider;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.PluginIndexManager;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
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
    
    private static final Logger LOG = Logger.getLogger(MavenProjectGrammar.class.getName());
    private static final String[] SCOPES = new String[] {
        "compile", //NOI18N
        "test", //NOI18N
        "runtime", //NOI18N
        "provided", //NOI18N
        "system" //NOI18N
    };

    private Set<String> groupCache;
    private boolean groupCachePartial = false;
    private RequestProcessor.Task groupTask;
    private Map<String, RequestProcessor.Task> artifactTasks = new HashMap<String, RequestProcessor.Task>();
    private Map<String, Set<String>> artifactCache = new HashMap<String, Set<String>>();
    private Set<String> artifactsPartialCache = new HashSet<String>();
    private Map<String, RequestProcessor.Task> versionTasks = new HashMap<String, RequestProcessor.Task>();
    private Map<String, Set<String>> versionCache = new HashMap<String, Set<String>>();
    private Set<String> versionPartialCache = new HashSet<String>();
    private final Map<String,RequestProcessor.Task> classifierTasks = new HashMap<String,RequestProcessor.Task>();
    private final Map<String,Set<String>> classifierCache = new HashMap<String,Set<String>>();
    private Set<String> classifierPartialCache = new HashSet<String>();
    private final Object GROUP_LOCK = new Object();
    private final Object ARTIFACT_LOCK = new Object();
    private final Object VERSION_LOCK = new Object();
    private final Object CLASSIFIER_LOCK = new Object();
    private static RequestProcessor RP = new RequestProcessor(MavenProjectGrammar.class.getName(), 3);
    private final Project owner;


    MavenProjectGrammar(GrammarEnvironment env, Project owner) {
        super(env);
        this.owner = owner;
        groupTask = RP.create(new GroupTask());
    }
    
    @Override
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
        if (holder.getVersion() != null && holder.getVersion().contains("${")) {
            //cannot do anything with unresolved value, clear and hope for the best
            holder.setVersion(null);
        }
        if (holder.getVersion() == null && holder.getGroupId() != null && holder.getArtifactId() != null) {
            NbMavenProject prj = owner.getLookup().lookup(NbMavenProject.class);
            if (prj != null) {
                for (Plugin a : prj.getMavenProject().getBuildPlugins()) {
                    if (holder.getGroupId().equals(a.getGroupId()) && holder.getArtifactId().equals(a.getArtifactId())) {
                        holder.setVersion(a.getVersion());
                        break;
                    } 
                }
                if (holder.getVersion() == null) {
                    PluginManagement man = prj.getMavenProject().getPluginManagement();
                    if (man != null) {
                        for (Plugin p : man.getPlugins()) {
                            if (holder.getGroupId().equals(p.getGroupId()) && holder.getArtifactId().equals(p.getArtifactId())) {
                                holder.setVersion(p.getVersion());
                                break;
                            } 
                        }
                    }
                }
            }
        }
        if (checkLocalRepo && (holder.getVersion() == null || "LATEST".equals(holder.getVersion()) || "RELEASE".equals(holder.getVersion()))  //NOI18N
                && holder.getArtifactId() != null && holder.getGroupId() != null) { //NOI18N
            File lev1 = new File(embedder.getLocalRepositoryFile(), holder.getGroupId().replace('.', File.separatorChar));
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
                    LOG.log(Level.FINER, "", ex);
                } catch (XmlPullParserException ex) {
                    LOG.log(Level.FINER, "", ex);
                } catch (IOException ex) {
                    LOG.log(Level.FINER, "", ex);
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
            @Override
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
        if (virtualTextCtx.getCurrentPrefix().length() > 0) {
            String prefix = virtualTextCtx.getCurrentPrefix();
            if (prefix.lastIndexOf("${") > prefix.lastIndexOf("}")) {
                String propPrefix = prefix.substring(prefix.lastIndexOf("${") + 2);
                FileObject fo = getEnvironment().getFileObject();
                if (fo != null) {
                    List<String> set = new ArrayList<String>();
                    set.add("basedir");
                    set.add("project.build.finalName");
                    set.add("project.version");
                    set.add("project.groupId");
                    Project p;
                    try {
                        p = ProjectManager.getDefault().findProject(fo.getParent());
                        if (p != null) {
                            NbMavenProject nbprj = p.getLookup().lookup(NbMavenProject.class);
                            if (nbprj != null) {
                                Properties props = nbprj.getMavenProject().getProperties();
                                if (props != null) {
                                    set.addAll(props.stringPropertyNames());
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                    Collection<GrammarResult> elems = new ArrayList<GrammarResult>();
                    Collections.sort(set);
                    for (String pr : set) {
                        if (pr.startsWith(propPrefix)) {
                            elems.add(new ExpressionValueTextElement(pr, propPrefix));
                        }
                    }
                    return Collections.enumeration(elems);
                }
            }
        }
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
                synchronized (VERSION_LOCK) {
                    if (versionPartialCache.contains(hold.getGroupId() + ":" + hold.getArtifactId())) {
                        elems.add(new PartialTextElement());
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
                synchronized (GROUP_LOCK) {
                    if (groupCachePartial) {
                        texts.add(new PartialTextElement());
                    }
                }
                return Collections.enumeration(texts);
            
        }
        if (path.endsWith("plugins/plugin/groupId")) { //NOI18N
                Result<String> result = RepositoryQueries.filterPluginGroupIdsResult(virtualTextCtx.getCurrentPrefix(), RepositoryPreferences.getInstance().getRepositoryInfos());
//                elems.addAll(getRelevant(virtualTextCtx.getCurrentPrefix(), getCachedPluginGroupIds()));
                ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
                for (String elem : result.getResults()) {
                    texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                }
                if (result.isPartial()) {
                    texts.add(new PartialTextElement());
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
                    synchronized (ARTIFACT_LOCK) {
                        if (artifactsPartialCache.contains(hold.getGroupId())) {
                            texts.add(new PartialTextElement());
                        }
                    }
                    return Collections.enumeration(texts);
               
            }
        }
        if (path.endsWith("dependencies/dependency/classifier")) { // #200852
            Node previous;
            if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                previous = virtualTextCtx.getPreviousSibling();
            } else {
                previous = virtualTextCtx.getParentNode().getPreviousSibling();
            }
            ArtifactInfoHolder hold = findArtifactInfo(previous);
            if (hold.getGroupId() != null && hold.getArtifactId() != null && hold.getVersion() != null) {
                Set<String> elems = getClassifiers(hold.getGroupId(), hold.getArtifactId(), hold.getVersion());
                List<GrammarResult> texts = new ArrayList<GrammarResult>();
                String currprefix = virtualTextCtx.getCurrentPrefix();
                for (String elem : elems) {
                    if (elem.startsWith(currprefix)) {
                        texts.add(new MyTextElement(elem, currprefix));
                    }
                }
                synchronized (CLASSIFIER_LOCK) {
                    String id = hold.getGroupId() + ':' + hold.getArtifactId() + ':' + hold.getVersion();
                    if (classifierPartialCache.contains(id)) {
                        texts.add(new PartialTextElement());
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
                Result<String> result = RepositoryQueries.filterPluginArtifactIdsResult(hold.getGroupId(), virtualTextCtx.getCurrentPrefix(), RepositoryPreferences.getInstance().getRepositoryInfos());
                ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
                for (String elem : result.getResults()) {
                    texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
                }
                if (result.isPartial()) {
                    texts.add(new PartialTextElement());
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

    //XXX: mkleint I think this whole caching logic can go..
    private Set<String> getGroupIds() {
        Set<String> elems = null;
        synchronized (GROUP_LOCK) {
            if (groupCache != null) {
                elems = groupCache;
                //for partial results attempt re-query, do not wait for results
                if (groupCachePartial) {
                    groupTask.schedule(500);
                }
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
    
    //XXX: mkleint I think this whole caching logic can go..
    private Set<String> getArtifactIds(String groupId) {
        Set<String> elems = null;
        RequestProcessor.Task tsk;
        synchronized (ARTIFACT_LOCK) {
            tsk = artifactTasks.get(groupId);
            if (tsk == null) {
                tsk = RP.create(new ArtifactTask(groupId));
                artifactTasks.put(groupId, tsk);
            }
            Set<String> c = artifactCache.get(groupId);
            if (c != null) {
                elems = c;
                //for partial results attempt re-query, do not wait for results
                if (artifactsPartialCache.contains(groupId)) {
                    tsk.schedule(200);
                }
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
    
    //XXX: mkleint I think this whole caching logic can go..
    private Set<String> getVersions(String groupId, String artifactId) {
        Set<String> elems = null;
        RequestProcessor.Task tsk;
        String id = groupId + ":" + artifactId; //NOI18N
        synchronized (VERSION_LOCK) {
            tsk = versionTasks.get(id);
            if (tsk == null) {
                tsk = RP.create(new VersionTask(groupId, artifactId));
                versionTasks.put(id, tsk);
            }
            Set<String> c = versionCache.get(id);
            if (c != null) {
                elems = c;
                //for partial results attempt re-query, do not wait for results
                if (versionPartialCache.contains(groupId)) {
                    tsk.schedule(200);
                }
                
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

    //XXX: mkleint I think this whole caching logic can go..
    private Set<String> getClassifiers(String groupId, String artifactId, String version) {
        Set<String> elems = null;
        RequestProcessor.Task tsk;
        String id = groupId + ':' + artifactId + ':' + version;
        synchronized (CLASSIFIER_LOCK) {
            tsk = classifierTasks.get(id);
            if (tsk == null) {
                tsk = RP.create(new ClassifierTask(groupId, artifactId, version));
                classifierTasks.put(id, tsk);
            }
            Set<String> c = classifierCache.get(id);
            if (c != null) {
                elems = c;
                //for partial results attempt re-query, do not wait for results
                if (classifierPartialCache.contains(groupId)) {
                    tsk.schedule(200);
                }
            } else {
                if (!tsk.isFinished()) {
                    tsk.run();
                }
            }
        }
        if (elems == null) {
            tsk.waitFinished();
        }
        synchronized (CLASSIFIER_LOCK) {
            Set<String> c = classifierCache.get(id);
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
            File fil = new File(embedder.getLocalRepositoryFile(), repopath);
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
                    LOG.log(Level.FINER, "", ex);
                } catch (JDOMException ex) {
                    LOG.log(Level.FINER, "", ex);
                }
            }
            
        }
        return null;
    }

    private Enumeration<GrammarResult> collectGoals(Document pluginDoc, HintContext virtualTextCtx) {
        @SuppressWarnings("unchecked")
        Iterator<Element> it = pluginDoc.getRootElement().getDescendants(new Filter() {
            @Override
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

        @Override
        public void run() {
            Result<String> result = RepositoryQueries.getGroupsResult(RepositoryPreferences.getInstance().getRepositoryInfos());
            Set<String> set = new TreeSet<String>(result.getResults());
            synchronized (GROUP_LOCK) {
                MavenProjectGrammar.this.groupCache = set;
                MavenProjectGrammar.this.groupCachePartial = result.isPartial();
            }
        }
    }
            
    private class ArtifactTask implements Runnable {
        private String groupId;

        ArtifactTask(String groupId) {
            this.groupId = groupId;
        }
        
        @Override
        public void run() {
            Result<String> result = RepositoryQueries.getArtifactsResult(groupId, RepositoryPreferences.getInstance().getRepositoryInfos());
            Set<String> elems = new TreeSet<String>(result.getResults());
            synchronized (ARTIFACT_LOCK) {
                MavenProjectGrammar.this.artifactCache.put(groupId, elems);
                if (result.isPartial()) {
                    MavenProjectGrammar.this.artifactsPartialCache.add(groupId);
                } else {
                    MavenProjectGrammar.this.artifactsPartialCache.remove(groupId);
                }
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
        
        @Override
        public void run() {
            Result<NBVersionInfo> result = RepositoryQueries.getVersionsResult(groupId, artifactId, RepositoryPreferences.getInstance().getRepositoryInfos());
            Set<String> elems = new LinkedHashSet<String>();
            for (NBVersionInfo inf : result.getResults()) {
                elems.add(inf.getVersion());
            }
            synchronized (VERSION_LOCK) {
                final String key = groupId + ":" + artifactId;
                MavenProjectGrammar.this.versionCache.put(key, elems); //NOI18N
                if (result.isPartial()) {
                    MavenProjectGrammar.this.versionPartialCache.add(key);
                } else {
                    MavenProjectGrammar.this.versionPartialCache.remove(key);
                }
            }
        }
        
    }
    
    private class ClassifierTask implements Runnable {
        private final String groupId;
        private final String artifactId;
        private final String version;
        ClassifierTask(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
        @Override public void run() {
            Result<NBVersionInfo> result = RepositoryQueries.getRecordsResult(groupId, artifactId, version, RepositoryPreferences.getInstance().getRepositoryInfos());
            Set<String> elems = new LinkedHashSet<String>();
            for (NBVersionInfo inf : result.getResults()) {
                if (inf.getClassifier() != null) {
                    elems.add(inf.getClassifier());
                }
            }
            synchronized (CLASSIFIER_LOCK) {
                final String key = groupId + ':' + artifactId + ':' + version;
                classifierCache.put(key, elems);
                if (result.isPartial()) {
                    MavenProjectGrammar.this.classifierPartialCache.add(key);
                } else {
                    MavenProjectGrammar.this.classifierPartialCache.remove(key);
                }
                
            }
        }
    }

}
