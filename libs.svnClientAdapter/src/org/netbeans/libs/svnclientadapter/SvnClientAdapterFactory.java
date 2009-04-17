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

package org.netbeans.libs.svnclientadapter;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;

/**
 *
 * @author Tomas Stupka
 */
public class SvnClientAdapterFactory {
    
    private Logger LOG = Logger.getLogger("org.netbeans.libs.svnclientadapter");
    private static SvnClientAdapterFactory instance;
    private Client client;

    private SvnClientAdapterFactory() { }

    public static SvnClientAdapterFactory getInstance() {
        if(instance == null) {
            instance = new SvnClientAdapterFactory();
        }
        return instance;
    }

    private static boolean isSupportedJavahlVersion(String version) {
        boolean retval = false;
        if (version != null) {
            version = version.toLowerCase();
            if (version.startsWith("1.6") ||
                    version.contains("version 1.6")) {
                retval = true;
            }
        }
        return retval;
    }

    public enum Client {
        javahl,
        svnkit
    }

    public boolean setup(Client c) throws SVNClientException {
        client = c;
        switch(c) {
            case javahl: {
                try {
                    JhlClientAdapterFactory.setup();
                } catch (Throwable t) {
                    String jhlErorrs = JhlClientAdapterFactory.getLibraryLoadErrors();
                    LOG.log(Level.INFO, t.getMessage());
                    LOG.warning(jhlErorrs + "\n");                    
                    return false;
                }
                return JhlClientAdapterFactory.isAvailable();
            }
            case svnkit: {
                SvnKitClientAdapterFactory.setup();
                return SvnKitClientAdapterFactory.isAvailable();
            }
        }
        return false;
    }

    public ISVNClientAdapter createClient() {
        switch(client) {
            case javahl: {
                return JhlClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
            }
            case svnkit: {
                return SvnKitClientAdapterFactory.createSVNClient(SvnKitClientAdapterFactory.SVNKIT_CLIENT); //provider.createClient();
            }
        }
        return null;
    }

    /**
     * Checks if accessible javahl libraries' version is supported.
     * Currently supported:
     * <ul>
     * <li>1.3</li>
     * <li>1.4</li>
     * <li>1.5</li>
     * </ul>
     * @return true if javahl is of a supported version, otherwise false
     */
    public boolean isSupportedJavahlVersion() {
        boolean retval = false;
        if (Client.javahl.equals(client)) {
            ISVNClientAdapter adapter = JhlClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);
            if (adapter != null && adapter instanceof JhlClientAdapter) {
                JhlClientAdapter jhlAdapter = (JhlClientAdapter) adapter;
                String version = jhlAdapter.getNativeLibraryVersionString();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "isSupportedJavahlVersion: version {0}", version);
                }
                retval = isSupportedJavahlVersion(version);
            }
        }
        return retval;
    }
}
