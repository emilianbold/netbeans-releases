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
package org.netbeans.modules.xml.text.syntax;

import java.util.MissingResourceException;

import org.openide.util.HelpCtx;

import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.LocaleSupport.Localizer;

import org.netbeans.modules.xml.text.indent.XMLIndentEngine;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
abstract class AbstractBaseOptions extends BaseOptions implements Localizer {

    private static final long serialVersionUID =-1042044316100452977L;

    //
    // init
    //

    /** */
    public AbstractBaseOptions (Class kitClass, String typeName) {
        super (kitClass, typeName);
        LocaleSupport.addLocalizer (this);
    }


    //
    // BaseOptions
    //

    /**
     */
    


    public HelpCtx getHelpCtx() {
        return new HelpCtx (this.getClass());
    }

    //
    // Localizer
    //

    /**
     */
    public String getString (String s) {
        try {
            return Util.THIS.getString (s);
        } catch (MissingResourceException e) {
            return super.getString (s);
        }
    }

}
