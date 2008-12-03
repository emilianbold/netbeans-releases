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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.modules.hudson.constants.HudsonJobBuildConstants;
import org.netbeans.modules.hudson.constants.HudsonJobChangeFileConstants;
import org.netbeans.modules.hudson.constants.HudsonJobChangeItemConstants;
import org.netbeans.modules.hudson.constants.HudsonJobConstants;
import org.netbeans.modules.hudson.constants.HudsonXmlApiConstants;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.HudsonJobChangeFile;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.HudsonJobChangeItem;
import org.netbeans.modules.hudson.impl.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
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
public class HudsonConnector implements HudsonXmlApiConstants,
        HudsonJobConstants, HudsonJobBuildConstants, HudsonJobChangeFileConstants, HudsonJobChangeItemConstants {
    
    private DocumentBuilder builder;
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
        
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }  catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        }
    }
    
    public synchronized Collection<HudsonJob> getAllJobs() {
        Document docInstance = getDocument(instance.getUrl());
        
        if (null == docInstance)
            return new ArrayList<HudsonJob>();
        
        // Clear cache
        cache.clear();
        
        // Get views and jobs
        NodeList views = docInstance.getElementsByTagName(XML_API_VIEW_ELEMENT);
        NodeList jobs = docInstance.getElementsByTagName(XML_API_JOB_ELEMENT);
        
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
            final URL url = new URL(Utilities.getURLWithoutSpaces(job.getUrl() + "/" + XML_API_BUILD_URL));
            
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
                if (n.getNodeName().equals(XML_API_BUILDING_ELEMENT)) {
                    result.putProperty(HUDSON_JOB_BUILD_BUILDING, Boolean.parseBoolean(n.getFirstChild().getTextContent()));
                } else if (n.getNodeName().equals(XML_API_DURATION_ELEMENT)) {
                    result.putProperty(HUDSON_JOB_BUILD_DURATION, Long.parseLong(n.getFirstChild().getTextContent()));
                } else if (n.getNodeName().equals(XML_API_TIMESTAMP_ELEMENT)) {
                    result.putProperty(HUDSON_JOB_BUILD_TIMESTAMP, Long.parseLong(n.getFirstChild().getTextContent()));
                } else if (!result.isBuilding() && n.getNodeName().equals(XML_API_RESULT_ELEMENT)) {
                    result.putProperty(HUDSON_JOB_BUILD_RESULT, (n.getFirstChild().getTextContent().
                            equals("SUCCESS")) ? Result.SUCCESS : Result.FAILURE);
                }
            }
        }
        
        try {
            // Get changes
            NodeList changes = docBuild.getElementsByTagName(XML_API_ITEM_ELEMENT);
            
            for (int i = 0 ; i < changes.getLength() ; i++) {
                Node n = changes.item(i);
                
                // Create a new change item
                HudsonJobChangeItem item = new HudsonJobChangeItem();
                
                for (int j = 0 ; j < n.getChildNodes().getLength() ; j++) {
                    Node o = n.getChildNodes().item(j);
                    
                    if (o.getNodeType() == Node.ELEMENT_NODE) {
                        if (o.getNodeName().equals(XML_API_FILE_ELEMENT)) {
                            
                            HudsonJobChangeFile file = new HudsonJobChangeFile();
                            
                            for (int k = 0 ; k < o.getChildNodes().getLength() ; k++) {
                                Node d = o.getChildNodes().item(k);
                                
                                if (d.getNodeType() == Node.ELEMENT_NODE) {
                                    if (d.getNodeName().equals(XML_API_EDIT_TYPE_ELEMENT)) {
                                        file.putProperty(HUDSON_JOB_CHANGE_FILE_EDIT_TYPE,
                                                EditType.valueOf(d.getFirstChild().getTextContent()));
                                    } else if (d.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                                        file.putProperty(HUDSON_JOB_CHANGE_FILE_NAME, d.getFirstChild().getTextContent());
                                    } else if (d.getNodeName().equals(XML_API_PREV_REVISION_ELEMENT)) {
                                        file.putProperty(HUDSON_JOB_CHANGE_FILE_PREVIOUS_REVISION,
                                                d.getFirstChild().getTextContent());
                                    } else if (d.getNodeName().equals(XML_API_REVISION_ELEMENT)) {
                                        file.putProperty(HUDSON_JOB_CHANGE_FILE_REVISION,
                                                d.getFirstChild().getTextContent());
                                    }
                                }
                            }
                            
                            item.addFile(file);
                        } else if (o.getNodeName().equals(XML_API_MSG_ELEMENT)) {
                            item.putProperty(HUDSON_JOB_CHANGE_ITEM_MESSAGE, o.getFirstChild().getTextContent());
                        } else if (o.getNodeName().equals(XML_API_USER_ELEMENT)) {
                            item.putProperty(HUDSON_JOB_CHANGE_ITEM_USER, o.getFirstChild().getTextContent());
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
    
    protected synchronized HudsonVersion getHudsonVersion() {
        if (null == version)
            version = retrieveHudsonVersion();
        
        return version;
    }
    
    protected boolean isConnected() {
        return connected;
        
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
                    if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                        name = o.getFirstChild().getTextContent();
                    } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                        url = o.getFirstChild().getTextContent();
                    }
                }
            }
            
            if (null != name && null != url) {
                Document docView = getDocument(url);
                
                if (null == docView)
                    continue;
                
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
                    if (o.getNodeName().equals(XML_API_NAME_ELEMENT)) {
                        job.putProperty(HUDSON_JOB_NAME, o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(XML_API_URL_ELEMENT)) {
                        job.putProperty(HUDSON_JOB_URL, o.getFirstChild().getTextContent());
                    } else if (o.getNodeName().equals(XML_API_COLOR_ELEMENT)) {
                        String color = o.getFirstChild().getTextContent().trim();
                        try {
                            job.putProperty(HUDSON_JOB_COLOR, Color.valueOf(color));
                        } catch (IllegalArgumentException x) {
                            Exceptions.attachMessage(x,
                                    "http://www.netbeans.org/nonav/issues/show_bug.cgi?id=126166 - no Color value '" +
                                    color + "' among " + Arrays.toString(Color.values()));
                            Exceptions.printStackTrace(x);
                            job.putProperty(HUDSON_JOB_COLOR, Color.red_anime);
                        }
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
                        if (d.getNodeName().equals(XML_API_DESCRIPTION_ELEMENT)) {
                            try {
                                job.putProperty(HUDSON_JOB_DESCRIPTION, d.getFirstChild().getTextContent());
                            } catch (NullPointerException e) {}
                        } else if (d.getNodeName().equals(XML_API_DISPLAY_NAME_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_DISPLAY_NAME, d.getFirstChild().getTextContent());
                        } else if (d.getNodeName().equals(XML_API_BUILDABLE_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_BUILDABLE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_INQUEUE_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_IN_QUEUE, Boolean.valueOf(d.getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_BUILD_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_LAST_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_FAILED_BUILD_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_LAST_FAILED_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_STABLE_BUILD_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_LAST_STABLE_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
                        } else if (d.getNodeName().equals(XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT)) {
                            job.putProperty(HUDSON_JOB_LAST_SUCCESSFUL_BUILD, Integer.valueOf(d.getFirstChild().getFirstChild().getTextContent()));
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
    
    private synchronized HudsonVersion retrieveHudsonVersion() {
        HudsonVersion v = null;
        
        try {
            URL u = new java.net.URL(instance.getUrl());
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            String sVersion = conn.getHeaderField("X-Hudson");
            if (sVersion != null) {
                v = new HudsonVersionImpl(sVersion);
            }
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
            URL u = new java.net.URL(Utilities.getURLWithoutSpaces(url + XML_API_URL));
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            
            // Connected failed
            if(conn.getResponseCode() != 200) {
                connected = false;
                return null;
            }
            
            // Connected successfully
            if (!isConnected()) {
                connected = true;
                version = retrieveHudsonVersion();
            }
            
            // Get input stream
            InputStream stream = conn.getInputStream();
            
            // Parse document
            doc = builder.parse(stream);
            
            // Check for right version
            if (!Utilities.isSupportedVersion(getHudsonVersion())) {
                HudsonVersion v = retrieveHudsonVersion();
                
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