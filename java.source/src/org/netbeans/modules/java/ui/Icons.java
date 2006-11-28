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

package org.netbeans.modules.java.ui;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hrebejk
 */
public final class Icons {

    private static final String ICON_BASE = "org/netbeans/modules/java/source/resources/icons/";
    private static final String GIF_EXTENSION = ".gif";
    private static final String PNG_EXTENSION = ".png";
    private static final String WAIT = ICON_BASE + "wait" + PNG_EXTENSION;
        
    /** Creates a new instance of Icons */
    private Icons() {
    }
    
    public static Icon getBusyIcon () {
        Image img = Utilities.loadImage (WAIT);
        if (img == null) {
            return null;
        }
        else {
            return new ImageIcon (img);
        }
    }
            
    
    public static Icon getElementIcon( ElementKind elementKind, Collection<Modifier> modifiers ) {
        
        if ( modifiers == null ) {
            modifiers = Collections.<Modifier>emptyList();
        }
        
        Image img = null;
	
	switch( elementKind ) {
	    case PACKAGE:
		img = Utilities.loadImage( ICON_BASE + "package" + GIF_EXTENSION );
		break;
	    case ENUM:	
		img = Utilities.loadImage( ICON_BASE + "enum" + PNG_EXTENSION );
		break;
	    case ANNOTATION_TYPE:
		img = Utilities.loadImage( ICON_BASE + "annotation" + PNG_EXTENSION );
		break;
	    case CLASS:	
		img = Utilities.loadImage( ICON_BASE + "class" + PNG_EXTENSION );
		break;
	    case INTERFACE:
		img = Utilities.loadImage( ICON_BASE + "interface"  + PNG_EXTENSION );
		break;
	    case FIELD:
		img = Utilities.loadImage( getIconName( ICON_BASE + "field", PNG_EXTENSION, modifiers ) );
		break;
	    case ENUM_CONSTANT: 
		img = Utilities.loadImage( ICON_BASE + "constant" + PNG_EXTENSION );
		break;
	    case CONSTRUCTOR:
		img = Utilities.loadImage( getIconName( ICON_BASE + "constructor", PNG_EXTENSION, modifiers ) );
		break;
	    case STATIC_INIT: 	
		img = Utilities.loadImage( getIconName( ICON_BASE + "initializer", PNG_EXTENSION, modifiers ) );      
		break;
	    case METHOD: 	
		img = Utilities.loadImage( getIconName( ICON_BASE + "method", PNG_EXTENSION, modifiers ) );      
		break;
	    default:	
	        img = null;
        }
	
	return img == null ? null : new ImageIcon (img);
        
    }
    
    // Private Methods ---------------------------------------------------------
           
    private static String getIconName( String typeName, String extension, Collection<Modifier> modifiers ) {
        
        StringBuffer fileName = new StringBuffer( typeName );
        
        if ( modifiers.contains( Modifier.STATIC ) ) {
            fileName.append( "Static" );
        }
        if ( modifiers.contains( Modifier.PUBLIC ) ) {
            return fileName.append( "Public" ).append( extension ).toString();
        }
        if ( modifiers.contains( Modifier.PROTECTED ) ) {
            return fileName.append( "Protected" ).append( extension ).toString();
        }
        if ( modifiers.contains( Modifier.PRIVATE ) ) {
            return fileName.append( "Private" ).append( extension ).toString();
        }
        return fileName.append( "Package" ).append( extension ).toString();
                        
    }
    
}
