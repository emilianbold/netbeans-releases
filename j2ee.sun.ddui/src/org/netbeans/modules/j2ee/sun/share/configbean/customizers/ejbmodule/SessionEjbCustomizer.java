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
/*
 * SessionEjbCustomizer        October 20, 2003, 11:49 PM
 *
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;

import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.SessionEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public abstract class SessionEjbCustomizer extends EjbCustomizer
            implements TableModelListener {

    /** Creates a new instance of SessionEjbCustomizer */
	public SessionEjbCustomizer() {
	}
	

    protected void addTabbedBeanPanels() {
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        if(!(theBean instanceof SessionEjb)){
            assert(false);
        }
        SessionEjb sessionEjb = (SessionEjb)theBean;
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Session Ejb field Validations

        return errors;
    }
}
