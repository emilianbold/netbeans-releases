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
package org.netbeans.modules.javacard.spi;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * The set of Card objects owned by a JavacardPlatform, which can be
 * listened on for changes.
 * <p/>
 * Provides a list of Lookup.Providers.  Each one's lookup should contain
 * a Card instance (although, since these represent system filesystem fileobjects
 * typically, it is important to check that they really do provide card
 * instances).
 *
 * @author Tim Boudreau
 */
public abstract class Cards {
    private final ChangeSupport supp = new ChangeSupport(this);
    protected Cards() {
    }

    /**
     * Called when the first listener is added.  If this object listens
     * on an external source to detect changes in the list of cards,
     * attach listeners here.
     */
    protected void addNotify() {
        //do nothing
    }

    /**
     * Called when the last listener is removed.  If this object listens
     * on an external source to detect changes in the list of cards,
     * detach listeners here.
     */
    protected void removeNotify() {
        //do nothing
    }

    /**
     * Get a list of Lookup.Providers (typically DataObjects for the
     * children of some folder in the system filesystem, or Nodes, but can
     * be anything) which may
     * contain Card instances.  If some do not contain cards, that is okay.
     * <p/>
     * A Card will be searched for as follows:
     * Look for a DataObject.  If present, look for a Card in its
     * Lookup.  If no card present, check the lookup of its Node.  If no
     * card present, check the Lookup directly for an instance of Card.
     * <p/>
     * Only one Card per Lookup.Provider is expected.
     * <p/>
     * The simplest way to implement this is to allow cards to be registered
     * in a known folder in the System Filesystem, then iterate its children,
     * and return add the DataObject for each child file.
     * <p/>
     * This method should never be called on the AWT event thread, as it
     * may involve I/O with a remote device or process.
     * 
     * @return A list of sources of Cards
     */
    public abstract List<? extends Lookup.Provider> getCardSources();

    /**
     * Get the cards provided by this Cards instance.
     * @param onlyValid do not include any cards that return false for isValid()
     * @return A list of Card objects
     */
    public final List<Card> getCards(boolean onlyValid) {
        List<? extends Lookup.Provider> provs = getCardSources();
        List<Card> result = new ArrayList<Card>(provs.size());
        for (Lookup.Provider prov : provs) {
            Card c = findCard(prov);
            if (c != null) {
                if (!onlyValid || (onlyValid && c.isValid())) {
                    result.add (c);
                }
            }
        }
        return result;
    }

    /**
     * Attach a listener for changes in the set of cards
     * @param cl
     */
    public synchronized final void addChangeListener(ChangeListener cl) {
        boolean hadListeners = supp.hasListeners();
        supp.addChangeListener(cl);
        if (!hadListeners) {
            addNotify();
        }
    }

    /**
     * Detach a listener for changes in the set of cards
     * @param cl
     */
    public synchronized final void removeChangeListener(ChangeListener cl) {
        supp.removeChangeListener(cl);
        if (!supp.hasListeners()) {
            removeNotify();
        }
    }

    /**
     * Create a Children object which can display all available cards
     * as children of a node.
     * @return A Children object
     */
    public Children createChildren() {
        return Children.create(new CF(), true);
    }

    /**
     * Fire a change, indicating to any listener that the set of cards
     * has changed.
     */
    protected final void fireChange() {
        supp.fireChange();
    }

    private Card findCard(Lookup.Provider prov) {
        DataObject dob = prov.getLookup().lookup(DataObject.class);
        if (dob != null) {
            Card card = dob.getLookup().lookup(Card.class);
            if (card == null) {
                card = dob.getNodeDelegate().getLookup().lookup(Card.class);
                if (card != null) {
                    return card;
                }
            } else {
                return card;
            }
        }
        Card card = prov.getLookup().lookup(Card.class);
        return card;
    }

    private class CF extends ChildFactory.Detachable<Lookup.Provider> implements ChangeListener {

        @Override
        protected void addNotify() {
            super.addNotify();
            Cards.this.addChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            Cards.this.removeChangeListener(this);
        }

        @Override
        protected boolean createKeys(List<Lookup.Provider> toPopulate) {
            toPopulate.addAll(getCardSources());
            return true;
        }

        @Override
        protected Node createNodeForKey(Lookup.Provider key) {
            Lookup l = key.getLookup();
            DataObject dob = l.lookup(DataObject.class);
            if (dob != null) {
                Card card = dob.getLookup().lookup(Card.class);
                if (card == null) {
                    card = dob.getNodeDelegate().getLookup().lookup(Card.class);
                    if (card != null) {
                        return new FilterNode(dob.getNodeDelegate(), Children.LEAF);
                    }
                } else {
                    return new FilterNode(dob.getNodeDelegate(), Children.LEAF);
                }
            }
            Card card = l.lookup(Card.class);
            if (card != null) {
                return new CardNode(key);
            }
            return null;
        }

        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
    }

    private static final class CardNode extends AbstractNode {

        CardNode(Lookup.Provider p) {
            super(Children.LEAF, p.getLookup());
            Card card = getLookup().lookup(Card.class);
            setName(card.getSystemId());
            CardInfo info = card.getCapability(CardInfo.class);
            if (info != null) {
                setDisplayName(info.getDisplayName());
                setShortDescription(info.getDescription());
            } else {
                setDisplayName(getName());
            }
        }

        @Override
        public Image getIcon(int type) {
            Card card = getLookup().lookup(Card.class);
            if (card != null) {
                CardInfo info = card.getCapability(CardInfo.class);
                if (info != null) {
                    return info.getIcon();
                }
            }
            return super.getIcon(type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }
}
