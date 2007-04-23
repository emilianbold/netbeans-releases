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

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.HudsonJobChangeFile;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.HudsonJobChangeItem;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.util.Utilities;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Hudson Server Connector
 *
 * @author Michal Mocnak
 */
public class HudsonConnector {
    
    // XML API Suffix
    private static final String XML_API_URL ="/api/xml";
    
    // Hudson Instance Element
    private static final String VIEW_ELEMENT_NAME = "view";
    private static final String JOB_ELEMENT_NAME = "job";
    private static final String NAME_ELEMENT_NAME = "name";
    private static final String URL_ELEMENT_NAME = "url";
    private static final String COLOR_ELEMENT_NAME = "color";
    
    // Hudson Job Elements
    private static final String DESCRIPTION_ELEMENT_NAME = "description";
    private static final String DISPLAY_NAME_ELEMENT_NAME = "displayName";
    private static final String BUILDABLE_ELEMENT_NAME = "buildable";
    private static final String INQUEUE_ELEMENT_NAME = "inQueue";
    private static final String LAST_BUILD_ELEMENT_NAME = "lastBuild";
    private static final String LAST_STABLE_BUILD_ELEMENT_NAME = "lastStableBuild";
    private static final String LAST_SUCCESSFUL_BUILD_ELEMENT_NAME = "lastSuccessfulBuild";
    private static final String LAST_FAILED_BUILD_ELEMENT_NAME = "lastFailedBuild";
    
    // Hudson Job Build Elements
    private static final String BUILDING_ELEMENT_NAME = "building";
    private static final String DURATION_ELEMENT_NAME = "duration";
    private static final String TIMESTAMP_ELEMENT_NAME = "timestamp";
    private static final String RESULT_ELEMENT_NAME = "result";
    private static final String ITEM_ELEMENT_NAME = "item";
    private static final String FILE_ELEMENT_NAME = "file";
    private static final String EDIT_TYPE_ELEMENT_NAME = "editType";
    private static final String REVISION_ELEMENT_NAME = "revision";
    private static final String PREV_REVISION_ELEMENT_NAME = "prevrevision";
    private static final String MSG_ELEMENT_NAME = "msg";
    private static final String USER_ELEMENT_NAME = "user";
    
    
    // Start Hudson Job Command
    private static final String BUILD_URL = "build";
    
    private DocumentBuilder builder;
    private HudsonInstanceImpl instance;
    private HudsonVersion version;
    
    private boolean isConnected = false;
    
    private Map<String, HudsonView> cache = new HashMap<String, HudsonView>();
    
