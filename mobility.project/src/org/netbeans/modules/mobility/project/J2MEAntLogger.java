/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.mobility.project;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Logger which redirects links from preprocessed files to original sources.
 * @author Adam Sotona
 */
@org.openide.util.lookup.ServiceProvider(service=org.apache.tools.ant.module.spi.AntLogger.class, position=26)
public final class J2MEAntLogger extends AntLogger {
    
    private static final String separator = File.separatorChar == '\\' ? "\\\\" : "/"; //NOI18N
    
    private static final Pattern PREPROCESSED = Pattern.compile(
            "^.*" + separator + "build(?:" + separator + "[a-zA-Z_$][a-zA-Z0-9_$]*)?" + separator + "preprocessed" + separator); // NOI18N
    
    //this funny stuff is just to escape backslashes and dollar characters in a replacement path string using String.replaceAll(regexp, regexp)
    private static final String CHARSTOESCAPE = "([\\\\\\$])"; //NOI18N
    private static final String ESCAPESEQUENCE = "\\\\$1"; //NOI18N
    
    
    /** Default constructor for lookup. 
    public J2MEAntLogger() {
    }
    */
    public boolean interestedInSession(AntSession session) {
        // disable this feature when verbosity set to DEBUG
        return session.getVerbosity() < AntEvent.LOG_DEBUG;
    }
    
    public boolean interestedInScript(File script, AntSession session) {
        FileObject projfile = FileUtil.toFileObject(FileUtil.normalizeFile(script));
        if (projfile == null) return false;
        Project proj = FileOwnerQuery.getOwner(projfile);
        if (proj == null) return false;
        AntProjectHelper helper = proj.getLookup().lookup(AntProjectHelper.class);
        if (helper == null) return false;
        String sourceRoot = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
        if (sourceRoot == null) return false;
        File srcRoot = helper.resolveFile(sourceRoot);
        if (srcRoot == null) return false;
        HashMap<File, String> roots = (HashMap)session.getCustomData(this);
        if (roots == null) {
            roots = new HashMap();
            session.putCustomData(this, roots);
        }
        roots.put(script, srcRoot.getAbsolutePath().replaceAll(CHARSTOESCAPE, ESCAPESEQUENCE) + separator);
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        // may be restricted to "compile" target only
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        // may be restricted to "javac" task only
        return AntLogger.ALL_TASKS;
    }
    
    public void messageLogged(AntEvent event) {
        if (event.isConsumed()) return;
        Object cd=event.getSession().getCustomData(this);        
        String srcRoot;
        if (cd instanceof HashMap)
        {
            HashMap<File, String> roots = (HashMap)cd;
            if (roots == null) return;
            srcRoot = roots.get(event.getScriptLocation());
        }
        else 
            srcRoot = (String)cd;
        if (srcRoot == null) return;
        String message = event.getMessage();
        String newMessage = PREPROCESSED.matcher(message).replaceFirst(srcRoot);
        if (!message.equals(newMessage)) {
            event.consume();
            event.getSession().deliverMessageLogged(event, newMessage, event.getLogLevel());
        }
    }
    
    public int[] interestedInLogLevels(AntSession session) {
        return new int[]{AntEvent.LOG_VERBOSE, AntEvent.LOG_INFO, AntEvent.LOG_WARN, AntEvent.LOG_ERR};
    }
    
}
