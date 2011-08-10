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
package org.netbeans.modules.coherence.server.ui;

import org.netbeans.modules.coherence.server.CoherenceServerProperty;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * Class for creating node with {@link Sheet} for setup all {@link CoherenceServerProperty}.
 * Sheet is supported just for {@code Node}s so it has to be enclosed in {@code AbstractNode}.
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ServerPropertiesNode extends AbstractNode {

    private static final Logger LOGGER = Logger.getLogger(ServerPropertiesNode.class.getName());

    public InstanceProperties instanceProperties;

    public ServerPropertiesNode(InstanceProperties instanceProperties) {
        super(Children.LEAF);
        this.instanceProperties = instanceProperties;
    }

    @Override
    public Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set properties = null;
        if (properties == null) {
            properties = Sheet.createPropertiesSet();
            sheet.put(properties);
        }

        for (CoherenceServerProperty key : CoherenceProperties.SERVER_PROPERTIES) {
            properties.put(new CoherenceProptertySupport(key));
        }

        return sheet;
    }

    private class CoherenceProptertySupport extends PropertySupport.ReadWrite {

        private CoherenceServerProperty key = null;

        public CoherenceProptertySupport(CoherenceServerProperty key) {
            super(key.getDisplayName(), key.getClazz() == Boolean.class ? Boolean.class : String.class, key.getDisplayName(), key.getHint());
            this.key = key;
        }

        @Override
        public Object getValue() {
            if (key.getClazz() == Boolean.class) {
                return instanceProperties.getBoolean(
                        key.getPropertyName(),
                        Boolean.parseBoolean(key.getDefaultValue() == null ? "false" : key.getDefaultValue())); //NOI18N
            }
            return instanceProperties.getString(
                    key.getPropertyName(),
                    key.getDefaultValue() == null ? "" : key.getDefaultValue());
        }

        @Override
        public void setValue(Object nue) {
            LOGGER.log(Level.FINEST, "*** APH-I3 : Class instanceof {0}", nue.getClass().getSimpleName()); //NOI18N
            if (nue == null || (nue instanceof String && nue.toString().length() == 0)
                    || (nue instanceof Integer && ((Integer) nue).intValue() <= 0)
                    || (nue instanceof Long && ((Long) nue).longValue() <= 0)) {
                instanceProperties.removeKey(key.getPropertyName());
            } else {
                try {
                    if (key.getClazz() == Integer.class) {
                        Integer i = Integer.parseInt(nue.toString());
                    } else if (key.getClazz() == Long.class) {
                        Long l = Long.parseLong(nue.toString());
                    }
                    instanceProperties.putString(key.getPropertyName(), nue.toString());
                } catch (NumberFormatException nfe) {
                    LOGGER.log(Level.INFO, "*** APH-I3 : Property Value {0} is not a number", nue.toString()); //NOI18N
                    instanceProperties.removeKey(key.getPropertyName());
                }
            }
        }
    }
}
