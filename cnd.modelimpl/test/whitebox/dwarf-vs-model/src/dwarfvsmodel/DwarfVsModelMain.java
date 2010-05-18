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

package dwarfvsmodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import modeldump.ModelDump;
import java.io.PrintStream;
import java.util.Collection;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;

/**
 * Main class for "Dwarf vs Model"
 * @author ak119685, vk155633
 */
public class DwarfVsModelMain {
    
    private ModelDump modelDump;
    private File tempDir = null;
    boolean printToScreen = false;
    private File errorStatisticsFile;
    private int verbosity = 1;
    
    public static void main(String[] args) {
	try {
	    DMFlags.parse(args);
	    new DwarfVsModelMain().main();
	} catch (Config.WrongArgumentException ex) {
	    //ex.printStackTrace();
	    System.err.println(ex.getMessage());
	}
    }
    
    public DwarfVsModelMain() {
	printToScreen = DMFlags.printToScreen.getValue();
    }
    
    public void main() {
	
        try {
	    
	    if( ! printToScreen ) {
		tempDir = new File(DMFlags.tempDir.getValue());
		if( tempDir.exists() ) {
		    if( ! tempDir.isDirectory() ) {
			System.err.println("File " + tempDir.getAbsolutePath() + " exists and it isn't directory"); // NOI18N
			return;
		    }
		    else if( ! tempDir.canWrite() ) {
			System.err.println("Directory " + tempDir.getAbsolutePath() + " isn't writeble"); // NOI18N
			return;
		    }
		}
		else {
		    if( ! tempDir.mkdirs() ) {
			System.err.println("Can't create directory " + tempDir.getAbsolutePath()); // NOI18N
			return;
		    }
		}
	    }
	    
	    PrintStream globalTraceLog = printToScreen ? System.out : DMUtils.createStream(tempDir, "_all", "trace"); // NOI18N
	    
	    initErrorStatistics();
            
            String logFile = DMFlags.logFile.getValue();
            PrintStream resultLog = (logFile == null) ? System.out : new PrintStream(logFile);
	    resultLog.println(""); // NOI18N
            
            String configFileName = DMFlags.configFile.getValue();
            
            if (configFileName == null) {
                return;
            }
            
            ConfigFile configFile = new ConfigFile(configFileName, globalTraceLog);

	    
            modelDump = new ModelDump(System.out);
            
            Collection <FileInfo> filesToProcess = configFile.getFilesToProcess();
            ComparationResult result = new ComparationResult("Total", 0, 0, 0); // NOI18N

	    if( DMFlags.COMPILE_ALL_FIRST ) {
		long memo = usedMemory();
		long time = System.currentTimeMillis();
		for (Iterator<FileInfo> i = filesToProcess.iterator(); i.hasNext(); ) {
		    FileInfo file = i.next();
		    PrintStream traceLog = getTraceStream(file, false);
		    if( traceLog != System.out && verbosity > 0 ) {
			System.out.println("Compiling file: " + file.getSrcFileName()); // NOI18N
		    }
		    compileFile(traceLog, file);
		    if( traceLog != System.out ) {
			traceLog.close();
		    }
		}
		printTime("\nTotal parsing time:", time, resultLog); // NOI18N
		printMemory("Total memory used by code model:", memo, resultLog); // NOI18N
		resultLog.println("");
	    }
	    
	    long memo = usedMemory();
	    long time = System.currentTimeMillis();
	    
            for (Iterator<FileInfo> i = filesToProcess.iterator(); i.hasNext(); ) {
		FileInfo fileInfo = i.next();

		Dwarf dwarfDump = null;
		CompilationUnit dwarfData = null;
		try {
		    dwarfDump = new Dwarf(fileInfo.getObjFileName());
		    dwarfData = dwarfDump.getCompilationUnit(fileInfo.getSrcFileName());
		}
		catch( IOException e ) {
		    resultLog.println(e.toString());
		    continue;
		}

		if (dwarfData == null) {
		    resultLog.println("Cannot get DWARF data from " + fileInfo.getObjFileName() + " for " + fileInfo.getSrcFileName()); // NOI18N
		    continue;
		}
		
		PrintStream traceLog = getTraceStream(fileInfo, DMFlags.COMPILE_ALL_FIRST);		
		if( ! DMFlags.COMPILE_ALL_FIRST ) {
		    compileFile(traceLog, fileInfo);
		}
		CsmFile codeModel = fileInfo.getCsmFile();
		traceLog.println("Comparing file: " + fileInfo.getSrcFileName()); // NOI18N

		ModelComparator comparator = new ModelComparator(codeModel, dwarfData, dwarfDump, fileInfo, resultLog, traceLog);
		comparator.setBidirectional(DMFlags.bidirectional.getValue());
		comparator.setPrintToScreen(printToScreen);
		comparator.setCompareBodies(!DMFlags.flat.getValue());
		comparator.setTemp(tempDir);
		//comparator.setDumpDwarf(true); // config.flagSet("-u")); // // NOI18N
		try {
		    result.add(comparator.compare());
		}
		catch( Exception e ) {
		    System.err.println("Error when processing files " + fileInfo.getObjFileName() + " and " + fileInfo.getSrcFileName());
		    e.printStackTrace(System.err);
		    //return;
		}
            }
            
            //resultLog.println("Final statistics:"); // NOI18N
            result.dump(resultLog);
	    resultLog.printf("Total parser error count: %5d", calculateTotalErrorCount()); // NOI18N
	    
	    if( DMFlags.COMPILE_ALL_FIRST ) {
		printTime("\nTotal comparison time:", time, resultLog); // NOI18N
		printMemory("Total memory used by comparison:", memo, resultLog); // NOI18N
	    }
	    else {
		printTime("\nTotal processing time:", time, resultLog); // NOI18N
		printMemory("Total memory used", memo, resultLog); // NOI18N
	    }
	    printMemory("\nFinal memory footprint:", -1, resultLog); // NOI18N
	    resultLog.println("");
            
        } catch (Exception ex) {
            System.err.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace(System.err);
        } finally {
            if (modelDump != null) {
                modelDump.stopModel();
            }
        }
	printErrorStatistics();
    }

