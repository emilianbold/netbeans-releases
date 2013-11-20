/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * RunTask.java
 *
 * Created on 15. prosinec 2003, 9:13
 */
package org.netbeans.modules.j2me.common.ant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 *
 * Run Task adds support for application running and debugging.
 *
 * <p>Attributes:<ol>
 * <li>JadFile - Semi-required. Jad file location with application description. This is used/mandatory for standard/default execution method.
 * <li>JadUrl - Semi-required. Jad file URL with application description. This is used/mandatory for OTA execution method. If not set, default execution method is forced.
 * <li>PlatformHome - Required. Home directory location of platform/emulator.
 * <li>PlatformType - Optional. Platform type - one of these: "UEI 1.0", "UEI 1.0.1" (default), Custom. When Custom is set, ExecMethod is ignored.
 * <li>Device - Optional. Target device.
 * <li>ExecMethod - Optional. One of these values: STANDARD (default), OTA.
 * <li>ClassPath - Optional. Classpath required by classes in SrcDir. It supports nested classpath tag too.
 * <li>ClassPathRef - Optional. Classpath specified by reference required by SrcJar classes.
 * <li>SecurityDomain - Optional. Security domain.
 * <li>Debug - Optional. Boolean value. True for run application in debugger. Default: false.
 * <li>DebugAddress - Optional. Address debug property should contain only port number since localhost is assumed. If not set a free ephemeral port is used.
 * <li>DebuggerAddressProperty - Optional. Name of the attribute for the debugger task that specifies the address to which the debugger will connect.
 * <li>DebugTransport - Optional. Transport debug property. Default: dt_socket.
 * <li>DebugServer - Optional. Server debug property. Default: true/n.
 * <li>DebugSuspend - Optional. Suspend debug property. Default: true/n.
 * <li>JarFile - Optional. Used to copy the Jad and Jar to a temporary folder for execution. The copy is performed when the Jad and Jar are located in the same folder only.
 * <li>CommandLine - Semi-required. Command line for running emulator. Required when PlatformType is Custom. See Command Line paragraph for more info.
 * <li>CmdOptions - Optional. Command line options added to the execution of emulator.
 * </ol>
 *
 * <p>CommandLine attribute allows you to fully customize command line for running a emulator in execution or debug mode.
 * Command Line string processed using EMapFormat class (see its javadoc for formatting hints). This task presets following identificators:<ul>
 * <li>platformhome - always set - value of PlatformHome attribute
 * <li>device - value of Device attribute
 * <li>classpath - classpath string composed from ClassPath, ClassPathRef, nested ClassPath
 * <li>jadfile - value of JadFile attribute
 * <li>jadurl - value of JadUrl attribute
 * <li>securitydomain - value of SecurityDomain attribute
 * <li>debug - value of Debug attribute
 * <li>debugaddress - value of DebugAddress attribute
 * <li>debugtransport - value of DebugTransport attribute
 * <li>debugserver - value of DebugServer attribute. Default: true.
 * <li>debugsuspend - value of DebugSuspend attribute. Default: true.
 * <li>cmdoptions - command line options propagated from project settings. Empty by default.
 * <li>/ - value of File.separator
 * </ul>
 * If value of attribute is not set, its value is not passed to Command Line string formatter.
 *
 * @author  Adam Sotona, David Kaspar
 */
public class RunTask extends Task
{
    
    private static final String DEFAULT_PLATFORM_TYPE = "UEI-1.0"; // NO I18N
    
    /** Holds value of property jadFile. */
    private File jadFile;
    
    /** Holds value of property jarFile. */
    private File jarFile;
    
    /** Holds value of property jadUrl. */
    private String jadUrl;
    
    /** Holds value of property execMethod. */
    private String execMethod;
    
    /** Holds value of property device. */
    private String device;
    
    /** Holds value of property platformHome. */
    private File platformHome;
    
    /** Holds value of property platformType. */
    private String platformType;
    
    /** Holds value of property classPath. */
    private Path classPath;
    
    /** Holds value of property securityDomain. */
    private String securityDomain;
    
