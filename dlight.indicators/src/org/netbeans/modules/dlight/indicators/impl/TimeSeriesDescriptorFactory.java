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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor.Kind;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mt154047
 */
public final class TimeSeriesDescriptorFactory {

    static Collection<TimeSeriesDescriptor> createList(Map map) {
        Collection<TimeSeriesDescriptor> result = new ArrayList<TimeSeriesDescriptor>();
        FileObject rootFolder = FileUtil.getConfigRoot();
        String itemsPath = (String) map.get("items");//NOI18N
        FileObject itemsFolder = rootFolder.getFileObject(itemsPath);
        FileObject[] descriptors = itemsFolder.getChildren();
        for (FileObject fo : descriptors) {
            TimeSeriesDescriptor descr = (TimeSeriesDescriptor) fo.getAttribute("instanceCreate");//NOI18N
            if (descr != null) {
                result.add(descr);
            }
        }
        return result;
    }

    static TimeSeriesDescriptor create(Map map) {
        String colorString = getStringValue(map, "color");//NOI18N
        String[] rgb = colorString.split(":");//NOI18N
        Color color = Color.BLACK;
        if (rgb != null && rgb.length == 3){
            try{
                color = new Color( Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
        String displayName = getStringValue(map, "displayName");//NOI18N
        Kind kind = Kind.valueOf(getStringValue(map, "kind"));//NOI18N
        return  new TimeSeriesDescriptor(color, displayName, kind);
    }

    private static String getStringValue(Map map, String key) {
        return (String) map.get(key);
    }
}