    private void compileFile(final PrintStream traceLog, final FileInfo file) {
	traceLog.println("Compiling file: " + file.getSrcFileName()); // NOI18N
	// Setup includes ...
	ArrayList<String> includes = file.getQuoteIncludes();
	List<String> cl_includes = DMFlags.userIncludes.getValue();
	if (cl_includes != null) {
	    includes.addAll(cl_includes);
	}
	
//	ArrayList<String> dwarfIncludes = file.convertPaths(dwarfData.getStatementList().getIncludeDirectories());
//	includes.addAll(dwarfIncludes);
	
	// Setup defines ...
	ArrayList<String> defines = file.getDefines();
	List<String> cl_defines = DMFlags.userDefines.getValue();
	if (cl_defines != null) {
	    defines.addAll(cl_defines);
	}
	
//	DwarfMacinfoTable dwarfMacrosTable = dwarfData.getMacrosTable();
//	
//	if (dwarfMacrosTable != null) {
//	    ArrayList<String> dwarfDefines = dwarfMacrosTable.getCommandLineDefines();
//	    defines.addAll(dwarfDefines);
//	}
	
	modelDump.setLog(traceLog);
	
	// Get Model to compare ...
	
	CsmFile tmpCsmFile = modelDump.process(file.getSrcFileName(), includes, defines);
	file.setCsmFile(tmpCsmFile);
    }
    
    private PrintStream getTraceStream(FileInfo file, boolean append) throws FileNotFoundException {
	return printToScreen ? System.out : DMUtils.createStream(tempDir, file.getSrcFileName(), "trace", append); // NOI18N
    }
    
    private long usedMemory() {
	System.gc();
	return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    private void printMemory(String text, long memUsed, PrintStream ps) {
	long delta =  usedMemory();
	if( memUsed >= 0 ) {
	    delta -= memUsed;
	}
	NumberFormat nf = NumberFormat.getIntegerInstance();
	nf.setGroupingUsed(true);
	nf.setMinimumIntegerDigits(6);
	ps.printf("%s %s Kb\n", text, nf.format((delta)/1024));	// NOI18N
    }
    
    private void printTime(String text, long time, PrintStream ps) {
	long delta = System.currentTimeMillis() - time;
	NumberFormat nf = NumberFormat.getIntegerInstance();
	nf.setGroupingUsed(true);
	nf.setMinimumIntegerDigits(6);
	//ps.println(text + nf.format((delta)/1000) + " Kb"); // NOI18N
	ps.printf("%s %s seconds\n", text, nf.format((delta)/1000));	// NOI18N
    }

    private void initErrorStatistics() {
	errorStatisticsFile = new File(tempDir, "_errorStat"); // NOI18N
//	if( errorStatisticsFile.exists() ) {
//	    errorStatisticsFile.delete();
//	}
	Diagnostic.setStatisticsLevel(Integer.getInteger("cnd.modelimpl.stat.level", 1).intValue()); // NOI18N
	Diagnostic.initFileStatistics(errorStatisticsFile.getAbsolutePath());
    }
    
    private void printErrorStatistics() {
	try {
	    Diagnostic.dumpUnresolvedStatistics(errorStatisticsFile.getAbsolutePath(), false);
	    Diagnostic.dumpFileStatistics(errorStatisticsFile.getAbsolutePath(), true);
	}
	catch( Exception e ) {
	    e.printStackTrace(System.err);
	}
    }
    
    private int calculateTotalErrorCount() {
	int cnt = 0;
	Set<CsmProject> processedProjects = new HashSet<CsmProject>();
	Set<CsmFile> processedFiles = new HashSet<CsmFile>();
	for( CsmProject prj : (Collection<CsmProject>) CsmModelAccessor.getModel().projects() ) {
	    cnt += calculateTotalErrorCount(prj, processedProjects, processedFiles);
	}
	return cnt;
    }
    
    private int calculateTotalErrorCount(CsmProject prj, Set<CsmProject> processedProjects, Set<CsmFile> processedFiles) {
	int cnt = 0;
	if( ! processedProjects.contains(prj) ) {
	    for( CsmProject lib : (Collection<CsmProject>) prj.getLibraries() ) {
		cnt += calculateTotalErrorCount(lib, processedProjects, processedFiles);
	    }
	    for( FileImpl file : ((ProjectBase) prj).getAllFileImpls() ) {
		cnt += file.getErrorCount();
	    }
	}
	return cnt;
    }
}
