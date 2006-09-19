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
 *
 * $Id$
 */
package org.netbeans.installer;

import java.io.File;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.actions.FinalizeRegistryAction;
import org.netbeans.installer.wizard.components.actions.InitalizeRegistryAction;

/**
 * The main class of the NBBA installer framework. It represents the installer and
 * provides methods to start the installation/maintenance process as well as to
 * finish/cancel/break the installation.
 *
 * @author Kirill Sorokin
 */
public class Installer {
    /////////////////////////////////////////////////////////////////////////////////
    // Main
    /**
     * The main method. It gets an instance of <code>Installer</code> and calls the
     * <code>start</code> method, passing in the command line arguments.
     *
     * @param arguments The command line arguments
     * @see #start(String[])
     */
    public static void main(String[] arguments) {
        new Installer(arguments).start();
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_LOCAL_DIRECTORY_PATH =
            System.getProperty("user.home") + File.separator + ".nbi";
    
    public static final String LOCAL_DIRECTORY_PATH_PROPERTY =
            "nbi.local.directory.path";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Installer instance;
    
    /**
     * Returns an instance of <code>Installer</code>. If the instance does not
     * exist - it is created.
     *
     * @return An instance of <code>Installer</code>
     */
    public static synchronized Installer getInstance() {
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File localDirectory =
            new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
    
    private LogManager logManager = LogManager.getInstance();
    
    /**
     * The only private constructor - we need to hide the default one as
     * <code>Installer is a singleton.
     */
    private Installer(String[] arguments) {
        logManager.log(ErrorLevel.MESSAGE, "initializing the installer engine");
        logManager.indent();
        
        logManager.log(ErrorLevel.MESSAGE, "setting the look and feel");
        String lfName = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lfName);
        } catch (ClassNotFoundException e) {
            ErrorManager.getInstance().notify(ErrorLevel.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        } catch (InstantiationException e) {
            ErrorManager.getInstance().notify(ErrorLevel.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        } catch (IllegalAccessException e) {
            ErrorManager.getInstance().notify(ErrorLevel.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        } catch (UnsupportedLookAndFeelException e) {
            ErrorManager.getInstance().notify(ErrorLevel.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        }
        logManager.log(ErrorLevel.MESSAGE, "... look and feel set to " + lfName);
        
        logManager.log(ErrorLevel.MESSAGE, "parsing command-line arguments");
        parseCommandLineArguments(arguments);
        logManager.log(ErrorLevel.MESSAGE, "... command line arguments successfully parsed");
        
        instance = this;
        
        logManager.log(ErrorLevel.MESSAGE, "initializing the local directory");
        if (System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY) != null) {
            localDirectory = new File(System.getProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY)).getAbsoluteFile();
        }
        
        logManager.unindent();
    }
    
    /**
     * Starts the installer. This method parses the passed-in command line arguments,
     * initializes the wizard and the components registry.
     *
     * @param arguments The command line arguments
     */
    public void start() {
        if (!localDirectory.exists()) {
            if (!localDirectory.mkdirs()) {
                ErrorManager.getInstance().notify(ErrorLevel.CRITICAL, "Cannot create local directory: " + localDirectory);
            }
        } else if (localDirectory.isFile()) {
            ErrorManager.getInstance().notify(ErrorLevel.CRITICAL, "Local directory exists and is a file: " + localDirectory);
        } else if (!localDirectory.canRead()) {
            ErrorManager.getInstance().notify(ErrorLevel.CRITICAL, "Cannot read local directory - not enought permissions");
        } else if (!localDirectory.canWrite()) {
            ErrorManager.getInstance().notify(ErrorLevel.CRITICAL, "Cannot write to local directory - not enought permissions");
        }
        
        
        final Wizard wizard = Wizard.getInstance();
        
        wizard.open();
        wizard.executeAction(new InitalizeRegistryAction());
        wizard.next();
    }
    
    /**
     * Cancels the installation. This method cancels the changes that were possibly
     * made to the components registry and exits with the cancel error code.
     *
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        // exit with the cancel error code
        System.exit(CANCEL_ERRORCODE);
    }
    
    /**
     * Finishes the installation. This method finalizes the changes made to the
     * components registry and exits with a normal error code.
     *
     * @see #cancel()
     * @see #criticalExit()
     */
    public void finish() {
        Wizard wizard = Wizard.getInstance();
        
        wizard.executeAction(new FinalizeRegistryAction());
        wizard.close();
        
        System.exit(NORMAL_ERRORCODE);
    }
    
    /**
     * Critically exists. No changes will be made to the components registry - it
     * will remain at the same state it was at the moment this method was called.
     *
     * @see #cancel()
     * @see #finish()
     */
    public void criticalExit() {
        // exit immediately, as the system is apparently in a crashed state
        System.exit(CRITICAL_ERRORCODE);
    }
    
    public File getLocalDirectory() {
        return localDirectory;
    }
    
    /**
     * Parses the command line arguments passed to the installer. All unknown
     * arguments are ignored.
     *
     * @param arguments The command line arguments
     */
    private void parseCommandLineArguments(String[] arguments) {
        // TODO
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /** Errorcode to be used at normal exit */
    public static final int NORMAL_ERRORCODE = 0;
    
    /** Errorcode to be used when the installer is canceled */
    public static final int CANCEL_ERRORCODE = 1;
    
    /** Errorcode to be used when the installer exits because of a critical error */
    public static final int CRITICAL_ERRORCODE = Integer.MAX_VALUE;
}
