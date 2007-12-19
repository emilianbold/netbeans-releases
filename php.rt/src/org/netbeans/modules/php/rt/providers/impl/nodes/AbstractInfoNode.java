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

package org.netbeans.modules.php.rt.providers.impl.nodes;

import java.awt.Component;
import org.netbeans.modules.php.rt.providers.impl.DefaultServerCustomizer;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.ServerCustomizerComponent;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public abstract class AbstractInfoNode extends AbstractNode {

    static final String ELLIPSIS = " ... "; // NOI18N
    static final int SHOW_FIRST_CHARS_IN_NAME = 20;
    static final int SHOW_LAST_CHARS_IN_NAME = 10;
    static final int ALLOWED_CHARS_IN_NAME 
            = SHOW_FIRST_CHARS_IN_NAME + ELLIPSIS.length() + SHOW_LAST_CHARS_IN_NAME;
    
    static final String SERVER_CUSTOMIZER_DIALOG_TITLE 
            = "LBL_Server_Customizer_Dialog_Title"; // NOI18N

    public AbstractInfoNode(Children children, Lookup lookup) {
        super(children, lookup);
    }

    protected String truncateName(String name){
        if (name.length() <= ALLOWED_CHARS_IN_NAME){
            return name;
        }
        return name.substring(0, SHOW_FIRST_CHARS_IN_NAME+1)
                + ELLIPSIS
                + name.substring(name.length() - SHOW_LAST_CHARS_IN_NAME);
    }
    
    /**
     * If you do not override org.netbeans.modules.php.rt.providers.impl.AbstractServerNode#getCustomizer()
     * This Should create ServerCustomizerComponent panel with server specific fields.
     * It will be Embeded into DefaultServerCustomizer dialog created in 
     * org.netbeans.modules.php.rt.providers.impl.AbstractServerNode#getCustomizer().
     */
    protected ServerCustomizerComponent getCustomizerComponent(
            DefaultServerCustomizer parentDialog)
    {
        return null;
    }

    /**
     * Creates DefaultServerCustomizer dialog window with embedded pannel
     * retrieved from getCustomizerComponent(ServerCustomizer parentDialog).
     * Should be overriden to create own component.
     * 
     * @see getCustomizerComponent(DefaultServerCustomizer);
     */
    @Override
    public Component getCustomizer() {
        Host host = getLookup().lookup(Host.class);
        if (host != null) {
            if (host instanceof HostImpl) {
                HostImpl impl = (HostImpl) host;
                String title = NbBundle.getMessage( AbstractInfoNode.class, 
                        SERVER_CUSTOMIZER_DIALOG_TITLE, impl.getDisplayName() );

                DefaultServerCustomizer customizer = new DefaultServerCustomizer(impl);
                ServerCustomizerComponent specificCustomizerPanel 
                        = getCustomizerComponent(customizer);
                if (specificCustomizerPanel != null) {
                    return customizer.createCustomizerDialog(specificCustomizerPanel, title);
                }
            }
        }
        return null;
    }

    /**
     * override this to return <code>true</code> value if you want to support customizer
     */
    @Override
    public boolean hasCustomizer() {
        return false;
    }

}
