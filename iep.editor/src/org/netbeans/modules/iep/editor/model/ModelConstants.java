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

package org.netbeans.modules.iep.editor.model;

import java.sql.Types;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;


// Must match library.xml
public interface ModelConstants extends TcgModelConstants, SharedConstants {
    // In both TcgModelConstants, and SharedConstants
    public static String NAME_KEY = "name";
     
    public static String COMMENT_KEY = "comment";
    
    // TcgComponentType
    public static String PROPERTY_PATH = "/Metadata/BasicModuleProgramming/Property";
    public static TcgComponentType PROPERTY_TYPE = TcgModelManager.getTcgComponentType(PROPERTY_PATH);    
    
    public static String LINK_PATH = "/IEP/Model/Link";
    public static TcgComponentType LINK_TYPE = TcgModelManager.getTcgComponentType(LINK_PATH);

    public static String PLAN_PATH = "/IEP/Model/Plan";
    
    public static String COLUMN_METADATA_PATH = "/IEP/Metadata/ColumnMetadata";
    
    public static String SCHEMA_PATH = "/IEP/Metadata/Schema";
    public static TcgComponentType SCHEMA_TYPE = TcgModelManager.getTcgComponentType(SCHEMA_PATH);

    public static String OPERATOR_PATH = "/IEP/Operator";

    public static String INPUT_PATH = "/IEP/Input";

    public static String STREAM_INPUT_PATH = "/IEP/Input/StreamInput";

    public static String OUTPUT_PATH = "/IEP/Output";

    public static String STREAM_OUTPUT_PATH = "/IEP/Output/StreamOutput";
    
    public static String SCHEMA_MARKER = "SchemaMarker";

}
    
