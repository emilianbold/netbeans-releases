/**
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

package org.netbeans.modules.j2ee.persistence.unit;

import junit.framework.*;
import java.util.List;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.xml.multiview.Error;

/**
 * Tests for the <code>PersistenceValidator</code>.
 * @author Erno Mononen
 */
public class PersistenceValidatorTest extends PersistenceEditorTestBase {
    
    public PersistenceValidatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Tests that validator reports duplicate names as errors.
     */
    public void testValidateNameIsUnique() {
        PersistenceUnit unit1 = new PersistenceUnit();
        unit1.setName("name1");
        dataObject.addPersistenceUnit(unit1);
        PersistenceUnit unit2 = new PersistenceUnit();
        unit2.setName("name1");
        dataObject.addPersistenceUnit(unit2);
        PersistenceValidator validator = new PersistenceValidatorImpl(dataObject, false);
        List<Error> errors = validator.validate();
        assertEquals(2, errors.size());
        assertEquals(Error.DUPLICATE_VALUE_MESSAGE, errors.get(0).getErrorType());
        assertEquals(Error.DUPLICATE_VALUE_MESSAGE, errors.get(1).getErrorType());
    }

    
    /**
     * Tests that validator reports usage of exclude-unlisted-classes in 
     * Java SE environments as errors.
     */
    public void testValidateExcludeUnlistedClasses(){
        // Java SE
        PersistenceValidator javaSEvalidator = new PersistenceValidatorImpl(dataObject, true);
        PersistenceUnit unit1 = new PersistenceUnit();
        unit1.setName("unit1");
        unit1.setExcludeUnlistedClasses(true);
        dataObject.addPersistenceUnit(unit1);
        List<Error> errors = javaSEvalidator.validate();
        assertEquals(1, errors.size());
        assertEquals(Error.TYPE_WARNING, errors.get(0).getErrorType());
        // Java EE
        PersistenceValidator javaEEvalidator = new PersistenceValidatorImpl(dataObject, false);
        errors = javaEEvalidator.validate();
        assertTrue(errors.isEmpty());;
    }
    
    /**
     * Tests that validator reports usage of jar-files in 
     * Java SE environments as errors.
     */
    public void testValidateJarFiles(){
        // Java SE
        PersistenceValidator javaSEvalidator = new PersistenceValidatorImpl(dataObject, true);
        PersistenceUnit unit1 = new PersistenceUnit();
        unit1.setName("unit1");
        unit1.addJarFile("my-jar.jar");
        dataObject.addPersistenceUnit(unit1);
        List<Error> errors = javaSEvalidator.validate();
        assertEquals(1, errors.size());
        assertEquals(Error.TYPE_WARNING, errors.get(0).getErrorType());
        // Java EE
        PersistenceValidator javaEEvalidator = new PersistenceValidatorImpl(dataObject, false);
        errors = javaEEvalidator.validate();
        assertTrue(errors.isEmpty());;
    }
    
    /**
     * Implementation of PersistenceValidator that allows to be specified 
     * whether we're dealing with Java SE environment. 
     */ 
    private static class PersistenceValidatorImpl extends PersistenceValidator {
        
        private boolean javaSE;
        
        public PersistenceValidatorImpl(PUDataObject puDataObject, boolean javaSE){
            super(puDataObject);
            this.javaSE = javaSE;
        }

        protected boolean isJavaSE() {
            return javaSE;
        }
        
        
    }
}
