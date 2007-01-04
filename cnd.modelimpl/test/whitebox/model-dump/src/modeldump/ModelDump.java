/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package modeldump;

import java.io.IOException;
import modelutils.FileCodeModel;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import modelutils.Config;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTPreprocStateImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.utils.APTMacroUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
//import org.openide.util.NotImplementedException;

/**
 *
 * @author ak119685
 */
public class ModelDump {
    PrintStream log = null;
    ModelImpl model = null;
    FileCodeModelReader modelReader = new FileCodeModelReader();
    boolean modelStarted = false;
    ProjectBase project = null;
    
    static ArrayList<String> sys_c_includes = new ArrayList<String>();
    static ArrayList<String> sys_c_defines = new ArrayList<String>();
    static ArrayList<String> sys_cpp_includes = new ArrayList<String>();
    static ArrayList<String> sys_cpp_defines = new ArrayList<String>();
    
    static {
        getSystemPredefines("c", sys_c_includes, sys_c_defines);
        getSystemPredefines("c++", sys_cpp_includes, sys_cpp_defines);
    }
    
    public ModelDump(PrintStream log) {
        this.log = log;
        model = new ModelImpl();
    }
    
    // Get predefined includes and defines...
    
    private static void getSystemPredefines(String lang, ArrayList<String> includes, ArrayList<String> defines) {
        Process gcc = null;
        boolean inSearchList = false;
        boolean waitingDefinitions = true;
        
        try {
            gcc = Runtime.getRuntime().exec("gcc -v -E -x " + lang + " /dev/null");
            
            InputStream in = gcc.getErrorStream();
            StringBuffer str = new StringBuffer();
            int i;
            char c;
            
            while((i = in.read()) != -1) {
                c = (char)i;
                
                if (c == '-' && (char)in.read() == 'D') {
                    do {
                        str = new StringBuffer();
                        while ((c = (char)in.read()) != ' ') {
                            str.append(c);
                        }
                        defines.add(str.toString());
                        
                    } while ((char)in.read() == '-' && (char)in.read() == 'D');
                } else {
                    if (c == '\n') {
                        String string = str.toString();
                        if (string.matches("#include <.*")) {
                            inSearchList = true;
                        } else if (string.matches("End of search list.*")) {
                            inSearchList = false;
                        } else if (inSearchList) {
                            includes.add(string.trim());
                        }
                        str = new StringBuffer();
                    } else {
                        str.append(c);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void startModel() {
        if (!modelStarted) {
            log.println("Starting model...");
            model.startup();
            project = model.addProject("DummyProjectID", "Dummy Project");
            modelStarted = true;
        }
    }
    
    public void stopModel() {
        if (modelStarted) {
            log.println("Stopping model...");
            model.shutdown();
            modelStarted = false;
        }
    }
    
    public static void main(String[] args) {
        try {
            Config config = new Config("l:i:d:vh", args);
            
            if (config.flagSet("-h")) {
                outUsage();
                return;
            }
            
            String logFile = config.getParameterFor("-l");
            PrintStream log = (logFile == null) ? System.out : new PrintStream(logFile);
            
            String sourceFile = config.getArgument();
            List<String> includes = config.getParametersFor("-i");
            List<String> defines = config.getParametersFor("-d");
            
            if (includes == null) {
                includes = new ArrayList<String>();
            }
            
            if (defines == null) {
                defines = new ArrayList<String>();
            }
            
            ModelDump modelDump = new ModelDump(log);
            CsmFile csmFile = modelDump.process(sourceFile, includes, defines);
            FileCodeModel codeModel = modelDump.modelReader.getModelFor(csmFile);
            
            if (config.flagSet("-v")) {
                codeModel.dump();
            }
            
            modelDump.stopModel();
            
        } catch (Exception ex) {
            System.err.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
    
    public FileImpl process(String fileName, List<String> includes, List<String> defines) {
        log.println("\nGetting model data for " + fileName + " ...");
        
        if (!modelStarted) {
            startModel();
        }
        
        ArrayList<String> sysIncludes = null;
        if (fileName.endsWith(".c")) {
            sysIncludes = sys_c_includes;
            defines.addAll(sys_c_defines);
        } else {
            sysIncludes = sys_cpp_includes;
            defines.addAll(sys_cpp_defines);
        }
        
        FileImpl fileImpl = parseFile(fileName, sysIncludes, includes, defines);
        
        log.println("\t... done.\n");
        
        return fileImpl;
    }
    
    private FileImpl parseFile(String fileName, List<String> sysIncludes, List<String> quoteIncludes, List<String> defines) {
        File file = new File(fileName);
        FileImpl fileImpl = null;
        
        log.println("Processing file: " + fileName);
        log.println("System includes: " + sysIncludes);
        log.println("Quote includes: " + quoteIncludes);
        log.println("Definitions: " + defines);
        
        APTSystemStorage aptSystemStorage = new APTSystemStorage();
        APTMacroMap map = new APTFileMacroMap();
        APTMacroUtils.fillMacroMap(map, defines);
        
        List checkedSysIncludes = aptSystemStorage.getIncludes("TraceModelSysIncludes", sysIncludes);
        APTIncludeHandler aptIncludeHandler = new APTIncludeHandlerImpl(quoteIncludes, checkedSysIncludes);
        
        APTPreprocState preprocState = new APTPreprocStateImpl(map, aptIncludeHandler, true);
        fileImpl = (FileImpl) project.testAPTParseFile(file.getAbsolutePath(), preprocState);
        
        try {
            fileImpl.scheduleParsing(true);
        } catch (InterruptedException e) {
            e.printStackTrace(log);
        }
        
        for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
            CsmProject lib = (CsmProject) it.next();
            lib.waitParse();
        }
        
        project.waitParse();
        
        return fileImpl;
    }

    private static void outUsage() {
        System.err.println("ModelDump usage:");
        System.err.println("ant -f build_cli.xml -Dargs=\"args\"");
        System.err.println("Where args can be: l:i:d:vh");
    }
    
    
}
