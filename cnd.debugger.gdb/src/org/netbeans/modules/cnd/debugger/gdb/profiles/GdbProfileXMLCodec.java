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

/*
 * "GdbProfileXMLCodec.java"
 */

package org.netbeans.modules.cnd.debugger.gdb.profiles;

import org.xml.sax.Attributes;

import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;

class GdbProfileXMLCodec extends XMLDecoder implements XMLEncoder {

    private final static int thisversion = 1;

    private final static String GDB_COMMAND_ELEMENT = "gdb_command"; // NOI18N

    private GdbProfile profile;		// we decode into

    public GdbProfileXMLCodec(GdbProfile profile) {
	this.profile = profile;
    }

    public static int getVersion() {
	return thisversion;
    }

    // interface XMLDecoder
    public String tag() {
	return GdbProfile.GDB_PROFILE_ID;
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
	String what = tag();
	int maxVersion = getVersion();
	checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    public void end() {
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
    }

    // interface XMLDecoder
    public void endElement(String element, String currentText) {
    	if (element.equals(GDB_COMMAND_ELEMENT)) {
	    profile.setGdbCommand(currentText);
	}
    }

    // intrface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	encode(xes, profile);
    }
    
    private static void encode(XMLEncoderStream xes, GdbProfile profile) {
	xes.elementOpen(GdbProfile.GDB_PROFILE_ID, getVersion());
	xes.element(GDB_COMMAND_ELEMENT, profile.getGdbCommand());
	xes.elementClose(GdbProfile.GDB_PROFILE_ID);
    }
}
