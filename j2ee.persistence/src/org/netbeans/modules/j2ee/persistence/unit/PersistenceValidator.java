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
 */

package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.xml.multiview.Error;

/**
 * Validator for persistence.xml.
 * 
 * @author Erno Mononen
 */
public class PersistenceValidator {
    
    private final PUDataObject puDataObject;
    private List<Error> errors = new ArrayList<Error>();
    
    /**
     * Creates a new instance of PersistenceValidator
     * @param puDataObject the PUDataObject whose model 
     * is to be validated.
     */
    public PersistenceValidator(PUDataObject puDataObject) {
        this.puDataObject = puDataObject;
    }
    
    /**
     * Validates the model.
     * @return list of errors or an empty list if there were no errors, never null. 
     */
    public List<Error> validate(){
        validateName();
        validateExcludeUnlisted();
        validateJarFiles();
        return errors;
    }
    
    /**
     * Validates that name is not empty and that it is unique.
     */
    private void validateName(){
        PersistenceUnit[] persistenceUnits = puDataObject.getPersistence().getPersistenceUnit();
        for (int i=0 ;i < persistenceUnits .length; i++) {
            String title = persistenceUnits[i].getName();
            if (isEmpty(title)) {
                Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "name");
                errors.add(new Error(Error.MISSING_VALUE_MESSAGE, "name", loc));
            }
            for (int j = 0; j < persistenceUnits.length; j++) {
                String tit = persistenceUnits[j].getName();
                if (!isEmpty(title) && i != j && title.equals(tit)) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "name");
                    errors.add(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, title, loc));
                }
            }
        }
    }
    
    /**
     * Validates that exclude-unlisted-classes is not used in Java SE environment.
     */
    private void validateExcludeUnlisted(){
        if (!isJavaSE()){
            return;
        }
        PersistenceUnit[] persistenceUnits = puDataObject.getPersistence().getPersistenceUnit();
        for (int i=0 ;i < persistenceUnits .length; i++) {
           if (persistenceUnits[i].isExcludeUnlistedClasses()){
                Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "exclude-unlisted-classes");
                errors.add(new Error(Error.TYPE_FATAL, Error.WARNING_MESSAGE, "exclude-unlisted-classes property is not supported in Java SE environments", loc));
            }
        }
    }
    
    /**
     * Validates that jar-files is not used in Java SE environment.
     */
    private void validateJarFiles(){
        if (!isJavaSE()){
            return;
        }
        PersistenceUnit[] persistenceUnits = puDataObject.getPersistence().getPersistenceUnit();
        for (int i=0 ;i < persistenceUnits .length; i++) {
            if (persistenceUnits[i].getJarFile() != null && persistenceUnits[i].getJarFile().length > 0){
                Error.ErrorLocation loc = new Error.ErrorLocation(persistenceUnits[i], "jar-files");
                errors.add(new Error(Error.TYPE_FATAL, Error.WARNING_MESSAGE, "jar-files property is not supported in Java SE environments", loc));
            }
        }
        
    }
    
    /**
     * @return true if the current environment is Java SE. 
     */
    protected boolean isJavaSE(){
        Project project = FileOwnerQuery.getOwner(puDataObject.getPrimaryFile());
        return Util.isJavaSE(project);
    }
    
    private boolean isEmpty(String str){
        return null == str || "".equals(str.trim());
    }
    
}
