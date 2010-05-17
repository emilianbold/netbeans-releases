/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.PropertyEditorResourceLazyInit;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGWaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGComponentImageParser;
import org.netbeans.modules.vmd.midpnb.propertyeditors.table.TableModelEditorElement;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyEditorResourceLazyInitFactory {

    public static final DesignPropertyEditor createTaskPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SimpleCancellableTaskCD.TYPEID,
                NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), // NOI18N
                NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_NONE"), // NOI18N
                NbBundle.getMessage(WaitScreenCD.class, "LBL_CANCELLABLETASK_UCLABEL"), // NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new TaskEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createSVGTaskPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SimpleCancellableTaskCD.TYPEID,
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_NEW"), // NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_NONE"), // NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_CANCELLABLETASK_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new TaskEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createTableModelPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SimpleTableModelCD.TYPEID,
                NbBundle.getMessage(TableItemCD.class, "LBL_TABLEMODEL_NEW"), //NOI18N
                NbBundle.getMessage(TableItemCD.class, "LBL_TABLEMODEL_NONE"), //NOI18N
                NbBundle.getMessage(TableItemCD.class, "DISP_TableItem_TableModel_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new TableModelEditorElement();
            }
        };
    }

    public static final DesignPropertyEditor createSVGFormPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SVGImageCD.TYPEID,
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {

                return new SVGFormEditorElement();
            }

            @Override
            public void setAsText(final String text) {
                super.setAsText(text);
                final DesignComponent parentComponent = ActiveDocumentSupport.getDefault().getActiveComponents().iterator().next();
                final FileObject[] svgImageFileObject = new FileObject[1];
                final Boolean[] parseIt = new Boolean[1];
                parseIt[0] = Boolean.TRUE;
                parentComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        DesignComponent childComponent = getComponentsMap().get(text);
                        if (childComponent == null) {
                            return;
                        }
                        PropertyValue propertyValue = childComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                        if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                            //String svgImagePath = MidpTypes.getString(propertyValue);
                            Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(parentComponent.getDocument(), MidpTypes.getString(propertyValue));
                            Iterator<FileObject> iterator = images.keySet().iterator();
                            svgImageFileObject[0] = iterator.hasNext() ? iterator.next() : null;
                            parseIt[0] = Boolean.TRUE;
                        }
                        DesignComponent oldComponent = parentComponent.readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
                        if (oldComponent == childComponent && svgImageFileObject[0] != null) {
                            parseIt[0] = Boolean.FALSE;
                        }
                    }
                });
                nullValueSet(parentComponent);
                FileObject imageFO = svgImageFileObject[0];
                if (imageFO == null) {
                    return;
                }
                final SVGComponentImageParser[] svgComponentImageParser = new SVGComponentImageParser[1];
                parentComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        svgComponentImageParser[0] = SVGComponentImageParser.getParserByComponent(parentComponent);
                    }
                });

                SVGComponentImageParser parser = svgComponentImageParser[0];
                if (parser == null) {
                    return;
                }
                InputStream inputStream = null;
                try {
                    inputStream = imageFO.getInputStream();
                    if (inputStream != null) {
                        parser.parse(inputStream, parentComponent);
                    }
                } catch (FileNotFoundException ex) {
                    Debug.warning(ex);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ioe) {
                            Debug.warning(ioe);
                        }
                    }
                }
            }

            @Override
            public void customEditorOKButtonPressed() {
                super.customEditorOKButtonPressed();
                if (getValue() instanceof PropertyValue) {
                    PropertyValue propertyValue = (PropertyValue) getValue();
                    final DesignComponent component_ = propertyValue.getComponent();
                    if (component_ == null) {
                        return;
                    }
                    final String instanceNamme[] = new String[1];
                    component_.getDocument().getTransactionManager().readAccess(new Runnable() {

                        public void run() {
                            instanceNamme[0] = (String) component_.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
                        }
                    });
                    //setAsText(instanceNamme[0]);
                }

            }

            private void nullValueSet(final DesignComponent svgForm) {
                svgForm.getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        SVGFormSupport.removeAllSVGFormComponents(svgForm);
                        svgForm.resetToDefault(SVGFormCD.PROP_SVG_IMAGE);
                    }
                });
            }
        };
    }

    public static final DesignPropertyEditor createSVGImageEditorPropertyEditor() {
        return new PropertyEditorResourceLazyInit(SVGImageCD.TYPEID,
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NEW"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_NONE"), //NOI18N
                NbBundle.getMessage(SVGWaitScreenCD.class, "LBL_SVGIMAGE_UCLABEL"), //NOI18N
                false) {

            @Override
            protected PropertyEditorResourceElement createElement() {
                return new SVGImageEditorElement();
            }
        };
    }
}
