/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.io.*;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

/** A custom editor for Properties.
*
* @author  Ian Formanek
*/
public class PropertiesCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    private PropertiesEditor editor;

    private javax.swing.JScrollPane textAreaScroll;
    private javax.swing.JEditorPane editorPane;
    
    private static final long serialVersionUID =2473843831910895646L;
    /** Initializes the Form */
    public PropertiesCustomEditor(PropertiesEditor ed) {
        editor = ed;
        initComponents ();
        Properties props = (Properties) editor.getValue ();
        if (props == null) props = new Properties ();
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        try {
            props.store (baos, ""); // NOI18N
        } catch (IOException e) {
            // strange, strange -> ignore
        }
        editorPane.setText (baos.toString ());
        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(12, 12, 0, 11)));
        HelpCtx.setHelpIDString (this, PropertiesCustomEditor.class.getName ());
        
        editorPane.getAccessibleContext().setAccessibleName(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACS_PropertiesEditorPane"));
        editorPane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACSD_PropertiesEditorPane"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertiesCustomEditor.class).getString("ACSD_CustomPropertiesEditor"));
    }

    private boolean containsCommentedProps() {
        StringTokenizer tok = new StringTokenizer( editorPane.getText(), "\n", false); // NOI18N
        while ( tok.hasMoreTokens() ) {
            String row = tok.nextToken().trim();
            if ( row.startsWith( "#" ) && row.indexOf( '=' ) > 0 )  // NOI18N
                return true;
        }
        return false;
    }

    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {
        if ( containsCommentedProps() ) {
            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getBundle(PropertiesCustomEditor.class).getString("MSG_PropertiesComments")
                );
            if ( !TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
                throw new IllegalStateException();
            }
        }
        
        Properties props = new Properties ();
        try {
            props.load (new ByteArrayInputStream (editorPane.getText ().getBytes ()));
        } catch (IOException e) {
            // strange, strange -> ignore
        }
        return props;
    }
    
    /** Returns preferredSize as the preferred height and the width of the panel */
    public java.awt.Dimension getPreferredSize() {
        int screenWidth = org.openide.util.Utilities.getScreenSize().width;
        
        if (super.getPreferredSize().width >= screenWidth)
            return new java.awt.Dimension(screenWidth - 50, super.getPreferredSize().height);
        else
            return super.getPreferredSize();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        textAreaScroll = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        setLayout(new java.awt.BorderLayout());
        
        
        editorPane.setContentType("text/x-properties");
        textAreaScroll.setViewportView(editorPane);
        
        
        add(textAreaScroll, java.awt.BorderLayout.CENTER);
        
    }
}
