/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.card.loader;

import com.sun.javacard.filemodels.ParseErrorHandler;
import com.sun.javacard.filemodels.XListEntry;
import com.sun.javacard.filemodels.XListInstanceEntry;
import com.sun.javacard.filemodels.XListModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.constants.JavacardDeviceKeyNames;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Children of a Card node, which get their contents by calling
 * $CARDMANAGER_URL/xlist/
 *
 * @author Tim Boudreau
 */
public class CardChildren extends ChildFactory.Detachable<XListEntry> implements PropertyChangeListener {

    private final CardDataObject ob;
    private ObservableProperties props;

    public CardChildren(CardDataObject ob) {
        this.ob = ob;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        PropertiesAdapter adap = ob.getLookup().lookup(PropertiesAdapter.class);
        if (adap != null) {
            props = adap.asProperties();
            props.addPropertyChangeListener(this);
        }
    }

    @Override
    protected void removeNotify() {
        if (props != null) {
            props.removePropertyChangeListener(this);
            props = null;
        }
        super.removeNotify();
    }

    @Override
    protected Node createNodeForKey(XListEntry key) {
        return new EntryNode (key);
    }

    @Override
    protected boolean createKeys(List<XListEntry> toPopulate) {
        if (props != null) {
            String url = props.getProperty(JavacardDeviceKeyNames.DEVICE_CARDMANAGERURL);
            if (url != null) {
                if (!url.endsWith("/")) { //NOI18N
                    url = url + "/"; //NOI18N
                }
                url = url + "xlist"; //NOI18N
                InputStream in = null;
                try {
                    URL connectTo = new URL(url);
                    in = connectTo.openStream();
                    try {
                        XListModel mdl = new XListModel(in, ParseErrorHandler.NULL);
                        toPopulate.addAll(mdl.getData());
                    } catch (IOException ioe) {
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(CardChildren.class,
                                "MSG_LOAD_FAILED", url)); //NOI18N
                        Logger.getLogger(CardChildren.class.getName()).log(
                                Level.INFO, "Could not load children from " + //NOI18N
                                "xlist command for " + url, ioe); //NOI18N
                    } finally {
                        in.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CardChildren.class.getName()).log(
                            Level.WARNING,
                            "IOException getting children for URL from " + //NOI18N
                            ob.getPrimaryFile().getPath() + ":" + url, ex); //NOI18N
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(CardChildren.class.getName()).log(
                                    Level.WARNING, "IOException closing stream " + //NOI18N
                                    "for URL from " + //NOI18N
                                    ob.getPrimaryFile().getPath() + ":" + //NOI18N
                                    url, ex); //NOI18N
                            }
                }
            }
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
                            "resources/capproject.png"); //NOI18N
                    break;
                case CLASSIC_LIBRARY :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "resources/clslibproject.png"); //NOI18N
                    break;
                case EXTENDED_APPLET :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "resources/eapproject.png"); //NOI18N
                    break;
                case EXTENSION_LIBRARY :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "resources/extlibproject.png"); //NOI18N
                    break;
                case WEB :
                    setIconBaseWithExtension("org/netbeans/modules/javacard/" + //NOI18N
                            "resources/webproject.png"); //NOI18N
                    break;
                default :
                    break;
            }
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
                            "resources/instance.png"); //NOI18N
        }

        @Override
        public Action[] getActions (boolean ignored) {
            return new Action[]{};
        }
    }
}
