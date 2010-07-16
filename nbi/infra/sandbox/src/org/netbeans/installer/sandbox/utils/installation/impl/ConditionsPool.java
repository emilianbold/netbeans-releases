/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
