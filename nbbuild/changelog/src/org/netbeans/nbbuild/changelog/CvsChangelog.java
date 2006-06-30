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

/*
 * CvsChangelog.java
 *
 * Created on January 28, 2003, 4:31 PM
 */

package org.netbeans.nbbuild.changelog;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * @author rbalada
 */
public class CvsChangelog extends Task {

    
    private File topDir;         // toplevel directory, where to start changelogging
    private Date fromDate;
    private Date toDate;
    private String branch = "";
    private Vector modules = new Vector();
    private GlobalOptions globalOptions = new GlobalOptions();
    private CVSRoot cvsRoot;

    public void setTopDir(File f) {
        log("Setting topDir to: "+f.getAbsolutePath(), Project.MSG_VERBOSE);
        topDir = new File(f.getAbsolutePath());
    }

    public void setFromDate(String fd) {
        if ( fd != null ) {
            if ( ! fd.equals("")) {
                SimpleDateFormat parser = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
                try {
                    fromDate = parser.parse(fd);
                } catch (ParseException jpe) {
                    throw new BuildException("Wrong date format of fromdate attribute: \""+fd+"\"", jpe, location);
                }
            }
        }
    }
    
    public void setToDate(String td) {
        if ( td != null ) {
            if ( ! td.equals("")) {
                SimpleDateFormat parser = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
                try {
                    toDate = parser.parse(td);                
                } catch (ParseException jpe) {
                    throw new BuildException("Wrong date format of todate attribute: \""+td+"\"", jpe, location);
                }
            }
        }
    }

    public void setBranch(String s) {
        if (! (s.equalsIgnoreCase("trunk") || s.equalsIgnoreCase("dev"))) {
            branch = s;
        }
    }
    
    public void setModules(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        modules = new Vector();
        while (st.hasMoreTokens()) {
            modules.addElement((String) st.nextToken().trim());
        }
        if (modules.isEmpty()) {
            throw new BuildException("No modules specified for changelog");
        }
    }
 
