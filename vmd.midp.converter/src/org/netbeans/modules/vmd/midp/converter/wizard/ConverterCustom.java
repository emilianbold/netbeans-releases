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
 */package org.netbeans.modules.vmd.midp.converter.wizard;

import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.*;
import org.netbeans.modules.vmd.midp.components.items.*;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.palette.wizard.ComponentInstaller;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.*;
import org.openide.util.Utilities;

import java.util.*;

/**
 * @author David Kaspar
 */
public class ConverterCustom {

    static void loadItemsToRegistry (final List<ConverterItem> items, DesignDocument document) {
        final DescriptorRegistry registry = document.getDescriptorRegistry ();
        final Collection<String> unresolved = new HashSet<String> ();
        registry.readAccess (new Runnable() {
            public void run () {
                for (ConverterItem item : items) {
                    if (! isClassComponent (item))
                        continue;
                    String string = item.getTypeID ();
                    if (registry.getComponentDescriptor (new TypeID (TypeID.Kind.COMPONENT, string)) == null)
                        unresolved.add (string);
                }
            }
        });
        if (! unresolved.isEmpty ()) {
            Map<String, ComponentInstaller.Item> found = ComponentInstaller.search (ProjectUtils.getProject (document));
            ArrayList<ComponentInstaller.Item> install = new ArrayList<ComponentInstaller.Item> ();
            for (String s : unresolved) {
                ComponentInstaller.Item item = found.get (s);
                if (item != null)
                    install.add (item);
            }
            ComponentInstaller.install (found, install);
        }
    }

    static boolean isClassComponent (ConverterItem item) {
        return Utilities.isJavaIdentifier (item.getID ())  &&  MidpTypes.isValidFQNClassName (item.getTypeID ());
    }

    // Created: YES, Adds: NO
    static void convertCustom (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        TypeID typeID = new TypeID (TypeID.Kind.COMPONENT, item.getTypeID ());
        ComponentProducer producer = DocumentSupport.getComponentProducer (document, typeID.toString ());
        if (producer == null)
            return;

        DesignComponent component = document.createComponent (producer.getMainComponentTypeID ());
        producer.postInitialize (document, component);
        convertCustomProperties (id2item, item, component, component.getComponentDescriptor ());
    }

