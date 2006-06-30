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
package org.netbeans.modules.xml.text.completion;

import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * @author  Sandeep Singh Randhawa
 * @version 0.1
 */
final class IconStore extends Object {

    public static final String EMPTY_TAG = "/org/netbeans/modules/xml/text/completion/resources/emptyTag";
    public static final String END_TAG = "/org/netbeans/modules/xml/text/completion/resources/endTag";
    public static final String CHILDREN = "/org/netbeans/modules/xml/text/completion/resources/typeChildren";
    public static final String MIXED = "/org/netbeans/modules/xml/text/completion/resources/typeMixed";
    public static final String PCDATA = "/org/netbeans/modules/xml/text/completion/resources/typePCDATA";
    
    public static final String TYPE_ENTITY = "/org/netbeans/modules/xml/text/completion/resources/attTypeENTITY";
    public static final String TYPE_ENTITIES = "/org/netbeans/modules/xml/text/completion/resources/attTypeENTITIES";    
    public static final String TYPE_ENUMERATION = "/org/netbeans/modules/xml/text/completion/resources/attTypeEn";
    public static final String TYPE_ID = "/org/netbeans/modules/xml/text/completion/resources/attTypeID";
    public static final String TYPE_IDREF = "/org/netbeans/modules/xml/text/completion/resources/attTypeIDREF";
    public static final String TYPE_IDREFS = "/org/netbeans/modules/xml/text/completion/resources/attTypeIDREFS";
    public static final String TYPE_NMTOKEN = "/org/netbeans/modules/xml/text/completion/resources/attTypeNMTOKEN";
    public static final String TYPE_NMTOKENS = "/org/netbeans/modules/xml/text/completion/resources/attTypeNMTOKENS";
    public static final String TYPE_NOTATION = "/org/netbeans/modules/xml/text/completion/resources/attTypeNOTATION";
    public static final String TYPE_CDATA = "/org/netbeans/modules/xml/text/completion/resources/typeCDATA";
        
    public static final String SPACER_16 = "/org/netbeans/modules/xml/text/completion/resources/spacer_16";
    public static final String SPACER_8 = "/org/netbeans/modules/xml/text/completion/resources/spacer_8";
    public static final String ICON_SUFFIX = ".gif";

    /** HashMap{@link java.util.HashMap } that acts as a store for the icons.
     */    
    private static HashMap iconsMap = new HashMap();
    
    /** Main method to retrieve the ImageIcon{@link javax.swing.ImageIcon}
     * @param name Name of the icon to retrieve. In most instances would be one of the variables of
     * this class.
     * @return ImageIcon{@link javax.swing.ImageIcon}
     */    
    
    public static ImageIcon getImageIcon(String name){
      if(name == null)
        name = SPACER_16;
      
        if(iconsMap.containsKey(name))
            return (ImageIcon)iconsMap.get(name);
        else{
            iconsMap.put(name, new ImageIcon(IconStore.class.getResource(name + ICON_SUFFIX)));
            return (ImageIcon)iconsMap.get(name);
        }
    }
}
