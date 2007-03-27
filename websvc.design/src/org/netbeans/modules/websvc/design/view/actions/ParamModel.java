/*
 * ParamModel.java
 *
 * Created on March 23, 2007, 3:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.actions;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;

/**
 *
 * @author mkuchtiak
 */
public class ParamModel {
        private String paramName;
        private ReferenceableSchemaComponent paramType;
        
        ParamModel(String paramName) {
            this.paramName=paramName;
        }
        
        ParamModel() {
        }
        
        public void setParamName(String paramName) {
            this.paramName = paramName;
        }        

        public String getParamName() {
            return paramName;
        }
        
        public void setParamType(ReferenceableSchemaComponent paramType) {
            this.paramType = paramType;
        }
        
        public ReferenceableSchemaComponent getParamType() {
            return paramType;
        }
        
        public String getDisplayName() {
            return Utils.getDisplayName(paramType);
        }
        

    
}