    /** Holds value of property debug. */
    private boolean debug;
    
    /** Holds value of the debugger's address attribute name. */
    private String debuggerAddressProperty;
    
    /** Holds value of property debugAddress. */
    private String debugAddress;
    
    /** Holds value of property debugTransport. */
    private String debugTransport;
    
    /** Holds value of property debugServer. */
    private boolean debugServer = true;
    
    /** Holds value of property debugSuspend. */
    private boolean debugSuspend = true;
    
    /** Holds value of property commandLine. */
    private String commandLine = null;
    
    /** Holds value of property cmdOptions. */
    private String cmdOptions;
    
    private String listCommandLine = null;
    private String otaRunCommandLine = null;
    protected  URL jadUrlURL = null;
    final protected HashMap<String,Object> args = new HashMap<String,Object>();
    static final private String CMD_RUN="CMD_Run_";
    
    /**
     * Do the work.
     * @throws BuildException if attribute is missing or there is a problem during application execution.
     */
    @Override
    public void execute() throws BuildException
    {
        File tempFolder = null;
        try
        {
            if (platformHome == null) throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "platformHome")); // NO I18N
            if (jadFile == null && jadUrl == null) throw new BuildException(Bundle.getMessage("ERR_MissingJad")); // NO I18N
            if (!platformHome.isDirectory()) throw new BuildException(Bundle.getMessage("ERR_MissingPlatformHome", platformHome.toString())); // NO I18N
            if (platformType == null)
            {
                log(Bundle.getMessage("WARN_DefaultPlatform", DEFAULT_PLATFORM_TYPE), Project.MSG_WARN); // No I18N
                platformType = DEFAULT_PLATFORM_TYPE;
            }
            if (Bundle.getMessage("NAME_STANDARD").equals(execMethod))// NO I18N
                execMethod = null;
            if (Bundle.getMessage("NAME_CUSTOM").equals(platformType))
            { // NO I18N
                if (commandLine == null)
                {
                    log(Bundle.getMessage("WARN_MissingCommandLine", DEFAULT_PLATFORM_TYPE), Project.MSG_WARN); // No I18N
                    platformType = DEFAULT_PLATFORM_TYPE;
                }
            }
            else
                commandLine = null;
            if (jadUrl != null  &&  ! jadUrl.startsWith("${")) try
            {
                jadUrlURL = new URL(jadUrl);
            }
            catch (MalformedURLException e)
            {}
            if (commandLine == null  &&  execMethod != null)
            {
                try
                {
                    commandLine = Bundle.getMessage(CMD_RUN + platformType + "_" + execMethod); // No I18N
                    if (Bundle.getMessage("NAME_OTA").equals(execMethod))
                    { // NO I18N
                        try
                        {
                            listCommandLine = Bundle.getMessage(CMD_RUN + platformType + "_" + execMethod + "_List"); // No I18N
                        }
                        catch (MissingResourceException mre)
                        {}
                        try
                        {
                            otaRunCommandLine = Bundle.getMessage(CMD_RUN + platformType + "_" + execMethod + "_Run"); // No I18N
                        }
                        catch (MissingResourceException mre)
                        {}
                        if (jadUrlURL == null)
                        { // NOI18N
                            log(Bundle.getMessage("WARN_MissingJadUrlAttrSettingDefault"), Project.MSG_WARN); // NO I18N
                            execMethod = null;
                            listCommandLine = null;
                            otaRunCommandLine = null;
                        }
                    }
                }
                catch (MissingResourceException mre)
                {
                    log(Bundle.getMessage(debug ? "WARN_UnsupportedDebugMethod" : "WARN_UnsupportedExecMethod", platformType, execMethod), Project.MSG_WARN); // NO I18N
                    execMethod = null;
                    listCommandLine = null;
                    otaRunCommandLine = null;
                }
            }
            if (commandLine == null)
            {
                try
                {
                    commandLine = Bundle.getMessage(CMD_RUN + platformType); // NO I18N
                    if (jadFile == null)
                        throw new BuildException(Bundle.getMessage("ERR_MissingAttr", "jadFile")); // NO I18N
                }
                catch (MissingResourceException mre)
                {
                    throw new BuildException(Bundle.getMessage(debug ? "ERR_UnsupportedDebugging" : "ERR_UnsupportedExecution", platformType)); // NO I18N
                }
            }
            args.put("platformhome", platformHome.getAbsolutePath()); // NO I18N
            args.put("cmdoptions", cmdOptions == null ? "" : cmdOptions); //NOI18N
            if (classPath != null)
                args.put("classpath", classPath.toString()); // NO I18N
            if (device != null  &&  ! "".equals(device)) //NOI18N
                args.put("device", device); // NO I18N
            if (jadFile != null)
            {
                if (jarFile != null && jarFile.isFile() && jadFile.isFile() && jarFile.getParentFile() != null && jarFile.getParentFile().equals(jadFile.getParentFile())) try
                {
                    tempFolder = File.createTempFile("nbrun", "", jarFile.getParentFile()); //NOI18N
                    tempFolder.delete();
                    copy(jadFile, tempFolder);
                    copy(jarFile, tempFolder);
                    tempFolder.deleteOnExit();
                    jadFile = new File(tempFolder, jadFile.getName());
                    try
                    {
                        jadFile = jadFile.getCanonicalFile();
                    }
                    catch (IOException ioe)
                    {
                        jadFile = jadFile.getAbsoluteFile();
                    }
                    jadFile.deleteOnExit();
                    jarFile = new File(tempFolder, jarFile.getName());
                    try
                    {
                        jarFile = jarFile.getCanonicalFile();
                    }
                    catch (IOException ioe)
                    {
                        jarFile = jarFile.getAbsoluteFile();
                    }
                    jarFile.deleteOnExit();
                }
                catch (Exception e)
                {
                    log(e.getLocalizedMessage(), Project.MSG_WARN);
                }
                args.put("jadfile", jadFile.getAbsolutePath()); // NO I18N
            }
            if (jadUrlURL != null) {
                args.put("jadurl", jadUrlURL); // NO I18N
                log(Bundle.getMessage("Inf_JadURL", jadUrlURL.toExternalForm()), Project.MSG_INFO);
            }
            if (securityDomain != null  &&  ! "".equals(securityDomain)) //NOI18N
                args.put("securitydomain", securityDomain); // NO I18N
            if (debug)
            {
                if (debugAddress == null)
                {
                    try
                    {
                        debugAddress = Integer.toString(this.determineFreePort());
                    }
                    catch (IOException e)
                    {
                        throw new BuildException(e);
                    }
                }
                args.put("debug", ""); // NO I18N
                args.put("debugaddress", debugAddress); // No I18N
                args.put("debugtransport", debugTransport != null ? debugTransport : "dt_socket"); // NO I18N
                args.put("debugserver", debugServer ? "y" : "n"); // NO I18N
                args.put("debugsuspend", debugSuspend ? "y" : "n"); // NO I18N
                if (debuggerAddressProperty != null)
                {
                    this.getProject().setNewProperty(debuggerAddressProperty, debugAddress);
                }
            }
            args.put("/", File.separator); // NO I18N
            if (debug)
                log(Bundle.getMessage("MSG_RunDebug", debugAddress), Project.MSG_INFO); // NO I18N
            else
                log(Bundle.getMessage("MSG_RunExec"), Project.MSG_INFO); // NO I18N
            
