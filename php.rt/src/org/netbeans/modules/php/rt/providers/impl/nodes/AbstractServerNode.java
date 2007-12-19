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

package org.netbeans.modules.php.rt.providers.impl.nodes;

import org.netbeans.modules.php.rt.providers.impl.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.modules.php.rt.actions.DeleteAction;
import org.netbeans.modules.php.rt.resources.ResourceMarker;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.actions.CustomizeAction;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author ads
 */
public abstract class AbstractServerNode extends AbstractInfoNode {

    public AbstractServerNode(String name, Host host) {
        this(Children.LEAF, name, host);
    }

    public AbstractServerNode(Children children, String name, Host host) {
        super(children, createLookup(host));
        setName(host.getId());
        setDisplayName(name);
        setIconBaseWithExtension(ResourceMarker.getLocation() + ResourceMarker.SERVER_ICON);
        setShortDescription(createNodeDescription(host));
    }

    private String createNodeDescription(Host host){
        String name = host.getProvider().getTypeName();
        if (host instanceof HostImpl){
            HostImpl impl = (HostImpl)host;
            if (HostImpl.Helper.isHttpReady(impl)){
                name  = HostImpl.Helper.getHttpUrl(impl) + " [ "+name+" ]";
            }
        }
        return name;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.openide.nodes.Node#getActions(boolean)
     */
    @Override
    public Action[] getActions(boolean context) {
        if (hasCustomizer()) {
            ArrayList<Action> actionsList = new ArrayList(Arrays.asList(ACTIONS));
            
            actionsList.add(null);
            actionsList.add(SystemAction.get(CustomizeAction.class));
            
            return actionsList.toArray(new Action[]{});
        } else {
            return ACTIONS;
        }
    }

    static Lookup createLookup(Host host) {
        if (host != null) {
            return Lookups.fixed(new Object[]{host});
        } else {
            return null;
        }
    }

    private static final Action[] ACTIONS = new Action[]{new DeleteAction()};
}
