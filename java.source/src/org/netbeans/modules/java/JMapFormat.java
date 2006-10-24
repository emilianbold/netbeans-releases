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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java;

import java.util.*;

import org.openide.util.MapFormat;

/**
* The message formatter, which uses map's keys in place of numbers.
* This class extends functionality of MessageFormat by allowing the user to
* specify strings in place of MessageFormatter's numbers. It then uses given
* map to translate keys to values. It also handles conditional expressions.
*
* You will usually use this formatter as follows:
* 	<code>JMapFormat.format("Hello {name}", map);</code>
*
* For conditional expression:
*       <code>JMapFormat.format("Hello {name,old_suffix,new_suffix,no_match}"); </code>
*  Last three parameters are optional. At first, name is translated. If it then has
*  suffix old_suffix it is replaced by te new_suffix else whole parrent is replaced by
*  no_match string.
*  
* Notes: if map does not contain value for key specified, it substitutes
* the value by <code>"null"</code> word to qualify that something goes wrong.
*
* @author   Slavek Psenicka, Martin Ryzl
* @version  1.0, March 11. 1999
*/

public class JMapFormat extends MapFormat {

    private String cdel = ","; // NOI18N

    static final long serialVersionUID =5503640004816201285L;
    public JMapFormat(Map map) {
        super(map);
    }

    protected Object processKey(String key) {
        //    System.out.println("key = " + key); // NOI18N
        StringTokenizer st = new StringTokenizer(key, cdel, true);
        String data[] = new String[4];
        String temp;

        for(int i = 0; (i < 4) && st.hasMoreTokens(); ) {
            temp = st.nextToken();
            if (temp.equals("$")) i++; // NOI18N
            else data[i] = temp;
        }

        Object obj = super.processKey(data[0]);
        if (obj instanceof String) {
            if (data[1] != null) {
                String name = (String)obj;
                if (name.endsWith(data[1])) {
                    if (data[2] == null) data[2] = ""; // NOI18N
                    return name.substring(0, name.length() - data[1].length()) + data[2];
                } else {
                    if (data[3] != null) return data[3];
                    else return obj;
                }
            }
        }
        return obj;
    }

    public String getCondDelimiter() {
        return cdel;
    }

    public void setCondDelimiter(String cdel) {
        this.cdel = cdel;
    }

}
