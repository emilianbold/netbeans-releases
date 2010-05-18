/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.editor.deprecated.pre61settings;

import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;

/**
 *
 * @author vita
 */
public abstract class OrgNbEditorAccessor {

    private static OrgNbEditorAccessor ACCESSOR = null;
    
    public static synchronized void register(OrgNbEditorAccessor accessor) {
        assert ACCESSOR == null : "Can't register two SPI package accessors!";
        ACCESSOR = accessor;
    }
    
    public static synchronized OrgNbEditorAccessor get() {
        // Trying to wake up Settings ...
        try {
            Class clazz = Class.forName(Settings.class.getName());
        } catch (ClassNotFoundException e) {
            // ignore
        }
        
        assert ACCESSOR != null : "There is no org.netbeans.editor package accessor available!";
        return ACCESSOR;
    }
    
    /** Creates a new instance of OrgNbEditorAccessor */
    protected OrgNbEditorAccessor() {
    }
    
    public abstract List [] Settings_getListsOfInitializers();
    public abstract void Settings_interceptSetValue(EditorPreferencesInjector interceptor);
    public abstract void Settings_addPropertyChangeListener(PropertyChangeListener l);
    public abstract void Settings_removePropertyChangeListener(PropertyChangeListener l);
    public abstract void Settings_addSettingsChangeListener(SettingsChangeListener l);
    public abstract void Settings_removeSettingsChangeListener(SettingsChangeListener l);
    public abstract boolean isResetValuesEvent();
    
}
