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
package org.netbeans.modules.php.rt.providers.impl.local;

import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.CommandProvider;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.UiConfigProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 *
 */
public class DefaultLocalServerProvider extends AbstractProvider<LocalHostImpl> {

    private static final String DESCRIPTION = "TXT_LocalServerDescription"; // NOI18N    

    private static final String TYPE_NAME = "TXT_LocalServerTypeName";    // NOI18N    

    private static final String DOCUMENT_ROOT = "documentRoot";               // NOI18N     

    private static final String WEB_SERVER_CONFIG_FILE = "webServerConfig";   // NOI18N     

    private static final String PHP_CONFIG_FILE = "phpConfig";                // NOI18N     


    public DefaultLocalServerProvider(String domain, String baseDir, String port, String docRoot, String indexFile) {
        myCommandProvider = new LocalCommandProvider(this) {

	    @Override
	    public Command[] getAllSupportedCommands(Project project) {
		ArrayList<Command> commands = new ArrayList<Command>();
		commands.addAll(Arrays.asList(getProjectCommands(project)));
		commands.addAll(Arrays.asList(getObjectCommands(project)));
		return commands.toArray(new Command[0]);
	    }	    
	    
            @Override
            public Command[] getAdditionalCommands(Project project) {
                if (isInvokedForProject() || isInvokedForSrcRoot()) {
                    return getAdditionalProjectCommands(project);
                } else {
                    return getAdditionalObjectCommands(project);
                }
            }

            private Command[] getAdditionalProjectCommands(Project project) {
                return new Command[]{
                            new UploadFilesCommandImpl(project, DefaultLocalServerProvider.this),
                            new DownloadFilesCommandImpl(project, DefaultLocalServerProvider.this),
                        };
            }

            private Command[] getAdditionalObjectCommands(Project project) {
                return new Command[]{
                            new UploadFilesCommandImpl(project, DefaultLocalServerProvider.this),
                            new DownloadFilesCommandImpl(project, DefaultLocalServerProvider.this),
                        };
            }
        };
        LocalHostImpl impl = new LocalHostImpl("defaultHost", domain, port, baseDir, this);//NOI18N

        impl.setProperty(LocalHostImpl.DOCUMENT_PATH, docRoot);
        impl.setProperty(LocalHostImpl.WEB_CONFIG_FILE, "");
        impl.setProperty(LocalHostImpl.PHP_CONFIG_FILE, "");
        if (indexFile != null && indexFile.trim().length() > 0) {
            impl.setProperty(LocalHostImpl.INDEX_FILE, indexFile);
        }
        doGetHosts().add(impl);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getConfigProvider()
     */
    public UiConfigProvider getConfigProvider() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getTypeName()
     */
    public String getTypeName() {
        return NbBundle.getMessage(DefaultLocalServerProvider.class, TYPE_NAME);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getCommandProvider()
     */
    public CommandProvider getCommandProvider() {
        return myCommandProvider;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getDescription()
     */
    public String getDescription() {
        return NbBundle.getMessage(DefaultLocalServerProvider.class, DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#createNode(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    public Node createNode(Host host) {
        throw new UnsupportedOperationException();
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.WebServerProvider#getProjectConfigProvider()
     */
    public ProjectConfigProvider getProjectConfigProvider() {
        throw new UnsupportedOperationException();
    }

    protected LocalHostImpl configureHost(FileObject object) {
        return null;
    }

    @Override
    protected boolean acceptHost(FileObject fileObject, Host host) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.providers.impl.AbstractProvider#serializeAdded(org.netbeans.modules.php.rt.spi.providers.Host)
     */
    @Override
    protected void serializeAdded(LocalHostImpl host) {
    }

    @Override
    protected void serializeUpdated(LocalHostImpl oldHost, LocalHostImpl newHost) {
    }
    private final LocalCommandProvider myCommandProvider;
}
