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
/*
 * XMLCatalogCustomizerDialogDialogOperator.java
 *
 * Created on 11/13/03 4:25 PM
 */
package org.netbeans.jellytools.modules.xml.catalog;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/** Class implementing all necessary methods for handling "Customizer Dialog" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class XMLCatalogCustomizerDialogOperator extends NbDialogOperator {

    /** Creates new XMLCatalogCustomizerDialogDialogOperator that can handle it.
     */
    public XMLCatalogCustomizerDialogOperator() {
        super("Customizer Dialog");
    }

    private JLabelOperator _lblXMLCatalogURL;
    private JTextFieldOperator _txtXMLCatalogURL;
    private JTextAreaOperator _txtJTextArea;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "XML Catalog URL:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblXMLCatalogURL() {
        if (_lblXMLCatalogURL==null) {
            _lblXMLCatalogURL = new JLabelOperator(this, "XML Catalog URL:");
        }
        return _lblXMLCatalogURL;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtXMLCatalogURL() {
        if (_txtXMLCatalogURL==null) {
            _txtXMLCatalogURL = new JTextFieldOperator(this);
        }
        return _txtXMLCatalogURL;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtXMLCatalogURL
     * @return String text
     */
    public String getXMLCatalogURL() {
        return txtXMLCatalogURL().getText();
    }

    /** sets text for txtXMLCatalogURL
     * @param text String text
     */
    public void setXMLCatalogURL(String text) {
        txtXMLCatalogURL().setText(text);
    }

    /** types text for txtXMLCatalogURL
     * @param text String text
     */
    public void typeXMLCatalogURL(String text) {
        txtXMLCatalogURL().typeText(text);
    }

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }

    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of XMLCatalogCustomizerDialogDialogOperator by accessing all its components.
     */
    public void verify() {
        lblXMLCatalogURL();
        txtXMLCatalogURL();
        txtJTextArea();
    }

    /** Performs simple test of XMLCatalogCustomizerDialogDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new XMLCatalogCustomizerDialogOperator().verify();
        System.out.println("XMLCatalogCustomizerDialogDialogOperator verification finished.");
    }
}

