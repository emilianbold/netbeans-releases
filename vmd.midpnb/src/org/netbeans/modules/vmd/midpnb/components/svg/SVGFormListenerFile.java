/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.vmd.midpnb.components.svg;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGComponentImageParser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Karol Harezlak
 */
class SVGFileListenerPresenter extends DynamicPresenter {

    private ActiveDocumentSupport.Listener listener;

    @Override
    protected void notifyAttached(DesignComponent component) {

        listener = new ActiveDocumentSupport.Listener() {

            public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
                if (activatedDocument == getComponent().getDocument()) {
                    getComponent().getDocument().getTransactionManager().writeAccess(new Runnable() {

                        public void run() {
                            FileObject fo = getFileObject(getComponent().getDocument());
                            if (fo == null) {
                                return;
                            }
                            SVGComponentImageParser parser = SVGComponentImageParser.getParserByComponent(getComponent());
                            try {
                                parser.parse(fo.getInputStream(), getComponent());
                            } catch (FileNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });

                }
            }

            public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
            }
        };
        ActiveDocumentSupport.getDefault().addActiveDocumentListener(listener);
    }

    @Override
    protected void notifyDetached(DesignComponent component) {
        ActiveDocumentSupport.getDefault().removeActiveDocumentListener(listener);
        listener = null;
    }

    @Override
    protected DesignEventFilter getEventFilter() {
        return null;
    }

    @Override
    protected void designChanged(DesignEvent event) {
    }

    @Override
    protected void presenterChanged(PresenterEvent event) {
    }

    private FileObject getFileObject(final DesignDocument document) {

        final FileObject[] svgImageFileObject = new FileObject[1];
        document.getTransactionManager().readAccess(new Runnable() {

            public void run() {
                DesignComponent svgImage = getComponent().readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
                if (svgImage == null) {
                    return;
                }
                PropertyValue propertyValue = svgImage.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                    Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(document, MidpTypes.getString(propertyValue));
                    Iterator<FileObject> iterator = images.keySet().iterator();
                    svgImageFileObject[0] = iterator.hasNext() ? iterator.next() : null;
                }
            }
        });
        return svgImageFileObject[0];
    }
}