    /**
     * Lookup the password in the .cvspass file. This file is looked for
     * in the user.home directory if the option cvs.passfile is not set
     * @param CVSRoot the CVS root for which the password is being searched
     * @return the password, scrambled
     */
    private String lookupPassword(String CVSRoot) throws BuildException {
        File passFile = new File(System.getProperty("cvs.passfile",
                                                    System.getProperty("user.home") +
                                                    "/.cvspass"));

        BufferedReader reader = null;
        String password = null;

        try {
            reader = new BufferedReader(new FileReader(passFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(CVSRoot)) {
                    password = line.substring(CVSRoot.length() + 1);
                    break;
                }
            }
        }
        catch (IOException e) {
            log("Could not read password for host: " + e, Project.MSG_ERR);
            throw new BuildException("Could not read password for host. Check passfile \"" + passFile.getName() + "\"", this.location);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    log("Warning: could not close password file.", Project.MSG_WARN);
                }
            }
        }
        return password;
    }

    public void setCvsRoot(String s) {
        log("Trying to set CVSROOT: " + s, Project.MSG_VERBOSE);
        try {
            cvsRoot = new CVSRoot(s);
            globalOptions.setCVSRoot(":"+cvsRoot.connectionType+":"+cvsRoot.user+"@"+cvsRoot.host+":"+cvsRoot.repository);
        } catch (Exception e) {
            throw new BuildException("Incorrect CVSROOT value: " + s, e, location);
        }
    }
    
    /**
     * A struct containing the various bits of information in a CVS root
     * string, allowing easy retrieval of individual items of information
     */
    private static class CVSRoot
    {
        public String connectionType;
        public String user;
        public String host;
        public String repository;
        
        public CVSRoot(String root) throws BuildException //IllegalArgumentException
        {
            if (!root.startsWith(":"))
                throw new BuildException("CVSROOT doesn't start with a colon"); //IllegalArgumentException();
            
            int oldColonPosition = 0;
            int colonPosition = root.indexOf(':', 1);
            if (colonPosition==-1)
                throw new BuildException("CVSROOT doesn't contain connection type"); //IllegalArgumentException();
            connectionType = root.substring(oldColonPosition + 1, colonPosition);
            oldColonPosition = colonPosition;
            colonPosition = root.indexOf('@', colonPosition+1);
            if (colonPosition==-1)
                throw new BuildException("CVSROOT doesn't have a username"); //IllegalArgumentException();
            user = root.substring(oldColonPosition+1, colonPosition);
            oldColonPosition = colonPosition;
            colonPosition = root.indexOf(':', colonPosition+1);
            if (colonPosition==-1)
                throw new BuildException("CVSROOT doesn't have a hostname"); //IllegalArgumentException();
            host = root.substring(oldColonPosition+1, colonPosition);
            repository = root.substring(colonPosition+1);
            if (connectionType==null || user==null || host==null ||
                repository==null)
                throw new BuildException("CVSROOT doesn't have a repository path"); //IllegalArgumentException();
        }
    }
    
    public PServerConnection connectCVS() throws BuildException {
        PServerConnection c = new PServerConnection();
        c.setUserName(cvsRoot.user);
        String pwd = lookupPassword(globalOptions.getCVSRoot());
        log("Encoded CVS password for CVSROOT \"" + globalOptions.getCVSRoot() + "\" is \"" + pwd + "\"", Project.MSG_VERBOSE);
        if ( pwd != null ) {
            c.setEncodedPassword(pwd);
        } else {
            c.setEncodedPassword("A");
        }
        c.setHostName(cvsRoot.host);
        c.setRepository(cvsRoot.repository);
        log("Connecting to repository", Project.MSG_VERBOSE);
        try {
            c.open();
        } catch (org.netbeans.lib.cvsclient.connection.AuthenticationException ae) {
            log("Incorrect login/password for \""+globalOptions.getCVSRoot()+"\". Do login from command line first.", Project.MSG_ERR);
            throw new BuildException(ae.getMessage(), ae, this.location);
        }
        return c;
    }
    
    public class BasicListener extends CVSAdapter
    {
        /**
         * Stores a tagged line
         */
        private final StringBuffer taggedLine = new StringBuffer();

        /**
         * Called when the server wants to send a message to be displayed to
         * the user. The message is only for information purposes and clients
         * can choose to ignore these messages if they wish.
         * @param e the event
         */
        public void messageSent(MessageEvent e)
        {
            String line = e.getMessage();

//            int logmask = e.isError()?Project.MSG_WARN:Project.MSG_INFO;
            if (e.isTagged())
            {
                String message = e.parseTaggedMessage(taggedLine, line);
	        // if we get back a non-null line, we have something
                // to output. Otherwise, there is more to come and we
                // should do nothing yet.
                if (message != null)
                {
                    if (e.isError()) {
                        log("TERROR: " + message);//, logmask);
                    } else {
                        log("TINFO:  " + message);
                    }
                }
            }
            else
            {
                if (e.isError()) {
                    log("LERROR: " + line);//, logmask);
                } else {
                    log("LINFO:  " + line);
                }
//                log(line);//, logmask);
            }
        }
        public void commandTerminated(TerminationEvent e)
        {
            log("Command terminated.",Project.MSG_ERR);
        }
        public void fileInfoGenerated(FileInfoEvent e)
        {
            LogInformation li = (LogInformation) e.getInfoContainer();           
            log("FileInfoGenerated for "+li.getRepositoryFilename(), Project.MSG_VERBOSE);
            log("Data could be: "+li.toString(), Project.MSG_VERBOSE);
        }
    }

    public void execute() throws BuildException {
        // connect to CVS server
        PServerConnection connection = connectCVS();
        // create new client instance
        Client client = new Client(connection, new StandardAdminHandler());     
        client.setLocalPath(topDir.getName());
        client.getEventManager().addCVSListener(new BasicListener());
        // create new command
        LogCommand command = new LogCommand();
        command.setNoTags(((branch == null) || (branch.equals(""))));
        // get file array for modules being changelogged
        Vector fmodules = new Vector();
        for (int i = 0; i < modules.size(); i++) {
            final String module = (String) modules.elementAt(i);
            File fmodule = new File(topDir, module);;
            if (fmodule.exists()) {
                fmodules.addElement(fmodule);
                log("Info: Adding \""+module+"\" ("+fmodule.getAbsolutePath()+") to an array", Project.MSG_VERBOSE);
            } else {
                log("Warning: \""+module+"\" ("+fmodule.getAbsolutePath()+") is not valid filesystem object", Project.MSG_WARN);
            }
        }
        log("Got "+fmodules.size()+" objects for changelog", Project.MSG_INFO);
        File[] absmodules = (File[]) fmodules.toArray(new File[fmodules.size()]);
        command.setFiles(absmodules);
        LogBuilder logbld = new LogBuilder(client.getEventManager(), command);
        command.setBuilder(logbld);
        log("cvs command: " + command.getCVSCommand(), Project.MSG_VERBOSE);
        log("in command's local directory: "+command.getLocalDirectory(), Project.MSG_VERBOSE);
        log("in client's local path: "+client.getLocalPath(), Project.MSG_VERBOSE);
        try {
            client.executeCommand(command, globalOptions);
        } catch (Exception e) {
            throw new BuildException("CVS command has failed", e, location);
        }
//        logbld.ii
        log("cvs command: " + command.getCVSCommand(), Project.MSG_VERBOSE);
    }
}