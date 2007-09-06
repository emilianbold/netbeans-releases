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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.text.NumberFormat;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.LibProjectImpl;
import java.io.*;
import java.util.*;
import java.util.List;

import antlr.*;
import antlr.collections.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.*;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.editor.parser.FoldingParser;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

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
				return "N/A"; // NOI18N
			} else {
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
				} else {
					//		    lineCount = -1;
				}
			}
		}
	}
	
	
	private static final int APT_REPEAT_TEST =Integer.getInteger("apt.repeat.test", 3).intValue(); // NOI18N

        public static boolean LEAVE_PP_STATE_UNCLEANED = false;
	
	
	public static void main(String[] args) {
		new TraceModel().test(args);
		if (TraceFlags.USE_AST_CACHE) {
			CacheManager.getInstance().close();
		} else {
                    APTDriver.getInstance().close();
                }
		//System.out.println("" + org.netbeans.modules.cnd.apt.utils.APTIncludeUtils.getHitRate());
	}
	
	private ModelImpl model;
        
        // only one of project/projectUID must be used 
	private ProjectBase project;
        private CsmUID<CsmProject> projectUID;
        
	private Cache cache;
	
	private static CsmTracer tracer = new CsmTracer(false);

	private boolean showAstWindow = false;
	private boolean dumpAst = false;
	private boolean dumpModel = false;
	private boolean dumpLib = false;
	private boolean dumpFileOnly = false;
	private boolean showTime = false;
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
        private boolean dumpModelAfterCleaningCache = false; // --clean4dump
	
	private List quoteIncludePaths = new ArrayList();
	private List systemIncludePaths = new ArrayList();
	private List currentIncludePaths = null;
	
	private List macros = new ArrayList();
	
	private List fileList = new ArrayList();
	
	private boolean dumpStatistics = false;
	private static final int DEFAULT_TRACEMODEL_STATISTICS_LEVEL = 1;
	private String  dumpFile = null;
	private String  dumpDir  = null;
	private static final String statPostfix = ".stat"; // NOI18N
	
	// Cache options
	private boolean enableCache = false;
	private boolean testCache = false;
	
	// Callback options
	private boolean dumpPPState = false;

        public void setDumpModel(boolean dumpModel) {
            this.dumpModel = dumpModel;
        }

        public void setDumpPPState(boolean dumpPPState) {
            this.dumpPPState = dumpPPState;
            this.LEAVE_PP_STATE_UNCLEANED = true;
        }
	
	// if true, then relative include paths oin -I option are considered
	// to be based on the file that we currently compile rather then current dir
	private boolean pathsRelCurFile = false;
	
	private boolean listFilesAtEnd = false;
	private boolean testRawPerformance = false;
	private boolean printUserFileList = false;
	private boolean quiet = false;
	private boolean memBySize = false;
	private boolean doCleanRepository = Boolean.getBoolean("cnd.clean.repository");
	
	private boolean testFolding = false;
	
	private Map/*<String, Integer>*/ cacheTimes = new HashMap();
	
	private int lap = 0;
	
	public TraceModel() {
            RepositoryUtils.cleanCashes();            
            model =  (ModelImpl) CsmModelAccessor.getModel(); // new ModelImpl(true);
            if( model == null ) {
                model = new ModelImpl();
            }
            model.startup();
            initProject();
            currentIncludePaths = quoteIncludePaths;
        }

        /*package*/ final void setIncludePaths(List<String> sysIncludes, List<String> usrIncludes) {
            this.quoteIncludePaths = usrIncludes;
            this.systemIncludePaths = sysIncludes;
            this.currentIncludePaths = this.quoteIncludePaths;
        }
        
        /*package*/ final void shutdown() {
            model.shutdown();
            RepositoryUtils.cleanCashes();
        }
        
	private void initProject() {
		if( getProject() != null ) {
			Object platformProject = getProject().getPlatformProject();
			getProject().dispose(true);
			((ModelImpl) CsmModelAccessor.getModel()).removeProject(platformProject);
		}
		setProject(model.addProject("DummyProject", "DummyProject", true)); // NOI18N
	}
	
	private boolean processFlag(char flag, String argRest) {
		boolean argHasBeenEaten = false;
		switch( flag ) {
			case 'n':   deep = false; break;
			case 'e':	System.setErr(System.out); break;
			case 'w':	showAstWindow = true; break;
			case 'a':	dumpAst = true; break;
			case 'm':	dumpModel = true; dumpFileOnly = false; break; // -m overrides -f
			case 'M':   showMemoryUsage = true; break;
			case 'u':   testUniqueName = true; break;
			case 'f':   if( ! dumpModel ) { // do not ovverride -m
					dumpModel = true; 
					dumpFileOnly = true; 
				    } 
				    break;
			case 't':	showTime = true; break;
			//            case 'L':   testLexer = true; break;
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
			case 'p':   dumpPPState = true; LEAVE_PP_STATE_UNCLEANED = true; break;
			case 'D':   macros.add(argRest);argHasBeenEaten = true;break;
			// "-SDir" defines dump directory for per file statistics
			case 'S':   dumpStatistics=true;
			if (argRest.length() > 0) {
				// dump directory for per file statistics
				File dumpDir = new File(argRest);
				dumpDir.mkdirs();
				if (!dumpDir.isDirectory()) {
					print("Parameter -S" + argRest + " does not specify valid directory"); // NOI18N
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
			//            case 'b':   testAPTPlainLexer = true; testAPT = true; breakAfterAPT = true; break;
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
	
	protected void processFlag(String flag) {
		if( "dumplib".equals(flag) ) { // NOI18N
			dumpLib = true;
		} else if( "relpath".equals(flag) ) { // NOI18N
			pathsRelCurFile = true;
		} else if( "listfiles".equals(flag) ) { // NOI18N
			listFilesAtEnd = true;
		} else if( "raw".equals(flag) ) { // NOI18N
			testRawPerformance = true;
			//TraceFlags.DO_NOT_RENDER = true;
		} else if( "listfiles".equals(flag) ) { // NOI18N
			printUserFileList = true;
		} else if( "mbs".equals(flag) ) { // NOI18N
			memBySize = true;
		} else if( "cleanrepository".equals(flag) ) { // NOI18N
			doCleanRepository = true;
		} else if ( "folding".equals(flag)) { // NOI18N
			testFolding = true;
		} else if ( "clean4dump".equals(flag)) { // NOI18N
                        dumpModelAfterCleaningCache = true;
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
	    try {
                processArguments(args);
		doTest();
	    }
	    finally {
		model.shutdown();
	    }
	}
	
	/*package*/ void doTest() {
		long time = 0;
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
			} catch( Exception e ) {
				e.printStackTrace(System.err);
				return;
			}
		}
		
		if( writeAst && readAst ) {
			print("Impossible options combination: both writing and reading AST\n"); // NOI18N
			return;
		}
		if( useZip && ! (writeAst || readAst) ) {
			print("Impossible options combination: using ZIP format, but neither writing nor reading AST. Ignoring ZIP format.\n"); // NOI18N
			useZip = false;
		}
		
		if (testCache) {
			//print("Test cache mode ON." + '\n');
			enableCache = true;
		} else
			//print("Setting cache to " + (enableCache ? "ON" : "OFF") + '\n');
			
			if (dumpStatistics) {
				if (dumpFile == null && dumpDir == null) {
					print("Turning OFF statistics as neither global file nor directory is specified"); // NOI18N
					dumpStatistics = false;
				} else {
					print("Dumping Statistics is ON"); // NOI18N
					if (Diagnostic.getStatisticsLevel() == 0) {
						// need to set the default level
						Diagnostic.setStatisticsLevel(DEFAULT_TRACEMODEL_STATISTICS_LEVEL);
					}
					if (dumpFile != null) {
						print("Global Dump file is " + dumpFile); // NOI18N
					}
					if (dumpDir != null) {
						print("Dump directory for per file statistics is " + dumpDir); // NOI18N
					}
				}
			}
		
		if( testLibProject ) {
			testLibProject();
		}
		
		if( printUserFileList ) {
			print("Processing files:\n" + fileList.toString() + '\n'); // NOI18N
		}
		
		long memUsed = 0;
		if( showMemoryUsage ) {
			memUsed = usedMemory();
		}
		
		long t = System.currentTimeMillis();
		TestResult total = test();
		total.time = System.currentTimeMillis() - t;
		
		if( testRawPerformance ) {
			print("Take one finished."); // NOI18N
			print("Total parsing time " + total.time + " ms"); // NOI18N
			calculateAverageLPS(total, true);
			print("Lines count " + total.lineCount); // NOI18N
			print("Average LPS " + total.getLPS()); // NOI18N
			
			if( showMemoryUsage ) {
				showMemoryUsage(memUsed);
			}
			
			//	    for (int i = 0; i < 100; i++) {
			//		initProject();
			//		test();
			//		showMemoryUsage(memUsed);
			//	    }
			
			print("\nTesting raw performance: parsing project, take two\n"); // NOI18N
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
			for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
				CsmProject lib = (CsmProject) it.next();
				tracer.dumpModel(lib);
			}
		}
		
		if( isShowTime() ) {
			
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
				print("\n========== User project files =========="); // NOI18N
				List l = new ArrayList(getProject().getAllFiles().size());
				for (Iterator it = getProject().getAllFiles().iterator(); it.hasNext();) {
					CsmFile file = (CsmFile) it.next();
					l.add(file.getAbsolutePath());
				}
				Collections.sort(l);
				for (Iterator it = l.iterator(); it.hasNext();) {
					print((String) it.next());
					
				}
				print("\n========== Library files =========="); // NOI18N
				l = new ArrayList();
				for (Iterator it1 = getProject().getLibraries().iterator(); it1.hasNext();) {
					ProjectBase lib = (ProjectBase) it1.next();
					for (Iterator it2 = lib.getAllFiles().iterator(); it2.hasNext();) {
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
				print("\nGuessing statistics:"); // NOI18N
				print(
						"Id" // NOI18N
						+ "\t" + padR("Rule:Line", maxLen) // NOI18N
						+ "\tTime" // NOI18N
						+ "\tCount" // NOI18N
						+ "\tFail" // NOI18N
						//+ "\tTime in failures"
						+ "\tSuccess, %"); // NOI18N
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
							+ "\t" + padR(CPPParserEx.guessingNames[i], maxLen) // NOI18N
							+ "\t" + CPPParserEx.guessingTimes[i] // NOI18N
							+ "\t" + CPPParserEx.guessingCount[i] // NOI18N
							+ "\t" + CPPParserEx.guessingFailures[i] // NOI18N
							//+ "\t" + (int)sps
							+ "\t" + (int)usa); // NOI18N
				}
				
				print("\nTotal guessing time: " + guessingTime + "ms " + "(" + ((total.getTime() != 0) ? guessingTime*100/total.getTime() : -1) + "% of total parse time)"); // NOI18N
			}
		}
		if( isShowTime() || testRawPerformance ) {
			print("Total parsing time: " + total.getTime() + "ms"); // NOI18N
			//print("Average LPS: " + total.getLPS());
			calculateAverageLPS(total, ! testRawPerformance);
			print("Lines count " + total.lineCount); // NOI18N
			String text = testRawPerformance ? "Raw performance (average LPS): " : "Average LPS: "; // NOI18N
			print(text + total.getLPS());
			int userFiles = countUserFiles();
			int systemHeaders = countSystemHeaders();
			print("" + userFiles + " user files"); // NOI18N
			print("" + systemHeaders + " system headers"); // NOI18N
		}
		if( showMemoryUsage ) {
			showMemoryUsage(memUsed);
		}
		if( isShowTime() || showMemoryUsage || dumpModel || dumpFileOnly || dumpPPState) {
                    print("\n"); // NOI18N
		}
		if (dumpStatistics) {
			if (this.dumpFile != null) {
				try {
					Diagnostic.dumpUnresolvedStatistics(this.dumpFile, true);
				} catch( FileNotFoundException e ) {
					e.printStackTrace(System.err);
				}
			}
		}
                
                if (TraceFlags.CLEAN_MACROS_AFTER_PARSE) {
                    List restoredFiles = ProjectBase.testGetRestoredFiles();
                    if (restoredFiles != null) {
                        System.err.println("the number of restored files " + restoredFiles.size());
                        for (int i = 0; i < restoredFiles.size(); i++) {
                            System.err.println("#"+i+":"+restoredFiles.get(i));
                        }
                    }
                }
                
                if (dumpModelAfterCleaningCache) {
			anyKey("Press any key to clean repository:"); // NOI18N
                        RepositoryAccessor.getRepository().debugClear();
                        System.gc();System.gc();System.gc();System.gc();System.gc();
                        anyKey("Press any key to dump model:"); // NOI18N
                        if (!dumpFileOnly) {
                                tracer.dumpModel(getProject());
                        }                        
                }
		if (stopAfterAll) {
                        System.gc();
			anyKey("Press any key to finish:"); // NOI18N
		}
	}

        /*package*/ void processArguments(final String[] args) {
            for( int i = 0; i < args.length; i++ ) {
                    if( args[i].startsWith("--") ) { // NOI18N
                            processFlag(args[i].substring(2));
                    } else
                            if( args[i].startsWith("-") ) { // NOI18N
                                    for( int charIdx = 1; charIdx < args[i].length(); charIdx++ ) {
                                            boolean argHasBeenEaten = processFlag(args[i].charAt(charIdx), args[i].substring(charIdx+1));
                                            if (argHasBeenEaten)
                                                    break;
                                    }
                            } else {
                            addFile(fileList, new File(args[i]));
                            }
            }
        }
	
        private void anyKey(String message) {
            System.err.println(message);
            try {
                    System.in.read();
            } catch (IOException ex) {
                    ex.printStackTrace();
            }            
        }
        
	private void showMemoryUsage(long memUsed) {
		long newMemUsed = usedMemory();
		long memDelta = newMemUsed - memUsed;
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(true);
		nf.setMinimumIntegerDigits(6);
		print("Amount of memory used" + getLap() + ": " + nf.format((memDelta)/1024) + " Kb"); // NOI18N
		if( memBySize ) {
			TestResult rInc = new TestResult();
			TestResult rExc = new TestResult();
			calculateAverageLPS(rInc, true);
			calculateAverageLPS(rExc, false);
			print("User code lines:  " + rExc.lineCount); // NOI18N
			print("Total lines (including all headers):  " + rInc.lineCount); // NOI18N
			print("Memory usage per (user) line " + getLap() + '\t' + nf.format(memDelta/rExc.lineCount) + " bytes per line"); // NOI18N
			print("Memory usage per (total) line" + getLap() + '\t' + nf.format(memDelta/rInc.lineCount) + " bytes per line"); // NOI18N
		}
	}
	
	private void waitAnyKey() {
		System.out.println("Press any key to continue:"); // NOI18N
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
			} catch( Exception e ) {
				e.printStackTrace(System.err);
			}
		}
                
		return total;
	}
	
	private String getLap() {
		return " (lap " + lap + ") "; // NOI18N
	}
	
	private String padR(String s, int len) {
		if (s == null) {
			s="";
		}
		if( s.length() >= len ) {
			return s;
		} else {
			StringBuilder sb = new StringBuilder(s);
			sb.setLength(len);
			for (int i = s.length(); i < len; i++) {
				sb.setCharAt(i, ' ');
			}
			return sb.toString();
		}
	}
	
	private void waitProjectParsed(boolean trace) {
            boolean wasWait = true;
            while (wasWait) {
                wasWait = waitParsed(getProject(), trace);
                if (getProject().getLibraries().size() > 0) {
                    if (trace) System.err.println("checking libraries");
                    for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
                            CsmProject lib = (CsmProject) it.next();
                            if (trace) System.err.println("checking library " + lib.getName());
                            wasWait |= waitParsed((ProjectBase) lib, trace);
                    }
                }
            }
	}
        
        private boolean waitParsed(ProjectBase project, boolean trace) {
            boolean wasWait = false;
            project.waitParse();
            if (false && TraceFlags.USE_REPOSITORY) {
                for (Iterator it = project.getAllFiles().iterator(); it.hasNext();) {
                    FileImpl file = (FileImpl) it.next();
                    if (!file.isParsed()) {
                        wasWait = true;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }   
            }
            return wasWait;
        }
	
	private static final boolean C_SYS_INCLUDE = Boolean.getBoolean("cnd.modelimpl.c.include");
	private static final boolean C_DEFINE = Boolean.getBoolean("cnd.modelimpl.c.define");
	private static final boolean CPP_SYS_INCLUDE = Boolean.getBoolean("cnd.modelimpl.cpp.include");
	private static final boolean CPP_DEFINE = Boolean.getBoolean("cnd.modelimpl.cpp.define");
        private static final boolean DISABLE_PREDEFINED = DebugUtils.getBoolean("cnd.modelimpl.disable.predefined", true);
	
	private APTSystemStorage sysAPTData = APTSystemStorage.getDefault();
	
	private APTIncludeHandler getIncludeHandler(File file) {
		List sysIncludes = sysAPTData.getIncludes("TraceModelSysIncludes", getSystemIncludes()); // NOI18N
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
                StartEntry startEntry = new StartEntry(file.getAbsolutePath(),
                        RepositoryUtils.UIDtoKey(getProject().getUID()));
		return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludes, qInc);
	}
	
	private APTMacroMap getMacroMap(File file) {
		//print("SystemIncludePaths: " + systemIncludePaths.toString() + "\n");
		//print("QuoteIncludePaths: " + quoteIncludePaths.toString() + "\n");
		APTMacroMap map = APTHandlersSupport.createMacroMap(getSysMap(file), this.macros);
		return map;
	}
	
	private APTPreprocHandler getPreprocHandler(File file) {
		APTPreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(getMacroMap(file), getIncludeHandler(file), !file.getPath().endsWith(".h")); // NOI18N
		return preprocHandler;
	}
	
	
	private APTMacroMap getSysMap(File file) {
		APTMacroMap map = sysAPTData.getMacroMap("TraceModelSysMacros", getSysMacros()); // NOI18N
		return map;
	}
	
	private List getSystemIncludes() {
		Set all = new HashSet(systemIncludePaths);
                if (DISABLE_PREDEFINED) {
                    return new ArrayList(all);
                }            
		if (CPP_SYS_INCLUDE) {
			// add generated by gcc 3.3.4 on SuSe 9.2
			// #gcc -x c++ -v -E - < /dev/null
                    if ((Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0) {
 			all.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3"); // NOI18N
			all.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/i386-pc-solaris2.10"); // NOI18N
			all.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/../../../../include/c++/3.4.3/backward"); // NOI18N
			all.add("/usr/local/include"); // NOI18N
			all.add("/usr/sfw/include"); // NOI18N
			all.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include"); // NOI18N
			all.add("/usr/include"); // NOI18N
                    } else {
			all.add("/usr/include/g++"); // NOI18N
			all.add("/usr/include/g++/i586-suse-linux"); // NOI18N
			all.add("/usr/include/g++/backward"); // NOI18N
			all.add("/usr/local/include"); // NOI18N
			all.add("/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include"); // NOI18N
			all.add("/usr/i586-suse-linux/include"); // NOI18N
			all.add("/usr/include"); // NOI18N
                    }
		} else if (C_SYS_INCLUDE) {
			// add generated by gcc 3.3.4 on SuSe 9.2
			// #gcc -x c -v -E - < /dev/null
			all.add("/usr/local/include"); // NOI18N
			all.add("/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include"); // NOI18N
			all.add("/usr/i586-suse-linux/include"); // NOI18N
			all.add("/usr/include"); // NOI18N
		} else {
                    // NB: want any fake value but not for suite.sh which is run with dumpPPState
                    if (!dumpPPState) {
                        all.add("/usr/non-exists"); // NOI18N
                    }
                }
		return new ArrayList(all);
	}
	
	private List getSysMacros() {
		Set all = new HashSet();
                if (DISABLE_PREDEFINED) {
                    return Collections.EMPTY_LIST;
                }
		if (CPP_DEFINE) {
                    if ((Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0) {
                        // Solaris 10, amd x86
                        // gcc -x c++ -dM -E /dev/null | awk '{print "all.add(\"" $2 "=" $3 $4 $5 $6 $7 "\");" }'
                        all.add("__DBL_MIN_EXP__=(-1021)"); // NOI18N
                        all.add("__EXTENSIONS__=1"); // NOI18N
                        all.add("__FLT_MIN__=1.17549435e-38F"); // NOI18N
                        all.add("__CHAR_BIT__=8"); // NOI18N
                        all.add("_XOPEN_SOURCE=500"); // NOI18N
                        all.add("__WCHAR_MAX__=2147483647"); // NOI18N
                        all.add("__DBL_DENORM_MIN__=4.9406564584124654e-324"); // NOI18N
                        all.add("__FLT_EVAL_METHOD__=2"); // NOI18N
                        all.add("__DBL_MIN_10_EXP__=(-307)"); // NOI18N
                        all.add("__FINITE_MATH_ONLY__=0"); // NOI18N
                        all.add("__GNUC_PATCHLEVEL__=3"); // NOI18N
                        all.add("__SHRT_MAX__=32767"); // NOI18N
                        all.add("__LDBL_MAX__=1.18973149535723176502e+4932L"); // NOI18N
                        all.add("__unix=1"); // NOI18N
                        all.add("__LDBL_MAX_EXP__=16384"); // NOI18N
                        all.add("__SCHAR_MAX__=127"); // NOI18N
                        all.add("__USER_LABEL_PREFIX__="); // NOI18N
                        all.add("__STDC_HOSTED__=1"); // NOI18N
                        all.add("_LARGEFILE64_SOURCE=1"); // NOI18N
                        all.add("__LDBL_HAS_INFINITY__=1"); // NOI18N
                        all.add("__DBL_DIG__=15"); // NOI18N
                        all.add("__FLT_EPSILON__=1.19209290e-7F"); // NOI18N
                        all.add("__GXX_WEAK__=1"); // NOI18N
                        all.add("__LDBL_MIN__=3.36210314311209350626e-4932L"); // NOI18N
                        all.add("__unix__=1"); // NOI18N
                        all.add("__DECIMAL_DIG__=21"); // NOI18N
                        all.add("_LARGEFILE_SOURCE=1"); // NOI18N
                        all.add("__LDBL_HAS_QUIET_NAN__=1"); // NOI18N
                        all.add("__GNUC__=3"); // NOI18N
                        all.add("__DBL_MAX__=1.7976931348623157e+308"); // NOI18N
                        all.add("__DBL_HAS_INFINITY__=1"); // NOI18N
                        all.add("__SVR4=1"); // NOI18N
                        all.add("__cplusplus=1"); // NOI18N
                        all.add("__DEPRECATED=1"); // NOI18N
                        all.add("__DBL_MAX_EXP__=1024"); // NOI18N
                        all.add("__GNUG__=3"); // NOI18N
                        all.add("__LONG_LONG_MAX__=9223372036854775807LL"); // NOI18N
                        all.add("__GXX_ABI_VERSION=1002"); // NOI18N
                        all.add("__FLT_MIN_EXP__=(-125)"); // NOI18N
                        all.add("__DBL_MIN__=2.2250738585072014e-308"); // NOI18N
                        all.add("__FLT_MIN_10_EXP__=(-37)"); // NOI18N
                        all.add("__DBL_HAS_QUIET_NAN__=1"); // NOI18N
                        all.add("__tune_i386__=1"); // NOI18N
                        all.add("__sun=1"); // NOI18N
                        all.add("__REGISTER_PREFIX__="); // NOI18N
                        all.add("__NO_INLINE__=1"); // NOI18N
                        all.add("__i386=1"); // NOI18N
                        all.add("__FLT_MANT_DIG__=24"); // NOI18N
                        all.add("__VERSION__=\"3.4.3(csl-sol210-3_4-branch+sol_rpath)\""); // NOI18N
                        all.add("i386=1"); // NOI18N
                        all.add("sun=1"); // NOI18N
                        all.add("unix=1"); // NOI18N
                        all.add("__i386__=1"); // NOI18N
                        all.add("__SIZE_TYPE__=unsignedint"); // NOI18N
                        all.add("__ELF__=1"); // NOI18N
                        all.add("__FLT_RADIX__=2"); // NOI18N
                        all.add("__LDBL_EPSILON__=1.08420217248550443401e-19L"); // NOI18N
                        all.add("__FLT_HAS_QUIET_NAN__=1"); // NOI18N
                        all.add("__FLT_MAX_10_EXP__=38"); // NOI18N
                        all.add("__LONG_MAX__=2147483647L"); // NOI18N
                        all.add("__FLT_HAS_INFINITY__=1"); // NOI18N
                        all.add("__PRAGMA_REDEFINE_EXTNAME=1"); // NOI18N
                        all.add("__EXCEPTIONS=1"); // NOI18N
                        all.add("__LDBL_MANT_DIG__=64"); // NOI18N
                        all.add("__WCHAR_TYPE__=longint"); // NOI18N
                        all.add("__FLT_DIG__=6"); // NOI18N
                        all.add("__INT_MAX__=2147483647"); // NOI18N
                        all.add("__FLT_MAX_EXP__=128"); // NOI18N
                        all.add("__DBL_MANT_DIG__=53"); // NOI18N
                        all.add("__WINT_TYPE__=longint"); // NOI18N
                        all.add("__LDBL_MIN_EXP__=(-16381)"); // NOI18N
                        all.add("__LDBL_MAX_10_EXP__=4932"); // NOI18N
                        all.add("__DBL_EPSILON__=2.2204460492503131e-16"); // NOI18N
                        all.add("__sun__=1"); // NOI18N
                        all.add("__svr4__=1"); // NOI18N
                        all.add("__FLT_DENORM_MIN__=1.40129846e-45F"); // NOI18N
                        all.add("__FLT_MAX__=3.40282347e+38F"); // NOI18N
                        all.add("__GNUC_MINOR__=4"); // NOI18N
                        all.add("__DBL_MAX_10_EXP__=308"); // NOI18N
                        all.add("__LDBL_DENORM_MIN__=3.64519953188247460253e-4951L"); // NOI18N
                        all.add("__PTRDIFF_TYPE__=int"); // NOI18N
                        all.add("__LDBL_MIN_10_EXP__=(-4931)"); // NOI18N
                        all.add("__LDBL_DIG__=18"); // NOI18N
                    } else {
			// add generated by gcc 3.3.4 on SuSe 9.2
			// #gcc -x c++ -dM -E - < /dev/null
			all.add("__CHAR_BIT__=8"); // NOI18N
			all.add("__cplusplus=1"); // NOI18N
			all.add("__DBL_DENORM_MIN__=4.9406564584124654e-324"); // NOI18N
			all.add("__DBL_DIG__=15"); // NOI18N
			all.add("__DBL_EPSILON__=2.2204460492503131e-16"); // NOI18N
			all.add("__DBL_MANT_DIG__=53"); // NOI18N
			all.add("__DBL_MAX_10_EXP__=308"); // NOI18N
			all.add("__DBL_MAX__=1.7976931348623157e+308"); // NOI18N
			all.add("__DBL_MAX_EXP__=1024"); // NOI18N
			all.add("__DBL_MIN_10_EXP__=(-307)"); // NOI18N
			all.add("__DBL_MIN__=2.2250738585072014e-308"); // NOI18N
			all.add("__DBL_MIN_EXP__=(-1021)"); // NOI18N
			all.add("__DECIMAL_DIG__=21"); // NOI18N
			all.add("__DEPRECATED=1"); // NOI18N
			all.add("__ELF__=1"); // NOI18N
			all.add("__EXCEPTIONS=1"); // NOI18N
			all.add("__FINITE_MATH_ONLY__=0"); // NOI18N
			all.add("__FLT_DENORM_MIN__=1.40129846e-45F"); // NOI18N
			all.add("__FLT_DIG__=6"); // NOI18N
			all.add("__FLT_EPSILON__=1.19209290e-7F"); // NOI18N
			all.add("__FLT_EVAL_METHOD__=2"); // NOI18N
			all.add("__FLT_MANT_DIG__=24"); // NOI18N
			all.add("__FLT_MAX_10_EXP__=38"); // NOI18N
			all.add("__FLT_MAX__=3.40282347e+38F"); // NOI18N
			all.add("__FLT_MAX_EXP__=128"); // NOI18N
			all.add("__FLT_MIN_10_EXP__=(-37)"); // NOI18N
			all.add("__FLT_MIN__=1.17549435e-38F"); // NOI18N
			all.add("__FLT_MIN_EXP__=(-125)"); // NOI18N
			all.add("__FLT_RADIX__=2"); // NOI18N
			all.add("__GNUC__=3"); // NOI18N
			all.add("__GNUC_MINOR__=3"); // NOI18N
			all.add("__GNUC_PATCHLEVEL__=4"); // NOI18N
			all.add("__GNUG__=3"); // NOI18N
			all.add("__gnu_linux__=1"); // NOI18N
			all.add("_GNU_SOURCE=1"); // NOI18N
			all.add("__GXX_ABI_VERSION=102"); // NOI18N
			all.add("__GXX_WEAK__=1"); // NOI18N
			all.add("__i386=1"); // NOI18N
			all.add("__i386__=1"); // NOI18N
			all.add("i386=1"); // NOI18N
			all.add("__INT_MAX__=2147483647"); // NOI18N
			all.add("__LDBL_DENORM_MIN__=3.64519953188247460253e-4951L"); // NOI18N
			all.add("__LDBL_DIG__=18"); // NOI18N
			all.add("__LDBL_EPSILON__=1.08420217248550443401e-19L"); // NOI18N
			all.add("__LDBL_MANT_DIG__=64"); // NOI18N
			all.add("__LDBL_MAX_10_EXP__=4932"); // NOI18N
			all.add("__LDBL_MAX__=1.18973149535723176502e+4932L"); // NOI18N
			all.add("__LDBL_MAX_EXP__=16384"); // NOI18N
			all.add("__LDBL_MIN_10_EXP__=(-4931)"); // NOI18N
			all.add("__LDBL_MIN__=3.36210314311209350626e-4932L"); // NOI18N
			all.add("__LDBL_MIN_EXP__=(-16381)"); // NOI18N
			all.add("__linux=1"); // NOI18N
			all.add("__linux__=1"); // NOI18N
			all.add("linux=1"); // NOI18N
			all.add("__LONG_LONG_MAX__=9223372036854775807LL"); // NOI18N
			all.add("__LONG_MAX__=2147483647L"); // NOI18N
			all.add("__NO_INLINE__=1"); // NOI18N
			all.add("__PTRDIFF_TYPE__=int"); // NOI18N
			all.add("__REGISTER_PREFIX__"); // NOI18N
			all.add("__SCHAR_MAX__=127"); // NOI18N
			all.add("__SHRT_MAX__=32767"); // NOI18N
			all.add("__SIZE_TYPE__=unsigned int"); // NOI18N
			all.add("__STDC_HOSTED__=1"); // NOI18N
			all.add("__tune_i586__=1"); // NOI18N
			all.add("__tune_pentium__=1"); // NOI18N
			all.add("__unix=1"); // NOI18N
			all.add("__unix__=1"); // NOI18N
			all.add("unix=1"); // NOI18N
			all.add("__USER_LABEL_PREFIX__"); // NOI18N
			all.add("__VERSION__=\"3.3.4 (pre 3.3.5 20040809)\""); // NOI18N
			all.add("__WCHAR_MAX__=2147483647"); // NOI18N
			all.add("__WCHAR_TYPE__=long int"); // NOI18N
			all.add("__WINT_TYPE__=unsigned int"); // NOI18N
                    }
		} else if (C_DEFINE) {
			// add generated by gcc 3.3.4 on SuSe 9.2
			// #gcc -x c -dM -E - < /dev/null
			all.add("__DBL_MIN_EXP__=(-1021)"); // NOI18N
			all.add("__FLT_MIN__=1.17549435e-38F"); // NOI18N
			all.add("__CHAR_BIT__=8"); // NOI18N
			all.add("__WCHAR_MAX__=2147483647"); // NOI18N
			all.add("__DBL_DENORM_MIN__=4.9406564584124654e-324"); // NOI18N
			all.add("__FLT_EVAL_METHOD__=2"); // NOI18N
			all.add("__unix__=1"); // NOI18N
			all.add("unix=1"); // NOI18N
			all.add("__i386__=1"); // NOI18N
			all.add("__SIZE_TYPE__=unsigned=int"); // NOI18N
			all.add("__ELF__=1"); // NOI18N
			all.add("__DBL_MIN_10_EXP__=(-307)"); // NOI18N
			all.add("__FINITE_MATH_ONLY__=0"); // NOI18N
			all.add("__GNUC_PATCHLEVEL__=4"); // NOI18N
			all.add("__FLT_RADIX__=2"); // NOI18N
			all.add("__LDBL_EPSILON__=1.08420217248550443401e-19L"); // NOI18N
			all.add("__SHRT_MAX__=32767"); // NOI18N
			all.add("__LDBL_MAX__=1.18973149535723176502e+4932L"); // NOI18N
			all.add("__linux=1"); // NOI18N
			all.add("__unix=1"); // NOI18N
			all.add("__LDBL_MAX_EXP__=16384"); // NOI18N
			all.add("__LONG_MAX__=2147483647L"); // NOI18N
			all.add("__linux__=1"); // NOI18N
			all.add("__SCHAR_MAX__=127"); // NOI18N
			all.add("__DBL_DIG__=15"); // NOI18N
			all.add("__USER_LABEL_PREFIX__"); // NOI18N
			all.add("linux=1"); // NOI18N
			all.add("__tune_pentium__=1"); // NOI18N
			all.add("__STDC_HOSTED__=1"); // NOI18N
			all.add("__LDBL_MANT_DIG__=64"); // NOI18N
			all.add("__FLT_EPSILON__=1.19209290e-7F"); // NOI18N
			all.add("__LDBL_MIN__=3.36210314311209350626e-4932L"); // NOI18N
			all.add("__WCHAR_TYPE__=long int"); // NOI18N
			all.add("__FLT_DIG__=6"); // NOI18N
			all.add("__FLT_MAX_10_EXP__=38"); // NOI18N
			all.add("__INT_MAX__=2147483647"); // NOI18N
			all.add("__gnu_linux__=1"); // NOI18N
			all.add("__FLT_MAX_EXP__=128"); // NOI18N
			all.add("__DECIMAL_DIG__=21"); // NOI18N
			all.add("__DBL_MANT_DIG__=53"); // NOI18N
			all.add("__WINT_TYPE__=unsigned int"); // NOI18N
			all.add("__GNUC__=3"); // NOI18N
			all.add("__LDBL_MIN_EXP__=(-16381)"); // NOI18N
			all.add("__tune_i586__=1"); // NOI18N
			all.add("__LDBL_MAX_10_EXP__=4932"); // NOI18N
			all.add("__DBL_EPSILON__=2.2204460492503131e-16"); // NOI18N
			all.add("__DBL_MAX__=1.7976931348623157e+308"); // NOI18N
			all.add("__DBL_MAX_EXP__=1024"); // NOI18N
			all.add("__FLT_DENORM_MIN__=1.40129846e-45F"); // NOI18N
			all.add("__LONG_LONG_MAX__=9223372036854775807LL"); // NOI18N
			all.add("__FLT_MAX__=3.40282347e+38F"); // NOI18N
			all.add("__GXX_ABI_VERSION=102"); // NOI18N
			all.add("__FLT_MIN_10_EXP__=(-37)"); // NOI18N
			all.add("__FLT_MIN_EXP__=(-125)"); // NOI18N
			all.add("i386=1"); // NOI18N
			all.add("__GNUC_MINOR__=3"); // NOI18N
			all.add("__DBL_MAX_10_EXP__=308"); // NOI18N
			all.add("__LDBL_DENORM_MIN__=3.64519953188247460253e-4951L"); // NOI18N
			all.add("__DBL_MIN__=2.2250738585072014e-308"); // NOI18N
			all.add("__PTRDIFF_TYPE__=int"); // NOI18N
			all.add("__LDBL_MIN_10_EXP__=(-4931)"); // NOI18N
			all.add("__REGISTER_PREFIX__"); // NOI18N
			all.add("__LDBL_DIG__=18"); // NOI18N
			all.add("__NO_INLINE__=1"); // NOI18N
			all.add("__i386=1"); // NOI18N
			all.add("__FLT_MANT_DIG__=24"); // NOI18N
			all.add("__VERSION__=\"3.3.4 (pre 3.3.5 20040809)\""); // NOI18N
		} else {
                    if (!dumpPPState) {
                        // NB: want any fake value but not for suite.sh which is run with dumpPPState
                        all.add("NO_DEFAULT_DEFINED_SYSTEM_MACROS"); // NOI18N
                    }
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
	
	private long testAPTLexer(File file, boolean printTokens)
			throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
		print("Testing APT lexer:"); // NOI18N
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
			if( isShowTime() ) {
				print("APT Lexing " + file.getName() + " took " + time + " ms"); // NOI18N
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
                APTPreprocHandler ppHandler = APTHandlersSupport.createPreprocHandler(getMacroMap(file), getIncludeHandler(file), true);
		APTWalkerTest walker = new APTWalkerTest(apt, ppHandler);
		walker.visit();
		time = System.currentTimeMillis() - time;
		
		if( isShowTime() ) {
			print("Visiting APT "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms"); // NOI18N
			print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms"); // NOI18N
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
                APTPreprocHandler ppHandler = APTHandlersSupport.createPreprocHandler(macroMap, getIncludeHandler(file), true);
		APTWalkerTest walker = new APTWalkerTest(apt, ppHandler);
		TokenStream ts = walker.getTokenStream();
		if (expand) {
			ts = new APTMacroExpandedStream(ts, macroMap);
		}
		if (filter) {
			ts = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP).getFilteredStream(new APTCommentsFilter(ts));
		}
                int lastLine = -1;
		for(Token t = ts.nextToken() ; !APTUtils.isEOF(t); t = ts.nextToken() ) {
			if( printTokens) {
				print(" " + t.getText(), t.getLine() != lastLine); // NOI18N
			}
                        lastLine = t.getLine();
		}
                if (printTokens && lastLine > 0) {
                    print("", true);
                }
		time = System.currentTimeMillis() - time;
		
		if( isShowTime() ) {
			print("Getting" + (expand?" expanded":"") + (filter?" filtered":"") + " APT token stream "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms"); // NOI18N
			print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms"); // NOI18N
		}
		return time;
	}
	
	private long testAPTParser(FileBuffer buffer, boolean cleanAPT) throws IOException, RecognitionException, TokenStreamException {
		print("Testing APT Parser"); // NOI18N
		int flags = CPPParserEx.CPP_CPLUSPLUS;
		File file = buffer.getFile();
		long time = System.currentTimeMillis();
		if (cleanAPT) {
			invalidateAPT(buffer);
			time = System.currentTimeMillis();
		}
		FileImpl fileImpl = null;
		APTPreprocHandler preprocHandler = getPreprocHandler(file);
                APTPreprocHandler.State state = preprocHandler.getState();
		fileImpl = (FileImpl) getProject().testAPTParseFile(file.getAbsolutePath(), preprocHandler);
		try {
			fileImpl.scheduleParsing(true, state);
		} catch( InterruptedException e ) {
			// nothing to do
			print("Interrupted parsing"); // NOI18N
		}
		time = System.currentTimeMillis() - time;
		
		if( isShowTime() ) {
			print("Parsing" + (cleanAPT ? " with cleaning APT in driver":"") + " took " + time + " ms"); // NOI18N
		}
		return time;
	}
	
	private void testAPT(File file)
			throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
		FileBuffer buffer = new FileBufferFile(file);
		print("Testing APT:" + file); // NOI18N
		long minLexer = Long.MAX_VALUE, maxLexer = Long.MIN_VALUE;
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
		long minAPTParsing = Long.MAX_VALUE, maxAPTParsing = Long.MIN_VALUE;
		if (testAPTParser) {
			for (int i = -1; i < APT_REPEAT_TEST; i++) {
				long val = testAPTParser(buffer, cleanAPT);
				minAPTParsing = Math.min(minAPTParsing, val);
				maxAPTParsing = Math.max(maxAPTParsing, val);
			}
		}
		if( isShowTime() ) {
			print("APT BEST/WORST results for " + file.getAbsolutePath()); // NOI18N
			if (minLexer != Long.MAX_VALUE) {
				print(minLexer  + " ms BEST Plain lexer"); // NOI18N
				print(maxLexer  + " ms WORST Plain lexer"); // NOI18N
			}
			if (minAPTLexer != Long.MAX_VALUE) {
				print(minAPTLexer + " ms BEST APT lexer"); // NOI18N
				print(maxAPTLexer + " ms WORST APT lexer"); // NOI18N
			}
			if (minDriver != Long.MAX_VALUE) {
				print(minDriver + " ms BEST Building APT:"); // NOI18N
				print(maxDriver + " ms WORST Building APT:"); // NOI18N
			}
			
			if (minVisit != Long.MAX_VALUE) {
				print(minVisit + " ms BEST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
				print(maxVisit + " ms WORST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
			}
			if (minGetTS != Long.MAX_VALUE) {
				print(minGetTS + " ms BEST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
				print(maxGetTS + " ms WORST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
			}
			if (minGetExpandedTS != Long.MAX_VALUE) {
				print(minGetExpandedTS + " ms BEST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
				print(maxGetExpandedTS + " ms WORST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
			}
			if (minGetFilteredTS != Long.MAX_VALUE) {
				print(minGetFilteredTS + " ms BEST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
				print(maxGetFilteredTS + " ms WORST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
			}
			if (minParsing != Long.MAX_VALUE) {
				print(minParsing + " ms BEST Plaing Parsing"); // NOI18N
				print(maxParsing + " ms WORST Plaing Parsing"); // NOI18N
			}
			if (minAPTParsing != Long.MAX_VALUE) {
				print(minAPTParsing + " ms BEST APT parsing" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
				print(maxAPTParsing + " ms WORST APT parsing" + (cleanAPT ? " with cleaning APT in driver:":":")); // NOI18N
			}
		}
	}
	
	private static String firstFile = null;
	private void invalidateAPT(final FileBuffer buffer) {
		File file = buffer.getFile();
		if (firstFile == null || firstFile.equalsIgnoreCase(file.getAbsolutePath())) {
			firstFile = file.getAbsolutePath();
			APTDriver.getInstance().invalidateAll();
			getProject().invalidateFiles();
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
		if( isShowTime() ) {
			minDriver = Math.min(minDriver, time);
			maxDriver = Math.max(maxDriver, time);
			print("Building APT for " + file.getName() + "\n SIZE OF FILE:" + file.length()/1024 + "Kb\n TIME: took " + time + " ms\n MEMORY: changed from " + // NOI18N
					(oldMem)/(1024) + " to " + newMem/(1024)+ "[" + (newMem-oldMem)/1024 + "]Kb"); // NOI18N
		}
		
		//        System.out.println("apt tree: \n" + APTTraceUtils.toStringList(apt));
		if (buildXML) {
			File outDir = new File("/tmp/aptout/"); // NOI18N
			outDir.mkdirs();
			File outFile = new File(outDir, file.getName()+".xml"); // NOI18N
			if (outFile.exists()) {
				outFile.delete();
			}
			outFile.createNewFile();
			Writer out = new BufferedWriter(new FileWriter(outFile));
			APTTraceUtils.xmlSerialize(apt, out);
			out.flush();
			APT light = APTBuilder.buildAPTLight(apt);
			File outFileLW = new File(outDir, file.getName()+"_lw.xml"); // NOI18N
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
	
        /*package*/ void test(File file, PrintStream out, PrintStream err) throws Exception {
            tracer.setPrintStream(out);
            addFile(fileList, file);
            doTest();
        }
        
	private TestResult test(File file)
			throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
		
		TestResult result = new TestResult();
		
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
			print("AST read; time: " + t2 + " ms"); // NOI18N
		}
		
		AST tree = null;
		FileImpl fileImpl = null;
		int errCount = 0;
		
		APTPreprocHandler preprocHandler = getPreprocHandler(file);
                APTPreprocHandler.State state = preprocHandler.getState();
		fileImpl = (FileImpl) getProject().testAPTParseFile(file.getAbsolutePath(), preprocHandler);
		try {
			fileImpl.scheduleParsing(true, state);
			if( preprocHandler != null ) { // i.e. if TraceFlags.USE_APT
				preprocHandler.setState(fileImpl.testGetPreprocState());
			}
			fileImpl.setPreprocHandler(null);
			waitProjectParsed(false);
			if( dumpAst || writeAst || showAstWindow ) {
			    tree = fileImpl.parse(getPreprocHandler(file));
			}
		} catch( InterruptedException e ) {
			// nothing to do
		}
		errCount = fileImpl.getErrorCount();
		if ( dumpPPState ) {
			dumpMacroMap(preprocHandler.getMacroMap());
		}
		time = System.currentTimeMillis() - time;
		if( isShowTime() ) {
			result.setTime(time);
			result.setLineCount(countLines(fileImpl));
			if( ! quiet ) {
				print("Processing " + file.getName() + " took " + time + " ms; LPS=" + result.getLPS() + "; error count: " + errCount); // NOI18N
			}
		}
		
		if (dumpStatistics) {
			if (this.dumpDir != null) {
				String postfix = statPostfix;
				if (Diagnostic.getStatisticsLevel() > 1) {
					postfix += "."+Diagnostic.getStatisticsLevel(); // NOI18N
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
			System.out.println("AST DUMP for file " + file.getName()); // NOI18N
			dumpAst(tree);
		}
		
		if( writeAst && tree != null ) {
			long t2 = System.currentTimeMillis();
			t2 = System.currentTimeMillis() - t2;
			cache.writeAst(tree, file);
			print("AST stored; time: " + t2 + " ms"); // NOI18N
		}
		
                if ( doCleanRepository ) {
		    CsmProject project = fileImpl.getProject();
		    String absPath = fileImpl.getAbsolutePath();
		    fileImpl = null;
                    RepositoryAccessor.getRepository().debugClear();
		    fileImpl = (FileImpl) project.findFile(absPath);
                }
                
		if( dumpModel ) {
			if( fileImpl != null ) {
                                tracer.setDeep(deep);
				tracer.setTestUniqueName(testUniqueName);
				tracer.dumpModel(fileImpl);
				if (!dumpFileOnly) {
					tracer.dumpModel(getProject());
				}
			} else {
				print("FileImpl is null - not possible to dump File Model"); // NOI18N
			}
		}
		
		if( showAstWindow  ) {
			test(tree, file.getName());
		}
		
		return result;
	}
	
	private boolean hasNonEmptyIncludes(CsmFile fileImpl) {
		for (Iterator<CsmInclude> it = fileImpl.getIncludes().iterator(); it.hasNext();) {
			CsmInclude inc = it.next();
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
	
	public static void dumpAst(AST ast) {
		ASTVisitor visitor = new ASTVisitor() {
			public void visit(AST node) {
				for( AST node2 = node; node2 != null; node2 = node2.getNextSibling() ) {
					String ofStr = (node2 instanceof CsmAST) ? (" offset=" + ((CsmAST) node2).getOffset() + " file = " + ((CsmAST)node2).getFilename()) : ""; // NOI18N
					print("" + node2.getText() + " [" +  node2.getType() + "] " + node2.getLine() + ':' + node2.getColumn() + ofStr); // NOI18N
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
	
	private void dumpMacroMap(APTMacroMap macroMap) {
		tracer.print("State of macro map:"); // NOI18N
		tracer.print(macroMap == null ? "empty macro map" : macroMap.toString()); // NOI18N
	}
	
	private void testLibProject() {
		LibProjectImpl libProject = LibProjectImpl.createInstance(model, "/usr/include"); // NOI18N
		model.addProject(libProject);
		tracer.dumpModel(libProject);
	}
	
	private static void print(String s) {
		tracer.print(s);
	}
	
	private void print(String s, boolean newLine) {
		tracer.print(s, newLine);
	}
        
	private static void indent() {
		tracer.indent();
	}
	
	private static void unindent() {
		tracer.unindent();
	}
	
	private int countUserFiles() {
		return getProject().getAllFiles().size();
	}
	
	private int countSystemHeaders() {
		int cnt = 0;
		Set processedProjects = new HashSet();
		for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
			cnt += countFiles((ProjectBase) it.next(), processedProjects);
		}
		return cnt;
	}
	
	private int countFiles(ProjectBase prj, Collection processedProjects) {
		if( processedProjects.contains(prj) ) {
			return 0;	// already counted
		}
		int cnt = prj.getAllFiles().size();
		for (Iterator it = prj.getLibraries().iterator(); it.hasNext();) {
			cnt += countFiles((ProjectBase) it.next(), processedProjects);
		}
		return cnt;
	}
	
	private void calculateAverageLPS(TestResult total, boolean includeLibs) {
		total.lineCount = 0;
		for (Iterator it = getProject().getAllFiles().iterator(); it.hasNext();) {
			CsmFile file = (CsmFile) it.next();
			total.lineCount += countLines(file, true);
		}
		if( includeLibs ) {
			for (Iterator it1 = getProject().getLibraries().iterator(); it1.hasNext();) {
				ProjectBase lib = (ProjectBase) it1.next();
				for (Iterator it2 = lib.getAllFiles().iterator(); it2.hasNext();) {
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
                FoldingParser p = (FoldingParser)Lookup.getDefault().lookup(FoldingParser.class);
                if (p != null) {
                    List<CppFoldRecord> folds = p.parse(file.getAbsolutePath(), reader);
                    try {
                            reader.close();
                    } catch (IOException ex) {
                            ex.printStackTrace();
                    }
                    printFolds(file.getAbsolutePath(), folds);
                } else {
                    System.out.println("No Folding Provider"); // NOI18N
                }
	}
	
	private void printFolds(String file, List<CppFoldRecord> folds) {
		Collections.sort(folds, FOLD_COMPARATOR);
		System.out.println("Foldings of the file " + file); // NOI18N
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

    /*package*/ ProjectBase getProject() {
        ProjectBase project;
        if (TraceFlags.USE_REPOSITORY) {
            project = projectUID == null ? null : (ProjectBase)projectUID.getObject();
        } else {
            project = this.project;
        }
        return project;
    }

    private void setProject(ProjectBase project) {
        if (TraceFlags.USE_REPOSITORY) {
            projectUID = project == null ? null : project.getUID();
        } else {
            this.project = project;
        }
    }
    
    /*package*/final CsmModel getModel() {
        return model;
    }

    boolean isShowTime() {
        return showTime;
    }
}
