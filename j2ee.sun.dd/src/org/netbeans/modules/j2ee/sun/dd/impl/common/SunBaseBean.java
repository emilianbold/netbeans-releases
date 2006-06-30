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
package org.netbeans.modules.j2ee.sun.dd.impl.common;

import java.util.Vector;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Version;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;

/**
 *
 * @author  Rajeshwar Patil
 */
public abstract class SunBaseBean extends BaseBean implements CommonDDBean {

	/** Creates a new instance of SunBaseBean
	 */
//	public SunBaseBean() {
//	}
	
	public SunBaseBean(Vector comps, Version version) {
		super(comps, version);
	}
	
	/* Dump the content of this bean returning it as a String
	 */
	public void dump(StringBuffer str, String indent){
	}
        
    public CommonDDBean getPropertyParent(String name){
        if(this.graphManager() != null)
            return (CommonDDBean) this.graphManager().getPropertyParent(name);
        else
            return null;
    }

    public void write(java.io.Writer w) throws java.io.IOException, org.netbeans.modules.j2ee.sun.dd.api.DDException {
        try {
            super.write(w);
        } catch(org.netbeans.modules.schema2beans.Schema2BeansException ex) {
            // !PW FIXME We should do a proper wrapped exception here, but there are 
            // difficulties overriding this method if DDException is not derived directly
            // from Schema2BeanException (due to method signature mismatch.)
            DDException ddEx = new DDException(ex.getMessage());
            ddEx.setStackTrace(ex.getStackTrace());
            throw ddEx;
        }
    }

    public void merge(CommonDDBean root, int mode) {
        // !PW Ugly casts to get Java to invoke merge(BaseBean, int) on BaseBean base class.
        ((BaseBean) this).merge((BaseBean) root, mode);
    }
}
