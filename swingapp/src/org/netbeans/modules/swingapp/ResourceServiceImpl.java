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

package org.netbeans.modules.swingapp;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.form.ResourcePanel;
import org.netbeans.modules.form.ResourceService;
import org.netbeans.modules.form.ResourceValue;
import org.netbeans.modules.properties.LocalePanel;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Implementation of the ResourceService. It takes care of all requests from the
 * form editor when it needs to manipulate with resources. Registered in
 * META-INF.services.
 * 
 * @author Tomas Pavek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.form.ResourceService.class)
public class ResourceServiceImpl implements ResourceService {

    public void prepareNew(FileObject srcFile) {
        // just make sure the resources folder exist
        try {
            ResourceUtils.createResourcesFolder(srcFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public ResourceValue get(String key, Class type, String localeSuffix, FileObject srcFile) {
        DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(srcFile, true);
        resMap.setLocalization(localeSuffix);
        return resMap.getResourceValue(key, type);
    }

    public Collection<String> findKeys(String keyRegex, FileObject srcFile) {
        return ResourceUtils.getDesignResourceMap(srcFile, true).collectKeys(keyRegex, true);
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
            DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(resValue.getSourceFile(), true);
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
        for (MultiDataObject.Entry locEntry : ResourceUtils.getDesignResourceMap(srcFile, true).collectLocaleEntries()) {
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
        DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(srcFile,true);
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

    private static boolean appframeworkUsedLogged = false;
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

        if (oldRes != null && (newRes == null || !oldRes.getKey().equals(newRes.getKey())
                || oldRes.getStorageLevel() != newRes.getStorageLevel())) {
            ResourceUtils.getDesignResourceMap(oldRes.getSourceFile(), true).removeResourceValue(oldRes);
        }

        if (newRes != null) {
            DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(newRes.getSourceFile(), true);
            resMap.setLocalization(localeSuffix);
            resMap.addResourceValue(newRes); // this also reads the value back
        }

        if (!appframeworkUsedLogged) {
            Logger logger = Logger.getLogger("org.netbeans.ui.metrics.swingapp"); // NOI18N
            LogRecord rec = new LogRecord(Level.INFO, "USG_FORM_APPFRAMEWORK_USED"); // NOI18N
            rec.setLoggerName(logger.getName());
            logger.log(rec);
            appframeworkUsedLogged = true;
        }
    }

    public void autoSave(FileObject srcFile) {
        DesignResourceMap resMap = ResourceUtils.getDesignResourceMap(srcFile, false);
        if (resMap != null) {
            resMap.save();
        }
    }

    public void close(FileObject srcFile) {
        DesignResourceMap resMap = ResourceUtils.unregisterDesignResourceMap(srcFile);
        if (resMap != null) {
            resMap.revertChanges();
        }
        AppFrameworkSupport.fileClosed(srcFile);
    }

    /**
     * @return true if app framework library is on classpath and the project is
     *         an application (i.e. executable project with Application subclass)
     */
    public boolean projectWantsUseResources(FileObject fileInProject) {
        return AppFrameworkSupport.isFrameworkEnabledProject(fileInProject);
    }

    /**
     * @return true if app framework library is on classpath of the project
     *         (the project can also be a library, not only an application)
     */
    public boolean projectUsesResources(FileObject fileInProject) {
        return AppFrameworkSupport.isFrameworkLibAvailable(fileInProject);
    }

//    public boolean updateProjectForResources(FileObject fileInProject) {
//        return AppFrameworkSupport.updateProjectClassPath(fileInProject);
//    }

    public boolean isExcludedProperty(Class componentType, String propName) {
        return java.awt.Component.class.isAssignableFrom(componentType)
               && "name".equals(propName); // NOI18N
    }

    public String getInjectionCode(Object bean, String variableName, FileObject srcFile) {
        if (bean instanceof java.awt.Component) {
            java.awt.Component component = (java.awt.Component) bean;
            if (component.getParent() == null)
                return ResourceUtils.getResourceMapCode(srcFile) + ResourceUtils.CODE_MARK_END
                        + ".injectComponents(" + variableName + ");"; // NOI18N
        }
        return null;
    }

    public ResourcePanel createResourcePanel(Class valueType, FileObject srcFile) {
        return new ResourcePanelImpl(ResourceUtils.getDesignResourceMap(srcFile, true), valueType);
    }

    public List<URL> getResourceFiles(FileObject srcFile) {
        PropertiesDataObject dobj = ResourceUtils.getPropertiesDataObject(srcFile);
        if (dobj != null) {
            try {
                List<URL> list = new ArrayList<URL>();
                list.add(dobj.getPrimaryEntry().getFile().getURL());
                for (MultiDataObject.Entry e : dobj.secondaryEntries()) {
                    list.add(e.getFile().getURL());
                }
                return list;
            } catch (IOException ex) {
                Logger.getLogger(ResourceServiceImpl.class.getName()).log(Level.INFO, null, ex); // NOI18N
            }
        }
        return Collections.emptyList();
    }
}
