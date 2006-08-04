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

package org.netbeans.modules.mobility.project;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Logger which redirects links from preprocessed files to original sources.
 * @author Adam Sotona
 */
public final class J2MEAntLogger extends AntLogger {
    
    private static final String separator = File.separatorChar == '\\' ? "\\\\" : "/"; //NOI18N
    
    private static final Pattern PREPROCESSED = Pattern.compile(
            ".*" + separator + "build(?:" + separator + "[a-zA-Z_$][a-zA-Z0-9_$]*)?" + separator + "preprocessed" + separator); // NOI18N
    
    //this funny stuff is just to escape backslashes and dollar characters in a replacement path string using String.replaceAll(regexp, regexp)
    private static final String CHARSTOESCAPE = "([\\\\\\$])"; //NOI18N
    private static final String ESCAPESEQUENCE = "\\\\$1"; //NOI18N
    
    
    /** Default constructor for lookup. 
    public J2MEAntLogger() {
    }
    */
    public boolean interestedInSession(final AntSession session) {
        // disable this feature when verbosity set to DEBUG
        return session.getVerbosity() < AntEvent.LOG_DEBUG;
    }
    
    private static File getSourceRoot(final File dir) {
        final FileObject projdir = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        try {
            final Project proj = ProjectManager.getDefault().findProject(projdir);
            if (proj == null) return null;
            final AntProjectHelper helper = proj.getLookup().lookup(AntProjectHelper.class);
            if (helper == null) return null;
            final String sourceRoot = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
            return sourceRoot == null ? null : helper.resolveFile(sourceRoot);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    public boolean interestedInScript(final File script, final AntSession session) {
        if (script.getName().equals("build-impl.xml")) { // NOI18N
            final File parent = script.getParentFile();
            if (parent != null && parent.getName().equals("nbproject")) { // NOI18N
                final File parent2 = parent.getParentFile();
                if (parent2 != null) {
                    final File srcRoot = getSourceRoot(parent2);
                    if (srcRoot == null) return false;
                    session.putCustomData(this, srcRoot.getAbsolutePath().replaceAll(CHARSTOESCAPE, ESCAPESEQUENCE)+separator);
                    return true;
                }
            }
        }
        return false;
    }
    
    public String[] interestedInTargets(@SuppressWarnings("unused")
	final AntSession session) {
        // may be restricted to "compile" target only
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(@SuppressWarnings("unused")
	final AntSession session) {
        // may be restricted to "javac" task only
        return AntLogger.ALL_TASKS;
    }
    
    public void messageLogged(final AntEvent event) {
        if (event.isConsumed()) return;
        final String message = event.getMessage();
        final String newMessage = PREPROCESSED.matcher(message).replaceFirst((String)event.getSession().getCustomData(this));
        if (!message.equals(newMessage)) {
            event.consume();
            event.getSession().deliverMessageLogged(event, newMessage, event.getLogLevel());
        }
    }
    
    public int[] interestedInLogLevels(@SuppressWarnings("unused")
	final AntSession session) {
        return new int[]{AntEvent.LOG_VERBOSE, AntEvent.LOG_INFO, AntEvent.LOG_WARN, AntEvent.LOG_ERR};
    }
    
}
