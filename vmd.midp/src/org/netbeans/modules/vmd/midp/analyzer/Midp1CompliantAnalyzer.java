/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */package org.netbeans.modules.vmd.midp.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;

/**
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.analyzer.Analyzer.class)
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

        document.getTransactionManager ().readAccess (new Runnable() {
            public void run () {
                DefaultListModel model = (DefaultListModel) list.getModel ();
                model.removeAllElements ();
                DesignComponent rootComponent = document.getRootComponent ();
                if (rootComponent != null)
                    analyze (model, rootComponent);
            }
        });
    }

    private void analyze (DefaultListModel list, DesignComponent component) {
        ComponentDescriptor descriptor = component.getComponentDescriptor ();
        if (descriptor == null)
            return;

        VersionDescriptor version = descriptor.getVersionDescriptor ();
        if (! version.isCompatibleWith (MidpVersionDescriptor.MIDP_1)) {
            reportComponent (list, component);
            return;
        }

        for (PropertyDescriptor property : descriptor.getPropertyDescriptors ()) {
            if (! property.getVersionable ().isCompatibleWith (MidpVersionable.MIDP_1))
                if (! component.isDefaultValue (property.getName ()))
                    reportComponentProperty (list, component, property.getName ());
            processComponentProperty (list, component, property);
        }

        for (DesignComponent child : component.getComponents ())
            analyze (list, child);
    }

    private void reportComponent (DefaultListModel list, DesignComponent component) {
        list.addElement (NbBundle.getMessage(Midp1CompliantAnalyzer.class, "MSG_IncompatibleComponent", InfoPresenter.getHtmlDisplayName (component))); // NOI18N
    }

    private void processComponentProperty (DefaultListModel list, DesignComponent component, PropertyDescriptor property) {
        DescriptorRegistry registry = component.getDocument ().getDescriptorRegistry ();
        if (registry.isInHierarchy (ItemCD.TYPEID, component.getType ())  &&  ItemCD.PROP_LAYOUT.equals (property.getName ())) {
            if (! registry.isInHierarchy (ImageItemCD.TYPEID, component.getType ())) {
                list.addElement (NbBundle.getMessage(Midp1CompliantAnalyzer.class, "MSG_IncompatibleItemLayout", InfoPresenter.getHtmlDisplayName (component))); // NOI18N
                return;
            }
            int value = MidpTypes.getInteger (component.readProperty (ItemCD.PROP_LAYOUT));
            if ((value & (ItemCD.VALUE_LAYOUT_TOP | ItemCD.VALUE_LAYOUT_BOTTOM | ItemCD.VALUE_LAYOUT_VCENTER | ItemCD.VALUE_LAYOUT_SHRINK | ItemCD.VALUE_LAYOUT_VSHRINK | ItemCD.VALUE_LAYOUT_VSHRINK | ItemCD.VALUE_LAYOUT_VEXPAND | ItemCD.VALUE_LAYOUT_2)) != 0) {
                list.addElement (NbBundle.getMessage(Midp1CompliantAnalyzer.class, "MSG_IncompatibleItemLayout", InfoPresenter.getHtmlDisplayName (component))); // NOI18N
                return;
            }
        }
    }

    private void reportComponentProperty (DefaultListModel list, DesignComponent component, String propertyName) {
        DescriptorRegistry registry = component.getDocument ().getDescriptorRegistry ();
        if (registry.isInHierarchy (ItemCD.TYPEID, component.getType ())) {
            if (ItemCD.PROP_ITEM_COMMAND_LISTENER.equals (propertyName))
                return;
            if (ItemCD.PROP_COMMANDS.equals (propertyName)) {
                list.addElement (NbBundle.getMessage(Midp1CompliantAnalyzer.class, "MSG_ItemsCommandsNotAllowed", InfoPresenter.getHtmlDisplayName (component))); // NOI18N
                return;
            }
        }
        list.addElement (NbBundle.getMessage(Midp1CompliantAnalyzer.class, "MSG_IncompatiblePropertyValue", propertyName, InfoPresenter.getHtmlDisplayName (component))); // NOI18N
    }

}
