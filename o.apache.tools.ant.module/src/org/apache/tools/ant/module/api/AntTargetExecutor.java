/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2003.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick, David Konecny
 */
 
package org.apache.tools.ant.module.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.openide.execution.ExecutorTask;

/** Executes an Ant Target asynchronously in the IDE.
 *
 * @since 2.15
 */
final public class AntTargetExecutor {
    
    private Env env;
    
    /** Create instance of Ant target executor for the given Ant project.
     */
    private AntTargetExecutor(Env env) {
        this.env = env;
    }
    
    /** Factory method for creation of AntTargetExecutor with the given environment.
     * The factory does not clone Env what means that any change to Env will
     * influence the factory.
     */
    public static AntTargetExecutor createTargetExecutor(Env env) {
        return new AntTargetExecutor(env);
    }
    
    /** Execute given target(s).
     * @param targets may be null to indicate default target
     * @return task for tracking of progress of execution
     */
    public ExecutorTask execute(AntProjectCookie antProject, String[] targets) throws IOException {
        TargetExecutor te = new TargetExecutor(antProject, targets);
        te.setVerbosity(env.getVerbosity());
        te.setProperties(env.getProperties());
        if (env.getLogger() == null) {
            return te.execute();
        } else {
            return te.execute(env.getLogger());
        }
    }

    /** Class describing the environment in which the Ant target will be executed.
     * The class can be used for customization of properties avaialble during the 
     * execution, verbosity of Ant target execution and output stream definition.
     */
    final public static class Env {

        private int verbosity;
        private Properties properties;
        private OutputStream outputStream;

        /** Create instance of Env class describing environment for Ant target execution.
         */
        public Env() {
            verbosity = AntSettings.getDefault().getVerbosity();
            properties = (Properties) AntSettings.getDefault().getProperties().clone();
        }

        /** Set verbosity of Ant script execution. See org.apache.tools.ant.Project.MSG_
         * properties for list of possible values.
         */
        public void setVerbosity(int v) {
            verbosity = v;
        }

        /** Get verbosity of Ant script execution. See org.apache.tools.ant.Project.MSG_
         * properties for list of possible values.
         */
        public int getVerbosity() {
            return verbosity;
        }

        /** Set properties of Ant script execution.
         */
        public synchronized void setProperties(Properties p) {
            properties = (Properties) p.clone();
        }

        /** Get current Ant script execution properties. The clone of
         * real properties is returned.
         */
        public synchronized Properties getProperties() {
            return (Properties)properties.clone();
        }

        /** Set output stream into which the output of the
         * Ant script execution will be sent. If not set
         * the standard NetBeans output window will be used.
         * See spi.AntOutputStream support class.
         */
        public void setLogger(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        /** Get output stream. If no output stream was
         * set then null will be returned what means that standard
         * NetBeans output window will be used.
         */
        public OutputStream getLogger() {
            return outputStream;
        }

    }
    
}
