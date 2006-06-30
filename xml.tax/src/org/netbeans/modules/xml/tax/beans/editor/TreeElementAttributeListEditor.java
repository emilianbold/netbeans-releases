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
package org.netbeans.modules.xml.tax.beans.editor;

import java.beans.PropertyEditorSupport;
import java.awt.Component;

/**
 * PropertyEditor of TreeElementAttributeList.class
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class TreeElementAttributeListEditor extends PropertyEditorSupport {

    //
    // init
    //

    /** Creates new TreeElementAttributeListEditor */
    public TreeElementAttributeListEditor () {
    }


    //
    // itself
    //

    /**
     */
    public void setAsText (String text) throws IllegalArgumentException {
      // can not be set as text
    }

    /**
     */
    public boolean supportsCustomEditor () {
        return true;
    }

    /**
     */
    public Component getCustomEditor () {
        TreeElementAttributeListCustomizer comp = new TreeElementAttributeListCustomizer();
        comp.setObject (getValue());

        return comp;
    }

    /**
     */
    public boolean isPaintable () {
      return false;
    }

    /**
     */
    public String getAsText () {
        return Util.THIS.getString ("NAME_pe_attributes");
    }

}
