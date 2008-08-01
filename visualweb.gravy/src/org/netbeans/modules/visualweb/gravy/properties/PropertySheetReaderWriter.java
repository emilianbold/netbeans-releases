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

package org.netbeans.modules.visualweb.gravy.properties;


import org.netbeans.modules.visualweb.gravy.properties.Property;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.MainWindowOperator;

import javax.swing.*;

import org.netbeans.jemmy.operators.JTableOperator;

/**
 * PropertySheetReaderWriter class
 * <p>
 * This class can read and write properties on property sheet.
 *
 */

public class PropertySheetReaderWriter implements PropertyReaderWriter {
    //private PropertySheetOperator pso;
      private SheetTableOperator pso;

    /**
     * Constructs PropertySheetReaderWriter object
     */
    public PropertySheetReaderWriter() {
        pso = new SheetTableOperator();//new PropertySheetOperator(Util.getMainWindow());
	pso.setCompareStyle(true, false);
        // descr = pso.getDescriptionHeader();
    }


    /**
     * Read value of specified property
     *
     * @param name Name of property to read
     * @return  value of specified property
     */
    public Object getPropertyValue(String name) {
	String value = (new Property(new PropertySheetOperator(Util.getMainWindow()), name)).getValue();//pso.getValue(name);
        return value;//(new Property(pso, name)).getValue();
    }

    /**
     * Set value for specified property
     *
     * @param name Name of property to read
     *        value Property value
     */
    public void setPropertyValue(String name, Object value) {
	System.out.println("Setting value of " + name + " TO " +value);
        //if(!(getPropertyValue(name).equals(value))) {
            //(new Property(pso, name)).setValue(value.toString());
	    //pso.setTextValue(name, value.toString());
	    //new SheetTableOperator().setTextValue(name, value.toString());
	new Property (
		new PropertySheetOperator(Util.getMainWindow()),name).setValue(value.toString());
        //}    
    }

    /**
     * Returns name of Reader
     *
     * @return  name of Reader
     */
    public String getName() {
        return "PropertySheet";
    }
}
