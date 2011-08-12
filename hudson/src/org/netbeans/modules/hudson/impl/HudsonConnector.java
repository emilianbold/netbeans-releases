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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import static org.netbeans.modules.hudson.constants.HudsonJobConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonXmlApiConstants.*;
import static org.netbeans.modules.hudson.impl.Bundle.*;
import org.netbeans.modules.hudson.api.Utilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Hudson Server Connector
 *
 * @author Michal Mocnak
 */
public class HudsonConnector {
    private static final Logger LOG = Logger.getLogger(HudsonConnector.class.getName());
    
    private HudsonInstanceImpl instance;
    
    private HudsonVersion version;
    private boolean connected = false;
    /** #182689: true if have no anon access and need to log in just to see job list */
    boolean forbidden;
    
    private Map<String, HudsonView> cache = new HashMap<String, HudsonView>();
    
    /**
     * Creates a new instance of HudsonConnector
     *
     * @param HudsonInstance
     */
    public HudsonConnector(HudsonInstanceImpl instance) {
        this.instance = instance;
    }
    
    private boolean canUseTree(boolean authentication) {
        HudsonVersion v = getHudsonVersion(authentication);
        return v != null && v.compareTo(new HudsonVersion("1.367")) >= 0; // NOI18N
    }
    
    public synchronized Collection<HudsonJob> getAllJobs(boolean authentication) {
        Document docInstance = getDocument(instance.getUrl() + XML_API_URL + (canUseTree(authentication) ?
                "?tree=primaryView[name],views[name,url,jobs[name]]," +
                "jobs[name,url,color,displayName,buildable,inQueue," +
                "lastBuild[number],lastFailedBuild[number],lastStableBuild[number],lastSuccessfulBuild[number],lastCompletedBuild[number]," +
                "modules[name,displayName,url,color]]," +
                "securedJobs[name,url]" : // HUDSON-3924
                "?depth=1&xpath=/&exclude=//assignedLabel&exclude=//primaryView/job" +
                "&exclude=//view/job/url&exclude=//view/job/color&exclude=//description&exclude=//job/build&exclude=//healthReport" +
                "&exclude=//firstBuild&exclude=//keepDependencies&exclude=//nextBuildNumber&exclude=//property&exclude=//action" +
                "&exclude=//upstreamProject&exclude=//downstreamProject&exclude=//queueItem&exclude=//scm&exclude=//concurrentBuild" +
                "&exclude=//job/lastUnstableBuild&exclude=//job/lastUnsuccessfulBuild"), authentication); // NOI18N
        
        if (null == docInstance)
            return new ArrayList<HudsonJob>();
        
        // Clear cache
        cache.clear();
        
        configureViews(instance, docInstance);
        
        // Parse jobs and return them
        return getJobs(docInstance);
    }

