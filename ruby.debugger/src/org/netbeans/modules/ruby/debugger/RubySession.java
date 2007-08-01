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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.ruby.debugger.model.CallSite;
import org.netbeans.modules.ruby.debugger.ui.CallStackAnnotation;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.rubyforge.debugcommons.RubyDebugEventListener;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.model.RubyThreadInfo;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.model.RubyFrame;
import org.rubyforge.debugcommons.model.RubyThread;
import org.rubyforge.debugcommons.model.RubyValue;
import org.rubyforge.debugcommons.model.RubyVariable;

/**
 * @author Martin Krauskopf
 */
public final class RubySession {
    
    /**
     * Used by the NetBeans META-INF tree to identify the language type session
     * directory.
     */
    private final static String RUBY_SESSION = "RubySession"; // NOI18N
    
    private final RubyThreadInfo[] EMPTY_THREAD_INFOS = new RubyThreadInfo[0];
    private final RubyFrame[] EMPTY_FRAMES = new RubyFrame[0];
    private final RubyVariable[] EMPTY_VARIABLES = new RubyVariable[0];

    private final RubyDebuggerProxy proxy;
    private final FileLocator fileLocator;
    private RubyThread activeThread;
    private RubyFrame selectedFrame;
    private final DebuggerManagerListener sessionListener;

