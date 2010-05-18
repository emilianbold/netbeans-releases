/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
