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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openidex.search.SearchGroup;
import static org.netbeans.modules.search.ReplaceTask.ResultStatus.SUCCESS;
import static org.netbeans.modules.search.ReplaceTask.ResultStatus.PRE_CHECK_FAILED;
import static org.netbeans.modules.search.ReplaceTask.ResultStatus.PROBLEMS_ENCOUNTERED;

/**
 * Manager of the Search module's activities.
 * It knows which tasks are running and manages the module's actions so that
 * no two conflicting tasks are running at a moment.
 *
 * @see <a href="doc-files/manager-state-diagram.png">State diagram</a>
 * @author  Marian Petras
 */
final class Manager {
    
    /**
     * timeout for cleanup in the case that the module is being uninstalled
     * (in milliseconds)
     */
    private static final int CLEANUP_TIMEOUT_MILLIS = 3000;
    

    static final int NO_TASK          = 0;
    
    static final int SEARCHING        = 0x01;
    
    static final int CLEANING_RESULT  = 0x02;

    static final int PRINTING_DETAILS = 0x04;
    
    static final int REPLACING        = 0x08;
    
    static final int EVENT_SEARCH_STARTED = 1;
    
    static final int EVENT_SEARCH_FINISHED = 2;
    
    static final int EVENT_SEARCH_INTERRUPTED = 3;
    
    static final int EVENT_SEARCH_CANCELLED = 4;
    
    private static final Manager instance = new Manager();
    
    
    private boolean moduleBeingUninstalled = false;


    private final Object lock = new Object();

    private int state = NO_TASK;

    private int pendingTasks = 0;
    
    private TaskListener taskListener;
    
    private SearchTask currentSearchTask;
    
    private SearchTask pendingSearchTask;

    private SearchTask lastSearchTask;
    
    private ReplaceTask currentReplaceTask;
    
    private ReplaceTask pendingReplaceTask;

    private PrintDetailsTask currentPrintDetailsTask;
    
    private PrintDetailsTask pendingPrintDetailsTask;

    private Task searchTask;
    
    private Task replaceTask;

    private Task cleanResultTask;
    
    private Task printDetailsTask;

    private ResultModel resultModelToClean;

    private boolean searchWindowOpen = false;
    
    private Reference outputWriterRef;
    

    /**
     */
    static Manager getInstance() {
        return instance;
    }

    /**
     */
    private Manager() { }
    
    /*
     * INVARIANTS:
     * #1: If the Search Results window is open, its root node displays:
     *     - if the search task is in progress:
     *             - summary of results
     *     - if the Search module is inactive:
     *             - summary of current results (continuously updated)
     *     - if the search task in scheduled but another task is blocking it:
     *             - name of the current task blocking the search
     * #2: At most one result model exists at a single moment.
     */

    /**
     */
    void scheduleSearchTask(SearchTask task) {
        assert EventQueue.isDispatchThread();
        
        synchronized (lock) {
            ResultView.getInstance().setResultModel(null);
            if (currentSearchTask != null) {
                currentSearchTask.stop(false);
            }
            if (resultModelToClean != null) {
                pendingTasks |= CLEANING_RESULT;
            }
            pendingTasks |= SEARCHING;
            pendingSearchTask = task;
            lastSearchTask = task;
            if (state == NO_TASK) {
                processNextPendingTask();
            } else {
                notifySearchPending(state);                     //invariant #1
            }
        }
    }
    
    /**
     */
    void scheduleReplaceTask(ReplaceTask task) {
        assert EventQueue.isDispatchThread();
        
        synchronized (lock) {
            assert (state == NO_TASK) && (pendingTasks == 0);
            
            pendingTasks |= REPLACING;
            pendingReplaceTask = task;
            processNextPendingTask();
        }
    }
    
