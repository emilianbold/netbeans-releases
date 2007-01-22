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

package org.netbeans.modules.j2me.cdc.platform.sun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.netbeans.modules.j2me.cdc.platform.spi.StreamReader;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author suchys
 */
public class SunPlatformDetector extends CDCPlatformDetector {
    
    /** Creates a new instance of CDCPlatform */
    public SunPlatformDetector() {
    }

    public String getPlatformName() {
        return "SavaJe"; //NOI18N
    }

    public String getPlatformType() {
        return "savaje";
    }    

    public boolean accept(FileObject dir) {
        FileObject tool = CDCPlatformUtil.findTool("bin","emulator", Collections.singleton(dir));  //NOI18N
        FileObject tool2 = CDCPlatformUtil.findTool("lib","emulator.properties", Collections.singleton(dir));  //NOI18N
        return tool != null && tool2 != null;
    }

    public CDCPlatform detectPlatform(FileObject dir) throws IOException {
        assert dir != null;
        FileObject java = CDCPlatformUtil.findTool("bin","emulator", Collections.singleton(dir)); //NOI18N
        if (java == null){
            throw new IOException("emulator.exe can not be found in desired location!"); //NOI18N
        }
        File javaFile = FileUtil.toFile (java);
        if (javaFile == null)
            throw new IOException("emulator.exe can not be found in desired location!"); //NOI18N
        String javapath = javaFile.getAbsolutePath();
            
        FileObject bin = dir.getFileObject("bin"); //NOI18N        

        StringBuffer sb = new StringBuffer();
        try {
            String[] command = new String[2];
            command[0] = javapath;
            command[1] = "-version"; //NOI18N
            final Process process = Runtime.getRuntime().exec(command, null, FileUtil.toFile(bin));
            StreamReader ior = new StreamReader (process.getInputStream(), sb);
            StringBuffer err = new StringBuffer();
            StreamReader irr = new StreamReader (process.getErrorStream(), err);

            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            ior.join();
            irr.join();
            process.waitFor();
	    process.getOutputStream().close();
            int exitValue = process.exitValue();
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
        
        //search for libraries
        FileObject libBin = dir.getFileObject("lib"); //NOI18N            
        FileObject foProps = libBin.getFileObject("emulator", "properties"); //NOI18N
        
        String name = sb.toString();
        Properties props = new Properties();
        String bcp;
        InputStream is = foProps.getInputStream();
        try {
            props.load(is);
        } finally {
            if (is != null){
                is.close();
            }
        }
        
        name = props.get("emulator.name") != null ? (String) props.get("emulator.name")  : name; //NOI18N        
        
        List devices = new ArrayList();
        String dvcs = (String) props.get("device.list"); //NOI18N
        assert dvcs != null;
        StringTokenizer st = new StringTokenizer(dvcs, ",");
       
        Map modes = new HashMap();
        modes.put(CDCPlatform.PROP_EXEC_MAIN, null);
        modes.put(CDCPlatform.PROP_EXEC_XLET, null);
//        modes.put(CDCPlatform.PROP_EXEC_APPLET, null); //no applet, there is no PP !!!

        while( st.hasMoreTokens() ){
            String deviceName = st.nextToken().trim();
            String apis = (String) props.get("api.list"); //NOI18N
            assert apis != null;
            StringTokenizer stapis = new StringTokenizer(apis, ","); //NOI18N
            List profiles = new ArrayList();
            while(stapis.hasMoreTokens()){
                String api = stapis.nextToken().trim();
                CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile( api, api, api.substring(api.indexOf('-') + 1), modes, 
                        getClassPathForConfiguration(libBin, (String) props.get(api + ".classpath.build")),
                        getClassPathForConfiguration(libBin, (String) props.get(api + ".classpath.run")),
                        api.startsWith("AGUI")? true : false);
                profiles.add( profile );
            }
            devices.add(new CDCDevice(deviceName, deviceName, (CDCDevice.CDCProfile[]) profiles.toArray(new CDCDevice.CDCProfile[profiles.size()]), null));
        }
        
       
        //CDCDevice.Screen screen = new CDCDevice.Screen(width, height, bitDepth, isColor, "false", "true"); //NOI18N

        List jdocs = new ArrayList();
        FileObject base = dir.getFileObject("docs"); //NOI18N
        if (base != null){
            findJavaDoc(base, jdocs);
        }
        
        return new CDCPlatform(name, name, getPlatformType(), "1.4",       //NOI18N
                Collections.singletonList(dir.getURL()), 
                Collections.EMPTY_LIST, 
                jdocs, 
                (CDCDevice[]) devices.toArray(new CDCDevice[devices.size()]), true);        
    }

    private static void findJavaDoc(FileObject folder, List folders){
        if (folder == null)
            return;
        FileObject[] fo = folder.getChildren();
        for (int i = 0; i < fo.length; i++) {
            if (fo[i].isData() && "index".equals(fo[i].getName())){
                folders.add(fo[i].getParent());
            }
        }
        for (int i = 0; i < fo.length; i++) {
            if (fo[i].isFolder() && !folders.contains(fo[i].getParent())){
                findJavaDoc(fo[i], folders);
            }
        }
    }
    
    private String getClassPathForConfiguration(FileObject libBin, String items){
        Set set = new HashSet();
        StringTokenizer st = new StringTokenizer(items, ";");
        while(st.hasMoreTokens()){
            set.add(st.nextToken());
        }
        
        StringBuffer bcp  = new StringBuffer();
        for (Enumeration children = libBin.getChildren(true); children.hasMoreElements();) {
            FileObject elem = (FileObject) children.nextElement();
            if (set.contains(elem.getNameExt())){
                bcp.append(FileUtil.toFile(elem).getAbsolutePath());
                if (children.hasMoreElements()){
                    bcp.append(';');
                }
            }
        }
        return bcp.toString();
    }
    
    public int getVersion() {
        return 1;
    }
}
