/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 */
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpArraySupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.screen.display.ScreenSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGFormImageParser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;

/**
 *
 * @author Karol Harezlak
 */
public class SVGFormFileObjectListener implements FileChangeListener, ActiveViewSupport.Listener {

    private WeakReference<DesignComponent> component;
    private WeakReference<DesignComponent> imageComponent;
    private String propertyName;
    private DataEditorView.Kind activatedView;

    public SVGFormFileObjectListener(DesignComponent component, DesignComponent imageComponent, String propertyName) {
        assert (component != null);
        assert (imageComponent != null);
        this.component = new WeakReference<DesignComponent>(component);
        this.imageComponent = new WeakReference<DesignComponent>(imageComponent);
        this.propertyName = propertyName;
    }

    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
    }

    public void fileChanged(FileEvent fe) {
        regenerateComponents(fe.getFile());
    }

    public void fileDeleted(FileEvent fe) {
        regenerateComponents(fe.getFile());
        fe.getFile().removeFileChangeListener(this);
        ActiveViewSupport.getDefault().removeActiveViewListener(this);
        activatedView = null;
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private void regenerateComponents(FileObject fe) {
        if (component == null || component.get() == null || component.get().getDocument() == null) {
            return;
        }
        final DesignComponent svgForm = component.get();
        regenerateSVGComponentsStructure(fe, svgForm, activatedView);
    }

    public static synchronized void regenerateSVGComponentsStructure(FileObject fo, final DesignComponent svgForm, final DataEditorView.Kind activatedView) {
        InputStream is = null;
        try {
            if (fo.isValid()) {
                is = fo.getInputStream();
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (fo == null && is == null) {
            return;
        }
        //Updating Screen Designer
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                svgForm.getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        ScreenDisplayPresenter sdp = svgForm.getPresenter(ScreenDisplayPresenter.class);
                        ScreenDeviceInfo di = ScreenSupport.getDeviceInfo(svgForm.getDocument());
                        sdp.reload(di);
                    }
                });
            }
        });
        String[][] idsArray = SVGFormImageParser.getComponentsInformation(is);

        final Map<String, String> exisitngIDs = new HashMap<String, String>();
        svgForm.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                DescriptorRegistry registry = svgForm.getDocument().getDescriptorRegistry();

                for (DesignComponent component : svgForm.getComponents()) {
                    if (registry.isInHierarchy(SVGComponentCD.TYPEID, component.getType())) {
                        String id = (String) component.readProperty(SVGComponentCD.PROP_ID).getPrimitiveValue();
                        exisitngIDs.put(id, component.getType().toString());
                    }
                }
            }
        });

        Map<String, String> changedIDs = new LinkedHashMap<String, String>();
        for (int i = 0; i < idsArray.length; i++) {
            changedIDs.put(idsArray[i][1], idsArray[i][0]);
        }

        final HashSet<String> toDelete = new HashSet<String>();
        for (String id : exisitngIDs.keySet()) {
            if (!changedIDs.containsKey(id)) {
                toDelete.add(id);
            }
        }
        final Map<String, String> toAdd = new LinkedHashMap<String, String>();
        for (String id : changedIDs.keySet()) {
            if (!exisitngIDs.containsKey(id)) {
                toAdd.put(id, changedIDs.get(id));
            }
        }
        if (!toAdd.isEmpty() || !toDelete.isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    svgForm.getDocument().getTransactionManager().writeAccess(new Runnable() {

                        public void run() {
                            DescriptorRegistry registry = svgForm.getDocument().getDescriptorRegistry();
                            Collection<DesignComponent> components = new HashSet<DesignComponent>(svgForm.getComponents());

                            for (DesignComponent component : components) {
                                if (registry.isInHierarchy(SVGComponentCD.TYPEID, component.getType())) {
                                    String id = (String) component.readProperty(SVGComponentCD.PROP_ID).getPrimitiveValue();
                                    if (toDelete.contains(id)) {
                                        ArraySupport.remove(svgForm, SVGFormCD.PROP_COMPONENTS, component);
                                        removeSVGButtonEventSource(component, svgForm);
                                        svgForm.getDocument().deleteComponent(component);
                                    }
                                }
                            }
                            addComponents(toAdd, svgForm);
                        }
                    });
                }
            });
            final DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(svgForm.getDocument());
            if (context != null && context.getDataObject() != null) {
                if (DataEditorView.Kind.CODE == activatedView) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            IOSupport.forceUpdateCode(context.getDataObject());
                        }
                    });

                }
            }
        }
    }

    private static void addComponents(Map<String, String> ids, DesignComponent svgForm) {
        for (String id : ids.keySet()) {
            String type = ids.get(id);
            if (MidpTypes.getSimpleClassName(SVGButtonCD.TYPEID).equals(type)) {
                SVGFormImageParser.SVGFormComponent srcSvgComponent = SVGFormImageParser.SVGFormComponent.createSVGButton(id, SVGButtonCD.TYPEID, new Float(10000));
                DesignComponent svgComponent = srcSvgComponent.createComponent(svgForm);
                svgForm.addComponent(svgComponent);
                MidpArraySupport.append(svgForm, SVGFormCD.PROP_COMPONENTS, svgComponent);
            } else if (MidpTypes.getSimpleClassName(SVGCheckBoxCD.TYPEID).equals(type)) {
                createComponent(id, SVGCheckBoxCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGComboBoxCD.TYPEID).equals(type)) {
                createComponent(id, SVGComboBoxCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGLabelCD.TYPEID).equals(type)) {
                createComponent(id, SVGLabelCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGListCD.TYPEID).equals(type)) {
                createComponent(id, SVGListCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGRadioButtonCD.TYPEID).equals(type)) {
                createComponent(id, SVGRadioButtonCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGSliderCD.TYPEID).equals(type)) {
                createComponent(id, SVGSliderCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGSpinnerCD.TYPEID).equals(type)) {
                createComponent(id, SVGSpinnerCD.TYPEID, svgForm);
            } else if (MidpTypes.getSimpleClassName(SVGTextFieldCD.TYPEID).equals(type)) {
                createComponent(id, SVGTextFieldCD.TYPEID, svgForm);
            }
        }
    }

    private static void createComponent(String id, TypeID type, DesignComponent svgForm) {
        SVGFormImageParser.SVGFormComponent srcSvgComponent = SVGFormImageParser.SVGFormComponent.create(id, type, new Float(10000));
        DesignComponent svgComponent = srcSvgComponent.createComponent(svgForm);
        svgForm.addComponent(svgComponent);
        MidpArraySupport.append(svgForm, SVGFormCD.PROP_COMPONENTS, svgComponent);
    }

    private static void removeSVGButtonEventSource(DesignComponent svgButton, DesignComponent svgForm) {
        if (svgButton.getType() != SVGButtonCD.TYPEID) {
            return;
        }
        Collection<DesignComponent> components = new HashSet(svgForm.getComponents());
        for (DesignComponent potentialButtonEventSource : components) {
            if (potentialButtonEventSource.getType() != SVGButtonEventSourceCD.TYPEID) {
                continue;
            }
            PropertyValue value = potentialButtonEventSource.readProperty(SVGButtonEventSourceCD.PROP_SVGBUTTON);
            if (value != null && value.getComponent() != null && value.getComponent() == svgButton) {
                potentialButtonEventSource.getDocument().deleteComponent(potentialButtonEventSource);
            }
        }
    }

    public void activeViewChanged(DataEditorView deactivatedView, DataEditorView activatedView) {
        if (activatedView != null) {
            this.activatedView = activatedView.getKind();
        }
    }
}
