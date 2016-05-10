/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.otool.debugger.api.io.IOPack;
import org.netbeans.modules.cnd.otool.debugger.api.registry.DebuggerProcessor;
import org.netbeans.modules.cnd.otool.debugger.spi.DebuggerExecutionListener;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Lookup;
import org.netbeans.modules.cnd.otool.debugger.spi.OtoolNativeDebuggerToolRecognizer;
//import javax.management.NotificationListener;

/**
 *
 * @author Nikolay Koldunov
 * @param <T>
 */
public abstract class OtoolNativeDebugger <T extends OtoolNativeDebuggerInfo> {
    // turned on when postKill is called
    protected volatile boolean postedKill = false;

    // turned on when killEngine is issued
    protected volatile boolean postedKillEngine = false;    
    private final T ndi;
    private final PropertyChangeSupport       pcs;

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state";  // NOI18N
    
    /** Debugger state constant. */
    public static final int             STATE_STARTING = 1;
    /** Debugger state constant. */
    public static final int             STATE_RUNNING = 2;
    /** Debugger state constant. */
    public static final int             STATE_STOPPED = 3;
    /** Debugger state constant. */
    public static final int             STATE_DISCONNECTED = 4;   
    
    public static final int STATE_LOADING = 0;
    
  /** Name of property for current thread. */
    public static final String          PROP_CURRENT_THREAD = "currentThread";  // NOI18N
    /** Name of property for current stack frame. */
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame";  // NOI18N
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    public static final String PROP_LOCALS  = "currentLocals";//NOI18N
    private IOPack ioPack;

  


    private OtoolNativeDebugger() {
        this.pcs = new PropertyChangeSupport (this);
        this.ndi = null;
    }
    
   
    
    protected OtoolNativeDebugger(ContextProvider ctx) {
       pcs = new PropertyChangeSupport (this);
       this.ndi = ctx.lookupFirst(null, getInstanceClass(this));
       
    }
    
    abstract public boolean isConnected();
    
    protected void setIOPack(IOPack ioPack) {
        this.ioPack = ioPack;
    }

    public IOPack getIOPack() {
        return ioPack;
    }    
    
    
    public static String getDebuggerString(String debuggerID, MakeConfiguration conf) {
        // Figure out dbx command
        // Copied from GdbProfile
        CompilerSet2Configuration csconf = conf.getCompilerSet();

	/* OLD
        if (csconf.isValid()) {
            cs = CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getCompilerSet(csconf.getOption());
        } else {
            cs = CompilerSet.getCompilerSet(conf.getDevelopmentHost().getExecutionEnvironment(), csconf.getOldName(), conf.getPlatformInfo().getPlatform());
            CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).add(cs);
            csconf.setValid();
        }
        Tool debuggerTool = cs.getTool(Tool.DebuggerTool);
        if (debuggerTool != null) {
            return debuggerTool.getPath();
        }
	 */

        CompilerSet cs = csconf.getCompilerSet();
        ExecutionEnvironment exEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        String csname = csconf.getOption();
        if (cs == null) {
            final int platform = conf.getPlatformInfo().getPlatform();
            CompilerFlavor flavor = CompilerFlavor.toFlavor(csname, platform);
            flavor = flavor == null ? CompilerFlavor.getUnknown(platform) : flavor;
            cs = CompilerSetFactory.getCompilerSet(exEnv, flavor, csname);
        }
        Tool debuggerTool = cs.getTool(PredefinedToolKind.DebuggerTool);
        if (debuggerTool != null) {
            Collection<? extends OtoolNativeDebuggerToolRecognizer> debuggerRecognizers = Lookup.getDefault().lookupAll(OtoolNativeDebuggerToolRecognizer.class);
            for (OtoolNativeDebuggerToolRecognizer debuggerToolRecognizer : debuggerRecognizers) {
                if (debuggerToolRecognizer.canHandle(debuggerID) && debuggerToolRecognizer.isTheSame(debuggerID, debuggerTool)) {
                    String path = debuggerTool.getPath();
                    if (path != null && !path.isEmpty()) {
                        return path;
                    }
                }
            }
            //fallback
            if (debuggerTool.getName().contains(debuggerID)) {
                    String path = debuggerTool.getPath();
                    if (path != null && !path.isEmpty()) {
                        return path;
                    }
            }
        }
//        // ask for debugger, IZ 192540
//        ToolsPanelModel model = new LocalToolsPanelModel();
//        model.setCRequired(false);
//        model.setCppRequired(false);
//        model.setFortranRequired(false);
//        model.setMakeRequired(false);
//        model.setDebuggerRequired(true);
//        model.setShowRequiredBuildTools(false);
//        model.setShowRequiredDebugTools(true);
//        model.setCompilerSetName(null); // means don't change
//        model.setSelectedCompilerSetName(csname);
//        model.setSelectedDevelopmentHost(exEnv);
//        model.setEnableDevelopmentHostChange(false);
//        BuildToolsAction bt = SystemAction.get(BuildToolsAction.class);
//        bt.setTitle(Catalog.get("LBL_ResolveMissingDebugger_Title")); // NOI18N
//        if (bt.initBuildTools(model, new ArrayList<String>(), cs)) {
//            conf.getCompilerSet().setValue(model.getSelectedCompilerSetName());
//            cs = CompilerSetManager.get(exEnv).getCompilerSet(model.getSelectedCompilerSetName());
//            if (cs != null) {
//                debuggerTool = cs.getTool(PredefinedToolKind.DebuggerTool);
//                if (debuggerTool != null) {
//                    return debuggerTool.getPath();
//                }
//            }
//        }
        return null;
    }
    
