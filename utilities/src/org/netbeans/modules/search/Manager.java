/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.lang.ref.Reference;
import java.lang.reflect.Method;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openidex.search.SearchGroup;

/**
 * Manager of the Search module's activities.
 * It knows which tasks are running and manages the module's actions so that
 * no two conflicting tasks are running at a moment.
 *
 * @see <a href="doc-files/manager-state-diagram.png">State diagram</a>
 * @author  Marian Petras
 */
final class Manager {

    static final int NO_TASK          = 0;
    
    static final int SEARCHING        = 0x01;
    
    static final int CLEANING_RESULT  = 0x02;

    static final int PRINTING_DETAILS = 0x04;
    
    static final int EVENT_SEARCH_STARTED = 1;
    
    static final int EVENT_SEARCH_FINISHED = 2;
    
    static final int EVENT_SEARCH_INTERRUPTED = 3;
    
    static final int EVENT_SEARCH_CANCELLED = 4;
    
    private static final Manager instance = new Manager();


    private final Object lock = new Object();

    private int state = NO_TASK;

    private int pendingTasks = 0;
    
    private TaskListener taskListener;
    
    private SearchTask currentSearchTask;

    private SearchTask pendingSearchTask;

    private PrintDetailsTask currentPrintDetailsTask;
    
    private PrintDetailsTask pendingPrintDetailsTask;

    private Task searchTask;

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
        synchronized (lock) {
            callOnWindowFromAWT("setResultModel", null);                //NOI18N
            if (currentSearchTask != null) {
                currentSearchTask.stop(false);
            }
            if (resultModelToClean != null) {
                pendingTasks |= CLEANING_RESULT;
            }
            pendingTasks |= SEARCHING;
            pendingSearchTask = task;
            if (state == NO_TASK) {
                processNextPendingTask();
            } else {
                notifySearchPending(state);                     //invariant #1
            }
        }
    }
    
    /**
     */
    void schedulePrintingDetails(ResultTreeChildren children,
                                 SearchGroup searchGroup) {
        synchronized (lock) {
            assert state == NO_TASK;
            pendingTasks |= PRINTING_DETAILS;
            
            pendingPrintDetailsTask = new PrintDetailsTask(
                    children.getNodes(),
                    searchGroup);
            processNextPendingTask();
        }
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
     * Calls a given method on the Search Results window, from the AWT thread.
     *
     * @param  methodName  name of the method to be called
     */
    private void callOnWindowFromAWT(final String methodName) {
        Method theMethod;
        try {
            theMethod = ResultView.class
                        .getDeclaredMethod(methodName, new Class[0]);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException();
        }
        callOnWindowFromAWT(theMethod, null);
    }
    
    /**
     * Calls a given method on the Search Results window, from the AWT thread.
     *
     * @param  methodName  name of the method to be called
     * @param  param  parameter to be passed to the method
     */
    private void callOnWindowFromAWT(final String methodName,
                                     final Object param) {
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
        callOnWindowFromAWT(theMethod, new Object[] {param});
    }
    
    /**
     */
    private void callOnWindowFromAWT(final Method method,
                                     final Object[] params) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                final ResultView resultViewInstance = ResultView.getInstance();
                try {
                    method.invoke(resultViewInstance, params);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        });
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
        synchronized (lock) {
            searchWindowOpen = false;
            
            callOnWindowFromAWT("setResultModel", null);                //NOI18N
            if (currentSearchTask != null) {
                currentSearchTask.stop(false);
            }
            if (resultModelToClean != null) {
                pendingTasks |= CLEANING_RESULT;
            }
            pendingTasks &= ~SEARCHING;
            pendingSearchTask = null;
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
            } else {
                assert pendingTasks == 0;
            }
        }
    }

    /**
     */
    private void startSearching() {
        synchronized (lock) {
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
