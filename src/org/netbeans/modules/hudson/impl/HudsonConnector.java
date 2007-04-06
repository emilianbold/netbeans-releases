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

import org.netbeans.modules.hudson.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Hudson Server Connector
 * 
 * @author pblaha
 */
public class HudsonConnector {
    
    static final String XML_API_URL ="/api/xml";
    private static final String JOB_ELEMENT_NAME = "job";
    private static final String NAME_ELEMENT_NAME = "name";
    private static final String URL_ELEMENT_NAME = "url";
    private static final String LAST_BUILD_ELEMENT_NAME = "lastBuild";
    private static final String COLOR_ELEMENT_NAME = "color";
    
    private DocumentBuilder builder;
    private HudsonInstanceImpl instance;
    private boolean isConnected;
    
    /** Creates a new instance of HudsonXMLFacade
     * @param instanceURL
     */
    public HudsonConnector(HudsonInstanceImpl instance) {
        this.instance = instance;
        this.isConnected = false;
        
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }  catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        }
    }
    
    public synchronized boolean isConnected() {
        return isConnected;
    }
    
    /**
     *
     * @return
     */
    public synchronized List<HudsonJob> getAllJobs() {
        ArrayList<HudsonJob> jobsArray = new ArrayList<HudsonJob>();
        Document doc = getDocument();
        
        if (null == doc)
            return jobsArray;
        
        NodeList jobs = doc.getElementsByTagName(JOB_ELEMENT_NAME);
        
        for (int i = 0; i < jobs.getLength(); i++) {
            Node job = jobs.item(i);
            String name = null;
            String url = null;
            int lastBuild = 0;
            Color color = null;
            
            for (int j = 0; j < job.getChildNodes().getLength(); j++) {
                Node jobNode = job.getChildNodes().item(j);
                
                if (jobNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    if (jobNode.getNodeName().equals(NAME_ELEMENT_NAME)) {
                        name = jobNode.getFirstChild().getTextContent();
                    } else if (jobNode.getNodeName().equals(URL_ELEMENT_NAME)) {
                        url = jobNode.getFirstChild().getTextContent();
                    } else if (jobNode.getNodeName().equals(LAST_BUILD_ELEMENT_NAME)) {
                        lastBuild = Integer.parseInt(jobNode.getFirstChild().getTextContent());
                    } else if (jobNode.getNodeName().equals(COLOR_ELEMENT_NAME)) {
                        color = Color.valueOf(jobNode.getFirstChild().getTextContent());
                    }
                }
            }
            
            if (null != name && null != url && null != color)                    
                jobsArray.add(new HudsonJobImpl(name, url, color, lastBuild));
        }
        
        return jobsArray;
    }
    
    private synchronized Document getDocument() {
        Document doc = null;
        
        isConnected = false;
        
        try{
            URL u = new java.net.URL(instance.getUrl() + XML_API_URL);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            InputStream stream = conn.getInputStream();
            doc = builder.parse(stream);
            
            isConnected = true;
            
            if(conn != null)
                conn.disconnect();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_MalformedURL", instance.getUrl() + XML_API_URL));
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_IOError", instance.getUrl() + XML_API_URL));
        } catch (SAXException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        } catch (NullPointerException ex) {
            ErrorManager.getDefault().log(ErrorManager.ERROR,NbBundle.getMessage(HudsonConnector.class, "MSG_ParserError", ex.getLocalizedMessage()));
        }
        
        return doc;
    }  
}