    protected final T getNDI() {
        return ndi;
    }
//    public abstract void start();
    public abstract void debug();
    public abstract void stepOver();
    public abstract void stepInto();
    public abstract void stop();
    public abstract void toggleLineBreakpoint(String url, int line); //FIXME int -> String?
    //public abstract void toggleBreakpoint(String location);
    public abstract void cont();
    

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public abstract int getState ();    
    
    public abstract void setState (int state);        
    
     /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public final void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public final void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    /**
    * Adds property change listener.
    *
    * @param l new listener.
    */
    public final void addPropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener (propertyName, l);
    }

    /**
    * Removes property change listener.
    *
    * @param l removed listener.
    */
    public final void removePropertyChangeListener (String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

     /**
     * Fires property change.
     */
    protected final  void firePropertyChange (String name, Object o, Object n) {
        pcs.firePropertyChange (name, o, n);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }

    /**
     * Fires property change.
     */
    protected final  void firePropertyChange (PropertyChangeEvent evt) {
        pcs.firePropertyChange (evt);
        //System.err.println("ALL Change listeners count = "+pcs.getPropertyChangeListeners().length);
    }
    
 /**
     * Common code to be called by subclass when engine goes away.
     */
    protected final void preKill() {
        // May be a bit redundant as it is usually set in postKill(),
        // but we might get here just by the user typing 'quit' or
        // the engine dying on us.
        postedKill = true;

        // DEBUG System.out.println("NativeDebuggerImpl.kill()");

        // SHOULD disable all (most) actions so we don't end up sending
        // stuff to engine.


//        if ( /* OLD ConsoleTopComponent.getDefault() != null && */getIOPack() != null
//                && // getIOPack().console() != null &&
//                getIOPack().console().getTerm() != null) {
//
//            SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    String warn = debuggerType() + " terminated\n"; // NOI18N
//                    char[] warnArray = warn.toCharArray();
//                    getIOPack().console().getTerm().putChars(warnArray,
//                            0, warnArray.length);
//                }
//            });
//        }
//        // Close if no more sessions
//        OtoolNativeSession[] sessions = OtoolNativeDebuggerManager.get().getSessions();
//        if (sessions.length <= 1) {
//            Disassembly.close();
//        }
//
//        // Go through the array conversion otherwise we'll get
//        // ConcurrentModificatonExpcetions.
//
//        for (Handler h : bm().getHandlers()) {
//            bm().deleteHandler(h, Gen.secondary(null), true);
//        }
//
//        for (WatchVariable w : getWatches()) {
//            deleteWatch(w, false);
//        }


//        if (OtoolNativeDebuggerManager.isPerTargetBpts()) {
//            bm().breakpointBag().cleanupBpts();
//        }
        DebuggerExecutionListener debuggerExecListener = getNDI().getDebuggerExecutionListener();
        if (debuggerExecListener != null) {
            debuggerExecListener.executionFinished(0);
        }
    }
 
    protected final  ExecutionEnvironment getExecEnv() {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        Configuration makeConfiguration = getNDI().getConfiguration();
        //ExecutionEnvironment execEnv = makeConfiguration == null ? .getDevelopmentHost().getExecutionEnvironment()
        if (makeConfiguration instanceof MakeConfiguration) {
            execEnv = ((MakeConfiguration)makeConfiguration).getDevelopmentHost().getExecutionEnvironment();
            
        }
        return execEnv;
    }    
        
    abstract public List<OtoolNativeVariable> getLocalVars() ;
    
    abstract public SourceInfo getCurrentSourceInfo();
    
    abstract public List<? extends OtoolNativeFrame> getCallStack();

    abstract  public List<? extends OtoolNativeThread> getThreads();
    
    abstract public List<? extends OtoolNativeBreakpoint> getBreakpoints();
    
    abstract public void deleteBreakpoint(final OtoolNativeBreakpoint bpt);
    
    abstract public void stepOut();
    
    
    @Retention(RetentionPolicy.SOURCE)
    @java.lang.annotation.Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         * Usually the session ID.
         */
        String path() default "";

    } 
    static class ContextAware extends OtoolNativeDebugger implements ContextAwareService<OtoolNativeDebugger> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public OtoolNativeDebugger forContext(ContextProvider context) {
            return (OtoolNativeDebugger) ContextAwareSupport.createInstance(serviceName, context);
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            return new ContextAware(serviceName);
        }

        @Override
        public void debug() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void stepOver() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void stepInto() {
            throw new UnsupportedOperationException("Not supported yet.");  // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void toggleLineBreakpoint(String sourceFile, int line) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

//        @Override
//        public void toggleBreakpoint(String location) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }

        @Override
        public void cont() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<OtoolNativeVariable> getLocalVars() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public SourceInfo getCurrentSourceInfo() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<OtoolNativeFrame> getCallStack() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<OtoolNativeThread> getThreads() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<OtoolNativeBreakpoint> getBreakpoints() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void stepOut() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setState(int state) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isConnected() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void deleteBreakpoint(OtoolNativeBreakpoint bpt) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }


    }
    
    @SuppressWarnings("unchecked") 
    private static <T extends OtoolNativeDebuggerInfo> Class<T> getInstanceClass(OtoolNativeDebugger<T> debugger) {
        return (Class<T>) (((ParameterizedType) debugger.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }    
}
