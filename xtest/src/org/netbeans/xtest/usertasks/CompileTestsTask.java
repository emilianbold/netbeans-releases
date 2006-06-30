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


package org.netbeans.xtest.usertasks;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.netbeans.xtest.plugin.*;
import org.apache.tools.ant.taskdefs.Property;

/**
 * @author mb115822
 */
public class CompileTestsTask extends TestsActionTask {
    // property names - should be public

    /** sources to compile
     */
    public static final String COMPILE_SRCDIR = "compile.srcdir";
    /** destination dir of compiled sources
     */
    public static final String COMPILE_DESTDIR = "compile.destdir";
    /**
     * classpath used for compilation along with junit */
    public static final String COMPILE_CLASSPATH  = "compile.classpath";
    /**
     * files excluded from compilation
     */
    public static final String COMPILE_EXCLUDES = "compile.excludes";
    /**
     * should compiled code include debug information
     */
    public static final String COMPILE_DEBUG = "build.compiler.debug";
    /** 
     * should be shown deprecation information
     */
    public static final String COMPILE_DEPRECATION = "build.compiler.deprecation";
   
    
    
    //    
    protected Path srcDir;
    protected Path destDir;
    protected Path compileClasspath;
    protected String compileExcludes;
    
    // not to be public for now
    protected Boolean debug;
    protected Boolean deprecation;
        
    
    // ant script setters  
    
    // for compiling
    public void setSrcDir(Path srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestDir(Path destDir) {
        this.destDir = destDir;
    }
    
    public void setClasspath(Path compileClasspath) {
        if (this.compileClasspath == null) {
            this.compileClasspath = compileClasspath;
        } else {
            this.compileClasspath.append(compileClasspath);
        }
    }    
    
    public void setClasspathRef(Reference ref) {
        createClasspath().setRefid(ref);
    }
    
    public Path createClasspath() {
        if (compileClasspath == null) {
            this.compileClasspath = new Path(getProject());
        } 
        return compileClasspath.createPath();
    }
    
    
    public void setCompileExcludes(String compileExcludes) {
        this.compileExcludes = compileExcludes;
    }
    
    public void setDebug(boolean debug) {
        this.debug = new Boolean(debug);
    }
    
    public void setDeprecate(boolean deprecation) {
        this.deprecation = new Boolean(deprecation);
    }
        
    
    
    
    // action for TestsActionTask
    protected PluginDescriptor.Action getSelectedAction(PluginDescriptor pluginDescriptor)  throws PluginResourceNotFoundException {
        return pluginDescriptor.getCompiler(actionID);        
    }
    
    // prepare properties for compile target in plugin 
    //  - in future it might be a simple java method call in a plugin
    protected void runCompileAction() {
        if (srcDir != null) {
            addProperty(COMPILE_SRCDIR,srcDir.toString());
        }
        if (destDir != null) {
            addProperty(COMPILE_DESTDIR,destDir.toString());
        }
        if (compileClasspath != null) {
            addProperty(COMPILE_CLASSPATH,compileClasspath.toString());
        }
        if (compileExcludes != null) {
            addProperty(COMPILE_EXCLUDES,compileExcludes);
        }
        if (debug != null) {
            addProperty(COMPILE_DEBUG, debug.toString());
        }
        if (deprecation != null) {
            addProperty(COMPILE_DEPRECATION, deprecation.toString());
        }
                
        // finally execute the parent task
        super.execute();
    }
    
    public void execute () throws BuildException {
        log("XTest: compiling tests.");
        if (getPluginName() == null) {
            // there is no need to specify plugin for compilation
            // when not specified, base compiler is used
            setPluginName("base");
        }        
        // run the the plugin action
        runCompileAction();

    }
    
}
