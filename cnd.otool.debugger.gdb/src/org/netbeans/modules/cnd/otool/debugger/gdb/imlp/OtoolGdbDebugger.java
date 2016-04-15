/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.gdb.imlp;

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.GdbUtils;
import org.netbeans.modules.cnd.debugger.gdb.mi.MICommand;
import org.netbeans.modules.cnd.debugger.gdb.mi.MICommandInjector;
import org.netbeans.modules.cnd.debugger.gdb.mi.MIProxy;
import org.netbeans.modules.cnd.debugger.gdb.mi.MIRecord;
import org.netbeans.modules.cnd.debugger.gdb.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb.mi.MITList;
import org.netbeans.modules.cnd.debugger.gdb.mi.MITListItem;
import org.netbeans.modules.cnd.debugger.gdb.mi.MIUserInteraction;
import org.netbeans.modules.cnd.debugger.gdb.mi.MIValue;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.otool.debugger.api.Executor;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeFrame;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeBreakpoint;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeDebugger;
import static org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeDebugger.PROP_CURRENT_CALL_STACK_FRAME;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeThread;
import org.netbeans.modules.cnd.otool.debugger.api.SourceInfo;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeVariable;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolDebuggerManagerAdapterImpl;
import org.netbeans.modules.cnd.otool.debugger.api.io.IOPack;
import org.netbeans.modules.cnd.otool.debugger.gdb.providers.OtoolGdbEngineProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Nikolay Koldunov
 */
@OtoolNativeDebugger.Registration(path = "netbeans-OtoolGdbSession")
public class OtoolGdbDebugger extends OtoolNativeDebugger<GdbDebuggerInfo> {

    private GdbVersionPeculiarity peculiarity;  // gdb version differences
    private int state = OtoolNativeDebugger.STATE_DISCONNECTED;
    private final Object stateLock = new Object();
    private final Object currentThreadAndFrameLock = new Object();
    private List<GdbVariable> local_vars = new ArrayList();
    private VariableBag variableBag = new VariableBag();
    private List<OtoolNativeThread> threads = new ArrayList();
    private List<GdbFrame> stack = new ArrayList();
    private final BreakpointList breakpoints = new BreakpointList();   //FIXME maybe another place for this
//    protected final Target target;
    private final SourceInfo info = new SourceInfo();
    private CommandInjectorImpl commandInjectorImpl;
    private MIProxy myMIProxy;
    private ContextProvider lookupProvider;
    private OtoolGdbEngineProvider otoolNativeEngineProvider;
    static final String STRUCT_VALUE = "{...}"; // NOI18N
    static final Logger LOG = Logger.getLogger(OtoolGdbDebugger.class.toString());
    // startup parameters
    private Executor executor;

//    /*package*/ GdbDebugger(Target t, NotificationListener listener) {
//        super(t, listener);
//    }
    public OtoolGdbDebugger(ContextProvider lookupProvider) {
        super(lookupProvider);
        this.lookupProvider = lookupProvider;
        List l = lookupProvider.lookup(null, DebuggerEngineProvider.class);
        int i, k = l.size();
        for (i = 0; i < k; i++) {
            if (l.get(i) instanceof OtoolGdbEngineProvider) {
                otoolNativeEngineProvider = (OtoolGdbEngineProvider) l.get(i);
            }
        }
        if (otoolNativeEngineProvider == null) {
            throw new IllegalArgumentException("OtoolNativeEngineProvider have to be used to start OtoolNativeDebugger!");
        }

        setGdbVersion("7.7");
    }

    final void setGdbVersion(String version) {
        GdbVersionPeculiarity.Version gdbVersion = new GdbVersionPeculiarity.Version(6, 8);
        try {
            gdbVersion = GdbUtils.parseVersionString(version);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to parse gdb version {0}", version); //NOI18N
        }
        peculiarity = GdbVersionPeculiarity.create(gdbVersion);
        if (!peculiarity.isSupported()) {
            LOG.log(Level.WARNING, "version is not supported {0}", gdbVersion.toString()); //NOI18N
        }
    }

