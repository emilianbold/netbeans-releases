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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl.local.nodes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.rt.providers.impl.DefaultServerCustomizer;
import org.netbeans.modules.php.rt.providers.impl.ServerCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.local.LocalHostImpl;
import org.netbeans.modules.php.rt.providers.impl.local.LocalServerHttpCustomizerComponent;
import org.netbeans.modules.php.rt.providers.impl.nodes.AbstractWebInfoNode;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author avk
 */
public class WebInfoNode extends AbstractWebInfoNode {

    public WebInfoNode( LocalHostImpl host ) {
        super(new LocalWebInfoNodeChildren(), host);
    }

    @Override
    protected ServerCustomizerComponent getCustomizerComponent(
            DefaultServerCustomizer parentDialog) 
    {
        Host host = getLookup().lookup(Host.class);
        if (host != null && host instanceof LocalHostImpl) {
            LocalHostImpl impl = (LocalHostImpl) host;
            return new LocalServerHttpCustomizerComponent(impl, parentDialog);
        }
        return null;
    }

    private static class LocalWebInfoNodeChildren extends Children.Keys {

        @Override
        protected void addNotify() {
            updateKeys();
        }
        
        private void updateKeys(){
            Host host = getNode().getLookup().lookup(Host.class);
            if (host != null && host instanceof LocalHostImpl) {
                LocalHostImpl impl = (LocalHostImpl)host;
                setKeys(impl);
            }
        }
        
        private void setKeys(LocalHostImpl host){
            if (    LocalHostImpl.Helper.isPhpConfigSet(host)
                    || LocalHostImpl.Helper.isWebConfigSet(host))
            {
                setKeys(new Object[]{host});
            } else {
                setKeys(Collections.EMPTY_SET);
            }
        }
        

        @Override
        protected Node[] createNodes(Object key) {
            if (key instanceof LocalHostImpl) {
                LocalHostImpl host = (LocalHostImpl)key;
                List<Node> nodesList = new LinkedList<Node>();
                
                if (LocalHostImpl.Helper.isPhpConfigSet(host)){
                    nodesList.add(new PhpConfigNode(host));
                }
                if (LocalHostImpl.Helper.isWebConfigSet(host)){
                    nodesList.add(new WebConfigNode(host));
                }
                return nodesList.toArray(new Node[]{});
            } else {
                return null;
            }
        }
        
    }
}
