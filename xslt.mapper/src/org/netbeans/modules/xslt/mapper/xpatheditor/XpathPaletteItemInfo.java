/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xslt.mapper.xpatheditor;

import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.openide.filesystems.FileObject;


/**
 * XPATH Palette Item information, used to keep information and helper methods
 * used by XPATH collaboration editor.
 *
 * @author Tientien Li
 * @version 
 */
public class XpathPaletteItemInfo {
    
    private FileObject myFo;
    
    /**
     * Creates a new instance of XpathPaletteItemInfo
     *
     * @param cat the category
     * @param itm the item
     */
    public XpathPaletteItemInfo(FileObject itmFo) {
        myFo = itmFo;
    }
    
    /**
     * get the maximum number of input.
     *
     * @return the max input count
     */
    public int getMaxInput() {
        int val = 0;
        Object o = myFo.getAttribute(Constants.XPATH_MAXINPUT);
        if (o != null) {
            try {
                val = Integer.parseInt((String) o);
            } catch (Exception ex) {
                // OK.. it is not a valid integer, return 0 instead.
            }
        }
        return val;
    }
    
    /**
     * is the palette an operator.
     *
     * @return true if it is an operator
     */
    public boolean isOperator() {
        String s = (String) myFo.getAttribute(Constants.XPATH_OPERATOR);
        return ((s != null) && !s.equals(""));
    }
    
    /**
     * is the palette an operator.
     *
     * @return true if it is an operator
     */
    public boolean isFunction() {
        String s = (String) myFo.getAttribute(Constants.XPATH_FUNCTION);
        return (s != null && !s.equals(""));
    }
    
    /**
     * is the palette an Number.
     *
     * @return true if it is an Number
     */
    public boolean isNumber() {
        String s = (String) myFo.getAttribute(Constants.XPATH_NUMBER);
        return (s != null && !s.equals(""));
    }
    
    /**
     * is the palette an Boolean.
     *
     * @return true if it is an Boolean
     */
    public boolean isBoolean() {
        String s = (String) myFo.getAttribute(Constants.XPATH_BOOLEAN);
        return (s != null && !s.equals(""));
    }
    
    /**
     * is the palette an String.
     *
     * @return true if it is an String
     */
    public boolean isString() {
        String s = (String) myFo.getAttribute(Constants.XPATH_STRING);
        return (s != null && !s.equals(""));
    }
    
    /**
     * Retrieve the value of the attribute from the IPaletteItem.
     * (from layer.xml)
     *
     * @param attrbName name of the attribute whose value we return
     * @return string
     */
    public String getItemAttribute(String attrbName) {
        
        return (String) myFo.getAttribute(attrbName);
    }
    
    /**
     * Retrieve the value of the attribute from the IPaletteItem.
     * (from layer.xml)
     *
     * @return string
     */
    private String getOperator() {
        
        return isOperator() ? (String) myFo.getAttribute(Constants.XPATH_OPERATOR) : null;
    }
    
    /**
     * Retrieve the value of the attribute from the IPaletteItem.
     * (from layer.xml)
     *
     * @return string
     */
    private String getFunction() {
        
        return isFunction() ? (String) myFo.getAttribute(Constants.XPATH_FUNCTION) : null;
    }
    
    /**
     * Retrieve the value of the attribute from the IPaletteItem.
     * (from layer.xml)
     *
     * @return string
     */
    private String getNumber() {
        
        return isNumber() ? (String) myFo.getAttribute(Constants.XPATH_NUMBER) : null;
    }
    
    /**
     * Retrieve the value of the attribute from the IPaletteItem.
     * (from layer.xml)
     *
     * @return string
     */
    private String getBoolean() {
        
        return isBoolean() ? (String) myFo.getAttribute(Constants.XPATH_BOOLEAN) : null;
    }
    
    /**
     * Retrieve the value of the attribute from the IPaletteItem.
     * (from layer.xml)
     *
     * @return string
     */
    private String getString() {
        return isString() ? (String) myFo.getAttribute(Constants.XPATH_STRING) : null;
    }
    
    /**
     * Describe <code>getOperation</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getOperation() {
        
        if (isOperator()) {
            return getOperator();
            
        } else if (isFunction()) {
            return getFunction();
            
        } else if (isBoolean()) {
            return getBoolean();
            
        } else if (isNumber()) {
            return getNumber();
            
        } else if (isString()) {
            return getString();
            
        }
        return null;
    }
    
}
