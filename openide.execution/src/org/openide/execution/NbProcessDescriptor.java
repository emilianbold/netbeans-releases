/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.execution;

import java.text.Format;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

//for OpenVMS conditional execution change
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

/** Encapsulates start information for a process. It allows the user to
* specify the process name to execute and arguments to provide. The progammer
* then uses method exec to start the process and can pass additional format that
* will be applied to arguments. 
* <P>
* This allows to define arguments in format -user {USER_NAME} -do {ACTION} and then
* use MapFormat with defined values for USER_NAME and ACTION that will be substitued
* by into the arguments.
*
* @author  Ian Formanek, Jaroslav Tulach
*/
public final class NbProcessDescriptor extends Object implements java.io.Serializable {

    private static final long serialVersionUID = -4535211234565221486L;
    
    /** ErrorManager for logging execs */
    private static ErrorManager execLog;

    /** The name of the executable to run */
    private String processName;
    /** argument format */
    private String arguments;
    /** info about format of the arguments */
    private String info;

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param processName     the name of the executable to run
    * @param arguments string for formating of arguments (may be {@link Utilities#parseParameters quoted})
    */
    public NbProcessDescriptor(String processName, String arguments) {
        this (processName, arguments, null);
    }

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param processName     the name of the executable to run
    * @param arguments string for formating of arguments (may be {@link Utilities#parseParameters quoted})
    * @param info info how to format the arguments (human-readable string)
    */
    public NbProcessDescriptor(String processName, String arguments, String info) {
        this.processName = processName;
        this.arguments = arguments;
        this.info = info;
    }


    /** Get the name of the executable to run.
    * @return the name
    */
    public String getProcessName () {
        return processName;
    }

    /** Getter the execution arguments of the process.
    * @return the switch that the executable uses for passing the classpath as its command-line parameter 
    */
    public String getArguments () {
        return arguments;
    }

    /** Getter for the human readable info about the arguments.
    * @return the info string or null
    */
    public String getInfo () {
        return info;
    }

    /* JST: Commented out, should not be needed.
    *
    *  Get the command string and arguments from the supplied process name.
    * Normally the process name will be the actual name of the process executable,
    * in which case this method will just return that name by itself.
    * However, {@link org.openide.util.Utilities#parseParameters} is used
    * to break apart the string into tokens, so that users may:
    * <<moved to Utilities.parseParameters Javadoc>>
    * @return a list of the command name itself and any arguments, unescaped
    * @see Runtime#exec(String[])
    *
    public String[] getProcessArgs() {
      if (processArguments == null) {
        processArguments = parseArguments(processName);
      }
      return (String[]) processArguments.clone();
    }
    */

    /** Executes the process with arguments formatted by the provided
    * format. Also the envp properties are passed to the executed process,
    * and a working directory may be supplied.
    *
    * @param format format to be applied to arguments, process and envp supplied by user. It can be <code>null</code> if no formatting should be done.
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @param cwd the working directory to use, or <code>null</code> if this should not be specified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails, or if setting the working directory is not supported
    */
    public Process exec (Format format, String[] envp, File cwd) throws IOException {
        return exec (format, envp, false, cwd);
    }
    
    /** Executes the process with arguments, processName and envp formatted by the provided
    * format. Also the envp properties are passed to the executed process,
    * and a working directory may be supplied. Optionally the environment variables of the IDE may
    * be appended to (replaced when there is overlap) instead of specifying
    * all of the environment variables from scratch. This requires the IDE's
    * launcher to translate environment variables to system properties prefixed
    * by <samp>Env-</samp> in order to work correctly.
    *
    * @param format format to be applied to arguments, process and envp supplied by user. It can be <code>null</code> if no formatting should be done.
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @param appendEnv if true and <code>envp</code> is not <code>null</code>, append or replace IDE's environment
    * @param cwd the working directory to use, or <code>null</code> if this should not be specified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails, or if setting the working directory is not supported
    * @since 1.15
    */
    public Process exec (Format format, String[] envp, boolean appendEnv, File cwd) throws IOException {
        String stringArgs = format == null ? arguments : format.format (arguments);
        String[] args = parseArguments (stringArgs);
        String[] call = null;
        
        envp = substituteEnv(format, envp);
       
        //Conditional for OpenVMS execution. OpenVMS has a 255 character limit for shell command, so we use
        //special OpenVMS JVM  switch, -V <file>. The switch causes the JVM to read the remainder of the command line switches
        //from <file>. 
        //This code only affects the OpenVMS platform. 
        
        //we are only interested in commands that support -V switch, these are java and javac
        //
        if ( (org.openide.util.Utilities.getOperatingSystem() == org.openide.util.Utilities.OS_VMS) &&
              isJavaCmd( args ) ) {        
            call = constructVMSCmdLine( format, args );
        }
        else {
             // copy the call string
            call = new String[args.length + 1];
            call[0] = format == null ? processName : format.format(processName);
            System.arraycopy (args, 0, call, 1, args.length); 
        }

        logArgs(call);
        
        if (envp != null && appendEnv) {
            // Take system properties Env-* and use them as defaults.
            // XXX #4738465 requests a better way - supposedly fixed in Tiger...
            Map e = new HashMap (100); // Map<String,String>
            Iterator it = System.getProperties ().entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry entry = (Map.Entry) it.next ();
                String prop = (String) entry.getKey ();
                if (prop.startsWith ("Env-")) { // NOI18N
                    String evar = prop.substring (4); // length of "Env-"
                    e.put (evar, entry.getValue ());
                }
            }
            for (int i = 0; i < envp.length; i++) {
                String nameval = envp[i];
                int idx = nameval.indexOf ('='); // NOI18N
                // [PENDING] add localized annotation...
                if (idx == -1) throw new IOException ("No equal sign in name=value: " + nameval); // NOI18N
                e.put (nameval.substring (0, idx), nameval.substring (idx + 1));
            }
            envp = new String[e.size ()];
            int i = 0;
            it = e.entrySet ().iterator ();
            while (it.hasNext ()) {
                Map.Entry entry = (Map.Entry) it.next ();
                envp[i++] = ((String) entry.getKey ()) + '=' + ((String) entry.getValue ()); // NOI18N
            }
        }

        // XXX(ttran, psuchomel) for Windows 98 it is nessesary to change
        // calling thread's priority to NORM_PRIORITY when starting external
        // process and then switch it back to the original value.  See JDK bug
        // #4086045

        int os = Utilities.getOperatingSystem();

        // XXX(ttran) must set these variables to some values to silence
        // javac's error "variable xyz might not have been initialized"
        
        Thread currentThread = null;
        int currentPriority = 0;

        try {
            if (os == Utilities.OS_WIN98) {
                currentThread  = Thread.currentThread();
                currentPriority = currentThread.getPriority();
                currentThread.setPriority(Thread.NORM_PRIORITY);
            }

            if (cwd == null) {
                if (envp == null) {
                    return Runtime.getRuntime ().exec (call);
                } else {
                    return Runtime.getRuntime ().exec (call, envp);
                }
            } else {
                return Runtime.getRuntime().exec(call, envp, cwd);
            }
        }
        finally {
            if (os == Utilities.OS_WIN98) {
                currentThread.setPriority(currentPriority);
            }
        }
    }
    
    private static void logArgs(String[] args) {
        try {
            java.util.ResourceBundle rb = NbBundle.getBundle("org.openide.execution.Bundle"); // NOI18N
            String exc = rb.getString ("CTL_Exec"); // NOI18N
            String fmt = rb.getString("FMT_ExecParams"); // NOI18N
            java.text.MessageFormat msgformat = new java.text.MessageFormat(fmt);
            StringWriter writer;
            PrintWriter printer = new PrintWriter(writer = new StringWriter());
            printer.println(exc);
            for (int i = 0; i < args.length; i++) {
                printer.println(msgformat.format(new Object[] { new Integer(i), args[i]})); // NOI18N
            }
            printer.close();
            
            getExecLog().log(ErrorManager.INFORMATIONAL, writer.toString());
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /** Executes the process with arguments and processNme formatted by the provided
    * format. Also the envp properties are passed to the executed process.
    *
    * @param format format to be aplied to arguments, process and envp suplied by user. It can be <code>null</code> if no formatting should be done.
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec (Format format, String[] envp) throws IOException {
        return exec (format, envp, null);
    }

    /** Executes the process with arguments and processName formatted by the provided
    * format. 
    *
    * @param format format to be aplied to arguments and process. It can be <code>null</code> if no formatting should be done.
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec (Format format) throws IOException {
        return exec (format, null);
    }

    /** Executes the process with arguments provided in constructor.
    *
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec () throws IOException {
        return exec (null, null);
    }

    /* hashCode */
    public int hashCode() {
        return processName.hashCode() + arguments.hashCode ();
    }

    /* equals */
    public boolean equals(Object o) {
        if (! (o instanceof NbProcessDescriptor)) return false;
        NbProcessDescriptor him = (NbProcessDescriptor) o;
        return processName.equals(him.processName) && arguments.equals(him.arguments);
    }

    /** Parses given string to an array of arguments.
    * @param sargs is tokenized by spaces unless a space is part of "" token
    * @return tokenized string
    */
    private static String[] parseArguments(String sargs) {
        return Utilities.parseParameters(sargs);
    }
    
    /** Getter for the execLog */
    private static ErrorManager getExecLog() {
        if (execLog == null) {
            execLog = ErrorManager.getDefault().getInstance("IDE-Exec"); // NOI18N
        }
        return execLog;
    }
    
    /** Determines if the shell command is for java or javac
     * @param args is argument string
     * @return boolean
     */
    private boolean isJavaCmd ( String[] args ) {
     
        return  (args.length > 0 ) &&
            ( processName.endsWith( "{" + ProcessExecutor.Format.TAG_SEPARATOR + "}" + "java") || // NOI18N
              processName.endsWith( "{" + ProcessExecutor.Format.TAG_SEPARATOR + "}" + "javac") ) ; // NOI18N
    }
    
    /** Construct the file containing most of the VMS command lines
     * @param format contains the format of the command and command line arguments
     * @param args contains the actual command line arguments
     * @return the array of commands containing special OpenVMS JVM  switch, -V <file>
     */
    private String [] constructVMSCmdLine( Format format, String [] args ) throws IOException {
        
        //creates a temporary file containing the switches
        //
        PrintWriter pWriter = null;
        File switchFile = null;
        String atFileName = null;
        final String javacCmd = "{" + ProcessExecutor.Format.TAG_SEPARATOR + "}" + "javac" ; // NOI18N
        try {
            switchFile = File.createTempFile("compilerparams", "pms"); // NOI18N
            switchFile.deleteOnExit(); 
            pWriter = new PrintWriter ( new BufferedOutputStream ( new FileOutputStream( switchFile ) ) );
                     
            //copy the command arguments into the file
            //
            for ( int i=0; i< args.length; i++ ){
                         
                //only one @file at most
                //
                if ( (args[i].charAt(0) == '@') &&  
                      processName.endsWith( javacCmd ) ) {
                     atFileName = args[i];
                     continue;
                }
                pWriter.println( args[i] );
            }
        }
        finally {
            if ( pWriter != null )
                pWriter.close();
        }

        // copy the call string
        //
        String [] call = atFileName != null ?  new String[4] : new String[3];   //when atFileName is null then we only have three args
        call[0] = format == null ? processName : format.format(processName);
        call[1] = "-V"; // NOI18N
        call[2] = switchFile.getAbsolutePath();
                 
        if ( call.length > 3 )
            call[3] = atFileName;
            
        return call;
    }
    
    /** Iterates through envp and applies format.format() on values
     * @param format for formatting, i.e. substitute {filesystems} to /home/phil/dev/classes/pack1:/home/phil/dev/classes/pack2:...
     * @param envp an String array
     *
     * @return substitutet array
     */
    private static String[] substituteEnv(Format format, String[] envp) {
        if (envp == null || envp.length == 0 || format == null) {
            return envp;
        }
        
        String[] ret = new String[envp.length];
        StringBuffer adder = new StringBuffer();
        for (int i = 0; i < envp.length; i++) {
            ret[i] = envp[i];
            if (ret[i] == null) {
                continue;
            }
            
            int idx = ret[i].indexOf('=');
            if (idx < 0) {
                continue;
            }
            
            String val = ret[i].substring(idx + 1);
            String key = ret[i].substring(0, idx);
            adder.append(key).append('=').append(format.format(val));
            ret[i] = adder.toString();
            adder.setLength(0);
        }
        
        return ret;
    }
}
