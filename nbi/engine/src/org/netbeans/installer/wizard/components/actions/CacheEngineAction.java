/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Dmitry Lipin
 */
public class CacheEngineAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public CacheEngineAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        try {
            final Progress progress = new Progress();
            
            getWizardUi().setProgress(progress);
            
            cacheEngineLocally(progress);
        } catch (IOException e) {
            ErrorManager.notifyCritical("Cannot cache engine", e);
        }  catch (XMLException e) {
            ErrorManager.notifyCritical("Cannot cache engine", e);
        }
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }

    private void cacheEngineJar(Progress progress) throws IOException, XMLException {
        LogManager.log("... starting copying engine content to the new jar file");
        String [] entries = StreamUtils.readStream(
                ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST)).
                toString().split(StringUtils.NEW_LINE_PATTERN);
        
        File dest = getCacheExpectedFile();
        
        JarOutputStream jos = null;
        
        try {
            Manifest mf = new Manifest();
            mf.getMainAttributes().put(Attributes.Name.MAIN_CLASS, Installer.class.getName());
            mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, "");
            
            dest.getParentFile().mkdirs();
            jos = new JarOutputStream(new FileOutputStream(dest),mf);
            LogManager.log("... total entries : " + entries.length);
            for(int i=0;i<entries.length;i++) {
                progress.setPercentage((i * 100) /entries.length);
                String name = entries[i];
                if(name.length() > 0) {
                    String dataDir = EngineResources.DATA_DIRECTORY +
                            StringUtils.FORWARD_SLASH;
                    if(!name.startsWith(dataDir) || // all except "data/""
                            name.equals(dataDir) || // "data/"
                            name.equals(EngineResources.ENGINE_PROPERTIES)) { // "data/engine.properties"
                        jos.putNextEntry(new JarEntry(name));
                        if(!name.endsWith(StringUtils.FORWARD_SLASH)) {
                            StreamUtils.transferData(ResourceUtils.getResource(name), jos);
                        }
                    }
                }
            }
            LogManager.log("... adding content list and some other stuff");
            
            jos.putNextEntry(new JarEntry(
                    EngineResources.DATA_DIRECTORY + StringUtils.FORWARD_SLASH +
                    "registry.xml"));
            
            XMLUtils.saveXMLDocument(
                    Registry.getInstance().getEmptyRegistryDocument(),
                    jos);
            
            jos.putNextEntry(new JarEntry(EngineResources.ENGINE_CONTENTS_LIST));
            jos.write(StringUtils.asString(entries, SystemUtils.getLineSeparator()).getBytes());
        } finally {
            if(jos!=null) {
                try {
                    jos.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
                
            }
        }
        
        cachedEngine = (!dest.exists()) ? null : dest;
        
        LogManager.log("NBI Engine jar file = [" +
                cachedEngine + "], exist = " +
                ((cachedEngine==null) ? false : cachedEngine.exists()));
    }
    
    private File getCacheExpectedFile() {
        File localDirectory = new File(System.getProperty(
                Installer.LOCAL_DIRECTORY_PATH_PROPERTY));
        return new File(localDirectory, "nbi-engine.jar");
    }
    
    private void cacheEngineLocally(Progress progress) 
            throws IOException, XMLException {
        LogManager.logIndent("cache engine data locally to run uninstall in the future");
        
        String filePrefix = "file:";
        String httpPrefix = "http://";
        String jarSep     = "!/";
        
        String installerResource = Installer.class.getName().replace(".","/") + ".class";
        URL url = this.getClass().getClassLoader().getResource(installerResource);
        if(url == null) {
            throw new IOException("No main Installer class in the engine");
        }
        
        LogManager.log(ErrorLevel.DEBUG, "NBI Engine URL for Installer.Class = " + url);
        LogManager.log(ErrorLevel.DEBUG, "URL Path = " + url.getPath());
        
        boolean needCache = true;
        
        if("jar".equals(url.getProtocol())) {
            LogManager.log("... running engine as a .jar file");
            // we run engine from jar, not from .class
            String path = url.getPath();
            String jarLocation;
            
            if (path.startsWith(filePrefix)) {
                LogManager.log("... classloader says that jar file is on the disk");
                if (path.indexOf(jarSep) != -1) {
                    jarLocation = path.substring(filePrefix.length(),
                            path.indexOf(jarSep + installerResource));
                    jarLocation = URLDecoder.decode(jarLocation, StringUtils.ENCODING_UTF8);
                    File jarfile = new File(jarLocation);
                    LogManager.log("... checking if it runs from cached engine");
                    if(jarfile.getAbsolutePath().equals(
                            getCacheExpectedFile().getAbsolutePath())) {
                        needCache = false; // we already run cached version
                        cachedEngine = jarfile;
                    }
                    LogManager.log("... " + !needCache);
                } else {
                    throw new IOException("JAR path " + path +
                            " doesn`t contaion jar-separator " + jarSep);
                }
            } else if (path.startsWith(httpPrefix)) {
                LogManager.log("... classloader says that jar file is on remote server");
            }
        } else {
            // a quick hack to allow caching engine when run from the IDE (i.e.
            // as a .class) - probably to be removed later. Or maybe not...
            LogManager.log("... running engine as a .class file");
        }
        
        if (needCache) {
                cacheEngineJar(progress);
        }
        
        System.setProperty(
                EngineResources.LOCAL_ENGINE_PATH_PROPERTY,
                cachedEngine.getAbsolutePath());
        
        LogManager.logUnindent("... finished caching engine data");
    }
 
    
    private File cachedEngine ;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CacheEngineAction.class,
            "CEA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CacheEngineAction.class,
            "CEA.description"); // NOI18N
}
