/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.SessionGenerator;

/**
 * @author pfiala
 */
public class SessionHelper extends EntityAndSessionHelper {

    public SessionHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, Session session) {
        super(ejbJarMultiViewDataObject, session);
        abstractMethodController = new SessionMethodController(session, sourceClassPath);
    }

    protected EntityAndSessionGenerator getGenerator() {
        return new SessionGenerator();
    }
}
