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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import javax.swing.Action;
import javax.swing.JButton;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.spi.actions.CardActions;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import org.netbeans.modules.javacard.spi.capabilities.EpromFileCapability;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class CardActionsTest extends NbTestCase {
    public CardActionsTest() {
        super ("CardActionsTest");
    }

    static InstanceContent selContent = new InstanceContent();
    static Lookup globalSelection = new AbstractLookup(selContent);
    Node nd;
    FakeCard card;

    @Before
    @Override
    public void setUp() {
        MockServices.setServices(CTX.class);
        card = new FakeCard();
        nd = new AbstractNode (Children.LEAF, Lookups.fixed(card));
        for (Object o : globalSelection.lookupAll(Object.class)) {
            selContent.remove(o);
        }
        selContent.add(nd);
    }

    @Test
    public void testCardActions() {
        assertNotNull (Utilities.actionsGlobalContext().lookup(Node.class));
        assertSame (nd, Utilities.actionsGlobalContext().lookup(Node.class));
        Action start = CardActions.createStartAction();
        Action stop = CardActions.createStopAction();
        Action eprom = CardActions.createClearEpromAction();
        //Ensure action addNotify called
        JButton[] jbs = new JButton[] {
            new JButton(start), new JButton(stop), new JButton(eprom),
        };
        assertNotNull (start);
        assertNotNull (stop);
        assertNotNull (eprom);
        assertTrue (start.isEnabled());
        assertFalse (stop.isEnabled());
        assertFalse (eprom.isEnabled());
        start.actionPerformed(null);
        assertFalse (start.isEnabled());
        assertTrue (stop.isEnabled());
        assertFalse (eprom.isEnabled());
        stop.actionPerformed(null);
        assertTrue (start.isEnabled());
        assertFalse (stop.isEnabled());
        assertTrue (eprom.isEnabled());
        eprom.actionPerformed(null);
        assertTrue (start.isEnabled());
        assertFalse (stop.isEnabled());
        assertFalse (eprom.isEnabled());
    }

    private static class FakeCard implements Card, CardInfo {
        private final InstanceContent content = new InstanceContent();
        private final AbstractLookup lkp = new AbstractLookup(content);
        FakeCard() {
            content.add(this);
            content.add(new Start());
        }

        public <T extends ICardCapability> T getCapability(Class<T> type) {
            if (type.isInstance(this)) {
                return (T) this;
            }
            return null;
        }

        private Set<Class<? extends ICardCapability>> set(Class<? extends ICardCapability>... c) {
            Set<Class<? extends ICardCapability>> result = new HashSet();
            result.addAll (Arrays.asList(c));
            return result;
        }

        public Set<Class<? extends ICardCapability>> getSupportedCapabilities() {
            return (set(CardInfo.class, StopCapability.class, ClearEpromCapability.class, StartCapability.class));
        }

        public Set<Class<? extends ICardCapability>> getEnabledCapabilities() {
            Collection<? extends ICardCapability> all = lkp.lookupAll(ICardCapability.class);
            Set<Class<? extends ICardCapability>> result = new HashSet<Class<? extends ICardCapability>>(all.size());
            for (ICardCapability c : all) {
                Class<?> type = c.getClass();
                for (Class<?> i : type.getInterfaces()) {
                    if (ICardCapability.class.isAssignableFrom(i)) {
                        result.add ((Class<? extends ICardCapability>) i);
                    }
                }
            }
            return result;
        }

        public boolean isCapabilityEnabled(Class<? extends ICardCapability> type) {
            return getEnabledCapabilities().contains(type);
        }

        public boolean isCapabilitySupported(Class<? extends ICardCapability> type) {
            return getSupportedCapabilities().contains(type);
        }

        CardState state = CardState.NEW;
        public synchronized CardState getState() {
            return state;
        }

        public void addCardStateObserver(CardStateObserver obs) {
            //do nothing
        }

        public void removeCardStateObserver(CardStateObserver obs) {
            //do nothing
        }

        public JavacardPlatform getPlatform() {
            return new BrokenJavacardPlatform("Foo");
        }

        public String getSystemId() {
            return "Foo";
        }

        public boolean isValid() {
            return true;
        }

        public Lookup getLookup() {
            return lkp;
        }

        private synchronized void setState(CardState state) {
            this.state = state;
        }

        public String getDisplayName() {
            return getSystemId();
        }

        public Image getIcon() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        private class Start implements StartCapability {
            public Condition start(RunMode mode, Project project) {
                setState(CardState.NOT_RUNNING);
                content.remove(this);
                content.add(new Stop());
                return null;
            }
        }

        private class Stop implements StopCapability {
            public Condition stop() {
                setState (CardState.RUNNING);
                content.remove(this);
                content.add(new Start());
                content.add(new Clear());
                return null;
            }
        }

        private class Clear implements ClearEpromCapability, EpromFileCapability {

            public void clear() {
                content.remove(this);
            }

            public FileObject getEpromFile() {
                return FileUtil.getConfigRoot();
            }
        }
    }

    public static final class CTX implements ContextGlobalProvider {

        public Lookup createGlobalContext() {
            return globalSelection;
        }

    }

}