    public RubySession(final RubyDebuggerProxy proxy, final FileLocator fileLocator) {
        this.proxy = proxy;
        this.fileLocator = fileLocator;
        this.sessionListener = new RubySessionListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_SESSION, sessionListener);
    }
    
    public void resume() {
        selectFrame(null);
        activeThread.resume();
        EditorUtil.unmarkCurrent();
    }

    public void stepInto() {
        try {
            selectFrame(null);
            if (!activeThread.canStepInto()) {
                return;
            }
            activeThread.stepInto(forceNewLine());
        } catch (RubyDebuggerException e) {
            Util.severe("Cannot step into", e); // NOI18N
        }
    }
    
    public void stepOver() {
        try {
            selectFrame(null);
            if (!activeThread.canStepOver()) {
                return;
            }
            activeThread.stepOver(forceNewLine());
        } catch (RubyDebuggerException e) {
            Util.severe("Cannot step over", e); // NOI18N
        }
    }
    
    public void stepReturn() {
        try {
            selectFrame(null);
            activeThread.stepReturn();
        } catch (RubyDebuggerException e) {
            Util.severe("Cannot step return", e); // NOI18N
        }
    }
    
    public void runToCursor() {
        selectFrame(null);
        Line line = EditorUtil.getCurrentLine();
        if (line == null) { return; }
        FileObject fo = line.getLookup().lookup(FileObject.class);
        if (fo == null) { return; }
        if (!Util.isRubySource(fo)) { return; }
        File file = FileUtil.toFile(fo);
        if (file != null) {
            try {
                activeThread.runTo(file.getAbsolutePath(), line.getLineNumber() + 1);
            } catch (RubyDebuggerException e) {
                Util.severe("Cannot step return", e); // NOI18N
            }
        }
    }
    
    public void finish(final RubyDebugEventListener listener, final boolean terminate) {
        CallStackAnnotation.clearAnnotations();
        DebuggerManager.getDebuggerManager().removeDebuggerListener(sessionListener);
        proxy.removeRubyDebugEventListener(listener);
        if (terminate) {
            proxy.finish(true);
        }
    }

    public String getName() {
        return "localhost:" + proxy.getDebugTarged().getPort(); // NOI18N
    }
    
    /**
     * Returns latest known threads for this session.
     */
    public RubyThreadInfo[] getThreadInfos() {
        try {
            return proxy.checkConnection() ? proxy.readThreadInfo() : EMPTY_THREAD_INFOS;
        } catch (RubyDebuggerException e) {
            if (proxy.checkConnection()) {
                Util.LOGGER.log(Level.INFO, "Cannot read thread information", e);
            }
            return EMPTY_THREAD_INFOS;
        }
    }
    
    /**
     * Returns latest known frames for this session.
     */
    public RubyFrame[] getFrames() {
        try {
            return isSessionSuspended() ? activeThread.getFrames() : EMPTY_FRAMES;
        } catch (RubyDebuggerException e) {
            Util.severe("Cannot read frames information", e); // NOI18N
            return EMPTY_FRAMES;
        }
    }
    
    /**
     * Return top stack frame for the currently suspended thread.
     *
     * @return stack frame instance or <code>null</code> if there is not any
     *         suspended thread at the time
     */
    private RubyFrame getTopFrame() throws RubyDebuggerException {
        return isSessionSuspended() ? activeThread.getTopFrame() : null;
    }
    
    /**
     * Selected frame is used for evaluating variables in Local Variables view
     * or expressions in Watches view.
     */
    public void selectFrame(final RubyFrame frame) {
        this.selectedFrame = frame;
    }

    private RubyFrame getSelectedFrame() {
        try {
            return selectedFrame == null ? getTopFrame() : selectedFrame;
        } catch (RubyDebuggerException e) {
            Util.LOGGER.log(Level.INFO, "Unable to read top stack frame", e); // NOI18N
            return null;
        }
    }
    
    public boolean isSelectedFrame(final RubyFrame frame) {
        return frame.equals(getSelectedFrame());
    }
    
    public RubyVariable[] getGlobalVariables() {
        try {
            return isSessionSuspended() ? proxy.readGlobalVariables() : EMPTY_VARIABLES;
        } catch (RubyDebuggerException e) {
            Util.LOGGER.log(Level.INFO, "Cannot read global variables information", e); // NOI18N
            return EMPTY_VARIABLES;
        }
    }
    
    /**
     * Returns latest known variables for this session.
     */
    public RubyVariable[] getVariables() {
        try {
            RubyFrame frame = getSelectedFrame();
            return frame == null ? EMPTY_VARIABLES : frame.getVariables();
        } catch (RubyDebuggerException e) {
            Util.LOGGER.log(Level.INFO, "Cannot read variables information", e); // NOI18N
            return EMPTY_VARIABLES;
        }
    }
    
    public RubyVariable[] getChildren(RubyVariable parent) {
        try {
            RubyValue val = parent.getValue();
            return val == null ? EMPTY_VARIABLES : val.getVariables();
        } catch (RubyDebuggerException e) {
            Util.severe("Cannot read variables information", e); // NOI18N
            return EMPTY_VARIABLES;
        }
    }
    
    public RubyVariable inspectExpression(final String expression) {
        try {
            RubyFrame frame = getSelectedFrame();
            return frame == null ? null : frame.inspectExpression(expression);
        } catch (RubyDebuggerException e) {
            Util.finest("Unable to inspect expression [" + expression + ']'); // NOI18N
            return null;
        }
    }
    
    public void switchThread(final RubyThread thread, final ContextProviderWrapper contextProvider) {
        if (thread.isSuspended()) {
            activeThread = thread;
            try {
                RubyFrame frame = getTopFrame();
                if (frame == null) {
                    return;
                }
                EditorUtil.markCurrent(resolveAbsolutePath(frame.getFile()), frame.getLine() - 1);
                annotateCallStack(thread);
                if (contextProvider != null) {
                    contextProvider.fireModelChanges();
                }
            } catch (RubyDebuggerException e) {
                Util.severe("Cannot switch thread", e); // NOI18N
            }
        } else {
            Util.finest("Cannot switch to thread which is not suspended [" + thread + "]");
        }
    }
    
    public void switchThread(final int threadID, final ContextProviderWrapper contextProvider) {
        RubyThread thread = proxy.getDebugTarged().getThreadById(threadID);
        if (thread != null) {
            switchThread(thread, contextProvider);
        }
    }
    
    public boolean isActiveThread(int id) {
        return activeThread != null && activeThread.getId() == id;
    }
    
    /** Package-private for tests only. */
    public boolean isSessionSuspended() {
        return activeThread != null && activeThread.isSuspended();
    }
    
    public String resolveAbsolutePath(final String path) {
        if (new File(path).isAbsolute()) {
            return path;
        }
        String result = null;
        FileObject fo = fileLocator.find(path);
        if (fo != null) {
            File file = FileUtil.toFile(fo);
            if (file != null && file.isFile()) {
                result = file.getAbsolutePath();
            }
        }
        if (result == null) {
            Util.finest("Cannot resolve absolute path for: \"" + path + '"'); // NOI18N
        }
        return result;
    }

    private void annotateCallStack(final RubyThread thread) {
        try {
            RubyFrame[] frames = thread.getFrames();
            assert frames.length > 0 : "thread has >0 frames";
            CallSite[] callSites = new CallSite[frames.length - 1]; // minus first frame
            for (int i = 1; i < frames.length; i++) {
                RubyFrame frame = frames[i];
                final CallSite site = new CallSite(resolveAbsolutePath(frame.getFile()), frame.getLine() - 1);
                callSites[i - 1] = site;
            }
            CallStackAnnotation.annotate(callSites);
        } catch (RubyDebuggerException e) {
            Util.LOGGER.log(Level.WARNING, "Cannot annotated current call stack", e);
        }

    }

    private void refresh() {
        if (isSessionSuspended()) {
            switchThread(activeThread, null);
        }
    }

    /**
     * ERB generates several instructions for a single line of template code. So
     * this method returns <code>true</code> for the ERB templates.
     */
    private boolean forceNewLine() throws RubyDebuggerException {
        RubyFrame frame = activeThread.getTopFrame();
        assert frame != null;
        String path = frame.getFile();
        File f = FileUtil.normalizeFile(new File(path));
        FileObject fo = f.isAbsolute() ? FileUtil.toFileObject(f) : fileLocator.find(path);
        return fo == null ? false : Util.isERBSource(fo);
    }
    
    /** Package-private for Unit tests only. */
    RubyDebuggerProxy getProxy() {
        return proxy;
    }
    
    public SessionProvider createSessionProvider() {
        return new SessionProvider() {
            public String getSessionName() {
                return RubySession.this.getName();
            }
            
            public String getLocationName() {
                return "localhost"; // NOI18N
            }
            
            public String getTypeID() {
                return RUBY_SESSION;
            }
            
            public Object[] getServices() {
                return new Object[] {};
            };
        };
    }

    private class RubySessionListener extends DebuggerManagerAdapter {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (currentSession != null && RubyDebuggerEngineProvider.RUBY_LANGUAGE.equals(currentSession.getCurrentLanguage())) {
                Util.getCurrentSession().refresh();
            }
        }
    }
    
}
