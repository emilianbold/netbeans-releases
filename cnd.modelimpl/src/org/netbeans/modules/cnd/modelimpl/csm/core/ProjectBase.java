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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTPreprocStateImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTSystemStorage;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.utils.APTMacroUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Disposable {
    
    /** Creates a new instance of CsmProjectImpl */
    public ProjectBase(ModelImpl model, Object platformProject, String name) {
        this.model = model;
        this.platformProject = platformProject;
        this.name = name;
        globalNamespace = new NamespaceImpl(this, true);
    }
    
    public CsmNamespace getGlobalNamespace() {
        return globalNamespace;
    }
    
    public String getName() {
        return name;
    }
    
    
    /** Gets an object, which represents correspondent IDE project */
    public Object getPlatformProject() {
        return platformProject;
    }

    /** Finds namespace by its qualified name */
    public CsmNamespace findNamespace( String qualifiedName, boolean findInLibraries ) {
        CsmNamespace result = findNamespace(qualifiedName);
        if( result == null && findInLibraries ) {
            for (Iterator it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = (CsmProject) it.next();
                result = lib.findNamespace(qualifiedName);
                if( result != null ) {
                    break;
                }
            }
        }
        return result;
    }
    
    /** Finds namespace by its qualified name */
    public CsmNamespace findNamespace( String qualifiedName ) {
        CsmNamespace nsp = (CsmNamespace) namespaces.get(qualifiedName);
        return nsp;
    }
    
    public NamespaceImpl findNamespace(NamespaceImpl parent, String name, boolean createIfNotFound) {
        String qualifiedName = (parent == null || parent.isGlobal()) ? name : parent.getQualifiedName() + "::" + name;
        NamespaceImpl nsp = (NamespaceImpl) namespaces.get(qualifiedName);
        if( nsp == null && createIfNotFound ) {
            synchronized (namespaceLock){
                nsp = (NamespaceImpl) namespaces.get(qualifiedName);
                if( nsp == null ) {
                    nsp = new NamespaceImpl(this, parent, name);
                }
            }
        }
        return nsp;
    }
    
    public void registerNamespace(NamespaceImpl namespace) {
        namespaces.put(namespace.getQualifiedName(),  namespace);
    }
    
    public CsmClassifier findClassifier(String qualifiedName, boolean findInLibraries) {
        CsmClassifier result = findClassifier(qualifiedName);
        if( result == null && findInLibraries ) {
            for (Iterator it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = (CsmProject) it.next();
                result = lib.findClassifier(qualifiedName);
                if( result != null ) {
                    break;
                }
            }
        }
        return result;
    }
    
    public CsmClassifier findClassifier(String qualifiedName) {
        CsmClassifier result = (CsmClassifier) classifiers.get(qualifiedName);
        if( result == null ) {
//TODO think over: of the project isn't completely parsed, classifier might not be found
//            waitParse();
            
            
//            result = (CsmCompoundClassifier) classifiers.get(qualifiedName);
//            if( result == null ) {
//                CsmDeclaration decl = (CsmDeclaration) declarations.get(qualifiedName);
//                if( decl != null && decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
//                    CsmTypedef typedef = (CsmTypedef) decl;
//                    CsmType type = typedef.getType();
//                    if( type != null ) {
//                        CsmClassifier classifier = type.getClassifier();
//                        if( classifier instanceof CsmCompoundClassifier ) {
//                            result = (CsmCompoundClassifier) classifier;
//                        }
//                    }
//                }
//            }
        }
        return result;
    }

    public CsmDeclaration findDeclaration(String uniqueName) {
        CsmDeclaration result = (CsmDeclaration) declarations.get(uniqueName);
//TODO think over: of the project isn't completely parsed, declaration might not be found
//        if( result == null ) {
//            waitParse();
//            result = (CsmDeclaration) declarations.get(uniqueName);
//        }
        return result;
    }
    
//    public void registerClassifier(ClassEnumBase ce) {
//        classifiers.put(ce.getQualifiedName(), ce);
//        registerDeclaration(ce);
//    }
    
    public void registerDeclaration(CsmDeclaration decl) {
        
        // don't put unnamed declarations into namespace
        if( decl.getName().length() == 0 ) {
            return;
        }
        declarations.put(decl.getUniqueName(),  decl);
        
	if( decl instanceof CsmClassifier ) {
            String qn = decl.getQualifiedName();
            if (!classifiers.containsKey(qn)){
                classifiers.put(qn, decl);
            }
	}
    }
    
    public void unregisterDeclaration(CsmDeclaration decl) {
        declarations.remove(decl.getUniqueName());
	if( decl instanceof CsmClassifier ) {
	    classifiers.remove(decl.getQualifiedName());
	}
    }

//    public Collection /*<CsmFile>*/ getFiles() {
//        return files;
//    }
    
    
    public void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if( insideParser ) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err);
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        if( insideParser ) {
            return;
        }
        if( ! insideParser && ParserQueue.instance().hasFiles(this, null) ) {
            try {
                synchronized( waitParseLock ) {
                    waitParseLock.wait();
                }
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
    }
    
//    protected synchronized void parseAllIfNeed() {
//        ModelSupport.instance().visitProjectFiles(this, platformProject, new FileVisitor() {
//            public void visit(NativeFileItem file) throws FileVisitor.StopException {
//                createIfNeed(file);
//            }
//        });
//    }

    protected synchronized void ensureChangedFilesEnqueued() {
    }
    
    /**
     * @param skipFile if null => check all files, otherwise skip checking
     * this file 
     *
     */
    protected boolean hasChangedFiles(CsmFile skipFile) {
        return false;
    }
    
    protected synchronized void ensureFilesCreated() {
        if( status ==  Status.Initial ) {
	    try {
		status = Status.AddingFiles;
		ParserQueue.instance().onStartAddingProjectFiles(this);
		ModelSupport.instance().registerProjectListeners(this, platformProject);
		ModelSupport.instance().visitProjectFiles(this, platformProject, new FileVisitor() {
		    public void visit(NativeFileItem file, boolean isSourceFile) throws FileVisitor.StopException {
			if( ProjectBase.this.isProjectDisposed ) {
                            if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ProjevtBase.ensureFilesCreated interrupted");
			    // TODO: remove visitor and this exception-driven flow
			    throw new FileVisitor.StopException();
			}
			createIfNeed(file, isSourceFile);
		    }
		});
		ParserQueue.instance().onEndAddingProjectFiles(this);
	    }
	    finally {
		status = Status.Ready;
	    }
        }
    }
    
    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public void onAddedToModel() {
	if( status == Status.Initial ) {
	    Runnable r = new Runnable() {
		public void run() {
		    ensureFilesCreated();
		}
	    };
            CodeModelRequestProcessor.instance().post(r, "Filling parser queue for " + getName());
	}
    }
    
        
    protected APTPreprocState createDefaultPreprocState(File file) {
        return new APTPreprocStateImpl(new APTFileMacroMap(), new APTIncludeHandlerImpl(), false);
    }
    
    protected APTPreprocState getDefaultPreprocState(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocState preprocState = new APTPreprocStateImpl(macroMap, inclHandler, isSourceFile(nativeFile));
        return preprocState;
    }
    
    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)){
            nativeFile = new DefaultFileItem(nativeFile);
        }
        List userIncludePaths = nativeFile.getUserIncludePaths();
        List sysIncludePaths = nativeFile.getSystemIncludePaths();
        sysIncludePaths = sysAPTData.getIncludes(sysIncludePaths.toString(), sysIncludePaths);
        return new APTIncludeHandlerImpl(sysIncludePaths, userIncludePaths);
    }
    
    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)){
            nativeFile = new DefaultFileItem(nativeFile);
        }
        List/*<String>*/ userMacros = nativeFile.getUserMacroDefinitions();
        List/*<String>*/ sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap sysMap = getSysMacroMap(sysMacros);
        APTFileMacroMap map = new APTFileMacroMap(sysMap);
        APTMacroUtils.fillMacroMap(map, userMacros);
        return map;        
    }

    protected boolean isSourceFile(NativeFileItem nativeFile){
        return nativeFile.getSystemIncludePaths().size()>0;
//        NativeProject prj = nativeFile.getNativeProject();
//        if (prj != null){
//            return prj.getAllSourceFiles().contains(nativeFile);
//        }
//        return false;
    }
    
    private APTMacroMap getSysMacroMap(List/*<String>*/ sysMacros)
    {   
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }
    
    public APTPreprocState getPreprocState(File file) {
        APTPreprocState preprocState = createDefaultPreprocState(file);
        APTPreprocState.State state = getPreprocStateState(file);
        if (state != null) {
            preprocState.setState(state);
        }        
        return preprocState;
    }
    
    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public CsmFile testAPTParseFile(String file, APTPreprocState preprocState) {
        APTPreprocState.State state = (APTPreprocState.State) getPreprocStateState(new File(file));
        boolean firsTime = (state == null);
        if( firsTime ) {           
            // remember the first callback state
            putPreprocStateState(new File(file), preprocState.getState());
        } else {            
            preprocState.setState(state);
        }               
        CsmFile csmFile = findFile(file, FileImpl.UNDEFINED_FILE, preprocState, true);
        return csmFile;        
    }

    protected void putPreprocStateState(File file, APTPreprocState.State state) {
        String path = getFileKey(file);
        filesHandlers.put(path, state);
    }
    
    protected APTPreprocState.State getPreprocStateState(File file) {
        String path = getFileKey(file);      
        return (APTPreprocState.State) filesHandlers.get(path);
    }
    
    public void invalidateFiles() {
        filesHandlers.clear();
        for (Iterator it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.invalidateFiles();
        }
    }
    
    public static final int GATHERING_MACROS    = 0;
    public static final int GATHERING_TOKENS    = 1;
    
    /**
     * called to inform that file was #included from another file with specific preprocState
     * 
     * @param file included file path
     * @param preprocState preprocState with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(String file, APTPreprocState preprocState, int mode) {
        FileImpl csmFile = (FileImpl)findFile(file, FileImpl.HEADER_FILE, preprocState, false);            

        APTPreprocState.State state = updateFileStateIfNeeded(csmFile, preprocState);
        
        // gather macro map from all includes
        try {
            APTFile aptLight = null;
            if (TraceFlags.USE_AST_CACHE) {
                aptLight = CacheManager.getInstance().findAPTLight(csmFile);
            } else {
                aptLight = APTDriver.getInstance().findAPTLight(csmFile.getBuffer());
            }
            APTParseFileWalker walker = new APTParseFileWalker(aptLight, csmFile, preprocState);            
            walker.visit();                
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        
        if (state != null) {
            scheduleIncludedFileParsing(csmFile, state);
        }        
        return csmFile;
    }   
    
//    protected boolean needScheduleParsing(FileImpl file, APTPreprocState preprocState) {
//        APTPreprocState.State curState = (APTPreprocState.State) filesHandlers.get(file);
//        if (curState != null && !curState.isStateCorrect() && preprocState != null && preprocState.isStateCorrect()) {
//            return true;
//        }
//        return !file.isParsingOrParsed() || !TraceFlags.APT_CHECK_GET_STATE ;
//    }
    
    protected APTPreprocState.State updateFileStateIfNeeded(FileImpl csmFile, APTPreprocState preprocState) {
        APTPreprocState.State state = null;
        File file = csmFile.getBuffer().getFile();
        APTPreprocState.State curState = (APTPreprocState.State) getPreprocStateState(file);
        boolean update = false;
        if (curState == null) {
            update = true;
        } else if (!curState.isStateCorrect() && preprocState.isStateCorrect()) {
            update = true;
            // invalidate file
            csmFile.stateChanged(null, true);
        }
        if( update ) {
            state = preprocState.getState();
            putPreprocStateState(file, state);
        }   
        return state;
    }
    
    protected Map/*<String, APTPreprocState.State>*/ filesHandlers = Collections.synchronizedMap(new HashMap(/*<File, APTPreprocState.State>*/));
 
    public ProjectBase resolveFileProject(String absPath) {
        return resolveFileProject(absPath, false);
    }

    public ProjectBase resolveFileProjectOnInclude(String absPath) {
        return resolveFileProject(absPath, true);
    }
    
    protected ProjectBase resolveFileProject(String absPath, boolean onInclude) {
        ProjectBase owner = null;
        // check own files
        if (getFile(new File(absPath)) != null) {
            owner = this;
        } else {
            // else check in libs
            for (Iterator it = getLibraries().iterator(); it.hasNext() && (owner == null);) {
                LibProjectImpl lib = (LibProjectImpl) it.next();
                assert (lib != null);
                owner = lib.resolveFileProject(absPath, false);
                if (owner == null) {
                    Object p = getPlatformProject();
                    if (p instanceof NativeProject){
                        owner = lib.resolveFileProject(absPath, onInclude, ((NativeProject)p).getSystemIncludePaths());
                    }
                }
            }
        }
        // during include phase of parsing process we should help user with project
        // config. If he forgot to add header to project we should add them anyway to not lost
        if (owner == null && onInclude) {
            owner = this;
        }
        return owner;
    }

    protected abstract void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile);
    protected abstract FileImpl findFile(File file, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed);
    public abstract void onFileAdded(NativeFileItem nativeFile);
    public abstract void onFileRemoved(NativeFileItem nativeFile);
    protected abstract void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state);

    public CsmFile findFile(String absolutePath) {
		APTPreprocState preprocState = null;
		if (getPreprocStateState(new File(absolutePath)) == null){
			// Try to find native file
			if (getPlatformProject() instanceof NativeProject){
				NativeProject prj = (NativeProject)getPlatformProject();
				if (prj != null){
					NativeFileItem nativeFile = prj.findFileItem(new File(absolutePath));
		if( nativeFile == null ) {
			nativeFile = new DefaultFileItem(prj, absolutePath);
		}
					preprocState = getDefaultPreprocState(nativeFile);
				}
			}
		}
		return findFile(absolutePath, FileImpl.UNDEFINED_FILE, preprocState, true);
    }

    public FileImpl findFile(String absolutePath, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed) {
        return findFile(new File(absolutePath), fileType, preprocState, scheduleParseIfNeed);
    }
    
    protected Map getFiles() {
        return files;
    }
    
    protected FileImpl getFile(File file) {
        String path = getFileKey(file);
        return (FileImpl) files.get(path);
    }
    
    protected void removeFile(File file) {
        String path = getFileKey(file);
        files.remove(path);
    }
    
    protected void putFile(File file, FileImpl impl) {
        String path = getFileKey(file);
        files.put(path, impl);
    }
    
    private String getFileKey(File file) {
        String key = null;
        if (TraceFlags.USE_CANONICAL_PATH) {
            try {
                key = file.getCanonicalPath();
            } catch (IOException ex) {
                key = file.getAbsolutePath();
            }
        } else {
            key = file.getAbsolutePath();
        }
        return key;
    }
    
    public Collection/*<CsmProject>*/ getLibraries() {
        ProjectBase lib = getModel().getLibrary("/usr/include");
        return lib == null ? Collections.EMPTY_LIST : Collections.singletonList(lib);
    }
    
    protected ModelImpl getModelImpl() {
        return null;
    }
    
    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map
     * @param nameTokens name
     * @param nameTokens file file that contains unresolved name (used for the purpose of statictics)
     * @param nameTokens name offset that contains unresolved name (used for the purpose of statictics)
     */
    public CsmClass getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset) {
        if( unresolved == null ) {
            unresolved = new Unresolved(this);
        }
        if (Diagnostic.needStatistics()) Diagnostic.onUnresolvedError(nameTokens, file, offset);
        return unresolved.getDummyForUnresolved(nameTokens);
    }
    
    public boolean isValid() {
        return platformProject != null  && !isProjectDisposed;
    }
    
    public void setDisposed(){
        isProjectDisposed = true;
        ParserQueue.instance().removeAll(this);
    }
    
    public boolean isDisposed() {
        return isProjectDisposed;
    }
    
    public void dispose() {
        isProjectDisposed = true;
        ParserQueue.instance().removeAll(this);
        ArrayList list = new ArrayList(getFiles().values());
        for (Iterator it = list.iterator(); it.hasNext();) {
            FileImpl file = (FileImpl) it.next();
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(file);
            } else {
                APTDriver.getInstance().invalidateAPT(file.getBuffer());
            }
        }        
        platformProject = null;
	// we have clear all collections
	// to protect IDE against the code model client
	// that stores the instance of the project
	// and does not release it upon project closure
	namespaces.clear();
	files.clear();
	classifiers.clear();
	declarations.clear();
	globalNamespace = new NamespaceImpl(this, true);
	filesHandlers.clear();
	sysAPTData = new APTSystemStorage();
	unresolved = null;
    }
    
    protected ModelImpl getModel() {
        return model;
    }
    
    public void onFileEditStart(FileBuffer buf) {
    }

    public void onFileEditEnd(FileBuffer buf) {
    }
    
    public boolean isStable(CsmFile skipFile) {
        if( status == Status.Ready ) {
            if( ! hasChangedFiles(skipFile) ) {
                return ! ParserQueue.instance().hasFiles(this, (FileImpl)skipFile);
            }
        }
        return false;
    }
    
    public void onParseFinish() {
        synchronized( waitParseLock ) {
            waitParseLock.notifyAll();
        }
	for (Iterator it = getFileList().iterator(); it.hasNext();) {
	    FileImpl file= (FileImpl) it.next();
	    file.fixFakeRegistrations();
	}
    }

    /** 
     * We'd better name this getFiles(); 
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public Collection/*FileImpl*/ getFileList() {
	return new ArrayList(files.values());
    }

    public Collection getSourceFiles() {
        List res = new ArrayList();
        for(Iterator i = getFileList().iterator(); i.hasNext();){
            FileImpl file = (FileImpl)i.next();
            if (file.isSourceFile())
                res.add(file);
        }
        return res;
    }

     public Collection getHeaderFiles() {
        List res = new ArrayList();
        for(Iterator i = getFileList().iterator(); i.hasNext();){
            FileImpl file = (FileImpl)i.next();
            if (file.isHeaderFile())
                res.add(file);
        }
        return res;
    }
     
    public long getMemoryUsageEstimation() {
	//TODO: replace with some smart algorythm
	return files.size();
    }
     
    public String toString() {
	return getName() + ' ' + getClass().getName() + " @" + hashCode();
    }

    private static class DefaultFileItem implements NativeFileItem {
	
	private NativeProject project;
	private String absolutePath;
	
	public DefaultFileItem(NativeProject project, String absolutePath) {
	    this.project = project;
	    this.absolutePath = absolutePath;
	}

        public DefaultFileItem(NativeFileItem nativeFile) {
	    this.project = nativeFile.getNativeProject();
	    this.absolutePath = nativeFile.getFile().getAbsolutePath();
	}

	public List getUserMacroDefinitions() {
	    return project.getUserMacroDefinitions();
	}
	
	public List getUserIncludePaths() {
	    return project.getUserIncludePaths();
	}
	
	public List getSystemMacroDefinitions() {
	    return project.getSystemMacroDefinitions();
	}
	
	public List getSystemIncludePaths() {
	    return project.getSystemIncludePaths();
	}
	
	public NativeProject getNativeProject() {
	    return project;
	}
	
	public File getFile() {
	    return new File(absolutePath);
	}
	
    }

    /**
     * Represent the project status. 
     *
     * Concerns only initial stage of project lifecycle:
     * allows to distingwish just newly-created project, 
     * the phase when files are being added to project (and to parser queue)
     * and the phase when all files are already added.
     *
     * It isn't worth tracking further stages (stable/unstable) 
     * since it's error prone (it's better to ask, say, parser queue
     * whether it contains files that belong to this projec tor not)
     */
    protected static enum Status {
	Initial, 
	AddingFiles, 
	Ready;
    }
    private Status status = Status.Initial;

    private Object waitParseLock = new Object();
    
    private ModelImpl model;
    private Unresolved unresolved;
    private String name = "<MyProject>";
    private NamespaceImpl globalNamespace;
    private Object platformProject;
    private boolean isProjectDisposed;
    private Map/*<String, CsmNamespaceImpl>*/ namespaces = Collections.synchronizedMap(new HashMap/*<String, CsmNamespaceImpl>*/());
    private Map/*<File, FileImpl>*/ files = Collections.synchronizedMap(new HashMap(/*<File, FileImpl>*/));
    private Map/*<String, ClassImpl>*/ classifiers = Collections.synchronizedMap(new HashMap(/*<String, ClassImpl>*/));
    private Map/*<String, ClassImpl>*/ declarations = Collections.synchronizedMap(new HashMap(/*<String, CsmDeclaration>*/));
//    private Collection files = new HashSet();
    // collection of sharable system macros and system includes
    private APTSystemStorage sysAPTData = new APTSystemStorage();
    
    private Object namespaceLock = new Object(){
        public String toString(){
            return "namespaceLock in Projectbase "+hashCode();
        }
    };

    //private NamespaceImpl fakeNamespace;
    
    // test variables.
    protected static final boolean NO_REPARSE_INCLUDE = Boolean.getBoolean("cnd.modelimpl.no.reparse.include");
    protected static final boolean LEX_NEXT_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.next.include");
    protected static final boolean ONLY_LEX_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.include");
    protected static final boolean ONLY_LEX_SYS_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.sys.include");
}
