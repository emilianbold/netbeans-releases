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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wag.manager.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.wag.manager.actions.ViewApiDocAction;
import org.netbeans.modules.wag.manager.model.WagService;
import org.netbeans.modules.wag.manager.util.WagTransferable;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author peterliu
 */
public class WagServiceNode extends AbstractNode {

    private WagService service;
    private Transferable transferable;

    public WagServiceNode(WagService service) {
        this(service, new InstanceContent());
    }

    WagServiceNode(WagService service, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        content.add(service);
        this.service = service;
        transferable = ExTransferable.create(
                new WagTransferable<WagService>(service, WagTransferable.WAG_SERVICE_FLAVORS));
    }

    @Override
    public String getName() {
        return "wagServiceNode";
    }

    @Override
    public String getDisplayName() {
        return service.getName();
    }

    @Override
    public String getShortDescription() {
        return service.getDescription();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(ViewApiDocAction.class)
        };

    }
    static final java.awt.Image ICON =
            ImageUtilities.loadImage("org/netbeans/modules/wag/manager/resources/restservice.png"); //NOI18N

    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        return WagTransferable.addFlavors(transferable);
    }
}
