/*
 * Installer.java
 *
 * $Id$
 */
package org.netbeans.installer;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.SystemUtils.Platform;
import org.netbeans.installer.utils.error.ErrorManager;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.wizard.Wizard;

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
    /**
     * The only private constructor - we need to hide the default one as 
     * <code>Installer is a singleton.
     */
    private Installer(String[] arguments) {
        // set the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            ErrorManager.getInstance().notify(ErrorManager.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        } catch (InstantiationException e) {
            ErrorManager.getInstance().notify(ErrorManager.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        } catch (IllegalAccessException e) {
            ErrorManager.getInstance().notify(ErrorManager.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        } catch (UnsupportedLookAndFeelException e) {
            ErrorManager.getInstance().notify(ErrorManager.WARNING, "Could " +
                    "not set the Look And Feel to the system default.", e);
        }
        
        // parse the command line arguments
        parseCommandLineArguments(arguments);
        
        // save this as the instance
        instance = this;
    }
    
    /**
     * Starts the installer. This method parses the passed-in command line arguments,
     * initializes the wizard and the components registry.
     * 
     * @param arguments The command line arguments
     */
    public void start() {
        try {
            // initialize the components registry
            ProductRegistry.getInstance().initialize();
            
            // start the wizard
            Wizard.getInstance().start();
        } catch (InitializationException e) {
            ErrorManager.getInstance().notify(ErrorManager.CRITICAL, e);
        }
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
    
    /**
     * Cancels the installation. This method cancels the changes that were possibly 
     * made to the components registry and exits with the cancel error code.
     * 
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        // cancel changes in components registry
        ProductRegistry.getInstance().cancelChanges();
        
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
        // save changes in components registry
        try {
            ProductRegistry.getInstance().finalizeChanges();
        } catch (FinalizationException e) {
            ErrorManager.getInstance().notify(ErrorManager.CRITICAL, "Cannot save registry", e);
        }
        
        // exit with success error code
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
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /** Errorcode to be used at normal exit */
    public static final int NORMAL_ERRORCODE = 0;
    
    /** Errorcode to be used when the installer is canceled */
    public static final int CANCEL_ERRORCODE = 1;
    
    /** Errorcode to be used when the installer exits because of a critical error */
    public static final int CRITICAL_ERRORCODE = Integer.MAX_VALUE;
}
