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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.xtest.pes.dbfeeder;

import org.netbeans.xtest.xmlserializer.*;
import java.sql.*;
import java.io.*;
import org.netbeans.xtest.util.NetUtils;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.netbeans.xtest.util.SerializeDOM;

public class UploadMetadata implements XMLSerializable {

    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(UploadMetadata.class);
    static {
        try {        	
                // load global registry
                GlobalMappingRegistry.registerClassForElementName("PESdbUpload", UploadMetadata.class);
                // register this class
                //classMappingRegistry.registerSimpleField("team",ClassMappingRegistry.ATTRIBUTE,"team");
	        classMappingRegistry.registerSimpleField("mailContact",ClassMappingRegistry.ATTRIBUTE,"mailContact");
	        classMappingRegistry.registerSimpleField("timestamp",ClassMappingRegistry.ATTRIBUTE,"timestamp");
	        classMappingRegistry.registerContainerField("uploadedZips","uploadedZips",ClassMappingRegistry.SUBELEMENT);	        
	        classMappingRegistry.registerContainerSubtype("uploadedZips",String.class,"zipFile");
                classMappingRegistry.registerContainerField("webs","PESStatus",ClassMappingRegistry.SUBELEMENT);       
                classMappingRegistry.registerContainerSubtype("webs",WebStatus.class,"web");
        } catch (MappingException me) {
        	me.printStackTrace();
        	classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }
    
    // empty constructor - required by XMLSerializer
    public UploadMetadata() {}
    
    public UploadMetadata(String  mailContact) {        
        this.mailContact = mailContact;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    
    public static UploadMetadata loadUploadMetadata(File metadataFile) throws  XMLSerializeException {
        if (!metadataFile.isFile()) {
            throw new XMLSerializeException("Cannot load UploadMetadata from file "+metadataFile.getPath()+", file does not exist");
        }
        try {
            Document doc = SerializeDOM.parseFile(metadataFile);
            XMLSerializable xmlObject = XMLSerializer.getXMLSerializable(doc);
            if (xmlObject instanceof UploadMetadata) {
                UploadMetadata metadata = (UploadMetadata)xmlObject;
                return metadata;
            }
        } catch (IOException ioe) {
            throw new XMLSerializeException("IOException caught when loading UploadMetadata file:"+metadataFile.getPath(),ioe);
        }
        // xmlobject is not of required type
        throw new XMLSerializeException("Loaded xml document is not UploadMetadata");
    }
    
    public void saveUploadMetadata(File metadataFile) throws XMLSerializeException {
        try {
            Document doc = XMLSerializer.toDOMDocument(this);
            SerializeDOM.serializeToFile(doc, metadataFile);
        } catch (IOException ioe) {
            throw new XMLSerializeException("IOException caught when saving UploadMetadata file:"+metadataFile.getPath(),ioe);
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new XMLSerializeException("ParserConfigurationException caught when saving UploadMetadata file:"+metadataFile.getPath(),pce);
        }
    }
    
    
    private String mailContact;
    
    
    /*
     private String team;
    public String getTeam() {
        return team;
    }    
    */
    
    private Timestamp timestamp;
    
    private String[] uploadedZips;
    
    private WebStatus[] webs;
    
    /** Getter for property mailContact.
     * @return Value of property mailContact.
     *
     */
    public java.lang.String getMailContact() {
        return mailContact;
    }    
    

    
    /** Getter for property timestamp.
     * @return Value of property timestamp.
     *
     */
    public java.sql.Timestamp getTimestamp() {
        return timestamp;
    }
    
    /** Setter for property timestamp.
     * @param timestamp New value of property timestamp.
     *
     */
    /*
    public void setTimestamp(java.sql.Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    /*
    /** Getter for property uploadedZips.
     * @return Value of property uploadedZips.
     *
     */
    public java.lang.String[] getUploadedZipFilenames() {
        if (uploadedZips == null) {
            uploadedZips = new String[0];
        }
        return this.uploadedZips;
    }
    
    /** get uploaded zips as file objects
     */
    public File[] getUploadedZipFiles(File parentDir) {
        File[] zips = new File[getUploadedZipFilenames().length];        
        for (int i=0; i<uploadedZips.length; i++) {
            zips[i] = new File(parentDir, uploadedZips[i].trim());
        }
        return zips;
    }
    
    /** Setter for property uploadedZips.
     * @param uploadedZips New value of property uploadedZips.
     *
     */
    public void setUploadedZips(java.lang.String[] uploadedZips) {
        this.uploadedZips = uploadedZips;
    }    
    
    public void setUploadedZips(File[] uploadedZips) {
        String[] zipNames = new String[uploadedZips.length];        
        for (int i=0; i < uploadedZips.length; i++) {
            zipNames[i] = uploadedZips[i].getName();
        }
        this.uploadedZips = zipNames;
    }
    
    /* removes uploaded zip from the array 
     *
     */
    public boolean removeUploadedZip(String uploadedZip) {
        if (uploadedZips != null) {
            for (int i=0; i<uploadedZips.length; i++) {
                if (uploadedZip.equals(uploadedZips[i])) {
                    uploadedZips[i] = null;
                    // the array should be shrinked as well
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean removeUploadedZip(File uploadedZip) {
        return removeUploadedZip(uploadedZip.getName());
    }
    
    
    public String getMetadataFilename() {
        String hostname = NetUtils.getLocalHostName();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmmss");
        String timestamp = formatter.format(this.timestamp);
        return "pes-"+NetUtils.getLocalHostName()+"-"+timestamp+".xml";
    }
    
    /** Getter for property web.
     * @return Value of property web.
     *
     */
    public WebStatus[] getWebs() {
        return this.webs;
    }
    
    /** Setter for property web.
     * @param web New value of property web.
     *
     */
    public void setWebs(WebStatus[] webs) {
        this.webs = webs;
    }    
}

