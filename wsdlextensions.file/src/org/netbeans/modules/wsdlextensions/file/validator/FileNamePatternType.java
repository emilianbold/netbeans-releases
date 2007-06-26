/*
 * FileNamePatternType.java
 * 
 * Created on Jun 21, 2007, 1:51:17 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.file.validator;

/**
 * added for cleaner file name pattern validation
 * @author jfu
 */
public enum FileNamePatternType {
    NAME_WITH_TIMESTAMP,
    NAME_WITH_UUID,
    NAME_WITH_SEQ,
    NAME_WITH_PERSISTED_SEQ,
    NAME_INVALID;
}
