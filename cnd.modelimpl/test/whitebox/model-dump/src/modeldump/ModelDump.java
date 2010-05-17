/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package modeldump;

import modelutils.FileCodeModel;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import modelutils.Config;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTPreprocHandlerImpl;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.GNUCCCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.GNUCCompiler;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
//import org.netbeans.modules.cnd.modelimpl.parser.apt.APTPreprocStateImpl;
//import org.netbeans.modules.cnd.modelimpl.parser.apt.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
//import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.StartEntry;
//import org.netbeans.modules.cnd.apt.utils.APTMacroUtils;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
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
        getSystemPredefines("c", sys_c_includes, sys_c_defines); // NOI18N
        getSystemPredefines("c++", sys_cpp_includes, sys_cpp_defines); // NOI18N
    }
    
    public ModelDump(PrintStream log) {
        this.log = log;
        model = (ModelImpl) CsmModelAccessor.getModel(); //new ModelImpl();
    }
    
    public void setLog(PrintStream log) {
	this.log = log;
    }
    
    // Get predefined includes and defines...

    private static void getSystemPredefines(String lang, ArrayList<String> includes, ArrayList<String> defines) {
	BasicCompiler compiler;
	if( "c".equals(lang) ) { // NOI18N
	    compiler = new GNUCCompiler(CompilerFlavor.GNU, 0, "gcc", "gcc", "");
	}
	else if( "c++".equals(lang) ) { // NOI18N
	    compiler = new GNUCCCompiler(CompilerFlavor.GNU, 1, "gcc", "gcc", "");
	}
	else {
	    return;
	}
	Platform platform = Platforms.getPlatform(Platform.getDefaultPlatform());
	for( Object o : compiler.getSystemIncludeDirectories() ) {
	    includes.add((String) o);
	}
	for( Object o : compiler.getSystemPreprocessorSymbols() ) {
	    defines.add((String) o);
	}
    }

//
// NB!!!: The function below "eats" dashes ("-" symbols)
//    
//    private static void getSystemPredefines(String lang, ArrayList<String> includes, ArrayList<String> defines) {
//        Process gcc = null;
//        boolean inSearchList = false;
//        boolean waitingDefinitions = true;
//        
//        try {
//            gcc = Runtime.getRuntime().exec("gcc -v -E -x " + lang + " /dev/null"); // NOI18N
//            
//            InputStream in = gcc.getErrorStream();
//            StringBuffer str = new StringBuffer();
//            int i;
//            char c;
//            
//            while((i = in.read()) != -1) {
//                c = (char)i;
//                
//                if (c == '-' && (char)in.read() == 'D') {
//                    do {
//                        str = new StringBuffer();
//                        while ((c = (char)in.read()) != ' ') {
//                            str.append(c);
//                        }
//                        defines.add(str.toString());
//                        
//                    } while ((char)in.read() == '-' && (char)in.read() == 'D');
//                } else {
//                    if (c == '\n') {
//                        String string = str.toString();
//                        if (string.matches("#include <.*")) { // NOI18N
//                            inSearchList = true;
//                        } else if (string.matches("End of search list.*")) { // NOI18N
//                            inSearchList = false;
//                        } else if (inSearchList) {
//                            includes.add(string.trim());
//                        }
//                        str = new StringBuffer();
//                    } else {
//                        str.append(c);
//                    }
//                }
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
    
    public void startModel(NativeProject p) {
        if (!modelStarted) {
            log.println("Starting model..."); // NOI18N
            model.startup();
            //project = model.addProject("DummyProjectID", "Dummy Project"); // NOI18N
            
            project = model.addProject(p, "Dummy Project", true); // NOI18N
            modelStarted = true;
        }
    }
    
    public void stopModel() {
        if (modelStarted) {
            log.println("Stopping model..."); // NOI18N
            model.shutdown();
            modelStarted = false;
        }
    }
    
    public static void main(String[] args) {
        try {
            Config config = new Config("l:i:d:vh", args); // NOI18N
            
            if (config.flagSet("-h")) { // NOI18N
                outUsage();
                return;
            }
            
            String logFile = config.getParameterFor("-l"); // NOI18N
            PrintStream log = (logFile == null) ? System.out : new PrintStream(logFile);
            
            String sourceFile = config.getArgument();
            List<String> includes = config.getParametersFor("-i"); // NOI18N
            List<String> defines = config.getParametersFor("-d"); // NOI18N
            
            if (includes == null) {
                includes = new ArrayList<String>();
            }
            
            if (defines == null) {
                defines = new ArrayList<String>();
            }
            
            ModelDump modelDump = new ModelDump(log);
            CsmFile csmFile = modelDump.process(sourceFile, includes, defines);
            FileCodeModel codeModel = modelDump.modelReader.getModelFor(csmFile);
            
            if (config.flagSet("-v")) { // NOI18N
                codeModel.dump();
            }
            
            modelDump.stopModel();
            
        } catch (Exception ex) {
            System.err.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
    
    public FileImpl process(String fileName, List<String> includes, List<String> defines) {
        log.println("\nGetting model data for " + fileName + " ..."); // NOI18N
        
        ArrayList<String> sysIncludes = null;
        if (fileName.endsWith(".c")) { // NOI18N
            sysIncludes = sys_c_includes;
            defines.addAll(sys_c_defines);
        } else {
            sysIncludes = sys_cpp_includes;
            defines.addAll(sys_cpp_defines);
        }

        File file = new File(fileName);
        ArrayList<File> files = new  ArrayList<File>();
        files.add(file);
        NativeProject p = NativeProjectProvider.createProject("DummyProjectID", files, sysIncludes, includes, new ArrayList<String>(), defines, true);
       
        if (!modelStarted) {
            startModel(p);
        }
        
        FileImpl fileImpl = parseFile(p, fileName, sysIncludes, includes, defines);
        
        log.println("\t... done.\n"); // NOI18N
        
        return fileImpl;
    }
    
    private FileImpl parseFile(NativeProject p, String fileName, List<String> sysIncludes, List<String> quoteIncludes, List<String> defines) {
        File file = new File(fileName);
        FileImpl fileImpl = null;
        
        log.println("Processing file: " + fileName); // NOI18N
        log.println("System includes: " + sysIncludes); // NOI18N
        log.println("Quote includes: " + quoteIncludes); // NOI18N
        log.println("Definitions: " + defines); // NOI18N
        
        APTMacroMap map = APTSystemStorage.getDefault().getMacroMap(defines);
        
        List checkedSysIncludes = APTSystemStorage.getDefault().getIncludes(sysIncludes);
        APTIncludeHandler aptIncludeHandler = new APTIncludeHandlerImpl(new StartEntry(file.getAbsolutePath(), null), quoteIncludes, checkedSysIncludes);
        
        APTPreprocHandlerImpl ph = new APTPreprocHandlerImpl(map, aptIncludeHandler, true);
        
        //APTPreprocState preprocState = new APTPreprocStateImpl(map, aptIncludeHandler, true);
        //APTPreprocState.State state = preprocState.getState();
        //fileImpl = (FileImpl) project.testAPTParseFile(file.getAbsolutePath(), preprocState);
        
        ArrayList<File> files = new  ArrayList<File>();
        files.add(file);
        //NativeProject p = NativeProjectProvider.createProject("DummyProjectID", files, null, null, null, null, true);
        fileImpl = (FileImpl) project.testAPTParseFile(p.findFileItem(file));
        
        try {
            fileImpl.scheduleParsing(true, ph.getState());
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
