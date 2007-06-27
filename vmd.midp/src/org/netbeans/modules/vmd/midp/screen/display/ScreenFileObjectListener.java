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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
