/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools;

/*
 * XTestExecutor.java
 *
 * Created on April 29, 2002, 10:54 AM
 */

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.openide.TopManager;
import org.openide.ServiceType;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.windows.InputOutput;
import org.openide.execution.Executor;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;

import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.modules.testtools.wizards.WizardIterator;

/** Executor for XTest Workspace Build Script Data Object
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestExecutor extends Executor {
    
    static final long serialVersionUID = -5490616206437129681L;    
    
    /** Holds value of property netbeansHome. */
    private File netbeansHome;

    /** Holds value of property xtestHome. */
    private File xtestHome;
    
    /** Holds value of property jemmyHome. */
    private File jemmyHome;
    
    /** Holds value of property jellyHome. */
    private File jellyHome;
    
    /** Holds value of property testType. */
    private String testType="";
    
    /** Holds value of property attributes. */
    private String attributes="";
    
    /** Holds value of property showResults. */
    private boolean showResults=true;
    
    /** creates new XTestExecutor */    
    public XTestExecutor() {
        String home=System.getProperty("netbeans.home");
        if (!new File(home+File.separator+"xtest-distribution").exists()) 
            home=System.getProperty("netbeans.user");
        xtestHome=new File(home+File.separator+"xtest-distribution");
        jemmyHome=new File(home+File.separator+"lib"+File.separator+"ext");
        jellyHome=new File(home+File.separator+"lib"+File.separator+"ext");
    }
    
    /** creates new XTestEecutor, fills and returns propper Handler
     * @return ServiceType.Handle */    
    public static ServiceType.Handle getHandle() {
        return new ServiceType.Handle(new XTestExecutor());
    }
    
    /** throws "Not yet implemented" IOException
     * @param info ExecInfo
     * @throws IOException "Not yet implemented" IOException
     * @return never returns value */    
    public ExecutorTask execute(ExecInfo info) throws IOException {
        throw new IOException("Not yet implemented.");
    }
    
    /** performs execution of given DataObject
     * @param obj DataObject
     * @throws IOException when some IO problems
     * @return ExecutorTask */    
    public ExecutorTask execute(DataObject obj) throws IOException {
        AntProjectCookie cookie=(AntProjectCookie)obj.getCookie(AntProjectCookie.class);
        if (cookie==null) {
            throw new IOException("Missing Ant Project Cookie.");
        }
        if (netbeansHome==null || XTestCompilerType.netHome.equals(netbeansHome)) {
            File home=WizardIterator.showFileChooser(TopManager.getDefault().getWindowManager().getMainWindow(), "Select Tested Netbeans Home Directory (different than current)", true, false);
            if ((home!=null)&&(!XTestCompilerType.netHome.equals(home)))
                setNetbeansHome(home);
            else
                throw new IOException("Home directory of tested IDE must be set.");
        }
        TargetExecutor executor = new TargetExecutor(cookie, new String[]{"all"});
        executor.addProperties(getProperties());
        if (showResults)
            return showResults(executor.execute(), obj);
        else
            return executor.execute();
    }
    
    private ExecutorTask showResults(final ExecutorTask task, final DataObject obj) {
        Thread t=new Thread(new Runnable() {
            public void run() {
                if (task.result()==0) {
                    try {
                        FileObject fo=obj.getFolder().getPrimaryFile();
                        fo=fo.getFileObject("results");
                        fo=fo.getFileObject("index", "html");
                        TopManager.getDefault().showUrl(fo.getURL());
                    } catch (Exception e) {}
                }
            }
        });
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        return task;
    }
    
    /** returns Help Context
     * @return HelpCtx */    
    public HelpCtx getHelpCtx() {
        return new HelpCtx (XTestExecutor.class);
    }
    
    /** Getter for property netbeansHome.
     * @return Value of property netbeansHome.
     */
    public File getNetbeansHome() {
        return this.netbeansHome; 
    }
    
    /** Setter for property netbeansHome.
     * @param netbeansHome New value of property netbeansHome.
     */
    public void setNetbeansHome(File netbeansHome) {
        File old=this.netbeansHome;
        this.netbeansHome = netbeansHome;
        firePropertyChange("netbeansHome", old, netbeansHome);
    }
    
    /** Getter for property xtestHome.   
     * @return Value of property xtestHome.
     */
    public File getXtestHome() {
        return this.xtestHome;
    }
    
    /** Setter for property xtestHome.
     * @param xtestHome New value of property xtestHome.
     */
    public void setXtestHome(File xtestHome) {
        File old=this.xtestHome;
        this.xtestHome = xtestHome;
        firePropertyChange("xtestHome", old, xtestHome);
    }
    
    /** Getter for property jemmyHome.
     * @return Value of property jemmyHome.
     */
    public File getJemmyHome() {
        return this.jemmyHome;
    }
    
    /** Setter for property jemmyHome.
     * @param jemmyHome New value of property jemmyHome.
     */
    public void setJemmyHome(File jemmyHome) {
        File old=this.jemmyHome;
        this.jemmyHome = jemmyHome;
        firePropertyChange("jemmyHome", old, jemmyHome);
    }
    
    /** Getter for property jellyHome.
     * @return Value of property jellyHome.
     */
    public File getJellyHome() {
        return this.jellyHome;
    }
    
    /** Setter for property jellyHome.
     * @param jellyHome New value of property jellyHome.
     */
    public void setJellyHome(File jellyHome) {
        File old=this.jellyHome;
        this.jellyHome = jellyHome;
        firePropertyChange("jellyHome", old, jellyHome);
    }
    
    /** Getter for property testType.
     * @return Value of property testType.
     */
    public String getTestType() {
        return this.testType;
    }
    
    /** Setter for property testType.
     * @param testType New value of property testType.
     */
    public void setTestType(String testType) {
        String old=this.testType;
        this.testType = testType;
        firePropertyChange("testType", old, testType);
    }
    
    /** Getter for property attributes.
     * @return Value of property attributes.
     */
    public String getAttributes() {
        return this.attributes;
    }
    
    /** Setter for property attributes.
     * @param attributes New value of property attributes.
     */
    public void setAttributes(String attributes) {
        String old=this.attributes;
        this.attributes = attributes;
        firePropertyChange("attributes", old, attributes);
    }
   
    private Properties getProperties() {
        Properties props=new Properties();
        if (netbeansHome!=null)
            props.setProperty("netbeans.home", netbeansHome.getAbsolutePath());
        if (xtestHome!=null)
            props.setProperty("xtest.home", xtestHome.getAbsolutePath());
        if (jemmyHome!=null)
            props.setProperty("jemmy.home", jemmyHome.getAbsolutePath());
        if (jellyHome!=null)
            props.setProperty("jelly.home", jellyHome.getAbsolutePath());
        if (testType!=null && !testType.equals(""))
            props.setProperty("xtest.testtype", testType);
        if (attributes!=null && !attributes.equals(""))
            props.setProperty("xtest.attribs", attributes);
        return props;
    } 
    
    /** Getter for property showResults.
     * @return Value of property showResults.
     */
    public boolean isShowResults() {
        return this.showResults;
    }
    
    /** Setter for property showResults.
     * @param showResults New value of property showResults.
     */
    public void setShowResults(boolean showResults) {
        Boolean old=new Boolean(this.showResults);
        this.showResults = showResults;
        firePropertyChange("showResults", old, new Boolean(showResults));
    }
    
}