            try
            {
                final int i = doExecute();
                if (i != 0)
                    throw new BuildException(Bundle.getMessage("ERR_RunFailed", String.valueOf(i))); // No I18N
            }
            catch (IOException ioe)
            {
                throw new BuildException(ioe);
            }
        }
        finally
        {
            if (tempFolder != null) delete(tempFolder);
        }
    }
    
    private void copy(final File sourceFile, final File targetFolder) throws BuildException
    {
        final Copy c = new Copy();
        c.setProject(getProject());
        final FileSet fileset = new FileSet();
        fileset.setFile(sourceFile);
        c.addFileset(fileset);
        c.setTodir(targetFolder);
        c.execute();
    }
    
    private void delete(final File f)
    {
        if (f.isDirectory())
        {
            final File fs[] = f.listFiles();
            for (int i=0; i<fs.length; i++) delete(fs[i]);
        }
        f.delete();
    }
    
    private int doExecute() throws IOException
    {
        commandLine = EMapFormat.format(commandLine, args);
        Execute exec = new Execute();
        exec.setAntRun(getProject());
        exec.setVMLauncher(true);
        exec.setCommandline(Commandline.translateCommandline(commandLine));
        if (classPath != null && isClassFileAvailable()) exec.setStreamHandler(new RunTask.StackTraceTranslatorHandler(getProject().getBaseDir(), classPath.list()));
        log(Bundle.getMessage("MSG_ExecCmd", commandLine), Project.MSG_VERBOSE); // No I18N
	int i = 0;
        try {
            i = exec.execute();
        } catch (IllegalThreadStateException itse) {
            //workaround for bug #85200, ITSE here is a race condition caused by JDK
            log(itse.getLocalizedMessage(), Project.MSG_VERBOSE);
        }
        if (i == 0 && listCommandLine != null)
        {
            listCommandLine = EMapFormat.format(listCommandLine, args);
            exec = new Execute();
            exec.setAntRun(getProject());
            exec.setVMLauncher(true);
            exec.setCommandline(Commandline.translateCommandline(listCommandLine));
            exec.setStreamHandler(new RunTask.ListStreamHandler());
            log(Bundle.getMessage("MSG_ExecCmd", listCommandLine), Project.MSG_VERBOSE); // No I18N
            try {
                i = exec.execute();
            } catch (IllegalThreadStateException itse) {
                //workaround for bug #85200, ITSE here is a race condition caused by JDK
                log(itse.getLocalizedMessage(), Project.MSG_VERBOSE);
            }
        }
        if (i == 0 && otaRunCommandLine != null)
        {
            otaRunCommandLine = EMapFormat.format(otaRunCommandLine, args);
            exec = new Execute();
            exec.setAntRun(getProject());
            exec.setVMLauncher(true);
            exec.setCommandline(Commandline.translateCommandline(otaRunCommandLine));
            if (classPath != null && isClassFileAvailable()) exec.setStreamHandler(new RunTask.StackTraceTranslatorHandler(getProject().getBaseDir(), classPath.list()));
            log(Bundle.getMessage("MSG_ExecCmd", otaRunCommandLine), Project.MSG_VERBOSE); // No I18N
            try {
                i = exec.execute();
            } catch (IllegalThreadStateException itse) {
                //workaround for bug #85200, ITSE here is a race condition caused by JDK
                log(itse.getLocalizedMessage(), Project.MSG_VERBOSE);
            }
        }
        return i;
    }
    
    private boolean isClassFileAvailable()
    {
        try
        {
            Class.forName("org.netbeans.modules.classfile.ClassFile"); //NOI18N
            return true;
        }
        catch (Exception e)
        {}
        return false;
    }
    
    /**
     * Finds a free port to be used for listening for debugger connection.
     * @return free port number
     * @throws IOException
     */
    private int determineFreePort() throws IOException
    {
        final Socket sock = new Socket();
        sock.bind(null);
        final int port = sock.getLocalPort();
        sock.close();
        return port;
    }
    
    /** Setter for property jadFile.
     * @param jadFile New value of property jadFile.
     *
     */
    public void setJadFile(final File jadFile)
    {
        this.jadFile = jadFile;
    }
    
    /** Setter for property jarFile.
     * @param jarFile New value of property jarFile.
     *
     */
    public void setJarFile(final File jarFile)
    {
        this.jarFile = jarFile;
    }
    
    /** Setter for property jadUrl.
     * @param jadUrl New value of property jadUrl.
     *
     */
    public void setJadUrl(final String jadUrl)
    {
        this.jadUrl = jadUrl;
    }
    
    /** Setter for property execMethod.
     * @param execMethod New value of property execMethod.
     *
     */
    public void setExecMethod(final String execMethod)
    {
        this.execMethod = execMethod.toUpperCase();
    }
    
    /** Setter for property device.
     * @param device New value of property device.
     *
     */
    public void setDevice(final String device)
    {
        this.device = device;
    }
    
    /** Setter for property platformHome.
     * @param platformHome New value of property platformHome.
     *
     */
    public void setPlatformHome(final File platformHome)
    {
        this.platformHome = platformHome;
    }
    
    /** Setter for property platformType.
     * @param platformType New value of property platformType.
     *
     */
    public void setPlatformType(final String platformType)
    {
        this.platformType = platformType.toUpperCase();
    }
    
    /**
     * Setter for property classPath.
     * @param classPath New value of property classPath.
     */
    public void setClassPath(final Path classPath)
    {
        createClassPath().append(classPath);
    }
    
    /**
     * Creates ClassPath.
     * @return Created Path
     */
    public Path createClassPath()
    {
        if (classPath == null)
        {
            classPath = new Path(getProject());
        }
        return classPath.createPath();
    }
    
    /**
     * Setter for property classPathRef.
     * @param classPathRef New value of property classPathRef.
     */
    public void setClassPathRef(final Reference classPathRef)
    {
        createClassPath().setRefid(classPathRef);
    }
    
    /**
     * Setter for property securityDomain.
     * @param securityDomain New value of property securityDomain.
     */
    public void setSecurityDomain(final java.lang.String securityDomain)
    {
        this.securityDomain = securityDomain;
    }
    
    /** Setter for property debug.
     * @param debug New value of property debug.
     */
    public void setDebug(final boolean debug)
    {
        this.debug = debug;
    }
    
    /** Setter for debugger address property.
     * @param debuggerAddressProperty The debuggerAddressProperty to set.
     */
    public void setDebuggerAddressProperty(final String debuggerAddressProperty)
    {
        this.debuggerAddressProperty = debuggerAddressProperty;
    }
    
    /**
     * Setter for property debugAddress.
     * @param debugAddress New value of property debugAddress.
     */
    public void setDebugAddress(final String debugAddress)
    {
        this.debugAddress = debugAddress;
    }
    
    /**
     * Setter for property debugTransport.
     * @param debugTransport New value of property debugTransport.
     */
    public void setDebugTransport(final String debugTransport)
    {
        this.debugTransport = debugTransport;
    }
    
    /**
     * Setter for property debugServer.
     * @param debugServer New value of property debugServer.
     */
    public void setDebugServer(final boolean debugServer)
    {
        this.debugServer = debugServer;
    }
    
    /**
     * Setter for property debugSuspend.
     * @param debugSuspend New value of property debugSuspend.
     */
    public void setDebugSuspend(final boolean debugSuspend)
    {
        this.debugSuspend = debugSuspend;
    }
    
    /**
     * Setter for property commandLine.
     * @param commandLine New value of property commandLine.
     */
    public void setCommandLine(final String commandLine)
    {
        this.commandLine = commandLine;
    }
    
    /**
     * Setter for property cmdOptions.
     * @param cmdOptions New value of property cmdOptions.
     */
    public void setCmdOptions(final String cmdOptions)
    {
        
        this.cmdOptions = cmdOptions;
    }
    
    private class ListStreamHandler implements ExecuteStreamHandler, Runnable
    {
        
        private BufferedReader in;
        
        public void setProcessErrorStream(@SuppressWarnings("unused") InputStream is) throws IOException
        {
        }
        
        public void setProcessInputStream(@SuppressWarnings("unused") OutputStream os) throws IOException
        {
        }
        
        public void setProcessOutputStream(InputStream is) throws IOException
        {
            this.in = new BufferedReader(new InputStreamReader(is));
        }
        
        public void start() throws IOException
        {
            new Thread(this).start();
        }
        
        public void stop()
        {
            try
            {
                in.close();
            }
            catch (IOException ioe)
            {}
        }
        
        public void run()
        {
            try
            {
                int i = 0;
                String s;
                while ((s=in.readLine()) != null)
                {
                    s = s.trim();
                    if (s.startsWith("[") && s.endsWith("]")) try
                    { //NOI18N
                        i = Integer.parseInt(s.substring(1, s.length() - 1));
                    }
                    catch (NumberFormatException nfe)
                    {}
                    else if (s.startsWith("Installed From:")) try
                    { //NOI18N
                        s = s.substring(15).trim();
                        if (new URL(s).equals(jadUrlURL) || new URL(URLDecoder.decode(s, "UTF-8")).equals(jadUrlURL))
                        {
                            args.put("storagenum", String.valueOf(i)); //NOI18N
                            return;
                        }
                    }
                    catch (MalformedURLException mue)
                    {}
                }
            }
            catch (IOException ioe)
            {
                throw new BuildException(ioe);
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {}
            }
        }
        
    }
      
    
    private class StackTraceTranslatorHandler extends PumpStreamHandler
    {        
        protected final StackTraceTranslator stt;
        private boolean isBci;
        
        StackTraceTranslatorHandler(File root, String[] pathElements)
        {
            stt = new StackTraceTranslator(root, pathElements);
        }
        
        protected Thread createPump(final InputStream is, final OutputStream os, final boolean closeWhenExhausted)
        {
            final Thread result = new Thread(new Runnable()
            {
                private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                private final StringBuffer sb = new StringBuffer();
                protected boolean sleep;
                private final Thread timer = new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            while (true)
                            {
                                sleep = true;
                                while (sleep)
                                {
                                    sleep = false;
                                    Thread.sleep(400);
                                }
                                flush(true);
                            }
                        }
                        catch (InterruptedException ie)
                        {}
                    }
                });
                
                protected synchronized void flush(boolean forced)
                {
                    final Pattern STACK_TRACE_REGEXP_LINE = Pattern.compile("((?:\t| *|\\[catch\\] )at )((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*[a-zA-Z_$][a-zA-Z0-9_$]*)\\.([a-zA-Z_$<][a-zA-Z0-9_$>]*)\\(\\+([0-9]+)\\)[\t ]*[\n\r]*"); //NOI18N
                    final Pattern STACK_TRACE_BCI_REGEXP_LINE = Pattern.compile("((?:\t| *)- )((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*[a-zA-Z_$][a-zA-Z0-9_$]*)\\.([a-zA-Z_$<][a-zA-Z0-9_$>]*)\\(\\), bci=([0-9]+)[\t ]*[\n\r]*"); //NOI18N
                    try
                    {
                        if (buffer.size() > 0)
                        {
                            String line = buffer.toString(); //XXX may need handle encoding here
                            boolean bci = STACK_TRACE_BCI_REGEXP_LINE.matcher(line).matches();
                            if ( bci ){
                                isBci = true;
                            }
                            if (STACK_TRACE_REGEXP_LINE.matcher(line).matches() || bci)
                            {
                                sb.append(line);
                                buffer.reset();
                            }
                        }
                        if (forced || buffer.size() > 0)
                        {
                            if (sb.length() > 0) try
                            {
                                String translate = stt.translate(sb.toString(), isBci);
                                os.write( translate.getBytes()); //XXX may need handle encoding here
                                sb.setLength(0);
                            }
                            catch (IOException ioe)
                            {}
                        }
                        if (buffer.size() > 0)
                        {
                            os.write(buffer.toByteArray());
                            buffer.reset();
                        }
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }
                
                public void run()
                {
                    try
                    {
                        timer.setDaemon(true);
                        timer.start();
                        int i = is.read();
                        while (i >= 0)
                        {
                            while (i >= 0 && i != '\n' && i != '\r')
                            {
                                synchronized (this)
                                {
                                    buffer.write(i);
                                }
                                sleep = true;
                                i = is.read();
                            }
                            while (i == '\n' || i == '\r')
                            {
                                synchronized (this)
                                {
                                    buffer.write(i);
                                }
                                sleep = true;
                                i = is.read();
                            }
                            flush(i < 0);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        timer.interrupt();
                        if (closeWhenExhausted)
                        {
                            try
                            {
                                os.close();
                            }
                            catch (IOException e)
                            {
                            }
                        }
                    }
                }
            });
            result.setDaemon(true);
            return result;
        }
    }
}
