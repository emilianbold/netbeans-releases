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
 * $Id$
 */

package org.netbeans.installer.sandbox.utils.installation.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.ZipPackUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.sandbox.utils.installation.Installation;
import org.netbeans.installer.sandbox.utils.installation.InstallationFileObject;
import org.netbeans.installer.sandbox.utils.installation.InstallationFiles;
import org.netbeans.installer.sandbox.utils.installation.conditions.FileCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.LogicalCondition;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.xml.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Dmitry Lipin
 */
public class InstallationImpl implements Installation {
    private Progress progress;
    private InstallationFiles installationFiles;
    
    
    
    public InstallationImpl() {
        installationFiles = new InstallationFilesImpl();
    }
    
    public void setProgress(Progress progress) {
        this.progress = progress;
    }
    
    private void setNodeAttributes(Element node, InstallationFileObject fo) {
        
        HashMap <String, Object> map = fo.getFileData();
        Set <String> keys = map.keySet();
        for(String key:keys) {
            node.setAttribute(key,fo.getData(key).toString());
        }
        node.setAttribute(fo.COND_KEY, getConditionString(fo.getFileCondition()));
    }
    public void saveInstallationFiles(File file) throws XMLException {
        try {
            final Document document = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().newDocument();
            final Element  root     = document.createElement("catalog");
            
            document.appendChild(root);
            
            if (progress != null) {
                progress.setTitle("Saving component installation files to " + file);
                progress.setPercentage(Progress.START);
            }
            
            Element  fileNode;
            for (int i = 0; i < installationFiles.size(); i++) {
                fileNode = XMLUtils.addChildNode(root, "file", null);
                InstallationFileObject fo = installationFiles.get(i);
                if (progress != null) {
                    progress.setPercentage((i * Progress.COMPLETE) / installationFiles.size());
                    progress.setDetail("Saving entry " + fo.getFile());
                    
                }
                setNodeAttributes(fileNode, fo);
            }
            
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            XMLUtils.saveXMLDocument(document,file);
        } catch (ParserConfigurationException ex) {
            throw new XMLException(
                    "Can`t save installation file list to file "+file.getPath(),
                    ex);
        } catch (XMLException ex) {
            throw new XMLException("Can`t save installation file list to file " + file.getPath(),ex);
            
        } finally {
            if (progress != null) {
                progress.setPercentage(Progress.COMPLETE);
            }
        }
    }
    private String getConditionString(FileCondition fc) {
        String result = "";
        if(fc!=null) {
            result = fc.getName();
            
            if(fc instanceof LogicalCondition) {
                result += "(";
                FileCondition [] fcs = ((LogicalCondition)fc).getConditions();
                for(int i=0;i<fcs.length;i++) {
                    result+= (i!=0) ? "," : "";
                    result += getConditionString(fcs[i]);
                }
                result += ")";
            }
        }
        return result;
    }
    
    public void loadInstallationFiles(File file) throws XMLException {
        
        try {
            Document document = DomUtil.parseXmlFile(file);
            List <Node> list = XMLUtils.getChildList(document.getDocumentElement(),"file");
            if(progress!=null) {
                progress.setTitle("Loading component installation files from " + file);
                progress.setPercentage(Progress.START);
            }
            for(int i=0;i<list.size();i++) {
                if(progress!=null) {
                    progress.setPercentage((i * Progress.COMPLETE) / list.size());
                }
                Node node = list.get(i);
                InstallationFileObject fo = new InstallationFileObject();
                for(int j=0;j<node.getAttributes().getLength();j++) {
                    Node nd = node.getAttributes().item(j);
                    fo.setData(nd.getNodeName(),nd.getNodeValue());
                }
                if(progress!=null) {
                    progress.setDetail("Adding entry " + fo.getFile());
                }
                installationFiles.add(fo);
            }
        } catch (IOException ex) {
            throw new XMLException("Can`t load installation file list from file " + file.getPath(),ex);
        } catch (ParseException ex) {
            throw new XMLException("Can`t load installation file list from file " + file.getPath(),ex);
        } finally {
            if(progress!=null) {
                progress.setPercentage(Progress.COMPLETE);
            }
        }
    }
    public InstallationFiles getInstallationFiles() {
        return installationFiles;
    }
    
    public void deleteInstallationFiles() {
        int size = installationFiles.size();
        for(int i=size-1;i>=0;i--) {
            InstallationFileObject fo = installationFiles.get(i);
            if(progress!=null) {
                progress.setPercentage(Progress.COMPLETE - ((i * Progress.COMPLETE) / size));
            }
            if(fo.accept()) {
                if(progress!=null) {
                    progress.setDetail("Deleting file " + fo.getFile());
                }
                try {
                    FileUtils.deleteFile(fo.getFile());
                } catch (IOException ex) {
                    LogManager.log(ErrorLevel.WARNING,ex);
                }
            } else {
                if(progress!=null) {
                    progress.setDetail("Skipping file " + fo.getFile());
                }
            }
        }
        if(progress!=null) {
            progress.setPercentage(Progress.COMPLETE);
        }
    }
    public void extract(File data, File location, boolean unpack, boolean useZipFile) {
        if(progress!=null) {
            progress.setDetail("Extracting installation data..");
        }
        List<File> fileList = ZipPackUtils.unzip(data, location, unpack, useZipFile, progress);
        
        if(progress!=null) {
            progress.setDetail("Receiving extracted files information..");
        }
        int size = fileList.size();
        // load data
        FileCondition fc = installationFiles.getDefaultCondition();
        for(int i=0;i<size;i++) {
            InstallationFileObject obj = new InstallationFileObject(fileList.get(i),fc);
            if(progress!=null) {
                progress.setPercentage((i * Progress.COMPLETE) / size);
                progress.setDetail("Processing: " + fileList.get(i));
            }
            obj.initDataFromFile();
            installationFiles.add(obj);
        }
        if(progress!=null) {
            progress.setDetail(".. receiving information done");
            progress.setPercentage(Progress.COMPLETE);
        }
    }
}