    // Created: NO, Adds: NO
    private static void convertCustomProperties (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignComponent component, ComponentDescriptor descriptor) {
        if (descriptor == null)
            return;
        TypeID typeID = descriptor.getTypeDescriptor ().getThisType ();
        if (DisplayableCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertDisplayable (id2item, item, component);
        else if (CanvasCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertCanvas (id2item, item, component);
        else if (ScreenCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertScreen (id2item, item, component);
        else if (AlertCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertAlertCore (id2item, item, component);
        else if (FormCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertFormCore (id2item, item, component);
        else if (ListCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertListCore (id2item, item, component);
        else if (TextBoxCD.TYPEID.equals (typeID))
            ConverterDisplayables.convertTextBoxCore (id2item, item, component);

        else if (ItemCD.TYPEID.equals (typeID))
            ConverterItems.convertItem (id2item, item, component);
        else if (CustomItemCD.TYPEID.equals (typeID))
            ConverterItems.convertCustomItem (id2item, item, component);
        else if (DateFieldCD.TYPEID.equals (typeID))
            ConverterItems.convertDateFieldCore (id2item, item, component);
        else if (ChoiceGroupCD.TYPEID.equals (typeID))
            ConverterItems.convertChoiceGroupCore (id2item, item, component);
        else if (GaugeCD.TYPEID.equals (typeID))
            ConverterItems.convertGaugeCore (id2item, item, component);
        else if (ImageItemCD.TYPEID.equals (typeID))
            ConverterItems.convertImageItemCore (id2item, item, component);
        else if (SpacerCD.TYPEID.equals (typeID))
            ConverterItems.convertSpacerCore (id2item, item, component);
        else if (StringItemCD.TYPEID.equals (typeID))
            ConverterItems.convertStringItemCore (id2item, item, component);
        else if (TextFieldCD.TYPEID.equals (typeID))
            ConverterItems.convertTextFieldCore (id2item, item, component);

        else if (AbstractInfoScreenCD.TYPEID.equals (typeID))
            ConverterBuilt.convertAbstractInfoScreen (id2item, item, component);
        else if (SimpleCancellableTaskCD.TYPEID.equals (typeID))
            ConverterBuilt.convertSimpleCancellableTask (id2item, item, component.getDocument ());
        else if (SimpleTableModelCD.TYPEID.equals (typeID))
            ConverterBuilt.convertSimpleTableModel (id2item, item, component.getDocument ());
        else if (SplashScreenCD.TYPEID.equals (typeID))
            ConverterBuilt.convertSplashScreen (id2item, item, component.getDocument ());
        else if (TableItemCD.TYPEID.equals (typeID))
            ConverterBuilt.convertTableItem (id2item, item, component.getDocument ());
        else if (WaitScreenCD.TYPEID.equals (typeID))
            ConverterBuilt.convertWaitScreen (id2item, item, component.getDocument ());

        else if (CommandCD.TYPEID.equals (typeID))
            ConverterResources.convertCommand (item, component.getDocument ());
        else if (TickerCD.TYPEID.equals (typeID))
            ConverterResources.convertTicker (item, component.getDocument ());

        else if (SVGImageCD.TYPEID.equals (typeID))
            ConverterSVG.convertImage (id2item, item, component.getDocument ());
        else if (SVGAnimatorWrapperCD.TYPEID.equals (typeID))
            ConverterSVG.convertPlayer (id2item, item, component.getDocument ());
        else if (SVGMenuCD.TYPEID.equals (typeID))
            ConverterSVG.convertMenu (id2item, item, component.getDocument ());
        else if (SVGSplashScreenCD.TYPEID.equals (typeID))
            ConverterSVG.convertSplashScreen (id2item, item, component.getDocument ());
        else if (SVGWaitScreenCD.TYPEID.equals (typeID))
            ConverterSVG.convertWaitScreen (id2item, item, component.getDocument ());

        else {
            convertCustomProperties (id2item, item, component, descriptor.getSuperDescriptor ());

            String fqn = MidpTypes.getFQNClassName (typeID);
            for (PropertyDescriptor property : descriptor.getPropertyDescriptors ()) {
                String prefix = fqn + "#"; // NOI18N
                String name = property.getName ();
                if (! name.startsWith (prefix))
                    continue;
                name = name.substring (prefix.length ());
                int index = name.indexOf ('#'); // NOI18N
                if (index >= 0) { // NOI18N
                    prefix = "%%" + name.substring (0, index) + "_" + name.substring (index + 1) + "_"; // NOI18N
                    String found = null;
                    for (String s : item.getPropertyNames ()) {
                        if (s.startsWith (prefix)) {
                            found = s;
                            break;
                        }
                    }
                    if (found != null)
                        ConverterUtil.convertToPropertyValue (component, property.getName (), property.getType (), item.getPropertyValue (found));
                } else {
                    if (! name.startsWith ("set")) // NOI18N
                        continue;
                    name = name.substring ("set".length ()); // NOI18N
                    if (item.isPropertyValueSet ("%" + name)) // NOI18N
                        ConverterUtil.convertToPropertyValue (component, property.getName (), property.getType (), item.getPropertyValue ("%" + name)); // NOI18N
                    else if (name.length () > 0) {
                        name = Character.toLowerCase (name.charAt (0)) + name.substring (1);
                        if (item.isPropertyValueSet ("%" + name)) // NOI18N
                            ConverterUtil.convertToPropertyValue (component, property.getName (), property.getType (), item.getPropertyValue ("%" + name)); // NOI18N
                    }
                }
            }
        }
    }

}