    /**
     */
    void scheduleSearchTaskRerun() {
        assert EventQueue.isDispatchThread();
        
        synchronized (lock) {
            SearchTask newSearchTask = lastSearchTask.createNewGeneration();
            lastSearchTask = null;
            scheduleSearchTask(newSearchTask);
        }
    }
    
    /**
     */
    void schedulePrintingDetails(Object[] matchingObjects,
                                 SearchGroup searchGroup) {
        synchronized (lock) {
            assert state == NO_TASK;
            pendingTasks |= PRINTING_DETAILS;
            
            pendingPrintDetailsTask = new PrintDetailsTask(
                    matchingObjects,
                    searchGroup);
            processNextPendingTask();
        }
    }
    
    /**
     * Queries whether the user should be allowed to initiate a new search.
     * For example, the user should not be allowed to do so if the last
     * replace action has not finished yet.
     * 
     * @return  message to the user, describing the reason why a new search
     *          cannot be started, or {@code null} if there is no such reason
     *          (i.e. if a new search may be started)
     */
    String mayStartSearching() {
        boolean replacing;
        
        synchronized (lock) {
            replacing = (state == REPLACING);
        }
        
        String msgKey = replacing ? "MSG_Cannot_start_search__replacing"//NOI18N
                                  : null;
        return (msgKey != null) ? NbBundle.getMessage(getClass(), msgKey)
                                : null;
    }
    
    /**
     */
    private void notifySearchStarted() {
        notifySearchTaskStateChange(EVENT_SEARCH_STARTED);
    }
    
    /**
     */
    private void notifySearchFinished() {
        notifySearchTaskStateChange(EVENT_SEARCH_FINISHED);
    }
    
    /**
     */
    private void notifySearchInterrupted() {
        notifySearchTaskStateChange(EVENT_SEARCH_INTERRUPTED);
    }
    
    /**
     */
    private void notifySearchCancelled() {
        notifySearchTaskStateChange(EVENT_SEARCH_CANCELLED);
    }
    
    /**
     * Notifies the result window of a search task's state change.
     *
     * @param  changeType  constant describing what happened
     *                     - one of the EVENT_xxx constants
     */
    private void notifySearchTaskStateChange(final int changeType) {
        synchronized (lock) {
            if (!searchWindowOpen) {
                return;
            }
        }
        callOnWindowFromAWT("searchTaskStateChanged",                   //NOI18N
                          new Integer(changeType));
    }

    /**
     */
    private void notifySearchPending(final int blockingTask) {
        if (!searchWindowOpen) {
            return;
        }
        callOnWindowFromAWT("notifySearchPending",                      //NOI18N
                          new Integer(blockingTask));
    }
    
