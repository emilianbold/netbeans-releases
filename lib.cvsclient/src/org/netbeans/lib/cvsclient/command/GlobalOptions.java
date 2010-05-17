/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command;

import java.io.File;
import java.util.*;

import org.netbeans.lib.cvsclient.request.*;

/**
 * Provides access to global options for a specific command.
 * These are options traditionally set in the command line CVS tool before the
 * command name, for example in the command:
 * <pre>cvs -n update -dP</pre>
 * -n is a global options but -dP are options specific to the update command.
 *
 * <br>Note that you can have different global options for each command you
 * execute (just like command-line CVS).
 *
 * @author  Robert Greig
 */
public class GlobalOptions implements Cloneable {

    private List variables;
    /**
     * Determines whether no changes should be done to the local files.
     * This is useful to request files, that would be updated.
     */
    private boolean doNoChanges;

    /**
     * Whether to make checked out files read only (read/write is the default).
     */
    private boolean checkedOutFilesReadOnly;

    /**
     * The CVS root to use.
     */
    private String cvsRoot;

    /**
     * Whether to use Gzip-compression.
     */
    private boolean useGzip = true;
    
    /**
     * The gzip compression level.
     */
    private int compressionLevel = 0;

    /**
     * Supresses logging of the command in CVSROOT/history in the repository.
     */
    private boolean noHistoryLogging;

    /**
     * The cvs gets more quiet than without this switch.
     * However it still prints the important stuff.
     * Note: If this switch is used, the Builder stuff and parsing of the output
     * might break.
     */
    private boolean moderatelyQuiet;

    /**
     * Is even more quiet.
     * Commands which primary function is to send info, still do print something
     * (diff, etc.), however other command are completely quiet.
     * Note: If this switch is used, the Builder stuff and parsing of the output
     * will break.
     */
    private boolean veryQuiet;

    /**
     * Traces the execution of the command. Useful for tracing down what is
     * causing problems.
     * Note: If this switch is used, the Builder stuff and parsing of the output
     * might break.
     */
    private boolean traceExecution;
    
    /**
     * Whether a help information should be displayed - usage of the command.
     */
    private boolean showHelp;

    /**
     * Whether a version information should be displayed.
     */
    private boolean showVersion;
    
    /**
     * Whether to use ~/.cvsrc file or ignore it.
     */
    private boolean ignoreCvsrc;
    
    /**
     * The directory that is used for temporary files.
     */
    private File tempDir;
    
    /**
     * The editor, that is used to edit the commit message.
     */
    private String editor;

    /**
     * Explicitly lists files/folders that must be left intact by the command.
     */ 
    private File[] exclusions;
    
    public GlobalOptions() {
        variables = new ArrayList();
    }
    
