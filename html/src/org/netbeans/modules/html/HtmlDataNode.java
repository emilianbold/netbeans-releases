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

package org.netbeans.modules.html;

import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 * Node that represents HTML data object.
 *
 * @author  Radim Kubacki
 */
public class HtmlDataNode extends org.openide.loaders.DataNode {
    public static final String PROP_FILE_ENCODING = "encoding"; //NOI18N
    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N
    private Sheet sheet = null;
    
    /** Creates new HtmlDataNode */
    public HtmlDataNode(DataObject dobj, Children ch) {
        super(dobj, ch);
        setShortDescription(NbBundle.getMessage(HtmlDataNode.class, "LBL_htmlNodeShortDesc"));
    }
    
    @Override
    public Node.PropertySet[] getPropertySets() {
        if(sheet == null) {
            sheet = new Sheet();
            
            Node.PropertySet[] tmp = super.getPropertySets();
            Sheet.Set set;
            for(int i = 0; i < tmp.length; i++) {
                set = new Sheet.Set();
                set.setName(tmp[i].getName());
                set.setShortDescription(tmp[i].getShortDescription());
                set.setDisplayName(tmp[i].getDisplayName());
                set.setValue("helpID", HtmlDataNode.class.getName() + ".PropertySheet");// NOI18N
                set.put(tmp[i].getProperties());
                sheet.put(set);
            }
            // add encoding property
            set = new Sheet.Set();
            set.setName(SHEETNAME_TEXT_PROPERTIES);
            set.setDisplayName(NbBundle.getBundle(HtmlDataNode.class).getString("PROP_textfileSetName")); // NOI18N
            set.setShortDescription(NbBundle.getBundle(HtmlDataNode.class).getString("HINT_textfileSetName")); // NOI18N
            set.put(new PropertySupport.ReadOnly(
                    PROP_FILE_ENCODING,
                    String.class,
                    NbBundle.getBundle(HtmlDataNode.class).getString("PROP_fileEncoding"), //NOI18N
                    NbBundle.getBundle(HtmlDataNode.class).getString("HINT_fileEncoding") //NOI18N
                    ) {
                public Object getValue() {
                    return FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile()).name();
                }
            });
            sheet.put(set);
        }
        return sheet.toArray();
    }
}