    /**
     */
    private void notifyReplaceFinished() {
        assert Thread.holdsLock(lock);
        assert currentReplaceTask != null;
        
        ReplaceTask.ResultStatus resultStatus
                = currentReplaceTask.getResultStatus();
        if (resultStatus == SUCCESS) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(getClass(), "MSG_Success"));    //NOI18N
            if (searchWindowOpen) {
                callOnWindowFromAWT("closeAndSendFocusToEditor", false);//NOI18N
            }
        } else {
            String msgKey = (resultStatus == PRE_CHECK_FAILED)
                            ? "MSG_Issues_found_during_precheck"        //NOI18N
                            : "MSG_Issues_found_during_replace";        //NOI18N
            String title = NbBundle.getMessage(getClass(), msgKey);
            displayIssuesFromAWT(title,
                                 currentReplaceTask.getProblems());
            if (resultStatus == PRE_CHECK_FAILED) {
                offerRescanAfterIssuesFound();
            }
        }
    }
    
    /**
     */
    private void offerRescanAfterIssuesFound() {
        assert Thread.holdsLock(lock);
        assert currentReplaceTask != null;
        
        String msg = NbBundle.getMessage(getClass(),
                                         "MSG_IssuesFound_Rescan_");    //NOI18N
        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                            msg,
                                            NotifyDescriptor.QUESTION_MESSAGE);
        String rerunOption = NbBundle.getMessage(getClass(),
                                                 "LBL_Rerun");          //NOI18N
        nd.setOptions(new Object[] {rerunOption,
                                    NotifyDescriptor.CANCEL_OPTION});
        Object dlgResult = DialogDisplayer.getDefault().notify(nd);
        if (rerunOption.equals(dlgResult)) {
            /*
             * The rescan method calls 'scheduleSearchTaskRerun()' on this.
             * But it will wait until 'taskFinished()' returns, which is
             * exactly what we need to keep consistency of the manager's fields
             * like 'currentReplaceTask', 'replaceTask' and 'state'.
             * Using this mechanism also requires that, when sending a method
             * to the EventQueue thread, we use invokeLater(...) and not
             * invokeAndWait(...).
             */
            callOnWindowFromAWT("rescan", false);                       //NOI18N
        }
    }
    
    /**
     */
    private void notifyPrintingDetailsFinished() {
        if (!searchWindowOpen) {
            return;
        }
        callOnWindowFromAWT("showAllDetailsFinished");                  //NOI18N
    }

    /**
     */
    private void activateResultWindow() {
        Method theMethod;
        try {
            theMethod = ResultView.class
                        .getMethod("requestActive", new Class[0]);      //NOI18N
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException();
        }
        callOnWindowFromAWT(theMethod, null);
    }
    
    /**
     */
    private void displayIssuesFromAWT(String title, String[] issues) {
        Method theMethod;
        try {
            theMethod = ResultView.class.getDeclaredMethod(
                                                "displayIssuesToUser",  //NOI18N
                                                String.class, String[].class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        callOnWindowFromAWT(theMethod, new Object[] {title, issues}, false);
    }
    
    /**
     * Calls a given method on the Search Results window, from the AWT thread.
     *
     * @param  methodName  name of the method to be called
     */
    private void callOnWindowFromAWT(final String methodName) {
        callOnWindowFromAWT(methodName, true);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final String methodName,
                                     final boolean wait) {
        Method theMethod;
        try {
            theMethod = ResultView.class
                        .getDeclaredMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException();
        }
        callOnWindowFromAWT(theMethod, null, wait);
    }
    
    /**
     * Calls a given method on the Search Results window, from the AWT thread.
     *
     * @param  methodName  name of the method to be called
     * @param  param  parameter to be passed to the method
     */
    private void callOnWindowFromAWT(final String methodName,
                                     final Object param) {
        callOnWindowFromAWT(methodName, param, true);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final String methodName,
                                     final Object param,
                                     final boolean wait) {
        Method theMethod = null;
        Method[] methods = ResultView.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(methodName)) {
                Class[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Class paramType = parameterTypes[0];
                    if ((param == null
                               && !paramType.isPrimitive())
                            || (paramType == Integer.TYPE)
                               && (param instanceof Integer)
                            || parameterTypes[0].isInstance(param)) {
                        theMethod = method;
                    }
                }
            }
        }
        if (theMethod == null) {
            throw new IllegalArgumentException();
        }
        callOnWindowFromAWT(theMethod, new Object[] {param}, wait);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final Method method,
                                     final Object[] params) {
        callOnWindowFromAWT(method, params, true);
    }
    
    /**
     */
    private void callOnWindowFromAWT(final Method method,
                                     final Object[] params,
                                     final boolean wait) {
        Runnable runnable = new Runnable() {
            public void run() {
                final ResultView resultViewInstance = ResultView.getInstance();
                try {
                    method.invoke(resultViewInstance, params);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            if (wait) {
                try {
                    EventQueue.invokeAndWait(runnable);
                } catch (InvocationTargetException ex1) {
                    ErrorManager.getDefault().notify(ex1);
                } catch (Exception ex2) {
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex2);
                }
            } else {
                EventQueue.invokeLater(runnable);
            }
        }
    }
    
    /**
     */
    void searchWindowOpened() {
        synchronized (lock) {
            searchWindowOpen = true;
        }
    }

    /**
     */
    void searchWindowClosed() {
        assert EventQueue.isDispatchThread();
        
        synchronized (lock) {
            searchWindowOpen = false;
            
            if (moduleBeingUninstalled) {
                return;
            }
            
            if (currentSearchTask != null) {
                currentSearchTask.stop(false);
            }
            if (resultModelToClean != null) {
                pendingTasks |= CLEANING_RESULT;
            }
            pendingTasks &= ~SEARCHING;
            pendingSearchTask = null;
            lastSearchTask = null;
            if (state == NO_TASK) {
                processNextPendingTask();
            }
        }
    }
    
    /**
     */
    private void processNextPendingTask() {
        synchronized (lock) {
            assert state == NO_TASK;
            if (resultModelToClean == null) {
                pendingTasks &= ~CLEANING_RESULT;
            }
            if ((pendingTasks & PRINTING_DETAILS) != 0) {
                if ((pendingTasks & SEARCHING) != 0) {
                    notifySearchPending(PRINTING_DETAILS);      //invariant #1
                }
                startPrintingDetails();
            } else if ((pendingTasks & CLEANING_RESULT) != 0) {
                if ((pendingTasks & SEARCHING) != 0) {
                    notifySearchPending(CLEANING_RESULT);       //invariant #1
                }
                startCleaning();
            } else if ((pendingTasks & SEARCHING) != 0) {
                startSearching();
            } else if ((pendingTasks & REPLACING) != 0) {
                startReplacing();
            } else {
                assert pendingTasks == 0;
            }
        }
    }

    /**
     */
    private void startSearching() {
        synchronized (lock) {
            assert pendingSearchTask != null;
            
            notifySearchStarted();
            
            ResultModel resultModel = pendingSearchTask.getResultModel();
            callOnWindowFromAWT("setResultModel",                       //NOI18N
                                resultModel);
            resultModelToClean = resultModel;

            if (outputWriterRef != null) {
                SearchDisplayer.clearOldOutput(outputWriterRef);
                outputWriterRef = null;

                /*
                 * The following is necessary because clearing the output window
                 * activates the output window:
                 */
                activateResultWindow();
            }
            
            RequestProcessor.Task task;
            task = RequestProcessor.getDefault().create(pendingSearchTask);
            task.addTaskListener(getTaskListener());
            task.schedule(0);
            
            currentSearchTask = pendingSearchTask;
            pendingSearchTask = null;

            searchTask = task;
            pendingTasks &= ~SEARCHING;
            state = SEARCHING;
        }
    }
    
    /**
     */
    private void startReplacing() {
        synchronized (lock) {
            assert pendingReplaceTask != null;
            
            RequestProcessor.Task task;
            task = RequestProcessor.getDefault().create(pendingReplaceTask);
            task.addTaskListener(getTaskListener());
            task.schedule(0);
            
            currentReplaceTask = pendingReplaceTask;
            pendingReplaceTask = null;

            replaceTask = task;
            pendingTasks &= ~REPLACING;
            state = REPLACING;
        }
    }
    
    /**
     */
    private void startPrintingDetails() {
        synchronized (lock) {
            if (outputWriterRef != null) {
                SearchDisplayer.clearOldOutput(outputWriterRef);
                outputWriterRef = null;
            }

            RequestProcessor.Task task;
            task = RequestProcessor.getDefault()
                   .create(pendingPrintDetailsTask);
            task.addTaskListener(getTaskListener());
            task.schedule(0);
            
            printDetailsTask = task;
            pendingTasks &= ~PRINTING_DETAILS;
            currentPrintDetailsTask = pendingPrintDetailsTask;
            pendingPrintDetailsTask = null;
            
            state = PRINTING_DETAILS;
        }
    }
    
    /**
     */
    private void startCleaning() {
        synchronized (lock) {
            Runnable cleaner = new CleanTask(resultModelToClean);
            resultModelToClean = null;
            
            RequestProcessor.Task task;
            task = RequestProcessor.getDefault().create(cleaner);
            task.addTaskListener(getTaskListener());
            task.schedule(0);
            
            cleanResultTask = task;
            pendingTasks &= ~CLEANING_RESULT;
            state = CLEANING_RESULT;
        }
    }

    /**
     */
    void stopSearching() {
        synchronized (lock) {
            if ((pendingTasks & SEARCHING) != 0) {
                pendingTasks &= ~SEARCHING;
                pendingSearchTask = null;
                notifySearchCancelled();
            } else if (currentSearchTask != null) {
                currentSearchTask.stop();
            }
        }
    }

    /**
     */
    private void taskFinished(Task task) {
        synchronized (lock) {
            if (moduleBeingUninstalled) {
                allTasksFinished();
                return;
            }
            
            if (task == searchTask) {
                assert state == SEARCHING;
                if (currentSearchTask.notifyWhenFinished()) {
                    if (currentSearchTask.wasInterrupted()) {
                        notifySearchInterrupted();
                    } else {
                        notifySearchFinished();
                    }
                }
                currentSearchTask = null;
                searchTask = null;
                state = NO_TASK;
            } else if (task == replaceTask) {
                assert state == REPLACING;
                notifyReplaceFinished();
                currentReplaceTask = null;
                replaceTask = null;
                state = NO_TASK;
            } else if (task == cleanResultTask) {
                assert state == CLEANING_RESULT;
                cleanResultTask = null;
                state = NO_TASK;
            } else if (task == printDetailsTask) {
                assert state == PRINTING_DETAILS;
                notifyPrintingDetailsFinished();

                outputWriterRef = currentPrintDetailsTask.getOutputWriterRef();
                currentPrintDetailsTask = null;
                printDetailsTask = null;
                state = NO_TASK;
            } else {
                assert false;
            }
            processNextPendingTask();
        }
    }
    
    /**
     * Called only if the module is about to be uninstalled.
     * This method is called at the moment that there are no active tasks
     * (searching, printing details, etc.) and the module is ready for
     * final cleanup.
     */
    private void allTasksFinished() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
    
    /**
     * Called from the <code>Installer</code> to notify that the module
     * is being uninstalled.
     * Calling this method sets a corresponding flag. When the flag is set,
     * no new actions (cleaning results, printing details, etc.) are started
     * and the behaviour is changed so that manipulation with the ResultView
     * is reduced or eliminated. Also, if no tasks are currently active,
     * immediatelly closes the results window; otherwise it postpones closing
     * the window until the currently active task(s) finish.
     */
    void doCleanup() {
        synchronized (lock) {
            moduleBeingUninstalled = true;
            if (state != NO_TASK) {
                if (currentSearchTask != null) {
                    currentSearchTask.stop(false);
                }
                if (currentPrintDetailsTask != null) {
                    currentPrintDetailsTask.stop();
                }
                try {
                    lock.wait(CLEANUP_TIMEOUT_MILLIS);
                } catch (InterruptedException ex) {
                    ErrorManager.getDefault().notify(
                            ErrorManager.EXCEPTION,
                            ex);
                }
            }
            callOnWindowFromAWT("closeResults");                        //NOI18N
        }
    }
    
    /**
     */
    private TaskListener getTaskListener() {
        if (taskListener == null) {
            taskListener = new MyTaskListener();
        }
        return taskListener;
    }


    /**
     */
    private class MyTaskListener implements TaskListener {

        /**
         */
        MyTaskListener() {
            super();
        }

        /**
         */
        public void taskFinished(Task task) {
            synchronized (lock) {
                Manager.this.taskFinished(task);
            }
        }

    }
    
}
