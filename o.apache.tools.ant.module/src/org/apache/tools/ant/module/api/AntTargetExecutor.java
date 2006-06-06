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

package org.apache.tools.ant.module.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbCollections;

/**
 * Executes an Ant target or list of targets asynchronously inside NetBeans.
 * @since 2.15
 */
final public class AntTargetExecutor {
    
    private final Env env;
    
    /** Create instance of Ant target executor for the given Ant project.
     */
    private AntTargetExecutor(Env env) {
        this.env = env;
    }
    
    /** Factory method for creation of AntTargetExecutor with the given environment.
     * The factory does not clone Env what means that any change to Env will
     * influence the factory.
     * @param env a configuration for the executor
     * @return an executor which can run projects with the given configuration
     */
    public static AntTargetExecutor createTargetExecutor(Env env) {
        return new AntTargetExecutor(env);
    }
    
    /** Execute given target(s).
     * <p>The {@link AntProjectCookie#getFile} must not be null, since Ant can only
     * run files present on disk.</p>
     * <p>The returned task may be used to wait for completion of the script
     * and check result status.</p>
     * <p class="nonnormative">
     * The easiest way to get the project cookie is to get a <code>DataObject</code>
     * representing an Ant build script and to ask it for this cookie. Alternatively,
     * you may implement the cookie interface directly, where
     * <code>getFile</code> is critical and other methods may do nothing
     * (returning <code>null</code> as needed).
     * While the specification for <code>AntProjectCookie</code> says that
     * <code>getDocument</code> and <code>getParseException</code> cannot
     * both return <code>null</code> simultaneously, the <em>current</em>
     * executor implementation does not care; to be safe, return an
     * {@link UnsupportedOperationException} from <code>getParseException</code>.
     * </p>
     * @param antProject a representation of the project to run
     * @param targets non-empty list of target names to run; may be null to indicate default target
     * @return task for tracking of progress of execution
     * @throws IOException if there is a problem running the script
     */
    public ExecutorTask execute(AntProjectCookie antProject, String[] targets) throws IOException {
        TargetExecutor te = new TargetExecutor(antProject, targets);
        te.setVerbosity(env.getVerbosity());
        te.setProperties(NbCollections.checkedMapByCopy(env.getProperties(), String.class, String.class, true));
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
         * @param v the new verbosity
         */
        public void setVerbosity(int v) {
            verbosity = v;
        }

        /** Get verbosity of Ant script execution. See <code>org.apache.tools.ant.Project.MSG_*</code>
         * properties for list of possible values.
         * @return the current verbosity
         */
        public int getVerbosity() {
            return verbosity;
        }

        /** Set properties of Ant script execution.
         * @param p a set of name/value pairs passed to Ant (will be cloned)
         */
        public synchronized void setProperties(Properties p) {
            properties = (Properties) p.clone();
        }

        /** Get current Ant script execution properties. The clone of
         * real properties is returned.
         * @return the current name/value pairs passed to Ant
         */
        public synchronized Properties getProperties() {
            return (Properties)properties.clone();
        }

        /** Set output stream into which the output of the
         * Ant script execution will be sent. If not set
         * the standard NetBeans output window will be used.
         * @param outputStream a stream to send output to, or <code>null</code> to reset
         * @see org.apache.tools.ant.module.spi.AntOutputStream
         * @deprecated Usage of a custom output stream is not recommended, and prevents some
         *             Ant module features from working correctly.
         */
        @Deprecated
        public void setLogger(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        /** Get output stream. If no output stream was
         * set then null will be returned what means that standard
         * NetBeans output window will be used.
         * @return the output stream to which Ant output will be sent, or <code>null</code>
         */
        public OutputStream getLogger() {
            return outputStream;
        }

    }
    
}
