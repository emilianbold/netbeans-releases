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

package org.netbeans.modules.cnd.otool.debugger.gdb.imlp;

import org.netbeans.modules.cnd.debugger.gdb.mi.MIResult;
import org.netbeans.modules.cnd.debugger.gdb.mi.MITList;
import org.netbeans.modules.cnd.otool.debugger.api.OtoolNativeVariable;



class GdbVariable extends OtoolNativeVariable {
    private static final String HAS_MORE = "has_more";   //NOI18N
    private String miName;
    private boolean inScope = true;
    private boolean editable;
    
    GdbVariable(String name, String type, String value) {
        super(null, name, type, value);
    }
    
    GdbVariable(OtoolNativeVariable parent, String name, String type, String value) {
        super(parent, name, type, value);
    }    
    
    void setMIName(String miName) {
        this.miName = miName;
    }
    
    String getMIName() {
        return miName;
    }
    
    public void setInScope(boolean inScope) {
	this.inScope = inScope;
        if (!inScope) {
           // setNumChild("0"); //NOI18N
        }
    }

    public boolean isInScope() {
	return inScope;
    } 
    
void populateFields(MITList results) {
        setMIName(results.getConstValue("name")); // NOI18N
        setType(results.getConstValue("type")); // NOI18N

//      //  String numchild_l = results.getConstValue(OtoolG.MI_NUMCHILD);
//        MIValue dynamicVal = results.valueOf("dynamic"); //NOI18N
//        if (dynamicVal != null) {
//          //  setDynamic(dynamicVal.asConst().value());
//            //setDisplayHint(results.getConstValue("displayhint")); //NOI18N
//            String hasMoreVal = results.getConstValue(HAS_MORE);
//            if (!hasMoreVal.isEmpty()) {
//                numchild_l = hasMoreVal;
//              //  setHasMore(hasMoreVal);
//            } 
////            else {
////                switch (displayHint) {
////                    case ARRAY:
////                    case MAP:
////                    case NONE:
////                        numchild_l = "1"; // NOI18N
////                }
//            }
//        }
//        setNumChild(numchild_l); // also set children if there is any
    }

    protected void setEditable(String attr) {
	editable = attr.equals("editable"); // NOI18N
    }

    @Override
    public boolean isEditable() {
	return editable;
    }
    
    
    void populateUpdate(MITList results, VariableBag variableBag) {
        for (MIResult item : results.getOnly(MIResult.class)) {
            if (item.matches("in_scope")) { //NOI18N
//                if (this instanceof GdbWatch) {
//                    setInScope(Boolean.parseBoolean(item.value().asConst().value()));
//                }
            } else if (item.matches("new_type")) { //NOI18N
                setType(item.value().asConst().value());
            } else if (item.matches("new_num_children")) { //NOI18N
//                setNumChild(item.value().asConst().value());
//                if (!isLeaf()) {
//                    Variable[] ch = getChildren();
//                    for (Variable v : ch) {
//                        variableBag.remove(v);
//                    }
//                }
//                setChildren(null, false);
            } else if (item.matches("dynamic")) { //NOI18N
             //   setDynamic(item.value().asConst().value());
            } else if (item.matches("displayhint")) { //NOI18N
               // setDisplayHint(item.value().asConst().value());
            } else if (item.matches(HAS_MORE)) {
//                setHasMore(item.value().asConst().value());
//                if (hasMore) {
//                    setNumChild(item.value().asConst().value());
//                    if (!isLeaf()) {
//                        Variable[] ch = getChildren();
//                        for (Variable v : ch) {
//                            variableBag.remove(v);
//                        }
//                    }
//                    setChildren(null, false);
//                }
            } else if (item.matches("new_children")) { //NOI18N
                //TODO: can update new children from here
            }
        }
    }
    
}
