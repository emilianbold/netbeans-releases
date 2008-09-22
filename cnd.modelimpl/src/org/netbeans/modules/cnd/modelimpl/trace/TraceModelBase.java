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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 * A class that is able to
 * - create CsmModel
 * - create NativeProject and CsmProject
 *   according to parameters (file names, -I and -D options)
 * @author Vladimir Kvashin
 */
public class TraceModelBase {

    private boolean useCSysIncludes = Boolean.getBoolean("cnd.modelimpl.c.include"); //NOI18N
    private boolean useCSysDefines = Boolean.getBoolean("cnd.modelimpl.c.define"); //NOI18N
    private boolean useCppSysIncludes = Boolean.getBoolean("cnd.modelimpl.cpp.include"); //NOI18N
    private boolean useCppSysDefines = Boolean.getBoolean("cnd.modelimpl.cpp.define"); //NOI18N
    
    private ModelImpl model;

    private CsmUID<CsmProject> projectUID;

    private List<String> quoteIncludePaths = new ArrayList<String>();
    private List<String> systemIncludePaths = new ArrayList<String>();
    private List<File> files = new ArrayList<File>();
    private List<String> currentIncludePaths = null;

    private List<String> macros = new ArrayList<String>();

    // if true, then relative include paths oin -I option are considered
    // to be based on the file that we currently compile rather then current dir
    private boolean pathsRelCurFile = false;
    
    public TraceModelBase() {
        Logger openideLogger = Logger.getLogger("org.openide.loaders"); // NOI18N
        // reduce log level to prevent unnecessary messages in tests
        openideLogger.setLevel(Level.SEVERE);
	RepositoryUtils.cleanCashes();
	model = (ModelImpl) CsmModelAccessor.getModel(); // new ModelImpl(true);
	if (model == null) {
	    model = new ModelImpl();
        }
	model.startup();
	currentIncludePaths = quoteIncludePaths;
    }
    
    protected final void setIncludePaths(List<String> sysIncludes, List<String> usrIncludes) {
	this.quoteIncludePaths = usrIncludes;
	this.systemIncludePaths = sysIncludes;
	this.currentIncludePaths = this.quoteIncludePaths;
    }

    protected final void shutdown() {
        waitModelTasks();
	model.shutdown();
        waitModelTasks();
	RepositoryUtils.cleanCashes();
    }

    private void waitModelTasks(){
        Cancellable task = model.enqueueModelTask(new Runnable(){
            public void run() {
            }
        }, "wait finished other tasks"); //NOI18N
        if (task instanceof Task) {
            ((Task)task).waitFinished();
        }
    }
    
    public void processArguments(final String... args) {
	for (int i = 0; i < args.length; i++) {
	    if (args[i].startsWith("--")) { // NOI18N
		// NOI18N
		processFlag(args[i].substring(2));
	    } else if (args[i].startsWith("-")) { // NOI18N
		// NOI18N
		for (int charIdx = 1; charIdx < args[i].length(); charIdx++) {
		    ProcessFlagResult res = processFlag(args[i].charAt(charIdx), args[i].substring(charIdx + 1));
		    if (res == ProcessFlagResult.ALL_PROCESSED) {
			break;
		    }
		}
	    } else {
		addFile(files, new File(args[i]));
	    }
	}
    }
    
    protected enum ProcessFlagResult {
	NONE_PROCESSED,
	CHAR_PROCESSED,
	ALL_PROCESSED
    }
    
    protected ProcessFlagResult processFlag(char flag, String argRest) {
	// it's easier to set the most "popular" return value here and NONE_... in default case
	ProcessFlagResult result = ProcessFlagResult.CHAR_PROCESSED;
	switch( flag ) {
	    // TODO: support not only "-Idir" but "-I dir" as well
	    // TODO: support -iquote. Now I disabled this since the contract reads:
	    // -iquote<dir> Add the directory dir to the head of the list of directories to be searched for header files
	    // ONLY for the case of #include "file"; they are NOT searched for #include <file>
	    case 'I':   
		    if (argRest.length() > 0) {
			if (argRest.charAt(0) == '-') {
			    // switch following include paths destination list
			    currentIncludePaths = (currentIncludePaths == quoteIncludePaths) ? systemIncludePaths : quoteIncludePaths;
			    argRest = argRest.substring(1);
			}
			String includePath = argRest;
			currentIncludePaths.add(includePath);
			result = ProcessFlagResult.ALL_PROCESSED;
		    }
	    break;
	    case 'D':   macros.add(argRest); result = ProcessFlagResult.ALL_PROCESSED; break;
	    default:	result = ProcessFlagResult.NONE_PROCESSED;
	}
	return result;
    }

