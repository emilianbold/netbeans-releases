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
public class CharacterSetsReferenceDataDefiner extends ReferenceDataDefiner {
    protected static final String FILENAME = "character-sets.txt"; // NOI18N

    public void addBaseItems(List list) {
        InputStream in;
        BufferedReader reader;
        ReferenceDataItem item;
        final String lookForName = "Name:"; // NOI18N
        final String lookForAlias = "Alias:"; // NOI18N
        final String noneAlias = "None"; // NOI18N

        super.addBaseItems(list);
        // programatically add the "", null reference item
        item = newItem("", null, null, true, false); // NOI18N
        list.add(item);
        in = null;
        try {
            in = getClass().getResourceAsStream(FILENAME);
            reader = new BufferedReader(new InputStreamReader(in));
            String line, extractedName;

            // Go to very first Name: line
            while (true) {
                line = reader.readLine();
                if (line == null || line.startsWith(lookForName)) {
                    break;
                }
            } while (line != null) {
                int index;
                ReferenceDataItem aliasFor;

                extractedName = line.substring(lookForName.length()).trim();
                index = extractedName.indexOf(' ');
                if (index != -1) {
                    extractedName = extractedName.substring(0, index);
                }
                aliasFor = newItem(extractedName, extractedName, null, false, false);
                list.add(aliasFor);
                while (true) {
                    String alias;

                    line = reader.readLine();
                    if (line == null || line.startsWith(lookForName)) {
                        break;
                    }
                    if (line.startsWith(lookForAlias)) {
                        alias = line.substring(lookForAlias.length()).trim();
                        if (!alias.equals(noneAlias)) {
                            item = newItem(alias, alias, null, false, false, aliasFor);
                            list.add(item);
                        }
                    }
                }
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
