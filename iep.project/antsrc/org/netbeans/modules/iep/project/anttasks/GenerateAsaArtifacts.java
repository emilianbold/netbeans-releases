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

package org.netbeans.modules.iep.project.anttasks;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPModelFactory;
import org.netbeans.modules.iep.model.lib.IOUtil;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;



/**
 *
 * @author blu
 */
public class GenerateAsaArtifacts extends Task {
    private String mSrcDirectoryLocation;
    private String mBuildDirectoryLocation;
    private String mJbiDescriptorFileLocation;
    
    /** Creates a new instance of GenerateIEPASAArtifacts */
    public GenerateAsaArtifacts() {
    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return mSrcDirectoryLocation;
    }
    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setSrcDirectoryLocation(String srcDirectoryLocation) {
        mSrcDirectoryLocation = srcDirectoryLocation;
    }
    
    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getBuildDirectoryLocation() {
        return mBuildDirectoryLocation;
    }
    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setBuildDirectoryLocation(String buildDirectoryLocation) {
        mBuildDirectoryLocation = buildDirectoryLocation;
    }
    
    /**
     * @return Returns the portMapFileLocation.
     */
    public String getJbiDescriptorFileLocation() {
        return mJbiDescriptorFileLocation;
    }
    /**
     * @param portMapFileLocation The portMapFileLocation to set.
     */
    public void setJbiDescriptorFileLocation(String jbiDescriptorFileLocation) {
        mJbiDescriptorFileLocation = jbiDescriptorFileLocation;
    }
    
    public void execute() throws BuildException {
        File srcDir = new File(mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new BuildException("Directory " + mSrcDirectoryLocation + " does not exit.");
        }
        String srcDirPath = srcDir.getAbsolutePath();
        //1: for appending '/'
        int srcDirPathLen = srcDir.getPath().length() + 1;
        String[] ext = new String[]{".iep"};
        List iepFiles = DirectoryUtil.getFilesRecursively(srcDir, ext);
        List portMapEntryList = new ArrayList();
        List nsList = new ArrayList();
        FileOutputStream fos = null;
        try {
            for (int i = 0, I = iepFiles.size(); i < I; i++) {
                File f = (File)iepFiles.get(i);
                String fPath = f.getPath();
                
                ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(FileUtil.toFileObject(f), true);
                if(modelSource != null) {
                    IEPModel model = IEPModelFactory.getDefault().getModel(modelSource);
//                  see com.sun.jbi.engine.iep.jbiadapter.IEPSEServiceUnitManager
                    //String ns = NameUtil.makeJavaId(fPath.substring(srcDirPathLen));
                    
                    //use qualified name
                    String ns = model.getQualifiedName();
                    
                    nsList.add(ns);
                    String tns = "ns" + (i + 1);
                    List<PortMapEntry> portMaps = Util.generatePortMapEntryList(model, tns);
//                  Generate PortMapEntries and append to portMapEntryList
                    portMapEntryList.addAll(portMaps);
                }
               
            }
            // Generate jbi.xml
            // <?xml version='1.0'?>
            // <jbi version="1.0" 
            //         xmlns="http://java.sun.com/xml/ns/jbi" 
            //         xmlns:ns0=${ns1} ... xmlns:nsN=${nsN} 
            //         version="1.0">
            //     <services binding-component="false">
            //         <provides interface-name=port-type service-name=partner-link endpoint-name=role-name/>
            //         <consumes interface-name=port-type service-name=partner-link endpoint-name=role-name link-type="standard"/>
            //     </services>
            // </jbi>
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("<!--start of generated code -->\n");
                sb.append("<jbi version=\"1.0\"\n");
                sb.append("        xmlns=\"http://java.sun.com/xml/ns/jbi\"\n");
                for (int i = 0, I = nsList.size(); i < I; i++) {
                    String ns = (String)nsList.get(i);
                    sb.append("        xmlns:ns" + (i + 1) + "=\"" + ns + "\"");
                    if (i < I - 1) {
                        sb.append("\n");
                    }
                }
                sb.append(">\n");
                sb.append("    <services binding-component=\"false\">\n");
                // Generate all <provides> first
                for (int i = 0, I = portMapEntryList.size(); i < I; i++) {
                    PortMapEntry pme = (PortMapEntry)portMapEntryList.get(i);
                    if (pme.getRole().equals(PortMapEntry.MY_ROLE)) {
                        sb.append("        <provides interface-name=\"" + pme.getPortType());
                        sb.append("\" service-name=\"" + pme.getPartnerLink());
                        sb.append("\" endpoint-name=\"" + pme.getRoleName());
                        sb.append("\"/>\n");
                    } 
                }
                // Generate all <consumes> second
                for (int i = 0, I = portMapEntryList.size(); i < I; i++) {
                    PortMapEntry pme = (PortMapEntry)portMapEntryList.get(i);
                    if (pme.getRole().equals(PortMapEntry.PARTNER_ROLE)) {
                        sb.append("        <consumes interface-name=\"" + pme.getPortType());
                        sb.append("\" service-name=\"" + pme.getPartnerLink());
                        sb.append("\" endpoint-name=\"" + pme.getRoleName());
                        sb.append("\" link-type=\"standard\"/>\n");
                    }
                }
                sb.append("    </services>\n");
                sb.append(" </jbi>\n");
                sb.append("<!--end of generated code -->\n");
                
                String content = sb.toString();
                fos = new FileOutputStream(mJbiDescriptorFileLocation);
                IOUtil.copy(content.getBytes("UTF-8"), fos);
            } catch (Exception e) {
                throw e;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }       
    }
    
    public static void main(String[] args) {
        GenerateAsaArtifacts tsk = new GenerateAsaArtifacts();
        tsk.setJbiDescriptorFileLocation("c:/temp/portMap.xml");
        tsk.setSrcDirectoryLocation("c:/temp");
        tsk.execute();
    }    
    
    
}
