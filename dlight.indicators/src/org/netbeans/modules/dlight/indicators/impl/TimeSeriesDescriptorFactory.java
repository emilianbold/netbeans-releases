/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.indicators.impl;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mt154047
 */
public final class TimeSeriesDescriptorFactory {

    private TimeSeriesDescriptorFactory() {
    }

    /*package*/ static Collection<TimeSeriesDescriptor> createList(Map<?, ?> map) {
        Collection<TimeSeriesDescriptor> result = new ArrayList<TimeSeriesDescriptor>();
        FileObject rootFolder = FileUtil.getConfigRoot();
        String itemsPath = (String) map.get("items");//NOI18N
        FileObject itemsFolder = rootFolder.getFileObject(itemsPath);
        FileObject[] descriptors = itemsFolder.getChildren();
        for (FileObject fo : descriptors) {
            TimeSeriesDescriptor descr = TimeSeriesIndicatorConfigurationFactory.createInstance(fo, TimeSeriesDescriptor.class);
            if (descr != null) {
                result.add(descr);
            }
        }
        return result;
    }

    /*package*/ static TimeSeriesDescriptor createTimeSeriesDescriptor(Map<?, ?> map) {
        String name = getString(map, "name"); // NOI18N
        String displayName = getString(map, "displayName"); // NOI18N
        Color color = decodeColor(getString(map, "color")); // NOI18N
        TimeSeriesDescriptor.Kind kind = TimeSeriesDescriptor.Kind.valueOf(getString(map, "kind")); // NOI18N
        TimeSeriesDescriptor descriptor = new TimeSeriesDescriptor(name, displayName, color, kind);

        Column column = TimeSeriesIndicatorConfigurationFactory.createInstance(getString(map, "column"), Column.class); // NOI18N
        if (column != null) {
            descriptor.setSourceColumns(Collections.singleton(column));
        }

        return descriptor;
    }

    private static String getString(Map<?, ?> map, String key) {
        return (String) map.get(key);
    }

    private static Color decodeColor(String color) {
        if (color == null) {
            return null;
        }
        try {
            return Color.decode(color);
        } catch (NumberFormatException ex) {
        } catch (SecurityException ex) {
        }
        try {
            Field field = Color.class.getDeclaredField(color);
            if ((field.getModifiers() & Modifier.STATIC) != 0 && field.getType() == Color.class) {
                return (Color) field.get(null);
            }
        } catch (NoSuchFieldException ex) {
        } catch (IllegalAccessException ex) {
        }
        return Color.BLACK;
    }
}
