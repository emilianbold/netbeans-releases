/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.netbeans.modules.subversion.client.cli.commands.SwitchToCommand;
import org.netbeans.modules.subversion.client.cli.commands.UpdateCommand;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.tigris.subversion.svnclientadapter.AbstractClientAdapter;
import org.tigris.subversion.svnclientadapter.Annotations;
import org.tigris.subversion.svnclientadapter.Annotations.Annotation;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNKeywords;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNNotificationHandler;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CommandlineClient extends AbstractClientAdapter implements ISVNClientAdapter, ISVNNotifyListener {

    private Set<ISVNNotifyListener> listeners = new HashSet<ISVNNotifyListener>(3);
    
    private Commandline cli = new Commandline();
    private String user;
    private String psswd;
    private File configDir;
    
    // XXX singleton
    private SvnWcParser wcParser = new SvnWcParser();  
    
    public void addNotifyListener(ISVNNotifyListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }
    public void removeNotifyListener(ISVNNotifyListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    public void setUsername(String user) {
        this.user = user;  // XXX
    }

    public void setPassword(String psswd) {
        this.psswd = psswd;  // XXX
    }

    public void setConfigDirectory(File file) throws SVNClientException {
        this.configDir = file; // XXX
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
        CommitCommand cmd = new CommitCommand(files, keep, recursive, message);
        exec(cmd);
        return cmd.getRevision();
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

    public ISVNInfo getInfo(SVNUrl url, SVNRevision revision, SVNRevision pegging) throws SVNClientException {
        InfoCommand cmd = new InfoCommand(url, revision, pegging);
        exec(cmd);
        return cmd.getInfo();
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
        CopyCommand cmd = new CopyCommand(fromUrl, toUrl, msg, rev);
        exec(cmd);
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
        RevertCommand cmd = new RevertCommand(files, recursivelly);
        exec(cmd);
    }

    public ISVNStatus[] getStatus(File[] files) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
//        List<ISVNStatus> ret = new ArrayList<ISVNStatus>();
//        for (File f : files) {
//            if(!SvnUtils.hasMetadata(f)) {
//                ret.add(new SVNStatusUnversioned(f));
//            }
//        }
    }

    public ISVNStatus[] getStatus(File arg0, boolean arg1, boolean arg2, boolean arg3) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ISVNStatus[] getStatus(File arg0, boolean arg1, boolean arg2, boolean arg3, boolean arg4) throws SVNClientException {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return execBinary(cmd);
    }

    public InputStream getContent(File file, SVNRevision rev) throws SVNClientException {
        CatCommand cmd = new CatCommand(file, rev);
        return execBinary(cmd);        
    }

    public void propertySet(File file, String name, String value, boolean rec) throws SVNClientException {
        PropertySetCommand cmd = new PropertySetCommand(name, value, file, rec);
        exec(cmd);
        // XXX this is sick. the status just isn't relevant
        notifyChangedStatus(file, rec, cmd);
    }

    public void propertySet(File file, String name, File propFile, boolean rec) throws SVNClientException, IOException {
        PropertySetCommand cmd = new PropertySetCommand(name, propFile, file, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, cmd);
    }

    public void propertyDel(File file, String name, boolean rec) throws SVNClientException {
        PropertyDelCommand cmd = new PropertyDelCommand(file, name, rec);
        exec(cmd);
        notifyChangedStatus(file, rec, cmd);
    }
    
    public ISVNProperty propertyGet(final File file, final String name) throws SVNClientException {
        // XXX
        try {
            PropertyGetCommand cmd = new PropertyGetCommand(file, name);
            InputStream is = execBinary(cmd);
            int data = -1;
            final List<Byte> byteList = new ArrayList<Byte>();
            while ((data = is.read()) != -1) {
                byteList.add(new Byte((byte) data));                
            }
            if(byteList.size() == 0) {
                return null;
            }
            final byte[] bytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                bytes[i] = byteList.get(i);                
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
                    return null;
                }
                public byte[] getData() {
                    return bytes;
                }
            };
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
    }

    @Override
    public ISVNProperty propertyGet(SVNUrl url, String name) throws SVNClientException {
        return super.propertyGet(url, name);
    }

    public ISVNProperty propertyGet(final SVNUrl url, SVNRevision rev, SVNRevision peg, final String name) throws SVNClientException {
        // XXX
        try {
            PropertyGetCommand cmd = new PropertyGetCommand(url, rev, peg, name);
            InputStream is = execBinary(cmd);
            int data = -1;
            final List<Byte> byteList = new ArrayList<Byte>();
            while ((data = is.read()) != -1) {
                byteList.add(new Byte((byte) data));                
            }
            if(byteList.size() == 0) {
                return null;
            }
            final byte[] bytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                bytes[i] = byteList.get(i);                
            }
            return new ISVNProperty() {
                public String getName() {
                    return name;
                }
                public String getValue() {
                    return new String(bytes);
                }
                public File getFile() {
                    return null;
                }
                public SVNUrl getUrl() {
                    return url;
                }
                public byte[] getData() {
                    return bytes;
                }
            };
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
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
        InputStream is = execBinary(catCmd);
        
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
    
    // notify listener start
    public void setCommand(int arg0) { /* boring */ }
    public void logCommandLine(String arg0) { /* boring */ }
    public void logMessage(String arg0) { /* boring */ }
    public void logError(String arg0) { /* boring */ }
    public void logRevision(long arg0, String arg1) { /* boring */ }
    public void logCompleted(String arg0) { /* boring */ }
    public void onNotify(File file, SVNNodeKind kind) {
        ISVNNotifyListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new ISVNNotifyListener[listeners.size()]);
        }
        for (ISVNNotifyListener l : la) {
            l.onNotify(file, kind);
        }
    }    
    // notify listener end
    
    private void exec(SvnCommand cmd) throws SVNClientException {
        try {
            cmd.setListener(this);
            cli.exec(cmd);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        checkErrors(cmd);
    }

    private InputStream execBinary(SvnCommand cmd) throws SVNClientException {
        InputStream ret;
        try {
            cmd.setListener(this);
            ret = cli.execBinary(cmd);
        } catch (IOException ex) {
            throw new SVNClientException(ex);
        }
        checkErrors(cmd);
        return ret;
    }
    
    private void checkErrors(SvnCommand cmd) throws SVNClientException {

        List<String> errors = cmd.getCmdError();
        if (errors.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < errors.size(); i++) {
                sb.append(errors.get(i));
                if (i < errors.size() - 1) {
                    sb.append('\n');
                }
            }
            throw new SVNClientException(sb.toString());
        }
    }

    private List<SVNUrl> getAllNotExistingParents(SVNUrl url) throws SVNClientException {        
        List<SVNUrl> ret = new ArrayList<SVNUrl>();
        if(url == null) {
            return ret;
        }
        try {
            getInfo(url);            
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("Not a valid URL") > -1) { // XXX are we shure this is it?
                ret.addAll(getAllNotExistingParents(url.getParent()));
                ret.add(url);                        
            } else {
                throw e;
            }                    
        }        
        return ret;
    }
    
    // unsupported start
    
    public SVNNotificationHandler getNotificationHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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

    private void notifyChangedStatus(File file, boolean rec, SvnCommand cmd) throws SVNClientException {
        ISVNStatus[] status = getStatus(file, rec, false);
        for (ISVNStatus s : status) {
            onNotify(cmd.getAbsoluteFile(s.getFile().getAbsolutePath()), null);
        }
    }
    
    // unsupported start
    
}
