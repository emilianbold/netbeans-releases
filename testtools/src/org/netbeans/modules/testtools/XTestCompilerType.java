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

import java.io.IOException;
import java.util.StringTokenizer;

import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerType;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.CompilerCookie;

import org.apache.tools.ant.module.api.AntProjectCookie;
import java.util.Properties;
import java.io.File;
import org.netbeans.modules.testtools.wizards.WizardIterator;
import org.openide.TopManager;
import org.openide.ServiceType;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class XTestCompilerType extends CompilerType {

    private static final String compileTarget = "buildtests";
    private static final String cleanTarget = "cleantests";
    
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
    
    public XTestCompilerType() {
        String home=System.getProperty("netbeans.home");
        if (!new File(home+File.separator+"xtest-distribution").exists()) 
            home=System.getProperty("netbeans.user");
        xtestHome=new File(home+File.separator+"xtest-distribution");
        jemmyHome=new File(home+File.separator+"lib"+File.separator+"ext");
        jellyHome=new File(home+File.separator+"lib"+File.separator+"ext");
    }
    
    public static ServiceType.Handle getCompiler() {
        return new ServiceType.Handle(new XTestCompilerType());
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (XTestCompilerType.class);
    }

    public void prepareJob (CompilerJob job, Class type, DataObject obj) {
        AntProjectCookie cookie = (AntProjectCookie) obj.getCookie (AntProjectCookie.class);
        if (cookie == null) { 
            throw new IllegalArgumentException ("Missing Ant Project Cookie.");
        }
        if (netbeansHome==null || netbeansHome.equals(new File(System.getProperty("netbeans.home")))) {
            File home=WizardIterator.showFileChooser(TopManager.getDefault().getWindowManager().getMainWindow(), "Select Tested Netbeans Home Directory (different than current)", true, false);
            if (home!=null) 
                setNetbeansHome(home);
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
}
