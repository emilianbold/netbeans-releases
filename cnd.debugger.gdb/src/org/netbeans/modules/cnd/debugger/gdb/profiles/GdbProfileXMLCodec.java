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

/*
 * "GdbProfileXMLCodec.java"
 */

package org.netbeans.modules.cnd.debugger.gdb.profiles;

import org.xml.sax.Attributes;

import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.api.xml.VersionException;

class GdbProfileXMLCodec extends XMLDecoder implements XMLEncoder {

    private final static int thisversion = 2;

    private final static String GDB_COMMAND_ELEMENT = "gdb_command"; // NOI18N
    private final static String ARRAY_REPEAT_THRESHOLD_ELEMENT = "array_repeat_threshold"; // NOI18N

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
//    	if (element.equals(GDB_COMMAND_ELEMENT)) {
//	    profile.setGdbCommand(currentText);
//	} else if (element.equals(ARRAY_REPEAT_THRESHOLD_ELEMENT)) {
//            try {
//                profile.setArrayRepeatThreshold(Integer.parseInt(currentText));
//            } catch (NumberFormatException ex) {
//            }
//        }
    }

    // intrface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	encode(xes, profile);
    }
    
    private static void encode(XMLEncoderStream xes, GdbProfile profile) {
//	xes.elementOpen(GdbProfile.GDB_PROFILE_ID, getVersion());
//	xes.element(GDB_COMMAND_ELEMENT, profile.getGdbCommand());
//        xes.element(ARRAY_REPEAT_THRESHOLD_ELEMENT, Integer.toString(profile.getArrayRepeatThreshold()));
//	xes.elementClose(GdbProfile.GDB_PROFILE_ID);
    }
}