    @Messages("MSG_Starting=Starting {0}")
    public synchronized void startJob(final HudsonJob job) {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                MSG_Starting(job.getName()));
        handle.start();
        try {
            new ConnectionBuilder().instance(instance).url(job.getUrl() + "build").postData("delay=0sec".getBytes()).followRedirects(false).connection(); // NOI18N
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not start {0}: {1}", new Object[] {job, e});
        } finally {
            handle.finish();
        }
        instance.synchronize(false);
    }

    /**
     * Gets general information about a build.
     * The changelog ({@code <changeSet>}) can be interpreted separately by {@link HudsonJobBuild#getChanges}.
     */
    Collection<? extends HudsonJobBuild> getBuilds(HudsonJobImpl job) {
        Document docBuild = getDocument(job.getUrl() + XML_API_URL + (canUseTree(true) ?
            "?tree=builds[number,result,building]" :
            "?xpath=/*/build&wrapper=root&exclude=//url"), true);
        if (docBuild == null) {
            return Collections.emptySet();
        }
        List<HudsonJobBuildImpl> builds = new ArrayList<HudsonJobBuildImpl>();
        NodeList buildNodes = docBuild.getElementsByTagName("build"); // NOI18N // HUDSON-3267: might be root elt
        for (int i = 0; i < buildNodes.getLength(); i++) {
            Node build = buildNodes.item(i);
            int number = 0;
            boolean building = false;
            Result result = null;
            NodeList details = build.getChildNodes();
            for (int j = 0; j < details.getLength(); j++) {
                Node detail = details.item(j);
                if (detail.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String nodeName = detail.getNodeName();
                Node firstChild = detail.getFirstChild();
                if (firstChild == null) {
                    LOG.warning("#170267: unexpected empty <build> child: " + nodeName);
                    continue;
                }
                String text = firstChild.getTextContent();
                if (nodeName.equals("number")) { // NOI18N
                    number = Integer.parseInt(text);
                } else if (nodeName.equals("building")) { // NOI18N
                    building = Boolean.valueOf(text);
                } else if (nodeName.equals("result")) { // NOI18N
                    result = Result.valueOf(text);
                } else {
                    LOG.warning("unexpected <build> child: " + nodeName);
                }
            }
            builds.add(new HudsonJobBuildImpl(this, job, number, building, result));
        }
        return builds;
    }

    void loadResult(HudsonJobBuildImpl build, AtomicBoolean building, AtomicReference<Result> result) {
        Document doc = getDocument(build.getUrl() + XML_API_URL +
                "?xpath=/*/*[name()='result'%20or%20name()='building']&wrapper=root", true);
        if (doc == null) {
            return;
        }
        Element docEl = doc.getDocumentElement();
        Element resultEl = XMLUtil.findElement(docEl, "result", null);
        if (resultEl != null) {
            result.set(Result.valueOf(XMLUtil.findText(resultEl)));
        }
        Element buildingEl = XMLUtil.findElement(docEl, "building", null);
        if (buildingEl != null) {
            building.set(Boolean.parseBoolean(XMLUtil.findText(buildingEl)));
        }
    }
    
    protected synchronized @CheckForNull HudsonVersion getHudsonVersion(boolean authentication) {
        if (version == null) {
            version = retrieveHudsonVersion(authentication);
        }
        return version;
    }
    
    protected boolean isConnected() {
        return connected;
        
    }
    
    private void configureViews(HudsonInstanceImpl instance, Document doc) {
        String primaryViewName = null;
        Element primaryViewEl = XMLUtil.findElement(doc.getDocumentElement(), "primaryView", null); // NOI18N
        if (primaryViewEl != null) {
            Element nameEl = XMLUtil.findElement(primaryViewEl, "name", null); // NOI18N
            if (nameEl != null) {
                primaryViewName = XMLUtil.findText(nameEl);
            }
        }
        
        Collection<HudsonView> views = new ArrayList<HudsonView>();
        HudsonView primaryView = null;

        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!n.getNodeName().equals(XML_API_VIEW_ELEMENT)) {
                continue;
            }
            
            String name = null;
            String url = null;
            boolean isPrimary = false;
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                if (o.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                    name = o.getFirstChild().getTextContent();
                    isPrimary = name.equals(primaryViewName);
                } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                    url = normalizeUrl(o.getFirstChild().getTextContent(), isPrimary ? "" : "view/[^/]+/"); // NOI18N
                }
            }
            
            if (null != name && null != url) {
                Element docView = (Element) n;
                
                HudsonViewImpl view = new HudsonViewImpl(instance, name, url);
                
                NodeList jobsList = docView.getElementsByTagName(XML_API_JOB_ELEMENT);
                for (int k = 0; k < jobsList.getLength(); k++) {
                    Node d = jobsList.item(k);
                    for (int l = 0; l < d.getChildNodes().getLength(); l++) {
                        Node e = d.getChildNodes().item(l);
                        if (e.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        String nodeName = e.getNodeName();
                        if (nodeName.equals(XML_API_NAME_ELEMENT)) {
                            cache.put(view.getName() + "/" + e.getFirstChild().getTextContent(), view); // NOI18N
                        } else {
                            LOG.fine("unexpected view <job> child: " + nodeName);
                        }
                    }
                }
                
                views.add(view);
                if (isPrimary) {
                    primaryView = view;
                }
            }
            
        }
        
        instance.setViews(views, primaryView);
    }
    
    private Collection<HudsonJob> getJobs(Document doc) {
        Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
        
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            boolean secured = n.getNodeName().equals(XML_API_SECURED_JOB_ELEMENT);
            if (!n.getNodeName().equals(XML_API_JOB_ELEMENT) && !secured) {
                continue;
            }
            
            HudsonJobImpl job = new HudsonJobImpl(instance);
            if (secured) {
                job.putProperty(JOB_COLOR, Color.secured);
            }
            
            NodeList jobDetails = n.getChildNodes();
            for (int k = 0; k < jobDetails.getLength(); k++) {
                Node d = jobDetails.item(k);
                if (d.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String nodeName = d.getNodeName();
                if (nodeName.equals(XML_API_NAME_ELEMENT)) {
                    job.putProperty(JOB_NAME, d.getFirstChild().getTextContent());
                } else if (nodeName.equals(XML_API_URL_ELEMENT)) {
                    job.putProperty(JOB_URL, normalizeUrl(d.getFirstChild().getTextContent(), "job/[^/]+/")); // NOI18N
                } else if (nodeName.equals(XML_API_COLOR_ELEMENT)) {
                    String color = d.getFirstChild().getTextContent().trim();
                    try {
                        job.putProperty(JOB_COLOR, Color.valueOf(color));
                    } catch (IllegalArgumentException x) {
                        Exceptions.attachMessage(x,
                                "http://www.netbeans.org/nonav/issues/show_bug.cgi?id=126166 - no Color value '" +
                                color + "' among " + Arrays.toString(Color.values())); // NOI18N
                        Exceptions.printStackTrace(x);
                        job.putProperty(JOB_COLOR, Color.red_anime);
                    }
                } else if (nodeName.equals(XML_API_DISPLAY_NAME_ELEMENT)) {
                    job.putProperty(JOB_DISPLAY_NAME, d.getFirstChild().getTextContent());
                } else if (nodeName.equals(XML_API_BUILDABLE_ELEMENT)) {
                    job.putProperty(JOB_BUILDABLE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_INQUEUE_ELEMENT)) {
                    job.putProperty(JOB_IN_QUEUE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_BUILD_ELEMENT)) {
                    job.putProperty(JOB_LAST_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_FAILED_BUILD_ELEMENT)) {
                    job.putProperty(JOB_LAST_FAILED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_STABLE_BUILD_ELEMENT)) {
                    job.putProperty(JOB_LAST_STABLE_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT)) {
                    job.putProperty(JOB_LAST_SUCCESSFUL_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals(XML_API_LAST_COMPLETED_BUILD_ELEMENT)) {
                    job.putProperty(JOB_LAST_COMPLETED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                } else if (nodeName.equals("module")) { // NOI18N
                    String name = null, displayName = null, url = null;
                    Color color = null;
                    NodeList moduleDetails = d.getChildNodes();
                    for (int j = 0; j < moduleDetails.getLength(); j++) {
                        Node n2 = moduleDetails.item(j);
                        if (n2.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        String nodeName2 = n2.getNodeName();
                        Node firstChild = n2.getFirstChild();
                        if (firstChild != null) {
                            String text = firstChild.getTextContent();
                            if (nodeName2.equals("name")) { // NOI18N
                                name = text;
                            } else if (nodeName2.equals("displayName")) { // NOI18N
                                displayName = text;
                            } else if (nodeName2.equals("url")) { // NOI18N
                                url = normalizeUrl(text, "job/[^/]+/[^/]+/"); // NOI18N
                            } else if (nodeName2.equals("color")) { // NOI18N
                                color = Color.valueOf(text);
                            } else {
                                LOG.fine("unexpected <module> child: " + nodeName);
                            }
                        } else {
                            LOG.fine("#178360: unexpected empty <module> child: " + nodeName);
                        }
                    }
                    job.addModule(name, displayName, color, url);
                } else {
                    LOG.fine("unexpected global <job> child: " + nodeName);
                }
            }

            for (HudsonView v : instance.getViews()) {
                if (/* https://github.com/hudson/hudson/commit/105f2b09cf1376f9fe4dbf80c5bdb7a0d30ba1c1#commitcomment-447142 */secured ||
                        null != cache.get(v.getName() + "/" + job.getName())) // NOI18N
                    job.addView(v);
            }

            jobs.add(job);
        }
        
        return jobs;
    }

    /**
     * Try to fix up a URL as returned by Hudson's XML API.
     * @param suggested whatever {@code .../api/xml#//url} offered, e.g. {@code http://localhost:9999/job/My%20Job/}
     * @param relativePattern regex for the expected portion of the URL relative to server root, e.g. {@code job/[^/]+/}
     * @return analogous URL constructed from instance root, e.g. {@code https://my.facade/hudson/job/My%20Job/}
     * @see "#165735"
     */
    private String normalizeUrl(String suggested, String relativePattern) {
        Pattern tailPattern;
        synchronized (tailPatterns) {
            tailPattern = tailPatterns.get(relativePattern);
            if (tailPattern == null) {
                tailPatterns.put(relativePattern, tailPattern = Pattern.compile(".+/(" + relativePattern + ")"));
            }
        }
        Matcher m = tailPattern.matcher(suggested);
        if (m.matches()) {
            String result = instance.getUrl() + m.group(1);
            if (!result.equals(suggested)) {
                LOG.log(Level.FINER, "Normalizing {0} -> {1}", new Object[] {suggested, result});
            }
            return result;
        } else {
            LOG.warning("Anomalous URL " + suggested + " not ending with " + relativePattern + " from " + instance);
            return suggested;
        }
    }
    private static final Map<String,Pattern> tailPatterns = new HashMap<String,Pattern>();
    
    private synchronized @CheckForNull HudsonVersion retrieveHudsonVersion(boolean authentication) {
        HudsonVersion v = null;
        
        try {

            String sVersion = new ConnectionBuilder().instance(instance).url(instance.getUrl()).authentication(authentication).httpConnection().getHeaderField("X-Hudson"); // NOI18N
            if (sVersion != null) {
                v = new HudsonVersion(sVersion);
            }
        } catch (IOException e) {
            // Nothing
        }
        
        return v;
    }

    Document getDocument(String url, boolean authentication) {
        forbidden = false;
        Document doc = null;
        
        try {
            HttpURLConnection conn = new ConnectionBuilder().instance(instance).url(url).authentication(authentication).httpConnection();
            
            // Connected successfully
            if (!isConnected()) {
                connected = true;
                version = retrieveHudsonVersion(authentication);
            }
            
            // Get input stream
            InputStream stream = conn.getInputStream();
            
            // Parse document
            InputSource source = new InputSource(stream);
            source.setSystemId(url);
            doc = XMLUtil.parse(source, false, false, new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    LOG.log(Level.FINE, "{0}:{1}: {2}", new Object[] {
                        exception.getSystemId(), exception.getLineNumber(), exception.getMessage()});
                }
                public void error(SAXParseException exception) throws SAXException {
                    warning(exception);
                }
                public void fatalError(SAXParseException exception) throws SAXException {
                    warning(exception);
                    throw exception;
                }
            }, null);
            
            // Check for right version
            if (!Utilities.isSupportedVersion(getHudsonVersion(authentication))) {
                HudsonVersion v = retrieveHudsonVersion(authentication);
                
                if (!Utilities.isSupportedVersion(v))
                    return null;
                
                version = v;
            }
            
            conn.disconnect();
        } catch (SAXParseException x) {
            // already reported
        } catch (Exception x) {
            LOG.log(Level.FINE, url, x);
            if (!authentication && x instanceof HttpRetryException && ((HttpRetryException) x).responseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                forbidden = true;
            }
        }
        
        return doc;
    }

}
