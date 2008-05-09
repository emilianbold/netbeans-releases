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
package org.netbeans.modules.vmd.midpnb.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Karol Harezlak
 */
public class EditSVGFileAction extends SystemAction {

    @Override
    public String getName() {
        return  NbBundle.getMessage(EditSVGFileAction.class, "NAME_EditSVGFile"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        FileObject svgFileObject = getSVGFileObject();
        if (svgFileObject == null) {
            return;
        }
        try {
            OpenCookie oc = DataObject.find(svgFileObject).getCookie(OpenCookie.class);
            if (oc == null) {
                return;
            }
            oc.open();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @Override
    public boolean isEnabled() {
        FileObject svgFileObject = getSVGFileObject();
        return svgFileObject != null;
    }
    
    
    
    private DesignComponent getActiveComponent() {
        Collection<DesignComponent> components = ActiveDocumentSupport.getDefault().getActiveComponents();
        if (components.size() == 1) {
            Iterator<DesignComponent> iterator = components.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }
        return null;
    }

    private FileObject getSVGFileObject() {
        final DesignComponent animatorComponent = getActiveComponent();
        if (animatorComponent == null) 
            return null;
        final FileObject[] svgFileWrapper = new FileObject[1];
        animatorComponent.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                PropertyValue value = animatorComponent.readProperty(SVGPlayerCD.PROP_SVG_IMAGE);
                if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
                    DesignComponent svgImageComponent = value.getComponent();
                    FileObject svgFileObject;
                    if (svgImageComponent != null) {
                        PropertyValue propertyValue = svgImageComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                        if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                            Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(animatorComponent.getDocument(), MidpTypes.getString(propertyValue));
                            Iterator<FileObject> iterator = images.keySet().iterator();
                            svgFileObject = iterator.hasNext() ? iterator.next() : null;
                            svgFileWrapper[0] = svgFileObject;
                        }
                    }
                }
            }
        });
        return svgFileWrapper[0];
    }
}
