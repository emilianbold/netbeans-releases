/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * ClientMultiViewElement.java
 *
 * Created on July 22, 2005, 11:21 AM
 *
 */
package org.netbeans.modules.mobility.jsr172.multiview;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 *
 * @author Michal Skvor, Anton Chechel
 */
public class Jsr172ClientMultiViewElement extends ToolBarMultiViewElement {
    private static final String CLIENT_NODE_ICON_BASE = "org/netbeans/modules/mobility/end2end/resources/e2eclienticon.png"; // NOI18N

    protected Jsr172ClientViewFactory factory;

    private SectionView view;
    private ToolBarDesignEditor comp;
    private int index;
    private boolean needInit = true;

    /** Creates a new instance of ClientMultiViewElement */
    public Jsr172ClientMultiViewElement(E2EDataObject dataObject, int index) {
        super(dataObject);
        this.index = index;

        comp = new ToolBarDesignEditor();
        factory = new Jsr172ClientViewFactory(comp, dataObject);
        setVisualEditor(comp);
    }

    @Messages("CTL_ClientTabCaption=Client") // NOI18N
    @MultiViewElement.Registration(mimeType = E2EDataObject.MIME_TYPE_JSR172,
        iconBase = E2EDataObject.ICON_BASE,
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "jsr172.client", // NOI18N
        displayName = "#CTL_ClientTabCaption", // NOI18N
        position = 100
    )
    public static MultiViewElement createJsr172ClientViewElement(Lookup lookup) {
        return new Jsr172ClientMultiViewElement(lookup.lookup(E2EDataObject.class), 0);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        dObj.setLastOpenView(index);
        if (needInit /* || !dObj.isDocumentParseable() */) {
            repaintView();
            needInit = false;
        }
    }

    protected void repaintView() {
        view = new ClientView();
        comp.setContentView(view);
        final Object lastActive = comp.getLastActive();
        if (lastActive != null) {
            view.openPanel(lastActive);
        } else {
            view.openPanel(Jsr172ClientViewFactory.PROP_PANEL_CLIENT_OPTIONS);
            view.openPanel(Jsr172ClientViewFactory.PROP_PANEL_CLIENT_GENERAL);
            view.openPanel(Jsr172ClientViewFactory.PROP_PANEL_SERVICES);
        }
        view.checkValidity();
    }

    @Override
    public SectionView getSectionView() {
        return view;
    }

    private class ClientView extends SectionView {

        final private Node servicesNode, clientGeneralInfoNode, clientOptionsNode;

        public ClientView() {
            super(factory);

            servicesNode = new ServicesNode();
            addSection(new SectionPanel(this, servicesNode, Jsr172ClientViewFactory.PROP_PANEL_SERVICES));

            clientGeneralInfoNode = new ClientGeneralInfoNode();
            addSection(new SectionPanel(this, clientGeneralInfoNode, Jsr172ClientViewFactory.PROP_PANEL_CLIENT_GENERAL));

            clientOptionsNode = new ClientOptionsNode();
            addSection(new SectionPanel(this, clientOptionsNode, Jsr172ClientViewFactory.PROP_PANEL_CLIENT_OPTIONS));

            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{servicesNode, clientGeneralInfoNode, clientOptionsNode});
            AbstractNode root = new AbstractNode(rootChildren);
            setRoot(root);
        }

        Node getServicesNode() {
            return servicesNode;
        }

        Node getClientGeneralInfoNode() {
            return clientGeneralInfoNode;
        }

        Node getClientOptionsNode() {
            return clientOptionsNode;
        }
    }

    /**
     * Represents section for Services view
     */
    private class ServicesNode extends AbstractNode {

        ServicesNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(Jsr172ClientMultiViewElement.class, "TTL_ServicesSection")); // NOI18N
            setIconBaseWithExtension(CLIENT_NODE_ICON_BASE);
            componentOpened();
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("me.wcb_clientsettingspanel"); // NOI18N
        }
    }

    /**
     * Represents section for General Information about client
     */
    private class ClientGeneralInfoNode extends AbstractNode {

        ClientGeneralInfoNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(Jsr172ClientMultiViewElement.class, "TTL_ClientGeneralInfoSection")); // NOI18N
            setIconBaseWithExtension(CLIENT_NODE_ICON_BASE);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("me.wcb_clientsettingspanel"); // NOI18N
        }
    }

    /**
     * Represents section for Client Options
     */
    private class ClientOptionsNode extends AbstractNode {

        ClientOptionsNode() {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(NbBundle.getMessage(Jsr172ClientMultiViewElement.class, "TTL_ClientOptionsSection")); // NOI18N
            setIconBaseWithExtension(CLIENT_NODE_ICON_BASE);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("me.wcb_clientsettingspanel"); // NOI18N
        }
    }
}
