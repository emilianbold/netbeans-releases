/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.util;

import java.io.CharConversionException;
import javax.swing.SwingUtilities;

import org.openide.xml.XMLUtil;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

import org.netbeans.tax.*;

/**
 *
 * @author Libor Kramolis
 */
public final class TAXUtil {
    
    /**
     * Try to set new value to the attribute. Method <code>XMLUtil.toAttributeValue</code> is used
     * to convert value to correct attribute value.
     *
     * @see org.openide.xml.XMLUtil#toAttributeValue
     */
    public static void setAttributeValue (TreeAttribute attribute, String value) throws TreeException {
        try {
            attribute.setValue (XMLUtil.toAttributeValue (value));
        } catch (CharConversionException exc) {
            throw new TreeException (exc);
        }
    }
    
    /**
     * Try to set new value to the text. Method <code>XMLUtil.toElementContent</code> is used
     * to convert value to correct element content.
     *
     * @see org.openide.xml.XMLUtil#toElementContent
     */
    public static void setTextData (TreeText text, String value) throws TreeException {
        try {
            text.setData (XMLUtil.toElementContent (value));
        } catch (CharConversionException exc) {
            throw new TreeException (exc);
        }
    }
    


    /**
     */
    public static void notifyWarning (final String message) {
        SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    NotifyDescriptor nd = new NotifyDescriptor.Message
                        (message, NotifyDescriptor.WARNING_MESSAGE);
                    TopManager.getDefault ().notify (nd);
                }
            });
    }

    /**
     */
    public static String printableValue (String value) {
        if (value == null)
            return new String ("<null>"); // NOI18N
        
        int ch;
        int MAX_LENGTH = 33;
        int len = Math.min (value.length (), MAX_LENGTH);
        
        StringBuffer sb = new StringBuffer (2 * len);
        for (int i = 0; i < len; i++) {
            ch = value.charAt (i);
            if ('\r' == ch) {
                sb.append ("\\r"); // NOI18N
            } else if ('\n' == ch) {
                sb.append ("\\n"); // NOI18N
            } else if ('\t' == ch) {
                sb.append ("\\t"); // NOI18N
            } else if ('\b' == ch) {
                sb.append ("\\b"); // NOI18N
            } else if ('\f' == ch) {
                sb.append ("\\f"); // NOI18N
            } else {
                sb.append ((char)ch);
            }
        }
        if (value.length () > len)
            sb.append ("..."); // NOI18N
        
        return sb.toString ();
    }

    /**
     */
    public static void notifyTreeException (TreeException exc) {
        notifyWarning (exc.getMessage());
    }

}
