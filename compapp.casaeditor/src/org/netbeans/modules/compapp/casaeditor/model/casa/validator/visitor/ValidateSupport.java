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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.compapp.casaeditor.model.casa.validator.visitor;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;


/**
 * Supports validation of CASA document.
 *
 * @author  edwong
 * @version 
 */
public class ValidateSupport  {
    
    /** Validate Configuration */
    private ValidateConfiguration mValConfig;
  
    private Collection<ResultItem> mResultItems; 
    private Validator mValidator; 
    
    /** Creates a new instance of ValidateSupport.
     * @param   valConfig   Validate configuration.
     */
    public ValidateSupport(ValidateConfiguration valConfig) {
        super();
        mValConfig = valConfig;
    }
    
    /** Gets the validate configuration currently used.
     * @return  Validate configuration currently used.
     */
    public ValidateConfiguration getValidateConfiguration() {
        return mValConfig;
    }
    
    /** Sets the validate configuration currently used.
     * @param   valConfig   Validate configuration to use.
     */
    public void setValidateConfiguration(ValidateConfiguration valConfig) {
        mValConfig = valConfig;
    }
    
    public void setResultItems(Collection<ResultItem> resultItems) {
        mResultItems = resultItems;
    }
    
    public void setValidator(Validator validator) {
        mValidator = validator;
    }
    
    /** Tests if an attribute value is absent.
     * @param   value   Value of attribute.
     * @return  <code>true</code> if value is absent.
     */
    public static boolean isAttributeAbsent(String value) {
        return ((null == value) || (value.trim().length() == 0));
    }
    
    /** Fires to-do events to listeners.
     * @param   toDoEvent   To-do event to fire.
     * @return  <code>true</code> if more events can be accepted by the listener;
     *          <code>false</code> otherwise.
     */
    public boolean fireToDo(Validator.ResultType type, Component component,  
            String desc, String correction) {
        String message = desc;
        if (correction != null) {
            message = desc + " : " + correction;
        }
        ResultItem item = new Validator.ResultItem(mValidator, 
                type, component, message);
        mResultItems.add(item);
        return true;
    }
}
