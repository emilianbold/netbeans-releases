/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
package org.netbeans.modules.bpel.nodes.refactoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.bpel.model.api.Process;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class Util {
    
    public static final int MAX_SIMPLE_NAME_LENGTH = 50;
    public static final String ENTITY_SEPARATOR = "."; // NOI18N
    private Util() {
    }
    
    public static String getUsageContextPath(String suffix, BpelEntity entity, Class<? extends BpelEntity> filter) {
        String resultStr = getUsageContextPath(entity, filter);
        if (resultStr != null) {
            suffix = suffix == null ? "" : ENTITY_SEPARATOR+suffix; // NOI18N
            resultStr += suffix;
        } else {
            resultStr = suffix;
        }
        return resultStr;
    }
    
    public static String getUsageContextPath(BpelEntity entity, Class<? extends BpelEntity> filter) {
        assert entity != null;
        StringBuffer path = new StringBuffer(getEntityName(entity));
        BpelEntity tmpEntity = entity;
        while((tmpEntity = tmpEntity.getParent()) != null) {
            if (tmpEntity.getElementType() == filter) {
                continue;
            }
            
            String tmpEntityName = getEntityName(tmpEntity);
            if (tmpEntityName != null && tmpEntityName.length() > 0) {
                path.insert(0,ENTITY_SEPARATOR).insert(0,tmpEntityName);
            }
        }
        
        return path.toString();
    }
    
    public static String getEntityName(BpelEntity entity) {
        assert entity != null;
        String name = null;
        if (entity instanceof Named) {
            name = ((Named)entity).getName();
        } else if (entity instanceof BooleanExpr) {
            name = ((BooleanExpr)entity).getContent();
            name = name == null ? null : name.trim();
            if (name != null && name.length() > MAX_SIMPLE_NAME_LENGTH) {
                name = name.substring(0, MAX_SIMPLE_NAME_LENGTH);
            }
        } else {
            org.netbeans.modules.bpel.design.nodes.NodeType
                    bpelNodeType = NavigatorNodeFactory.getBasicNodeType(entity);
            name = bpelNodeType == null ? name : bpelNodeType.getDisplayName();
        }
        
        return name == null ? "" : name;
    }
    
    public static String getNodeName(BpelNode node) {
        assert node != null;
            
        Object ref = node.getReference();
        if (ref == null || !(ref instanceof BpelEntity)) {
            return node.getHtmlDisplayName();
        }
        String nodeName = null;
        NodeType nodeType = node.getNodeType();
        switch (nodeType) {
            case BOOLEAN_EXPR :
                nodeName = Util.getEntityName((BpelEntity)ref);
                break;
            case INVOKE :
            case RECEIVE :
            case REPLY :
            case ON_EVENT :
            case MESSAGE_HANDLER :
            case PARTNER_LINK :
                nodeName = node.getShortDescription();
                if (nodeName != null 
                        && nodeName.startsWith(nodeType.getDisplayName())) 
                {
                    String tmpNodeName = nodeName
                                    .substring(nodeType.getDisplayName().length());
                    if (tmpNodeName.trim().length() > 0) {
                        nodeName = tmpNodeName;
                    }
                }
                break;
            default:
                nodeName = node.getHtmlDisplayName();
        }
        return nodeName;
    }

    
    
    public static String getTextForBpelEntity(final Component comp){
        BpelEntity entity = null;
        if (comp instanceof BpelEntity){
            entity = BpelEntity.class.cast(comp);
        }
        if (entity == null) {
            return ""; // NOI18N
        }
        
        FileObject fo = org.netbeans.modules.bpel.nodes.navigator.Util
                                    .getFileObjectByModel(entity.getBpelModel());
        if (fo == null) {
            return ""; // NOI18N
        }

        // TODO - if the line doesn't contain the target (query component name) string, keep searcing subsequent lines
        
        
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        
        int line = org.netbeans.modules.bpel.nodes.navigator.Util
                                    .getLineNum(entity);
        int col = org.netbeans.modules.bpel.nodes.navigator.Util
                                    .getColumnNum(entity);
        ModelSource modelSource = entity.getBpelModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = (StyledDocument)lookup.lookup(StyledDocument.class);
        if (document == null) {
            return ""; // NOI18N
        }
            
        CloneableEditorSupport editor = (CloneableEditorSupport)dobj.getCookie(org.openide.cookies.EditorCookie.class);
        Line.Set s =editor.getLineSet();
            
        Line xmlLine = s.getCurrent(line);
        String nodeLabel =   xmlLine.getText().trim();
        // substitute xml angle brackets <> for &lt; and &gt;
        Pattern lt = Pattern.compile("<"); //NOI18N
        Matcher mlt = lt.matcher(nodeLabel);
        nodeLabel = mlt.replaceAll("&lt;");  //NOI18N
        Pattern gt = Pattern.compile(">"); //NOI18N
        Matcher mgt = gt.matcher(nodeLabel);
        nodeLabel = mgt.replaceAll("&gt;");  //NOI18N
        return boldenRefOrType(nodeLabel);
    }
    
    // TODO get xml snippet for line that contains the
    //  query component name
    /**
     * If the label contains ref= or type=
     * the substring containing the named portion of the attribute
     * will be surrounded with html bold tags
     * e.g.,
     * input param <xsd:element ref="comment" minOccurs="0"/>
     * return <xsd:element ref="<b>comment</b>" minOccurs="0"/>
     *
     *
     */
    private static String boldenRefOrType(String label){
        // find index of type or ref
        // find 1st occurence of " from index
        // find 1st occurence of : after ", if any
        // insert <b>
        // find closing "
        // insert </b>
        int it = label.indexOf(" type"); //NOI18N
        if (it < 0){
            it = label.indexOf(" ref"); //NOI18N
        }
        if (it < 0){
            // no type or ref found
            return label;
        }
        int iq1 = label.indexOf('"',it);
        if (iq1 < it){
            // no begin quote
            return label;
        }
        int ic = label.indexOf(':',iq1);
        if (ic < iq1){
            // no colon
        }
        int iq2 = label.indexOf('"', iq1+1);
        if (iq2 < iq1 || ic > iq2){
            // couldn't find closing quote for tag
            return label;
        }
        int ib1 = -1;
        if (ic > -1){
            ib1 = ic+1;
        } else {
            ib1 = iq1+1;
        }
        StringBuffer l = new StringBuffer(label);
        l.insert(ib1,"<b>");
        // the close quote has now been pushed right 3 spaces
        l.insert(iq2+3,"</b>");
        return l.toString();
        
    }


}
