/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.jsfcl.std.reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MediaTypesReferenceDataDefiner extends ReferenceDataDefiner {
    protected static final String FILENAME = "media-types.txt"; //NOI18N

    public void addBaseItems(List list) {
        InputStream in;
        BufferedReader reader;
        ReferenceDataItem item;

        list.add(newItem(
            "", // NOI18N
            null,
            true,
            false));
        in = null;
        try {
            in = getClass().getResourceAsStream(FILENAME);
            if (in == null) {
                assert ReferenceDataManager.loggerUtil.warning("Could not find mime-types file: " +
                    FILENAME); //NOI18N
                return;
            }
            reader = new BufferedReader(new InputStreamReader(in));
            String line, contentType;

            contentType = null;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.length() == 0 || line.startsWith("#")) { //NOI18N
                    continue;
                }
                // are we at the end marker line, a line with at least ---
                if (line.startsWith("---")) { //NOI18N
                    break;
                }
                int index;
                String contentSubType, combinedName;
                boolean isSubTypeLine;

                isSubTypeLine = Character.isWhitespace(line.charAt(0));
                line = line.trim();
                if (!isSubTypeLine) {
                    // extract the content type
                    // these should really be up to first whitespace
                    index = line.indexOf(" "); //NOI18N
                    if (index == -1) {
                        contentType = line;
                    } else {
                        contentType = line.substring(0, index);
                        line = line.substring(index).trim();
                    }
                }
                // remove the portion pointing to the RFC for this subtype
                index = line.indexOf("["); //NOI18N
                if (index == -1) {
                    contentSubType = line;
                } else {
                    contentSubType = line.substring(0, index).trim();
                }
                if (contentSubType.length() == 0) {
                    contentSubType = null;
                }
                combinedName = contentType;
                if (contentType == null) {
                    throw new RuntimeException("Found an entry in " + FILENAME +
                        " with no content type defined"); //NOI18N
                }
                if (contentSubType != null) {
                    combinedName += "/" + contentSubType; //NOI18N
                }
                item = newItem(combinedName, combinedName, false, false);
                list.add(item);
            }
        } catch (IOException e) {
            assert ReferenceDataManager.loggerUtil.config(
                "Problems parsing reference data ref file for: " + FILENAME); //NOI18N
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
                ;
            }
        }
    }

    public boolean canAddRemoveItems() {

        return true;
    }

    public boolean isValueAString() {

        return true;
    }

}
