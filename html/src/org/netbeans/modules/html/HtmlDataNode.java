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

package org.netbeans.modules.html;

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
                    return ((HtmlDataObject)getDataObject()).getFileEncoding();
                }
            });
            sheet.put(set);
        }
        return sheet.toArray();
    }
}
