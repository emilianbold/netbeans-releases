package org.netbeans.modules.testtools;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.windows.InputOutput;
import org.openide.execution.Executor;
import org.openide.execution.ExecInfo;
import org.openide.execution.ExecutorTask;

import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.api.AntProjectCookie;
import java.util.Properties;
import java.io.File;
import org.netbeans.modules.testtools.wizards.WizardIterator;
import org.openide.TopManager;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.cookies.ViewCookie;

public class XTestExecutor extends Executor {
    
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
    
    /** Holds value of property windowSystem. */
    private int windowSystem=0;
    
    public XTestExecutor() {
        String home=System.getProperty("netbeans.home");
        if (!new File(home+File.separator+"xtest-distribution").exists()) 
            home=System.getProperty("netbeans.user");
        xtestHome=new File(home+File.separator+"xtest-distribution");
        jemmyHome=new File(home+File.separator+"lib"+File.separator+"ext");
        jellyHome=new File(home+File.separator+"lib"+File.separator+"ext");
    }
    
    public ExecutorTask execute(ExecInfo info) throws IOException {
        throw new IOException("Not yet implemented.");
    }
    
    public ExecutorTask execute(DataObject obj) throws IOException {
        AntProjectCookie cookie=(AntProjectCookie)obj.getCookie(AntProjectCookie.class);
        if (cookie==null) {
            throw new IOException("Missing Ant Project Cookie.");
        }
        if (netbeansHome==null || netbeansHome.equals(new File(System.getProperty("netbeans.home")))) {
            File home=WizardIterator.showFileChooser(TopManager.getDefault().getWindowManager().getMainWindow(), "Select Tested Netbeans Home Directory (different than current)", true, false);
            if (home!=null) 
                setNetbeansHome(home);
        }
        TargetExecutor executor = new TargetExecutor(cookie, new String[]{"all"});
        executor.addProperties(getProperties());
        return showResults(executor.execute(), obj);
    }
    
    private ExecutorTask showResults(final ExecutorTask task, final DataObject obj) {
        RequestProcessor.postRequest(new Runnable() {
            public void run() {
                if (task.result()==0) {
                    try {
                        FileObject fo=obj.getFolder().getPrimaryFile();
                        fo=fo.getFileObject("results");
                        fo.getFileObject("index", "html");
                        DataObject dob=DataObject.find(fo);
                        ViewCookie view=(ViewCookie)dob.getCookie(ViewCookie.class);
                        view.view();
                    } catch (Exception e) {}
                }
            }
        });
        return task;
    }
    
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
    
    /** Getter for property windowSystemSDI.
     * @return Value of property windowSystemSDI.
     */
    public int getWindowSystem() {
        return this.windowSystem;
    }
    
    /** Setter for property windowSystemSDI.
     * @param windowSystemSDI New value of property windowSystemSDI.
     */
    public void setWindowSystem(int windowSystem) {
        if (windowSystem>=0 && windowSystem<3) {
            Integer old=new Integer(this.windowSystem);
            this.windowSystem = windowSystem;
            firePropertyChange("windowSystem", old, new Integer(windowSystem));
        }
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
        if (windowSystem>0)
            props.setProperty("xtest.ide.winsys", windowSystem==1? "sdi" : "mdi");
        return props;
    } 
}
