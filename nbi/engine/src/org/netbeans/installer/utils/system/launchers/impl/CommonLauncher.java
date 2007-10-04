/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.system.launchers.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;

import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class CommonLauncher extends Launcher {
    private static final int BUF_SIZE = 102400;
    
    protected CommonLauncher(LauncherProperties pr) {
        super(pr);
    }
    protected long addData(FileOutputStream fos, InputStream is, Progress progress, long total) throws IOException{
        byte[] buffer = new byte[BUF_SIZE];
        int readBytes;
        int start = progress.getPercentage();
        long totalRead = 0;
        long perc = 0;
        while (is.available() > 0) {
            readBytes = is.read(buffer);
            totalRead += readBytes;
            fos.write(buffer, 0, readBytes);
            if(total!=0) {
                perc = (Progress.COMPLETE * totalRead) / total;
                progress.setPercentage(start + (int) perc);
            }
        }
        fos.flush();
        return totalRead;
    }
    
    protected long addData(FileOutputStream fos, File file, Progress progress, long total) throws IOException{
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return addData(fos,fis,progress,total);
        } finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch(IOException ex) {
                    LogManager.log(ex);
                }
            }
        }
        
    }
    
    //add rnd data
    protected void addData(FileOutputStream fos) throws IOException {
        double rand = Math.random() * Byte.MAX_VALUE;
        //fos.write(new byte[] {(byte)rand});
        fos.write(new byte[] {'#'});
    }
    
    protected long addString(FileOutputStream fos, String string, boolean isUnicode) throws IOException {
        byte [] bytes;
        if(isUnicode) {
            bytes = string.getBytes("UNICODE"); //NOI18N
        } else {
            bytes = string.getBytes();
        }
        fos.write(bytes);
        return bytes.length;
    }
    
    protected long addStringBuilder(FileOutputStream fos, StringBuilder builder, boolean isUnicode) throws IOException {
        return addString(fos, builder.toString() , isUnicode);
    }
    
    
    protected void checkAllParameters() throws IOException {
        checkBundledJars();
        checkJvmFile();
        checkOutputFileName();
        checkI18N();
        checkMainClass();
        checkTestJVMFile();
        checkTestJVMClass();
        checkCompatibleJava();
    }
    
    
    
    private void checkI18N() throws IOException {
        // i18n properties suffix
        LogManager.log(ErrorLevel.DEBUG, "Check i18n...");
        String suffix = getI18NResourcePrefix();
        if(i18nMap.isEmpty() && suffix!=null) {
            // load from engine`s entries list
            LogManager.log("... i18n properties were not set. using default from resources");
            InputStream is = ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST);
            String [] resources = StringUtils.splitByLines(
                    StreamUtils.readStream(is));
            List <String> list = new ArrayList <String> ();
            LogManager.log("... total engine resources: " + resources.length); //NOI18N
            for(String res : resources) {
                if(res.startsWith(suffix) &&
                        res.endsWith(FileUtils.PROPERTIES_EXTENSION)) {
                    list.add(res);
                }
            }
            LogManager.log("... total i18n resources: " + list.size()); //NOI18N
            setI18n(list);
        }
    }
    
    
    protected void checkBundledJars()  throws IOException  {
        LogManager.log(ErrorLevel.DEBUG, "Checking bundled jars...");
        for(LauncherResource f : jars) {
            if(f.isBundled()) {
                checkParameter("bundled JAR", f.getPath());
            }
        }
        if(jars.size()==0) {
            throw new IOException(ResourceUtils.getString(
                    CommonLauncher.class, ERROR_NO_JAR_FILES_KEY));
        }
    }
    
    protected void checkJvmFile()  throws IOException  {
        LogManager.log(ErrorLevel.DEBUG, "Checking JVMs...");
        for(LauncherResource file: jvms) {
            if(file.isBundled()) {
                InputStream is = null;
                try {
                    is = file.getInputStream();
                    if(is == null) {
                        throw new IOException(ResourceUtils.getString(
                                CommonLauncher.class,ERROR_CANNOT_FIND_JVM_FILE_KEY, file.getPath()));
                    }
                } finally {
                    if(is!=null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            LogManager.log(e);
                        }
                    }
                }
                
            }}
    }
    
    private void checkMainClass() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking main class...");
        // check main-class parameter
        // read main class from jar file if it is not specified
        if(mainClass==null) {
            // get the first            
            for(LauncherResource file : jars) {
                if(file.isBundled() && !file.isBasedOnResource()) {
                    JarFile jarFile = new JarFile(new File(file.getPath()));
                    Manifest manifest = jarFile.getManifest();
                    jarFile.close();
                    if(manifest!=null) {
                        mainClass = manifest.getMainAttributes().
                                getValue(Attributes.Name.MAIN_CLASS);
                    }
                    if(mainClass!=null) {
                        return;
                    }
                }
            }
            throw new IOException(ResourceUtils.getString(CommonLauncher.class, 
                    ERROR_MAIN_CLASS_UNSPECIFIED_KEY));
        } else{
            for(LauncherResource file : jars) {
                if(file.isBundled() && !file.isBasedOnResource() ) {
                    JarFile jarFile = new JarFile(new File(file.getPath()));
                    boolean mainClassExists = jarFile.getJarEntry(
                            mainClass.replace(".","/") + ".class") != null;
                    jarFile.close();
                    if(mainClassExists) {                        
                        return;
                    }                    
                } else {
                    return;
                }
            }
            
            throw new IOException(ResourceUtils.getString(CommonLauncher.class, 
                    ERROR_CANNOT_FIND_CLASS_KEY, mainClass));
        }
    }
    private void checkTestJVMClass() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking testJVM class...");
        if(testJVMClass==null) {
            testJVMClass = JavaUtils.TEST_JDK_CLASSNAME;
        }
    }
    
    private void checkParameter(String paramDescr, String parameter) throws IOException {
        if(parameter==null) {
            throw new IOException("Parameter " + paramDescr + " can`t be null");
        }
    }
    
    protected void checkParameter(String paramDescr, File parameter) throws IOException {
        if(parameter==null) {
            throw new IOException("Parameter " + paramDescr + " can`t be null");
        }
        if(!parameter.exists()) {
            throw new IOException(paramDescr + " doesn`t exist at " + parameter);
        }
    }
    
    protected void checkCompatibleJava() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking compatible java properties...");
        if(compatibleJava.isEmpty()) {
            compatibleJava.addAll(getDefaultCompatibleJava());
        }
        
    }
    protected void checkTestJVMFile()   throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking testJVM file...");
        if(testJVMFile==null) {
            testJVMFile = new LauncherResource(JavaUtils.TEST_JDK_RESOURCE);
        }
    }
    
    protected void checkOutputFileName() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking output file name...");
        if(outputFile==null) {
            LogManager.log(ErrorLevel.DEBUG, "... output file name is not specified, getting name from the first bundled file");
            String outputFileName  = null;
            for(LauncherResource file : jars) {
                if(file.isBundled() && !file.isBasedOnResource()) {
                    File jarFile = new File(file.getPath());
                    String name = jarFile.getName();
                    if(name.endsWith(FileUtils.JAR_EXTENSION)) {
                        outputFileName = name.substring(0,
                                name.lastIndexOf(FileUtils.JAR_EXTENSION));
                    }
                    outputFileName += getExtension();
                    outputFile = new File(jarFile.getParent(), outputFileName);
                    break;
                }
            }
            if(outputFile==null) {
                String exString = ResourceUtils.getString(CommonLauncher.class, ERROR_CANNOT_GET_OUTPUT_NAME_KEY);
                LogManager.log(exString);
                throw new IOException(exString);
            }
        } else if (addExtenstion) {
            LogManager.log(ErrorLevel.DEBUG, "... output is defined, adding extension");
            // outfile is defined but we need to set launcher-dependent extension
            outputFile = new File(outputFile.getParent(),
                    outputFile.getName() + getExtension());
            addExtenstion = false;
        }
        LogManager.log("... out file : " + outputFile); //NOI18N
    }
    protected String getJavaCounter(int counter) {
        return "{" + counter + "}";
    }
    protected long getBundledFilesSize() throws IOException {
        long total = 0;
        
        for (LauncherResource jvmFile : jvms) {
            total += jvmFile.getSize();
        }
        total += testJVMFile.getSize();
        
        for (LauncherResource jarFile : jars) {
            total += jarFile.getSize();
        }
        for(LauncherResource other : otherResources) {
            total += other.getSize();
        }
        return total;
    }
    protected long getBundledFilesNumber() {
        long total=0;
        for (LauncherResource jvmFile : jvms) {
            if ( jvmFile .isBundled()) {
                total ++;
            }
        }
        if(testJVMFile.isBundled()) {
            total++;
        }
        for (LauncherResource jarFile : jars) {
            if ( jarFile.isBundled()) {
                total ++;
            }
        }
        for (LauncherResource other : otherResources) {
            if (other.isBundled()) {
                total ++;
            }
        }
        return total;
    }
    
    private static final String ERROR_NO_JAR_FILES_KEY =
            "CnL.error.no.jars";//NOI18N
    private static final String ERROR_CANNOT_FIND_JVM_FILE_KEY =
            "CnL.error.cannot.find.jvm.file";//NOI18N
    private static final String ERROR_CANNOT_FIND_CLASS_KEY =
            "CnL.error.cannot.find.class";//NOI18N
    private static final String ERROR_MAIN_CLASS_UNSPECIFIED_KEY =
            "CnL.error.main.class.unspecified";//NOI18N
    private static final String ERROR_CANNOT_GET_OUTPUT_NAME_KEY =
            "CnL.error.cannot.get.output.name";//NOI18N
}
