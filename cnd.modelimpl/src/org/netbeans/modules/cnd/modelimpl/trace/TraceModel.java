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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.text.NumberFormat;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.modelimpl.antlr2.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.apt.impl.structure.APTBuilder;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTPreprocStateImpl;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTSystemStorage;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.modelimpl.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.LibProjectImpl;
import java.io.*;
import java.util.*;
import java.util.List;

import antlr.*;
import antlr.collections.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import org.netbeans.modules.cnd.modelimpl.antlr2.*;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPLexer;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTParserMacroExpandedStream;
import org.netbeans.modules.cnd.modelimpl.apt.structure.APT;
import org.netbeans.modules.cnd.modelimpl.apt.structure.APTFile;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.apt.utils.APTMacroUtils;
import org.netbeans.modules.cnd.modelimpl.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.folding.APTFoldingProvider;

/**
 * Tracer for model
 * @author Vladimir Kvasihn
 */
public class TraceModel {
    
    private static class TestResult {
	
	private long time;
	private long lineCount;
		
	public TestResult() {
	    this(0);
	}
	
	public TestResult(long time) {
	    this.setTime(time);
	}
	
	public TestResult(long time, long lineCount) {
	    this.setTime(time);
	    this.setLineCount(lineCount);
	}
	
	public String getLPS() {
	    if( time == 0 || lineCount <= 0 ) {
		    return "N/A";
	    }
	    else {
		return "" + (lineCount*1000/time);
	    }
	}

	public long getTime() {
	    return time;
	}
	
	public void setTime(long time) {
	    this.time = time;
	}
	
	public long getLineCount() {
	    return (lineCount < 0) ? 0 : lineCount;
	}
	
	public boolean isLineCountValid() {
	    return lineCount >= 0;
	}
	
	public void setLineCount(long lineCount) {
	    this.lineCount = lineCount;
	}
	
	public void accumulate(TestResult toAdd) {
	    time += toAdd.time;
	    if( isLineCountValid() ) {
		if( toAdd.isLineCountValid() ) {
		    lineCount += toAdd.getLineCount();
		}
		else {
//		    lineCount = -1;
		}
	    }
	}
    }
    
    
    private static final int APT_REPEAT_TEST =Integer.getInteger("apt.repeat.test", 3).intValue();

    
    public static void main(String[] args) {
        new TraceModel().test(args);
        if (TraceFlags.USE_AST_CACHE) {
            CacheManager.getInstance().close();
        }
	//System.out.println("" + org.netbeans.modules.cnd.modelimpl.apt.utils.APTIncludeUtils.getHitRate());
    }
    
    private ModelImpl model;
    private ProjectBase project;
    private Cache cache; 
    
    private CsmTracer tracer = new CsmTracer(false);
	    
    private boolean showAstWindow = false;
    private boolean dumpAst = false;
    private boolean dumpModel = false;
    private boolean dumpLib = false;
    private boolean dumpFileOnly = false;
    private boolean showTime = false;
    private boolean testLexer = false;
    private boolean recursive = false;
    //private boolean showErrorCount = false;
    private boolean writeAst = false;
    private boolean readAst = false;
    private boolean readWriteTokens = false;
    private boolean useZip = false;
    private boolean testParser = false;
    private boolean testLibProject = false;
    private boolean deep = true;
    private boolean showMemoryUsage = false;
    private boolean testUniqueName = false;
    private boolean testAPT     = false;
    private boolean testAPTLexer= false;
    private boolean testAPTPlainLexer = false;
    private boolean testAPTDriver = false;
    private boolean testAPTWalkerVisit = false;
    private boolean testAPTWalkerGetStream = false;
    private boolean testAPTWalkerGetExpandedStream = false;
    private boolean testAPTWalkerGetFilteredStream = false;
    private boolean testAPTParser = false;
    private boolean breakAfterAPT = false;
    
    private boolean stopBeforeAll = false;
    private boolean stopAfterAll = false;
    private boolean printTokens = false;
    
    private List quoteIncludePaths = new ArrayList();
    private List systemIncludePaths = new ArrayList();
    private List currentIncludePaths = null;
    
    private List macros = new ArrayList();

    private List fileList = new ArrayList();
    
    private boolean dumpStatistics = false;
    private static final int DEFAULT_TRACEMODEL_STATISTICS_LEVEL = 1;
    private String  dumpFile = null;
    private String  dumpDir  = null;
    private static final String statPostfix = ".stat";
    
    // Cache options
    private boolean enableCache = false;
    private boolean testCache = false;
    
    // Callback options
    private boolean dumpPPCallback = false;
    
    // if true, then relative include paths oin -I option are considered
    // to be based on the file that we currently compile rather then current dir
    private boolean pathsRelCurFile = false;
    
    private boolean listFilesAtEnd = false;
    private boolean testRawPerformance = false;
    private boolean printUserFileList = false;
    private boolean quiet = false;
    private boolean memBySize = false;
    
    private boolean testFolding = false;
    
    private Map/*<String, Integer>*/ cacheTimes = new HashMap();
    
    private int lap = 0;
    
    public TraceModel() {
        model =  (ModelImpl) CsmModelAccessor.getModel(); // new ModelImpl(true);
        if( model == null ) {
            model = new ModelImpl();
        }
        model.startup();
	initProject();
        currentIncludePaths = quoteIncludePaths;
    }
    
    private void initProject() {
	if( project != null ) {
	    Object platformProject = project.getPlatformProject();
	    project.dispose();
	    ((ModelImpl) CsmModelAccessor.getModel()).removeProject(platformProject);
	}
        project = model.addProject("DummyPrjId", "DummyProject");
    }
    
