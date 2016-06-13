/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Position;
import jdk.jshell.JShell;
import jdk.jshell.JShellAccessor;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.netbeans.lib.nbjshell.SnippetWrapping;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.GuardedException;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.jshell.parsing.JShellParser;
import org.netbeans.modules.jshell.parsing.ModelAccessor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * .
 * The Model is in two states: WRITE and EXECUTE. When the command is going to be executed by JShell,
 * the model switches to EXECUTE state and the document becomes r/o. Former input section is added
 * to the scrollback and any SnippetEvents which come form the JShell will be linked to that section.
 * 
 * Maintains a collection of sections from the scrollback, as the scrollback cannot change. Input section
 * may be invalidated.
 * <p/>
 * The last known editable ConsoleSection is served from {@link #getInputSection}. It may be null in the 
 * case that the JShell is executing the command and therefore there is no input section to write into. 
 * After document is modified and before the input section is revalidated, the old input section is served.
 * Use {@link #getInputEndOffset()} to get the current end offset or -1 if no active input section.
 * 
 * <p/>
 * <b>Threading model:</b> all updates must be done in JShell evaluator thread. Synchronization on 
 * the ConsoleModel <b>must be nested</b> in a document read-lock, if document access is necessary - doing
 * the opposite will deadlock with EDT.
 * 
 * @author sdedic
 */
public class ConsoleModel {
    private static Logger LOG = Logger.getLogger(ConsoleModel.class.getName());
    
    private volatile boolean valid;
    /**
     * The working and configured JShell instance
     */
    private JShell shell;
    
    /**
     * The document for console contents
     */
    private final LineDocument document;

    /**
     * Start of the unprocessed text
     */
    private int processed;

    /**
     * Position, which is protected from writing. Usually grater than
     * writableSectionStart, marks the text after the shell prompt. During
     * execution, the writablePos also marks the end of the input area, so
     * potential JShell output is properly recognized.
     */
    private volatile int writablePos;
    
    /**
     * Track possible changes, moves atomically as the input is being typed.
     */
    private Position inputEndPos = null;
    
    private volatile ConsoleSection executingSection;

    /**
     * Sections which were parsed out of the document, or seen previously
     */
    private List<ConsoleSection> scrollbackSections = new ArrayList<>();
    
    /**
     * Last section, which may be appended to, if further input comes and
     * fits into that section. This section may be even input one,
     * if executing: a trailing whitespace is appended to it.
     */
    private ConsoleSection lastSection = null;

    /**
     * Separate input section, which is writable and will be reparsed frequently
     */
    private ConsoleSection inputSection;
    
    private List<Consumer<SnippetEvent>> snippetListeners = new ArrayList<>();
    
    private final RequestProcessor evaluator;
    
    public synchronized int getInputEndOffset() {
        Position p = inputEndPos;
        return p == null ? document.getLength() : p.getOffset();
    }
    
    private Position inputOffset;
    
    public int getWritablePos() {
        ConsoleSection s = getInputSection();
        return s == null ? document.getLength() + 1 : s.getPartBegin();
    }

    public int getInputOffset() {
        ConsoleSection s = getInputSection();
        return isExecute() || s == null ? -1 : s.getStart();
    }
    
    private int getScrollbackEnd() {
        ConsoleSection s = getInputSection();
        if (isExecute()) {
            return document.getLength();
        }
        if (inputOffset == null) {
            return s != null ? s.getStart() : document.getLength();
        } else {
            return inputOffset.getOffset();
        }
    }
    
    private volatile List<ConsoleListener>   listeners = Collections.emptyList();
    
    public synchronized void forwardSnippetEvent(Consumer<SnippetEvent> l) {
        snippetListeners.add(l);
    }
    
    public synchronized void addConsoleListener(ConsoleListener l) {
        List<ConsoleListener> ll = new ArrayList<>(listeners);
        ll.add(l);
        listeners = ll;
    }
    
    public synchronized void removeConsoleListener(ConsoleListener l) {
        List<ConsoleListener> ll = new ArrayList<>(listeners);
        ll.remove(l);
        listeners = ll;
    }
    /**
     * Position of the progress indicator. The model will ignore changes past the progress
     * position.
     */
    private int progressPos = -1;
    
    private volatile boolean executing;
    
    public void setProgressPos(int pos) {
        this.progressPos = pos;
    }
    
    public boolean isExecute() {
        return executing;
    }
    
    public synchronized ConsoleSection getExecutingSection() {
        return executingSection;
    }
    
    public synchronized ConsoleSection getLastInputSection() {
        return isExecute() ? executingSection : getInputSection();
    }
    
    private volatile boolean inputValid = false;
    
    private RequestProcessor.Task inputTask;
    
    /**
     * Returns the input section information. If the data is inaccurate,
     * tries to refresh them before returning from the call. The call may block,
     * do not hold any lock neither to the ConsoleModel or write/atomic lock to the underlying
     * Document.
     * @return the input section
     */
    public ConsoleSection processInputSection() {
        synchronized (this) {
            if (inputValid) {
                return getInputSection();
            }
        }
        refreshInput(true).waitFinished();
        return inputSection;
    }
    
    /**
     * Returns input section in a non-blocking manner. Returns the last known
     * state of the input section. Some document changes may not be processed 
     * yet, as the processing must be serialized off EDT to the JShell
     * thread. Use {@link #processInputSection} to get the fresh data.
     * 
     * @return the input section
     */
    public ConsoleSection getInputSection() {
        ConsoleSection i;
        synchronized (this) {
            if (isExecute()) {
                return null;
            }
        } 
        if (!inputValid) {
            // in evaluator, the refresh happens immediately
            refreshInput(false);
        }
        return inputSection;
    }
    
    private Task refreshInput(boolean now) {
        Task t;
        boolean wait;
        
        synchronized (this) {
             if (executing) {
                return Task.EMPTY;
            }
            inputValid = false;
            if (inputTask != null) {
                inputTask.schedule(now ? 0 : 200);
            } else {
                inputTask = evaluator.post(new InputReader(), now ? 0 : 200);
            }
            t = inputTask;
            wait = !refreshPending && evaluator.isRequestProcessorThread();
        }
        if (wait) {
            t.waitFinished();
        }
        return t;
    }
    
    /**
     * If true, the refresh task is running. Input section is then to be returned
     * immediately, no wait on the refresh task to prevent self-deadlock.
     * Set and reset by the InputReader only.
     */
    private volatile boolean refreshPending;
    
    private class InputReader extends EventBuffer implements Runnable {
        private int stage;
        private long docSerial;
        private CharSequence contents;
        private ConsoleSection newSection;
        private int inputStart;
        private long endSerial;
        private Position endPos;
        private int stalledInput;
        
        private List<ConsoleSection>    updateSections;
        
        @Override
        public void run() {
            switch (stage++) {
                case 0: 
                    LOG.log(Level.FINER, "InputReader starting");
                    doIt();
                    break;
                case 1:
                    readContents();
                    if (contents != null) {
                        parseInput();
                        propagateResults();
                    }
                    break;
            }
        }
        
        public void doIt() {
            // stage 0, read the document and serial
            synchronized (ConsoleModel.this) {
                refreshPending = true;
            }
            try {
                // stage 1
                document.render(this);
            } finally {
                synchronized (ConsoleModel.this) {
                    refreshPending = false;
                }
                stage = 0;
            }
        }
        
        private void readContents() {
            stalledInput = getInputOffset();
            if (isExecute() || stalledInput == -1) {
                return;
            }
            int is = getScrollbackEnd();
            if (stalledInput < is) {
                inputStart = lastSection != null ? lastSection.getStart() : stalledInput;
                LOG.log(Level.FINER, "Detected stale input. Know input at {0} while anchor moved to {1}. LastSection = {2}, inputStart = {3}", new Object[] {
                    stalledInput, is, lastSection, inputStart
                });
            } else {
                inputStart = is;
            }
            try {
                contents = DocumentUtilities.getText(document, inputStart, document.getLength() - inputStart);
            } catch (BadLocationException ex) {
            }
            docSerial = DocumentUtilities.getDocumentVersion(document);
        }
        
        private void getPositionAndSerial() {
            if (newSection != null) {
                try {
                    if (newSection.getEnd() <= document.getLength()) {
                        endPos = document.createPosition(newSection.getEnd(), Position.Bias.Forward);
                    }
                } catch (BadLocationException ex) {
                }
            }
            endSerial = DocumentUtilities.getDocumentVersion(document);
        }
        
        private void propagateResults() {
            runUpdate();
        }
        
        protected void doUpdates() {
            getPositionAndSerial();
            if (endSerial != docSerial) {
                LOG.log(Level.FINER, "Input has changed, discarding....");
                discardSection(newSection);
                return;
            }
            
            inputEndPos = endPos;
            inputValid = true;
            try {
                inputOffset = document.createPosition(newSection.getStart(), Position.Bias.Forward);
            } catch (BadLocationException ex) {
                // should not happen, running inside readlock.
            }
            for (ConsoleSection s : updateSections) {
                addOrUpdate(s);
            }
        }
/*
        private void propagateResults2() {
            ConsoleSection changeLast = null;
            ConsoleSection oldLast = null;
            List<ConsoleSection> reportNew = new ArrayList<>();
            
            synchronized (ConsoleModel.this) {
                getPositionAndSerial();
                if (endSerial != docSerial) {
                    LOG.log(Level.FINER, "Input has changed, discarding....");
                    discardSection(newSection);
                    return;
                }
                
                oldLast = lastSection;
                ConsoleSection prevInput = inputSection;
                discardSection(prevInput);
                inputEndPos = endPos;
                inputSection = newSection;
                inputValid = true;
                try {
                    inputOffset = document.createPosition(newSection.getStart(), Position.Bias.Forward);
                } catch (BadLocationException ex) {
                    // should not happen, running inside readlock.
                }
                
                if (replaceLast != null) {
                    lastSection = replaceLast;
                }
                if (addScrollback != null && !addScrollback.isEmpty()) {
                    scrollbackSections.addAll(addScrollback);
                    if (oldLast != null) {
                        // the old last has been (?) updated, so it will be reported as a change
                        changeLast = addScrollback.remove(0);
                    }
                    reportNew.addAll(scrollbackSections);
                } else if (replaceLast != null) {
                    if (oldLast != null) {
                        // fire event that the last section has been changed.
                        changeLast = replaceLast;
                    } else {
                        reportNew.add(replaceLast);
                    }
                }
                LOG.log(Level.FINER, "Fire old change: {0}, added new: {1}, input refresh: {2}", new Object[] {
                    changeLast, reportNew, newSection
                });
            }
            invalidate();
            if (changeLast != null) {
                ConsoleSection fChangeLast = changeLast;
                RP.post(() -> notifyUpdated(fChangeLast));
            }
            if (!reportNew.isEmpty()) {
                RP.post(() -> notifyCreated(reportNew));
            }
            if (newSection != null) {
                RP.post(() -> notifyUpdated(newSection));
            }
        }
        */
        private void parseInput() {
            TokenHierarchy th = TokenHierarchy.get(getDocument());
            TokenSequence seq = th.tokenSequence();
            assert seq != null;
            seq.move(inputStart);
            JShellParser parser2 = new JShellParser(shell, seq, 0, inputStart + contents.length());
            
            parser2.execute();
            
            List<ConsoleSection> newSections = new ArrayList<>(parser2.sections());
            if (newSections.isEmpty()) {
                return;
            }
            LOG.log(Level.FINER, "Read sections: {0}", newSections);
            
            int iindex = newSections.size() - 1;
            ConsoleSection newInput = newSections.get(iindex);
            if (!newInput.getType().input) {
                LOG.log(Level.FINER, "Last section was not input - bail out");
                return;
            }
            this.updateSections = newSections;

            /*
            // if we started by executing from the last section (if the input section moved), we may need
            // to add something to the scrollback now.
            if (!newSections.isEmpty() && stalledInput < inputStart) {
                switch (newSections.size()) {
                    case 0:
                        break;
                    case 1:
                        replaceLast = newSections.get(0);
                        break;
                    default:
                        addScrollback.addAll(newSections.subList(0, newSections.size() - 1));
                        replaceLast = newSections.get(newSections.size() - 1);
                }
            }
            */
            if (shell != null) {
                // now try to process the input section into snippets, but do not execute them, just process the snippet events:
                Rng[] ranges = newInput.getAllSnippetBounds();
                int cnt = ranges.length;

                int snipPos = 0;
                for (int i = 0; i < cnt; i++) {
                    String text = newInput.getRangeContents(document, ranges[i]);
                    if (!text.isEmpty()) {
                        SnippetWrapping wr = JShellAccessor.wrapInput(shell, text);
                        snipPos = registerNewSnippet0(wr, newInput, snipPos);
                    }
                }
            }
            this.newSection = newInput;
        }
    }
    
    private synchronized void discardSection(ConsoleSection section) {
        if (section == null) {
            return;
        }
        Collection<SnippetHandle> snips = snippets.remove(section);
        if (snips != null) {
            sections.keySet().removeAll(
                    snips.stream().filter((h) -> h.getSnippet() != null).map(
                        (h) -> h.getSnippet()).collect(Collectors.toList())
            );
        }
    }
    
    public void inputChanged() {
        refreshInput(false);
    }
    
    private static final RequestProcessor RP = new RequestProcessor(ConsoleModel.class);
    
    private void notifyUpdated(ConsoleSection s) {
        ConsoleEvent e = new ConsoleEvent(this, s);
        listeners.stream().forEach(l -> l.sectionUpdated(e));
    }
    
    private void notifyUpdated(List<ConsoleSection> s) {
        ConsoleEvent e = new ConsoleEvent(this, s);
        listeners.stream().forEach(l -> l.sectionUpdated(e));
    }
    
    public Document getDocument() {
        return document;
    }
    
    private void notifyCreated(List<ConsoleSection> s) {
        ConsoleEvent e = new ConsoleEvent(this, s);
        listeners.stream().forEach(l -> l.sectionCreated(e));
    }
    
    private synchronized ConsoleSection getOpenSection() {
        if (lastSection != null) {
            return lastSection;
        } else if (executingSection != null) {
            return executingSection;
        } 
        return inputSection;
    }
    
    public synchronized ConsoleSection getLastSection() {
        ConsoleSection s = getOpenSection();
        if (s != null) {
            return s;
        }
        if (!scrollbackSections.isEmpty()) {
            return scrollbackSections.get(scrollbackSections.size() - 1);
        }
        return null;
    }
    
    private abstract class EventBuffer  {
        private List<ConsoleSection>    created;
        private List<ConsoleSection>    updated;
        ConsoleSection  prevInput;
        
        protected abstract void doUpdates();

        public void runUpdate() {
            List<ConsoleSection> myCreated;
            List<ConsoleSection> myUpdated;
            ConsoleSection executing = executingSection;
            boolean wasInput = executing == null && inputSection != null;
            boolean myInput;
            boolean same = true;
            
            synchronized (ConsoleModel.this) {
                created = new ArrayList<>();
                updated = new ArrayList<>();
                try {
                    doUpdates();
                } finally {
                    myCreated = created;
                    myUpdated = updated;
                    myInput = inputSection != null;
                    created = null;
                    updated = null;
                    // discard only the input, hopefully the preceding [output/message] section
                    // has no snippets
                    discardSection(prevInput);
                }
            }
            same &= myUpdated.isEmpty() && myCreated.isEmpty() && (myInput != wasInput);
            if (same) {
                return;
            }
            invalidate();
            RP.post(() -> {
                if (!myUpdated.isEmpty()) {
                    notifyUpdated(myUpdated);
                }
                if (!myCreated.isEmpty()) {
                    notifyCreated(myCreated);
                }
                if (!wasInput && myInput) {
                    if (executing != null) {
                        ConsoleEvent e = new ConsoleEvent(ConsoleModel.this, executing, wasInput);
                        listeners.stream().forEach(t -> t.executing(e));
                    }
                }
            });
        }

        protected void addOrUpdate(ConsoleSection s) {
            if (s.getType().input) {
                if (isExecute() || inputSection == null) {
                    created.add(s);
                } else {
                    updated.add(s);
                    prevInput = inputSection;
                }
                setInputSection(s);
            } else {
                int start = s.getStart();

                if (lastSection != null) {
                    if (start == lastSection.getStart()) {
                        updated.add(s);
                    } else if (start < lastSection.getEnd()) {
                        throw new IllegalStateException();
                    } else {
                        scrollbackSections.add(lastSection);
                    }
                    lastSection = s;
                } else {
                    if (!scrollbackSections.isEmpty()) {
                        ConsoleSection last = scrollbackSections.get(scrollbackSections.size() - 1);
                        if (last.getEnd() > start) {
                            throw new IllegalStateException();
                        }
                    }
                    lastSection = s;
                    created.add(s);
                }
            }
        }

    }
    
    /**
     * Informs that text of the document has been changed.
     * 
     * @param start start of the change
     * @param end end of the change
     */
    public void textAppended(int end) {
        ConsoleSection last;
        int start;
        
        // we should have always a last section
        synchronized (this) {
            last = lastSection;
            if (last != null) {
                start = last.getStart();
            } else if (executingSection != null) {
                start = executingSection.getEnd();
            } else if (inputSection != null) {
                start = inputSection.getStart();
            } else {
                start = processed;
            }
        }
        
        TokenHierarchy th = TokenHierarchy.get(getDocument());
        TokenSequence seq = th.tokenSequence();
        assert seq != null;
        seq.move(start);
        JShellParser parser2 = new JShellParser(shell, seq, 0, document.getLength());

        parser2.execute();
        List<ConsoleSection> sections = parser2.sections();
        if (sections.isEmpty()) {
            return;
        }
        
        List<ConsoleSection> created = new ArrayList<>(sections);
        ConsoleSection updated = null;

        /*
        ConsoleSection l = sections.get(sections.size() - 1);
        ConsoleSection f = sections.get(0);
        
        synchronized (this) {
            if (lastSection != null) {
                if (sections.size() > 1) {
                    if (lastSection != last) {
                        // no update
                        scrollbackSections.add(lastSection);
                    } else {
                        updated = created.remove(0);
                    }
                    scrollbackSections.addAll(sections.subList(0, sections.size() - 1));
                } else {
                    // just 1 section updated, it is the last section.
                    if (last != null && lastSection == last) {
                        lastSection = updated = f;
                        created.clear();
                    } else {
                        scrollbackSections.add(lastSection);
                    }
                }
            } else {
                scrollbackSections.addAll(sections.subList(0, sections.size() - 1));
            }
            if (l.getType().input) {
                if (sections.size() > 1) {
                    ConsoleSection prevLast = sections.get(sections.size() - 2);
                    if (!prevLast.getType().input) {
                        lastSection = prevLast;
                    } else {
                        lastSection = null;
                    }
                } else {
                    lastSection = null;
                }
            } else {
                lastSection = l;
            }
            processed = end;
        }
        ConsoleSection execSection;
        
        if (l.getType().input) {
            // prompt was displayed
            execSection = executingSection;
            setInputSection(l);
        } else {
            lastSection = l;
            execSection = null;
        } 
        final ConsoleSection fu = updated;
        RP.post(() -> {
            if (fu != null) {
                notifyUpdated(fu);
            }
            if (!created.isEmpty()) {
                notifyCreated(created);
            }
            if (execSection != null) {
                ConsoleEvent e = new ConsoleEvent(this, execSection, false);
                listeners.stream().forEach(t -> t.executing(e));
            }
        });
        */
        new EventBuffer() {
            @Override
            protected void doUpdates() {
                for (ConsoleSection s : sections) {
                    addOrUpdate(s);
                }
            }
        }.runUpdate();
        invalidate();
    }
    
    private synchronized void setInputSection(ConsoleSection s) {
        executingSection = null;
        if (s != null) {
            executing = false;
        }
        this.inputSection = s;
        inputValid = true;
        invalidate();
    }
    
    private void change(DocumentEvent e) {
        int s = e.getOffset();
        int l = e.getLength();
        ConsoleSection i = getInputSection();
        if (isExecute() || (i != null && s < i.getStart())) {
            if (progressPos != -1 && s >= progressPos) {
                return;
            }
            textAppended(document.getLength() + 1);
        } else if (inputSection != null && inputSection.getStart() < s) {
            refreshInput(false);
        }
    }
    
    private synchronized void invalidate() {
        allSections = null;
    }
    
    private List<ConsoleSection> allSections = null;
    
    public List<SnippetHandle>  getSnippets(ConsoleSection s) {
        return snippets.get(s);
    }
    
    public synchronized List<ConsoleSection> getSections() {
        if (allSections != null) {
            return allSections;
        }
        List<ConsoleSection> res = new ArrayList<>(scrollbackSections);
        if (lastSection != null) {
            res.add(lastSection);
        }
        ConsoleSection is = getInputSection();
        if (is != null) {
            res.add(is);
        }
        allSections = res;
        return res;
    }
    
    /**
     * Prepares for the JShell input execution.
     * Locks the document (moves writing pointer at the end), moves the current
     * input section to the scrollback
     * 
     */
    synchronized void beforeExecution() {
        assert !isExecute();
        ConsoleSection is = getInputSection();
        executingSection = is;
        if (is != null) {
            // the input will be added to the scrollback; if something is still
            // buffered in the lastSection, add it first:
            if (lastSection != null) {
                scrollbackSections.add(lastSection);
            }
            lastSection = null;
            scrollbackSections.add(executingSection);
        }
        executing = true;
        if (is != null) {
            RP.post(() -> { 
                // notify that the scrollback has been changed.
                notifyUpdated(is); 
                
                ConsoleEvent e = new ConsoleEvent(this, is, true);
                listeners.stream().forEach(l -> l.executing(e));
            });
        }
    }
    
    synchronized void afterExecution() {
//        if (lastSection != null) {
//            scrollbackSections.add(lastSection);
//        }
        ConsoleSection s = executingSection;
        if (s != null) {
            RP.post(() -> { 
                // execution finished.
                ConsoleEvent e = new ConsoleEvent(this, s, false);
                listeners.stream().forEach(l -> l.executing(e));
            });
        }
        executingSection = null;
        if (inputSection == null) {
            // create an input section forcefully
        }
    }
    
    private JShell.Subscription snippetSubscription;

    /**
     * Attaches to a JShell instance.
     * @param shell 
     */
    public void attach(JShell shell) {
        this.shell = shell;
        snippetSubscription = shell.onSnippetEvent(this::acceptSnippet);
    }

    /**
     * Provides mapping between snippets and individual input sections
     */
    private Map<Snippet, SnippetHandle> sections = new HashMap<>();
    private Map<ConsoleSection, List<SnippetHandle>> snippets = new HashMap<>();
    
    public Stream<Snippet> inactiveSnippets() {
        return sections.keySet().stream().filter(s -> !shell.status(s).isActive);
    }
    
    private void registerNewSnippet(Snippet snip) {
        assert isExecute();
        registerNewSnippet0(JShellAccessor.snippetWrap(shell, snip), executingSection, execSnippetOffset);
    }
    
    private int execSnippetOffset = -1;
    
    void setSnippetOffset(int localOffset) {
        this.execSnippetOffset = localOffset;
    }
    
    private synchronized int registerNewSnippet0(SnippetWrapping snip, ConsoleSection section, int sectionOffset) {
        List<SnippetHandle> sectionSnippets = snippets.get(section);
        if (sectionSnippets == null) {
            sectionSnippets = new ArrayList<>(1);
        } else {
            sectionSnippets = new ArrayList<>(sectionSnippets);
        }
        Rng[] fragments = null;
        int l = sectionSnippets.size();
        SnippetHandle replace = null;
        int replIndex = -1;
        // in section which has been parsed, as is now being executed, replace the snippets
        // according to the real 
        int start = sectionOffset;
        int end = sectionOffset + snip.getSource().length();

        int so = section.offsetFromContents(start);
        int eo = section.offsetFromContents(end);
        fragments = section.computeFragments(new Rng(so, eo));
        sectionOffset = end;
        if (section == executingSection) {
            // find the proper snippet / fragments
            
            for (int i = 0; i < sectionSnippets.size();) {
                SnippetHandle si = sectionSnippets.get(i);
                if (si != null) {
                    if (si.start() <= so && si.end() >= eo) {
                        // replace
                        if (replace != null) {
                            // remove all subsequent matching snippets
                            sectionSnippets.remove(i);
                            continue;
                        }
                        replace = si;
                        replIndex = i;
                    }
                }
                i++;
            }
            
        }
        SnippetHandle handle = new SnippetHandle(
                section,
                fragments, snip);
        if (replace != null) {
            if (replace.getSnippet() != null) {
                sections.remove(replace.getSnippet());
            }
            sectionSnippets.set(replIndex, handle);
        } else {
            sectionSnippets.add(handle);
        }
        snippets.put(section, sectionSnippets);
        if (snip.getSnippet() != null) {
            sections.put(snip.getSnippet(), handle);
        }
        return sectionOffset;
    }
    
    private void acceptSnippet(SnippetEvent ev) {
        Snippet snip = ev.snippet();
        Snippet.Status stat = ev.status();

        switch (stat) {
            case REJECTED: {
                SnippetWrapping wrap = JShellAccessor.snippetWrap(shell, snip);
                // special processing: the rejected snippet may have no wrapping;
                // in that case, we must create an artificial snippet and register it 
                // with the new filename.
                if (executingSection != null) {
                    registerNewSnippet0(wrap, executingSection, execSnippetOffset);
                    break;
                }
                // fall through - maybe we need to create an additional snippet because
                // of the snippet key
            }
            case VALID:
            case RECOVERABLE_DEFINED:
            case RECOVERABLE_NOT_DEFINED: {
                if (ev.previousStatus() == Snippet.Status.VALID) {
                    if (getInfo(ev.snippet()) == null) {
                        return;
                    }
                    break;
                }
                registerNewSnippet(snip);
                break;
            }

            case DROPPED:
            case OVERWRITTEN:
                // these snippets are no longer visible to others.
                // other snippets will stop importing them, we do not need to do anything.
                break;
        }
        
        Consumer<SnippetEvent>[] ll;
        synchronized (this) {
            if (snippetListeners.isEmpty()) {
                return;
            }
            ll = snippetListeners.toArray(new Consumer[snippetListeners.size()]);
        }
        final SnippetEvent ee = ev;
        Arrays.stream(ll).forEach(s -> s.accept(ee));
    }
    
    private class DocL implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            change(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            change(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {}
    }
    
    private class DocFilter extends DocumentFilter {

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (!isValid()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }
            bypass = fb;
            int wr = getWritablePos();
            if (offset >= wr) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }
            int endPos = offset + length;
            if (endPos < wr) {
                return;
            }
            int remainder = offset + length - wr;
            int prefix = wr - offset;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (!isValid()) {
                super.insertString(fb, offset, string, attr);
                return;
            }
            bypass = fb;
            if (offset >= getWritablePos()) {
                super.insertString(fb, offset, string, attr);
            } else {
                throw new GuardedException(string, offset);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (!isValid()) {
                super.remove(fb, offset, length);
                return;
            }
            bypass = fb;
            if (offset >= getWritablePos() || length == 0) {
                super.remove(fb, offset, length);
            } else {
                throw new GuardedException(null, offset);
            }
        }
        
    }
    
    // initial bypass impl just writes to the document.
    private DocumentFilter.FilterBypass bypass = new DocumentFilter.FilterBypass() {
        @Override
        public Document getDocument() {
            return document;
        }

        @Override
        public void remove(int offset, int length) throws BadLocationException {
            if (bypass != this) {
                bypass.remove(offset, length);
            } else {
                document.remove(offset, length);
            }
        }

        @Override
        public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
            if (bypass != this) {
                bypass.insertString(offset, string, attr);
            } else {
                document.insertString(offset, string, attr);
            }
        }

        @Override
        public void replace(int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (bypass != this) {
                bypass.replace(offset, length, string, attrs);
            } else {
                document.remove(offset, length); 
                document.insertString(offset, string, attrs);
            }
        }
    };
    
    public static ConsoleModel get(Document d) {
        return (ConsoleModel)d.getProperty(ConsoleModel.class);
    }

    public static ConsoleModel create(Document d, JShell shell, RequestProcessor evaluator) {
        LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
        
        if (ld == null) {
            return null;
        }
        ConsoleModel mdl;
        synchronized (d) {
            mdl = (ConsoleModel)ld.getProperty(ConsoleModel.class);
            if (mdl != null) {
                return mdl;
            }
            mdl= new ConsoleModel(ld, evaluator);
            d.putProperty(ConsoleModel.class, mdl);
            mdl.init();
        }
        return mdl;
    }

    public ConsoleModel(LineDocument document, RequestProcessor evaluator) {
        this.document = document;
        this.evaluator = evaluator;
    }
    
    private DocFilter f;
    private DocL l;
    
    private void init() {
        AbstractDocument ad = LineDocumentUtils.asRequired(document, AbstractDocument.class);
        this.valid = true;
        ad.setDocumentFilter(new DocFilter());
        try {
            // initialize the bypass:
            ad.replace(0, 0, "", null);
        } catch (BadLocationException ex) {
        }
        org.netbeans.lib.editor.util.swing.DocumentUtilities.addPriorityDocumentListener(document,
                l = new DocL(), DocumentListenerPriority.CARET_UPDATE);
    }
    
    public String getInputText() {
        int wr = getWritablePos();
        if (wr != -1) {
            String[] res = new String[1];
            document.render(() -> {
                try {
                    res[0] = document.getText(wr, document.getLength() - wr);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            if (res[0] != null) {
                return res[0];
            }
        }
        return "";
    }
    
    public void writeToShellDocument(String text) {
        AtomicLockDocument ald = LineDocumentUtils.asRequired(
                document, AtomicLockDocument.class);
        try {
            ald.runAtomic(()-> {
                try {
                    int offset = getInputOffset();
                    if (offset == -1) {
                        offset = document.getLength();
                    }
                    getProtectionBypass().insertString(offset, text, null);
                    textAppended(offset);
                } catch (BadLocationException ex) {
                }
            });
        } finally {
        }
    }
    
    public DocumentFilter.FilterBypass getProtectionBypass() {
        return bypass;
    }
    
    public List<String> history() {
        return scrollbackSections.stream().filter(s -> s.getType().input).
                map(s -> s.getContents(document)).collect(Collectors.toList());
    }
    
    /**
     * Encapsulates mapping from code snippet onto the console model and document.
     */
    public static class SnippetHandle {
        /**
         * The ConsoleSection which contains the snippet
         */
        final ConsoleSection section;
        
        final Rng[] fragments;
        
        /**
         * JShell wrapping for the snippet.
         */
        final SnippetWrapping wrapping;

        SnippetHandle(ConsoleSection section, 
                Rng[] fragments, 
                SnippetWrapping wrapping) {
            this.section = section;
            this.fragments = fragments;
            this.wrapping = wrapping;
        }
        
        public int start() {
            return fragments[0].start;
        }
        
        public int end() {
            return fragments[fragments.length - 1].end;
        }

        /**
         * Returns a completely wrapped code for the snippet.
         * The wrapped code may not be a valid java, for totally erroneous snippets,
         * where the parser cannot recognize even the kind of snippet. 
         * 
         * @return wrappped code
         */
        public String getWrappedCode() {
            return wrapping.getCode();
        }
        
        /**
         * Translates code position into the wrapped code positions.
         * Given a position in snippet's (input) code, produces a position in the wrapped code.
         * <p/>
         * If the position cannot be mapped, returns -1.
         * 
         * @param pos input text position
         * @return position in the wrapped text or -1 to indicate error.
         */
        public int getWrappedPosition(int pos) {
            return wrapping.getWrappedPosition(pos);
        }
        
        public Snippet getSnippet() {
            return wrapping.getSnippet();
        }
        
        public ConsoleSection getSection() {
            return section;
        }
        
        public Snippet.Kind getKind() {
            return wrapping.getSnippetKind();
        }
        
        public String getSource() {
            return wrapping.getSource();
        }
        
        public Rng[] getFragments() {
            return fragments;
        }
        
        public Snippet.Status getStatus() {
            return wrapping.getStatus();
        }

        public String getClassName() {
            return wrapping.getClassName();
        }
    }
    
    public synchronized SnippetHandle getInfo(Snippet snip) {
        return sections.get(snip);
    }
    
    public JShell getShell() {
        return shell;
    }
    
    static class ModelAccImpl extends ModelAccessor {

        @Override
        public void extendSection(ConsoleSection section, int start, int end, List<Rng> ranges, List<Rng> snippets) {
            if (ranges == null || ranges.isEmpty()) {
                section.extendWithPart(start, end);
            } else {
                section.extendToWithRanges(ranges);
            }
            if (snippets != null && snippets.size() > 1) {
                section.setSnippetRanges(snippets);
            }
        }

        @Override
        public void setSectionComplete(ConsoleSection target, boolean complete) {
            target.setComplete(complete);
        }

//        @Override
        public void beforeExecution(ConsoleModel model) {
            model.beforeExecution();
        }
        
        public void execute(ConsoleModel model, Runnable c, Supplier<String> prompt)  {
            model.beforeExecution();
            try {
                c.run();
            } finally {
                model.ensureInputSectionAvailable(prompt);
                model.afterExecution();
            }
        }

//        @Override
        public void afterExecution(ConsoleModel model) {
            model.afterExecution();
        }

        @Override
        public void setSnippetOffset(ConsoleModel model, int offset) {
            model.setSnippetOffset(offset);
        }
    }
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Detaches the console model from the document and the JShell. The model
     * will not restrict or observe the document
     */
    public JShell.Subscription detach() {
        JShell.Subscription d;
        synchronized (this) {
            if (!valid) {
                return null;
            }
            d = snippetSubscription;
            snippetSubscription = null;
            valid = false;
        }
        document.putProperty(ConsoleModel.class, null);
        document.removeDocumentListener(l);
        ConsoleEvent ev = new ConsoleEvent(this, Collections.emptyList());
        listeners.stream().forEach(l -> l.closed(ev));
        return d;
    }
    
    static {
        ModelAccessor.impl(new ModelAccImpl());
    }

    void ensureInputSectionAvailable(Supplier<String> promptSupplier) {
        ConsoleSection s = processInputSection();
        if (s != null) {
            return;
        }
        String promptText = "\n" + promptSupplier.get(); // NOI18N
        writeToShellDocument(promptText);
    }
    
}
