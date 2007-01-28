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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Created on Sep 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.visualweb.insync.live;

import java.beans.BeanInfo;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FacesDesignBean extends MarkupDesignBean implements com.sun.rave.designtime.faces.FacesDesignBean {

    /**
     * @param unit
     * @param beanInfo
     * @param liveBeanInfo
     * @param parent
     * @param instance
     * @param bean
     */
    public FacesDesignBean(LiveUnit unit, BeanInfo beanInfo, DesignInfo liveBeanInfo, SourceDesignBean parent, Object instance, FacesBean bean) {
        super(unit, beanInfo, liveBeanInfo, parent, instance, bean);
    }

    //-------------------------------------------------------------------------------- FacesDesignBean

    /* (non-Javadoc)
     * @see com.sun.rave.designtime.faces.FacesDesignBean#getFacet(java.lang.String)
     */
    public DesignBean getFacet(String facet) {
        DesignBean[] lbs = getChildBeans();
        for (int i = 0; i < lbs.length; i++) {
            if (lbs[i] instanceof BeansDesignBean) {
                BeansDesignBean jlb = (BeansDesignBean)lbs[i];
                String facetName = jlb.getFacetName();
                if (facetName != null && facetName.equals(facet))
                    return jlb;
            }
        }
        return null;
    }
}
