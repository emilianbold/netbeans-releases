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

package org.netbeans.modules.editor.options;

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/** BeanInfo for AnnotationTypeOptions
 *
 * @author David Konecny
 * @since 07/2001
 */
public class AnnotationTypeOptionsBeanInfo extends SimpleBeanInfo {

    /* PropertyDescriptotrs
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        PropertyDescriptor[] descriptors;
        
        try {
            descriptors = new PropertyDescriptor[] {
                              new PropertyDescriptor("highlightColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("useHighlightColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("foregroundColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("inheritForegroundColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("waveUnderlineColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("useWaveUnderlineColor", AnnotationTypeOptions.class), // NOI18N
                              new PropertyDescriptor("wholeLine", AnnotationTypeOptions.class, "isWholeLine", null) // NOI18N
                          };
            ResourceBundle bundle;
            bundle = NbBundle.getBundle(AnnotationTypeOptionsBeanInfo.class);

            descriptors[0].setDisplayName(bundle.getString("PROP_AT_HIGHLIGHT")); // NOI18N
            descriptors[0].setShortDescription(bundle.getString("HINT_AT_HIGHLIGHT")); // NOI18N
            descriptors[1].setDisplayName(bundle.getString("PROP_AT_USE_HIGHLIGHT")); // NOI18N
            descriptors[1].setShortDescription(bundle.getString("HINT_AT_USE_HIGHLIGHT")); // NOI18N
            descriptors[2].setDisplayName(bundle.getString("PROP_AT_FOREGROUND")); // NOI18N
            descriptors[2].setShortDescription(bundle.getString("HINT_AT_FOREGROUND")); // NOI18N
            descriptors[3].setDisplayName(bundle.getString("PROP_AT_INHERIT_FOREGROUND")); // NOI18N
            descriptors[3].setShortDescription(bundle.getString("HINT_AT_INHERIT_FOREGROUND")); // NOI18N
            descriptors[4].setDisplayName(bundle.getString("PROP_AT_WAVEUNDERLINE")); // NOI18N
            descriptors[4].setShortDescription(bundle.getString("HINT_AT_WAVEUNDERLINE")); // NOI18N
            descriptors[5].setDisplayName(bundle.getString("PROP_AT_USE_WAVEUNDERLINE")); // NOI18N
            descriptors[5].setShortDescription(bundle.getString("HINT_AT_USE_WAVEUNDERLINE")); // NOI18N
            descriptors[6].setDisplayName(bundle.getString("PROP_AT_WHOLELINE")); // NOI18N
            descriptors[6].setShortDescription(bundle.getString("HINT_AT_WHOLELINE")); // NOI18N

        } catch (Exception e) {
            descriptors = new PropertyDescriptor[0];
        }
        return descriptors;
    }

}