    private void startGdb() {

        
        executor = Executor.getDefault("Gdb",  // NOI18Nb
            getExecEnv(),
            0,
            new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (e instanceof NativeProcessChangeEvent) {
                        if (((NativeProcessChangeEvent) e).state == NativeProcess.State.FINISHED) {
                            if (!postedKill) {
//                                NativeDebuggerManager.warning(// In order to avoid catching the exception from exitValue()
//                                        Catalog.format(
//                                        "MSG_GdbUnexpectedlyStopped", // NOI18N
//                                        executor.getExitValue()));
                                kill();
                            }
                        }
                    }
                }
        });        
        setIOPack(IOPack.create(false, getNDI(), executor));
    // We need the slave name ahead of time
        boolean havePio = false;
        havePio = getIOPack().start();
        if (!havePio) {
            // SHOULD do something
            System.out.println("NO PTYYYYY");
        }
        List<String> args = new ArrayList();
        args.add("gdb");
        args.add("--interpreter");
        args.add("mi");
        if (havePio) {
            args.add("-tty");
            args.add(getIOPack().getSlaveName());
        }
        ProcessBuilder builder = new ProcessBuilder(args);
        try {
            Process process = builder.start();
            commandInjectorImpl = new CommandInjectorImpl(process.getOutputStream(), process.getInputStream());
            myMIProxy = new MyMIProxy(commandInjectorImpl);
            commandInjectorImpl.setMiProxy(myMIProxy);
            System.out.println("Gdb started");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

 /**
     * Only called by proxy when gdb goes away.
     * (Or on ACTION_KILL if there is no good gdb connection)
     *
     * was: sessionExited() and if(cleanup) portion of finishDebugger()
     */
    public final void kill() {
        super.preKill();



        IOPack ioPack = getIOPack();
        if (ioPack != null) {
            ioPack.bringDown();
            ioPack.close();
        }

        postedKillEngine = true;
	
         firePropertyChange(new PropertyChangeEvent(this, PROP_STATE, STATE_RUNNING, STATE_DISCONNECTED));

        // tell debuggercore that we're going away
        otoolNativeEngineProvider.getDestructor().killEngine();

	// It all ends here
    }
    @Override
    public void debug() {// TODO add checking if the instance has already been started
        setState(OtoolNativeDebugger.STATE_STARTING);

        startGdb();

        OtoolDebuggerManagerAdapterImpl.assignCurrentDebugger(this);

        setExecutable(getNDI().getTarget());
        Breakpoint[] brs = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint br : brs) {
            if (br instanceof OtoolNativeBreakpoint) {
                insertBreakpoint(((OtoolNativeBreakpoint) br).getUrl(), ((OtoolNativeBreakpoint) br).getLine());
            }
        }
        //   insertTemporaryBreakpoint("main");

        run();
    }

    private void updateCurrentState() {

        updateStackFromGdb();
        updateThreadsFromGdb();
    }

    private void updateLocalsFromGdb() {
        if (myMIProxy == null) {
            return;
        }
        String mi_command = stackListLocalsCommand();
//        String mi_command = "-stack-list-locals ";
//        mi_command += "--simple-values";

        MICommand cmd = new MiCommandImpl(mi_command) {

            @Override
            protected void onDone(MIRecord record) {
                //locals = getLocalsFromRecord(record);
                setLocals(record);
//                listener.handleNotification(new Notification("locals", locals, 0), null);//FIXME Maybe we should use AttributeChangeNotification
                super.onDone(record);
            }
        };
        myMIProxy.send(cmd);
    }

    private String stackListLocalsCommand() {
        return "-stack-list-locals --no-values"; // NOI18N
    }

    /*package*/ GdbFrame getCurrentFrame() {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.get(0);
    }

    /*
     * update local vars, include paramaters
     *
     */
    private void setLocals(MIRecord locals) {
        MITList localsresults = locals.results();
        MITList locals_list = (MITList) localsresults.valueOf("locals"); // NOI18N
        int size = locals_list.size();
        int local_count = size;

        List<GdbLocal> param_list = null;
        int params_count = 0;

        // paramaters
        GdbFrame cf = getCurrentFrame();
        if (cf != null) {
            param_list = cf.getArgsList();
            if (param_list != null) {
                params_count = param_list.size();
            }
        }

        local_count += params_count;
        //    if (Log.Variable.mi_vars) {
        System.out.println("locals " + locals_list.toString()); // NOI18N
        System.out.println("args " + param_list.toString()); // NOI18N
        System.out.println("local_count " + local_count); // NOI18N            
        //    }

        // iterate through local list
        GdbVariable[] new_local_vars = new GdbVariable[local_count];
        for (int vx = 0; vx < size; vx++) {
            MITListItem localItem = locals_list.get(vx);
//            if (peculiarity.isLocalsOutputUnusual()) {
//                localItem = ((MITList) localItem).get(0);
//            }
            MIResult localvar = (MIResult) localItem;
            String var_name = localvar.value().asConst().value();
            GdbVariable gv = variableBag.get(var_name,
                    false, VariableBag.FROM_LOCALS);
            if (gv == null) {
                new_local_vars[vx] = new GdbVariable(var_name, null, null);
                createMIVar(new_local_vars[vx]);
            } else {
                new_local_vars[vx] = gv;
                evalMIVar(new_local_vars[vx]);
            }
        }

        // iterate through frame arguments list
        for (int vx = 0; vx < params_count; vx++) {
            GdbLocal loc = param_list.get(vx);
            String var_name = loc.getName();
            String var_value = loc.getValue();

            GdbVariable gv = variableBag.get(var_name, false, VariableBag.FROM_LOCALS);
            if (gv != null) {
                gv.setValue(var_value); // update value
                new_local_vars[size + vx] = gv;
            } else {
                new_local_vars[size + vx] = new GdbVariable(var_name, loc.getType(), loc.getValue());
                createMIVar(new_local_vars[size + vx]);
            }
        }
        // need to update local_vars with fully filled array
        local_vars = Arrays.asList(new_local_vars);
        updateMIVar(); // call var-update * , but results are not reliable

    }

    /**
     * process a -var-update command
     */
    private void interpUpdate(MIRecord var) {
        MITList varsresults = var.results();
        MITList update_list = (MITList) varsresults.valueOf("changelist"); // NOI18N
        // if (Log.Variable.mi_vars) {
        System.out.println("update_list " + update_list.toString()); // NOI18N
        // }

        // iterate through update list
        for (MITListItem item : update_list) {
            MIValue updatevar;

            // On the Mac a 'changelist' is a list of results not values
            if (update_list.isResultList()) {
                MIResult result = (MIResult) item;
                //CndUtils.assertTrue(result.variable().equals("varobj"), "Erroneous response:" + var.toString()); // NOI18N
                if (!result.variable().equals("varobj")) {
                    LOG.log(Level.SEVERE, "Erroneous response: {0}", var.toString());
                }
                updatevar = result.value();
            } else {
                updatevar = (MIValue) item;
            }

            String mi_name = updatevar.asTuple().getConstValue("name"); // NOI18N
            String in_scope = updatevar.asTuple().getConstValue("in_scope"); // NOI18N
            // if (Log.Variable.mi_vars) {
            System.out.println("update name " + mi_name + " in_scope " + in_scope); // NOI18N
            // }
            /* not used
            MIValue type_changed_entry = updatevar.asTuple().valueOf("type_changed");
            String type_changed;
            if (type_changed_entry != null)
            type_changed = type_changed_entry.asConst().value();
             */
//            if (in_scope != null && in_scope.equals("true")) { // NOI18N
//                Variable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
//                if (wv != null) {
//                    evalMIVar(wv);
//                }
//            }
            GdbVariable wv = variableBag.get(mi_name, true, VariableBag.FROM_BOTH);
            if (wv != null) {
                wv.populateUpdate(updatevar.asTuple(), variableBag);

                // update value
                if (updatevar.asTuple().valueOf("value") != null) { //NOI18N
                    updateValue(wv, updatevar.asTuple().valueOf("value"), true); //NOI18N
                } else if (in_scope == null || in_scope.equalsIgnoreCase("true")) {  //NOI18N
                    evalMIVar(wv);
                }
            }
        }
    }

    private void updateValue(final GdbVariable v, MIRecord varvalue, boolean pretty) {
        MITList value_results = varvalue.results();
        MIValue miValue = value_results.valueOf("value"); //NOI18N
        updateValue(v, miValue, pretty);
    }

    private void updateValue(final GdbVariable v, MIValue miValue, boolean pretty) {
        String value = null;
        if (miValue != null) {
            value = miValue.asConst().value();
        }
        value = processValue(value);

        v.setAsText(value);

        // pretty printer for string type
        if (pretty) {
            updateStringValue(v);
        }

    }

    private void updateStringValue(final GdbVariable v) {
        if (!ValuePresenter.acceptsType(v.getType())) { //NOI18N
            return;
        }
        MiCommandImpl cmd = new MiCommandImpl("-data-evaluate-expression \"" + v.getFullName() + '\"') { //NOI18N
            @Override
            protected void onDone(MIRecord record) {
                updateValue(v, record, false);
                super.onDone(record);
            }
        };
        // cmd.dontReportError();
        myMIProxy.send(cmd);
    }

    private static String processValue(String value) {
        if (value == null) {
            return STRUCT_VALUE;
        } else if (value.startsWith("[") && value.endsWith("]")) { //NOI18N
            // detect arrays, see IZ 192927
            return STRUCT_VALUE;
        }
        return value;
    }

    private void interpVar(GdbVariable v, MIRecord var) {
        v.populateFields(var.results());

        OtoolNativeVariable wv = variableBag.get(v.getMIName(), true, VariableBag.FROM_BOTH);
        if (wv == null) {
            variableBag.add(v);
        }
        attrMIVar(v, true);
    }

    private void attrMIVar(final GdbVariable v, final boolean evalValue) {
        // see IZ 197562, on MacOSX -var-show-attributes on invalid watch breaks gdb
//        if (v.getNumChild() == -1) {
//            return;
//        }
        String expr = v.getMIName();
        // editable ?
        String cmdString = peculiarity.showAttributesCommand(expr);
        MICommand cmd
                = new MiCommandImpl(cmdString) {
            @Override
            protected void onDone(MIRecord record) {
                updateVarAttr(v, record, evalValue);
                finish();
            }
        };
        myMIProxy.send(cmd);
    }

    private void updateVarAttr(GdbVariable v, MIRecord attr, boolean evalValue) {
        MITList attr_results = attr.results();
        String value = attr_results.getConstValue("attr"); // NOI18N
        v.setEditable(value);
        if (v.isEditable() && evalValue) {
            evalMIVar(v);
        }
    }

