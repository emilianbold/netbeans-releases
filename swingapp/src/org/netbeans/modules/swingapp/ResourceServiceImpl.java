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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.swingapp;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.form.ResourcePanel;
import org.netbeans.modules.form.ResourceService;
import org.netbeans.modules.form.ResourceValue;
import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.LocalePanel;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;

/**
 * Implementation of the ResourceService. It takes care of all requests from the
 * form editor when it needs to manipulate with resources. Registered in
 * META-INF.services.
 * 
 * @author Tomas Pavek
 */
public class ResourceServiceImpl implements ResourceService {

    private static class ChangeInfo {
        private String key;
        private Object[] originalData; // if null, the key was just added
        private BundleStructure bundle;
    }

    public ResourceValue get(String key, Class type, String localeSuffix, FileObject srcFile) {
        DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(srcFile);
        resMap.setLocalization(localeSuffix);
        return resMap.getResourceValue(key, type);
    }

    public Collection<String> findKeys(String keyRegex, FileObject srcFile) {
        return ResourceUtils.getDesignResourceMap(srcFile).collectKeys(keyRegex, true);
    }

    public ResourceValue create(String key, Class type, Object value, String stringValue, FileObject srcFile) {
        return new ResourceValueImpl(key, type, value, null, stringValue,
                                     type == String.class,
                                     DesignResourceMap.CLASS_LEVEL,
                                     srcFile);
    }

    public ResourceValue changeKey(ResourceValue resource, String newKey) {
        ResourceValueImpl resValue = (ResourceValueImpl) resource;
        if (!ResourceValue.COMPUTE_AUTO_KEY.equals(resource.getKey())) {
            resValue = new ResourceValueImpl(resValue);
        } // otherwise: don't need new ResourceValue if the key has not been know yet
        resValue.setKey(newKey);
        return resValue;
    }

    public ResourceValue changeValue(ResourceValue resource, Object newValue, String newStringValue) {
        ResourceValueImpl resValue = (ResourceValueImpl) resource;
        return new ResourceValueImpl(resValue.getKey(), resValue.getValueType(),
                newValue, null, newStringValue, resValue.isInternationalized(),
                resValue.getStorageLevel(), resValue.getSourceFile());
    }

    public ResourceValue switchLocale(ResourceValue resource, String localeSuffix) {
        if (resource instanceof ResourceValueImpl) {
            ResourceValueImpl resValue = (ResourceValueImpl) resource;
            DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(resValue.getSourceFile());
            resMap.setLocalization(localeSuffix);
            return resMap.getResourceValue(resValue.getKey(), resValue.getValueType());
        }
        else if (resource instanceof ProxyAction) {
            ProxyAction action = (ProxyAction) resource;
            DesignResourceMap resMap = action.getResourceMap();
            resMap.setLocalization(localeSuffix);
            action = new ProxyAction(action);
            action.loadFromResourceMap();
            return action;
        }
        return resource;
    }

    public String[][] getAvailableLocales(FileObject srcFile) {
        Set<String> localeSet = new HashSet<String>();
        Map<String, MultiDataObject.Entry> entries = new HashMap<String, MultiDataObject.Entry>();
        for (MultiDataObject.Entry locEntry : ResourceUtils.getDesignResourceMap(srcFile).collectLocaleEntries()) {
            String locale = org.netbeans.modules.properties.Util.getLocaleSuffix(locEntry);
            if (!localeSet.contains(locale)) {
                localeSet.add(locale);
                entries.put(locale, locEntry);
            }
        }
        String[] locales = new String[localeSet.size()];
        localeSet.toArray(locales);
        Arrays.sort(locales);
        String[] displays = new String[locales.length];
        for (int i=0; i < locales.length; i++) {
            displays[i] = org.netbeans.modules.properties.Util.getLocaleLabel(entries.get(locales[i]));
        }
        return new String[][] { locales, displays };
    }

    public java.awt.Component getCreateLocaleComponent(final PropertyEditor prEd, FileObject srcFile) {
        DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(srcFile);
        String bundleName = resMap.getBundleNames().get(0);
        PropertiesDataObject dobj = resMap.getRepresentativeDataObject();
        if (dobj == null) {
            try {
                dobj = ResourceUtils.createPropertiesDataObject(srcFile, bundleName);
            }
            catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return null;
            }
        }
        final PropertiesDataObject propertiesDO = dobj;
        final Dialog[] dialog = new Dialog[1];
        final LocalePanel localePanel = new LocalePanel();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            localePanel,
            NbBundle.getBundle(PropertiesDataObject.class).getString("CTL_NewLocaleTitle"), // NOI18N
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        String locale = localePanel.getLocale().toString();
                        org.netbeans.modules.properties.Util.createLocaleFile(
                                propertiesDO, locale, false);
                        prEd.setValue("_" + locale); // NOI18N
                    }
                    dialog[0].setVisible(false);
                    dialog[0].dispose();
                }
            }
        );
        dialog[0] = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        return dialog[0];
    }

    public void update(ResourceValue oldValue, ResourceValue newValue,
                       FileObject srcFile, String localeSuffix)
        throws IOException
    {
        if ((oldValue != null && oldValue.getKey() == null)
                || (newValue != null && newValue.getKey() == null)) {
            return; // a complex resource value that is handled separately (action)
        }

        ResourceValueImpl oldRes = (ResourceValueImpl) oldValue;
        ResourceValueImpl newRes = (ResourceValueImpl) newValue;

        if (oldRes != null && (newRes == null || !oldRes.getKey().equals(newRes.getKey()))) {
            ResourceUtils.getDesignResourceMap(oldRes.getSourceFile()).removeResourceValue(oldRes);
        }

        if (newRes != null) {
            DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(newRes.getSourceFile());
            resMap.setLocalization(localeSuffix);
            resMap.addResourceValue(newRes); // this also reads the value back
        }
    }

    public void autoSave(FileObject srcFile) {
        ResourceUtils.getDesignResourceMap(srcFile).save();
    }

    public void close(FileObject srcFile) {
        DesignResourceMap resMap = ResourceUtils.unregisterDesignResourceMap(srcFile);
        if (resMap != null) {
            resMap.revertChanges();
        }
        AppFrameworkSupport.fileClosed(srcFile);
    }

    public boolean projectCanUseResources(FileObject fileInProject) {
        return AppFrameworkSupport.projectCanUseFramework(fileInProject);
    }

    public boolean projectUsesResources(FileObject fileInProject) {
        return AppFrameworkSupport.isFrameworkEnabledProject(fileInProject);
    }

    public boolean updateProjectForResources(FileObject fileInProject) {
        return AppFrameworkSupport.updateProjectClassPath(fileInProject);
    }

    public boolean isExcludedProperty(Class componentType, String propName) {
        return java.awt.Component.class.isAssignableFrom(componentType)
               && "name".equals(propName); // NOI18N
    }

    public String getInjectionCode(Object bean, String variableName, FileObject srcFile) {
        if (bean instanceof java.awt.Component) {
            java.awt.Component component = (java.awt.Component) bean;
            if (component.getParent() == null)
                return AppFrameworkSupport.getResourceMapCode(srcFile)
                        + ".injectComponents(" + variableName + ");"; // NOI18N
        }
        return null;
    }

    public ResourcePanel createResourcePanel(Class valueType, FileObject srcFile) {
        return new ResourcePanelImpl(ResourceUtils.getDesignResourceMap(srcFile), valueType);
    }
}
