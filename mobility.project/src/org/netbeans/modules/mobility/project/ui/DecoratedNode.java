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
package org.netbeans.modules.mobility.project.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.io.CharConversionException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

/**
 * Lightening up of ActionNode
 *
 * @author Tim Boudreau
 */
class DecoratedNode extends AbstractNode {
    static final String GRAY = "gray";
    static final String BOLD = "bold";
    static final String ERROR = "error";
    private final DisplayNameUpdater displayNameUpdater = new DisplayNameUpdater();
    private static final int INTERVAL = 200;
    private final BrokenStateUpdater brokenChecker = new BrokenStateUpdater();
    private final RequestProcessor.Task task = RequestProcessor.getDefault().create(brokenChecker);

    DecoratedNode(Children ch, Lookup lkp) {
        super(ch, lkp);
        task.schedule (400);
    }

    public DecoratedNode(Children ch, final Lookup lookup, String name, String dName, String icon) {
        this(ch, lookup);
        setName(name);
        if (dName != null) {
            setDisplayName(dName);
        }
        if (icon != null) {
            setIconBaseWithExtension(icon);
        }
    }

    protected boolean isAlive() {
        Node parent = this;
        while (parent != null) {
            parent = parent.getParentNode();
            if (parent != null && parent instanceof J2MEProjectRootNode) {
                if (parent.getParentNode() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final void setName(final String name) {
        if (name.equals(this.getName())) {
            fireDisplayNameChange(null, null);
        } else {
            super.setName(name);
        }
    }

    @Override
    public final void setValue(String attributeName, Object value) {
        super.setValue(attributeName, value);
        if (GRAY.equals(attributeName) || BOLD.equals(attributeName) || ERROR.equals (attributeName)) {
            //Do this asynchronously to fire on the event thread and avoid
            //Children.MUTEX vs. ProjectManager.mutex() problems - can happen
            //if the config is being deleted and the dead node notices
            //the property change
            EventQueue.invokeLater (displayNameUpdater);
        }
    }

    @Override
    public String getHtmlDisplayName() {
        String displayName = this.getDisplayName();
        try {
            displayName = XMLUtil.toElementContent(displayName);
        } catch (CharConversionException ex) {
            // OK, no annotation in this case
            return null;
        }
        StringBuilder sb = new StringBuilder();
        final boolean bold = Boolean.TRUE.equals(getValue(BOLD));
        if (bold) {
            sb.append ("<B>");
        }
        final boolean error = Boolean.TRUE.equals(getValue(ERROR));
        final boolean gray = Boolean.TRUE.equals(getValue(GRAY));
        if (error) {
            sb.append ("<font color=\"!nb.errorForeground\">");
        } else if (gray) {
            sb.append ("<font color=\"!controlShadow\">");
        }
        sb.append (displayName);
        return sb.toString();
    }

    @Override
    public Image getIcon(final int type) {
        final Image icon = super.getIcon(type);
        boolean broken = Boolean.TRUE.equals (getValue(ERROR));
        return broken ? ImageUtilities.mergeImages(icon,
                ImageUtilities.loadImage("org/netbeans/modules/mobility/project/ui/resources/brokenProjectBadge.gif"), 8, 0) : icon; //NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon (type);
    }

    private final class DisplayNameUpdater implements Runnable {
        public void run() {
            fireDisplayNameChange(null, null);
            fireIconChange();
            fireOpenedIconChange();
        }
    }

    protected final void checkBroken() {
        if (getParentNode() != null) {
            task.schedule(INTERVAL);
        }
    }
    
    protected boolean isBroken() {
        return false;
    }

    private final class BrokenStateUpdater implements Runnable {
        public void run() {
            setValue (ERROR, isBroken() ? true : false);
        }
    }
}
