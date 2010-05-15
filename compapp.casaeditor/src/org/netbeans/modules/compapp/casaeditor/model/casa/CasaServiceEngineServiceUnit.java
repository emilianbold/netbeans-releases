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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import java.util.List;

/**
 *
 * @author jqian
 */
public interface CasaServiceEngineServiceUnit extends CasaServiceUnit {
    
    public static final String ENDPOINTS_PROPERTY = "endpoints";        // NOI18N
    public static final String X_PROPERTY = "x";                        // NOI18N
    public static final String Y_PROPERTY = "y";                        // NOI18N
    public static final String INTERNAL_PROPERTY = "internal";          // NOI18N
    public static final String DEFINED_PROPERTY = "defined";            // NOI18N
    public static final String UNKNOWN_PROPERTY = "unknown";            // NOI18N
    public static final String CONSUMES_PROPERTY = "consumes";          // NOI18N
    public static final String PROVIDES_PROPERTY = "provides";          // NOI18N
       
    List<CasaConsumes> getConsumes();
    void addConsumes(int index, CasaConsumes casaConsumes);
    void removeConsumes(CasaConsumes casaConsumes);       
    
    List<CasaProvides> getProvides();
    void addProvides(int index, CasaProvides casaProvides);
    void removeProvides(CasaProvides casaProvides); 
    
    int getX();
    void setX(int x);
    
    int getY();
    void setY(int y);
    
    boolean isInternal();
    void setInternal(boolean internal);
    
    /**
     * Checks the build status of the service engine service unit.
     * It's only used to decide whether to show the question mark badge in the 
     * service unit widget inside CASA or not.
     *
     * For newly DnD'ed internal or external service engine service unit
     * from the Project Explorer, its initial status is always undefined.
     * A project build is needed to change its status from undefined to defined.
     *
     * For newly DnD'ed external service engine service unit from the Palette
     * or Runtime, its status is always defined.
     */
    boolean isDefined();
    void setDefined(boolean defined);
    
    /**
     * Checks whether this service unit is unknown.
     *
     * Currently, only external service engine service unit that the user
     * created by Drag and Drop from the CASA Palette is unknown.
     */
    boolean isUnknown();
    void setUnknown(boolean unknown);
    
    // Convinience methods
    List<CasaEndpointRef> getEndpoints();    
}
