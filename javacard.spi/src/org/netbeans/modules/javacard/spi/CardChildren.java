/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.spi;

import com.sun.javacard.filemodels.XListEntry;
import com.sun.javacard.filemodels.XListInstanceEntry;
import com.sun.javacard.filemodels.XListModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;

/**
 * Children of a Card node, which get their contents by calling
 * from the Card's CardContentsProvider capability, if present.
 *
 * @author Tim Boudreau
 */
final class CardChildren extends ChildFactory.Detachable<XListEntry> implements PropertyChangeListener {

    private final CardContentsProvider contents;

    public CardChildren(CardContentsProvider ob) {
        this.contents = ob;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
    }

    @Override
    protected Node createNodeForKey(XListEntry key) {
        return new EntryNode (key);
    }

    @Override
    protected boolean createKeys(List<XListEntry> toPopulate) {
        XListModel mdl = contents.getContents();
        if (mdl != null) {
            toPopulate.addAll(mdl.getData());
        }
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (JavacardDeviceKeyNames.DEVICE_CARDMANAGERURL.equals(evt.getPropertyName())) {
            refresh(false);
        }
    }

    private static final class EntryNode extends AbstractNode {
        EntryNode (XListEntry entry) {
            super (Children.create(new InstanceChildren(entry), false), Lookups.singleton(entry));
            setDisplayName(entry.getDisplayName());
            setName (entry.getDisplayName());
            setShortDescription(entry.getType());
            switch (ProjectKind.forManifestType(entry.getType())) {
                case CLASSIC_APPLET :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "spi/resources/capproject.png"); //NOI18N
                    break;
                case CLASSIC_LIBRARY :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "spi/resources/clslibproject.png"); //NOI18N
                    break;
                case EXTENDED_APPLET :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "spi/resources/eapproject.png"); //NOI18N
                    break;
                case EXTENSION_LIBRARY :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "spi/resources/extlibproject.png"); //NOI18N
                    break;
                case WEB :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "spi/resources/webproject.png"); //NOI18N
                    break;
                default :
                    break;
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.javacard.AboutJavaCard3Development"); //NOI18N
        }

        @Override
        public Action[] getActions (boolean ignored) {
            return new Action[]{};
        }
    }

    private static final class InstanceChildren extends ChildFactory<XListInstanceEntry> {
        private final XListEntry entry;
        InstanceChildren (XListEntry entry) {
            this.entry = entry;
        }

        @Override
        protected boolean createKeys(List<XListInstanceEntry> toPopulate) {
            toPopulate.addAll(entry.getInstances());
            return true;
        }

        @Override
        protected Node createNodeForKey(XListInstanceEntry key) {
            return new InstanceNode (entry, key);
        }
    }

    private static class InstanceNode extends AbstractNode {
        InstanceNode (XListEntry entry, XListInstanceEntry instance) {
            super (Children.LEAF, Lookups.fixed (entry, instance));
            setDisplayName (instance.getContent());
            setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "ri/platform/loader/instance.png"); //NOI18N
        }

        @Override
        public Action[] getActions (boolean ignored) {
            return new Action[]{};
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.javacard.AboutJavaCard3Development"); //NOI18N
        }
    }
}
