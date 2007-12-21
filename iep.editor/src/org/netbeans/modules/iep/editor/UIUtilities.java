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

package org.netbeans.modules.iep.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.text.StyledDocument;

import org.netbeans.modules.iep.editor.validation.ValidationAnnotation;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.openide.DialogDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class UIUtilities {

    /** Creates a new instance of UIUtilities */
    private UIUtilities() {
    }

    /**
     * annotates the source view and if shoudShowSource is true, then jumps to the line in the source editor
     * 
     * @param dobj
     * @param wsdlComp the component with error
     * @param errorMessage the errormessage
     * @param shouldShowSource whether should jump to source editor
     */
    public static void annotateSourceView(PlanDataObject dobj, DocumentComponent wsdlComp, String errorMessage, boolean shouldShowSource) {
        LineCookie lc = dobj.getCookie(LineCookie.class);
        EditCookie ec = dobj.getCookie(EditCookie.class);
        if (lc == null || ec == null) {
            return;
        }
        ec.edit();
        ValidationAnnotation.clearAll();
        int lineNum = getLineNumber(wsdlComp);
        if (lineNum < 1) {
            return;
        }
        
        Line l = lc.getLineSet().getCurrent(lineNum);
        if (errorMessage != null) {
            ValidationAnnotation annotation = ValidationAnnotation.getNewInstance();
            annotation.setErrorMessage(errorMessage);
            annotation.attach( l );
            l.addPropertyChangeListener( annotation );
        }
        
        if (shouldShowSource) {
            l.show(Line.SHOW_GOTO);
        }
    }
    
    public static int getLineNumber(DocumentComponent comp) {
        int position = comp.findPosition();
        ModelSource modelSource = comp.getModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = lookup.lookup(StyledDocument.class);
        if (document == null) {
            return -1;
        }
        return NbDocument.findLineNumber(document,position);
    }
}
