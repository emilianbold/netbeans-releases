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

import java.awt.Image;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * The set of Card objects owned by a JavacardPlatform, which can be
 * listened on for changes.
 * <p/>
 * Provides a list of Lookup.Providers.  Each one's lookup should contain
 * a Card instance (although, since these may represent system filesystem fileobjects
 * typically, it is important to check that they really do provide card
 * instances).  Typically, each Lookup.Provider is a DataObject, at least in
 * the case of the Java Card RI.  However, the only requirement is that it be
 * a Lookup.Provider with an instance of Card in its Lookup.
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
     * as children of a node.  This works as follows:  If the Lookup.Provider
     * for a given card contains a DataObject, the children of that DataObject
     * are used (so modules can completely replace the mechanism by which
     * children are shown if they want).  Otherwise, a Card will be searched
     * for in each Lookup.Provider's Lookup.  If found, then it will be queried
     * for a CardContentsProvider capability, and if that is found, then whatever
     * subtree that provides as an XListModel will be rendered as nodes.
     *
     * @return A Children object
     */
    public Children createChildren() {
        return Children.create(new CF(), true);
    }

    /**
     * Create a Children object which, if necessary, will contain a fake (invalid)
     * card with the given system id.
     *
     * @param expectedCardSystemId The system ID for a card that is expected to
     * exist
     * @return A Children object suitable for use under a Node
     */
    public Children createChildren(String expectedCardSystemId) {
        return Children.create(new CF(expectedCardSystemId), true);
    }

    /**
     * Fire a change, indicating to any listener that the set of cards
     * has changed.
     */
    protected final void fireChange() {
        supp.fireChange();
    }

    /**
     * Find a card with the given system ID (usually DataObject name).
     * @param systemId The system ID, unique per platform
     * @param <code>returnDummyCard</code> If true, never return null, but return a card
     * with the requested name which returns false from isValid().
     * @return A card or null if no card with that ID exists and
     * <code>returnDummyCard</code> is false
     */
    public final Card find (String systemId, boolean returnDummyCard) {
        for (Card c : getCards(false)) {
            if (systemId.equals(c.getSystemId())) {
                return c;
            }
        }
        return returnDummyCard ? AbstractCard.createBrokenCard(systemId) : null;
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
        private final String brokenCardName;
        CF(String brokenCardName) {
            this.brokenCardName = brokenCardName;
        }

        CF() {
            this(null);
        }

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
            if (brokenCardName != null) {
                boolean found = false;
                for (Lookup.Provider lp : toPopulate) {
                    Card card = lp.getLookup().lookup(Card.class);
                    found = card != null && brokenCardName.equals(card.getSystemId());
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    toPopulate.add(new DummyProvider(brokenCardName));
                }
            }
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
                    //PENDING: Check that we do not ever see card children in settings dlgs
                    //If broken card name is null, we are in use in the services tab, where
                    //we do want children
                    if (card != null) {
                        return brokenCardName == null ? new FilterNode(dob.getNodeDelegate()) : new FilterNode(dob.getNodeDelegate(), Children.LEAF);
                    }
                } else {
                    return brokenCardName == null ? new FilterNode(dob.getNodeDelegate()) : new FilterNode(dob.getNodeDelegate(), Children.LEAF);
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

    private static final class WeakCardStateObserver implements CardStateObserver {
        private final Reference<CardStateObserver> ref;
        WeakCardStateObserver(CardStateObserver real) {
            ref = new WeakReference<CardStateObserver>(real);
        }
        public void onStateChange(Card card, CardState old, CardState nue) {
            CardStateObserver real = ref.get();
            if (real == null) {
                card.removeCardStateObserver(this);
            } else {
                real.onStateChange(card, old, nue);
            }
        }

    }

    private static final class CardNode extends AbstractNode implements CardStateObserver {
        private Card hardRef; //DO NOT DELETE!
        CardNode(Lookup.Provider p) {
            super(Children.LEAF, p.getLookup());
            Card card = getLookup().lookup(Card.class);
            hardRef = card; //Ensure Card lifecycle matches Node's, otherwise
                            //polling to update node state will stop
            setName(card.getSystemId());
            CardInfo info = card.getCapability(CardInfo.class);
            if (info != null) {
                setDisplayName(info.getDisplayName());
                setShortDescription(info.getDescription());
            } else {
                setDisplayName(getName());
            }
            card.addCardStateObserver(new WeakCardStateObserver(this));
            CardContentsProvider prov =
                    card.getCapability(CardContentsProvider.class);
            if (prov != null) {
                setChildren (Children.create(new CardChildren(prov), true));
            }
        }

        @Override
        public void destroy() throws IOException {
            //Ensure anything polling isn't kept alive by someone
            //keeping a reference to a dead node
            hardRef = null;
            super.destroy();
        }

        @Override
        public Action[] getActions (boolean ignored) {
            Card card = getLookup().lookup(Card.class);
            JavacardPlatform platform = card.getPlatform();
            String kind = platform.getPlatformKind();
            String path = "org-netbeans-modules-javacard-spi/kinds/" + kind + "/Actions/"; //NOI18N
            return Lookups.forPath(path).lookupAll(Action.class).toArray(new Action[0]);
        }

        @Override
        public String getHtmlDisplayName() {
            Card card = getLookup().lookup(Card.class);
            if (!card.isValid()) {
                //XXX error fg?
                return "<font color='!nb.errorForeground'>" + getDisplayName(); //NOI18N
            } else if (!card.getState().isRunning()) {
                return "<font color='!controlShadow'>" + getDisplayName(); //NOI18N
            }
            return null;
        }

        @Override
        public Image getIcon(int type) {
            Image result = null;
            Card card = getLookup().lookup(Card.class);
            if (card != null) {
                CardInfo info = card.getCapability(CardInfo.class);
                if (info != null) {
                    result = info.getIcon();
                }
            }
            result = result == null ? super.getIcon(type) : result;
            if (card != null && card.getState().isRunning()) {
                Image badge = ImageUtilities.loadImage(
                        "org/netbeans/modules/javacard/spi/resources/running.png"); //NOI18N
                result = ImageUtilities.mergeImages(result, badge, 11, 11);
            }
            return result;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = Sheet.createDefault();
            Sheet.Set set = Sheet.createPropertiesSet();
            set.put(new IdProp());
            set.put(new StateProp());
            set.put(new ValidProp());
            sheet.put(set);
            return sheet;
        }

        public void onStateChange(Card card, CardState old, CardState nue) {
            fireIconChange();
            fireDisplayNameChange("", getDisplayName());
            firePropertyChange("state", old.toString(), nue.toString());
        }

        private class StateProp extends PropertySupport.ReadOnly<String> {

            StateProp() {
                super("state", String.class, NbBundle.getMessage(StateProp.class, //NOI18N
                        "PROP_STATE"), NbBundle.getMessage(StateProp.class, //NOI18N
                        "DESC_PROP_STATE")); //NOI18N

            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                Card card = getLookup().lookup(Card.class);
                return card == null ? "" : card.getState().toString();
            }
        }

        private class ValidProp extends PropertySupport.ReadOnly<Boolean> {
            ValidProp() {
                super ("valid", Boolean.class, NbBundle.getMessage(ValidProp.class, //NOI18N
                        "PROP_VALID"), NbBundle.getMessage(ValidProp.class, //NOI18N
                        "DESC_PROP_VALID")); //NOI18N
            }

            @Override
            public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
                Card card = getLookup().lookup(Card.class);
                return card != null && card.isValid();
            }
        }

        private class IdProp extends PropertySupport.ReadOnly<String> {
            IdProp() {
                super ("id", String.class, NbBundle.getMessage(IdProp.class, //NOI18N
                        "PROP_ID"), NbBundle.getMessage(IdProp.class, //NOI18N
                        "DESC_PROP_ID")); //NOI18N
            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                Card card = getLookup().lookup(Card.class);
                return card == null ? "?" : card.getSystemId(); //NOI18N
            }
        }
    }

    private static final class DummyProvider implements Lookup.Provider {
        private final Card dummyCard;
        DummyProvider(String s) {
            dummyCard = AbstractCard.createBrokenCard(s);
        }

        public Lookup getLookup() {
            return Lookups.singleton(dummyCard);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DummyProvider other = (DummyProvider) obj;
            if (this.dummyCard != other.dummyCard && (this.dummyCard == null ||
                    !this.dummyCard.equals(other.dummyCard))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return dummyCard.getSystemId().hashCode();
        }
    }
}
