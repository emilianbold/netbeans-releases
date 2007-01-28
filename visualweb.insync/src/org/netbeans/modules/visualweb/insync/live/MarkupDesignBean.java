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

import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import java.beans.BeanInfo;

import org.w3c.dom.Element;

import com.sun.rave.designtime.DesignInfo;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MarkupDesignBean extends BeansDesignBean implements com.sun.rave.designtime.markup.MarkupDesignBean {

    /**
     * @param unit
     * @param beanInfo
     * @param liveBeanInfo
     * @param parent
     * @param instance
     * @param bean
     */
    public MarkupDesignBean(LiveUnit unit, BeanInfo beanInfo, DesignInfo liveBeanInfo, SourceDesignBean parent, Object instance, MarkupBean bean) {
        super(unit, beanInfo, liveBeanInfo, parent, instance, bean);
        Element element = bean.getElement();
//        if (element instanceof RaveElement)
//            ((RaveElement)element).setDesignBean(this);
        MarkupUnit.setMarkupDesignBeanForElement(element, this);
    }

    //------------------------------------------------------------------------------- MarkupDesignBean

    public Element getElement() {
        return getBean().getElement();
    }

}
