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
 * $Id$
 */

package org.netbeans.installer.sandbox.utils.installation.impl;

import java.util.HashMap;
import org.netbeans.installer.sandbox.utils.installation.conditions.AndCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.CRC32Condition;
import org.netbeans.installer.sandbox.utils.installation.conditions.DefaultFileCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.EmptyDirectoryCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.FalseCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.FileCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.LogicalCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.MD5Condition;
import org.netbeans.installer.sandbox.utils.installation.conditions.NotCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.OrCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.SHA1Condition;
import org.netbeans.installer.sandbox.utils.installation.conditions.SizeCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.TimeCondition;
import org.netbeans.installer.sandbox.utils.installation.conditions.TrueCondition;
import org.netbeans.installer.utils.installation.conditions.*;

/**
 *
 * @author Dmitry Lipin
 */
class ConditionsPool {
    private HashMap     <String, FileCondition> conditionMap =
            new HashMap <String, FileCondition> ();
    
    public ConditionsPool() {
        initConditions();
    }
    protected HashMap getConditionMap() {
        return conditionMap;
    }
    protected FileCondition getCondition(String str) {
        String s = str;
        FileCondition result=null;
        if(s!=null) {
            int start = s.indexOf("(");
            int end = s.lastIndexOf(")");
            if(start!=-1 && end!=-1) {
                // there are some logical conditions: and, or, not
                result = getConditionByName(s.substring(0,start));
                String [] params =  s.substring(start+1,end).split(",");
                FileCondition [] fcs = new FileCondition [params.length] ;
                for(int i=0;i<params.length;i++) {
                    fcs[i] = getCondition(params[i]);
                }
                if(result instanceof LogicalCondition) {
                    result = ((LogicalCondition)result).clone();
                    ((LogicalCondition)result).setConditions(fcs);
                }
            } else {
                result = getConditionByName(s);
            }
        }
        return result;
    }
    private void initConditions() {
        addCondition(new AndCondition());
        addCondition(new CRC32Condition());
        addCondition(new DefaultFileCondition());
        addCondition(new FalseCondition());
        addCondition(new MD5Condition());
        addCondition(new NotCondition());
        addCondition(new EmptyDirectoryCondition());
        addCondition(new OrCondition());
        addCondition(new SHA1Condition());
        addCondition(new SizeCondition());
        addCondition(new TimeCondition());
        addCondition(new TrueCondition());
    }
    private FileCondition getConditionByName(String name) {
        // it is not a good idea to do that.
        // Maybe it should be replaced by DefaultFileCondition or smth else
        return (conditionMap.containsKey(name)) ?
            conditionMap.get(name) : new DefaultFileCondition();
        
        
    }
    public void addCondition(FileCondition fcc) {
        if(fcc instanceof LogicalCondition) {
            // if adding condition is complex then first add all child conditionsF
            FileCondition[] fcs = ((LogicalCondition)fcc).getConditions();
            for(FileCondition fc:fcs) {
                addCondition(fc);
            }
        }
        if(!conditionMap.containsKey(fcc.getName())) {
            conditionMap.put(fcc.getName(),fcc);
        }
    }
}