// common method for lldb-mi
    private void updateMIVar(GdbVariable var) {
        String cmdString = "-var-update --all-values " + var.getMIName(); // NOI18N
        MICommand cmd
                = new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                interpUpdate(record);
                finish();
            }
        };
        myMIProxy.send(cmd);
    }

    static String corrupt_stack = "Previous frame identical to this frame (corrupt stack?)"; // NOI18N
    boolean try_one_more = false;

    private void genericFailure(MIRecord record) {
        String errMsg = getErrMsg(record);
        System.out.println("Error: " + errMsg);
    }

    private void updateMIVar() {
        if (!peculiarity.isLldb()) {
            String cmdString = "-var-update --all-values * "; // NOI18N
            MICommand cmd
                    = new MiCommandImpl(cmdString) {

                @Override
                protected void onDone(MIRecord record) {
                    interpUpdate(record);
                    finish();
                }

                @Override
                protected void onError(MIRecord record) {
                    String errMsg = getErrMsg(record);

                    // to work around gdb "corrupt stack" problem
                    if (try_one_more && errMsg.equals(corrupt_stack)) {
                        try_one_more = true;
                        //updateMIVar();
                    }
                    // to work around gdb "out of scope" problem
                    String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                    if (!errMsg.equals(out_of_scope)) {
                        genericFailure(record);
                        finish();
                    }
                }
            };

            myMIProxy.send(cmd);
        }

        // update string values
        List<OtoolNativeVariable> list = getLocalVars();
        for (OtoolNativeVariable var : list) {
            if (var instanceof GdbVariable) {
                // TODO: MI name should be always available by this moment
                if (peculiarity.isLldb() && ((GdbVariable) var).getMIName() != null) {
                    updateMIVar((GdbVariable) var);
                }
                updateStringValue((GdbVariable) var);
            }
        }

//        for (WatchVariable var : getWatches()) {
//            if (var instanceof GdbVariable) {
//                // TODO: MI name should be always available by this moment
//                if (peculiarity.isLldb() && ((GdbVariable) var).getMIName() != null) {
//                    updateMIVar((GdbVariable) var);
//                }
//                updateStringValue((GdbVariable)var);
//            }
//        }
    }

    private void evalMIVar(final GdbVariable v) {
        String mi_name = v.getMIName();
        // value of mi_name
        String cmdString = "-var-evaluate-expression " + mi_name; // NOI18N
        final MICommand cmd
                = new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                updateValue(v, record, true);
                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                String errMsg = getErrMsg(record);

                // to work around gdb "out of scope" problem
                String out_of_scope = "mi_cmd_var_assign: Could not assign expression to varible object"; // NOI18N
                if (!errMsg.equals(out_of_scope)) {
                    genericFailure(record);
                    finish();
                }
            }
        };
        myMIProxy.send(cmd);
    }

    private String getErrMsg(MIRecord record) {
        String errMsg = null;

        if (record.isError()) {
            errMsg = record.error();

        } else if (!record.isEmpty()) {
            errMsg = record.results().getConstValue("msg");	// NOI18N

        } else {
            // See comment to MIRecord.isEmpty().
            if (record.command() != null) {
                errMsg = record.command().getLogStream();
            }
            if (errMsg == null) {
                errMsg = "unknown failure"; // NOI18N
            }
        }
        return errMsg;
    }

    private void createMIVar(final GdbVariable v) {
        if (myMIProxy == null) {
            return;
        }
        String expr = v.getName();
        OtoolNativeThread currentThread = getCurrentThread();
        if (currentThread == null) {
            return;
        }
        String cmdString = peculiarity.createVarCommand(expr, currentThread.getId(), "0"); // NOI18N // TODO: correct frame number
        MICommand cmd
                = new MiCommandImpl(cmdString) {

            @Override
            protected void onDone(MIRecord record) {
                v.setAsText("{...}");// clear any error messages // NOI18N
                v.setInScope(true);
                interpVar(v, record);
                updateValue(v, record, true);
                finish();
            }

            @Override
            protected void onError(MIRecord record) {
                // If var's being created for watches cannot be parsed
                // we get an error.
                String errMsg = getErrMsg(record);
                v.setAsText(errMsg);
                v.setInScope(false);
                finish();
            }
        };

        myMIProxy.send(cmd);
    }
    
    @Override
    public final void stepOver() {
        final OtoolNativeThread currentThread = getCurrentThread();
        if (currentThread == null) {
            return;
        }
        sendResumptive(peculiarity.execNextCommand(currentThread.getId()));
    }
    
    private void sendResumptive(String commandStr) {
        if (myMIProxy == null) {
            return;
        }
        MICommand cmd = new MIResumptiveCommand(commandStr);
        myMIProxy.send(cmd);
    }    

    private void send(String commandStr) {
        if (myMIProxy == null) {
            return;
        }
            
        MiCommandImpl cmd = new MiCommandImpl(commandStr);
        myMIProxy.send(cmd);
    }    
    
    @Override
    public final void stepOut() {
        final OtoolNativeThread currentThread = getCurrentThread();
        if (currentThread == null) {
            return;
        }        
        if (!peculiarity.isLldb()) {
            send("-stack-select-frame 0"); // NOI18N
        }
        sendResumptive(peculiarity.execFinishCommand(currentThread.getId()));
    }    

    public OtoolNativeThread getCurrentThread() {
        if (threads == null || threads.isEmpty()) {
            return null;
        }
        for (OtoolNativeThread thread : threads) {
            if (thread.isCurrent()) {
                return thread;
            }
        }
        return null;
    }

    private void notifyCurrentThread() {
        if (threads == null || threads.isEmpty()) {
            return;
        }
        PropertyChangeEvent evt = null;
        for (OtoolNativeThread thread : threads) {
            if (thread.isCurrent()) {
                evt = new PropertyChangeEvent(this, PROP_CURRENT_THREAD, null, thread);
                break;
            }
        }
        if (evt != null) {
            firePropertyChange(evt);
        }
    }

    private void updateThreadsFromGdb() {
        if (myMIProxy == null) {
            return;
        }
        MICommand cmd = new MiCommandImpl("-thread-info") { // NOI18N
            @Override
            protected void onDone(MIRecord record) {
                List<OtoolNativeThread> res = new ArrayList<OtoolNativeThread>();
                MITList results = record.results();
                String currentThreadId = results.getConstValue("current-thread-id");
                PropertyChangeEvent evt = null;
                for (MITListItem thr : results.valueOf("threads").asList()) {
                    MITList thrList = (MITList) thr;
                    String id = thrList.getConstValue("id"); //NOI18N
                    String name = thrList.getConstValue("target-id"); //NOI18N
//                    MIValue frame = thrList.valueOf(MI_FRAME);// frame entry
//                    OtoolNativeFrame f = new OtoolNativeFrame(GdbDebuggerImpl.this, frame, null, null);
//                    f.setCurrent(true);     // in order to let Thread make some updates | GDB response contains only current frame
                    String state = thrList.getConstValue("state"); // NOI18N
                    final boolean isCurrent = id.equals(currentThreadId);
                    OtoolNativeThread gdbThread = new OtoolNativeThreadImpl(id, name, state, isCurrent);
                    if (isCurrent) {
                        evt = new PropertyChangeEvent(this, PROP_CURRENT_THREAD, null, gdbThread);
                    }
                    res.add(gdbThread);
                }
                synchronized (currentThreadAndFrameLock) {
                    threads = res;
                }
//                threadUpdater.treeChanged();
                finish();
                if (evt != null) {
                    firePropertyChange(evt);
                }
                updateLocalsFromGdb();//FIXME maybe we should retrieve locals and stack at another time
            }
        };
        myMIProxy.send(cmd);
    }

    private void updateStackFromGdb() {
        if (myMIProxy == null) {
            return;
        }
        String mi_command = "-stack-list-frames";

        MICommand cmd = new MiCommandImpl(mi_command) {

            @Override
            protected void onDone(MIRecord record) {
                updateStackArgsFromGdb(record);

                super.onDone(record);
            }
        };
        myMIProxy.send(cmd);
    }

    private void updateStackArgsFromGdb(final MIRecord framerecord) {
        String mi_command = "-stack-list-arguments ";
        mi_command += "--all-values";

        MICommand cmd = new MiCommandImpl(mi_command) {

            @Override
            protected void onDone(MIRecord argrecord) {
                List old;
                synchronized (currentThreadAndFrameLock) {
                    old = new ArrayList(stack);
                    stack = getStackWithArgsFromRecord(framerecord, argrecord);
                }

                firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, old, stack);
                //and now notify about current thread, otherwise I cannot make tailwind work
                notifyCurrentThread();

//                listener.handleNotification(new Notification("stack", stack, 0), null);//FIXME Maybe we should use AttributeChangeNotification
                super.onDone(argrecord);
            }
        };
        myMIProxy.send(cmd);
    }

    private List getLocalsFromRecord(MIRecord record) {
        MITList localsresults = record.results();
        MITList locals_list = localsresults.valueOf("locals").asList();
        MITList varlist;

        List retVal = new ArrayList();

        for (MITListItem localvar : locals_list) {
            varlist = ((MIValue) localvar).asList();
            final GdbVariable gdbVariable = new GdbVariable(varlist.getConstValue("name"), varlist.getConstValue("type"), varlist.getConstValue("value"));
            gdbVariable.setMIName(varlist.getConstValue("name")); // NOI18N);
            retVal.add(gdbVariable);
        }

        return retVal;
    }

    private List<GdbFrame> getStackWithArgsFromRecord(MIRecord framerecord, MIRecord argrecord) {
        MITList stackresults = framerecord.results();
        MITList stack_list = stackresults.valueOf("stack").asList();
        MIValue frame;
        MIResult frameargs;
        MITList argsresults = argrecord.results();
        MITList args_list = argsresults.valueOf("stack-args").asList();

        int stacksize = stack_list.size();

        List<GdbFrame> retVal = new ArrayList();

        for (int i = 0; i < stacksize; i++) {
            frame = ((MIResult) stack_list.get(i)).value();
            frameargs = (MIResult) args_list.get(i);
            retVal.add(new GdbFrame(this, frame, frameargs, getCurrentThread()));
        }

        return retVal;
    }

    private OtoolNativeBreakpoint getBreakpointFromRecord(MIRecord record) {
        MITList breakpointresults = record.results();
        MITList breakpoint = breakpointresults.valueOf("bkpt").asList();
        final String numberValue = breakpoint.getConstValue("number");
        final String line = breakpoint.getConstValue("line");
        final String fileName = breakpoint.getConstValue("fullname");
        if (numberValue.isEmpty() || fileName.isEmpty() || line.isEmpty()) {
            return null;
        }
        return new OtoolNativeBreakpoint(Integer.parseInt(numberValue), fileName, Integer.parseInt(line));
    }

    private void insertTemporaryBreakpoint(String funcOrLine) {
        String mi_command = "-break-insert -t ";
        mi_command += funcOrLine;

        MICommand cmd = new MiCommandImpl(mi_command);
        myMIProxy.send(cmd);
    }

    private void setExecutable(String execPath) {
        String mi_command = "-file-exec-and-symbols ";
        mi_command += execPath;

        MICommand cmd = new MiCommandImpl(mi_command);
        myMIProxy.send(cmd);
    }

    private void run() {
        String mi_command = "-exec-run";

        MICommand cmd = new MIResumptiveCommand(mi_command);
        myMIProxy.send(cmd);
    }

