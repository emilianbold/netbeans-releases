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
package org.netbeans.modules.ruby.railsprojects.server;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Task;

/**
 * A helper class for checking what server, if any, has been explicitly set
 * in the <tt>script/server</tt> file.
 *
 * @author Erno Mononen
 */
final class ServerResolver {

    private static final Logger LOGGER = Logger.getLogger(ServerResolver.class.getName());
    /**
     * The pattern for capturing value for <code>ARGV[0]</code>.
     */
    private static final Pattern SERVER_ARGUMENT = 
            Pattern.compile("[^#+\\s*]\\s*[^\\S+]ARGV.*\\[0\\]\\s*=\\s*\"(\\S+)\\s*\""); // NOI18N


    /**
     * Gets the server that is explicitly specified in <code>script/server</code>. Returns
     * <code>null</code> if no server was explicitly set, or if the set server was not 
     * of known type or not available on the platform of the given project.
     * 
     * @param project the project whose <code>script/server</code> to check.
     * @return the explicitly set server or <code>null</code>.
     */
    public static RubyInstance getExplicitlySpecifiedServer(RailsProject project) {

        FileObject serverScript = project.getProjectDirectory().getFileObject("script/server"); // NOI18N

        if (serverScript == null) {
            return null;
        }

        if (serverScript != null) {
            try {
                DataObject dobj = DataObject.find(serverScript);
                EditorCookie editor = dobj.getCookie(EditorCookie.class);
                if (editor == null) {
                    return null;
                }
                try {
                    editor.prepareDocument().waitFinished(1500);
                } catch (InterruptedException ie) {
                    // do nothing
                }
                Document doc = editor.getDocument();
                if (doc == null) {
                    return null;
                }
                String text = doc.getText(0, doc.getLength());
                String serverId = getSpecifiedServer(text);
                if (serverId != null) {
                    RubyPlatform platform = RubyPlatform.platformFor(project);                             
                    RubyInstance result = ServerRegistry.getDefault().getServer(serverId.toUpperCase(), platform);
                    if (result == null) {
                        LOGGER.info("Found explicitly set server [" + serverId + "] in server/script, " + 
                                "but the server was not found on project's platform [ " + platform + "] or recognized " +
                                "as a known server"); //NOI18N
                        return null;
                    }
                    return result;
                }

            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (DataObjectNotFoundException dnfe) {
                Exceptions.printStackTrace(dnfe);
            }

        }
        return null;
    }

    // used in tests
    static String getSpecifiedServer(String text) {

        Matcher matcher = SERVER_ARGUMENT.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        
        return matcher.group(1);
    }
}