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

package org.netbeans.modules.bpel.mapper.properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.ext.Extensions;

/**
 *
 * @author anjeleevich
 */
public interface PropertiesConstants {
    public String ROOT_FOLDER = "bpel-nmproperties"; // NOI18N
    public String DISPLAY_NAME_ATTR = "display-name"; // NOI18N
    public String ICON_ATTR = "icon"; // NOI18N
    public String TOOLTIP_ATTR = "tooltip"; // NOI18N
    public String NM_PROPERTY_ATTR = "nmproperty"; // NOI18N
    
    public Icon NM_PROPERTY_ICON = new ImageIcon(PropertiesConstants.class
            .getResource("resources/nm_property.png")); // NOI18N
    public Icon FO_PROPERTY_ICON = new ImageIcon(PropertiesConstants.class
            .getResource("resources/fo_property.png")); // NOI18N
    
    public QName NM_PROPERTY_QNAME = new QName(Extensions.NM_PROPERTY_EXT_URI, 
            "nmProperty"); // NOI18N
}
