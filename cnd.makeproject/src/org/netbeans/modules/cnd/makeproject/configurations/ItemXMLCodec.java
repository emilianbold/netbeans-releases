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

import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.xml.sax.Attributes;

public class ItemXMLCodec extends XMLDecoder implements XMLEncoder {

    private ItemConfiguration item;

    public final static String ITEM_ELEMENT = "item"; // NOI18N
    public final static String PATH_ATTR = "path"; // NOI18N
    public final static String EXCLUDED_ELEMENT = "excluded"; // FIXUP: < 7 // NOI18N
    public final static String TOOL_ELEMENT = "tool"; // FIXUP: < 7 // NOI18N
    public final static String ITEM_EXCLUDED_ELEMENT = "itemExcluded"; // NOI18N
    public final static String ITEM_TOOL_ELEMENT = "itemTool"; // NOI18N
    public final static String DEBUGGING_ELEMENT = "justfordebugging"; // NOI18N

    public final static String TRUE_VALUE = "true"; // NOI18N
    public final static String FALSE_VALUE = "false"; // NOI18N

    public ItemXMLCodec(ItemConfiguration item) {
	this.item = item;
    }

    // interface XMLDecoder
    public String tag() {
	return item.getId();
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
        String what = "item"; // NOI18N
        int maxVersion = 1;
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
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	xes.elementOpen(ITEM_ELEMENT, new AttrValuePair[] {new AttrValuePair(PATH_ATTR, item.getItem().getPath())});
	if (item.getExcluded().getModified())
	    xes.element(ITEM_EXCLUDED_ELEMENT, "" + item.getExcluded().getValue()); // NOI18N
	xes.element(ITEM_TOOL_ELEMENT, "" + item.getTool()); // NOI18N
	if (item.getTool() == Tool.CCompiler) {
	    CommonConfigurationXMLCodec.writeCCompilerConfiguration(xes, item.getCCompilerConfiguration());
	}
	else if (item.getTool() == Tool.CCCompiler) {
	    CommonConfigurationXMLCodec.writeCCCompilerConfiguration(xes, item.getCCCompilerConfiguration());
	}
	else if (item.getTool() == Tool.CustomTool) {
	    CommonConfigurationXMLCodec.writeCustomToolConfiguration(xes, item.getCustomToolConfiguration());
	}
	xes.elementClose(ITEM_ELEMENT);
    } 
}
