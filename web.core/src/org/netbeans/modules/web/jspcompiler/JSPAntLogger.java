/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspcompiler;

import java.io.File;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Ant logger which handles compilation of JSPs, both the JSP -> Java and 
 * the Java -> class compilation stages.
 * Specifically, handles hyperlinking of errors from JspC and from Javac run on 
 * classes generated from JSPs.
 * @author Petr Jiricka, Jesse Glick
 * @see "#42525"
 */
public final class JSPAntLogger extends AntLogger {
    
/*    private static PrintWriter debugwriter = null;
    private static void debug(String s) {
        if (debugwriter == null) {
            try {
                debugwriter = new PrintWriter(new java.io.FileWriter("c:\\temp\\AntOutputParser.log")); // NOI18N
            } catch (java.io.IOException ioe) {
                return;
            }
        }
        debugwriter.println(s);
        debugwriter.flush();
    }*/
    
    /**
     * Regexp matching the compilation error from JspC. Sample message could look like this:
     * org.apache.jasper.JasperException: file:C:/project/AntParseTestProject2/build/web/index.jsp(6,0) Include action: Mandatory attribute page missing
     */
    private static final Pattern JSP_COMPILER_ERROR = Pattern.compile(
        "(.*)(org.apache.jasper.JasperException: file:)([^\\(]*)\\(([0-9]+),([0-9]+)\\)(.*)"); // NOI18N


    private static final String[] TASKS_OF_INTEREST = AntLogger.ALL_TASKS;
    
    private static final int[] LEVELS_OF_INTEREST = {
        //AntEvent.LOG_DEBUG, // XXX is this needed?
        //AntEvent.LOG_VERBOSE, // XXX is this needed?
        AntEvent.LOG_INFO, // XXX is this needed?
        AntEvent.LOG_WARN, // XXX is this needed?
        AntEvent.LOG_ERR, // XXX is this needed?
    };
    
    
    /** Default constructor for lookup. */
    public JSPAntLogger() {
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

        // XXX only check when the task is correct
        Matcher m = JSP_COMPILER_ERROR.matcher(line);
        if (m.matches()) {
//debug ("message matches ->" + line);
            // We have a JSP error
            String jspFile  = m.group(3).trim();
            int lineNumber      = Integer.parseInt(m.group(4))/* - 1*/;
            int columnNumber    = Integer.parseInt(m.group(5));
            String jspErrorText = m.group(6).trim();
            File f = new File(jspFile);
            FileObject fo = FileUtil.toFileObject(f);
            // Check to see if this JSP is in the web module.
            FileObject jspSource = getResourceInSources(fo);
            if (jspSource != null) {
                hyperlink(line, session, event, jspSource, messageLevel, sessionLevel, jspErrorText, lineNumber, -1/*columnNumber*/);
            }
        }
    }
    
    
    /** Given a resource in the build directory, returns the corresponding resource in the source directory.
     */
    private static FileObject getResourceInSources(FileObject inBuild) {
        if (inBuild == null) {
            return null;
        }
        WebModule wm = WebModule.getWebModule(inBuild);
        if (wm != null) {
            // get the mirror of this page in sources
            FileObject webBuildDir = guessWebModuleOutputRoot(wm, inBuild);
            if (webBuildDir != null) {
                String jspResourcePath = FileUtil.getRelativePath(webBuildDir, inBuild);
                return wm.getDocumentBase().getFileObject(jspResourcePath);
            }

        }
        return null;
    }
    
    private static FileObject guessWebModuleOutputRoot(WebModule wm, FileObject fo) {
        /*
        File outputF = wm.getWebOutputRoot();
        if (outputF != null) {
            return FileUtil.toFileObject(outputF);
        }
        */
        FileObject potentialRoot = fo.getParent();
        while (potentialRoot != null) {
            if (potentialRoot.getFileObject("WEB-INF") != null) {
                return potentialRoot;
            }
            potentialRoot = potentialRoot.getParent();
        }
        return null;
    }
    
    private static void hyperlink(String line, AntSession session, AntEvent event, FileObject source, int messageLevel, int sessionLevel, String text, int lineNumber, int columnNumber) {
        if (messageLevel <= sessionLevel && !event.isConsumed()) {
            event.consume();
            try {
                session.println(line, true, session.createStandardHyperlink(source.getURL(), text, lineNumber, columnNumber, -1, -1));
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
        }
    }
    
}
