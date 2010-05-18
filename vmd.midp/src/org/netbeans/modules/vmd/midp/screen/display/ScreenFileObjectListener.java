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
 */
package org.netbeans.modules.vmd.midp.screen.display;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfoPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Karol Harezlak
 */
public class ScreenFileObjectListener implements FileChangeListener {
    
    private WeakReference<DesignComponent> component;
    private WeakReference<DesignComponent> imageComponent;
    private String propertyName;
    
    public ScreenFileObjectListener(DesignComponent component, DesignComponent imageComponent, String propertyName) {
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
        changeResourcePath(fe.getFile());
        reload();
    }
    
    public void fileDeleted(FileEvent fe) {
        changeResourcePath(fe.getFile());
        reload();
        fe.getFile().removeFileChangeListener(this);
    }
    
    public void fileRenamed(FileRenameEvent fe) {
        changeResourcePath(fe.getFile());
        reload();
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
        changeResourcePath(fe.getFile());
        reload();
    }
    
    private ScreenDeviceInfo getScreenDeviceInfo() {
        if (component == null || component.get() == null)
            return null;
        DesignDocument document = component.get().getDocument();
        if (document == null)
            return null;
        ScreenDeviceInfoPresenter dip = document.getRootComponent().getPresenter(ScreenDeviceInfoPresenter.class);
        if (dip != null)
            return  dip.getScreenDeviceInfo();
        return null;
    }
    
    private void reload() {
        if (component == null || component.get() == null)
            return;
        final DesignDocument document = component.get().getDocument();
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                ScreenDeviceInfo deviceInfo = getScreenDeviceInfo();
                if (deviceInfo != null) {
                    ScreenDisplayPresenter presenter = component.get().getPresenter(ScreenDisplayPresenter.class);
                    presenter.reload(deviceInfo);
                }
            }
        });
    }
    
    private void changeResourcePath(FileObject fo) {
        if (component == null || component.get() == null || component.get().getDocument() == null)
            return;
        Map<FileObject,String> fileMap = MidpProjectSupport.getAllFilesForProjectByExt(component.get().getDocument(), Arrays.asList(fo.getExt()));
        final String path = fileMap.get(fo);
        if (component == null || component.get() == null)
            return;
        final DesignDocument document = component.get().getDocument();
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                if (propertyName != null && path != null)
                    imageComponent.get().writeProperty(propertyName, MidpTypes.createStringValue(path));
                else if (propertyName != null)
                    imageComponent.get().writeProperty(propertyName, PropertyValue.createNull());
            }
        });
    }
    
}
