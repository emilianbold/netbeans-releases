/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import java.util.MissingResourceException;

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
    protected Class getDefaultIndentEngineClass () {
        return XMLIndentEngine.class;
    }


    //
    // Localizer
    //

    /**
     */
    public String getString (String s) {
        try {
            return Util.getString (s);
        } catch (MissingResourceException e) {
            return super.getString (s);
        }
    }

}
