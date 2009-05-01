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
        Document docInstance = getDocument(instance.getUrl() + XML_API_URL + "?depth=1"); // NOI18N
        
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
            new ConnectionBuilder().instance(instance).url(job.getUrl() + "build?delay=0sec").connection(); // NOI18N
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
                "?depth=1&xpath=/*/build&wrapper=root&exclude=//artifact&exclude=//action&exclude=//changeSet&exclude=//culprit");
        if (docBuild == null) {
            return Collections.emptySet();
        }
        List<HudsonJobBuildImpl> builds = new ArrayList<HudsonJobBuildImpl>();
        NodeList buildDetails = docBuild.getElementsByTagName("build"); // NOI18N // HUDSON-3267: might be root elt
        for (int i = 0; i < buildDetails.getLength(); i++) {
            Element build = (Element) buildDetails.item(i);
            int number = Integer.parseInt(Utilities.xpath("number", build)); // NOI18N
            boolean building = Boolean.valueOf(Utilities.xpath("building", build)); // NOI18N
            Result result = building ? Result.NOT_BUILT : Result.valueOf(Utilities.xpath("result", build)); // NOI18N
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
            String description = null;
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                        name = o.getFirstChild().getTextContent();
                    } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                        url = o.getFirstChild().getTextContent();
                    }
                }
            }
            
            if (null != name && null != url) {
                Element docView = (Element) n;
                
                // Retrieve description
                NodeList descriptionList = docView.getElementsByTagName(XML_API_DESCRIPTION_ELEMENT);
                
                try {
                    description = descriptionList.item(0).getFirstChild().getTextContent();
                } catch (NullPointerException e) {
                    description = "";
                }
                
                // Create HudsonView
                HudsonViewImpl view = new HudsonViewImpl(instance, name, description, url);
                
                if (!view.getName().equals(HudsonView.ALL_VIEW)) {
                    
                    // Retrieve jobs
                    NodeList jobsList = docView.getElementsByTagName(XML_API_JOB_ELEMENT);
                    
                    for (int k = 0; k < jobsList.getLength(); k++) {
                        Node d = jobsList.item(k);
                        
                        for (int l = 0; l < d.getChildNodes().getLength(); l++) {
                            Node e = d.getChildNodes().item(l);
                            
                            if (e.getNodeType() == Node.ELEMENT_NODE) {
                                if (e.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                                    cache.put(view.getName() + "/" + e.getFirstChild().getTextContent(), view); // NOI18N
                                }
                            }
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
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                        job.putProperty(JOB_NAME, o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                        job.putProperty(JOB_URL, o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(XML_API_COLOR_ELEMENT)) {
                        String color = o.getFirstChild().getTextContent().trim();
                        try {
                            job.putProperty(JOB_COLOR, Color.valueOf(color));
                        } catch (IllegalArgumentException x) {
                            Exceptions.attachMessage(x,
                                    "http://www.netbeans.org/nonav/issues/show_bug.cgi?id=126166 - no Color value '" +
                                    color + "' among " + Arrays.toString(Color.values())); // NOI18N
                            Exceptions.printStackTrace(x);
                            job.putProperty(JOB_COLOR, Color.red_anime);
                        }
                    }
                }
            }
            
            if (null != job.getName() && null != job.getUrl() && null != job.getColor()) {
                NodeList jobDetails = n.getChildNodes();
                
                for (int k = 0; k < jobDetails.getLength(); k++) {
                    Node d = jobDetails.item(k);
                    
                    if (d.getNodeType() == Node.ELEMENT_NODE) {
                        if (d.getNodeName().equals(XML_API_DESCRIPTION_ELEMENT)) {
                            try {
                                job.putProperty(JOB_DESCRIPTION, d.getFirstChild().getTextContent());
                            } catch (NullPointerException e) {}
                        } else if (d.getNodeName().equals(XML_API_DISPLAY_NAME_ELEMENT)) {
                            job.putProperty(JOB_DISPLAY_NAME, d.getFirstChild().getTextContent());
                        } else if (d.getNodeName().equals(XML_API_BUILDABLE_ELEMENT)) {
                            job.putProperty(JOB_BUILDABLE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_INQUEUE_ELEMENT)) {
                            job.putProperty(JOB_IN_QUEUE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_FAILED_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_FAILED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_STABLE_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_STABLE_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_SUCCESSFUL_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_COMPLETED_BUILD_ELEMENT)) {
                            job.putProperty(JOB_LAST_COMPLETED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals("module")) { // NOI18N
                            Element e = (Element) d;
                            job.addModule(Utilities.xpath("name", e), Utilities.xpath("displayName", e), // NOI18N
                                    Color.valueOf(Utilities.xpath("color", e)), Utilities.xpath("url", e)); // NOI18N
                        }
                    }
                }
                
                for (HudsonView v : instance.getViews()) {
                    // All view synchronization
                    if (v.getName().equals(HudsonView.ALL_VIEW)) {
                        job.addView(v);
                        continue;
                    }
                    
                    if (null != cache.get(v.getName() + "/" + job.getName())) // NOI18N
                        job.addView(v);
                }
                
                jobs.add(job);
            }
        }
        
        return jobs;
    }
    
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
