/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Type;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Utility methods, which help JavaSource processing tasks to coordinate with the indexing/scanning process.
 * There are two variants of {@link JavaSource#runUserActionTask}, which support <b>abort and restart</b> of the user task,
 * if the task indicates incomplete data and the scanning is in progress. The restart is done in a hope that, after scanning
 * completes, the missing types or elements will appear.
 * <p/>
 * The user task may use other provided wrapper methods to:
 * <ul>
 * <li>resolve ElementHandle - {@link #resolveElement}
 * <li>get named type - {@link #findTypedElement}
 * <li>get element on a specified TreePath - {@link #findElement}
 * </ul>
 * All those method check if the element is 'incomplete' and throw an exception, abort the current user task and reschedule the task
 * after scanning finishes. The user code may also call {@link #signalIncompleteData} manually if it decides to wait for parsing to complete.
 * <p/>
 * Note that even though the user task run when all the parsing/scanning is over, there's no guarantee that data obtained from
 * the parser structures will be complete and valid - the source code may be really badly broken, dependency missing etc.
 * <p/>
 * An example pattern how to use this class is:
 * <code><pre>
 * final TreePath tp = ... ; // obtain a tree path
 * ScanUtils.waitUserActionTask(new Task&lt;{@link CompilationController}>() {
 *      public void run(CompilationController ctrl) {
 *          // possibly abort and wait for all info to become available
 *          Element e = ScanUtils.findElement(tp);
 *          if (!ScanUtils.isElementUsable(e)) {
 *              // report the error; element is not found or is otherwise unusable
 *          } else {
 *              // do the really useful work on Element e
 *          }
 *      }
 * };
 * </pre></code>
 *
 * @author Svata Dedic
 */
public final class ScanUtils {
    private ScanUtils() {}
    
    /**
     * Attempts to find an Element at the TreePath, retries the action task if Element might not be available because of running scan.
     * If the element does not exist, or is 'not complete' (e.g. type was not yet scanned etc), the task is aborted, and
     * restarted when the parsing finishes.
     * <p/>
     * The method can be only used from {@link #waitUserActionTask}, {@link #postUserActionTask} as it throws an exception to interrupt
     * the process, which is processed by the mentioned methods. If the method is called in other context, {@link IllegalStateException}
     * will be thrown.
     * <p/>
     * Use this method if you want to not to fail computation if a referenced type might not be yet scanned.
     * Note that it is not guaranteed, even after task retry, that the Element will be resolved, or will be fully attributed. If the
     * handle points to Element which does not exist, or uses unknown type etc, the Element may be broken even after parsing finishes.
     * Always check the element for its kind and other properties.
     * 
     * @param s source to work with
     * @param trees Trees instance used to resolve the Element
     * @param path path to resolve
     *
     * @see #waituserActionTask
     * @see #postUserActionTask
     * @see #isElementUsable
     */
    public static <T extends Element> T findElement(@NonNull JavaSource s, @NonNull Trees trees, @NonNull TreePath path) {
        checkRetryContext();
        T e = (T) trees.getElement(path);
        if (e != null) {
            TypeMirror tm = e.asType();
            if (!isErrorKind(tm)) {
                return e;
            }
        }
        if (shouldSignal()) {
            signalIncompleteData(s, path);
        }
        return e;
    }
    
    /**
     * Attempts to find Element by path; aborts and restarts the user task if scanning is in progress and element could not be found.
     * If the element does not exist, or is 'not complete' (e.g. type was not yet scanned etc), the task is aborted, and
     * restarted when the parsing finishes.
     * <p/>
     * The method can be only used from {@link #waitUserActionTask}, {@link #postUserActionTask} as it throws an exception to interrupt
     * the process, which is processed by the mentioned methods. If the method is called in other context, {@link IllegalStateException}
     * will be thrown.
     * <p/>
     * Use this method if you want to not to fail computation if a referenced type might not be yet scanned.
     * Note that it is not guaranteed, even after task retry, that the Element will be resolved, or will be fully attributed. If the
     * handle points to Element which does not exist, or uses unknown type etc, the Element may be broken even after parsing finishes.
     * Always check the element for its kind and other properties.
     * 
     * @param s JavaSource to use
     * @param info CompilationInfo applicable for the user task
     * @param path path that should be resolved to Element
     * 
     * @return resolved Element instance, or {@code null} if the element could not be found.
     */
    public static <T extends Element> T findElement(@NonNull JavaSource s, @NonNull CompilationInfo info, 
            @NonNull TreePath path) {
        checkRetryContext();
        T e = (T) info.getTrees().getElement(path);
        if (e != null) {
            TypeMirror tm = e.asType();
            if (!isErrorKind(tm)) {
                return e;
            }
        }
        if (shouldSignal()) {
            TreePathHandle hnd = TreePathHandle.create(path, info);
            signalIncompleteData(s, hnd);
        }
        return e;
    }
    
    private static boolean shouldSignal() {
        return SourceUtils.isScanInProgress() && Boolean.TRUE.equals(retryGuard.get());
    }
    
    /**
     * Runs the user task through {@link JavaSource#runUserActionTask}, and returns Future completion handle for it. 
     * The executed Task may indicate that it does not have enough data when scan is in progress, through e.g. {@link #checkElement}.
     * If so, processing of the Task will abort, and will be restarted when the scan finishes.
     * <p/>
     * The task <b>may run asynchronously</b>, pay attention to synchronization of task's output
     * data. The first attempt to run the task MAY execute synchronously. Do not call the method from
     * Swing EDT, if the task typically takes non-trivial time to complete (see {@link JavaSource#runUserActionTask}
     * for discussion).
     * <p/>
     * Unline {@link #waitUserActionTask}, this method does not publish exceptional results from the action task, when
     * it is scheduled after parsing. It's the responsibility of the caller to communicate exceptions from the task.
     * Note that if the task is run synchronously
     *
     * @param src JavaSource to process
     * @param uat action task that will be executed
     * @param shared if true, shared Javac will be used (faster). Flag is passed to {@link JavaSource} methods.
     *
     * @return Future that allows to synchronize with task's completion.
     *
     * @see JavaSource#runUserActionTask
     */
    public static Future<Void> postUserActionTask(@NonNull final JavaSource src, @NonNull final Task<CompilationController> uat, final boolean shared) throws IOException {
        assert src != null;
        assert uat != null;
        return postUserActionTask(src, uat, shared, new AtomicReference(null));
    }

    private static Future<Void> postUserActionTask(@NonNull final JavaSource src, @NonNull final Task<CompilationController> uat, final boolean shared, final AtomicReference<Throwable> status) throws IOException {
        boolean retry = SourceUtils.isScanInProgress();
        Boolean b = retryGuard.get();
        try {
            retryGuard.set(Boolean.TRUE);
            src.runUserActionTask(uat, shared);
            // the action passed ;)
            retry = false;
        } catch (RetryWhenScanFinished e) {
            // expected, will retry in runWhenParseFinished
            retry = true;
        } finally {
            if (b == null) {
                retryGuard.remove();
            } else {
                retryGuard.set(b);
            }
        }
        if (!retry) {
            return new FinishedFuture();
        }
        final TaskWrapper wrapper = new TaskWrapper(uat, status);
        Future<Void> handle = src.runWhenScanFinished(wrapper, shared);
        return handle;
    }

    /**
     * Runs user action over source 'src' using {@link JavaSource#runUserActionTask} and waits for its completion.
     * The executed Task may indicate that it does not have enough data when scan is in progress, through e.g. {@link #checkElement}.
     * If so, processing of the Task will abort, and will be restarted when the scan finishes. The {@code waitUserActionTask} method
     * will wait until the rescheduled task completes.
     * <p/>
     * Calling this method from Swing ED thread is prohibited.
     *
     * @param src java source to process
     * @param uat task to execute
     * @param shared if true, shared Javac will be used to process the source
     * 
     * @throws IOException in the case of a failure in the user task, or scheduling failure (propagated from {@link JavaSource#runUserActionTask},
     * {@link JavaSource#runWhenScanFinished}
     *
     * @see JavaSource#runUserActionTask
     */
    public static void waitUserActionTask(@NonNull final JavaSource src, @NonNull final Task<CompilationController> uat, final boolean shared) throws IOException {
        assert src != null;
        assert uat != null;
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Illegal to call within EDT");
        }
        AtomicReference<Throwable> status = new AtomicReference(null);
        Future<Void> f = postUserActionTask(src, uat, shared, status);
        if (f.isDone()) {
            return;
        }
        try {
            f.get();
        } catch (InterruptedException ex) {
            IOException ioex = new IOException("Interrupted", ex);
            throw ioex;
        } catch (ExecutionException ex) {
            IOException ioex = new IOException("Interrupted", ex);
            throw ioex;
        } catch (RuntimeException ex) {
            throw ex;
        }
        // propagate the 'retry' instruction as an exception - was not thrown in an appropriate context.
        Throwable t = status.get();
        if (t != null) {
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            IOException ioex = new IOException("Exception during processing", t);
            throw ioex;
        }
    }

    /**
     * Attempts to locate a named {@link TypeElement} , retries action task if Element might not be available because of running scan.
     * If the element does not exist, or is 'not complete' (e.g. type was not yet scanned etc), the task is aborted, and
     * restarted when the parsing finishes.
     * <p/>
     * The method can be only used from {@link #waitUserActionTask}, {@link #postUserActionTask} as it throws an exception to interrupt
     * the process, which is processed by the mentioned methods. If the method is called in other context, {@link IllegalStateException}
     * will be thrown.
     * <p/>
     * Use this method if you want to not to fail computation if a referenced type might not be yet scanned.
     * Note that it is not guaranteed, even after task retry, that the Element will be resolved, or will be fully attributed. If the
     * handle points to Element which does not exist, or uses unknown type etc, the Element may be broken even after parsing finishes.
     * Always check the element for its kind and other properties.
     * 
     * @param s source to work with
     * @param elService Elements service used to locate the type
     * @param type type name whose TypeElement should be returned
     * 
     * @return resolved TypeElement, or null if type could not be located
     *
     * @see #waituserActionTask
     * @see #postUserActionTask
     * @see #isElementUsable
     * @throws IllegalStateException if not called from within {@link #waitUserActionTask} and the like
     */
    public static TypeElement findTypeElement(@NonNull JavaSource s, @NonNull Elements elService, @NonNull String type) {
        checkRetryContext();
        TypeElement e = elService.getTypeElement(type);
        if (e != null) {
            TypeMirror tm = e.asType();
            if (!isErrorKind(tm)) {
                return e;
            }
        }
        if (shouldSignal()) {
            signalIncompleteData(s, type);
        }
        return e;
    }
    
    /**
     * Returns true, if <ul>
     * <li>scanning is in progress, and
     * <li>the element is null or incomplete (see {@link #isElementUsable}),
     * </ul>
     * Code should use this method to detect if the user should be given a warning
     * that presented data may be incomplete or imprecise, and data should be refreshed
     * or gathered again after scanning completes.
     * 
     * @param e the Element to check
     * @return true, for a broken/missing element, which might become fixed/available after scan completes.
     */
    public static boolean mayBecomeCompleted(@NullAllowed Element e) {
        return SourceUtils.isScanInProgress() && !isElementUsable(e);
    }

    /**
     * Returns true, if the Element is attributed and usable for processing. An element may be unresolved,
     * or unattributed and should not be generally used without caution.
     *
     * @param e Element to check
     * @retrun true, if the element can be used.
     */
    public static boolean isElementUsable(@NullAllowed Element e) {
        if (e == null) {
            return false;
        }
        final TypeMirror type = e.asType();
        if (type instanceof Type) {
            if (((Type)type).isErroneous()) {
                return false;
            }
        }
        return !isErrorKind(type);
    }

    /**
     * Checks that the Element is valid, is not erroneous. If the Element is {@code null} or erroneous and
     * scanning is running, the method throws a {@link RetryWhenScanFinished} exception to
     * indicate that the action should be retried. The method should be only used in code, which
     * is directly or indirectly executed by {@link #waitUserActionTask}, otherwise {@link IllegalStateException}
     * will be thrown.
     * <p/>
     * If scan is not running, the method always returns the value of 'e' parameter.
     * <p/>
     * An example usage is as follows:
     * <code>
     * TreePath tp = ...;
     * Element e = checkElement(cu.getTrees().getElement(tp));
     * </code>
     * <p/>
     * <b>Note:</b> the method may be only called from within {@link #waitUserActionTask}, it aborts
     * the current processing under assumption the user task will be restarted. It is illegal to call the
     * method if not running as user task - IllegalStateException will be thrown.
     *
     * @param e the Element to check
     * @param s the source of the Element
     * @return the original Element, to support 'fluent' pattern
     *
     * @throws IllegalStateException if not called from within user task
     *
     */
    public static <T extends Element> T checkElement(@NonNull JavaSource s, @NonNull T e) {
        assert e != null;
        checkRetryContext();
        TypeMirror tm = e.asType();
        if (!isErrorKind(tm)) {
            return e;
        }
        if (shouldSignal()) {
            signalIncompleteData(s, e);
        }
        return e;
    }

    /**
     * Attempts to resolve Element, retries action task if Element might not be available because of running scan.
     * The method attempts to resolve the Handle to an Element. If it fails, or the Element is not
     * completed (e.g. its type was not scanned yet), the method will abort the user task and reschedules
     * it after parser finish.
     * <p/>
     * The method can be only used from {@link #waitUserActionTask}, {@link #postUserActionTask} as it throws an exception to interrupt
     * the process, which is processed by the mentioned methods. If the method is called in other context, {@link IllegalStateException}
     * will be thrown.
     * <p/>
     * Use this method if you want to not to fail computation if a referenced type might not be yet scanned.
     * Note that it is not guaranteed, even after task retry, that the Element will be resolved, or will be fully attributed. If the
     * handle points to Element which does not exist, or uses unknown type etc, the Element may be broken even after parsing finishes.
     * Always check the element for its kind and other properties.
     * 
     * @param s java source to work with
     * @param info CompilationInfo used for resolution
     * @param handle handle to resolve
     * 
     * @return resolved Element instance or null if the element could not be found or is incomplete.
     *
     * @see #waituserActionTask
     * @see #postUserActionTask
     * @see #isElementUsable
     * @throws IllegalStateException if not called from within {@link #waitUserActionTask} and the like
     */
    public static <T extends Element> T resolveElement(@NonNull JavaSource s, 
            @NonNull CompilationInfo info, @NonNull ElementHandle handle) {
        assert s != null;
        assert info != null;
        assert handle != null;
        assert info.getJavaSource() == null || info.getJavaSource() == s;
        checkRetryContext();
        T e = (T) handle.resolve(info);
        if (e != null) {
            TypeMirror tm = e.asType();
            if (!isErrorKind(tm)) {
                return e;
            }
        }
        if (shouldSignal()) {
            signalIncompleteData(s, handle);
        }
        return e;
    }
    
    private static void checkRetryContext() throws IllegalStateException {
        Boolean b = retryGuard.get();
        if (b == null) {
            throw new IllegalStateException("The method may be only called within SourceUtils.waitUserActionTask");
        }
    }
    
    private static void signalIncompleteData(JavaSource s, TreePathHandle handle) {
        checkRetryContext();
        throw new RetryWhenScanFinished(s, handle);
    }

    private static void signalIncompleteData(JavaSource s, TreePath path) {
        checkRetryContext();
        throw new RetryWhenScanFinished(s, path);
    }

    private static void signalIncompleteData(JavaSource s, String name) {
        checkRetryContext();
        throw new RetryWhenScanFinished(s, name);
    }

    /**
     * Aborts the user task and calls it again when parsing finishes, if a typename is not available.
     *
     * @param el the Element which causes the trouble
     * @param s source of the Element
     * @throws IllegalStateException if not called from within {@link #waitUserActionTask} and the like
     *
     */
    public static void signalIncompleteData(@NonNull JavaSource s, @NonNull ElementHandle handle) {
        assert handle != null;
        assert s != null;
        checkRetryContext();
        throw new RetryWhenScanFinished(s, handle);
    }

    private static void signalIncompleteData(JavaSource s, Element el) {
        assert el != null;
        checkRetryContext();
        throw new RetryWhenScanFinished(s, ElementHandle.create(el));
    }
    
    
    private static boolean isErrorKind(TypeMirror type) {
        return type != null && 
               (type.getKind() == TypeKind.ERROR || type.getKind() == TypeKind.OTHER);
    }

    
    /**
     * Guards usage of the abortAndRetry methods; since they throw an exception, which is 
     * only caught on specific places
     */
    private static final ThreadLocal<Boolean> retryGuard = new ThreadLocal<Boolean>();
    
    private final static class FinishedFuture implements Future<Void> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public String toString() {
            return "FinishedFuture";
        }
        
        
    }
    
    private static class TaskWrapper implements Task<CompilationController> {
        private Task<CompilationController> userTask;
        private AtomicReference<Throwable>  status;

        public TaskWrapper(Task<CompilationController> userTask, AtomicReference<Throwable> status) {
            this.userTask = userTask;
            this.status = status;
        }

        @Override
        public void run(CompilationController parameter) throws Exception {
            Boolean b = retryGuard.get();
            try {
                retryGuard.set(Boolean.TRUE);
                userTask.run(parameter);
            } catch (RetryWhenScanFinished ex) {
                // swallow Error, but signal error
                status.set(ex);
            } catch (Exception ex) {
                status.set(ex);
                throw ex;
            } finally {
                if (b == null) {
                    retryGuard.remove();
                } else {
                    retryGuard.set(b);
                }
            }
        }
    }

    /**
     * An Exception which indicates that the operation should be aborted, and
     * retried when parsing is finished. The exception derives from the {@link Error}
     * class to bypass ill-written code, which caught {@link RuntimeException} or
     * even {@link Exception}.
     * <p/>
     * The exception is interpreted by the Java Source infrastructure and is not
     * meant to be ever thrown or caught by regular application code. It's deliberately
     * package-private so users are not tempted to throw or catch it.
     */
    static class RetryWhenScanFinished extends Error {
        private ElementHandle   elHandle;
        private TreePathHandle  treeHandle;
        private String          typeName;
        private TreePath        elementPath;
        private JavaSource      jsrc;

        public RetryWhenScanFinished(JavaSource jsrc, String typeName) {
            this.jsrc = jsrc;
            this.typeName = typeName;
        }

        public RetryWhenScanFinished(JavaSource jsrc, TreePath elementPath) {
            this.jsrc = jsrc;
            this.elementPath = elementPath;
        }
        
        public RetryWhenScanFinished(JavaSource jsrc, ElementHandle elHandle) {
            this.jsrc = jsrc;
            this.elHandle = elHandle;
        }

        public RetryWhenScanFinished(JavaSource jsrc, TreePathHandle treeHandle) {
            this.jsrc = jsrc;
            this.treeHandle = treeHandle;
        }

        @Override
        public String toString() {
            return "RetryWhenScanFinished{" + "elHandle=" + elHandle + ", treeHandle=" + treeHandle + ", typeName=" + typeName + ", elementPath=" + elementPath + '}';
        }

    }
}
