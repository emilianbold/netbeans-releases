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
 * XTestCompilerType.java
 *
 * Created on April 29, 2002, 10:47 PM
 */

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.openide.util.Task;
import org.openide.TopManager;
import org.openide.ServiceType;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerType;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExecSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ProgressEvent;

import org.apache.tools.ant.module.run.AntCompiler;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.modules.testtools.wizards.WizardIterator;


/** class representing Compiler Type for XTest Workspace Build Script
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestCompilerType extends CompilerType {

    static final long serialVersionUID = -4763744289088576457L;

    private static final String compileTarget = "buildtests";
    private static final String cleanTarget = "cleantests";
    private static final String cleanResultsTarget = "cleanresults";
    
    /** Holds value of property netbeansHome. */
    private File netbeansHome=null;

    /** Holds value of property xtestHome. */
    private File xtestHome;
    
    /** Holds value of property jemmyHome. */
    private File jemmyHome;
    
    /** Holds value of property jellyHome. */
    private File jellyHome;
    
    /** Holds value of property testType. */
    private String testType="";
    
    /** creates new XTestCompilerType */    
    public XTestCompilerType() {
        String home=System.getProperty("netbeans.home");
        if (!new File(home+File.separator+"xtest-distribution").exists()) 
            home=System.getProperty("netbeans.user");
        xtestHome=new File(home+File.separator+"xtest-distribution");
        jemmyHome=new File(home+File.separator+"lib"+File.separator+"ext");
        jellyHome=new File(home+File.separator+"lib"+File.separator+"ext");
    }
    
    /** creates new XTestCompilerType, fills and returns propper Handler
     * @return ServiceType.Handler with XTestCompilerType */    
/*    public static ServiceType.Handle getHandle() {
        return new ServiceType.Handle(new XTestCompilerType());
    }
*/    
    /** returns Help Context
     * @return HelpCtx */    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (XTestCompilerType.class);
    }

    final static File netHome=new File(System.getProperty("netbeans.home","."));
    
    /** fills Compiler Job with propper compilers
     * @param job CompilerJob to be filled
     * @param type compilation type
     * @param obj DataObject to be compiled */    
    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        AntProjectCookie cookie = (AntProjectCookie) obj.getCookie (AntProjectCookie.class);
        if (cookie == null) { 
            throw new IllegalArgumentException ("Missing Ant Project Cookie.");
        }
        if (netbeansHome==null || netHome.equals(netbeansHome)) {
            File home=WizardIterator.showFileChooser(TopManager.getDefault().getWindowManager().getMainWindow(), "Select Tested Netbeans Home Directory (different than current)", true, false);
            if ((home!=null)&&(!netHome.equals(home))) {
                setNetbeansHome(home);
                if (obj instanceof MultiDataObject) {
                    Object coo=ExecSupport.getExecutor(((MultiDataObject)obj).getPrimaryEntry());
                    if ((coo==null)&&(obj instanceof XTestDataObject))
                        coo=TopManager.getDefault().getServices().find(XTestExecutor.class);
                    if ((coo!=null) && (coo instanceof XTestExecutor)) {
                        XTestExecutor exec=((XTestExecutor)coo);
                        if (exec.getNetbeansHome()==null)
                            exec.setNetbeansHome(home);
                    }
                }
            } else return;
        }
        Properties props=getProperties();
        File target;
        if (type == CompilerCookie.Compile.class) {
            job.add (new XTestCompiler (cookie, compileTarget, props));
        } else if (type == CompilerCookie.Build.class) {
            Compiler clean = new XTestCompiler (cookie, cleanTarget, props);
            job.add(clean);
            Compiler compile = new XTestCompiler (cookie, compileTarget, props);
            compile.dependsOn(clean);
            job.add(compile);
        } else if (type == CompilerCookie.Clean.class) {
            job.add (new XTestCompiler (cookie, cleanTarget, props));
        } else if (type == XTestDataObject.CleanResults.class) {
            job.add (new XTestCompiler (cookie, cleanResultsTarget, props));
        } else {
            return;
        }
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
    
    private Properties getProperties() {
        Properties props=new Properties();
        if (netbeansHome!=null)
            props.setProperty("netbeans.home",netbeansHome.getAbsolutePath());
        if (xtestHome!=null)
            props.setProperty("xtest.home",xtestHome.getAbsolutePath());
        if (jemmyHome!=null)
            props.setProperty("jemmy.home",jemmyHome.getAbsolutePath());
        if (jellyHome!=null)
            props.setProperty("jelly.home",jellyHome.getAbsolutePath());
        if (testType!=null && !testType.equals(""))
            props.setProperty("xtest.testtype",testType);
        return props;
    }        

    /** class representing Compiler for XTest Workspace Build Script */    
    public static  class XTestCompiler extends AntCompiler {
        private Properties props;

        /** creates new XTestCompiler
         * @param cookie AntProjectCookie of compiled Data Object
         * @param target String name of target to be called for compilation
         * @param props additional Properties for compilation */        
        public XTestCompiler (AntProjectCookie cookie, String target, Properties props) {
            super(cookie, target);
            this.props=props;
        }

        /** returns class of related Compiler Group
         * @return XTestCompilerGroup.class */        
        public Class compilerGroupClass () {
            return XTestCompilerGroup.class;
        }

        Properties getProperties() {
            return props;
        }
    }
    
    /** class representing Compiler Group for XTest Workspace Build Script */    
    public static class XTestCompilerGroup extends CompilerGroup {

        private Map torun = new HashMap();

        /** add Compiler to Compiler Group
         * @param c Compiler
         * @throws IllegalArgumentException when Compiler is not instance of XTestCompiler */        
        public void add(Compiler c) throws IllegalArgumentException {
            if(!(c instanceof XTestCompiler)) throw new IllegalArgumentException();
            XTestCompiler comp=(XTestCompiler) c;
            AntProjectCookie proj=comp.getProjectCookie();
            Map targets=(Map)torun.get(proj);
            if(targets== null) {
                targets=new HashMap();
                torun.put(proj, targets);
            }
            targets.put(comp.getTarget(), comp.getProperties());
        }

        /** starts compilation
         * @return boolean result of compilation */        
        public boolean start() {
            Iterator scripts = torun.entrySet().iterator();
            while(scripts.hasNext()) {
                Map.Entry entry=(Map.Entry)scripts.next();
                AntProjectCookie script=(AntProjectCookie)entry.getKey();
                if(script.getFileObject()!=null) {
                    fireProgressEvent(new ProgressEvent(this, script.getFileObject(), ProgressEvent.TASK_UNKNOWN));
                }
                Map targets=(Map)entry.getValue();
                Iterator targetsit=targets.entrySet().iterator();
                while(targetsit.hasNext()) {
                    Map.Entry target =(Map.Entry)targetsit.next();
                    try {
                        TargetExecutor te=new TargetExecutor(script,(target == null)? null : new String[]{(String)target.getKey()});
                        te.addProperties((Properties)target.getValue());
                        if(te.execute().result()!=0) {
                            return false;
                        }
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        return false;
                    }
                }
            }
            return true;
        }
    }
   
}