    /**
     * Creates a new instance of HudsonConnector
     *
     * @param HudsonInstance
     */
    public HudsonConnector(HudsonInstanceImpl instance) {
        this.instance = instance;
        
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }  catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        }
    }
    
    public synchronized boolean isConnected() {
        synchronized(this) {
            return isConnected;
        }
    }
    
    private synchronized void setConnected(boolean isConnected) {
        synchronized(this) {
            this.isConnected = isConnected;
        }
    }
    
    public synchronized HudsonVersion getHudsonVersion() {
        if (null == version)
            version = retrievedHudsonVersion();
        
        return version;
    }
    
    public synchronized Collection<HudsonJob> getAllJobs() {
        Document docInstance = getDocument(instance.getUrl());
        
        if (null == docInstance)
            return new ArrayList<HudsonJob>();
        
        // Clear cache
        cache.clear();
        
        // Get views and jobs
        NodeList views = docInstance.getElementsByTagName(VIEW_ELEMENT_NAME);
        NodeList jobs = docInstance.getElementsByTagName(JOB_ELEMENT_NAME);
        
        // Parse views and set them into instance
        Collection<HudsonView> cViews = getViews(views);
        
        if (null == cViews)
            cViews = new ArrayList<HudsonView>();
        
        instance.setViews(cViews);
        
        // Parse jobs and return them
        Collection<HudsonJob> cJobs = getJobs(jobs);
        
        if (null == cJobs)
            cJobs = new ArrayList<HudsonJob>();
        
        return cJobs;
    }
    
    public synchronized boolean startJob(HudsonJob job) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(HudsonInstanceImpl.class, "MSG_Starting", job.getName()));
        
        // Start progress
        handle.start();
        
        try {
            final URL url = new URL(job.getUrl() + "/" + BUILD_URL);
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        // Start job
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        
                        if(conn.getResponseCode() != 200)
                            ErrorManager.getDefault().log("Can't start build HTTP error: " + conn.getResponseMessage());
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } finally {
                        // Stop progress
                        handle.finish();
                    }
                }
            });
            
            return true;
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return false;
    }
    
    public synchronized HudsonJobBuild getJobBuild(HudsonJob job, int build) {
        Document docBuild = getDocument(job.getUrl() + build + "/");
        
        if (null == docBuild)
            return null;
        
        // Get build details
        NodeList buildDetails = docBuild.getDocumentElement().getChildNodes();
        
        HudsonJobBuild result = new HudsonJobBuild();
        
        for (int i = 0 ; i < buildDetails.getLength() ; i++) {
            Node n = buildDetails.item(i);
            
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(BUILDING_ELEMENT_NAME)) {
                    result.setBuilding(Boolean.parseBoolean(n.getFirstChild().getTextContent()));
                } else if (n.getNodeName().equals(DURATION_ELEMENT_NAME)) {
                    result.setDuration(Long.parseLong(n.getFirstChild().getTextContent()));
                } else if (n.getNodeName().equals(TIMESTAMP_ELEMENT_NAME)) {
                    result.setTimestamp(Long.parseLong(n.getFirstChild().getTextContent()));
                } else if (!result.isBuilding() && n.getNodeName().equals(RESULT_ELEMENT_NAME)) {
                    result.setResult((n.getFirstChild().getTextContent().
                            equals("SUCCESS")) ? Result.SUCCESS : Result.FAILURE);
                }
            }
        }
        
        try {
            // Get changes
            NodeList changes = docBuild.getElementsByTagName(ITEM_ELEMENT_NAME);
            
            for (int i = 0 ; i < changes.getLength() ; i++) {
                Node n = changes.item(i);
                
                // Create a new change item
                HudsonJobChangeItem item = new HudsonJobChangeItem();
                
                for (int j = 0 ; j < n.getChildNodes().getLength() ; j++) {
                    Node o = n.getChildNodes().item(j);
                    
                    if (o.getNodeType() == Node.ELEMENT_NODE) {
                        if (o.getNodeName().equals(FILE_ELEMENT_NAME)) {
                            
                            HudsonJobChangeFile file = new HudsonJobChangeFile();
                            
                            for (int k = 0 ; k < o.getChildNodes().getLength() ; k++) {
                                Node d = o.getChildNodes().item(k);
                                
                                if (d.getNodeType() == Node.ELEMENT_NODE) {
                                    if (d.getNodeName().equals(EDIT_TYPE_ELEMENT_NAME)) {
                                        file.setEditType(EditType.valueOf(d.getFirstChild().getTextContent()));
                                    } else if (d.getNodeName().equals(NAME_ELEMENT_NAME)) {
                                        file.setName(d.getFirstChild().getTextContent());
                                    } else if (d.getNodeName().equals(PREV_REVISION_ELEMENT_NAME)) {
                                        file.setPrevRevision(d.getFirstChild().getTextContent());
                                    } else if (d.getNodeName().equals(REVISION_ELEMENT_NAME)) {
                                        file.setRevision(d.getFirstChild().getTextContent());
                                    }
                                }
                            }
                            
                            item.addFile(file);
                        } else if (o.getNodeName().equals(MSG_ELEMENT_NAME)) {
                            item.setMsg(o.getFirstChild().getTextContent());
                        } else if (o.getNodeName().equals(USER_ELEMENT_NAME)) {
                            item.setUser(o.getFirstChild().getTextContent());
                        }
                    }
                }
                
                result.addChangeItem(item);
                
            }
        } catch (NullPointerException e) {
            // There is no changes
        }
        
        return result;
    }
    
    private Collection<HudsonView> getViews(NodeList nodes) {
        Collection<HudsonView> views = new ArrayList<HudsonView>();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            
            String name = null;
            String url = null;
            String description = null;
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    if (o.getNodeName().equals(NAME_ELEMENT_NAME)) {
                        name = o.getFirstChild().getTextContent();
                    } else if (o.getNodeName().equals(URL_ELEMENT_NAME)) {
                        url = o.getFirstChild().getTextContent();
                    }
                }
            }
            
            if (null != name && null != url) {
                Document docView = getDocument(url);
                
                if (null == docView)
                    continue;
                
                // Retrieve description
                NodeList descriptionList = docView.getElementsByTagName(DESCRIPTION_ELEMENT_NAME);
                
                try {
                    description = descriptionList.item(0).getFirstChild().getTextContent();
                } catch (NullPointerException e) {
                    description = "";
                }
                
                // Create HudsonView
                HudsonViewImpl view = new HudsonViewImpl(name, description, url);
                
                if (!view.getName().equals(HudsonView.ALL_VIEW)) {
                    
                    // Retrieve jobs
                    NodeList jobsList = docView.getElementsByTagName(JOB_ELEMENT_NAME);
                    
                    for (int k = 0; k < jobsList.getLength(); k++) {
                        Node d = jobsList.item(k);
                        
                        for (int l = 0; l < d.getChildNodes().getLength(); l++) {
                            Node e = d.getChildNodes().item(l);
                            
                            if (e.getNodeType() == Node.ELEMENT_NODE) {
                                if (e.getNodeName().equals(NAME_ELEMENT_NAME)) {
                                    cache.put(view.getName() + "/" + e.getFirstChild().getTextContent(), view);
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
    
    private Collection<HudsonJob> getJobs(NodeList nodes) {
        Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            
            HudsonJobImpl job = new HudsonJobImpl(instance);
            
            for (int j = 0; j < n.getChildNodes().getLength(); j++) {
                Node o = n.getChildNodes().item(j);
                
                if (o.getNodeType() == Node.ELEMENT_NODE) {
                    if (o.getNodeName().equals(NAME_ELEMENT_NAME)) {
                        job.setName(o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(URL_ELEMENT_NAME)) {
                        job.setUrl(o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(COLOR_ELEMENT_NAME)) {
                        job.setColor(Color.valueOf(o.getFirstChild().getTextContent()));
                    }
                }
            }
            
            if (null != job.getName() && null != job.getUrl() && null != job.getColor()) {
                Document docJob = getDocument(job.getUrl());
                
                if (null == docJob)
                    continue;
                
                NodeList jobDetails = docJob.getDocumentElement().getChildNodes();
                
                for (int k = 0; k < jobDetails.getLength(); k++) {
                    Node d = jobDetails.item(k);
                    
                    if (d.getNodeType() == Node.ELEMENT_NODE) {
                        if (d.getNodeName().equals(DESCRIPTION_ELEMENT_NAME)) {
                            try {
                                job.setDescription(d.getFirstChild().getTextContent());
                            } catch (NullPointerException e) {
                                job.setDescription("");
                            }
                        } else if (d.getNodeName().equals(DISPLAY_NAME_ELEMENT_NAME)) {
                            job.setDisplayName(d.getFirstChild().getTextContent());
                        } else if (d.getNodeName().equals(BUILDABLE_ELEMENT_NAME)) {
                            job.setIsBuildable(Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(INQUEUE_ELEMENT_NAME)) {
                            job.setIsInQueue(Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(LAST_BUILD_ELEMENT_NAME)) {
                            job.setLastBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(LAST_FAILED_BUILD_ELEMENT_NAME)) {
                            job.setLastFailedBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(LAST_STABLE_BUILD_ELEMENT_NAME)) {
                            job.setLastStableBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(LAST_SUCCESSFUL_BUILD_ELEMENT_NAME)) {
                            job.setLastSuccessfulBuild(Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        }
                    }
                }
                
                for (HudsonView v : instance.getViews()) {
                    // All view synchronization
                    if (v.getName().equals(HudsonView.ALL_VIEW)) {
                        job.addView(v);
                        continue;
                    }
                    
                    if (null != cache.get(v.getName() + "/" + job.getName()))
                        job.addView(v);
                }
                
                jobs.add(job);
            }
        }
        
        return jobs;
    }
    
    private synchronized HudsonVersion retrievedHudsonVersion() {
        HudsonVersion v = null;
        
        try {
            URL u = new java.net.URL(instance.getUrl());
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            
            // Get string version
            String sVersion = conn.getHeaderField("X-Hudson");
            
            // Create a HudsonVersion object
            v = new HudsonVersionImpl(sVersion);
        } catch (MalformedURLException e) {
            // Nothing
        } catch (IOException e) {
            // Nothing
        }
        
        return v;
    }
    
    private Document getDocument(String url) {
        Document doc = null;
        
        try {
            URL u = new java.net.URL(url + XML_API_URL);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            
            // Connected failed
            if(conn.getResponseCode() != 200) {
                setConnected(false);
                return null;
            }
            
            // Connected successfully
            if (!isConnected()) {
                setConnected(true);
                version = retrievedHudsonVersion();
            }
            
            // Get input stream
            InputStream stream = conn.getInputStream();
            
            // Parse document
            doc = builder.parse(stream);
            
            // Check for right version
            if (!Utilities.isSupportedVersion(getHudsonVersion())) {
                HudsonVersion v = retrievedHudsonVersion();
                
                if (!Utilities.isSupportedVersion(v))
                    return null;
                
                version = v;
            }
            
            if(conn != null)
                conn.disconnect();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_MalformedURL", url + XML_API_URL));
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_IOError", url + XML_API_URL));
        } catch (SAXException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        } catch (NullPointerException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        }
        
        return doc;
    }
}