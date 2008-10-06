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

package org.netbeans.modules.vmd.palette;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Anton Chechel
 */
public class PaletteItemDataNode extends DataNode {

    private static Image errorBadge;
    private PaletteItemDataObject obj;
    private Lookup lookup;
    private boolean isValid = true;
    private boolean needCheck = true;
    static {
        errorBadge = ImageUtilities.loadImage("org/netbeans/modules/vmd/palette/resources/error-badge.gif"); // NOI18N
    }

    public PaletteItemDataNode(PaletteItemDataObject obj) {
        this(obj, new InstanceContent());
    }

    private PaletteItemDataNode(PaletteItemDataObject obj, InstanceContent ic) {
        super(obj, Children.LEAF, new AbstractLookup(ic));
        ic.add(obj);
        ic.add(this);
        this.obj = obj;
        lookup = Lookups.singleton(this);
    }

    @Override
    public String getDisplayName() {
        return obj.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return obj.getToolTip();
    }

    @Override
    public Image getIcon(int type) {
        if (needCheck) {
            PaletteMap.getInstance().checkValidity(getProjectType(), lookup);
        }

        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            String iconPath = obj.getIcon();
            Image icon = null;
            if (iconPath != null) {
                icon = ImageUtilities.loadImage(iconPath);
            }
            if (icon == null) {
                icon = super.getIcon(type);
            }
            if (!isValid) {
                icon = ImageUtilities.mergeImages(icon, errorBadge, errorBadge.getWidth(null), errorBadge.getHeight(null));
            }
            return icon;
        }

        String iconPath = obj.getBigIcon();
        Image icon = null;
        if (iconPath != null) {
            icon = ImageUtilities.loadImage(iconPath);
        }
        if (icon == null) {
            icon = super.getIcon(type);
        }
        if (!isValid) {
            icon = ImageUtilities.mergeImages(icon, errorBadge, errorBadge.getWidth(null), errorBadge.getHeight(null));
        }
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    String getProjectType() {
        return obj.getProjectType();
    }

    String getProducerID() {
        return obj.getProducerID();
    }

    void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    boolean isValid() {
        return isValid;
    }

    void setNeedCheck(boolean needCheck) {
        this.needCheck = needCheck;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        boolean canDestroy = false;

        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();
        if (document != null) {
            DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        
            if (context != null) {
                final ComponentProducer[] producer = new ComponentProducer[1];
                final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry(context.getProjectType(), context.getProjectID());
                registry.readAccess(new Runnable() {

                    public void run() {
                        List<ComponentProducer> producers = registry.getComponentProducers();
                        for (ComponentProducer p : producers) {
                            if (p.getProducerID().equals(obj.getProducerID())) {
                                producer[0] = p;
                                break;
                            }
                        }
                    }
                });
                
                if (producer[0] != null) {
                    canDestroy = PaletteKit.CUSTOM_CATEGORY_NAME.equals(producer[0].getPaletteDescriptor().getCategoryID());
                }
            }
        }
        
        return canDestroy;
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
        DesignDocument document = ActiveDocumentSupport.getDefault().getActiveDocument();

        if (document != null) {
            DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
            if (context != null) {
                final ComponentProducer[] producer = new ComponentProducer[1];
                final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry(context.getProjectType(), context.getProjectID());
                registry.readAccess(new Runnable() {

                    public void run() {
                        List<ComponentProducer> producers = registry.getComponentProducers();
                        for (ComponentProducer p : producers) {
                            if (p.getProducerID().equals(obj.getProducerID())) {
                                producer[0] = p;
                                break;
                            }
                        }
                    }
                });
                
                if (producer[0] != null) {
                    registry.removeComponentDescriptor(producer[0].getMainComponentTypeID());
                }
            }
        }
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }
}