/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;
import org.netbeans.modules.uml.ui.support.ProductHelper;

import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;

/**
 *  author Craig Conover, craig.conover@sun.com
 */
public abstract class AbstractNBTask extends Thread 
    implements Cancellable, ITaskSupervisor
{
    private AggregateProgressHandle progressHandle;
    protected ProgressContributor[] progressContribs;
    
    protected boolean cancelled = false;
    protected long start;
    private IProject m_CurrentProject;
    protected boolean success = true;
    private InputOutput inputOutput;
    private PrintWriter out;
    private HashMap<String, Object> taskSettings = new HashMap<String, Object>();
    private String timeMsg;
    private int contribCount = -1;
    private int counter = 0;
    private int logLevel = SUMMARY;
    private boolean logging = false;
    
    public final static String SETTING_KEY_TASK_NAME = "TASK_NAME"; // NOI18N
    public final static String SETTING_KEY_TOTAL_ITEMS = "TOTAL_ITEMS"; // NOI18N
    public final static String SETTING_KEY_DISPLAY_OUTPUT = "DISPLAY_OUTPUT"; // NOI18N

    public AbstractNBTask()
    {
        initialize();
    }
    
    public AbstractNBTask(ITaskFinishListener listener)
    {
        initialize();
        addListener(listener);
    }

    public AbstractNBTask(HashMap settings)
    {
        taskSettings = settings;
        initialize();
    }

    public AbstractNBTask(HashMap settings, ITaskFinishListener listener)
    {
        taskSettings = settings;
        initialize();
        addListener(listener);
    }

    
    public void run()
    {
        if (progressContribs != null && progressContribs.length > 0)
        {
            progressHandle = AggregateProgressFactory.createHandle(
                getTaskName(), progressContribs, this, null);
        }
        
        try
        {
            beginTask();
        }
        
        catch(Exception e)
        {
            fail();
            ErrorManager.getDefault().notify(e);
        }
        
        finally
        {
            finishTask();
            progressHandle.finish();
        }
        
    }
    
    
    public boolean cancel()
    {
        cancelled = true;
        return true;
    }


    // methods that should be implemented or overriden by subclass
    /////////////////////////////////////////////////////////////////////

    protected abstract void initTask();
    protected abstract void begin();
    protected abstract void finish();
    
    /**
     *  This method logs a "header" message. Override to replace this message
     *  with a custom header message and invoke super to prepend or append
     *  to this message.
     */
    protected void beginLog()
    {
        String msg = getBundleMessage("MSG_Begin_Processing") // NOI18N
            + " " + getTaskName(); // NOI18N

        if (getTotalItems() > 0)
            msg += ": " + getTotalItems() + " "  // NOI18N
                + getBundleMessage("MSG_Items"); // NOI18N
        
        log(SUMMARY, msg);
        log(SUMMARY);
    }
    
    /**
     *  This method logs the finish status message (success, fail or cancel)
     *  and the run time stats. Override to replace this message
     *  with a custom header message and invoke super to prepend or append
     *  to this message.
     */
    protected void finishLog()
    {
        log(SUMMARY); // NOI18N
        log(SUMMARY, "================================"); // NOI18N
        
        if (cancelled)
            log(NbBundle.getMessage(
                    AbstractNBTask.class, "MSG_Report_Cancelled") + " " + // NOI18N
                NbBundle.getMessage(
                    AbstractNBTask.class, "MSG_TotalTime", timeMsg)); // NOI18N
        
        else if (success)
            log(NbBundle.getMessage(
                    AbstractNBTask.class, "MSG_Report_Successful") + " " + // NOI18N
                NbBundle.getMessage(
                    AbstractNBTask.class, "MSG_TotalTime", timeMsg)); // NOI18N
        
        else
            log(NbBundle.getMessage(
                    AbstractNBTask.class, "MSG_Report_Failed") + " " + // NOI18N
                NbBundle.getMessage(
                    AbstractNBTask.class, "MSG_TotalTime", timeMsg)); // NOI18N
    }

    /**
     * Provides tokenized defaults.
     * Should be overriden by subclass or settings values 
     * passed in by calling class.
     */
    protected void initDefaultSettings()
    {
        taskSettings.put(SETTING_KEY_TASK_NAME, 
            "<" + getBundleMessage("MSG_Default_Task_Name") + ">"); // NOI18N
        
        taskSettings.put(SETTING_KEY_TOTAL_ITEMS, new Integer(-1));
    }

    
    // public methods that should be invoked by subclass
    ////////////////////////////////////////////////////
    
    public boolean start(int totalItems)
    {
        return start(++contribCount, totalItems);
    }

    public boolean start(int contributor, int totalItems)
    {
        if (contributor < 0 && contributor > progressContribs.length-1)
        {
            finishTask();
            return false;
        }
        
        if (contributor > 0)
            progressContribs[contributor].finish();
        
        counter = 0;
        
        progressHandle.setDisplayName(getTaskName() + ": " + // NOI18N
            progressContribs[contributor].getTrackingId());

        progressContribs[contributor].start(totalItems);
        
        return true;
    }
    
    public int increment()
    {
        return ++counter;
    }
    
    public int increment(int step)
    {
        counter += step;
        return counter;
    }
    
    /**
     *  Called by task subclass to check confirm that the task hasn't been
     *  cancelled or failed. If there is a cancellation or failure, finish()
     *  is called.
     *
     *  @return true if the process hasn't failed or been canceled
     */
    public boolean proceed()
    {
        return proceed(0);
    }
        
    /**
     *  Called by task subclass to check confirm that the task hasn't been
     *  cancelled or failed. If there is a cancellation or failure, finish()
     *  is called.
     *
     *  @param count the number of items processed so far; used for % done
     *               calculation for the NB progress bar
     *
     *  @param step the amount to increment the counter by.
     *
     *  @return true if the process hasn't failed or been canceled
     */
    public boolean proceed(int step)
    {
        if (cancelled || !success)
        {
            progressContribs[contribCount].finish();
            finishTask();
            return false;
        }
        
        else if (counter > -1)
        {
            if (step > 0)
                increment(step);
            
            log(DEBUG, "count " + counter);
            progressContribs[contribCount].progress(counter);
        }
        
        return true;
    }
    
    
    public void setLogLevel(int level)
    {
        logLevel = level;
    }

    public int getLogLevel()
    {
        return logLevel;
    }

    public boolean isLogging()
    {
        return logging;
    }

    public void setLogging(boolean val)
    {
        logging = val;
    }
    
    /**
     * Outputs a blank line
     */
    public void log()
    {
        log("", true);
    }

    public void log(int level)
    {
        if (logLevel < level)
            return;

        log("", true);
    }

    public void log(int level, String msg)
    {
        if (logLevel < level)
            return;

        log(msg, true);
    }   
    
    public void log(int level, String msg, boolean newline)
    {
        if (logLevel < level)
            return;
        
        log(msg, newline);
    }   
    
    /**
     * Outputs a message with and appends newline by default
     * 
     * @param msg the message to be output
     */
    public void log(String msg)
    {
        log(msg, true);
    }

    /**
     * Outputs a message
     * 
     * @param msg the message to be output
     * @param newline if true, appends newline
     */
    public void log(String msg, boolean newline)
    {
        if (newline)
            out.println(msg);
        
        else
            out.print(msg);
        
        out.flush();
    }
    
    /**
     *  Call this method when a failure in your task is detected and it will
     *  set the success flag to false. The next time proceed() is called,
     *  it will invoke finish() return false.
     */
    public void fail()
    {
        success = false;
    }
    
    // settings methods
    
    public String getTaskName()
    {
        return (String)getSetting(SETTING_KEY_TASK_NAME);
    }
    
    public void setTaskName(String taskName)
    {
        setSetting(SETTING_KEY_TASK_NAME, taskName);
    }
    
    public int getTotalItems()
    {
        Integer total = (Integer)getSetting(SETTING_KEY_TOTAL_ITEMS);
        
        if (total == null)
            return -1;
        
        return total.intValue();
    }
    
    public void setTotalItems(int total)
    {
        setSetting(SETTING_KEY_TOTAL_ITEMS, new Integer(total));
    }

    public boolean isDisplayOutput()
    {
        Boolean show = (Boolean)getSetting(SETTING_KEY_DISPLAY_OUTPUT);
        
        if (show == null)
            return true;
        
        return show.booleanValue();
    }
    
    public void setDisplayOutput(boolean val)
    {
        setSetting(SETTING_KEY_DISPLAY_OUTPUT, new Boolean(val));
    }
    
    public Object getSetting(String key)
    {
        return taskSettings.get(key);
    }

    public void setSetting(String key, Object value)
    {
        taskSettings.put(key, value);
    }
    
    
    public void addListener(ITaskFinishListener listener)
    {
        if (!listeners.contains(listener))
            listeners.addElement(listener);
    }
    
    public void removeListener(ITaskFinishListener listener)
    {
        listeners.removeElement(listener);
    }
    
    
    // private methods
    //////////////////
    
    private void initialize()
    {
        if (taskSettings == null)
            initDefaultSettings();

        initTask();
        initLog();
    }

    
    private void initLog()
    {
        TopComponent tc = 
            WindowManager.getDefault().findTopComponent("output"); // NOI18N

        if (tc != null && isDisplayOutput()) // NOI18N
        {
            tc.open();
            tc.requestActive();
            tc.toFront();
        }
        
        inputOutput = IOProvider.getDefault().getIO(
            getTaskName() + " " + getBundleMessage("MSG_Log"), false); // NOI18N

        try
        {
            inputOutput.getOut().reset();
            out = inputOutput.getOut();
        }

        catch(IOException e)
        {
            // TODO: ignore
        }

        if (isDisplayOutput()) // NOI18N
        {
            inputOutput.select();
            inputOutput.setOutputVisible(true);
        }
    }

    
    private void beginTask()
    {
        if (getTotalItems() > -1)
            progressHandle.start(getTotalItems());
        
        else
            progressHandle.start();
        
        start = System.currentTimeMillis();
        
        beginLog();
        begin();
    }
    
    
    private void finishTask()
    {
        progressContribs[progressContribs.length-1].finish();
        finish();
        
        long total = (System.currentTimeMillis() - start)/1000;
        int minutes = (int)total/60;
        int seconds = (int)(total-minutes*60);

        timeMsg = minutes == 0 
            ? seconds + " " + getBundleMessage("MSG_Seconds") // NOI18N
            : minutes + " " + getBundleMessage("MSG_Minutes") + // NOI18N
                " "+ seconds + " " + getBundleMessage("MSG_Seconds"); // NOI18N

        finishLog();
        
        inputOutput.getOut().flush();
        inputOutput.getOut().close();
        out.close();
        notifyTaskFinishListeners();
    }
    
    // bundle message helper methods
    
    private String getBundleMessage(String key)
    {
        return NbBundle.getMessage(AbstractNBTask.class, key);
    }
    
    private String getBundleMessage(String key, Object[] params)
    {
        return NbBundle.getMessage(AbstractNBTask.class, key, params);
    }

    private String getBundleMessage(String key, Object param)
    {
        return NbBundle.getMessage(AbstractNBTask.class, key, param);
    }
    
    private String getBundleMessage(String key, Object param, Object param0)
    {
        return NbBundle.getMessage(AbstractNBTask.class, key, param, param0);
    }
    
    private String getBundleMessage(
        String key, Object param, Object param0, Object param1)
    {
        return NbBundle.getMessage(
            AbstractNBTask.class, key, param, param0, param1);
    }

    private Vector<ITaskFinishListener> listeners = 
        new Vector<ITaskFinishListener>();
    
    private void notifyTaskFinishListeners()
    {
        for (ITaskFinishListener listener: listeners)
            listener.taskFinished();
    }

}
