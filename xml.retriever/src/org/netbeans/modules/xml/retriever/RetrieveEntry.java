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
package org.netbeans.modules.xml.retriever;

import java.io.File;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;


public class RetrieveEntry{
    //address from where this file was refered. Null for root file.
    private String baseAddress;
    //relative/abs address of the external entry
    private String currentAddress;
    //location of the base file
    private File localBaseFile = null;
    //location where this file has to be stroed or was stored
    private File saveFile = null;
    //retrieve this entry recursively
    private boolean recursive = false;
    //what is the type of this document.
    private DocumentTypesEnum docType = DocumentTypesEnum.schema;
    //final abs address from where the file was retrieved
    private String effectiveAddress = null;
    
    public RetrieveEntry(String baseAddress, String currentAddress, File localBaseFile){
        this.baseAddress = baseAddress;
        this.currentAddress = currentAddress;
        this.localBaseFile = localBaseFile;
    }
    
    public RetrieveEntry(String baseAddress, String currentAddress, File localBaseFile, File saveFile, DocumentTypesEnum docType, boolean recursive){
        this.baseAddress = baseAddress;
        this.currentAddress = currentAddress;
        this.localBaseFile = localBaseFile;
        this.saveFile = saveFile;
        this.setDocType(docType);
        this.setRecursive(recursive);
    }
    
    public String getBaseAddress() {
        return baseAddress;
    }
    
    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }
    
    public String getCurrentAddress() {
        return currentAddress;
    }
    
    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }
    
    public File getLocalBaseFile() {
        return localBaseFile;
    }
    
    public void setLocalBaseFile(File localBaseFile) {
        this.localBaseFile = localBaseFile;
    }
    
    public File getSaveFile() {
        return saveFile;
    }
    
    public void setSaveFile(File saveFile) {
        this.saveFile = saveFile;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public DocumentTypesEnum getDocType() {
        return docType;
    }

    public void setDocType(DocumentTypesEnum docType) {
        this.docType = docType;
    }
    //NOI18N
    public String toString(){
        return "base:" +this.baseAddress+
                "\n\tcur:" +this.currentAddress+
                "\n\tbFile:" +this.localBaseFile+
                "\n\tcFile:" +this.saveFile+
                "\n\tdType:" +this.docType+
                "\n\trec:"+this.recursive;
    }

    public String getEffectiveAddress() {
        return effectiveAddress;
    }

    public void setEffectiveAddress(String effectiveAddress) {
        this.effectiveAddress = effectiveAddress;
    }
}