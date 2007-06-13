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
 *
 */

package org.netbeans.modules.vmd.midp.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class Midp1CompliantAnalyzer implements Analyzer {

    public String getProjectType () {
        return MidpDocumentSupport.PROJECT_TYPE_MIDP;
    }

    public String getDisplayName () {
        return NbBundle.getMessage(Midp1CompliantAnalyzer.class, "Midp1CompliantAnalyzer.displayName"); // NOI18N
    }

    public String getToolTip () {
        return NbBundle.getMessage(Midp1CompliantAnalyzer.class, "Midp1CompliantAnalyzer.toolTip"); // NOI18N
    }

    public Image getIcon () {
        return null;
    }

    public JComponent createVisualRepresentation () {
        JList list = new JList (new DefaultListModel ());
        JScrollPane scrollPane = new JScrollPane (list);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 150));
        return scrollPane;
    }

    public void update (JComponent visualRepresentation, final DesignDocument document) {
        if (visualRepresentation == null  ||  document == null)
            return;
        final JList list = (JList) ((JScrollPane) visualRepresentation).getViewport ().getView ();
        list.removeAll ();

        document.getTransactionManager ().readAccess (new Runnable() {
            public void run () {
                analyze ((DefaultListModel) list.getModel (), document.getRootComponent ());
            }
        });
    }

    private void analyze (DefaultListModel list, DesignComponent component) {
        ComponentDescriptor descriptor = component.getComponentDescriptor ();
        if (descriptor == null)
            return;

        VersionDescriptor version = descriptor.getVersionDescriptor ();
        if (! version.isCompatibleWith (MidpVersionDescriptor.MIDP))
            list.addElement ("<html>Incompatible component: " + InfoPresenter.getHtmlDisplayName (component));

        for (PropertyDescriptor property : descriptor.getPropertyDescriptors ())
            if (! property.getVersionable ().isCompatibleWith (MidpVersionable.MIDP_1))
                if (! component.isDefaultValue (property.getName ()))
                    list.addElement ("<html>Incompatible property value set for: " + property.getName () + " in " + InfoPresenter.getHtmlDisplayName (component));

        for (DesignComponent child : component.getComponents ())
            analyze (list, child);
    }

}
