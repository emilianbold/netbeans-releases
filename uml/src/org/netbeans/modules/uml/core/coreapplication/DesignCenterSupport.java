/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * Created on Mar 4, 2004
 *
 */
package org.netbeans.modules.uml.core.coreapplication;

/**
 * @author sumitabhk
 *
 */
public class DesignCenterSupport implements IDesignCenterSupport{

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#initialize(java.lang.Object)
	 */
	public long initialize(Object context) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#deInitialize(java.lang.Object)
	 */
	public long deInitialize(Object context) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#unLoad(java.lang.Object)
	 */
	public long unLoad(Object context) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getVersion()
	 */
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getID()
	 */
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getLocation()
	 */
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}
    
    ////////////////////////////////////////////////////////////////////////////
   // IDesignCenterSupport Methods
   
   /** save the design center addin */
    public void save()
    {
        // There is nothing to save.
    }

}



