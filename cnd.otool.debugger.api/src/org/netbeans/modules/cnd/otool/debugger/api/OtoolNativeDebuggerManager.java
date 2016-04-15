/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeDebuggerInfo.Factory;
import org.netbeans.modules.cnd.otool.debugger.api.options.EngineProfile;
import org.netbeans.modules.cnd.otool.debugger.spi.DebuggerExecutionListener;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author Nikolay Koldunov
 */
public final class OtoolNativeDebuggerManager {
    
    
    
    // request processor for various Native Debugger needs
    private static final RequestProcessor RP = new RequestProcessor("Native Debugger Request Processor", 10); //NOI18N
    
    
    // 
    // the mode in which we start debugger in
    //
    private int action = 0;
    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int RUN = (1 << 0);
    public static final int STEP = (1 << 1);
    public static final int ATTACH = (1 << 2);
    public static final int CORE = (1 << 3);
    public static final int LOAD = (1 << 4);
    public static final int CONNECT = (1 << 5);
    
    
    private static volatile boolean initialized = false;   
    
    private OtoolNativeDebugger<?> currentDebugger;
    
    private final static class LazyInitializer {
        private static final OtoolNativeDebuggerManager singleton;
        static {
            initialized = true;
            singleton = new OtoolNativeDebuggerManager();
            
//            // Initialize DebuggerManager
//            singleton.init();
//
//            // restore breakpints if any
//            singleton.breakpointBag();
//            
//            // restore watch bag
//            singleton.watchBag();
        }
    }
    
    private OtoolNativeDebuggerManager() {
        
    }
    public static OtoolNativeDebuggerManager get() {
        return LazyInitializer.singleton;
    }    
 
    public void setAction(int i) {
        action |= i;
    }

    public void removeAction(int i) {
        action &= ~i;
    }

    public int getAction() {
        // could be "run" or "step" or "" after load program
        // would be refered in Dbx.prog_loaded
        return action;
    } 
    
    /**
     * Start debugging by loading program.
     * @param executable
     * @param symbolFile
     * @param configuration
     * @param host
     * @param io
     * @param execListener
     * @param profile
     * @return 
     */
    public OtoolNativeDebuggerInfo debug(String executable, String symbolFile, 
            Configuration configuration, String host,
            InputOutput io, 
           DebuggerExecutionListener execListener, 
            RunProfile profile) {
        OtoolNativeDebuggerInfo ndi = makeNativeDebuggerInfo(debuggerType(configuration));
        ndi.setTarget(executable);
        ndi.setHostName(host);
        ndi.setConfiguration(configuration);
        ndi.setProfile(profile);
        ndi.setInputOutput(io);
        ndi.setDebuggerExecutionListener(execListener);
//        if (isStandalone() || !DebuggerOption.RUN_AUTOSTART.isEnabled(globalOptions())) {
//            ndi.setAction(LOAD);
//        } else {
            ndi.setAction(this.getAction());
 //       }
        
//        DbgProfile dbgProfile = ndi.getDbgProfile();
//        // override executable if needed
//        String debugExecutable = dbgProfile.getExecutable();
//        if (debugExecutable != null && !debugExecutable.isEmpty()) {
//            ndi.setTarget(debugExecutable);
//        }
//TODO: need to read symbol files from debug options, but unfortunely now to many UI are used there 
//        if (symbolFile == null || symbolFile.isEmpty()) {
//            symbolFile = DebuggerOption.SYMBOL_FILE.getCurrValue(dbgProfile.getOptions());
//            symbolFile = ((MakeConfiguration) configuration).expandMacros(symbolFile);
//            if (!CndPathUtilities.isPathAbsolute(symbolFile)) {
//                symbolFile = ((MakeConfiguration) configuration).getBaseDir() + "/" + symbolFile; // NOI18N
//                symbolFile = CndPathUtilities.normalizeSlashes(symbolFile);
//                symbolFile = CndPathUtilities.normalizeUnixPath(symbolFile);
//            }
//        }
        ndi.setSymbolFile(symbolFile);

        debugNoAsk(ndi);
        return ndi;
    }
    
   /**
     * Convenience function returns the DebuggerManager we're delegating to.
     */
    private static org.netbeans.api.debugger.DebuggerManager delegate() {
        return org.netbeans.api.debugger.DebuggerManager.getDebuggerManager();
    }    
    
    public static RequestProcessor getRequestProcessor() {
        return RP;
    }  
    