    /** 
     * Processes a string flag
     * @return true if the flag has been recognised and processed,
     * otherwise false
     */
    protected boolean processFlag(String flag) {
	if ("relpath".equals(flag)) { // NOI18N
	    // NOI18N
	    pathsRelCurFile = true;
	    return true;
	}
	return false;
    }
    
    private void addFile(List<File> files, File file) {
	if (file.isDirectory()) {
	    String[] list = file.list();
	    for (int i = 0; i < list.length; i++) {
		addFile(files, new File(file, list[i]));
	    }
	} else {
	    files.add(file);
	}
    }

    public ProjectBase getProject() {
        synchronized( this ) {
            if( projectUID == null ) {
                projectUID = createProject().getUID();
            }
        }
        return (projectUID == null) ? null : (ProjectBase) projectUID.getObject();
    }
    
    public static void closeProject(CsmProject project) {
	closeProject(project, false);
    }
 
    public static void closeProject(CsmProject project, boolean cleanRepository) {
        Object platformProject = project.getPlatformProject();
        ((ModelImpl) CsmModelAccessor.getModel()).closeProject(platformProject, cleanRepository);
    }
    
    public void resetProject() {
	resetProject(false);
    }
    
    public void resetProject(boolean cleanRepository) {
	ProjectBase aProject = getProject();
	if (aProject != null) {
	    closeProject(aProject, cleanRepository);
	}
	projectUID = null;
	//getProject();
    }
    
    
    private ProjectBase createProject() {
        NativeProject np = null;
        if (files.size() == 1 && files.get(0).getName().equals("project.xml")) { // NOI18N
            try {
                FileObject projectDir = FileUtil.toFileObject(
                        files.get(0).getParentFile().getParentFile());
                Project p = ProjectManager.getDefault().findProject(projectDir);
                np = p.getLookup().lookup(NativeProject.class);
            } catch (IOException ioe) {
                throw new IllegalArgumentException(ioe);
            }
        } else {
            np = NativeProjectProvider.createProject("DummyProject", files, // NOI18N
                    getSystemIncludes(), quoteIncludePaths, getSysMacros(),
                    macros, pathsRelCurFile);
        }
        return model.addProject(np, np.getProjectDisplayName(), true);
    }

    protected List<String> getSystemIncludes() {
	Set<String> all = new HashSet<String>(systemIncludePaths);
	if (useCppSysIncludes) {
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
	} else if (useCSysIncludes) {
	    // add generated by gcc 3.3.4 on SuSe 9.2
	    // #gcc -x c -v -E - < /dev/null
	    all.add("/usr/local/include"); // NOI18N
	    all.add("/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include"); // NOI18N
	    all.add("/usr/i586-suse-linux/include"); // NOI18N
	    all.add("/usr/include"); // NOI18N
	} 
	return new ArrayList<String>(all);
    }

    protected List<String> getSysMacros() {
	Set<String> all = new HashSet<String>();
	if (useCppSysDefines) {
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
	} else if (useCSysDefines) {
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
	}
	return new ArrayList<String>(all);
    }
    
    public ModelImpl getModel() {
	return model;
    }

    public List<String> getMacros() {
	return Collections.unmodifiableList(macros);
    }

    public boolean isPathsRelCurFile() {
	return pathsRelCurFile;
    }

    public List<File> getFiles() {
	return Collections.unmodifiableList(files);
    }

    public List<String> getQuoteIncludePaths() {
	return Collections.unmodifiableList(quoteIncludePaths);
    }

    public void setUseCSysDefines(boolean set) {
	this.useCSysDefines = set;
    }

    public void setUseCSysIncludes(boolean set) {
	this.useCSysIncludes = set;
    }

    public void setUseCppSysDefines(boolean set) {
	this.useCppSysDefines = set;
    }

    public void setUseCppSysIncludes(boolean set) {
	this.useCppSysIncludes = set;
    }
    
    public void setUseSysPredefined(boolean set) {
	setUseCSysDefines(set);
	setUseCSysIncludes(set);
	setUseCppSysDefines(set);
	setUseCppSysIncludes(set);
    }
        
    protected final void initDataObjects() {
        ProjectBase prj = getProject();
        if (prj != null) {
            Collection<CsmFile> allFiles = prj.getAllFiles();
            for (CsmFile csmFile : allFiles) {
                if (csmFile instanceof FileImpl) {
                    try {
                        FileImpl impl = (FileImpl) csmFile;
                        NativeFileItem item = impl.getNativeFileItem();
                        FileObject fo = item == null ? null : FileUtil.toFileObject(item.getFile());
                        DataObject dobj = fo == null ? null : DataObject.find(fo);
                        NativeProjectProvider.registerItemInDataObject(dobj, item);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
}