    private boolean processFlag(char flag, String argRest) {
        boolean argHasBeenEaten = false;
        switch( flag ) {
            case 'n':   deep = false; break;
            case 'e':	System.setErr(System.out); break;
            case 'w':	showAstWindow = true; break;
            case 'a':	dumpAst = true; break;
            case 'm':	dumpModel = true; break;
            case 'M':   showMemoryUsage = true; break;
            case 'u':   testUniqueName = true; break;
            case 'f':   dumpModel = true; dumpFileOnly = true; break;
            case 't':	showTime = true; break;
            case 'L':   testLexer = true; break;
            case 'r':   recursive = true; break;
            //case 'c':   showErrorCount = true; break;
            case 'W':   writeAst = true; break;
            case 'R':   readAst = true; break;
            case 'Z':   useZip = true; break;
            case 'T':   readWriteTokens = true; break;
            case 'P':   testParser = true; break;
            case 'C':   enableCache = true; break;
            case 'l':   testLibProject = true; break;
            // TODO: support not only "-Idir" but "-I dir" as well
            // TODO: support -iquote. Now I disabled this since the contract reads:
            // -iquote<dir> Add the directory dir to the head of the list of directories to be searched for header files 
            // ONLY for the case of #include "file"; they are NOT searched for #include <file>
            case 'I':   if (argRest.length() > 0 ) {
                            if (argRest.charAt(0)=='-')  {
                                // switch following include paths destination list
                                currentIncludePaths = (currentIncludePaths == quoteIncludePaths) 
                                                    ? systemIncludePaths : quoteIncludePaths;
                                argRest = argRest.substring(1);
                            }
                            String includePath = argRest;
                            currentIncludePaths.add(includePath);
                            argHasBeenEaten = true;
                        }
                        break;
            case 'c':   testCache = true; break;
            case 'p':   dumpPPCallback = true; break;
            case 'D':   macros.add(argRest);argHasBeenEaten = true;break;
            // "-SDir" defines dump directory for per file statistics
            case 'S':   dumpStatistics=true;
                        if (argRest.length() > 0) {
                            // dump directory for per file statistics
                            File dumpDir = new File(argRest);
                            dumpDir.mkdirs();
                            if (!dumpDir.isDirectory()) {
                                print("Parameter -S" + argRest + " does not specify valid directory");
                            } else {
                                this.dumpDir = dumpDir.getAbsolutePath();                         
                            }                            
                            argHasBeenEaten = true;
                        }
                        break;
            // "-sFileName" defines global statistics dump file            
            case 's':   dumpStatistics=true;
                        if (argRest.length() > 0) {
                            // global dumpFile
                            File dumpFile = new File(argRest);
                            if (dumpFile.exists()) {
                                dumpFile.delete();
                            }
                            try {
                                if (dumpFile.getParentFile() != null) {
                                    dumpFile.getParentFile().mkdirs();
                                    dumpFile.createNewFile();
                                    this.dumpFile = dumpFile.getAbsolutePath();     
                                    argHasBeenEaten = true;                                    
                                } else {
//                                    System.err.println("failed to create statistics file");
                                    argHasBeenEaten = false;  
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        break;            
            case 'A':   testAPT = true; 
                        testAPTWalkerVisit = true; 
                        testAPTWalkerGetStream = true;
                        testAPTWalkerGetExpandedStream = true;
                        testAPTWalkerGetFilteredStream = true;
                        testAPTLexer = true;
                        breakAfterAPT = true;  
                        testAPTDriver = true;
                        break;
            case 'b':   testAPTPlainLexer = true; testAPT = true; breakAfterAPT = true; break;
            case 'B':   testAPTLexer = true; testAPT = true; breakAfterAPT = true; break;
            case 'o':   printTokens = true; break;
            case 'v':   testAPTWalkerVisit = true; testAPT = true; breakAfterAPT = true; break;
            case 'g':   testAPTWalkerGetStream = true; testAPT = true; breakAfterAPT = true; break;
            case 'G':   testAPTWalkerGetExpandedStream = true; testAPT = true; breakAfterAPT = true; break;
            case 'F':   testAPTWalkerGetFilteredStream = true; testAPT = true; breakAfterAPT = true; break;
            case 'd':   testAPTDriver = true; testAPT = true; breakAfterAPT = true; break;
            case 'h':   testParser = true; testAPT = true; breakAfterAPT = true; break;
            case 'H':   testAPTParser = true; testAPT = true; breakAfterAPT = true; break;
            case 'O':   stopBeforeAll = true; stopAfterAll = true; break;
	    case 'q':	quiet = true; break;
            default:
        }
        return argHasBeenEaten;
    }
    
    private void processFlag(String flag) {
	if( "dumplib".equals(flag) ) {
	    dumpLib = true;
	}
	else if( "relpath".equals(flag) ) {
	    pathsRelCurFile = true;
	}
	else if( "listfiles".equals(flag) ) {
	    listFilesAtEnd = true;
	}
	else if( "raw".equals(flag) ) {
	    testRawPerformance = true;
	    //TraceFlags.DO_NOT_RENDER = true;
	}
	else if( "listfiles".equals(flag) ) {
	    printUserFileList = true;
	}
	else if( "mbs".equals(flag) ) {
	    memBySize = true;
	} else if ( "folding".equals(flag)) {
            testFolding = true;
        }
    }
    
    private void addFile(List files, File file) {
        if (file.isDirectory()) {
            String[] list = file.list();
            for (int i = 0; i < list.length; i++) {
                addFile(files, new File(file, list[i]));
            }
        } else {
            files.add(file);
        }
    }
        
    private void test(String[] args) {        
        long time = 0;
        for( int i = 0; i < args.length; i++ ) {
            if( args[i].startsWith("--") ) {
                processFlag(args[i].substring(2));
            }
            else 
            if( args[i].startsWith("-") ) {
                for( int charIdx = 1; charIdx < args[i].length(); charIdx++ ) {
                    boolean argHasBeenEaten = processFlag(args[i].charAt(charIdx), args[i].substring(charIdx+1));
                    if (argHasBeenEaten)
                        break;
                }
            }
            else {
                addFile(fileList, new File(args[i]));
            }
        }
	if( ! pathsRelCurFile ) {
	    List[] paths = { quoteIncludePaths, systemIncludePaths };
	    for (int listIdx = 0; listIdx < paths.length; listIdx++) {
		for (int pathIdx = 0; pathIdx < paths[listIdx].size(); pathIdx++) {
		    String path = (String) paths[listIdx].get(pathIdx);
		    if( ! new File(path).isAbsolute() ) {
			paths[listIdx].set(pathIdx, new File(path).getAbsolutePath());
		    }
		}
	    }
	}
        if (stopBeforeAll) {
	    waitAnyKey();
        }        
        if( writeAst || readAst ) {
            try {
                cache = new Cache(useZip);
            }
            catch( Exception e ) {
                e.printStackTrace(System.err);
                return;
            }
        }
        
        if( writeAst && readAst ) {
            print("Impossible options combination: both writing and reading AST\n");
            return;
        }
        if( useZip && ! (writeAst || readAst) ) {
            print("Impossible options combination: using ZIP format, but neither writing nor reading AST. Ignoring ZIP format.\n");
            useZip = false;
        }
        if( readWriteTokens && ! testLexer ) {
            readWriteTokens = false;
            print("Warning: \"reading/writing tokens\" options can't work w/o \"test Lexer\" option\n");
        }

        if (testCache) {
            //print("Test cache mode ON." + '\n');
            enableCache = true;
        }
        else
            //print("Setting cache to " + (enableCache ? "ON" : "OFF") + '\n');

        if (dumpStatistics) {
            if (dumpFile == null && dumpDir == null) {
                print("Turning OFF statistics as neither global file nor directory is specified");
                dumpStatistics = false;
            } else {         
                print("Dumping Statistics is ON");
                if (Diagnostic.getStatisticsLevel() == 0) {
                    // need to set the default level
                    Diagnostic.setStatisticsLevel(DEFAULT_TRACEMODEL_STATISTICS_LEVEL);
                }                
                if (dumpFile != null) {
                    print("Global Dump file is " + dumpFile);
                }
                if (dumpDir != null) {
                    print("Dump directory for per file statistics is " + dumpDir);
                }
            }
        }
        
        org.netbeans.modules.cnd.modelimpl.old.cache.CacheManager.instance().setUseCache(enableCache);
        
        if( testLibProject ) {
            testLibProject();
        }
        
	if( printUserFileList ) {
	    print("Processing files:\n" + fileList.toString() + '\n');
	}
        
        long memUsed = 0;
        if( showMemoryUsage ) {
            memUsed = usedMemory();
        }
        
	long t = System.currentTimeMillis();
	TestResult total = test();
	total.time = System.currentTimeMillis() - t;
	
	if( testRawPerformance ) {
	    print("Take one finished.");
	    print("Total parsing time " + total.time + " ms");
	    calculateAverageLPS(total, true);
	    print("Lines count " + total.lineCount);
	    print("Average LPS " + total.getLPS());
	    
	    if( showMemoryUsage ) {
		showMemoryUsage(memUsed);
	    }
	    
//	    for (int i = 0; i < 100; i++) {
//		initProject();
//		test();
//		showMemoryUsage(memUsed);
//	    }
	    
	    print("\nTesting raw performance: parsing project, take two\n");
	    initProject();
	    if (stopBeforeAll) {
		waitAnyKey();
	    }
	    t = System.currentTimeMillis();
	    total = test();
	    total.time = System.currentTimeMillis() - t;
	}
	
	/* this unnecessary since we call waitProjectParsed() for each file
	if( showTime ) {
	    print("Waiting for the rest of the parser queue to be parsed");
	}
	waitProjectParsed();
	*/
	
	if( dumpLib ) {
	    for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
		CsmProject lib = (CsmProject) it.next();
		tracer.dumpModel(lib);
	    }
	}
	
        model.shutdown();
	
        if( showTime ) {
	    
	    int maxLen = 0;
	    for (int i = 0; i<CPPParserEx.MAX_GUESS_IDX;i++) {
		if( CPPParserEx.guessingNames[i] != null ) {
		    int len = CPPParserEx.guessingNames[i].length();
		    if( len > maxLen ) {
			maxLen = len;
		    }
		}
	    }
            
            boolean printGuessStat = false;
            // check if we had the statistics
            for (int i = 0; i<CPPParserEx.MAX_GUESS_IDX;i++) {
                if (CPPParserEx.guessingCount[i] !=0) {
                    printGuessStat = true;
                    break;
                }
            }
            if( listFilesAtEnd ) {
		print("\n========== User project files ==========");
		List l = new ArrayList(project.getFileList().size());
		for (Iterator it = project.getFileList().iterator(); it.hasNext();) {
		    CsmFile file = (CsmFile) it.next();
		    l.add(file.getAbsolutePath());
		}
		Collections.sort(l);
		for (Iterator it = l.iterator(); it.hasNext();) {
		    print((String) it.next());
		    
		}
		print("\n========== Library files ==========");
		l = new ArrayList();
		for (Iterator it1 = project.getLibraries().iterator(); it1.hasNext();) {
		    ProjectBase lib = (ProjectBase) it1.next();
		    for (Iterator it2 = lib.getFileList().iterator(); it2.hasNext();) {
			CsmFile file = (CsmFile) it2.next();
			l.add(file.getAbsolutePath());
		    }
		}
		Collections.sort(l);
		for (Iterator it = l.iterator(); it.hasNext();) {
		    print((String) it.next());
		    
		}
	    }
            if (printGuessStat) {
                print("\nGuessing statistics:");
                print(
                        "Id"
                        + "\t" + padR("Rule:Line", maxLen)
                        + "\tTime"
                        + "\tCount"
                        + "\tFail"
                        //+ "\tTime in failures"
                        + "\tSuccess, %");
                long guessingTime=0;
                for (int i = 0; i<CPPParserEx.MAX_GUESS_IDX;i++) {
                    guessingTime += CPPParserEx.guessingTimes[i];
                    //double sps = (CPPParserEx.guessingTimes[i] !=0) ? ((double)CPPParserEx.guessingCount[i])/CPPParserEx.guessingTimes[i] : 0;
                    double usa = 0;
                    if( CPPParserEx.guessingCount[i] !=0 ) {
                        usa = (1-((double)CPPParserEx.guessingFailures[i])/CPPParserEx.guessingCount[i]) * 100;
                    }
                    print("" 
                            + i 
                            + "\t" + padR(CPPParserEx.guessingNames[i], maxLen)
                            + "\t" + CPPParserEx.guessingTimes[i]
                            + "\t" + CPPParserEx.guessingCount[i]
                            + "\t" + CPPParserEx.guessingFailures[i]
                            //+ "\t" + (int)sps
                            + "\t" + (int)usa);
                }

                print("\nTotal guessing time: " + guessingTime + "ms " + "(" + ((total.getTime() != 0) ? guessingTime*100/total.getTime() : -1) + "% of total parse time)");
            }
	}
	if( showTime || testRawPerformance ) {
            print("Total parsing time: " + total.getTime() + "ms");
            //print("Average LPS: " + total.getLPS()); 
	    calculateAverageLPS(total, ! testRawPerformance);
	    print("Lines count " + total.lineCount);
	    String text = testRawPerformance ? "Raw performance (average LPS): " : "Average LPS: ";
            print(text + total.getLPS()); 
	    int userFiles = countUserFiles();
	    int systemHeaders = countSystemHeaders();
	    print("" + userFiles + " user files");
	    print("" + systemHeaders + " system headers");
        }
        if( showMemoryUsage ) {
	    showMemoryUsage(memUsed);
        }
        //if( showTime || showMemoryUsage ) {
            print("\n");
        //}
        if (dumpStatistics) {
            if (this.dumpFile != null) {
                try {
                    Diagnostic.dumpUnresolvedStatistics(this.dumpFile, true);
                }
                catch( FileNotFoundException e ) {
                    e.printStackTrace(System.err);
                }
            }  
        }

        if (stopAfterAll) {
            System.out.println("Press any key to finish:");
            try {
                System.in.read();
            } catch (IOException ex) {
                ex.printStackTrace();
            }            
        }
    }

    private void showMemoryUsage(long memUsed) {
	long newMemUsed = usedMemory();
	long memDelta = newMemUsed - memUsed;
	NumberFormat nf = NumberFormat.getIntegerInstance();
	nf.setGroupingUsed(true);
	nf.setMinimumIntegerDigits(6);
	print("Amount of memory used" + getLap() + ": " + nf.format((memDelta)/1024) + " Kb");
	if( memBySize ) {
	    TestResult rInc = new TestResult();
	    TestResult rExc = new TestResult();
	    calculateAverageLPS(rInc, true);
	    calculateAverageLPS(rExc, false);
	    print("User code lines:  " + rExc.lineCount);
	    print("Total lines (including all headers):  " + rInc.lineCount);
	    print("Memory usage per (user) line " + getLap() + '\t' + nf.format(memDelta/rExc.lineCount) + " bytes per line");
	    print("Memory usage per (total) line" + getLap() + '\t' + nf.format(memDelta/rInc.lineCount) + " bytes per line");
	}
    }
    
    private void waitAnyKey() {
	System.out.println("Press any key to continue:");
	try {
	    System.in.read();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    private TestResult test() {
	lap++;
	TestResult total = new TestResult();
	for( int i = 0; i < fileList.size(); i++ ) {
	    try {
                if (!testFolding) {
                    TestResult res = test((File) fileList.get(i));
                    total.accumulate(res);
                } else {
                    testFolding((File)fileList.get(i));
                }
	    }
	    catch( Exception e ) {
		e.printStackTrace(System.err);
	    }
	}
	return total;
    }
    
    private String getLap() {
	return " (lap " + lap + ") ";
    }
    
    private String padR(String s, int len) {
        if (s == null) {
            s="";
        }
	if( s.length() >= len ) {
	    return s;
	}
	else {
	    StringBuffer sb = new StringBuffer(s);
	    sb.setLength(len);
	    for (int i = s.length(); i < len; i++) {
		sb.setCharAt(i, ' ');
	    }
	    return sb.toString();
	}
    }
    
    private void waitProjectParsed() {
	project.waitParse();
	for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
	    CsmProject lib = (CsmProject) it.next();
	    lib.waitParse();
	}
    }
    
    private static final boolean C_SYS_INCLUDE = Boolean.getBoolean("cnd.modelimpl.c.include");
    private static final boolean C_DEFINE = Boolean.getBoolean("cnd.modelimpl.c.define");
    private static final boolean CPP_SYS_INCLUDE = Boolean.getBoolean("cnd.modelimpl.cpp.include");
    private static final boolean CPP_DEFINE = Boolean.getBoolean("cnd.modelimpl.cpp.define");
    
    private PPCallback getCallback(File file)
    {
        //print("SystemIncludePaths: " + systemIncludePaths.toString() + "\n");
        //print("QuoteIncludePaths: " + quoteIncludePaths.toString() + "\n");
        PPCallbackImpl callback = new PPCallbackImpl(project, file.getAbsolutePath());
        ProjectBase.fillCallback(callback, macros, getSysMacros(), quoteIncludePaths, getSystemIncludes(), file);
        return callback;
    }

    private APTSystemStorage sysAPTData = new APTSystemStorage();
    
    private APTIncludeHandler getIncludeHandler(File file) {
        List sysIncludes = sysAPTData.getIncludes("TraceModelSysIncludes", getSystemIncludes());
	List qInc = quoteIncludePaths;
	if( pathsRelCurFile ) {
	    qInc = new ArrayList(quoteIncludePaths.size());
	    for (Iterator it = quoteIncludePaths.iterator(); it.hasNext();) {
		String path = (String) it.next();
		if( !( new File(path).isAbsolute() ) ) {
		    File dirFile = file.getParentFile();
		    File pathFile = new File(dirFile, path);
		    path = pathFile.getAbsolutePath();
		}
		qInc.add(path);
	    }
	}
        return new APTIncludeHandlerImpl(qInc, sysIncludes);
    }
    
    private APTMacroMap getMacroMap(File file)
    {
        //print("SystemIncludePaths: " + systemIncludePaths.toString() + "\n");
        //print("QuoteIncludePaths: " + quoteIncludePaths.toString() + "\n");
        APTMacroMap sysMap = getSysMap(file);
        APTFileMacroMap map = new APTFileMacroMap(sysMap);
        APTMacroUtils.fillMacroMap(map, this.macros);
        return map;
    }

    private APTPreprocState getPreprocState(File file) {
        APTPreprocState preprocState = new APTPreprocStateImpl(getMacroMap(file), getIncludeHandler(file), true);
        return preprocState;
    }
    

    private APTMacroMap getSysMap(File file) {
        APTMacroMap map = sysAPTData.getMacroMap("TraceModelSysMacros", getSysMacros());
        return map;
    }
    
    private List getSystemIncludes() {
        Set all = new HashSet(systemIncludePaths);
        if (CPP_SYS_INCLUDE) {
            // add generated by gcc 3.3.4 on SuSe 9.2 
            // #gcc -x c++ -v -E - < /dev/null
            all.add("/usr/include/g++");
            all.add("/usr/include/g++/i586-suse-linux");
            all.add("/usr/include/g++/backward");
            all.add("/usr/local/include");
            all.add("/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include");
            all.add("/usr/i586-suse-linux/include");
            all.add("/usr/include");
        } else if (C_SYS_INCLUDE) {
            // add generated by gcc 3.3.4 on SuSe 9.2 
            // #gcc -x c -v -E - < /dev/null    
            all.add("/usr/local/include");
            all.add("/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include");
            all.add("/usr/i586-suse-linux/include");
            all.add("/usr/include");                     
        }
        return new ArrayList(all);
    }
    
    private List getSysMacros() {
        Set all = new HashSet();
        if (CPP_DEFINE) {
            // add generated by gcc 3.3.4 on SuSe 9.2 
            // #gcc -x c++ -dM -E - < /dev/null
            all.add("__CHAR_BIT__=8");
            all.add("__cplusplus=1");
            all.add("__DBL_DENORM_MIN__=4.9406564584124654e-324");
            all.add("__DBL_DIG__=15");
            all.add("__DBL_EPSILON__=2.2204460492503131e-16");
            all.add("__DBL_MANT_DIG__=53");
            all.add("__DBL_MAX_10_EXP__=308");
            all.add("__DBL_MAX__=1.7976931348623157e+308");
            all.add("__DBL_MAX_EXP__=1024");
            all.add("__DBL_MIN_10_EXP__=(-307)");
            all.add("__DBL_MIN__=2.2250738585072014e-308");
            all.add("__DBL_MIN_EXP__=(-1021)");
            all.add("__DECIMAL_DIG__=21");
            all.add("__DEPRECATED=1");
            all.add("__ELF__=1");
            all.add("__EXCEPTIONS=1");
            all.add("__FINITE_MATH_ONLY__=0");
            all.add("__FLT_DENORM_MIN__=1.40129846e-45F");
            all.add("__FLT_DIG__=6");
            all.add("__FLT_EPSILON__=1.19209290e-7F");
            all.add("__FLT_EVAL_METHOD__=2");
            all.add("__FLT_MANT_DIG__=24");
            all.add("__FLT_MAX_10_EXP__=38");
            all.add("__FLT_MAX__=3.40282347e+38F");
            all.add("__FLT_MAX_EXP__=128");
            all.add("__FLT_MIN_10_EXP__=(-37)");
            all.add("__FLT_MIN__=1.17549435e-38F");
            all.add("__FLT_MIN_EXP__=(-125)");
            all.add("__FLT_RADIX__=2");
            all.add("__GNUC__=3");
            all.add("__GNUC_MINOR__=3");
            all.add("__GNUC_PATCHLEVEL__=4");
            all.add("__GNUG__=3");
            all.add("__gnu_linux__=1");
            all.add("_GNU_SOURCE=1");
            all.add("__GXX_ABI_VERSION=102");
            all.add("__GXX_WEAK__=1");
            all.add("__i386=1");
            all.add("__i386__=1");
            all.add("i386=1");
            all.add("__INT_MAX__=2147483647");
            all.add("__LDBL_DENORM_MIN__=3.64519953188247460253e-4951L");
            all.add("__LDBL_DIG__=18");
            all.add("__LDBL_EPSILON__=1.08420217248550443401e-19L");
            all.add("__LDBL_MANT_DIG__=64");
            all.add("__LDBL_MAX_10_EXP__=4932");
            all.add("__LDBL_MAX__=1.18973149535723176502e+4932L");
            all.add("__LDBL_MAX_EXP__=16384");
            all.add("__LDBL_MIN_10_EXP__=(-4931)");
            all.add("__LDBL_MIN__=3.36210314311209350626e-4932L");
            all.add("__LDBL_MIN_EXP__=(-16381)");
            all.add("__linux=1");
            all.add("__linux__=1");
            all.add("linux=1");
            all.add("__LONG_LONG_MAX__=9223372036854775807LL");
            all.add("__LONG_MAX__=2147483647L");
            all.add("__NO_INLINE__=1");
            all.add("__PTRDIFF_TYPE__=int");
            all.add("__REGISTER_PREFIX__");
            all.add("__SCHAR_MAX__=127");
            all.add("__SHRT_MAX__=32767");
            all.add("__SIZE_TYPE__=unsigned int");
            all.add("__STDC_HOSTED__=1");
            all.add("__tune_i586__=1");
            all.add("__tune_pentium__=1");
            all.add("__unix=1");
            all.add("__unix__=1");
            all.add("unix=1");
            all.add("__USER_LABEL_PREFIX__");
            all.add("__VERSION__=\"3.3.4 (pre 3.3.5 20040809)\"");
            all.add("__WCHAR_MAX__=2147483647");
            all.add("__WCHAR_TYPE__=long int");
            all.add("__WINT_TYPE__=unsigned int");
        } else if (C_DEFINE) {
            // add generated by gcc 3.3.4 on SuSe 9.2 
            // #gcc -x c -dM -E - < /dev/null
            all.add("__DBL_MIN_EXP__=(-1021)");
            all.add("__FLT_MIN__=1.17549435e-38F");
            all.add("__CHAR_BIT__=8");
            all.add("__WCHAR_MAX__=2147483647");
            all.add("__DBL_DENORM_MIN__=4.9406564584124654e-324");
            all.add("__FLT_EVAL_METHOD__=2");
            all.add("__unix__=1");
            all.add("unix=1");
            all.add("__i386__=1");
            all.add("__SIZE_TYPE__=unsigned=int");
            all.add("__ELF__=1");
            all.add("__DBL_MIN_10_EXP__=(-307)");
            all.add("__FINITE_MATH_ONLY__=0");
            all.add("__GNUC_PATCHLEVEL__=4");
            all.add("__FLT_RADIX__=2");
            all.add("__LDBL_EPSILON__=1.08420217248550443401e-19L");
            all.add("__SHRT_MAX__=32767");
            all.add("__LDBL_MAX__=1.18973149535723176502e+4932L");
            all.add("__linux=1");
            all.add("__unix=1");
            all.add("__LDBL_MAX_EXP__=16384");
            all.add("__LONG_MAX__=2147483647L");
            all.add("__linux__=1");
            all.add("__SCHAR_MAX__=127");
            all.add("__DBL_DIG__=15");
            all.add("__USER_LABEL_PREFIX__");
            all.add("linux=1");
            all.add("__tune_pentium__=1");
            all.add("__STDC_HOSTED__=1");
            all.add("__LDBL_MANT_DIG__=64");
            all.add("__FLT_EPSILON__=1.19209290e-7F");
            all.add("__LDBL_MIN__=3.36210314311209350626e-4932L");
            all.add("__WCHAR_TYPE__=long int");
            all.add("__FLT_DIG__=6");
            all.add("__FLT_MAX_10_EXP__=38");
            all.add("__INT_MAX__=2147483647");
            all.add("__gnu_linux__=1");
            all.add("__FLT_MAX_EXP__=128");
            all.add("__DECIMAL_DIG__=21");
            all.add("__DBL_MANT_DIG__=53");
            all.add("__WINT_TYPE__=unsigned int");
            all.add("__GNUC__=3");
            all.add("__LDBL_MIN_EXP__=(-16381)");
            all.add("__tune_i586__=1");
            all.add("__LDBL_MAX_10_EXP__=4932");
            all.add("__DBL_EPSILON__=2.2204460492503131e-16");
            all.add("__DBL_MAX__=1.7976931348623157e+308");
            all.add("__DBL_MAX_EXP__=1024");
            all.add("__FLT_DENORM_MIN__=1.40129846e-45F");
            all.add("__LONG_LONG_MAX__=9223372036854775807LL");
            all.add("__FLT_MAX__=3.40282347e+38F");
            all.add("__GXX_ABI_VERSION=102");
            all.add("__FLT_MIN_10_EXP__=(-37)");
            all.add("__FLT_MIN_EXP__=(-125)");
            all.add("i386=1");
            all.add("__GNUC_MINOR__=3");
            all.add("__DBL_MAX_10_EXP__=308");
            all.add("__LDBL_DENORM_MIN__=3.64519953188247460253e-4951L");
            all.add("__DBL_MIN__=2.2250738585072014e-308");
            all.add("__PTRDIFF_TYPE__=int");
            all.add("__LDBL_MIN_10_EXP__=(-4931)");
            all.add("__REGISTER_PREFIX__");
            all.add("__LDBL_DIG__=18");
            all.add("__NO_INLINE__=1");
            all.add("__i386=1");
            all.add("__FLT_MANT_DIG__=24");
            all.add("__VERSION__=\"3.3.4 (pre 3.3.5 20040809)\"");            
        }
        return new ArrayList(all);
    }
//    // generates NativeProject using input project info
//    private NativeProject getNativeProject(File file, ProjectBase prj) {
//        
//        
//    }
//    
//    // generates NativeFileItem object using input params info
//    private NativeFileItem getFileItem(File file, ProjectBase prj) {
//        
//        class NFI implements NativeFileItem {
//            File file;
//            NativeProject prj;
//            List sysIncludes;
//            List quoteIncludes;
//            List macros;
//            
//            NFI(File file, NativeProject prj, 
//                    List sysIncludes, List quoteIncludes, List macros) {
//                this.file = file;
//                this.prj = prj;
//                this.sysIncludes = sysIncludes;
//                this.quoteIncludes = quoteIncludes;
//                this.macros = macros;
//            }
//            
//            public NativeProject getNativeProject() {
//                return prj;
//            }
//
//            public File getFile() {
//                return file;
//            }
//
//            public List getIncludePaths() {
//                List includes = new ArrayList();
//                includes.addAll(sysIncludes);
//                includes.addAll(quoteIncludes);
//                return includes;
//            }
//
//            public List getMacroDefinitions() {
//                return macros;
//            }   
//        };
//        
//        return new NFI(file, prj, systemIncludePaths, quoteIncludePaths, Collections.EMPTY_LIST);
//    }
    
    private long testLexer(File file, boolean printTokens) 
        throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {

        print("Testing lexer:");
        long time = System.currentTimeMillis();
	
	TokenStreamSelector newSelector = new TokenStreamSelector();

	PPCallback callback = getCallback(file);
	
	//callback.define("__STDC__", "");
	//callback.define("__PRAGMA_REDEFINE_EXTNAME", "");

        DataInputStream stream = null;
        try {
            stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE));
            CPPLexer lexer = new CPPLexer(stream, newSelector, callback);
            newSelector.addInputStream(lexer, "main");
            newSelector.select("main");
            lexer.init(file.getName(), 0,  null);
            lexer.setBaseFilePath(file.getAbsolutePath());   
            for( Token t = newSelector.nextToken(); t.getType() != CPPTokenTypes.EOF; t = newSelector.nextToken() ) {
                if( readWriteTokens ) {
                    File cache = new File("token.dat");
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(cache));
                    out.writeObject(t);
                    out.close();
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cache), TraceFlags.BUF_SIZE));
                    Token t2 = (Token) in.readObject();
                    in.close();
                    print("original=" + t + " read=" + t2);
                }
                else {
                    if( printTokens) {
                        print("" + t);
                    }
                }
            }
            time = System.currentTimeMillis() - time;
            if( showTime ) {
                print("Scanning " + file.getName() + " took " + time + " ms");
            }
            return time;
        } finally {
            stream.close();
        }
    }

    private long testAPTLexer(File file, boolean printTokens) 
        throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        print("Testing APT lexer:");
        long time = System.currentTimeMillis();
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);
            TokenStream ts = APTTokenStreamBuilder.buildTokenStream(file.getAbsolutePath(), stream);
            for( Token t = ts.nextToken(); !APTUtils.isEOF(t); t = ts.nextToken() ) {
                if( printTokens) {
                    print("" + t);
                }
            }
            time = System.currentTimeMillis() - time;
            if( showTime ) {
                print("APT Lexing " + file.getName() + " took " + time + " ms");
            }   
            return time;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }  

    private long testAPTWalkerVisit(APTFile apt, FileBuffer buffer) throws TokenStreamException, IOException {
        File file = buffer.getFile();
        boolean cleanAPT = (apt == null);
        long time = System.currentTimeMillis();       
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();     
            apt = APTDriver.getInstance().findAPTLight(buffer);
        }           
        APTWalkerTest walker = new APTWalkerTest(apt, getMacroMap(file), getIncludeHandler(file));
        walker.visit();  
        time = System.currentTimeMillis() - time;   
        
        if( showTime ) {                     
            print("Visiting APT "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms");
            print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms");
        }         
        
//        time = System.currentTimeMillis();       
//        if (cleanAPT) {
//            invalidateAPT(file);
//            time = System.currentTimeMillis();  
//            apt = APTDriver.getInstance().findAPT(file);
//        }           
//        walker = new APTWalkerTest(apt, getMacroMap(file), getIncludeHandler(file));
//        walker.nonRecurseVisit();  
//        time = System.currentTimeMillis() - time; 
//        
//        if( showTime ) {                     
//            print("Non recursive visiting APT "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms");
//        }         
        
        return time;
    }

    private long testAPTWalkerGetStream(APTFile apt, FileBuffer buffer, boolean expand, boolean filter, boolean printTokens) throws TokenStreamException, IOException {
        File file = buffer.getFile();
        boolean cleanAPT = (apt == null);     
        long time = System.currentTimeMillis();   
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis(); 
            apt = APTDriver.getInstance().findAPT(buffer);
        }         
        APTMacroMap macroMap = getMacroMap(file);
        APTWalkerTest walker = new APTWalkerTest(apt, macroMap, getIncludeHandler(file));
        TokenStream ts = walker.getTokenStream();
        if (expand) {
            ts = new APTParserMacroExpandedStream(ts, macroMap);
        }
        if (filter) {
            ts = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP).getFilteredStream(new APTCommentsFilter(ts));
        }
        for( Token t = ts.nextToken(); !APTUtils.isEOF(t); t = ts.nextToken() ) {
            if( printTokens) {
                print("" + t);
            }
        }
        time = System.currentTimeMillis() - time;   
        
        if( showTime ) {                     
            print("Getting" + (expand?" expanded":"") + (filter?" filtered":"") + " APT token stream "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms");
            print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms");
        }  
        return time;
    }
    
    private long testAPTParser(FileBuffer buffer, boolean cleanAPT) throws IOException, RecognitionException, TokenStreamException {
        print("Testing APT Parser");
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        File file = buffer.getFile();   
        long time = System.currentTimeMillis();   
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis(); 
        }    
        if (!TraceFlags.USE_APT) {
            new Throwable("cnd.use.apt must be true").printStackTrace(System.err);
            return 0;
        }
        FileImpl fileImpl = null;
        APTPreprocState preprocState = getPreprocState(file);
        fileImpl = (FileImpl) project.testAPTParseFile(file.getAbsolutePath(), preprocState);        
        try {
            fileImpl.scheduleParsing(true);
        }
        catch( InterruptedException e ) {
            // nothing to do
            print("Interrupted parsing");
        }        
        time = System.currentTimeMillis() - time;   
        
        if( showTime ) {                     
            print("Parsing" + (cleanAPT ? " with cleaning APT in driver":"") + " took " + time + " ms");
        }
        return time;
    }
    
    private void testAPT(File file) 
        throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        FileBuffer buffer = new FileBufferFile(file);
        print("Testing APT:" + file);
        long minLexer = Long.MAX_VALUE, maxLexer = Long.MIN_VALUE;
        if (testAPTPlainLexer) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) { 
                long val = testLexer(file, i==-1 ? printTokens : false);
                minLexer = Math.min(minLexer, val);
                maxLexer = Math.max(maxLexer, val);
            }
        }
        long minAPTLexer = Long.MAX_VALUE, maxAPTLexer = Long.MIN_VALUE;
        if (testAPTLexer) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTLexer(file, i==-1 ? printTokens : false);
                minAPTLexer = Math.min(minAPTLexer, val);
                maxAPTLexer = Math.max(maxAPTLexer, val);
            }
        }
        APTFile apt = null;
        minDriver = Long.MAX_VALUE;
        maxDriver = Long.MIN_VALUE;            
        if (testAPTDriver) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                invalidateAPT(buffer);
                apt = testAPTDriver(buffer, i==-1 ? true : false);                
            }
        }
        boolean cleanAPT = (minDriver == Long.MAX_VALUE);        
        
        long minVisit = Long.MAX_VALUE, maxVisit = Long.MIN_VALUE;
        if (testAPTWalkerVisit) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerVisit(apt, buffer);
                minVisit = Math.min(minVisit, val);               
                maxVisit = Math.max(maxVisit, val);               
            }
        }
        long minGetTS = Long.MAX_VALUE, maxGetTS = Long.MIN_VALUE;        
        if (testAPTWalkerGetStream) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, false, false, i==-1 ? printTokens : false);
                minGetTS = Math.min(minGetTS, val);               
                maxGetTS = Math.max(maxGetTS, val);               
            }            
        }  
        long minGetExpandedTS = Long.MAX_VALUE, maxGetExpandedTS = Long.MIN_VALUE;        
        if (testAPTWalkerGetExpandedStream) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, true, false, i==-1 ? printTokens : false);
                minGetExpandedTS = Math.min(minGetExpandedTS, val);               
                maxGetExpandedTS = Math.max(maxGetExpandedTS, val);               
            }            
        }         
        long minGetFilteredTS = Long.MAX_VALUE, maxGetFilteredTS = Long.MIN_VALUE;        
        if (testAPTWalkerGetFilteredStream) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, true, true, i==-1 ? printTokens : false);
                minGetFilteredTS = Math.min(minGetFilteredTS, val);               
                maxGetFilteredTS = Math.max(maxGetFilteredTS, val);               
            }            
        } 
        long minParsing = Long.MAX_VALUE, maxParsing = Long.MIN_VALUE;
        if (testParser) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testParser(buffer.getFile());
                minParsing = Math.min(minParsing, val);               
                maxParsing = Math.max(maxParsing, val);                 
            }
        }        
        long minAPTParsing = Long.MAX_VALUE, maxAPTParsing = Long.MIN_VALUE;
        if (testAPTParser) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTParser(buffer, cleanAPT);
                minAPTParsing = Math.min(minAPTParsing, val);               
                maxAPTParsing = Math.max(maxAPTParsing, val);                 
            }            
        }
        if( showTime ) {                     
            print("APT BEST/WORST results for " + file.getAbsolutePath());
            if (minLexer != Long.MAX_VALUE) {
                print(minLexer  + " ms BEST Plain lexer");
                print(maxLexer  + " ms WORST Plain lexer");
            }
            if (minAPTLexer != Long.MAX_VALUE) {
                print(minAPTLexer + " ms BEST APT lexer");
                print(maxAPTLexer + " ms WORST APT lexer");
            }
            if (minDriver != Long.MAX_VALUE) {
                print(minDriver + " ms BEST Building APT:");
                print(maxDriver + " ms WORST Building APT:");
            }

            if (minVisit != Long.MAX_VALUE) {
                print(minVisit + " ms BEST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:":":"));
                print(maxVisit + " ms WORST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:":":"));
            }
            if (minGetTS != Long.MAX_VALUE) {
                print(minGetTS + " ms BEST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":"));
                print(maxGetTS + " ms WORST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":"));
            }            
            if (minGetExpandedTS != Long.MAX_VALUE) {
                print(minGetExpandedTS + " ms BEST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":"));
                print(maxGetExpandedTS + " ms WORST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":"));
            } 
            if (minGetFilteredTS != Long.MAX_VALUE) {
                print(minGetFilteredTS + " ms BEST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":"));
                print(maxGetFilteredTS + " ms WORST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":"));
            }   
            if (minParsing != Long.MAX_VALUE) {
                print(minParsing + " ms BEST Plaing Parsing");
                print(maxParsing + " ms WORST Plaing Parsing");
            }  
            if (minAPTParsing != Long.MAX_VALUE) {
                print(minAPTParsing + " ms BEST APT parsing" + (cleanAPT ? " with cleaning APT in driver:":":"));
                print(maxAPTParsing + " ms WORST APT parsing" + (cleanAPT ? " with cleaning APT in driver:":":"));
            }              
        }           
    }

    private static String firstFile = null;
    private void invalidateAPT(final FileBuffer buffer) {
        File file = buffer.getFile();
        if (firstFile == null || firstFile.equalsIgnoreCase(file.getAbsolutePath())) {
            firstFile = file.getAbsolutePath();
            APTDriver.getInstance().invalidateAll();
            project.invalidateFiles();
        } else {
            APTDriver.getInstance().invalidateAPT(buffer);
        }
    }

    long minDriver = Long.MAX_VALUE;    
    long maxDriver = Long.MIN_VALUE;   
    private APTFile testAPTDriver(final FileBuffer buffer, boolean buildXML) throws IOException, FileNotFoundException {
        File file = buffer.getFile();
        long oldMem = usedMemory();
        long time = System.currentTimeMillis();        
        APTFile apt = APTDriver.getInstance().findAPT(buffer);
        time = System.currentTimeMillis() - time;   
        long newMem = usedMemory();
        if( showTime ) {   
            minDriver = Math.min(minDriver, time);
            maxDriver = Math.max(maxDriver, time);
            print("Building APT for " + file.getName() + "\n SIZE OF FILE:" + file.length()/1024 + "Kb\n TIME: took " + time + " ms\n MEMORY: changed from " + 
                    (oldMem)/(1024) + " to " + newMem/(1024)+ "[" + (newMem-oldMem)/1024 + "]Kb");
        }        
        
//        System.out.println("apt tree: \n" + APTTraceUtils.toStringList(apt));
        if (buildXML) {
            File outDir = new File("/tmp/aptout/");
            outDir.mkdirs();
            File outFile = new File(outDir, file.getName()+".xml");
            if (outFile.exists()) {
                outFile.delete();            
            }
            outFile.createNewFile();
            Writer out = new BufferedWriter(new FileWriter(outFile));
            APTTraceUtils.xmlSerialize(apt, out);
            out.flush();
            APT light = APTBuilder.buildAPTLight(apt);
            File outFileLW = new File(outDir, file.getName()+"_lw.xml");
            if (outFileLW.exists()) {
                outFileLW.delete();            
            }
            outFileLW.createNewFile();
            Writer outLW = new BufferedWriter(new FileWriter(outFileLW));
            APTTraceUtils.xmlSerialize(light, outLW);
            outLW.flush();
        }
        return apt;
    }

    private long usedMemory() {
        System.gc();
	return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    private TestResult test(File file) 
        throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        
	TestResult result = new TestResult();
	
        if( testLexer ) {
            testLexer(file, false);
        }

        if (testAPT) {
            testAPT(file);
            if (breakAfterAPT) {
                return new TestResult();
            }
        }
        
        AST ast = null;

        if (dumpStatistics) {
            Diagnostic.initFileStatistics(file.getAbsolutePath());
        }
        
        long time = System.currentTimeMillis();

        if( readAst ) {
            long t2 = System.currentTimeMillis();
            ast = cache.readAst(file);
            t2 = System.currentTimeMillis() - t2;
            print("AST read; time: " + t2 + " ms");
        }

        AST tree = null;
        FileImpl fileImpl = null;
        int errCount = 0;
        
        PPCallback callback = null;

        if( testParser ) {
            testParser(file);
        }
        else {
            callback = getCallback(file);
            APTPreprocState preprocState = null;            
            if( ast == null ) {
		if( TraceFlags.USE_APT ) {
		    preprocState = getPreprocState(file);
		    fileImpl = (FileImpl) project.testAPTParseFile(file.getAbsolutePath(), preprocState);
		}
		else {
		    fileImpl = (FileImpl) project.testParseFile(file.getAbsolutePath(), callback);
		}
                try {
                    fileImpl.scheduleParsing(true);
		    if( preprocState != null ) { // i.e. if TraceFlags.USE_APT
			preprocState.setState(fileImpl.getPreprocStateState());
		    }
                    fileImpl.setPreprocState(null);
                    waitProjectParsed();
                }
                catch( InterruptedException e ) {
                    // nothing to do
                }
//                fileImpl = new FileImpl(new FileBufferFile(file), project,callback);
//                tree = fileImpl.parse();
            }
            else {
                //long t2 = System.currentTimeMillis();
                fileImpl = new FileImpl(new FileBufferFile(file), project, callback);
                tree = ast;
                fileImpl.render(tree);
                //t2 = System.currentTimeMillis() - t2;
                //print("Creating FileImpl took " + t2 + " ms");
            }
            errCount = fileImpl.getErrorCount();
            if ( dumpPPCallback ) {
                if (TraceFlags.USE_APT) {
                    
                    dumpMacroMap(preprocState.getMacroMap());
                } else {
                    dumpCallback(callback);
                }
            }
        }
        time = System.currentTimeMillis() - time;
        if( showTime ) {
	    result.setTime(time);
	    result.setLineCount(countLines(fileImpl));
	    if( ! quiet ) {
		print("Processing " + file.getName() + " took " + time + " ms; LPS=" + result.getLPS() + "; error count: " + errCount);
	    }
        }
        
        if (dumpStatistics) {
            if (this.dumpDir != null) {
                String postfix = statPostfix;
                if (Diagnostic.getStatisticsLevel() > 1) {
                    postfix += "."+Diagnostic.getStatisticsLevel();
                }
                String name = file.getName()+postfix;
                String dumpFile = new File(this.dumpDir, name).getAbsolutePath();
                Diagnostic.dumpFileStatistics(dumpFile);
            }
            if (this.dumpFile != null) {
                Diagnostic.dumpFileStatistics(this.dumpFile, true);
            }  
        }
        
        if (testCache)
            cacheTimes.put(file.getName(), new Long(time));

        if( dumpAst  ) {
            if( tree == null ) {
                tree = fileImpl.parse(callback);
            }
	    System.out.println("AST DUMP for file " + file.getName());
            dumpAst(tree);
	}
        
        if( writeAst && tree != null ) { 
            long t2 = System.currentTimeMillis();
            t2 = System.currentTimeMillis() - t2;
            cache.writeAst(tree, file); 
            print("AST stored; time: " + t2 + " ms");
        }
        
        if( dumpModel ) {
            if( fileImpl != null ) {
                tracer.setDeep(deep);
                tracer.setTestUniqueName(testUniqueName);
                tracer.dumpModel(fileImpl);
                if (!dumpFileOnly) {
                    tracer.dumpModel(project);
                }
            } else {
                print("FileImpl is null - not possible to dump File Model");
            }
        }
        
        if( showAstWindow  ) {
            if( tree== null ) {
                tree = fileImpl.parse(callback);
            }
            test(tree, file.getName());
	}                

        return result;
    }
    
    private boolean hasNonEmptyIncludes(CsmFile fileImpl) {
	for (Iterator it = fileImpl.getIncludes().iterator(); it.hasNext();) {
	    CsmInclude inc = (CsmInclude) it.next();
	    if( inc.getIncludeFile() != null ) {
		return true;
	    }
	}
	return false;
    }
    
    private long countLines(CsmFile fileImpl) {
	return countLines(fileImpl, false);
    }
    
    private long countLines(CsmFile fileImpl, boolean allowResolvedIncludes) {
	if( fileImpl == null ) {
	    return -1;
	}
	if( ! allowResolvedIncludes && hasNonEmptyIncludes(fileImpl) ) { //! fileImpl.getIncludes().isEmpty() ) {
	    return -1;
	}
	String text = fileImpl.getText();
	long cnt = 0;
	for( int pos = 0; pos < text.length(); pos++ ) {
	    if( text.charAt(pos) == '\n' ) {
		cnt++;
	    }
	}
	return cnt;
    }
    
    private long testParser(File file) throws IOException, RecognitionException, TokenStreamException {
        print("Testing Plaing Parser");
        /* merging DI changes
        TokenStreamSelector selector = new TokenStreamSelector();
        CPPLexer lexer = new CPPLexer(new DataInputStream(new FileInputStream(file)), selector);
        selector.addInputStream(lexer, "main");
        selector.select("main");
        CPPParserEx parser = new CPPParserEx(selector);
        lexer.init(file.getName(), flags,  parser);
        parser.init(file.getName(), flags);
         */
//        InputStream stream = null;
        long time = System.currentTimeMillis();   
        boolean orig = TraceFlags.USE_APT;
        if (TraceFlags.USE_APT) {
            new Throwable("cnd.use.apt must be false").printStackTrace(System.err);
            return 0;
        }
        FileImpl fileImpl = (FileImpl) project.testParseFile(file.getAbsolutePath(), getCallback(file));
        try {
            fileImpl.scheduleParsing(true);
        }
        catch( InterruptedException e ) {
            // nothing to do
        }        
        time = System.currentTimeMillis() - time;           
  
        if( showTime ) {                     
            print("Plain Parsing" + " took " + time + " ms");
        }        
        return time;
    }

    private void test(AST tree, String label) {
        
//	    System.out.println("LIST:");
//	    System.out.println(tree.toStringList());
//
//	    System.out.println("DUMP:");
//	    DumpASTVisitor visitor = new DumpASTVisitor();
//	    visitor.visit(tree);

	    ASTFrameEx frame = new ASTFrameEx(label, tree);
	    frame.setVisible(true);        
    }
    
    private boolean isDummyUnresolved(CsmDeclaration decl) {
        return decl == null || decl instanceof Unresolved.UnresolvedClass;
    }
    
    private void dumpAst(AST ast) {
        ASTVisitor visitor = new ASTVisitor() {
            public void visit(AST node) {
                for( AST node2 = node; node2 != null; node2 = node2.getNextSibling() ) {
                    String ofStr = (node2 instanceof CsmAST) ? (" offset=" + ((CsmAST) node2).getOffset() + " file = " + ((CsmAST)node2).getFilename()) : "";
                    print("" + node2.getText() + " [" +  node2.getType() + "] " + node2.getLine() + ':' + node2.getColumn() + ofStr);
                    if (node2.getFirstChild() != null) {
                        indent();
                        visit(node2.getFirstChild());
                        unindent();
                    }
                }
            }
        };
        visitor.visit(ast);
    }

    private void dumpCallback(PPCallback callback) {
        tracer.print("State of callback:");
        tracer.print(callback == null ? "empty callback" : 
            ((callback instanceof PPCallbackImpl) ? ((PPCallbackImpl)callback).toString(true) :
                callback.toString()));
    }
    
    private void dumpMacroMap(APTMacroMap macroMap) {
        tracer.print("State of macro map:");
        tracer.print(macroMap == null ? "empty macro map" : macroMap.toString());
    }
    
    private void testLibProject() {
        LibProjectImpl libProject = new LibProjectImpl(model, "/usr/include");
        model.addProject(libProject);
        tracer.dumpModel(libProject);
    }
    
    private void print(String s) {
	tracer.print(s);
    }
    
    private void indent() {
	tracer.indent();
    }
    
    private void unindent() {
	tracer.unindent();
    }

    private int countUserFiles() {
	return project.getFileList().size();
    }
    
    private int countSystemHeaders() {
	int cnt = 0;
	Set processedProjects = new HashSet();
	for (Iterator it = project.getLibraries().iterator(); it.hasNext();) {
	    cnt += countFiles((ProjectBase) it.next(), processedProjects);
	}
	return cnt;
    }
    
    private int countFiles(ProjectBase prj, Collection processedProjects) {
	if( processedProjects.contains(prj) ) {
	    return 0;	// already counted
	}
	int cnt = prj.getFileList().size();
	for (Iterator it = prj.getLibraries().iterator(); it.hasNext();) {
	    cnt += countFiles((ProjectBase) it.next(), processedProjects);
	}
	return cnt;
    }

    private void calculateAverageLPS(TestResult total, boolean includeLibs) {
	total.lineCount = 0;
	for (Iterator it = project.getFileList().iterator(); it.hasNext();) {
	    CsmFile file = (CsmFile) it.next();
	    total.lineCount += countLines(file, true);
	}
	if( includeLibs ) {
	    for (Iterator it1 = project.getLibraries().iterator(); it1.hasNext();) {
		ProjectBase lib = (ProjectBase) it1.next();
		for (Iterator it2 = lib.getFileList().iterator(); it2.hasNext();) {
		    CsmFile file = (CsmFile) it2.next();
		    total.lineCount += countLines(file, true);
		}
	    }
	}
    }

    private void testFolding(File file) {
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
        if (is == null) {
            return;
        }
        Reader reader  = new InputStreamReader(is);
        reader = new BufferedReader(reader);
        List<CppFoldRecord> folds = new APTFoldingProvider().parse(file.getAbsolutePath(), reader);
        try {
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        printFolds(file.getAbsolutePath(), folds);
    }

    private void printFolds(String file, List<CppFoldRecord> folds) {
        Collections.sort(folds, FOLD_COMPARATOR);
        System.out.println("Foldings of the file " + file);
        for (Iterator it = folds.iterator(); it.hasNext();) {
            CppFoldRecord fold = (CppFoldRecord) it.next();
            System.out.println(fold);
        }
    }
    
    private static Comparator<CppFoldRecord> FOLD_COMPARATOR = new Comparator<CppFoldRecord>() {
        public int compare(CppFoldRecord o1, CppFoldRecord o2) {
            int start1 = o1.getStartLine();
            int start2 = o2.getStartLine();
            if (start1 == start2) {
                return o1.getStartOffset() - o2.getStartOffset();
            } else {
                return start1 - start2;
            }
        }        
    };
}
