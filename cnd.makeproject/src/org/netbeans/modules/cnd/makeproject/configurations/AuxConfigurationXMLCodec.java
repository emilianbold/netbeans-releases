/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations;

import java.util.Vector;
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

    private Vector decoders = new Vector();

    public AuxConfigurationXMLCodec(String tag,
				    ConfigurationDescriptor configurationDescriptor) {
	super(configurationDescriptor, false);
	this.tag = tag;
	this.configurationDescriptor = configurationDescriptor;
    }

    // interface XMLDecoder
    public String tag() {
	return tag;
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
	String what = "project configuration"; // NOI18N
	checkVersion(atts, what, CURRENT_VERSION);
    }

    // interface XMLDecoder
    public void end() {
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
	if (element.equals(CONF_ELEMENT)) {
	    String currentConfName = atts.getValue(0);
	    Configurations confs = configurationDescriptor.getConfs();
	    Configuration currentConf = confs.getConf(currentConfName);

	    // switch out old decoders
	    for (int dx = 0; dx < decoders.size(); dx++) {
		XMLDecoder decoder = (XMLDecoder) decoders.elementAt(dx);
		deregisterXMLDecoder(decoder);
	    }

	    // switch in new decoders
	    ConfigurationAuxObject[] profileAuxObjects =
		currentConf.getAuxObjects();
	    decoders = new Vector();
	    for (int i = 0; i < profileAuxObjects.length; i++) {
		XMLDecoder newDecoder = profileAuxObjects[i].getXMLDecoder();
		registerXMLDecoder(newDecoder);
		decoders.add(newDecoder);
	    }
	}
    }

    // interface XMLDecoder
    public void endElement(String element, String currentText) {
    }
}
