/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.cnd.api.utils.Path;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Alexander Simon
 */
public final class ToolchainManager {
    private static final boolean TRACE = Boolean.getBoolean("cnd.toolchain.personality.trace"); // NOI18N
    private static final boolean CREATE_SHADOW = Boolean.getBoolean("cnd.toolchain.personality.create_shadow"); // NOI18N
    private static final ToolchainManager instance = new ToolchainManager();
    private List<ToolchainDescriptor> descriptors = new ArrayList<ToolchainDescriptor>();
    private Logger log = Logger.getLogger("cnd.toolchain.logger");
    
    static final ToolchainManager getInstance(){
        return instance;
    }
    
    private ToolchainManager(){
        try {
            Map<Integer,CompilerVendor> vendors = new TreeMap<Integer,CompilerVendor>();
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            FileObject folder = fs.findResource("Services/CndToolChain"); //NOI18N
            int indefinedID = Integer.MAX_VALUE/2;
            if (folder != null && folder.isFolder()) {
                FileObject[] files = folder.getChildren();
                for(FileObject file : files){
                    CompilerVendor v = new CompilerVendor(file.getNameExt());
                    Integer position = (Integer) file.getAttribute("position"); // NOI18N
                    if (position == null || vendors.containsKey(position)) {
                        position = new Integer(indefinedID++);
                    }
                    if (read(file, files, v, new HashSet<FileObject>())){
                        vendors.put(position, v);
                    }
                }
            }
            if (TRACE) System.out.println("Declared vendors:"); // NOI18N
            for(CompilerVendor v : vendors.values()){
                if (TRACE) System.out.println(v.toString());
                descriptors.add(new ToolchainDescriptorImpl(v));
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
        if (CREATE_SHADOW){
            writeToolchains();
        }
    }

    ToolchainDescriptor getToolchain(String name, int platform){
        ToolchainDescriptor nonePlatform = null;
        for (ToolchainDescriptor d : descriptors){
            if (isPlatforSupported(platform, d)){
                if (name.equals(d.getName())){
                    return d;
                }
            } 
            if (nonePlatform == null && isPlatforSupported(PlatformTypes.PLATFORM_NONE, d)) {
                nonePlatform = d;
            }
        }
        return nonePlatform;
    }

    List<ToolchainDescriptor> getAllToolchains(){
        return new ArrayList<ToolchainDescriptor>(descriptors);
    }

        List<ToolchainDescriptor> getToolchains(int platform){
        List<ToolchainDescriptor> res = new ArrayList<ToolchainDescriptor>();
        for(ToolchainDescriptor d : descriptors){
            if (isPlatforSupported(platform, d)) {
                res.add(d);
            }
        }
        return res;
    }

    boolean isPlatforSupported(int platform, ToolchainDescriptor d) {
        if (!releaseFileMatch(d)) {
            return false;
        }
        switch (platform) {
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                for(String p : d.getPlatforms()){
                    if ("sun_sparc".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                for(String p : d.getPlatforms()){
                    if ("sun_intel".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_LINUX:
                for(String p : d.getPlatforms()){
                    if ("linux".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_WINDOWS:
                for(String p : d.getPlatforms()){
                    if ("windows".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_MACOSX:
                for(String p : d.getPlatforms()){
                    if ("mac".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_GENERIC:
                for(String p : d.getPlatforms()){
                    if ("unix".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
            case PlatformTypes.PLATFORM_NONE:
                for(String p : d.getPlatforms()){
                    if ("none".equals(p)){ // NOI18N
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    /**
     * Check a file for a file for a specified pattern. This is typically used to search /etc/release on
     * Unix systems (many Unix' have an /etc/release* file). This lets us fine tune a toolchain for a
     * specific version of a Unix distribution.
     *
     * This method was written specifically to ensure that /opt/SunStudioExpress is <b>only</b> used on
     * OpenSolaris systems.
     */
    private boolean releaseFileMatch(ToolchainDescriptor d) {
        String releaseFile = d.getReleaseFile();
        String releasePattern = d.getReleasePattern();
        String line;

        if (releaseFile != null && releasePattern != null) {
            File file = new File(releaseFile);
            if (file.exists()) {
                Pattern pattern = Pattern.compile(releasePattern);
                try {
                    BufferedReader in = new BufferedReader(new FileReader(releaseFile));

                    while ((line = in.readLine()) != null) {
                        if (pattern.matcher(line).find()) {
                            return true;
                        }
                    }
                    in.close();
                } catch (Exception ex) {
                    log.warning("Excetpiont reading releae file [" + releaseFile + "] for " + d.getName());
                }
            }
            return false;
        }
        return true;
    }
    
    boolean isMyFolder(String path, ToolchainDescriptor d, int platform){
        boolean res = isMyFolderImpl(path, d, platform);
        if (TRACE && res) System.out.println("Path ["+path+"] belongs to tool chain "+d.getName()); // NOI18N
        return res;
    }
    
    private boolean isMyFolderImpl(String path, ToolchainDescriptor d, int platform){
        CompilerDescriptor c = d.getC();
        if (c == null || c.getNames().length == 0) {
            return false;
        }
        Pattern pattern = null;
        if (c.getPathPattern() != null) {
            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                pattern = Pattern.compile(c.getPathPattern(), Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(c.getPathPattern());
            }
        }
        if (pattern != null) {
            if (!pattern.matcher(path).find()){
                String f = c.getExistFolder();
                if (f == null) {
                    return false;
                }
                File folder = new File(path+"/"+f); // NOI18N
                if (!folder.exists() || !folder.isDirectory()) {
                    return false;
                }
            }
        }
        File file = new File(path+"/"+c.getNames()[0]); // NOI18N
        if (!file.exists()) {
            file = new File(path+"/"+c.getNames()[0]+".exe"); // NOI18N
            if (!file.exists()) {
                return false;
            }
        }
       String flag = c.getVersionFlags();
        if (flag == null) {
            return true;
        }
        if (c.getVersionPattern() == null) {
            return true;
        }
        pattern = Pattern.compile(c.getVersionPattern());
        String s = getCommandOutput(path, path+"/"+c.getNames()[0]+" "+flag, true); // NOI18N
        boolean res = pattern.matcher(s).find();
        if (TRACE && !res) System.out.println("No match for pattern ["+c.getVersionPattern()+"]:" ); // NOI18N
        if (TRACE && !res) System.out.println("Run "+path+"/"+c.getNames()[0]+" "+flag+"\n"+s); // NOI18N
        return res;
    }

    String getBaseFolder(ToolchainDescriptor d, int platform) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS) {
            return null;
        }
        String pattern = d.getBaseFolderPattern();
        String key = d.getBaseFolderKey();
        if (key == null || pattern == null) {
            return null;
        }
        String base = readRegestry(key, pattern);
        if (base != null && d.getBaseFolderSuffix() != null){
            base += "/"+d.getBaseFolderSuffix(); // NOI18N
        }
        return base;
    }

    String getCommandFolder(ToolchainDescriptor d, int platform) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS) {
            return null;
        }
        String pattern = d.getCommandFolderPattern();
        String key = d.getCommandFolderKey();
        if (key == null || pattern == null) {
            return null;
        }
        String base = readRegestry(key, pattern);
        if (base != null && d.getCommandFolderSuffix() != null){
            base += "\\"+d.getCommandFolderSuffix(); // NOI18N
        }
        // search for unregistered msys
        if (base == null) {
            pattern = d.getCommandFolderPathPattern();
            if (pattern != null && pattern.length() > 0 ) {
                Pattern p = Pattern.compile(pattern);
                for (String dir : Path.getPath()) {
                    if (p.matcher(dir).find()) {
                        base = dir;
                        break;
                    }
                }
            }
        }
        return base;
    }

    private String readRegestry(String key, String pattern) {
        List<String> list = new ArrayList<String>();
        list.add("C:/Windows/System32/reg.exe"); // NOI18N
        list.add("query"); // NOI18N
        list.add(key);
        list.add("/s"); // NOI18N
        ProcessBuilder pb = new ProcessBuilder(list);
        pb.redirectErrorStream(true);
        String base = null;
        try {
            if (TRACE) System.out.println("Read registry "+key); // NOI18N
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Pattern p = Pattern.compile(pattern);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (TRACE) System.out.println("\t"+line); // NOI18N
                Matcher m = p.matcher(line);
                if (m.find() && m.groupCount() == 1) {
                    base = m.group(1).trim();
                    if (TRACE) System.out.println("\tFound "+base); // NOI18N
                }
            }
        } catch (Exception ex) {
            if (TRACE) ex.printStackTrace();
        }
        return base;
    }

    private static String getCommandOutput(String path, String command, boolean stdout) {
        StringBuilder buf = new StringBuilder();
        if (path == null) {
            path = ""; // NOI18N
        }
        ArrayList<String> envp = new ArrayList<String>();
        for (String key : System.getenv().keySet()) {
            String value = System.getenv().get(key);
            if (key.equals(Path.getPathName())) {
                envp.add(Path.getPathName() + "=" + path + File.pathSeparatorChar + value); // NOI18N
            } else {
                String entry = key + "=" + (value != null ? value : ""); // NOI18N
                envp.add(entry);
            }
        }
        try {
            Process process = Runtime.getRuntime().exec(command, envp.toArray(new String[envp.size()])); // NOI18N
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                    buf.append('\n'); // NOI18N
                }
            } catch (IOException ex) {
                if (TRACE) ex.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                if (TRACE) ex.printStackTrace();
            }
            is = process.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(is));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                    buf.append('\n'); // NOI18N
                }
            } catch (IOException ex) {
                if (TRACE) ex.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                if (TRACE) ex.printStackTrace();
            }
        } catch (IOException ex) {
            if (TRACE) ex.printStackTrace();
        }
        return buf.toString();
    }
    
    private boolean read(FileObject file, FileObject[] files, CompilerVendor v, Set<FileObject> antiloop) {
        if (antiloop.contains(file)) {
            return false;
        }
        antiloop.add(file);
        String baseName = (String) file.getAttribute("extends"); // NOI18N
        if (baseName != null && baseName.length() > 0) {
            for(FileObject base : files){
                if (baseName.equals(base.getName())){
                    if (!read(base, files, v, antiloop)) {
                        return false;
                    }
                }
            }
        }
        try {
            read(file.getInputStream(),v);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    private boolean read(InputStream inputStream, CompilerVendor v) {
	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setValidating(false);
	XMLReader xmlReader = null;
	try {
	    SAXParser saxParser = spf.newSAXParser();
	    xmlReader = saxParser.getXMLReader();
	} catch(Exception ex) {
            ex.printStackTrace();
	    return false;
	}
        SAXHandler handler = new SAXHandler(v);
	xmlReader.setContentHandler(handler);

	try {
            InputSource source = new InputSource(inputStream);
            xmlReader.parse(source);
	} catch (Exception ex) {
            ex.printStackTrace();
	    return false;
	}
	return true;
    }

    private String unsplit(String [] array){
        if (array == null) {
            return ""; // NOI18N
        }
        StringBuilder buf = new StringBuilder();
        for(String s:array){
            if (buf.length()>0){
                buf.append(',');
            }
            buf.append(s);
        }
        return buf.toString();
    }

    private void writeToolchains(){
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject folder = fs.findResource("Services/CndToolChain"); //NOI18N
        if (folder != null && folder.isFolder()) {
            FileObject[] files = folder.getChildren();
            for(FileObject file : files){
                String name = file.getNameExt();
                for(ToolchainDescriptor descriptor : descriptors){
                    if (name.equals(descriptor.getFileName())){
                        System.out.println("Found file " + file.getNameExt()); // NOI18N
                        Document doc = XMLUtil.createDocument("toolchaindefinition", "http://www.netbeans.org/ns/cnd-toolchain-definition/1", null, null); // NOI18N
                        Element root = doc.getDocumentElement();
                        Element element;
                        element = doc.createElement("toolchain"); // NOI18N
                        element.setAttribute("name", descriptor.getName()); // NOI18N
                        element.setAttribute("display", descriptor.getDisplayName()); // NOI18N
                        element.setAttribute("family", unsplit(descriptor.getFamily())); // NOI18N
                        root.appendChild(element);

                        element = doc.createElement("platforms"); // NOI18N
                        element.setAttribute("stringvalue", unsplit(descriptor.getPlatforms())); // NOI18N
                        root.appendChild(element);

                        if (descriptor.getDriveLetterPrefix() != null) {
                            element = doc.createElement("drive_letter_prefix"); // NOI18N
                            element.setAttribute("stringvalue", descriptor.getDriveLetterPrefix()); // NOI18N
                            root.appendChild(element);
                        }

                        if (descriptor.getBaseFolderKey() != null ||
                            descriptor.getBaseFolderPattern() != null ||
                            descriptor.getBaseFolderPathPattern() != null ||
                            descriptor.getBaseFolderSuffix() != null) {
                            element = doc.createElement("base_folder"); // NOI18N
                            if (descriptor.getBaseFolderKey() != null) {
                                element.setAttribute("regestry", descriptor.getBaseFolderKey()); // NOI18N
                            }
                            if (descriptor.getBaseFolderPattern() != null) {
                                element.setAttribute("pattern", descriptor.getBaseFolderPattern()); // NOI18N
                            }
                            if (descriptor.getBaseFolderPathPattern() != null) {
                                element.setAttribute("path_patern", descriptor.getBaseFolderPathPattern()); // NOI18N
                            }
                            if (descriptor.getBaseFolderSuffix() != null) {
                                element.setAttribute("suffix", descriptor.getBaseFolderSuffix()); // NOI18N
                            }
                            root.appendChild(element);
                        }

                        if (descriptor.getCommandFolderKey() != null ||
                            descriptor.getCommandFolderPattern() != null ||
                            descriptor.getCommandFolderPathPattern() != null ||
                            descriptor.getCommandFolderSuffix() != null) {
                            element = doc.createElement("command_folder"); // NOI18N
                            if (descriptor.getCommandFolderKey() != null) {
                                element.setAttribute("regestry", descriptor.getCommandFolderKey()); // NOI18N
                            }
                            if (descriptor.getCommandFolderPattern() != null) {
                                element.setAttribute("pattern", descriptor.getCommandFolderPattern()); // NOI18N
                            }
                            if (descriptor.getCommandFolderPathPattern() != null) {
                                element.setAttribute("path_patern", descriptor.getCommandFolderPathPattern()); // NOI18N
                            }
                            if (descriptor.getCommandFolderSuffix() != null) {
                                element.setAttribute("suffix", descriptor.getCommandFolderSuffix()); // NOI18N
                            }
                            root.appendChild(element);
                        }

                        CompilerDescriptor compiler;
                        compiler = descriptor.getC();
                        if (compiler != null) {
                            element = doc.createElement("c"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }

                        compiler = descriptor.getCpp();
                        if (compiler != null) {
                            element = doc.createElement("cpp"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }

                        compiler = descriptor.getFortran();
                        if (compiler != null) {
                            element = doc.createElement("fortran"); // NOI18N
                            writeCompiler(doc, element, compiler);
                            root.appendChild(element);
                        }
                        
                        ScannerDescriptor scanner = descriptor.getScanner();
                        if (scanner != null){
                            element = doc.createElement("scanner"); // NOI18N
                            writeScanner(doc, element, scanner);
                            root.appendChild(element);
                        }
                        
                        LinkerDescriptor linker = descriptor.getLinker();
                        if (linker != null){
                            element = doc.createElement("linker"); // NOI18N
                            writeLinker(doc, element, linker);
                            root.appendChild(element);
                        }
                        
                        MakeDescriptor make = descriptor.getMake();
                        if (make != null){
                            element = doc.createElement("make"); // NOI18N
                            writeMake(doc, element, make);
                            root.appendChild(element);
                        }

                        DebuggerDescriptor debugger = descriptor.getDebugger();
                        if (debugger != null){
                            element = doc.createElement("debugger"); // NOI18N
                            writeDebugger(doc, element, debugger);
                            root.appendChild(element);
                        }
                        try {
                            FileLock lock = file.lock();
                            try {
                                OutputStream os = file.getOutputStream(lock);
                                try {
                                    XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                                    file.setAttribute("extends", ""); // NOI18N
                                } finally {
                                    os.close();
                                }
                            } finally {
                                lock.releaseLock();
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }

    private void writeCompiler(Document doc, Element element, CompilerDescriptor compiler){
        Element e;
        e = doc.createElement("compiler"); // NOI18N
        e.setAttribute("name", unsplit(compiler.getNames())); // NOI18N
        element.appendChild(e);
        if (compiler.getPathPattern() != null ||
            compiler.getExistFolder() != null) {
            e = doc.createElement("recognizer"); // NOI18N
            if (compiler.getPathPattern() != null) {
                e.setAttribute("pattern", compiler.getPathPattern()); // NOI18N
            }
            if (compiler.getExistFolder() != null) {
                e.setAttribute("or_exist_folder", compiler.getExistFolder()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getVersionFlags() != null ||
            compiler.getVersionPattern() != null) {
            e = doc.createElement("version"); // NOI18N
            if (compiler.getVersionFlags() != null) {
                e.setAttribute("flags", compiler.getVersionFlags()); // NOI18N
            }
            if (compiler.getVersionPattern() != null) {
                e.setAttribute("pattern", compiler.getVersionPattern()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getIncludeFlags() != null ||
            compiler.getIncludeParser() != null ||
            compiler.getRemoveIncludePathPrefix() != null ||
            compiler.getRemoveIncludeOutputPrefix() != null) {
            e = doc.createElement("system_include_paths"); // NOI18N
            if (compiler.getIncludeFlags() != null) {
                e.setAttribute("flags", compiler.getIncludeFlags()); // NOI18N
            }
            if (compiler.getIncludeParser() != null) {
                e.setAttribute("parser", compiler.getIncludeParser()); // NOI18N
            }
            if (compiler.getRemoveIncludePathPrefix() != null) {
                e.setAttribute("remove_in_path", compiler.getRemoveIncludePathPrefix()); // NOI18N
            }
            if (compiler.getRemoveIncludeOutputPrefix() != null) {
                e.setAttribute("remove_in_output", compiler.getRemoveIncludeOutputPrefix()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getUserIncludeFlag() != null) {
            e = doc.createElement("user_include"); // NOI18N
            e.setAttribute("flags", compiler.getUserIncludeFlag()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getMacroFlags() != null ||
            compiler.getMacroParser() != null) {
            e = doc.createElement("system_macros"); // NOI18N
            if (compiler.getMacroFlags() != null) {
                e.setAttribute("flags", compiler.getMacroFlags()); // NOI18N
            }
            if (compiler.getMacroParser() != null) {
                e.setAttribute("parser", compiler.getMacroParser()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getUserMacroFlag() != null) {
            e = doc.createElement("user_macro"); // NOI18N
            e.setAttribute("flags", compiler.getUserMacroFlag()); // NOI18N
            element.appendChild(e);
        }
        writeDevelopmentMode(doc, element, compiler);
        writeWarningLevel(doc, element, compiler);
        writeArchitecture(doc, element, compiler);
        if (compiler.getStripFlag() != null) {
            e = doc.createElement("strip"); // NOI18N
            e.setAttribute("flags", compiler.getStripFlag()); // NOI18N
            element.appendChild(e);
        }
        writeMultithreading(doc, element, compiler);
        writeStandard(doc, element, compiler);
        writeLanguageExtension(doc, element, compiler);
        writeLibrary(doc, element, compiler);
        if (compiler.getDependencyGenerationFlags() != null) {
            e = doc.createElement("dependency_generation"); // NOI18N
            e.setAttribute("flags", compiler.getDependencyGenerationFlags()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getPrecompiledHeaderFlags() != null ||
            compiler.getPrecompiledHeaderSuffix() != null) {
            e = doc.createElement("precompiled_header"); // NOI18N
            if (compiler.getPrecompiledHeaderFlags() != null) {
                e.setAttribute("flags", compiler.getPrecompiledHeaderFlags()); // NOI18N
            }
            if (compiler.getPrecompiledHeaderSuffix() != null) {
                e.setAttribute("suffix", compiler.getPrecompiledHeaderSuffix()); // NOI18N
            }
            if (compiler.getPrecompiledHeaderSuffixAppend()) {
                e.setAttribute("append", "true"); // NOI18N
            }
            element.appendChild(e);
        }
    }
    
    private void writeDevelopmentMode(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getDevelopmentModeFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.developmentMode.default_selection;
        }
        Element e = doc.createElement("development_mode"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"fast_build", "debug", "performance_debug", // NOI18N
                                      "test_coverage", "diagnosable_release", "release", // NOI18N
                                      "performance_release"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeWarningLevel(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getWarningLevelFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.warningLevel.default_selection;
        }
        Element e = doc.createElement("warning_level"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"no_warnings", "default", "more_warnings", // NOI18N
                                      "warning2error"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeArchitecture(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getArchitectureFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.architecture.default_selection;
        }
        Element e = doc.createElement("architecture"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"default", "bits_32", "bits_64"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeMultithreading(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getMultithreadingFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.multithreading.default_selection;
        }
        Element e = doc.createElement("multithreading"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "safe", "automatic", "open_mp"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeStandard(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getStandardFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.standard.default_selection;
        }
        Element e = doc.createElement("standard"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"old", "legacy", "default", "modern"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeLanguageExtension(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getLanguageExtensionFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.languageExtension.default_selection;
        }
        Element e = doc.createElement("language_extension"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "default", "all"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeLibrary(Document doc, Element element, CompilerDescriptor compiler){
        String[] flags = compiler.getLibraryFlags();
        if (flags == null){
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl)compiler).tool.library.default_selection;
        }
        Element e = doc.createElement("library"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "runtime", "classic", // NOI18N
                                      "binary_standard","conforming_standard"}; // NOI18N
        for(int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i){
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeScanner(Document doc, Element element, ScannerDescriptor scanner) {
        Element c;
        for(ScannerPattern pattern : scanner.getPatterns()){
            c = doc.createElement(pattern.getSeverity());
            c.setAttribute("pattern", pattern.getPattern()); // NOI18N
            if (pattern.getLanguage() != null) {
                c.setAttribute("language", pattern.getLanguage()); // NOI18N
            }
            element.appendChild(c);
        }
        if (scanner.getStackHeaderPattern() != null){
            c = doc.createElement("stack_header"); // NOI18N
            c.setAttribute("pattern", scanner.getStackHeaderPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getStackNextPattern() != null){
            c = doc.createElement("stack_next"); // NOI18N
            c.setAttribute("pattern", scanner.getStackNextPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getEnterDirectoryPattern() != null){
            c = doc.createElement("enter_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getEnterDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getChangeDirectoryPattern() != null){
            c = doc.createElement("change_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getChangeDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getLeaveDirectoryPattern() != null){
            c = doc.createElement("leave_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getLeaveDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeLinker(Document doc, Element element, LinkerDescriptor linker) {
        Element c;
        if (linker.getLibraryPrefix() != null){
            c = doc.createElement("library_prefix"); // NOI18N
            c.setAttribute("stringvalue", linker.getLibraryPrefix()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getLibrarySearchFlag() != null){
            c = doc.createElement("library_search"); // NOI18N
            c.setAttribute("flags", linker.getLibrarySearchFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibrarySearchFlag() != null){
            c = doc.createElement("dynamic_library_search"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibrarySearchFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getLibraryFlag() != null){
            c = doc.createElement("library"); // NOI18N
            c.setAttribute("flags", linker.getLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getPICFlag() != null){
            c = doc.createElement("PIC"); // NOI18N
            c.setAttribute("flags", linker.getPICFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getStaticLibraryFlag() != null){
            c = doc.createElement("static_library"); // NOI18N
            c.setAttribute("flags", linker.getStaticLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibraryFlag() != null){
            c = doc.createElement("dynamic_library"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibraryBasicFlag() != null){
            c = doc.createElement("dynamic_library_basic"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibraryBasicFlag()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeMake(Document doc, Element element, MakeDescriptor make) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(make.getNames())); // NOI18N
        element.appendChild(c);

        if (make.getVersionFlags() != null ||
            make.getVersionPattern() != null){
            c = doc.createElement("version"); // NOI18N
            if (make.getVersionFlags() != null) {
                c.setAttribute("flags", make.getVersionFlags()); // NOI18N
            }
            if (make.getVersionPattern() != null) {
                c.setAttribute("pattern", make.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }

        if (make.getDependencySupportCode() != null){
            c = doc.createElement("dependency_support"); // NOI18N
            c.setAttribute("code", make.getDependencySupportCode()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeDebugger(Document doc, Element element, DebuggerDescriptor debugger) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(debugger.getNames())); // NOI18N
        element.appendChild(c);
        if (debugger.getVersionFlags() != null ||
            debugger.getVersionPattern() != null){
            c = doc.createElement("version"); // NOI18N
            if (debugger.getVersionFlags() != null) {
                c.setAttribute("flags", debugger.getVersionFlags()); // NOI18N
            }
            if (debugger.getVersionPattern() != null) {
                c.setAttribute("pattern", debugger.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
    }

    public interface ToolchainDescriptor {
        String getFileName();
        String getName();
        String getDisplayName();
        String getReleaseFile();
        String getReleasePattern();
        String[] getFamily();
        String[] getPlatforms();
        String getDriveLetterPrefix();
        String getBaseFolderKey();
        String getBaseFolderPattern();
        String getBaseFolderSuffix();
        String getBaseFolderPathPattern();
        String getCommandFolderKey();
        String getCommandFolderPattern();
        String getCommandFolderSuffix();
        String getCommandFolderPathPattern();
        CompilerDescriptor getC();
        CompilerDescriptor getCpp();
        CompilerDescriptor getFortran();
        ScannerDescriptor getScanner();
        LinkerDescriptor getLinker();
        MakeDescriptor getMake();
        Map<String, String> getDefaultLocations();
        DebuggerDescriptor getDebugger();
    }

    public interface ToolDescriptor {
        String[] getNames();
        String getVersionFlags();
        String getVersionPattern();
    }

    public interface CompilerDescriptor extends ToolDescriptor {
        String getPathPattern();
        String getExistFolder();
        String getIncludeFlags();
        String getUserIncludeFlag();
        String getIncludeParser();
        String getRemoveIncludePathPrefix();
        String getRemoveIncludeOutputPrefix();
        String getMacroFlags();
        String getUserMacroFlag();
        String getMacroParser();
        String[] getDevelopmentModeFlags();
        String[] getWarningLevelFlags();
        String[] getArchitectureFlags();
        String getStripFlag();
        String[] getMultithreadingFlags();
        String[] getStandardFlags();
        String[] getLanguageExtensionFlags();
        String[] getLibraryFlags();
        String getDependencyGenerationFlags();
        String getPrecompiledHeaderFlags();
        String getPrecompiledHeaderSuffix();
        boolean getPrecompiledHeaderSuffixAppend();
    }

    public interface MakeDescriptor extends ToolDescriptor {
        String getDependencySupportCode();
    }

    public interface DebuggerDescriptor extends ToolDescriptor {
    }

    public interface LinkerDescriptor {
        String getLibraryPrefix();
        String getLibrarySearchFlag();
        String getDynamicLibrarySearchFlag();
        String getLibraryFlag();
        String getPICFlag();
        String getStaticLibraryFlag();
        String getDynamicLibraryFlag();
        String getDynamicLibraryBasicFlag();
    }

    public interface ScannerDescriptor {
        List<ScannerPattern> getPatterns();
        String getChangeDirectoryPattern();
        String getEnterDirectoryPattern();
        String getLeaveDirectoryPattern();
        String getStackHeaderPattern();
        String getStackNextPattern();
    }

    public interface ScannerPattern {
        String getPattern();
        String getSeverity();
        String getLanguage();
    }
   
    private static final class CompilerVendor {
        private final String toolChainFileName;
        private String toolChainName;
        private String toolChainDisplay;
        private Map<String, String> default_locations;
        private String family;
        private String platforms;
        private String release_file;
        private String release_pattern;
        private String driveLetterPrefix;
        private String baseFolderKey;
        private String baseFolderPattern;
        private String baseFolderSuffix;
        private String baseFolderPathPattern;
        private String commandFolderKey;
        private String commandFolderPattern;
        private String commandFolderSuffix;
        private String commandFolderPathPattern;
        private Compiler c = new Compiler();
        private Compiler cpp = new Compiler();
        private Compiler fortran = new Compiler();
        private Scanner scanner = new Scanner();
        private Linker linker = new Linker();
        private Make make = new Make();
        private Debugger debugger = new Debugger();

        private CompilerVendor(String fileName){
            toolChainFileName = fileName;
        }
        
        public boolean isValid(){
            return toolChainName != null && toolChainName.length() > 0 &&
                   (c.isValid() || cpp.isValid() || fortran.isValid()); 
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("Toolchain ["+toolChainName+"/"+family+"] "+toolChainDisplay+"\n"); // NOI18N
            buf.append("\tPlatforms ["+platforms+"]\n"); // NOI18N
            buf.append("\tDrive Letter Prefix ["+driveLetterPrefix+"]\n"); // NOI18N
            buf.append("\tBase Folder Key ["+baseFolderKey+"] Pattern ["+baseFolderPattern+ // NOI18N
                       "] Suffix ["+baseFolderSuffix+"] Path Pattern["+baseFolderPattern+"] \n"); // NOI18N
            buf.append("\tCommand Folder Key ["+commandFolderKey+"] Pattern ["+commandFolderPattern+ // NOI18N
                       "] Suffix ["+commandFolderSuffix+"] Path Pattern["+commandFolderPattern+"] \n"); // NOI18N
            buf.append("C compiler ["+c.name+"] Recognize path ["+c.pathPattern+ // NOI18N
                       "] Version ["+c.versionFlags+";"+c.versionPattern+"]\n"); // NOI18N
            buf.append("\tInclude flags ["+c.includeFlags+"] parser ["+c.includeOutputParser+ // NOI18N
                       "] remove from path["+c.removeIncludePathPrefix+"] remove from output ["+c.removeIncludeOutputPrefix+"]\n"); // NOI18N
            buf.append("\tMacros flags ["+c.macrosFlags+"] parser ["+c.macrosOutputParser+"]\n"); // NOI18N
            buf.append("\tDevelopment mode "+c.developmentMode+"\n"); // NOI18N
            buf.append("\tWarning Level "+c.warningLevel+"\n"); // NOI18N
            buf.append("\tArchitecture "+c.architecture+"\n"); // NOI18N
            buf.append("\tStrip ["+c.strip+"]\n"); // NOI18N
            if (c.multithreading.isValid()) buf.append("\tMultithreading ["+c.multithreading+"]\n"); // NOI18N
            if (c.standard.isValid()) buf.append("\tStandard ["+c.standard+"]\n"); // NOI18N
            if (c.languageExtension.isValid()) buf.append("\tLanguage ["+c.languageExtension+"]\n"); // NOI18N
            if (c.library.isValid()) buf.append("\tLibrary ["+c.library+"]\n"); // NOI18N
            buf.append("C++ compiler ["+cpp.name+"] Recognize path ["+cpp.pathPattern+ // NOI18N
                       "] Version ["+cpp.versionFlags+";"+cpp.versionPattern+"]\n"); // NOI18N
            buf.append("\tInclude flags ["+cpp.includeFlags+"] parser ["+cpp.includeOutputParser+ // NOI18N
                       "] remove from path["+cpp.removeIncludePathPrefix+"] remove from output ["+cpp.removeIncludeOutputPrefix+"]\n"); // NOI18N
            buf.append("\tMacros flags ["+cpp.macrosFlags+"] parser ["+cpp.macrosOutputParser+"]\n"); // NOI18N
            buf.append("\tDevelopment mode "+cpp.developmentMode+"\n"); // NOI18N
            buf.append("\tWarning Level "+cpp.warningLevel+"\n"); // NOI18N
            buf.append("\tArchitecture "+cpp.architecture+"\n"); // NOI18N
            buf.append("\tStrip ["+cpp.strip+"]\n"); // NOI18N
            buf.append("\tDependency generation flags ["+cpp.dependencyGenerationFlags+"]\n"); // NOI18N
            if (cpp.multithreading.isValid()) buf.append("\tMultithreading "+cpp.multithreading+"\n"); // NOI18N
            if (cpp.standard.isValid()) buf.append("\tStandard "+cpp.standard+"\n"); // NOI18N
            if (cpp.languageExtension.isValid()) buf.append("\tLanguage "+cpp.languageExtension+"\n"); // NOI18N
            if (cpp.library.isValid()) buf.append("\tLibrary "+cpp.library+"\n"); // NOI18N
            if (fortran.isValid()) {
            buf.append("Fortran compiler ["+fortran.name+"] Recognize path ["+fortran.pathPattern+ // NOI18N
                       "] Version ["+fortran.versionFlags+";"+fortran.versionPattern+"]\n"); // NOI18N
            buf.append("\tDevelopment mode "+fortran.developmentMode+"\n"); // NOI18N
            buf.append("\tWarning Level "+fortran.warningLevel+"\n"); // NOI18N
            buf.append("\tArchitecture "+fortran.architecture+"\n"); // NOI18N
            buf.append("\tStrip ["+fortran.strip+"]\n"); // NOI18N
            }
            buf.append("Scanner\n"); // NOI18N
            buf.append("\tChange Directory ["+scanner.changeDirectoryPattern+"]\n"); // NOI18N
            buf.append("\tEnter Directory ["+scanner.enterDirectoryPattern+"]\n"); // NOI18N
            buf.append("\tLeave Directory ["+scanner.leaveDirectoryPattern+"]\n"); // NOI18N
            buf.append("\tStack Header ["+scanner.stackHeaderPattern+"]\n"); // NOI18N
            buf.append("\tStack Next ["+scanner.stackNextPattern+"]\n"); // NOI18N
            for(ErrorPattern p : scanner.patterns) {
                buf.append("\tPattern ["+p.pattern+"] Level ["+p.severity+"] Language ["+p.language+"]\n"); // NOI18N
            }
            buf.append("Linker\n"); // NOI18N
            buf.append("\tLibrary prefix ["+linker.library_prefix+"]\n"); // NOI18N
            buf.append("\tLibrary search ["+linker.librarySearchFlag+"]\n"); // NOI18N
            buf.append("\tDynamic library search ["+linker.dynamicLibrarySearchFlag+"]\n"); // NOI18N
            buf.append("\tLibrary ["+linker.libraryFlag+"]\n"); // NOI18N
            buf.append("\tPIC ["+linker.PICFlag+"]\n"); // NOI18N
            buf.append("\tStatic library ["+linker.staticLibraryFlag+"]\n"); // NOI18N
            buf.append("\tDynamic library ["+linker.dynamicLibraryFlag+"]\n"); // NOI18N
            buf.append("\tDynamic library basic ["+linker.dynamicLibraryBasicFlag+"]\n"); // NOI18N
            buf.append("Make [" + make.name + "] Version [" + make.versionFlags + "; " + make.versionPattern + "]\n");  // NOI18N
            buf.append("\tDependency support code [" + make.dependencySupportCode + "]\n"); // NOI18N
            buf.append("Debugger [" + debugger.name + "] Version [" + debugger.versionFlags + "; " + debugger.versionPattern + "]\n"); // NOI18N
            return buf.toString();
        }
    }

    private static class Tool {
        protected String name;
        protected String versionFlags;
        protected String versionPattern;
    }

    private static final class Compiler extends Tool {
        private String pathPattern;
        private String existFolder;
        private String includeFlags;
        private String includeOutputParser;
        private String removeIncludePathPrefix;
        private String removeIncludeOutputPrefix;
        private String userIncludeFlag;
        private String macrosFlags;
        private String macrosOutputParser;
        private String userMacroFlag;
        private String dependencyGenerationFlags;
        private String precompiledHeaderFlags;
        private String precompiledHeaderSuffix;
        private boolean precompiledHeaderSuffixAppend;
        private DevelopmentMode developmentMode = new DevelopmentMode();
        private WarningLevel warningLevel = new WarningLevel();
        private Architecture architecture = new Architecture();
        private String strip;
        private MultiThreading multithreading = new MultiThreading();
        private Standard standard = new Standard();
        private LanguageExtension languageExtension = new LanguageExtension();
        private Library library = new Library();
        
        public boolean isValid(){
            return name != null && name.length() > 0;
        }
    }
    
    private static final class Scanner {
        private List<ErrorPattern> patterns = new ArrayList<ErrorPattern>();
        private String changeDirectoryPattern;
        private String enterDirectoryPattern;
        private String leaveDirectoryPattern;
        private String stackHeaderPattern;
        private String stackNextPattern;
        
    }
    
    private static final class ErrorPattern {
        private String pattern;
        private String severity;
        private String language;
    }
            
    private static final class Linker {
        private String library_prefix;
        private String librarySearchFlag;
        private String dynamicLibrarySearchFlag;
        private String libraryFlag;
        private String PICFlag;
        private String staticLibraryFlag;
        private String dynamicLibraryFlag;
        private String dynamicLibraryBasicFlag;
    }
    
    private static final class Make extends Tool {
        private String dependencySupportCode;
    }

    private static final class Debugger extends Tool {
    }

    private static final class DevelopmentMode {
        private String fast_build;
        private String debug;
        private String performance_debug;
        private String test_coverage;
        private String diagnosable_release;
        private String release;
        private String performance_release;
        private int default_selection = 0;

        @Override
        public String toString() {
            return "["+fast_build+";"+debug+";"+performance_debug+";"+test_coverage+";"+ // NOI18N
                   diagnosable_release+";"+release+";"+performance_release+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return fast_build != null && debug != null && performance_debug != null && test_coverage != null && 
                   diagnosable_release != null && release != null && performance_release != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{fast_build,debug,performance_debug,test_coverage,
                                    diagnosable_release,release,performance_release};
            }
            return null;
        }
    }

    private static final class WarningLevel {
        private String no_warnings;
        private String default_level;
        private String more_warnings;
        private String warning2error;
        private int default_selection = 0;
        
        @Override
        public String toString() {
            return "["+no_warnings+";"+default_level+";"+more_warnings+";"+warning2error+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return no_warnings != null && default_level != null && more_warnings != null && warning2error != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{no_warnings,default_level,more_warnings,warning2error};
            }
            return null;
        }
    }

    private static final class Architecture {
        private String default_architecture;
        private String bits_32;
        private String bits_64;
        private int default_selection = 0;

        @Override
        public String toString() {
            return "["+default_architecture+";"+bits_32+";"+bits_64+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return default_architecture != null && bits_32 != null && bits_64 != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{default_architecture,bits_32,bits_64};
            }
            return null;
        }
    }

    private static final class MultiThreading {
        private String none;
        private String safe;
        private String automatic;
        private String open_mp;
        private int default_selection = 0;

        @Override
        public String toString() {
            return "["+none+";"+safe+";"+automatic+";"+open_mp+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return none != null && safe != null && automatic != null && open_mp != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{none,safe,automatic,open_mp};
            }
            return null;
        }
    }
    
    private static final class Standard {
        private String old;
        private String legacy;
        private String default_standard;
        private String modern;
        private int default_selection = 0;

        @Override
        public String toString() {
            return "["+old+";"+legacy+";"+default_standard+";"+modern+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return old != null && legacy != null && default_standard != null && modern != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{old,legacy,default_standard,modern};
            }
            return null;
        }
    }
    
    private static final class LanguageExtension {
        private String none;
        private String default_extension;
        private String all;
        private int default_selection = 0;

        @Override
        public String toString() {
            return "["+none+";"+default_extension+";"+all+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return none != null && default_extension != null && all != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{none,default_extension,all};
            }
            return null;
        }
    }

    private static final class Library {
        private String none;
        private String runtime;
        private String classic;
        private String binary_standard;
        private String conforming_standard;
        private int default_selection = 0;

        @Override
        public String toString() {
            return "["+none+";"+runtime+";"+classic+";"+binary_standard+";"+conforming_standard+"] default "+default_selection; // NOI18N
        }
        
        public boolean isValid(){
            return none != null && runtime != null && classic != null && binary_standard != null && conforming_standard != null; 
        }
        
        public String[] values(){
            if (isValid()) {
                return new String[]{none,runtime,classic,binary_standard,conforming_standard};
            }
            return null;
        }
    }
    
    private static final class SAXHandler extends DefaultHandler {
        private String path;
        private CompilerVendor v;
        private boolean isScanerOverrided = false;
        private int version = 1;

        private SAXHandler(CompilerVendor v){
            this.v = v;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (path != null) {
                if (path.equals(qName)) {
                    path = null;
                } else {
                    path = path.substring(0,path.length()- qName.length()-1);
                }
            }
        }

        @Override
        public void startElement(String uri, String lname, String name, org.xml.sax.Attributes attributes) throws SAXException {
            super.startElement(uri, lname, name, attributes);
            if (path == null) {
                path = name;
            } else {
                path += "."+name; // NOI18N
            }
            if (path.equals("toolchaindefinition")) { // NOI18N
                String xmlns = attributes.getValue("xmlns"); // NOI18N
                if (xmlns != null) {
                    int lastSlash = xmlns.lastIndexOf('/'); // NOI18N
                    if (lastSlash >= 0 && (lastSlash + 1 < xmlns.length())) {
                        String versionStr = xmlns.substring(lastSlash + 1);
                        if (versionStr.length() > 0) {
                            try {
                                version = Integer.parseInt(versionStr);
                            } catch (NumberFormatException ex) {
                                // skip
                                if (TRACE) System.out.println("Incorrect version information:" + xmlns); // NOI18N
                            }
                        }
                    } else {
                        if (TRACE) System.out.println("Incorrect version information:" + xmlns); // NOI18N
                    }
                }
            } else if (path.endsWith(".toolchain")) { // NOI18N
                v.toolChainName = attributes.getValue("name"); // NOI18N
                v.toolChainDisplay = attributes.getValue("display"); // NOI18N
                v.family = attributes.getValue("family"); // NOI18N
                return;
            } else if (path.endsWith(".platforms")) { // NOI18N
                v.platforms = attributes.getValue("stringvalue"); // NOI18N
                v.release_file = attributes.getValue("release_file"); // NOI18N
                v.release_pattern = attributes.getValue("release_pattern"); // NOI18N
                return;
            } else if (path.endsWith(".drive_letter_prefix")) { // NOI18N
                v.driveLetterPrefix = attributes.getValue("stringvalue"); // NOI18N
                return;
            } else if (path.endsWith(".base_folder")) { // NOI18N
                v.baseFolderKey = attributes.getValue("regestry"); // NOI18N
                v.baseFolderPattern = attributes.getValue("pattern"); // NOI18N
                v.baseFolderSuffix = attributes.getValue("suffix"); // NOI18N
                v.baseFolderPathPattern = attributes.getValue("path_patern"); // NOI18N
                return;
            } else if (path.endsWith(".command_folder")) { // NOI18N
                v.commandFolderKey = attributes.getValue("regestry"); // NOI18N
                v.commandFolderPattern = attributes.getValue("pattern"); // NOI18N
                v.commandFolderSuffix = attributes.getValue("suffix"); // NOI18N
                v.commandFolderPathPattern = attributes.getValue("path_patern"); // NOI18N
                return;
            } else if (path.indexOf(".default_locations.") > 0) { // NOI18N
                if (path.endsWith(".platform")) { // NOI18N
                    String os_attr = attributes.getValue("os"); // NOI18N
                    String dir_attr = attributes.getValue("directory"); // NOI18N
                    if (os_attr != null && dir_attr != null) {
                        if (v.default_locations == null) {
                            v.default_locations = new HashMap<String, String>();
                        }
                        v.default_locations.put(os_attr, dir_attr);
                    }
                }
                return;
            }
            if (path.indexOf(".linker.")>0) { // NOI18N
                Linker l = v.linker;
                if (path.endsWith(".library_prefix")) { // NOI18N
                    l.library_prefix = attributes.getValue("stringvalue"); // NOI18N
                    return;
                } else if (path.endsWith(".library_search")) { // NOI18N
                    l.librarySearchFlag = attributes.getValue("flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library_search")) { // NOI18N
                    l.dynamicLibrarySearchFlag = attributes.getValue("flags"); // NOI18N
                    return;
                } else if (path.endsWith(".library")) { // NOI18N
                    l.libraryFlag = attributes.getValue("flags"); // NOI18N
                    return;
                } else if (path.endsWith(".PIC")) { // NOI18N
                    l.PICFlag = attributes.getValue("flags"); // NOI18N
                    return;
                } else if (path.endsWith(".static_library")) { // NOI18N
                    l.staticLibraryFlag = attributes.getValue("flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library")) { // NOI18N
                    l.dynamicLibraryFlag = attributes.getValue("flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library_basic")) { // NOI18N
                    l.dynamicLibraryBasicFlag = attributes.getValue("flags"); // NOI18N
                    return;
                }
                return;
            }
            if (path.indexOf(".make.") > 0) { // NOI18N
                Make m = v.make;
                if (path.endsWith(".tool")) { // NOI18N
                    m.name = attributes.getValue("name"); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    m.versionFlags = attributes.getValue("flags"); // NOI18N
                    m.versionPattern = attributes.getValue("pattern"); // NOI18N
                } else if (path.endsWith(".dependency_support")) { // NOI18N
                    m.dependencySupportCode = attributes.getValue("code").replace("\\n", "\n"); // NOI18N
                }
            }
            if (path.indexOf(".debugger.") > 0) { // NOI18N
                Debugger d = v.debugger;
                if (path.endsWith(".tool")) { // NOI18N
                    d.name = attributes.getValue("name"); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    d.versionFlags = attributes.getValue("flags"); // NOI18N
                    d.versionPattern = attributes.getValue("pattern"); // NOI18N
                }
            }
            if (path.indexOf(".scanner.")>0) { // NOI18N
                if (!isScanerOverrided){
                    v.scanner = new Scanner();
                    isScanerOverrided = true;
                }
                Scanner s = v.scanner;
                if (path.endsWith(".error")) { // NOI18N
                    ErrorPattern e = new ErrorPattern();
                    s.patterns.add(e);
                    e.severity = "error"; // NOI18N
                    e.pattern = attributes.getValue("pattern"); // NOI18N
                    e.language = attributes.getValue("language"); // NOI18N
                } else if (path.endsWith(".warning")) { // NOI18N
                    ErrorPattern e = new ErrorPattern();
                    s.patterns.add(e);
                    e.severity = "warning"; // NOI18N
                    e.pattern = attributes.getValue("pattern"); // NOI18N
                    e.language = attributes.getValue("language"); // NOI18N
                } else if (path.endsWith(".change_directory")) { // NOI18N
                    s.changeDirectoryPattern = attributes.getValue("pattern"); // NOI18N
                } else if (path.endsWith(".enter_directory")) { // NOI18N
                    s.enterDirectoryPattern = attributes.getValue("pattern"); // NOI18N
                } else if (path.endsWith(".leave_directory")) { // NOI18N
                    s.leaveDirectoryPattern = attributes.getValue("pattern"); // NOI18N
                } else if (path.endsWith(".stack_header")) { // NOI18N
                    s.stackHeaderPattern = attributes.getValue("pattern"); // NOI18N
                } else if (path.endsWith(".stack_next")) { // NOI18N
                    s.stackNextPattern = attributes.getValue("pattern"); // NOI18N
                }
            }
            Compiler c;
            if (path.indexOf(".c.")>0) { // NOI18N
                c = v.c;
            } else if (path.indexOf(".cpp.")>0) { // NOI18N
                c = v.cpp;
            } else if (path.indexOf(".fortran.")>0) { // NOI18N
                c = v.fortran;
            } else {
                return;
            }
            if (path.endsWith(".compiler")) { // NOI18N
                c.name = attributes.getValue("name"); // NOI18N
                return;
            } else if (path.endsWith(".recognizer")) { // NOI18N
                c.pathPattern = attributes.getValue("pattern"); // NOI18N
                c.existFolder = attributes.getValue("or_exist_folder"); // NOI18N
                return;
            } else if (path.endsWith(".version")) { // NOI18N
                c.versionPattern = attributes.getValue("pattern"); // NOI18N
                c.versionFlags = attributes.getValue("flags"); // NOI18N
                return;
            } 
            String flags = attributes.getValue("flags"); // NOI18N
            if (flags == null) {
                return;
            }
            boolean isDefault = "true".equals(attributes.getValue("default")); // NOI18N
            if (path.endsWith(".system_include_paths")) { // NOI18N
                c.includeFlags = flags;
                c.includeOutputParser = attributes.getValue("parser"); // NOI18N
                c.removeIncludePathPrefix = attributes.getValue("remove_in_path"); // NOI18N
                c.removeIncludeOutputPrefix = attributes.getValue("remove_in_output"); // NOI18N
            } else if (path.endsWith(".user_include")) { // NOI18N
                c.userIncludeFlag = flags;
            } else if (path.endsWith(".system_macros")) { // NOI18N
                c.macrosFlags = flags;
                c.macrosOutputParser = attributes.getValue("parser"); // NOI18N
            } else if (path.endsWith(".user_macro")) { // NOI18N
                c.userMacroFlag = flags;
            } else if (path.indexOf(".development_mode.")>0) { // NOI18N
                DevelopmentMode d = c.developmentMode;
                if (path.endsWith(".fast_build")) { // NOI18N
                    d.fast_build = flags;
                    if (isDefault) d.default_selection = 0;
                } else if (path.endsWith(".debug")) { // NOI18N
                    d.debug = flags;
                    if (isDefault) d.default_selection = 1;
                } else if (path.endsWith(".performance_debug")) { // NOI18N
                    d.performance_debug = flags;
                    if (isDefault) d.default_selection = 2;
                } else if (path.endsWith(".test_coverage")) { // NOI18N
                    d.test_coverage = flags;
                    if (isDefault) d.default_selection = 3;
                } else if (path.endsWith(".diagnosable_release")) { // NOI18N
                    d.diagnosable_release = flags;
                    if (isDefault) d.default_selection = 4;
                } else if (path.endsWith(".release")) { // NOI18N
                    d.release = flags;
                    if (isDefault) d.default_selection = 5;
                } else if (path.endsWith(".performance_release")) { // NOI18N
                    d.performance_release = flags;
                    if (isDefault) d.default_selection = 6;
                }
            } else if (path.indexOf(".warning_level.")>0) { // NOI18N
                WarningLevel w = c.warningLevel;
                if (path.endsWith(".no_warnings")) { // NOI18N
                    w.no_warnings = flags;
                    if (isDefault) w.default_selection = 0;
                } else if (path.endsWith(".default")) { // NOI18N
                    w.default_level = flags;
                    if (isDefault) w.default_selection = 1;
                } else if (path.endsWith(".more_warnings")) { // NOI18N
                    w.more_warnings = flags;
                    if (isDefault) w.default_selection = 2;
                } else if (path.endsWith(".warning2error")) { // NOI18N
                    w.warning2error = flags;
                    if (isDefault) w.default_selection = 3;
                }
            } else if (path.indexOf(".architecture.")>0) { // NOI18N
                Architecture a = c.architecture;
                if (path.endsWith(".default")) { // NOI18N
                    a.default_architecture = flags;
                    if (isDefault) a.default_selection = 0;
                } else if (path.endsWith(".bits_32")) { // NOI18N
                    a.bits_32 = flags;
                    if (isDefault) a.default_selection = 1;
                } else if (path.endsWith(".bits_64")) { // NOI18N
                    a.bits_64 = flags;
                    if (isDefault) a.default_selection = 2;
                }
            } else if (path.endsWith(".strip")) { // NOI18N
                c.strip = flags;
            } else if (path.endsWith(".dependency_generation")) { // NOI18N
                c.dependencyGenerationFlags = flags;
            } else if (path.endsWith(".precompiled_header")) { // NOI18N
                c.precompiledHeaderFlags = flags;
                c.precompiledHeaderSuffix = attributes.getValue("suffix"); // NOI18N
                c.precompiledHeaderSuffixAppend = Boolean.valueOf(attributes.getValue("append")); // NOI18N
            } else if (path.indexOf(".multithreading.")>0) { // NOI18N
                MultiThreading m = c.multithreading;
                if (path.endsWith(".none")) { // NOI18N
                    m.none = flags;
                    if (isDefault) m.default_selection = 0;
                } else if (path.endsWith(".safe")) { // NOI18N
                    m.safe = flags;
                    if (isDefault) m.default_selection = 1;
                } else if (path.endsWith(".automatic")) { // NOI18N
                    m.automatic = flags;
                    if (isDefault) m.default_selection = 2;
                } else if (path.endsWith(".open_mp")) { // NOI18N
                    m.open_mp = flags;
                    if (isDefault) m.default_selection = 3;
                }
            } else if (path.indexOf(".standard.")>0) { // NOI18N
                Standard s = c.standard;
                if (path.endsWith(".old")) { // NOI18N
                    s.old = flags;
                    if (isDefault) s.default_selection = 0;
                } else if (path.endsWith(".legacy")) { // NOI18N
                    s.legacy = flags;
                    if (isDefault) s.default_selection = 1;
                } else if (path.endsWith(".default")) { // NOI18N
                    s.default_standard = flags;
                    if (isDefault) s.default_selection = 2;
                } else if (path.endsWith(".modern")) { // NOI18N
                    s.modern = flags;
                    if (isDefault) s.default_selection = 3;
                }
            } else if (path.indexOf(".language_extension.")>0) { // NOI18N
                LanguageExtension e = c.languageExtension;
                if (path.endsWith(".none")) { // NOI18N
                    e.none = flags;
                    if (isDefault) e.default_selection = 0;
                } else if (path.endsWith(".default")) { // NOI18N
                    e.default_extension = flags;
                    if (isDefault) e.default_selection = 1;
                } else if (path.endsWith(".all")) { // NOI18N
                    e.all = flags;
                    if (isDefault) e.default_selection = 2;
                }
            } else if (path.indexOf(".library.")>0) { // NOI18N
                Library l = c.library;
                if (path.endsWith(".none")) { // NOI18N
                    l.none = flags;
                    if (isDefault) l.default_selection = 0;
                } else if (path.endsWith(".runtime")) { // NOI18N
                    l.runtime = flags;
                    if (isDefault) l.default_selection = 1;
                } else if (path.endsWith(".classic")) { // NOI18N
                    l.classic = flags;
                    if (isDefault) l.default_selection = 2;
                } else if (path.endsWith(".binary_standard")) { // NOI18N
                    l.binary_standard = flags;
                    if (isDefault) l.default_selection = 3;
                } else if (path.endsWith(".conforming_standard")) { // NOI18N
                    l.conforming_standard = flags;
                    if (isDefault) l.default_selection = 4;
                }
            }
        }
    }

    private static final class ToolchainDescriptorImpl implements ToolchainDescriptor {
        private CompilerVendor v;
        private CompilerDescriptor c;
        private CompilerDescriptor cpp;
        private CompilerDescriptor fortran;
        private LinkerDescriptor linker;
        private ScannerDescriptor scanner;
        private MakeDescriptor make;
        private DebuggerDescriptor debugger;
        private ToolchainDescriptorImpl(CompilerVendor v){
            this.v = v;
        }
        public String getFileName() { return v.toolChainFileName; }
        public String getName() { return v.toolChainName; }
        public String getDisplayName() { return v.toolChainDisplay; }
        public String getReleaseFile() { return v.release_file; }
        public String getReleasePattern() { return v.release_pattern; }
        public String[] getFamily() {
            if (v.family != null && v.family.length() > 0) {
                return v.family.split(","); // NOI18N
            }
            return new String[]{};
        }
        public String[] getPlatforms() {
            if (v.platforms != null && v.platforms.length() > 0 ) {
                return v.platforms.split(","); // NOI18N
            }
            return new String[]{};
        }
        public String getDriveLetterPrefix() { return v.driveLetterPrefix; }
        public CompilerDescriptor getC() {
            if (c == null && v.c.isValid()){
                c = new CompilerDescriptorImpl(v.c);
            }
            return c;
        }
        public String getBaseFolderKey() { return v.baseFolderKey; }
        public String getBaseFolderPattern() { return v.baseFolderPattern; }
        public String getBaseFolderSuffix() { return v.baseFolderSuffix; }
        public String getBaseFolderPathPattern() { return v.baseFolderPathPattern; }
        public String getCommandFolderKey() { return v.commandFolderKey; }
        public String getCommandFolderPattern() { return v.commandFolderPattern; }
        public String getCommandFolderSuffix() { return v.commandFolderSuffix; }
        public String getCommandFolderPathPattern() { return v.commandFolderPathPattern; }
        public CompilerDescriptor getCpp() {
            if (cpp == null && v.cpp.isValid()){
                cpp = new CompilerDescriptorImpl(v.cpp);
            }
            return cpp;
        }
        public CompilerDescriptor getFortran() {
            if (fortran == null && v.fortran.isValid()){
                fortran = new CompilerDescriptorImpl(v.fortran);
            }
            return fortran;
        }
        public ScannerDescriptor getScanner() {
            if (scanner == null) {
                scanner = new ScannerDescriptorImpl(v.scanner);
            }
            return scanner;
        }
        public LinkerDescriptor getLinker() {
            if (linker == null) {
                linker = new LinkerDescriptorImpl(v.linker);
            }
            return linker;
        }
        public MakeDescriptor getMake() {
            if (make == null) {
                make = new MakeDescriptorImpl(v.make);
            }
            return make;
        }
        public Map<String, String> getDefaultLocations() {
            return v.default_locations;
        }
        public DebuggerDescriptor getDebugger() {
            if (debugger == null) {
                debugger = new DebuggerDescriptorImpl(v.debugger);
            }
            return debugger;
        }
        @Override
        public String toString() {
            return v.toolChainName+"/"+v.family+"/"+v.platforms; // NOI18N
        }
    }

    private static class ToolDescriptorImpl<T extends Tool> implements ToolDescriptor {
        protected T tool;
        public ToolDescriptorImpl(T tool) {
            this.tool = tool;
        }
        public String[] getNames() {
            if (tool.name != null && tool.name.length() > 0) {
                return tool.name.split(","); // NOI18N
            }
            return new String[]{};
        }
        public String getVersionFlags() {
            return tool.versionFlags;
        }
        public String getVersionPattern() {
            return tool.versionPattern;
        }
    }

    private static final class CompilerDescriptorImpl
            extends ToolDescriptorImpl<Compiler> implements CompilerDescriptor {
        private CompilerDescriptorImpl(Compiler compiler) { super(compiler); }
        public String getPathPattern() { return tool.pathPattern; }
        public String getExistFolder() {return tool.existFolder; }
        public String getIncludeFlags() { return tool.includeFlags; }
        public String getIncludeParser() { return tool.includeOutputParser; }
        public String getRemoveIncludePathPrefix() { return tool.removeIncludePathPrefix; }
        public String getRemoveIncludeOutputPrefix() { return tool.removeIncludeOutputPrefix; }
        public String getUserIncludeFlag() { return tool.userIncludeFlag; }
        public String getMacroFlags() { return tool.macrosFlags; }
        public String getMacroParser() { return tool.macrosOutputParser; }
        public String getUserMacroFlag() { return tool.userMacroFlag; }
        public String[] getDevelopmentModeFlags() { return tool.developmentMode.values(); }
        public String[] getWarningLevelFlags() { return tool.warningLevel.values(); }
        public String[] getArchitectureFlags() { return tool.architecture.values(); }
        public String getStripFlag() { return tool.strip; }
        public String[] getMultithreadingFlags() { return tool.multithreading.values(); }
        public String[] getStandardFlags() { return tool.standard.values(); }
        public String[] getLanguageExtensionFlags() { return tool.languageExtension.values(); }
        public String[] getLibraryFlags() { return tool.library.values(); }
        public String getDependencyGenerationFlags() { return tool.dependencyGenerationFlags; }
        public String getPrecompiledHeaderFlags() { return tool.precompiledHeaderFlags; }
        public String getPrecompiledHeaderSuffix() { return tool.precompiledHeaderSuffix; }
        public boolean getPrecompiledHeaderSuffixAppend() { return tool.precompiledHeaderSuffixAppend; }
    }

    private static final class ScannerDescriptorImpl  implements ScannerDescriptor {
        private Scanner s;
        private List<ScannerPattern> patterns;
        private ScannerDescriptorImpl(Scanner s){
            this.s = s;
        }
        public List<ScannerPattern> getPatterns() {
            if (patterns == null) {
                patterns = new ArrayList<ScannerPattern>();
                for(ErrorPattern p : s.patterns){
                    patterns.add(new ScannerPatternImpl(p));
                }
            }
            return patterns;
        }
        public String getChangeDirectoryPattern() { return s.changeDirectoryPattern; }
        public String getEnterDirectoryPattern() { return s.enterDirectoryPattern; }
        public String getLeaveDirectoryPattern() { return s.leaveDirectoryPattern; }
        public String getStackHeaderPattern() { return s.stackHeaderPattern; }
        public String getStackNextPattern() { return s.stackNextPattern; }
    }
    
    private static final class ScannerPatternImpl implements ScannerPattern {
        private ErrorPattern e;
        private ScannerPatternImpl(ErrorPattern e){
            this.e = e;
        }
        public String getPattern() { return e.pattern; }
        public String getSeverity() { return e.severity; }
        public String getLanguage() { return e.language; }
    }
    
    private static final class LinkerDescriptorImpl implements LinkerDescriptor {
        private Linker l;
        private LinkerDescriptorImpl(Linker l){
            this.l = l;
        }
        public String getLibrarySearchFlag() { return l.librarySearchFlag; }
        public String getDynamicLibrarySearchFlag() { return l.dynamicLibrarySearchFlag; }
        public String getLibraryFlag() { return l.libraryFlag; }
        public String getLibraryPrefix() { return l.library_prefix; }
        public String getPICFlag() { return l.PICFlag; }
        public String getStaticLibraryFlag() { return l.staticLibraryFlag; }
        public String getDynamicLibraryFlag() { return l.dynamicLibraryFlag; }
        public String getDynamicLibraryBasicFlag() { return l.dynamicLibraryBasicFlag; }
    }

    private static final class MakeDescriptorImpl
            extends ToolDescriptorImpl<Make> implements MakeDescriptor {
        private MakeDescriptorImpl(Make make) {
            super(make);
        }
        public String getDependencySupportCode() {
            return tool.dependencySupportCode;
        }
    }

    private static final class DebuggerDescriptorImpl
            extends ToolDescriptorImpl<Debugger> implements DebuggerDescriptor {
        public DebuggerDescriptorImpl(Debugger debugger) {
            super(debugger);
        }
    }

}