    /**
     * Sets list of non-modifiable files/folders. The client will effectively ignore all server responses
     * that concern these files/folders or files beneath them.  
     * 
     * @param exclusions array of non-modifiable files/folders
     */
    public void setExclusions(File[] exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * Returns list of non-modifiable files/folders. The client effectively ignores all server responses
     * that concern these files/folders or files beneath them.  
     *  
     * @return File [] list of files/folders that must be left intact by the command. 
     */ 
    public File[] getExclusions() {
        return exclusions;
    }

    /**
     * Tests whether the file is not modifiable as set by {@link #setExclusions(java.io.File[])}.
     * 
     * @param file file to test
     * @return true if the file must not be modified on disk, false otherwise
     */ 
    public boolean isExcluded(File file) {
        if (exclusions != null) {
            for (int i = 0; i < exclusions.length; i++) {
                if (isParentOrEqual(exclusions[i], file)) return true;
            }
        }
        return false;
    }
    
    /**
     * Tests parent/child relationship of files.
     * 
     * @param parent file to be parent of the second parameter
     * @param file file to be a child of the first parameter
     * @return true if the second parameter represents the same file as the first parameter OR is its descendant (child)
     */ 
    private static boolean isParentOrEqual(File parent, File file) {
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(parent)) return true;
        }
        return false;
    }    
    
    /**
     * Creates a list of requests.
     * Only those global options are included that can be sent to server (-q,
     * -Q, -l, -t, -r, -n).
     * To be added to the request list sent to the server.
     */
    public List createRequestList() {
        List requestList = new LinkedList();
        if (variables.size() > 0) {
            Iterator it = variables.iterator();
            while (it.hasNext()) {
                String keyValue = it.next().toString();
                requestList.add(new SetRequest(keyValue));
            }
        }
        if (isNoHistoryLogging()) {
            requestList.add(new GlobalOptionRequest("-l")); //NOI18N
        }
        if (isDoNoChanges()) {
            requestList.add(new GlobalOptionRequest("-n")); //NOI18N
        }
        if (isModeratelyQuiet()) {
            requestList.add(new GlobalOptionRequest("-q")); //NOI18N
        }
        if (isVeryQuiet()) {
            requestList.add(new GlobalOptionRequest("-Q")); //NOI18N
        }
        if (isTraceExecution()) {
            requestList.add(new GlobalOptionRequest("-t")); //NOI18N
        }
        return requestList;
    }

    /**
     * Returns a String that defines which options are available for global
     * options.
     */
    public String getOptString() {
        return "Hvnfd:lqQtrws:z:T:e:"; //NOI18N
    }

    /**
     * EQUALS to Command.setCVSCommand()
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'n') {
            setDoNoChanges(true);
        }
        else if (opt == 'd') {
            setCVSRoot(optArg);
        }
        else if (opt == 'l') {
            setNoHistoryLogging(true);
        }
        else if (opt == 'q') {
            setModeratelyQuiet(true);
        }
        else if (opt == 'Q') {
            setVeryQuiet(true);
        }
        else if (opt == 't') {
            setTraceExecution(true);
        }
        else if (opt == 't') {
            setTraceExecution(true);
        }
        else if (opt == 'r') {
            setCheckedOutFilesReadOnly(true);
        } 
        else if (opt == 'w') {
            setCheckedOutFilesReadOnly(false);
        } 
        else if (opt == 's') {
            setCvsVariable(optArg);
        }
        else if (opt == 'z') {
            try {
                setCompressionLevel(Integer.parseInt(optArg));
            } catch (NumberFormatException nfex) {
                
            }
        }
        else if (opt == 'H') {
            setShowHelp(true);
        }
        else if (opt == 'v') {
            setShowVersion(true);
        }
        else if (opt == 'f') {
            setIgnoreCvsrc(true);
        }
        else if (opt == 'T') {
            setTempDir(new File(optArg));
        }
        else if (opt == 'e') {
            setEditor(optArg);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * Resets all switches in the command to the default behaviour.
     * After calling this method, the command should behave defaultly.
     * EQUALS to Command.resetCVSCommand()
     */
    public void resetCVSCommand() {
        setCheckedOutFilesReadOnly(false);
        setDoNoChanges(false);
        setModeratelyQuiet(false);
        setNoHistoryLogging(false);
        setTraceExecution(false);
        setUseGzip(true);
        setCompressionLevel(0);
        setVeryQuiet(false);
        setShowHelp(false);
        setShowVersion(false);
        setIgnoreCvsrc(false);
        setTempDir(null);
        setEditor(null);
        setCVSRoot("");
        clearCvsVariables();
    }

    /**
     * Equals to the Command.getCVSCommand() functionality.
     * Returns all the current switches in the command-line cvs style.
     */
    public String getCVSCommand() {
        StringBuffer switches = new StringBuffer();
        if (isDoNoChanges()) {
            switches.append("-n "); //NOI18N
        }
        if (isNoHistoryLogging()) {
            switches.append("-l "); //NOI18N
        }
        if (isModeratelyQuiet()) {
            switches.append("-q "); //NOI18N
        }
        if (isVeryQuiet()) {
            switches.append("-Q "); //NOI18N
        }
        if (isTraceExecution()) {
            switches.append("-t "); //NOI18N
        }
        if (isCheckedOutFilesReadOnly()) {
            switches.append("-r "); //NOI18N
        }
        if (variables.size() > 0) {
            Iterator it = variables.iterator(); 
            while (it.hasNext()) {
                String keyValue = it.next().toString();
                switches.append("-s " + keyValue + " "); //NOI18N
            }
        }
        if (compressionLevel != 0) {
            switches.append("-z ");
            switches.append(Integer.toString(compressionLevel));
            switches.append(" ");
        }
        if (isIgnoreCvsrc()) {
            switches.append("-f ");
        }
        if (tempDir != null) {
            switches.append("-T ");
            switches.append(tempDir.getAbsolutePath());
            switches.append(" ");
        }
        if (editor != null) {
            switches.append("-e ");
            switches.append(editor);
            switches.append(" ");
        }
        return switches.toString();
    }

    /**
     * Adds one cvs internal enviroment variable.
     * @param variable The format is NAME=VALUE.
     */
    public void setCvsVariable(String variable) {
        variables.add(variable);
    }

    /**
     * Clears the list of cvs internal enviroment variables.
     */

    public void clearCvsVariables() {
        this.variables.clear();
    }

    /**
     * Sets the cvs internal enviroment variables. 
     * It will clear any vrisables previously set.
     * @param variables array of strings in format "KEY=VALUE".
     */
    public void setCvsVariables(String[] variables) {
        clearCvsVariables();
        for (int i = 0; i < variables.length; i++) {
            String variable = variables[i];
            this.variables.add(variable);
        }
    }

    public String[] getCvsVariables() {
        String[] vars = new String[variables.size()];
        vars = (String[])variables.toArray(vars);
        return vars;
    }
    
    
    /**
     * Sets whether no changes should be done to the files.
     */
    public void setDoNoChanges(boolean doNoChanges) {
        this.doNoChanges = doNoChanges;
    }

    /**
     * Returns whether no changes should be done to the files.
     */
    public boolean isDoNoChanges() {
        return doNoChanges;
    }

    /**
     * Are checked out files read only.
     * @return the answer
     */
    public boolean isCheckedOutFilesReadOnly() {
        return checkedOutFilesReadOnly;
    }

    /**
     * Set whether checked out files are read only. False is the default.
     * @param readOnly true for readonly, false for read/write (default)
     */
    public void setCheckedOutFilesReadOnly(boolean readOnly) {
        checkedOutFilesReadOnly = readOnly;
    }

    /**
     * Get the CVS root
     * @return the CVS root value, e.g. :pserver:user@host@/usr/local/cvs
     */
    public String getCVSRoot() {
        return cvsRoot;
    }

    /**
     * Set the CVS root
     * @param cvsRoot CVS root to use
     */
    public void setCVSRoot(String cvsRoot) {
        this.cvsRoot = cvsRoot;
    }

    /**
     * Set whether to use Gzip for file transmission/reception
     * @param useGzip true if gzip should be used, false otherwise
     */
    public void setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
    }

    /**
     * Get whether to use Gzip
     * @return true if Gzip should be used, false otherwise
     */
    public boolean isUseGzip() {
        return useGzip;
    }

    /**
     * Getter for property compressionLevel.
     * @return Value of property compressionLevel.
     */
    public int getCompressionLevel() {
        return compressionLevel;
    }
    
    /**
     * Setter for property compressionLevel.
     * @param compressionLevel New value of property compressionLevel.
     */
    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }
    
    /** Getter for property noHistoryLogging.
     * @return Value of property noHistoryLogging.
     */
    public boolean isNoHistoryLogging() {
        return noHistoryLogging;
    }

    /** Setter for property noHistoryLogging.
     * @param noHistoryLogging New value of property noHistoryLogging.
     */
    public void setNoHistoryLogging(boolean noHistoryLogging) {
        this.noHistoryLogging = noHistoryLogging;
    }

    /** Getter for property moderatelyQuiet.
     * @return Value of property moderatelyQuiet.
     */
    public boolean isModeratelyQuiet() {
        return moderatelyQuiet;
    }

    /** Setter for property moderatelyQuiet.
     * @param moderatelyQuiet New value of property moderatelyQuiet.
     */
    public void setModeratelyQuiet(boolean moderatelyQuiet) {
        this.moderatelyQuiet = moderatelyQuiet;
    }

    /** Getter for property veryQuiet.
     * @return Value of property veryQuiet.
     */
    public boolean isVeryQuiet() {
        return veryQuiet;
    }

    /** Setter for property veryQuiet.
     * @param veryQuiet New value of property veryQuiet.
     */
    public void setVeryQuiet(boolean veryQuiet) {
        this.veryQuiet = veryQuiet;
    }

    /** Getter for property traceExecution.
     * @return Value of property traceExecution.
     */
    public boolean isTraceExecution() {
        return traceExecution;
    }

    /** Setter for property traceExecution.
     * @param traceExecution New value of property traceExecution.
     */
    public void setTraceExecution(boolean traceExecution) {
        this.traceExecution = traceExecution;
    }

    /**
     * Getter for property showHelp.
     * @return Value of property showHelp.
     */
    public boolean isShowHelp() {
        return showHelp;
    }
    
    /**
     * Setter for property showHelp.
     * @param showHelp New value of property showHelp.
     */
    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }
    
    /**
     * Getter for property showVersion.
     * @return Value of property showVersion.
     */
    public boolean isShowVersion() {
        return showVersion;
    }
    
    /**
     * Setter for property showVersion.
     * @param showVersion New value of property showVersion.
     */
    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }
    
    /**
     * Getter for property ignoreCvsrc.
     * @return Value of property ignoreCvsrc.
     */
    public boolean isIgnoreCvsrc() {
        return ignoreCvsrc;
    }
    
    /**
     * Setter for property ignoreCvsrc.
     * @param ignoreCvsrc New value of property ignoreCvsrc.
     */
    public void setIgnoreCvsrc(boolean ignoreCvsrc) {
        this.ignoreCvsrc = ignoreCvsrc;
    }
    
    /**
     * Getter for property tempDir.
     * @return Value of property tempDir.
     */
    public java.io.File getTempDir() {
        return tempDir;
    }
    
    /**
     * Setter for property tempDir.
     * @param tempDir New value of property tempDir.
     */
    public void setTempDir(java.io.File tempDir) {
        this.tempDir = tempDir;
    }
    
    /**
     * Getter for property editor.
     * @return Value of property editor.
     */
    public String getEditor() {
        return editor;
    }
    
    /**
     * Setter for property editor.
     * @param editor New value of property editor.
     */
    public void setEditor(String editor) {
        this.editor = editor;
    }
    
    /**
     * This method just calls the Object.clone() and makes it public.
     */
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException ex) {
            // never can occur
            return null;
        }
    }
    
}
