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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.dlight.annotationsupport;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;

/**
 * Collection of utility methods for Profiler Annotation systems implementors.
 * 
 * @author thp
 */
public final class AnnotationSupport {
    private PropertyChangeSupport pcs = null;
    private static AnnotationSupport annotationSupport = null;
    
    public static final String PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE = "textAnnotationsVisible"; // NOI18N
    
    private AnnotationSupport() {
        pcs = new PropertyChangeSupport(this);
    }

    public static AnnotationSupport getInstance() {
        if (annotationSupport == null) {
            annotationSupport = new AnnotationSupport();
        }
        return annotationSupport;
    }
    
    /**
     * Common settings and preferences for versioning modules are set in this preferences node.  
     * 
     * @return Preferences node for Versioning modules
     * @see #PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE
     */
    private Preferences getPreferences() {
        return NbPreferences.forModule(AnnotationSupport.class);
    }

    public boolean getTextAnnotationVisible() {
        return getPreferences().getBoolean(AnnotationSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true);
    }

    public void setTextAnnotationVisible(boolean val) {
        boolean oldVal = getTextAnnotationVisible();
        if (oldVal != val) {
            getPreferences().putBoolean(AnnotationSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, val);
            firePropertyChange(PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, oldVal, val);
        }
    }

    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private void firePropertyChange(String name, Object oldValue, Object newValue) {
        if (pcs != null) {
            pcs.firePropertyChange(name, oldValue, newValue);
        }
    }
}
