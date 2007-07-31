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

/*
 * InfoCollector.java
 *
 * Created on January 26, 2006, 3:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.wizard;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.retriever.*;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
//import org.netbeans.modules.xml.retriever.impl.*;

/**
 *
 * @author girix
 */
public class InfoCollector {
    public static enum InfoType {
        url, // warning condition
        relative_ok, //pass condition
        relative_bad_found, // error condition
        relative_bad_not_found, // error condition
        relative_notfound, // warning condition
        absolute_ok, // warning condition
        absolute_bad, // warning condition
        bad_uri // warning condition
    };
    
    Map<File, List<InfoEntry>> allFiles2Info = new HashMap<File,List<InfoEntry>>();
    
    Map<File, List<InfoEntry>> errorFiles2Info = new HashMap<File,List<InfoEntry>>();
    
    Map<File, List<InfoEntry>> warningFiles2Info = new HashMap<File,List<InfoEntry>>();
    
    public Map<File, List<InfoEntry>> absURL2Info = new HashMap<File,List<InfoEntry>>();
    
    List<File> goodFileList = new ArrayList<File>();
    
    File root = null;
    /** Creates a new instance of InfoCollector */
    public InfoCollector(File root) {
        this.root = root;
        goCollect();
    }
    
    public void goCollect(){
        //get all xsd files starting from root dir
        List<File> xsdFiles = Utilities.getFilesWithExtension(root, DocumentTypesEnum.schema.toString(), null);
        //for each schema gather all external refs
        Map<File,List<String>> xsdFile2Refs = getAllExternalRefs(xsdFiles, DocumentTypesEnum.schema);
        
        //get all wsdl files starting from root dir
        List<File> wsdlFiles = Utilities.getFilesWithExtension(root, DocumentTypesEnum.wsdl.toString(), null);
        //for each wsdl gather all external refs
        Map<File,List<String>> wsdlFile2Refs = getAllExternalRefs(wsdlFiles, DocumentTypesEnum.wsdl);
        
        //merg 2 results
        Map<File,List<String>> file2Refs = new HashMap<File,List<String>>();
        file2Refs.putAll(xsdFile2Refs);
        file2Refs.putAll(wsdlFile2Refs);
        
        //analyse the result and retain
        analyzeResult(file2Refs);
        makeGoodFileList();
    }
    
    public Map<File,List<String>> getAllExternalRefs(List<File> files, DocumentTypesEnum docType){
        Map<File,List<String>> file2Refs = new HashMap<File,List<String>>();
        for(File file: files){
            DocumentTypeParser schParser = DocumentParserFactory.getParser(docType);
            List<String> externalRefList = null;
            try {
                externalRefList = schParser.getAllLocationOfReferencedEntities(file);
            } catch (Exception ex) {
                externalRefList = Collections.emptyList();
            }
            file2Refs.put(file, externalRefList);
        }
        return file2Refs;
    }
    
    public void analyzeResult(Map<File,List<String>> file2Refs) {
        for(File file : file2Refs.keySet()){
            List<String> extRefList = file2Refs.get(file);
            if(extRefList.size() <= 0){
                //this is for the files that do not have any external refs.
                placeInProperBucket(file, new InfoEntry(null, InfoType.relative_ok));
                continue;
            }
            for(String refStr : extRefList){
                InfoEntry infEnt = analyze(file, refStr);
                placeInProperBucket(file, infEnt);
            }
        }
    }
    
    public void placeInProperBucket(File file, InfoEntry infEnt) {
        InfoType infoType = infEnt.getInfoType();
        placeIn(allFiles2Info, file, infEnt);
        switch(infoType){
            case url:
                placeIn(absURL2Info, file, infEnt);
                break;
            case absolute_ok:
            case absolute_bad:
            case bad_uri:
            case relative_notfound:
                //this is a warning condition
                placeIn(warningFiles2Info, file, infEnt);
                break;
                
            case relative_bad_found:
            case relative_bad_not_found:
                placeIn(errorFiles2Info, file, infEnt);
                break;
                
            case relative_ok:
                //nothing. Filter laters
                break;
        }
    }
    
    public void placeIn(Map<File, List<InfoCollector.InfoEntry>> files2Info, File file, InfoEntry infEnt) {
        List<InfoEntry> infEntList = files2Info.get(file);
        if(infEntList == null){
            List<InfoEntry> newEntList = new ArrayList<InfoEntry>();
            newEntList.add(infEnt);
            files2Info.put(file, newEntList);
        }else{
            infEntList.add(infEnt);
        }
    }
    
    public void makeGoodFileList() {
        for(File file : allFiles2Info.keySet()){
            //good files are files that do not belong to errors list
            if(!errorFiles2Info.containsKey(file))
                goodFileList.add(file);
        }
    }
    
    public InfoEntry analyze(File file, String refStr) {
        String rootURIStr = root.toURI().toString();
        URI fileURI = file.toURI();
        URI refURI = null;
        try {
            refURI = new URI(refStr);
        } catch (URISyntaxException ex) {
            return new InfoEntry(refStr, InfoType.bad_uri);
        } catch(NullPointerException npe){
            return new InfoEntry(refStr, InfoType.bad_uri);
        }
        if(refURI.isAbsolute()){
            if(refURI.getScheme().equalsIgnoreCase("http")) //NOI18N
                return new InfoEntry(refStr, InfoType.url);
            return new InfoEntry(refStr, InfoType.absolute_ok);
        }
        
        URI finalRes = fileURI.resolve(refURI);
        if(finalRes.toString().startsWith(rootURIStr)){
            File childFile = new File(finalRes);
            if(childFile.isFile())
                return new InfoEntry(refStr, InfoType.relative_ok);
            else
                return new InfoEntry(refStr, InfoType.relative_notfound);
        } else{
            File childfile = new File(finalRes);
            if(childfile.isFile())
                return new InfoEntry(refStr, InfoType.relative_bad_found);
            else
                return new InfoEntry(refStr, InfoType.relative_bad_not_found);
        }
    }
    
    public List<File> getCopyableFileList(){
        return goodFileList;
    }
    
    
    public Map<File, List<InfoEntry>> getWarnings(){
        return warningFiles2Info;
    }
    
    public Map<File, List<InfoEntry>> getErrors(){
        return errorFiles2Info;
    }
    
    Map<File, List<InfoEntry>> getAllEntries(){
        return allFiles2Info;
    }
    
    public boolean hasErrors(){
        if(errorFiles2Info.size() > 0)
            return true;
        return false;
    }
    
    public boolean hasWarnings(){
        if(warningFiles2Info.size() > 0)
            return true;
        return false;
    }
    
    public boolean hasReports(){
        if(hasErrors() || hasWarnings())
            return true;
        return false;
    }
    
    public static class InfoEntry {
        String childStr;
        InfoType infoType;
        public InfoEntry(String childStr, InfoType infoType){
            this.childStr = childStr;
            this.infoType = infoType;
        }
        
        public String getChildStr(){
            return childStr;
        }
        public InfoType getInfoType(){
            return infoType;
        }
        
        public String toString(){
            return "[Ref:"+childStr+", InfoType:"+infoType.toString()+"]"; //NOI18N
        }
    }

    public Map<File, List<InfoEntry>> getAbsURL2Info() {
        return absURL2Info;
    }
    
}
