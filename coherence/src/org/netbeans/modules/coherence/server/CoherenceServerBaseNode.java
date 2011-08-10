/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * This class represents Coherence server basic node. It handles base settings like
 * display name, icon, icons badges and it also appends to every node and {@link
 * CoherenceInstance} new instance of {@link CoherenceServer}.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CoherenceServerBaseNode extends AbstractNode implements ChangeListener {

    // Base server icon
    private static final String SERVER_ICON = "org/netbeans/modules/coherence/resources/icons/server.png"; //NOI18N
    // Icon status badges
    private static final String WAITING_BADGE_ICON = "org/netbeans/modules/coherence/resources/icons/server_waiting.png"; // NOI18N
    private static final String RUNNING_BADGE_ICON = "org/netbeans/modules/coherence/resources/icons/server_running.png"; // NOI18N
    
    protected final CoherenceInstance coherenceInstance;
    protected final CoherenceServer coherenceServer;

    public CoherenceServerBaseNode(CoherenceInstance coherenceInstance) {
        this(coherenceInstance, new InstanceContent());
    }

    private CoherenceServerBaseNode(CoherenceInstance coherenceInstance, InstanceContent instanceContent) {
        super(Children.LEAF, new AbstractLookup(instanceContent));
        this.coherenceInstance = coherenceInstance;
        this.coherenceServer = new CoherenceServer(coherenceInstance.getProperties());
        instanceContent.add(coherenceServer);

        coherenceServer.addChangeListener(WeakListeners.change(this, coherenceServer));

        // set display name, icon
        setIconBaseWithExtension(SERVER_ICON);
        setDisplayName(coherenceInstance.getDisplayName());
    }

    @Override
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{};
    }

    private Image badgeIcon(Image origImg) {
        Image badge = null;
        if (coherenceServer.isRunning()) {
            badge = ImageUtilities.loadImage(RUNNING_BADGE_ICON);
        }
        return badge != null ? ImageUtilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireIconChange();
    }
}