//    @Override
//    public void stepOver() {
//        String mi_command = "-exec-next";
//
//        MICommand cmd = new MIResumptiveCommand(mi_command);
//        myMIProxy.send(cmd);
//    }

    @Override
    public void stepInto() {
        String mi_command = "-exec-step";

        MICommand cmd = new MIResumptiveCommand(mi_command);
        myMIProxy.send(cmd);
    }

    @Override
    public void stop() {
        String mi_command = "-gdb-exit";

        MICommand cmd = new MiCommandImpl(mi_command) {

            @Override
            protected void onExit(MIRecord record) {
                setState(OtoolNativeDebugger.STATE_DISCONNECTED);
//                System.out.println("GdbDebugger.stop will send Notification to the listener:" + listener + " about the state (exited)");
//                listener.handleNotification(new Notification("state", "Exited", 0), null);//FIXME Maybe we should use AttributeChangeNotification
                //FIXME we should not pass the label text immediately here
                OtoolDebuggerManagerAdapterImpl.assignCurrentDebugger(null);

                otoolNativeEngineProvider.destroy();

                super.onExit(record);
            }
        };
        try {
            myMIProxy.send(cmd);
        } catch (Exception e) {
            //NPE?
            setState(OtoolNativeDebugger.STATE_DISCONNECTED);
            OtoolDebuggerManagerAdapterImpl.assignCurrentDebugger(null);
            otoolNativeEngineProvider.destroy();
        }
    }

    public void insertBreakpoint(String url, int line) {     //FIXME maybe file,line
        String mi_command = "-break-insert -f ";
        String filePath = url;
        try {
            filePath = new URL(url).getPath();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        mi_command += "\"" + filePath + ":" + line + "\"";

        MICommand cmd = new MiCommandImpl(mi_command) {

            @Override
            protected void onDone(MIRecord record) {
                OtoolNativeBreakpoint bpt = getBreakpointFromRecord(record);
                if (bpt == null) {
                    System.out.println("Something bad happened, no breakpoint added");
                    return;
                }
                breakpoints.add(bpt);

//                listener.handleNotification(new Notification("breakpoints", breakpoints, 0), bpt);//FIXME Maybe we should use AttributeChangeNotification
                super.onDone(record);
            }

            @Override
            protected void finish() {
                //cont();   //FIXME check the necessity to continue
                super.finish();
            }
        };
        myMIProxy.send(cmd);
    }

    public void deleteBreakpoint(final OtoolNativeBreakpoint bpt) {     //FIXME maybe file,line
        String mi_command = "-break-delete ";
        mi_command += bpt.getNumber();

        MICommand cmd = new MiCommandImpl(mi_command) {

            @Override
            protected void onDone(MIRecord record) {
                breakpoints.remove(bpt);

                bpt.disable();
//                listener.handleNotification(new Notification("breakpoints", breakpoints, 0), bpt);//FIXME Maybe we should use AttributeChangeNotification

                super.onDone(record);
            }
        };
        myMIProxy.send(cmd);
    }

    @Override
    public void toggleLineBreakpoint(String url, int line) {
        OtoolNativeBreakpoint bpt = breakpoints.getBptByLocation(url, line);

        if (bpt == null) {
            insertBreakpoint(url, line);
        } else {
            deleteBreakpoint(bpt);
        }
    }

    @Override
    public final void cont() {
        MICommand cmd = new MIResumptiveCommand("-exec-continue");
        myMIProxy.send(cmd);
    }

    void genericRunning() {
        setState(OtoolNativeDebugger.STATE_RUNNING);
//        System.out.println("GdbDebugger.genericStopped will send Notification to the listener:" + listener + " about the state (running)");
//        listener.handleNotification(new Notification("state", "Running", 0), null);//FIXME Maybe we should use AttributeChangeNotification
        //FIXME we should not pass the label text immediately here
    }

    void genericStopped(final MIRecord stopRecord) {
        System.out.println("OtoolGdbDebugger.genericStopped-----");
        setState(OtoolNativeDebugger.STATE_STOPPED);

        MITList stopResults = stopRecord.results();

        String reason = stopResults.getConstValue("reason");

        if (!reason.equals("exited") && !reason.equals("exited-normally")) {
            MITList frameList = stopResults.valueOf("frame").asTList();

            String file = frameList.valueOf("fullname").toString().replaceAll("\"", "");
            int line = Integer.parseInt(frameList.valueOf("line").toString().replaceAll("\"", ""));

            info.set(file, line);
//            System.out.println("GdbDebugger.genericStopped will send Notification to the listener:" + listener + " about source info " + file);
//            listener.handleNotification(new Notification("source", info, 0), null);//FIXME Maybe we should use AttributeChangeNotification

//            System.out.println("GdbDebugger.genericStopped will send Notification to the listener:" + listener + " about the state (stopped)");
//            listener.handleNotification(new Notification("state", "Stopped", 0), null);//FIXME Maybe we should use AttributeChangeNotification
            //FIXME we should not pass the label text immediately here
            updateCurrentState();

        } else {
//            System.out.println("GdbDebugger.genericStopped will send Notification to the listener:" + listener + " about the state (exited)");
//            listener.handleNotification(new Notification("state", "Exited", 0), null);//FIXME Maybe we should use AttributeChangeNotification
            //FIXME we should not pass the label text immediately here
        }
    }

    @Override
    public List<OtoolNativeVariable> getLocalVars() {
        //getLocalsFromGdb();
        if (local_vars == null) {
            return Collections.EMPTY_LIST;
        }
        return new ArrayList(local_vars);
    }

    @Override
    public SourceInfo getCurrentSourceInfo() {
        return info;
    }

    @Override
    public List<? extends OtoolNativeFrame> getCallStack() {
        // getStackFromGdb();
        if (stack == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(stack);
    }

    @Override
    public List<OtoolNativeThread> getThreads() {
        //getThreadsFromGdb();
        if (threads == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(threads);
    }

    @Override
    public List<OtoolNativeBreakpoint> getBreakpoints() {
        return breakpoints;
    }

    @Override
    public int getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    private PropertyChangeEvent setStateNoFire(int state) {
        int o;
        synchronized (stateLock) {
            if (state == this.state) {
                return null;
            }
            o = this.state;
            this.state = state;
        }
//        //PENDING HACK see issue 46287
//        System.setProperty(
//                "org.openide.awt.SwingBrowserImpl.do-not-block-awt",
//                String.valueOf(state != STATE_DISCONNECTED)
//        );
        return new PropertyChangeEvent(this, PROP_STATE, new Integer(o), new Integer(state));
    }

    @Override
    public void setState(int state) {
        if (state == STATE_RUNNING) {
            List old;
            synchronized (currentThreadAndFrameLock) {
                old = stack;
                stack = new ArrayList();//null;
            }
            if (threads != null) {
                for (OtoolNativeThread thread : threads) {
                    thread.resume();
                }
            }
            //this doesn't look correct, why new stack is NULL?
            firePropertyChange(
                    PROP_CURRENT_CALL_STACK_FRAME,
                    old,
                    null
            );
        }
        PropertyChangeEvent evt = setStateNoFire(state);
        if (evt != null) {
            firePropertyChange(evt);
        }
    }

    static class MiCommandImpl extends MICommand {

        protected MiCommandImpl(String cmd) {
            super(0, cmd);
        }

        @Override
        protected void onDone(MIRecord record) {
            System.out.println("MiCommandImpl.onDone");
            finish();
        }

        @Override
        protected void onRunning(MIRecord record) {
            finish();
        }

        @Override
        protected void onError(MIRecord record) {
            finish();
        }

        @Override
        protected void onExit(MIRecord record) {
            finish();
        }

        @Override
        protected void onStopped(MIRecord record) {
            finish();
        }

        @Override
        protected void onOther(MIRecord record) {
            finish();
        }

        @Override
        protected void onUserInteraction(MIUserInteraction ui) {
            finish();
        }

    }

    /**
     * Common behaviour for resumptive commands. While these commands are mostly
     * in the -exec family not all -exec comamnds are resumptive (e.g.
     * -exec-arguments)
     */
    private class MIResumptiveCommand extends MiCommandImpl {

        protected MIResumptiveCommand(String cmdString) {
            super(cmdString);
        }

        @Override
        protected void onRunning(MIRecord record) {
            // Actually we might get an error that will undo running
            // Perhaps we SHOULD note the receipt of running and commit to
            // it on a done?
            genericRunning();
        }

        /*@Override
        protected void onError(MIRecord record) {
            // gdb will send a "^running" even if step fails
            // cancel running state
            stateSetRunning(false);
            stateChanged();
	    session().setSessionState(state());

            genericFailure(record);
            finish();
        }*/ 
        @Override
        protected void onStopped(MIRecord record) {
            System.out.println("MIResumptiveCommand.onStopped");
            genericStopped(record);
            finish();
        }
    };

    private class MyMIProxy extends MIProxy {

        public MyMIProxy(MICommandInjector injector) {
            super(injector, "(gdb)", Charset.defaultCharset().name());
        }

        @Override
        protected void execAsyncOutput(MIRecord record) {
            System.out.println("GdbDebugger.MyMIProxy.execAsyncOutput " + "record.cls()=" + record.cls()
                    + " record.gettoken()=" + record.token() + " record is " + record);
            // dispatch async messages without a token here
            //TODO: check if the fix is correct
            // if (record.token() == 0) {
            if (record.cls().equals("stopped")) {
                System.out.println("GdbDebugger.MyMIProxy.execAsyncOutput will invoke genericStoppedNow");
                genericStopped(record);
                clearMessages();
                //     }
            } else {
                dispatch(record);
            }
        }
    }

    static class CommandInjectorImpl implements MICommandInjector {

        private RequestProcessor sendQueue = new RequestProcessor("SendQueue", 1);
        private Thread gdbOutputReader = new Thread() {

            @Override
            public void run() {
                BufferedReader gdbBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = gdbBufferedReader.readLine()) != null) {
                        System.out.println(line);
                        miProxy.processLine(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        private OutputStream outputStream;
        private InputStream inputStream;
        private MIProxy miProxy;

        public CommandInjectorImpl(OutputStream outputStream, InputStream inputStream) {
            this.outputStream = outputStream;
            this.inputStream = inputStream;
        }

        /*package*/ void setMiProxy(final MIProxy miProxy) {
            this.miProxy = miProxy;
            gdbOutputReader.start();//FIXME maybe we should change the start time
        }

        // interface MICommandInjector
        public void inject(final String cmd) {
            final char[] cmda = cmd.toCharArray();

            sendQueue.post(new Runnable() {
                public void run() {
                    try {
                        for (char c : cmda) {
                            outputStream.write(c);
                        }
                        outputStream.flush();

                        System.out.println(cmd);

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        // interface MICommandInjector
        public void log(String cmd) {
        }
    }
}
