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

package org.netbeans.modules.web.jspcompiler;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Ant logger which handles compilation of JSPs
 * @author Martin Grebac
 */
public final class TldAntLogger extends AntLogger {
    
    /**
     * Regexp matching the compilation error from JspC when error is in tld. Sample message could look like this:
     * org.apache.jasper.JasperException: Unable to initialize TldLocationsCache: XML parsing error on file /WEB-INF/jsp2/jsp2-example-taglib.tld: (line 18, col -1)
     */
    private static final Pattern TLD_ERROR = Pattern.compile(
        "(.*)(org.apache.jasper.JasperException:)(.*)( file )(.*)"); // NOI18N

    private static final Pattern FILE_PATTERN = Pattern.compile(
        "([^\\(]*)(: )\\(line ([0-9]+), col ([0-9-]+)\\)"); // NOI18N

    private static final String[] TASKS_OF_INTEREST = AntLogger.ALL_TASKS;
    
    private static final int[] LEVELS_OF_INTEREST = {
        //AntEvent.LOG_DEBUG, // XXX is this needed?
        //AntEvent.LOG_VERBOSE, // XXX is this needed?
        AntEvent.LOG_INFO, // XXX is this needed?
        AntEvent.LOG_WARN, // XXX is this needed?
        AntEvent.LOG_ERR, // XXX is this needed?
    };
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(TldAntLogger.class.getName());
    private static final boolean LOGGABLE = ERR.isLoggable(ErrorManager.INFORMATIONAL);
    
    /** Default constructor for lookup. */
    public TldAntLogger() {
    }
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public boolean interestedInScript(File script, AntSession session) {
        return true;
    }
    
    public String[] interestedInTasks(AntSession session) {
        return TASKS_OF_INTEREST;
    }
    
    public int[] interestedInLogLevels(AntSession session) {
        // XXX could exclude those in [INFO..ERR] greater than session.verbosity
        return LEVELS_OF_INTEREST;
    }

    public void messageLogged(AntEvent event) {
        AntSession session = event.getSession();
        int messageLevel = event.getLogLevel();
        int sessionLevel = session.getVerbosity();
        String line = event.getMessage();
        assert line != null;

        Matcher m = TLD_ERROR.matcher(line);
        if (m.matches()) { //it's our error
            if (LOGGABLE) ERR.log("matched line: " + line);
            // print the exception and error statement first
            String errorText = m.group(3) + m.group(4);
            session.println(m.group(2) + errorText, true, null);
            
            // get the file from the line
            String filePart = m.group(5).trim();
            if (LOGGABLE) ERR.log("file part: " + filePart);
            
            // now create hyperlink for the file
            Matcher fileMatcher = FILE_PATTERN.matcher(filePart);
            if (fileMatcher.matches()) {
                String tldFile = fileMatcher.group(1).trim();
                if (LOGGABLE) ERR.log("tld file: " + tldFile);

                int lineNumber = Integer.parseInt(fileMatcher.group(3));
                int columnNumber = Integer.parseInt(fileMatcher.group(4));
                if (LOGGABLE) ERR.log("linking line: " + lineNumber + ", column: " + columnNumber);

                File scriptLoc = event.getScriptLocation();
                FileObject scriptLocFO = FileUtil.toFileObject(scriptLoc);
                WebModule wm = WebModule.getWebModule(scriptLocFO);
                if (LOGGABLE) ERR.log("wm: " + wm);
                
                if (wm == null) {
                    session.println(tldFile, true, null);
                    event.consume();
                    return;
                }
                
                FileObject tldSource = wm.getDocumentBase().getFileObject(tldFile);
                if (LOGGABLE) ERR.log("tldSource: " + tldSource);
                
                if (tldSource == null) {
                    session.println(tldFile, true, null);
                    event.consume();
                    return;
                }
                
                if (messageLevel <= sessionLevel && !event.isConsumed()) {
                    try {
                        session.println(tldFile, true, session.createStandardHyperlink(tldSource.getURL(), errorText + tldFile, lineNumber, columnNumber, -1, -1));
                    } catch (FileStateInvalidException e) {
                        assert false : e;
                    }
                }
            }
            event.consume();
        }
    }   
}
