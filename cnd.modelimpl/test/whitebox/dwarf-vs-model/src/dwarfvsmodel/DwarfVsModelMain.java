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

package dwarfvsmodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import modeldump.ModelDump;
import modelutils.Config;
import java.io.PrintStream;
import java.util.Collection;

import org.netbeans.modules.cnd.api.model.*;

/**
 * Main class for "Dwarf vs Model"
 * @author ak119685, vk155633
 */
public class DwarfVsModelMain {
    
    private ModelDump modelDump;
    private Config config;
    private File tempDir = null;
    boolean printToScreen = false;
    
    public static void main(String[] args) {
	Config config = new Config("l:c:i:d:b:t:sf", args); // NOI18N
	new DwarfVsModelMain(config).main();
    }
    
    public DwarfVsModelMain(Config config) {
	this.config = config;
	 printToScreen = config.flagSet("-s"); // NOI18N
    }
    
    public void main() {
	
        try {
	    
	    if( ! printToScreen ) {
		tempDir = new File(config.getParameterFor("-t", "/tmp/whitebox"));    // NOI18N
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
            
            String logFile = config.getParameterFor("-l"); // NOI18N
            PrintStream resultLog = (logFile == null) ? System.out : new PrintStream(logFile);
	    resultLog.println(""); // NOI18N
            
            String configFileName = config.getParameterFor("-c"); // NOI18N
            
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
		    compileFile(traceLog, file);
		    traceLog.close();
		}
		printTime("Total parsing time:", time, resultLog);
		printMemory("Total memory used by code model:", memo, resultLog);
		resultLog.println("");
	    }
	    
	    long memo = usedMemory();
	    long time = System.currentTimeMillis();
	    
            for (Iterator<FileInfo> i = filesToProcess.iterator(); i.hasNext(); ) {
		FileInfo file = i.next();

		CompilationUnit dwarfData = null;
		try {
		    Dwarf dwarfDump = new Dwarf(file.getObjFileName());
		    dwarfData = dwarfDump.getCompilationUnit(file.getSrcFileName());
		}
		catch( IOException e ) {
		    resultLog.println(e.toString());
		}

		if (dwarfData == null) {
		    resultLog.println("Cannot get DWARF data from " + file.getObjFileName() + " for " + file.getSrcFileName()); // NOI18N
		    continue;
		}
		
		PrintStream traceLog = getTraceStream(file, DMFlags.COMPILE_ALL_FIRST);		
		if( ! DMFlags.COMPILE_ALL_FIRST ) {
		    compileFile(traceLog, file);
		}
		CsmFile codeModel = file.getCsmFile();
		traceLog.println("Comparing file: " + file.getSrcFileName()); // NOI18N

		ModelComparator comparator = new ModelComparator(codeModel, dwarfData, resultLog, traceLog);
		comparator.setBidirectional(config.flagSet("-b")); // NOI18N
		comparator.setPrintToScreen(printToScreen);
		comparator.setCompareBodies(!config.flagSet("-f")); // NOI18N
		comparator.setTemp(tempDir);
		//comparator.setDumpDwarf(true); // config.flagSet("-u")); // // NOI18N
		try {
		    result.add(comparator.compare());
		}
		catch( Exception e ) {
		    System.err.println("Error when processing files " + file.getObjFileName() + " and " + file.getSrcFileName());
		    e.printStackTrace(System.err);
		    //return;
		}
            }
            
            //resultLog.println("Final statistics:"); // NOI18N
            result.dump(resultLog);
	    
	    if( DMFlags.COMPILE_ALL_FIRST ) {
		printTime("Total comparison time:", time, resultLog);
		printMemory("Total memory used by comparison:", memo, resultLog);
	    }
	    else {
		printTime("Total processing time:", time, resultLog);
		printMemory("Total memory used", memo, resultLog);
	    }
	    resultLog.println("");
	    printMemory("Final memory footprint:", -1, resultLog);
	    resultLog.println("");
            
        } catch (Exception ex) {
            System.err.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace(System.err);
        } finally {
            if (modelDump != null) {
                modelDump.stopModel();
            }
        }
    }    

    private void compileFile(final PrintStream traceLog, final FileInfo file) {
	traceLog.println("Compiling file: " + file.getSrcFileName()); // NOI18N
	// Setup includes ...
	ArrayList<String> includes = file.getQuoteIncludes();
	List<String> cl_includes = config.getParametersFor("-i"); // NOI18N
	if (cl_includes != null) {
	    includes.addAll(cl_includes);
	}
	
//	ArrayList<String> dwarfIncludes = file.convertPaths(dwarfData.getStatementList().getIncludeDirectories());
//	includes.addAll(dwarfIncludes);
	
	// Setup defines ...
	ArrayList<String> defines = file.getDefines();
	List<String> cl_defines = config.getParametersFor("-d"); // NOI18N
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
    
}
