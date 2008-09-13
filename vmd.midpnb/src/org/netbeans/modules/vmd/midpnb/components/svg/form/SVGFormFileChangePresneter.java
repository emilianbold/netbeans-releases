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
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import javax.microedition.m2g.SVGImage;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;

/**
 *
 * @author Karol Harezlak
 */
public class SVGFormFileChangePresneter extends DynamicPresenter implements DesignDocumentAwareness {

    private WeakReference<FileObject> svgFileObject;
    private SVGFormFileObjectListener imageFileListener;
    private boolean isInit;
    private DataObjectContext context;

    @Override
    protected void notifyAttached(DesignComponent component) {
    }

    @Override
    protected void notifyDetached(DesignComponent component) {
        removeListener();
    }

    private void init() {
        DesignComponent svgImageComponent = getComponent().readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
        if (svgImageComponent == null) {
            return;
        }
        PropertyValue propertyValue = svgImageComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
        if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
            Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(getComponent().getDocument(), MidpTypes.getString(propertyValue));
            Iterator<FileObject> iterator = images.keySet().iterator();
            final FileObject svgFileObject_ = iterator.hasNext() ? iterator.next() : null;
            if (svgFileObject_ != null) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        SVGFormFileObjectListener.regenerateSVGComponentsStructure(svgFileObject_, getComponent(), DataEditorView.Kind.CODE);
                        context = ProjectUtils.getDataObjectContextForDocument(getComponent().getDocument());
                        context.addDesignDocumentAwareness(SVGFormFileChangePresneter.this);
                    }
                });
            }
        }

    }

    @Override
    protected DesignEventFilter getEventFilter() {
        return new DesignEventFilter().setGlobal(true);
    }

    @Override
    protected void designChanged(DesignEvent event) {
        DesignComponent svgImageComponent = getComponent().readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
        if (event.isComponentPropertyChanged(svgImageComponent, SVGImageCD.PROP_RESOURCE_PATH) || event.isComponentPropertyChanged(getComponent(), SVGFormCD.PROP_SVG_IMAGE)) {
            checkSVGComponent();
            if (!isInit) {
                isInit = true;
                init();
            }
        }
    }

    private void checkSVGComponent() {
        FileObject svgFileObject_ = null;
        if (svgFileObject != null && svgFileObject.get() != null) {
            svgFileObject_ = svgFileObject.get();
        }
        if (svgFileObject_ != null) {
            svgFileObject_.removeFileChangeListener(imageFileListener);
            imageFileListener = null;
        }
        DesignComponent svgImageComponent = getComponent().readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
        if (svgImageComponent == null) {
            return;
        }
        FileObject svgFileObjectNew = SVGFormSupport.getSVGFile(svgImageComponent);
        if (svgFileObjectNew != null) {
            try {
                //Check if this file is SVG file
                SVGImage svgImage = Util.createSVGImage(svgFileObjectNew, true);
                if (svgImage != null) {
                    imageFileListener = new SVGFormFileObjectListener(getComponent(), svgImageComponent, SVGImageCD.PROP_RESOURCE_PATH);
                    svgFileObjectNew.addFileChangeListener(imageFileListener);
                    ActiveViewSupport.getDefault().addActiveViewListener(imageFileListener);
                    svgFileObject = new WeakReference<FileObject>(svgFileObjectNew);
                }
            } catch (IOException e) {
                Debug.warning(e);
            }
        }

    }

    @Override
    protected void presenterChanged(PresenterEvent event) {
    }

    public void setDesignDocument(DesignDocument designDocument) {
        if (designDocument != null) {
            return;
        }
        if (context != null) {
            context.removeDesignDocumentAwareness(this);
        }
        removeListener();
    }

    private void removeListener() {
        if (svgFileObject == null || svgFileObject.get() == null) {
            return;
        }
        final FileObject svgFileObject_ = svgFileObject.get();
        if (svgFileObject_ != null) {
            svgFileObject_.removeFileChangeListener(imageFileListener);
        }
        ActiveViewSupport.getDefault().removeActiveViewListener(imageFileListener);
        imageFileListener = null;
        svgFileObject = null;
    }

    
}
