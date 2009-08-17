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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import static org.netbeans.modules.hudson.constants.HudsonJobConstants.*;
import static org.netbeans.modules.hudson.constants.HudsonXmlApiConstants.*;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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
    
    private Map<String, HudsonView> cache = new HashMap<String, HudsonView>();
    
    /**
     * Creates a new instance of HudsonConnector
     *
     * @param HudsonInstance
     */
    public HudsonConnector(HudsonInstanceImpl instance) {
        this.instance = instance;
    }
    
    public synchronized Collection<HudsonJob> getAllJobs() {
        Document docInstance = getDocument(instance.getUrl() + XML_API_URL + "?depth=1&xpath=/&exclude=//primaryView&exclude=//view[name='All']" +
                "&exclude=//view/job/url&exclude=//view/job/color&exclude=//description&exclude=//job/build&exclude=//healthReport" +
                "&exclude=//firstBuild&exclude=//keepDependencies&exclude=//nextBuildNumber&exclude=//property&exclude=//action" +
                "&exclude=//upstreamProject&exclude=//downstreamProject&exclude=//queueItem"); // NOI18N
        
        if (null == docInstance)
            return new ArrayList<HudsonJob>();
        
        // Clear cache
        cache.clear();
        
        // Parse views and set them into instance
        Collection<HudsonView> cViews = getViews(docInstance);
        
        if (null == cViews)
            cViews = new ArrayList<HudsonView>();
        
        instance.setViews(cViews);
        
        // Parse jobs and return them
        Collection<HudsonJob> cJobs = getJobs(docInstance);
        
        if (null == cJobs)
            cJobs = new ArrayList<HudsonJob>();
        
        return cJobs;
    }
    
    public synchronized void startJob(final HudsonJob job) {
        ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(HudsonInstanceImpl.class, "MSG_Starting", job.getName()));
        handle.start();
        try {
            new ConnectionBuilder().instance(instance).url(job.getUrl() + "build").postData("delay=0sec".getBytes()).connection(); // NOI18N
        } catch (IOException e) {
            LOG.log(Level.FINE, "Could not start {0}: {1}", new Object[] {job, e});
        } finally {
            handle.finish();
        }
        instance.synchronize();
    }

    /**
     * Gets general information about a build.
     * The changelog ({@code <changeSet>}) can be interpreted separately by {@link HudsonJobBuild#getChanges}.
     */
    Collection<? extends HudsonJobBuild> getBuilds(HudsonJobImpl job) {
        Document docBuild = getDocument(job.getUrl() + XML_API_URL +
                // XXX no good way to only include what you _do_ want without using XSLT
                "?depth=1&xpath=/*/build&wrapper=root&exclude=//artifact&exclude=//action&exclude=//changeSet&exclude=//culprit" +
                "&exclude=//duration&exclude=//fullDisplayName&exclude=//keepLog&exclude=//timestamp&exclude=//url&exclude=//builtOn" +
                "&exclude=//id&exclude=//description");
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
    
    protected synchronized HudsonVersion getHudsonVersion() {
        if (null == version)
            version = retrieveHudsonVersion();
        
        return version;
    }
    
    protected boolean isConnected() {
        return connected;
        
    }
    
    private Collection<HudsonView> getViews(Document doc) {
        Collection<HudsonView> views = new ArrayList<HudsonView>();

        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!n.getNodeName().equals(XML_API_VIEW_ELEMENT)) {
                continue;
            }
            
            String name = null;
            String url = null;
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                if (o.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                    name = o.getFirstChild().getTextContent();
                } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                    url = normalizeUrl(o.getFirstChild().getTextContent(), "view/[^/]+/"); // NOI18N
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
            }
            
        }
        
        return views;
    }
    
    private Collection<HudsonJob> getJobs(Document doc) {
        Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
        
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!n.getNodeName().equals(XML_API_JOB_ELEMENT)) {
                continue;
            }
            
            HudsonJobImpl job = new HudsonJobImpl(instance);
            
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
                        String text = n2.getFirstChild().getTextContent();
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
                    }
                    job.addModule(name, displayName, color, url);
                } else {
                    LOG.fine("unexpected global <job> child: " + nodeName);
                }
            }

            for (HudsonView v : instance.getViews()) {
                if (null != cache.get(v.getName() + "/" + job.getName())) // NOI18N
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
    
    private synchronized HudsonVersion retrieveHudsonVersion() {
        HudsonVersion v = null;
        
        try {

            String sVersion = new ConnectionBuilder().instance(instance).url(instance.getUrl()).httpConnection().getHeaderField("X-Hudson"); // NOI18N
            if (sVersion != null) {
                v = new HudsonVersion(sVersion);
            }
        } catch (IOException e) {
            // Nothing
        }
        
        return v;
    }
    
    Document getDocument(String url) {
        Document doc = null;
        
        try {
            HttpURLConnection conn = new ConnectionBuilder().instance(instance).url(url).httpConnection();
            
            // Connected successfully
            if (!isConnected()) {
                connected = true;
                version = retrieveHudsonVersion();
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
            if (!Utilities.isSupportedVersion(getHudsonVersion())) {
                HudsonVersion v = retrieveHudsonVersion();
                
                if (!Utilities.isSupportedVersion(v))
                    return null;
                
                version = v;
            }
            
            conn.disconnect();
        } catch (SAXParseException x) {
            // already reported
        } catch (Exception x) {
            LOG.log(Level.FINE, url, x);
        }
        
        return doc;
    }

}
