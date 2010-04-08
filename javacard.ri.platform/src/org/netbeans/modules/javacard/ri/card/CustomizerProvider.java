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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.ri.card;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Properties;
import java.util.WeakHashMap;
import org.netbeans.modules.javacard.common.KeysAndValues;
import org.netbeans.modules.javacard.ri.platform.installer.DevicePropertiesPanel;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.CardCustomizer;
import org.netbeans.modules.javacard.spi.capabilities.CardCustomizerProvider;
import org.netbeans.validation.api.ui.ValidationGroup;

/**
 * Implementation of CardCustomizerProvider for RI cards.  Registered in
 * layer file against the RI platform kind.
 *
 * @author Tim Boudreau
 */
public class CustomizerProvider implements CardCustomizerProvider {
    private final WeakHashMap<Card, Reference<CardCustomizer>> m = new WeakHashMap<Card, Reference<CardCustomizer>>();
    public CardCustomizer getCardCustomizer(Card card) {
        CardProperties props = card.getCapability(CardProperties.class);
        if (props != null) {
            synchronized (this) {
                Reference<CardCustomizer> ref = m.get(card);
                CardCustomizer cc = ref == null ? null : ref.get();
                if (cc == null) {
                    Properties p = props.toProperties();
                    cc = new CC(p);
                    m.put(card, new WeakReference<CardCustomizer>(cc));
                }
                return cc;
            }
        } else {
            //XXX return a dummy instance?
            return null;
        }
    }

    private static final class CC implements CardCustomizer {
        private DevicePropertiesPanel pnl;
        private final Properties props;
        CC(Properties props) {
            this.props = props;
        }

        public void save() {
            assert isContentValid();
            assert pnl != null;
            pnl.write(new KeysAndValues.PropertiesAdapter(props));
        }

        public ValidationGroup getValidationGroup() {
            getComponent();
            return pnl.getValidationGroup();
        }

        public boolean isContentValid() {
            getComponent();
            return !pnl.getValidationGroup().validateAll().isFatal();
        }

        public Component getComponent() {
            assert EventQueue.isDispatchThread();
            if (pnl == null) {
                pnl = new DevicePropertiesPanel(props);
            } else {
                pnl.read(new KeysAndValues.PropertiesAdapter(props));
            }
            return pnl;
        }
    }
}
