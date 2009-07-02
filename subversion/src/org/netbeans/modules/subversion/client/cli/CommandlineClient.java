/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.cli.commands.AddCommand;
import org.netbeans.modules.subversion.client.cli.commands.BlameCommand;
import org.netbeans.modules.subversion.client.cli.commands.CatCommand;
import org.netbeans.modules.subversion.client.cli.commands.CheckoutCommand;
import org.netbeans.modules.subversion.client.cli.commands.CommitCommand;
import org.netbeans.modules.subversion.client.cli.commands.CopyCommand;
import org.netbeans.modules.subversion.client.cli.commands.ListPropertiesCommand;
import org.netbeans.modules.subversion.client.cli.commands.ImportCommand;
import org.netbeans.modules.subversion.client.cli.commands.InfoCommand;
import org.netbeans.modules.subversion.client.cli.commands.ListCommand;
import org.netbeans.modules.subversion.client.cli.commands.LogCommand;
import org.netbeans.modules.subversion.client.cli.commands.MergeCommand;
import org.netbeans.modules.subversion.client.cli.commands.MkdirCommand;
import org.netbeans.modules.subversion.client.cli.commands.MoveCommand;
import org.netbeans.modules.subversion.client.cli.commands.PropertyDelCommand;
import org.netbeans.modules.subversion.client.cli.commands.PropertyGetCommand;
import org.netbeans.modules.subversion.client.cli.commands.PropertySetCommand;
import org.netbeans.modules.subversion.client.cli.commands.RelocateCommand;
import org.netbeans.modules.subversion.client.cli.commands.RemoveCommand;
import org.netbeans.modules.subversion.client.cli.commands.ResolvedCommand;
import org.netbeans.modules.subversion.client.cli.commands.RevertCommand;
import org.netbeans.modules.subversion.client.cli.commands.StatusCommand;
import org.netbeans.modules.subversion.client.cli.commands.StatusCommand.Status;
import org.netbeans.modules.subversion.client.cli.commands.SwitchToCommand;
import org.netbeans.modules.subversion.client.cli.commands.UpdateCommand;
import org.netbeans.modules.subversion.client.cli.commands.VersionCommand;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.AbstractClientAdapter;
import org.tigris.subversion.svnclientadapter.Annotations;
import org.tigris.subversion.svnclientadapter.Annotations.Annotation;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNConflictResolver;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback;
import org.tigris.subversion.svnclientadapter.ISVNMergeInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProgressListener;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;
import org.tigris.subversion.svnclientadapter.SVNKeywords;
import org.tigris.subversion.svnclientadapter.SVNNotificationHandler;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNRevisionRange;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CommandlineClient extends AbstractClientAdapter implements ISVNClientAdapter {
   
    private String user;
    private String psswd;
    private File configDir;
    private NotificationHandler notificationHandler;
    private SvnWcParser wcParser;
    private Commandline cli;     

    public static String ERR_CLI_NOT_AVALABLE = "commandline is not available";
    public static String ERR_JAVAHL_NOT_SUPPORTED = "unsupported javahl version";
    
    public CommandlineClient() {
        this.notificationHandler = new NotificationHandler();
        wcParser = new SvnWcParser();  
        cli = new Commandline();     
    }

    public void checkSupportedVersion() throws SVNClientException {
        VersionCommand cmd = new VersionCommand();        
        try {            
            config(cmd);
            cli.exec(cmd);
            checkErrors(cmd);            
            if(!cmd.checkForErrors()) {
                if (cmd.isUnsupportedVersion()) {
                    Subversion.LOG.log(Level.WARNING, "Unsupported svn version. You need >= 1.3");
                }
                throw new SVNClientException(ERR_CLI_NOT_AVALABLE + "\n" + cmd.getOutput());               
            }                       
        } catch (IOException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
            throw new SVNClientException(ERR_CLI_NOT_AVALABLE);
        }        
    }

    public void checkSupportedJavaHlVersion() throws SVNClientException {
        VersionCommand cmd = new VersionCommand();
        try {
            config(cmd);
            cli.exec(cmd);
            checkErrors(cmd);
            if(!cmd.isSupportedJavaHl()) {
                Subversion.LOG.log(Level.WARNING, "JavaHl for svn version >=1.6 not supported yet.");
                throw new SVNClientException(ERR_JAVAHL_NOT_SUPPORTED + "\n" + cmd.getOutput());
            }
        } catch (IOException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
            throw new SVNClientException(ERR_CLI_NOT_AVALABLE);
        }
    }
    
    public void addNotifyListener(ISVNNotifyListener l) {
        notificationHandler.add(l);
    }
    
    public void removeNotifyListener(ISVNNotifyListener l) {
        notificationHandler.remove(l);
    }

    public void setUsername(String user) {
        this.user = user;
    }

    public void setPassword(String psswd) {
        this.psswd = psswd; 
    }

    public void setConfigDirectory(File file) throws SVNClientException {
        this.configDir = file; 
    }

    public SVNNotificationHandler getNotificationHandler() {
        return notificationHandler;
    }
    
    public void addFile(File file) throws SVNClientException {
        addFile(new File[] { file }, false);
    }

    public void addFile(File[] file, boolean recursive) throws SVNClientException {
        AddCommand cmd = new AddCommand(file, recursive, false);
        exec(cmd);
    }

    public void addDirectory(File dir, boolean recursive) throws SVNClientException {
        addDirectory(dir, recursive, false);
    }

    public void addDirectory(File dir, boolean recursive, boolean force) throws SVNClientException {
        AddCommand cmd = new AddCommand(new File[] { dir } , recursive, force);
        exec(cmd);
    }

    public void checkout(SVNUrl url, File file, SVNRevision revision, boolean recurse) throws SVNClientException {
        CheckoutCommand cmd = new CheckoutCommand(url, file, revision, recurse);
        exec(cmd);
    }

    public long commit(File[] files, String message, boolean recurse) throws SVNClientException {
        return commit(files, message, false, recurse);
    }

    public long commit(File[] files, String message, boolean keep, boolean recursive) throws SVNClientException {        
        int retry = 0;
        CommitCommand cmd = null;
        while (true) {
            try {
                cmd = new CommitCommand(files, keep, recursive, message); // prevent cmd reuse
                exec(cmd);
                break;
            } catch (SVNClientException e) {
                if (e.getMessage().startsWith("svn: Attempted to lock an already-locked dir")) {
                    Subversion.LOG.fine("ComandlineClient.comit() : " + e.getMessage());
                    try {
                        retry++;
                        if (retry > 14) {
                            throw e;
                        }
                        Thread.sleep(retry * 50);
                    } catch (InterruptedException ex) {                        
                        break;
                    }
                } else {
                    throw e;
                }
            }                
        }               
        return cmd != null ? cmd.getRevision() : SVNRevision.SVN_INVALID_REVNUM;
    }
    
    public ISVNDirEntry[] getList(SVNUrl url, SVNRevision revision, boolean recursivelly) throws SVNClientException {
        ListCommand cmd = new ListCommand(url, revision, recursivelly);
        exec(cmd);
        return cmd.getEntries();
    }    

    @Override
    public ISVNInfo getInfo(SVNUrl url) throws SVNClientException {
        return super.getInfo(url);
    }

    public ISVNInfo getInfo(File file) throws SVNClientException {
        return getInfoFromWorkingCopy(file);
    }

    private ISVNInfo[] getInfo(File[] files, SVNRevision revision, SVNRevision pegging) throws SVNClientException, SVNClientException {
        if(files == null || files.length == 0) {
            return new ISVNInfo[0];
        }
        InfoCommand infoCmd = new InfoCommand(files, revision, pegging);
        exec(infoCmd);
        ISVNInfo[] infos = infoCmd.getInfo();

        return infos;
    }
    
    public ISVNInfo getInfo(SVNUrl url, SVNRevision revision, SVNRevision pegging) throws SVNClientException {
        InfoCommand cmd = new InfoCommand(url, revision, pegging);
        exec(cmd);
        return cmd.getInfo()[0];
    }  
    
    public void copy(File fileForm, File fileTo) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }    

    public void copy(File file, SVNUrl url, String msg) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(file, url, msg);
        exec(cmd);
    }

    public void copy(SVNUrl url, File file, SVNRevision rev) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(url, file, rev);
        exec(cmd);
    }

    public void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) throws SVNClientException {
        copy(fromUrl, toUrl, msg, rev, false);
    }

    public void remove(SVNUrl[] url, String msg) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove(File[] files, boolean force) throws SVNClientException {
        RemoveCommand cmd = new RemoveCommand(files, user, force);
        exec(cmd);
    }

    public void doImport(File File, SVNUrl url, String msg, boolean recursivelly) throws SVNClientException {
        ImportCommand cmd = new ImportCommand(File, url, recursivelly, msg);
        exec(cmd);
    }

    public void mkdir(SVNUrl url, String msg) throws SVNClientException {
        MkdirCommand cmd = new MkdirCommand(url, msg);
        exec(cmd);
    }

    @Override
    public void mkdir(SVNUrl url, boolean parents, String msg) throws SVNClientException {        
        if(parents) {
            List<SVNUrl> parent = getAllNotExistingParents(url);
            for (SVNUrl p : parent) {
                mkdir(p, msg);
            }
        } else {
            mkdir(url, msg);   
        }        
    }
    
    public void mkdir(File file) throws SVNClientException {
        MkdirCommand cmd = new MkdirCommand(file);
        exec(cmd);        
    }

    public void move(File fromFile, File toFile, boolean force) throws SVNClientException {
        MoveCommand cmd = new MoveCommand(fromFile, toFile, force);
        exec(cmd);
    }

    public void move(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) throws SVNClientException {
        MoveCommand cmd = new MoveCommand(fromUrl, toUrl, msg, rev);
        exec(cmd);
    }

    public long update(File file, SVNRevision rev, boolean recursivelly) throws SVNClientException {
        UpdateCommand cmd = new UpdateCommand(new File[] { file }, rev, recursivelly, false);
        exec(cmd);
        return cmd.getRevision();        
    }

    public long[] update(File[] files, SVNRevision rev, boolean recursivelly, boolean ignoreExternals) throws SVNClientException {        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void revert(File file, boolean recursivelly) throws SVNClientException {
        revert(new File[]{file}, recursivelly);
    }

    public void revert(File[] files, boolean recursivelly) throws SVNClientException {
        if(files == null || files.length == 0) {
            return;
        }
        RevertCommand cmd = new RevertCommand(files, recursivelly);
        exec(cmd);
    }

    public ISVNStatus[] getStatus(File[] files) throws SVNClientException {
        
        Map<File, ISVNStatus> unversionedMap = new HashMap<File, ISVNStatus>();
        List<File> filesForStatus = new ArrayList<File>();
        List<File> filesForInfo = new ArrayList<File>();
        for (File f : files) {
            if(!isManaged(f)) {
                unversionedMap.put(f, new SVNStatusUnversioned(f));
            } else {
                filesForStatus.add(f);
            }
        }

        Status[] statusValues = new Status[] {};
        if (!filesForStatus.isEmpty()) {
            StatusCommand statusCmd = new StatusCommand(filesForStatus.toArray(new File[filesForStatus.size()]), true, false, false, false);
            exec(statusCmd);
            statusValues = statusCmd.getStatusValues();
        }
        for (Status status : statusValues) {
            if(isManaged(status.getWcStatus())) {
                filesForInfo.add(new File(status.getPath()));
            }
        }
        Map<File, ISVNInfo> infoMap = new HashMap<File, ISVNInfo>();
        if (!filesForInfo.isEmpty()) {
            ISVNInfo[] infos = getInfo(filesForInfo.toArray(new File[filesForInfo.size()]), null, null);
            for (ISVNInfo info : infos) infoMap.put(info.getFile(), info);
        }
        
        Map<File, ISVNStatus> statusMap = new HashMap<File, ISVNStatus>();
        for (Status status : statusValues) {
            File file = new File(status.getPath());
            if (status == null || !isManaged(status.getWcStatus())) {
                if (!SVNStatusKind.UNVERSIONED.equals(status.getRepoStatus())) {
                    statusMap.put(file, new CLIStatus(status, status.getPath()));
                } else {
                    statusMap.put(file, new SVNStatusUnversioned(file, SVNStatusKind.IGNORED.equals(status.getWcStatus())));
                }
            } else {
                ISVNInfo info = infoMap.get(file);
                if (info != null) {
                    statusMap.put(file, new CLIStatus(status, info));
                }
            }            
        }
        
        List<ISVNStatus> ret = new ArrayList<ISVNStatus>();
        for (File f : files) {
            ISVNStatus s = statusMap.get(f);
            if(s == null) {
                s = unversionedMap.get(f);
            }
            if(s != null) {
                ret.add(s);
            }
        }
        return ret.toArray(new ISVNStatus[ret.size()]);
    }

    public ISVNStatus[] getStatus(File file, boolean descend, boolean getAll, boolean contactServer) throws SVNClientException {
        return getStatus(file, descend, getAll, contactServer, false);
    }

    // XXX merge with get status
    public ISVNStatus[] getStatus(File file, boolean descend, boolean getAll, boolean contactServer, boolean ignoreExternals) throws SVNClientException {
        Status[] statusValues = null;
        try {
            if(!isManaged(file)) {
                return new ISVNStatus[] {new SVNStatusUnversioned(file)};
            }
            StatusCommand statusCmd = new StatusCommand(new File[] { file }, getAll, descend, contactServer, ignoreExternals);
            exec(statusCmd);
            statusValues = statusCmd.getStatusValues();
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("is not a working copy") > -1) { 
                return new ISVNStatus[] {new SVNStatusUnversioned(file)};
            } else {
                throw e;
            }                    
        }        

        List<File> filesForInfo = new ArrayList<File>();        
        for (Status status : statusValues) {
            if(isManaged(status.getWcStatus())) {
                filesForInfo.add(new File(status.getPath()));
            } 
        }
        ISVNInfo[] infos = getInfo(filesForInfo.toArray(new File[filesForInfo.size()]), null, null);        
       
        Map<File, ISVNInfo> infoMap = new HashMap<File, ISVNInfo>();
        for (ISVNInfo info : infos) infoMap.put(info.getFile(), info);
        
        Map<File, ISVNStatus> statusMap = new HashMap<File, ISVNStatus>();
        for (Status status : statusValues) {
            File f = new File(status.getPath());
            if (status == null || !isManaged(status.getWcStatus())) {
                if (!SVNStatusKind.UNVERSIONED.equals(status.getRepoStatus())) {
                    statusMap.put(f, new CLIStatus(status, status.getPath()));
                } else {
                    statusMap.put(f, new SVNStatusUnversioned(f, SVNStatusKind.IGNORED.equals(status.getWcStatus())));
                }
            } else {
                ISVNInfo info = infoMap.get(f);
                if (info != null) {
                    statusMap.put(f, new CLIStatus(status, info));
                }
            }            
        }
        
        List<ISVNStatus> ret = new ArrayList<ISVNStatus>();        
        for (Status status : statusValues) {
            File f = new File(status.getPath());
            ISVNStatus s = statusMap.get(f);
            if(s == null) {
                s = new SVNStatusUnversioned(f);
            }
            ret.add(s);
        }
        return ret.toArray(new ISVNStatus[ret.size()]);        
    }
    
    @Override
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return super.getLogMessages(url, revStart, revEnd);
    }

    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(url, null, revStart, revEnd, false, fetchChangePath);
    }

    public ISVNLogMessage[] getLogMessages(SVNUrl url, String[] paths, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        LogCommand cmd = new LogCommand(url, paths, revStart, revEnd, stopOnCopy, fetchChangePath, 0);
        return getLog(cmd);
    }

    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revPeg, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        LogCommand cmd = new LogCommand(url, null, revStart, revEnd, stopOnCopy, fetchChangePath, limit);
        return getLog(cmd);
    }
    
    @Override
    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return super.getLogMessages(file, revStart, revEnd);
    }

    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, false, fetchChangePath);
    }

    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(file, revStart, revEnd, stopOnCopy, fetchChangePath, 0);
    }

    public ISVNLogMessage[] getLogMessages(File file, SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath, long limit) throws SVNClientException {
        LogCommand logCmd;
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if (info.getSchedule().equals(SVNScheduleKind.ADD) && 
            info.getCopyUrl() != null) 
        {
            logCmd = new LogCommand(info.getCopyUrl(), null, revStart, revEnd, stopOnCopy, fetchChangePath, limit);
        } else {
            logCmd = new LogCommand(file, revStart, revEnd, stopOnCopy, fetchChangePath, limit);
        }
        return getLog(logCmd);
    }

    private ISVNLogMessage[] getLog(LogCommand cmd) throws SVNClientException {
        exec(cmd);
        return cmd.getLogMessages();
    }
    
    public InputStream getContent(SVNUrl url, SVNRevision rev) throws SVNClientException {
        CatCommand cmd = new CatCommand(url, rev);
        exec(cmd);
        return cmd.getOutput();
    }

    public InputStream getContent(File file, SVNRevision rev) throws SVNClientException {
        CatCommand cmd = new CatCommand(file, rev);
        exec(cmd);
        return cmd.getOutput();
    }

    public void propertySet(File file, String name, String value, boolean rec) throws SVNClientException {
        ISVNStatus[] oldStatus = getStatus(file, rec, false);
        PropertySetCommand cmd = new PropertySetCommand(name, value, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
    }

    public void propertySet(File file, String name, File propFile, boolean rec) throws SVNClientException, IOException {
        ISVNStatus[] oldStatus = getStatus(file, rec, false);
        PropertySetCommand cmd = new PropertySetCommand(name, propFile, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
    }

    public void propertyDel(File file, String name, boolean rec) throws SVNClientException {
        ISVNStatus[] oldStatus = getStatus(file, rec, false);
        PropertyDelCommand cmd = new PropertyDelCommand(file, name, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, oldStatus);
    }
    
    public ISVNProperty propertyGet(final File file, final String name) throws SVNClientException {
        return propertyGet(new PropertyGetCommand(file, name), name, null, file);
    }

    @Override
    public ISVNProperty propertyGet(SVNUrl url, String name) throws SVNClientException {
        return super.propertyGet(url, name);
    }

    public ISVNProperty propertyGet(final SVNUrl url, SVNRevision rev, SVNRevision peg, final String name) throws SVNClientException {
        return propertyGet(new PropertyGetCommand(url, rev, peg, name), name, url, null);
    }

    ISVNProperty propertyGet(PropertyGetCommand cmd, final String name, final SVNUrl url, final File file) throws SVNClientException {
        exec(cmd);
        final byte[] bytes = cmd.getOutput();            
        if(bytes == null || bytes.length == 0) {
            return null;
        }
        return new ISVNProperty() {
            public String getName() {
                return name;
            }
            public String getValue() {
                return new String(bytes);
            }
            public File getFile() {
                return file;
            }
            public SVNUrl getUrl() {
                return url;
            }
            public byte[] getData() {
                return bytes;
            }
        };        
    }
    
    @Override
    public List getIgnoredPatterns(File file) throws SVNClientException {
        return super.getIgnoredPatterns(file);
    }

    @Override
    public void addToIgnoredPatterns(File file, String value) throws SVNClientException {
        super.addToIgnoredPatterns(file, value);
    }

    @Override
    public void setIgnoredPatterns(File file, List l) throws SVNClientException {
        super.setIgnoredPatterns(file, l);
    }

    public ISVNAnnotations annotate(SVNUrl url, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {
        return annotate(new BlameCommand(url, revStart, revEnd), new CatCommand(url, revEnd));
    }

    public ISVNAnnotations annotate(File file, SVNRevision revStart, SVNRevision revEnd) throws SVNClientException {        
        BlameCommand blameCommand;
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if (info.getSchedule().equals(SVNScheduleKind.ADD) && 
            info.getCopyUrl() != null) 
        {
            blameCommand = new BlameCommand(info.getCopyUrl(), revStart, revEnd);
        } else {
            blameCommand = new BlameCommand(file, revStart, revEnd);
        }
        return annotate(blameCommand, new CatCommand(file, revEnd));
    }
    
    public ISVNAnnotations annotate(BlameCommand blameCmd, CatCommand catCmd) throws SVNClientException {
        exec(blameCmd);
        Annotation[] annotations = blameCmd.getAnnotation();        
        exec(catCmd);
        InputStream is = catCmd.getOutput();
        
        Annotations ret = new Annotations();
        BufferedReader r = new BufferedReader(new InputStreamReader(is)); 
        try {
            for (Annotation annotation : annotations) {
                String line = null;
                try {
                    line = r.readLine();
                } catch (IOException ex) {
                    // try at least to return the annotations
                    Subversion.LOG.log(Level.INFO, ex.getMessage(), ex);
                }
                annotation.setLine(line);
                ret.addAnnotation(annotation);            
            }
        } finally {
            if (r != null) { 
                try { r.close(); } catch (IOException e) {} 
            }
        }        
        return ret;
    }

    public ISVNProperty[] getProperties(File file) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(file, false);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<ISVNProperty>(names.size());
        for (String name : names) {
            props.add(propertyGet(file, name));
        }
        return props.toArray(new ISVNProperty[props.size()]);
    }

    public ISVNProperty[] getProperties(SVNUrl url) throws SVNClientException {
        ListPropertiesCommand cmd = new ListPropertiesCommand(url, false);
        exec(cmd);
        List<String> names = cmd.getPropertyNames();
        List<ISVNProperty> props = new ArrayList<ISVNProperty>(names.size());
        for (String name : names) {
            props.add(propertyGet(url, name));
        }
        return props.toArray(new ISVNProperty[props.size()]);
    }

    public void resolved(File file) throws SVNClientException {
        ResolvedCommand cmd = new ResolvedCommand(file, false);
        exec(cmd);
    }

    public void cancelOperation() throws SVNClientException {
        cli.interrupt();
    }

    public void switchToUrl(File file, SVNUrl url, SVNRevision rev, boolean rec) throws SVNClientException {
        SwitchToCommand cmd = new SwitchToCommand(file, url, rev, rec);
        exec(cmd);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, File file, boolean force, boolean recurse) throws SVNClientException {
       super.merge(startUrl, startRev, endUrl, endRev, file, force, recurse);
    }

    @Override
    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, File file, boolean force, boolean recurse, boolean dryRun) throws SVNClientException {
        super.merge(startUrl, startRev, endUrl, endRev, file, force, recurse, dryRun);
    }       

    public void merge(SVNUrl startUrl, SVNRevision startRev, SVNUrl endUrl, SVNRevision endRev, File file, boolean force, boolean recurse, boolean dryRun, boolean ignoreAncestry) throws SVNClientException {
        MergeCommand cmd = new MergeCommand(startUrl, endUrl, startRev, endRev, file, recurse, force, ignoreAncestry, dryRun);
        exec(cmd);
    }

    public void relocate(String from, String to, String path, boolean rec) throws SVNClientException {
        RelocateCommand cmd = new RelocateCommand(from, to, path, rec);
        exec(cmd);
    }
    
    // parser start
    public ISVNStatus getSingleStatus(File file) throws SVNClientException {
        try {
            return wcParser.getSingleStatus(file);
        } catch (LocalSubversionException ex) {
            throw new SVNClientException(ex);
        }
    }
    
    public ISVNStatus[] getStatus(File file, boolean descend, boolean getAll) throws SVNClientException {
        try {
            return wcParser.getStatus(file, descend, getAll);
        } catch (LocalSubversionException ex) {
            throw new SVNClientException(ex);
        }
    }

    public ISVNInfo getInfoFromWorkingCopy(File file) throws SVNClientException {
        try {
            return wcParser.getInfoFromWorkingCopy(file);
        } catch (LocalSubversionException ex) {
            throw new SVNClientException(ex);
        }
    }
    
    // parser end
        
    private void exec(SvnCommand cmd) throws SVNClientException {
        try {            
            config(cmd);
            cli.exec(cmd);
        } catch (IOException ex) {
            Subversion.LOG.log(Level.FINE, null, ex);
            throw new SVNClientException(ex);
        }
        checkErrors(cmd);
    }
    
    private void config(SvnCommand cmd) {
        cmd.setNotificationHandler(notificationHandler);
        cmd.setConfigDir(configDir);
        cmd.setUsername(user);
        cmd.setPassword(psswd);
    }
    
    private void checkErrors(SvnCommand cmd) throws SVNClientException {
        List<String> errors = cmd.getCmdError();
        if(errors == null || errors.size() == 0) {
            return;
        }        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < errors.size(); i++) {
            sb.append(errors.get(i));
            if (i < errors.size() - 1) {
                sb.append('\n');
            }
        }
        throw new SVNClientException(sb.toString());
    }

    private List<SVNUrl> getAllNotExistingParents(SVNUrl url) throws SVNClientException {        
        List<SVNUrl> ret = new ArrayList<SVNUrl>();
        if(url == null) {
            return ret;
        }
        try {
            getInfo(url);            
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("Not a valid URL") > -1) { 
                ret.addAll(getAllNotExistingParents(url.getParent()));
                ret.add(url);                        
            } else {
                throw e;
            }                    
        }        
        return ret;
    }

    private boolean isManaged(SVNStatusKind s) {
        return !(s.equals(SVNStatusKind.UNVERSIONED) ||
                 s.equals(SVNStatusKind.NONE) ||
                 s.equals(SVNStatusKind.IGNORED) ||
                 s.equals(SVNStatusKind.EXTERNAL));
    }

    private boolean hasMetadata(File file) {
        return new File(file, SvnUtils.SVN_ENTRIES_DIR).canRead();
    }
    
    private boolean isManaged(File file) {        
        return hasMetadata(file.getParentFile()) || hasMetadata(file);        
    }
    
    // unsupported start
    
    @Override
    public long[] commitAcrossWC(File[] arg0, String arg1, boolean arg2, boolean arg3, boolean arg4) throws SVNClientException {
        return super.commitAcrossWC(arg0, arg1, arg2, arg3, arg4);
    }

    public ISVNDirEntry getDirEntry(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNDirEntry getDirEntry(File arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void doExport(SVNUrl arg0, File arg1, SVNRevision arg2, boolean arg3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void doExport(File arg0, File arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRevProperty(SVNUrl arg0, Number arg1, String arg2, String arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(File arg0, SVNRevision arg1, File arg2, SVNRevision arg3, File arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(File arg0, SVNRevision arg1, File arg2, SVNRevision arg3, File arg4, boolean arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(File arg0, File arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, File arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, File arg4, boolean arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, File arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(File arg0, SVNUrl arg1, SVNRevision arg2, File arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SVNKeywords getKeywords(File arg0) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setKeywords(File arg0, SVNKeywords arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SVNKeywords addKeywords(File arg0, SVNKeywords arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SVNKeywords removeKeywords(File arg0, SVNKeywords arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void createRepository(File arg0, String arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void lock(SVNUrl[] arg0, String arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unlock(SVNUrl[] arg0, boolean arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void lock(File[] arg0, String arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unlock(File[] arg0, boolean arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean statusReturnsRemoteInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean canCommitAcrossWC() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAdminDirectoryName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isAdminDirectory(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPasswordCallback(ISVNPromptUserPassword arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNDirEntry[] getList(File arg0, SVNRevision arg1, boolean arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void cleanup(File arg0) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void notifyChangedStatus(File file, boolean rec, ISVNStatus[] oldStatuses) throws SVNClientException {
        Map<File, ISVNStatus> oldStatusMap = new HashMap<File, ISVNStatus>();
        for (ISVNStatus s : oldStatuses) {
            oldStatusMap.put(s.getFile(), s);
        }
        ISVNStatus[] newStatuses = getStatus(file, rec, false);
        for (ISVNStatus newStatus : newStatuses) {
            ISVNStatus oldStatus = oldStatusMap.get(newStatus.getFile());
            if( (oldStatus == null && newStatus != null) ||
                 oldStatus.getTextStatus() != newStatus.getTextStatus() ||
                 oldStatus.getPropStatus() != newStatus.getPropStatus())
            {
                notificationHandler.notifyListenersOfChange(newStatus.getPath()); /// onNotify(cmd.getAbsoluteFile(s.getFile().getAbsolutePath()), null);   
            }            
       }
    }

    public void getLogMessages(File arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, boolean arg4, boolean arg5, long arg6, boolean arg7, String[] arg8, ISVNLogMessageCallback arg9) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getLogMessages(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, boolean arg4, boolean arg5, long arg6, boolean arg7, String[] arg8, ISVNLogMessageCallback arg9) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNInfo[] getInfo(File arg0, boolean arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // unsupported start

    class NotificationHandler extends SVNNotificationHandler {   }

    public boolean isThreadsafe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addConflictResolutionCallback(ISVNConflictResolver arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setProgressListener(ISVNProgressListener arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void checkout(SVNUrl arg0, File arg1, SVNRevision arg2, int arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNDirEntry[] getList(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNDirEntry[] getList(File arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Copies all files from <code>files</code> to repository URL at <code>targetUrl</code>.
     * @param files array of files which will be copied
     * @param targetUrl destination repository Url
     * @param message commit message
     * @param addAsChild not supported
     * @param makeParents creates parent folders
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    public void copy(File[] files, SVNUrl targetUrl, String message, boolean addAsChild, boolean makeParents) throws SVNClientException {
        for (File file : files) {
            CopyCommand cmd = new CopyCommand(file, targetUrl, message, makeParents);
            exec(cmd);
        }
    }

    public void copy(SVNUrl arg0, File arg1, SVNRevision arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void copy(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev, boolean makeParents) throws SVNClientException {
        CopyCommand cmd = new CopyCommand(fromUrl, toUrl, msg, rev, makeParents);
        exec(cmd);
    }

    public void copy(SVNUrl[] arg0, SVNUrl arg1, String arg2, SVNRevision arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long update(File arg0, SVNRevision arg1, int arg2, boolean arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long[] update(File[] arg0, SVNRevision arg1, int arg2, boolean arg3, boolean arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNLogMessage[] getLogMessages(File arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, boolean arg4, boolean arg5, long arg6, boolean arg7) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNLogMessage[] getLogMessages(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, boolean arg4, boolean arg5, long arg6, boolean arg7) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InputStream getContent(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, File arg4, int arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void diff(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, File arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNAnnotations annotate(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNAnnotations annotate(File arg0, SVNRevision arg1, SVNRevision arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resolve(File arg0, int arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void switchToUrl(File arg0, SVNUrl arg1, SVNRevision arg2, int arg3, boolean arg4, boolean arg5, boolean arg6) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void switchToUrl(File arg0, SVNUrl arg1, SVNRevision arg2, SVNRevision arg3, int arg4, boolean arg5, boolean arg6, boolean arg7) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, File arg4, boolean arg5, int arg6, boolean arg7, boolean arg8, boolean arg9) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mergeReintegrate(SVNUrl arg0, SVNRevision arg1, File arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void merge(SVNUrl arg0, SVNRevision arg1, SVNRevisionRange[] arg2, File arg3, boolean arg4, int arg5, boolean arg6, boolean arg7, boolean arg8) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNMergeInfo getMergeInfo(File arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNMergeInfo getMergeInfo(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNLogMessage[] getMergeinfoLog(int arg0, File arg1, SVNRevision arg2, SVNUrl arg3, SVNRevision arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNLogMessage[] getMergeinfoLog(int arg0, SVNUrl arg1, SVNRevision arg2, SVNUrl arg3, SVNRevision arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SVNDiffSummary[] diffSummarize(SVNUrl arg0, SVNRevision arg1, SVNUrl arg2, SVNRevision arg3, int arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public SVNDiffSummary[] diffSummarize(SVNUrl arg0, SVNRevision arg1, SVNRevision arg2, SVNRevision arg3, int arg4, boolean arg5) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] suggestMergeSources(File arg0) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] suggestMergeSources(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