    /**
     * Common debugger startup entry point.
     * Starts a new session w/o asking the user about session reuse.
     * @param ndi
     */
    public void debugNoAsk(OtoolNativeDebuggerInfo ndi) {
        // "convert" our NativeDebuggerInfo to a core DebuggerInfo
        // I"m not really sure what exactly happens here, just aping jpda.
        final DebuggerInfo di =
            DebuggerInfo.create(ndi.getID(), new Object[]{ndi});

        // We eventually end up in DbxStartActionProvider.post/doAction()
        // which calls DbxDebuggerImpl.start()
        // See "./README.startup"

     //   if (isAsyncStart()) {
        getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                delegate().startDebugging(di);
            }
        });
//        } else {
//            delegate().startDebugging(di);
//        }
    }    
    /**
     * Return the debugger type for the given configuration.
     * If -J-Dcnd.nativedebugger=dbx|gdb was used on the commandline then
     * it is used.
     * Otherwise the general Debugger properties "engine" property is 
     * inspected. If it is a specific debugger that one is used.
     * If it is set to "inherit" then the CompilerCollection property is used.
     * @param configuration
     * @return 
     */
    public static EngineType debuggerType(Configuration configuration) {
        EngineType ret = EngineTypeManager.getOverrideEngineType();
        if (ret != null) {
            return ret;
        }
        EngineProfile engineProfile = (EngineProfile) configuration.getAuxObject(EngineProfile.PROFILE_ID);
        if (engineProfile == null) {
            ret = EngineTypeManager.getInherited();
        } else {
            ret = engineProfile.getEngineType();
        }
        if (ret.isInherited()) {
            final CompilerSet compilerSet = compilerSet(configuration);
            if (compilerSet != null) {
                Tool debugger = compilerSet.getTool(PredefinedToolKind.DebuggerTool);
                if (debugger != null) {
                    ToolchainManager.DebuggerDescriptor descriptor = (ToolchainManager.DebuggerDescriptor) debugger.getDescriptor();
                    EngineType typeForTool = EngineTypeManager.getEngineTypeForDebuggerDescriptor(descriptor);
                    if (typeForTool != null) {
                        ret = typeForTool;
                    }
                }
            }
        }
        if (ret.isInherited()) {
            ret = EngineTypeManager.getFallbackEnineType();
        }
        return ret;
    }
        //
    // Utilities to extract stuff from Configuration's
    //
    private static CompilerSet compilerSet(Configuration makeConfiguration) {
        if ((makeConfiguration instanceof MakeConfiguration) && ((MakeConfiguration)makeConfiguration).getCompilerSet() != null) {
            return ((MakeConfiguration)makeConfiguration).getCompilerSet().getCompilerSet();
        } else {
            return null;
        }
    }
    
 /*
     * Factory for NativeDebuggerInfo
     */
    private OtoolNativeDebuggerInfo makeNativeDebuggerInfo(EngineType debuggerType) {

        OtoolNativeDebuggerInfo info = null;
        Collection<? extends Factory> factories = Lookup.getDefault().lookupAll(OtoolNativeDebuggerInfo.Factory.class);
        for (Factory factory : factories) {
            info = factory.create(debuggerType);
            if (info != null) {
                break;
            }
        }
        assert info != null : "unknown debugger type " + debuggerType;
//        if (debuggerType.equals("dbx")) {
//            info = DbxDebuggerInfo.create(debuggerType);
//        } else if (debuggerType.equals("gdb")) {
//            info = GdbDebuggerInfo.create(debuggerType);
//        } else {
//            assert false : "known debugger type";
//        }

        return info;
    }    
    
    public int sessionCount() {
        return delegate().getSessions().length;
    }

    public void setCurrentSession(Session s) {
        delegate().setCurrentSession(s);
    }
    
    
    public Session getSession (OtoolNativeDebugger<?> debugger) {
        for (OtoolNativeSession nativeSession : getSessions()) {
            if (debugger == nativeSession.getDebugger()) {                
                return nativeSession.coreSession();
            }
        }
        return null;        
    }
    
    
    public void setCurrentDebugger (OtoolNativeDebugger<?> debugger) {
        //find OtoolNativeSession first
        for (OtoolNativeSession nativeSession : getSessions()) {
            if (debugger == nativeSession.getDebugger()) {
                setCurrentSession(nativeSession.coreSession());
                return;
            }
        }
    }

     public OtoolNativeSession[] getSessions() {
        List<OtoolNativeSession> nativeSessions = new ArrayList<OtoolNativeSession>();
        Session[] coreSessions = delegate().getSessions();
        for (int sx = 0; sx < coreSessions.length; sx++) {
            OtoolNativeSession ds = OtoolNativeSession.map(coreSessions[sx]);
            if (ds != null) {
                nativeSessions.add(ds);
            }
        }
        return nativeSessions.toArray(new OtoolNativeSession[nativeSessions.size()]);
    }


}
