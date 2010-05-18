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


/*
 * UMLPropertySheetOperator.java
 *
 * Created on April 22, 2005, 5:02 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.umllib;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/**
 *
 * @author VijayaBabu Mummaneni
 */
public class UMLPropertySheetOperator extends PropertySheetOperator {
    
    /**
     * Creates a new instance of UMLPropertySheetOperator
     * @param propWindowTitle 
     */
    public UMLPropertySheetOperator(String propWindowTitle) {
        super(propWindowTitle);
    }
    
    
    /**
     * Returns a HashMap that contains all the property names and their 
     *  values. A Node should be selected before calling this method.
     * @return 
     */
    public Map<String, String> getUMLPropertyNamesAndValues(){
        int numberOfProps = this.tblSheet().getRowCount() ;
        System.out.println("######Row count  is :" + numberOfProps);
        Map<String, String> props =  new HashMap<String, String>();        
        for (int i=1; i<numberOfProps; i++){
            Property p = new Property(this, i);
            String propName = p.getName();
            String propValue = p.getValue();
            props.put(propName, propValue);
        }
        System.out.println("#####Map is :" + props);
        return props ;
    }
    
    
    
    
}
