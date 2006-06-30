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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerType;
import org.openide.compiler.CompilerJob;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExecutionSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ProgressEvent;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.netbeans.modules.testtools.wizards.WizardIterator;
import org.openide.ServiceType;
import org.openide.cookies.CompilerCookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;


/** class representing Compiler Type for XTest Workspace Build Script
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestCompilerType extends CompilerType {

    static final long serialVersionUID = -4763744289088576457L;

    private static final String compileTarget = "buildtests"; // NOI18N
    private static final String cleanTarget = "cleantests"; // NOI18N
    private static final String cleanResultsTarget = "cleanresults"; // NOI18N
    
    /** Holds value of property netbeansHome. */
    private File netbeansHome=null;

    /** Holds value of property xtestHome. */
    private File xtestHome;
    
    /** Holds value of property jemmyHome. */
    private File jemmyHome;
    
    /** Holds value of property jellyHome. */
    private File jellyHome;
    
    /** Holds value of property testType. */
    private String testType=""; // NOI18N
    
    /** creates new XTestCompilerType */    
    public XTestCompilerType() {
        String home=System.getProperty("netbeans.home"); // NOI18N
        if (!new File(home+File.separator+"xtest-distribution").exists())  // NOI18N
            home=System.getProperty("netbeans.user"); // NOI18N
        xtestHome=new File(home+File.separator+"xtest-distribution"); // NOI18N
        jemmyHome=new File(home+File.separator+"modules"+File.separator+"ext"); // NOI18N
        jellyHome=new File(home+File.separator+"modules"+File.separator+"ext"); // NOI18N
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

    final static File netHome=new File(System.getProperty("netbeans.home",".")); // NOI18N
    
    /** fills Compiler Job with propper compilers
     * @param job CompilerJob to be filled
     * @param type compilation type
     * @param obj DataObject to be compiled */    
    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        AntProjectCookie cookie = (AntProjectCookie) obj.getCookie (AntProjectCookie.class);
        if (cookie == null) { 
            throw new IllegalArgumentException (NbBundle.getMessage(XTestCompilerType.class, "Err_MissingAntProjectCookie")); // NOI18N
        }
        if (netbeansHome==null || netHome.equals(netbeansHome)) {
            File home=WizardIterator.showFileChooser(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(XTestCompilerType.class, "Title_SelectNetbeansHome"), true, false); // NOI18N
            if ((home!=null)&&(!netHome.equals(home))) {
                setNetbeansHome(home);
                if (obj instanceof MultiDataObject) {
                    Object coo=ExecutionSupport.getExecutor(((MultiDataObject)obj).getPrimaryEntry());
                    if ((coo==null)&&(obj instanceof XTestDataObject)) {
                        ServiceType.Registry reg=(ServiceType.Registry)Lookup.getDefault().lookup(ServiceType.Registry.class);
                        coo=reg.find(XTestExecutor.class);
                    } if ((coo!=null) && (coo instanceof XTestExecutor)) {
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
        firePropertyChange("netbeansHome", old, netbeansHome); // NOI18N
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
        firePropertyChange("xtestHome", old, xtestHome); // NOI18N
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
        firePropertyChange("jemmyHome", old, jemmyHome); // NOI18N
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
        firePropertyChange("jellyHome", old, jellyHome); // NOI18N
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
        firePropertyChange("testType", old, testType); // NOI18N
    }
    
    private Properties getProperties() {
        Properties props=new Properties();
        if (netbeansHome!=null)
            props.setProperty("netbeans.home",netbeansHome.getAbsolutePath()); // NOI18N
        if (xtestHome!=null)
            props.setProperty("xtest.home",xtestHome.getAbsolutePath()); // NOI18N
        if (jemmyHome!=null)
            props.setProperty("jemmy.home",jemmyHome.getAbsolutePath()); // NOI18N
        if (jellyHome!=null)
            props.setProperty("jelly.home",jellyHome.getAbsolutePath()); // NOI18N
        if (testType!=null && !testType.equals("")) // NOI18N
            props.setProperty("xtest.testtype",testType); // NOI18N
        return props;
    }        

    /** class representing Compiler for XTest Workspace Build Script */    
    public static  class XTestCompiler extends Compiler {
        private Properties props;
        private AntProjectCookie pcookie;
        private String target;

        /** creates new XTestCompiler
         * @param cookie AntProjectCookie of compiled Data Object
         * @param target String name of target to be called for compilation
         * @param props additional Properties for compilation */        
        public XTestCompiler (AntProjectCookie pcookie, String target, Properties props) {
            this.pcookie = pcookie;
            this.target = target;
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

        public boolean equals (Object o) {
            if (! (o instanceof XTestCompiler)) return false;
            XTestCompiler other = (XTestCompiler) o;
            return pcookie.equals (other.pcookie) &&
                Utilities.compareObjects (target, other.target);
        }

        public int hashCode () {
            return 4882 ^
                pcookie.hashCode () ^
                ((target == null) ? 0 : target.hashCode ());
        }

        public String toString () {
            // For debugging e.g. #10585:
            return "XTestCompiler[project=" + pcookie + ",target=" + target + "]"; // NOI18N
        }

        public AntProjectCookie getProjectCookie () {
            return pcookie;
        }

        public String getTarget () {
            return target;
        }

        public boolean isUpToDate () {
            // Only Ant knows for sure:
            return false;
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
                        AntTargetExecutor.Env env = new AntTargetExecutor.Env();
                        Properties properties = env.getProperties();
                        properties.putAll((Properties)target.getValue());
                        env.setProperties(properties);
                        AntTargetExecutor executor = AntTargetExecutor.createTargetExecutor(env);
                        int result = executor.execute(script, (target == null)? null : new String[]{(String)target.getKey()}).result();
                        if(result != 0) {
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
