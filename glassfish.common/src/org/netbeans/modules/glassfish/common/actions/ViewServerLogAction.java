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

package org.netbeans.modules.glassfish.common.actions;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.common.LogViewMgr;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.glassfish.spi.RecognizerCookie;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/** 
 * This action will open or focus the server log window for the selected server
 * instance.
 * 
 * @author Peter Williams
 */
public class ViewServerLogAction extends NodeAction {

    private static final String SHOW_SERVER_LOG_ICONBASE =
            "org/netbeans/modules/glassfish/common/resources/serverlog.gif"; // NOI18N

    @Override
    protected void performAction(Node[] nodes) {
        Lookup lookup = nodes[0].getLookup();
        GlassfishModule commonSupport = lookup.lookup(GlassfishModule.class);
        if(commonSupport != null) {
            Map<String, String> properties = commonSupport.getInstanceProperties();
            String uri = properties.get(GlassfishModule.URL_ATTR);
            LogViewMgr mgr = LogViewMgr.getInstance(uri);
            List<Recognizer> recognizers = getRecognizers(lookup.lookupAll(RecognizerCookie.class));
            mgr.ensureActiveReader(recognizers, getServerLog(properties));
            mgr.selectIO();
        }
    }
    
    private List<Recognizer> getRecognizers(Collection<? extends RecognizerCookie> cookies) {
        List<Recognizer> recognizers;
        if(!cookies.isEmpty()) {
            recognizers = new LinkedList<Recognizer>();
            for(RecognizerCookie cookie: cookies) {
                recognizers.addAll(cookie.getRecognizers());
            }
            recognizers = Collections.unmodifiableList(recognizers);
        } else {
            recognizers = Collections.emptyList();
        }
        return recognizers;
    }
    
    private File getServerLog(Map<String, String> ip) {
        File result = null;
        String domainsFolder = ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        String domainName = ip.get(GlassfishModule.DOMAIN_NAME_ATTR);
        File domainFolder = new File(domainsFolder, domainName);

        // domain folder must exist.
        if(domainFolder.exists()) {
            // however, logs folder or server.log does not have to exist yet.
            result = new File(domainFolder, "logs" + File.separatorChar + "server.log");
        } else {
            Logger.getLogger("glassfish").log(Level.WARNING, NbBundle.getMessage(
                    ViewServerLogAction.class, "MSG_DomainFolderNotFound", domainFolder.getAbsolutePath()));
        }
        return result;
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        if(nodes != null && nodes.length == 1 && nodes[0] != null) {
            GlassfishModule commonSupport = nodes[0].getLookup().lookup(GlassfishModule.class);
            if(commonSupport != null) {
                String uri = commonSupport.getInstanceProperties().get(GlassfishModule.URL_ATTR);
                return uri != null && uri.length() > 0 &&
                    null != commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR) ;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewServerLogAction.class, "CTL_ViewServerLogAction");
    }

    @Override
    protected String iconResource() {
        return SHOW_SERVER_LOG_ICONBASE;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
