/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.configurations;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.xml.sax.Attributes;

class AuxConfigurationXMLCodec extends CommonConfigurationXMLCodec {

    private String tag;
    private ConfigurationDescriptor configurationDescriptor;
    private List<XMLDecoder> decoders = new ArrayList<XMLDecoder>();

    public AuxConfigurationXMLCodec(String tag,
            ConfigurationDescriptor configurationDescriptor) {
        super(configurationDescriptor, false);
        this.tag = tag;
        this.configurationDescriptor = configurationDescriptor;
    }

    // interface XMLDecoder
    @Override
    public String tag() {
        return tag;
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
        String what = "project configuration"; // NOI18N
        checkVersion(atts, what, CURRENT_VERSION);
    }

    // interface XMLDecoder
    @Override
    public void end() {
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
        if (element.equals(CONF_ELEMENT)) {
            String currentConfName = atts.getValue(NAME_ATTR);
            Configurations confs = configurationDescriptor.getConfs();
            Configuration currentConf = confs.getConf(currentConfName);

            // switch out old decoders
            for (int dx = 0; dx < decoders.size(); dx++) {
                XMLDecoder decoder = decoders.get(dx);
                deregisterXMLDecoder(decoder);
            }

            // switch in new decoders
            ConfigurationAuxObject[] profileAuxObjects =
                    currentConf.getAuxObjects();
            decoders = new ArrayList<XMLDecoder>();
            for (int i = 0; i < profileAuxObjects.length; i++) {
                if (!profileAuxObjects[i].shared()) {
                    XMLDecoder newDecoder = profileAuxObjects[i].getXMLDecoder();
                    registerXMLDecoder(newDecoder);
                    decoders.add(newDecoder);
                }
            }
        }
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
        if (element.equals(DEFAULT_CONF_ELEMENT)) {
            configurationDescriptor.getConfs().setActive(new Integer(currentText).intValue());
        }
    }
}
