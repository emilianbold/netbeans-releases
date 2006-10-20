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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class CopyNode extends BpelNode<Copy> {
    
    private static String DELIMITER = "/"; // NOI18N
    private static String EQUAL_SIGN = "="; // NOI18N
    private static String EXP_LABEL = "(exp)"; // NOI18N
    private static String QUERY_LABEL = "(query)"; // NOI18N
    private static String ENDPOINT_REFERENCE= "endpointReference"; // NOI18N
    
    public CopyNode(Copy copy, Lookup lookup) {
        super(copy, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.COPY;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                From.class, COPY_FROM, "getFrom", null, null); // NOI18N
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                To.class, COPY_TO, "getTo", null, null); // NOI18N
        return sheet;
    }
    
    // TODO Need to be corrected!!!
    public static String serializeFrom(From from) {
        if (from == null) {
            return null;
        }
        //
        StringBuffer result = new StringBuffer(200);
        //
        BpelReference<VariableDeclaration> varRef = from.getVariable();
        WSDLReference<Part> partRef = from.getPart();
        String expression = from.getContent();
        WSDLReference<CorrelationProperty> propRef = from.getProperty();
        BpelReference<PartnerLink> pLinkRef = from.getPartnerLink();
        Roles role = from.getEndpointReference();
        Literal literal = from.getLiteral();
        //
        if (expression != null && expression.length() != 0) {
            result.append(expression);
        } else if (literal != null) {
            result.append(literal.getContent());
        } else if (varRef != null) {
            String varName = ResolverUtility.getNameByRef(varRef);
            if (varName != null) {
                if (propRef != null) {
                    String propName = ResolverUtility.getNameByRef(propRef);
                    if (propName != null) {
                        result.append(varName).append(DELIMITER).append(propName);
                    }
                } else {
                    result.append(varName);
                    //
                    String partName = ResolverUtility.getNameByRef(partRef);
                    if (partName != null ) {
                        result.append(DELIMITER).append(partName);
                    }
                }
            }
        } else if (pLinkRef != null) {
            String pLinkName = ResolverUtility.getNameByRef(pLinkRef);
            if (pLinkName != null) {
                if (role == null) {
                    result.append(pLinkName);
                } else {
                    //
                    PartnerLink pLink = pLinkRef.get();
                    if (pLink == null) {
                        PartnerLinkContainer plCont = from.getBpelModel().
                                getProcess().getPartnerLinkContainer();
                        if (plCont != null) {
                            PartnerLink[] plArr = plCont.getPartnerLinks();
                            for (PartnerLink pl : plArr) {
                                if (pl != null && pLinkName.equals(pl.getName())) {
                                    pLink = pl;
                                    break;
                                }
                            }
                        }
                    }
                    //
                    if (pLink != null) {
                        switch (role) {
                            case MY_ROLE:
                                result.append(pLinkName).append(DELIMITER).
                                        append(MY_ROLE);
                                break;
                            case PARTNER_ROLE:
                                result.append(pLinkName).append(DELIMITER).
                                        append(PARTNER_ROLE);
                                break;
                        }
                    }
                }
            }
        }
        //
        return result.toString();
    }
    
    // TODO Need to be corrected!!!
    public static String serializeTo(To to) {
        if (to == null) {
            return null;
        }
        //
        StringBuffer result = new StringBuffer(100);
        //
        BpelReference<VariableDeclaration> varRef = to.getVariable();
        WSDLReference<Part> partRef = to.getPart();
        String query = to.getContent();
        WSDLReference<CorrelationProperty> propRef = to.getProperty();
        BpelReference<PartnerLink> pLinkRef = to.getPartnerLink();
        //
        if (query != null && query.length() != 0) {
            result.append(query);
        } else if (varRef != null) {
            String varName = ResolverUtility.getNameByRef(varRef);
            if (varName != null) {
                if (propRef != null) {
                    String propName = ResolverUtility.getNameByRef(propRef);
                    if (propName != null) {
                        result.append(varName).append(DELIMITER).append(propName);
                    }
                } else {
                    result.append(varName);
                    //
                    String partName = ResolverUtility.getNameByRef(partRef);
                    if (partName != null ) {
                        result.append(DELIMITER).append(partName);
                    }
                }
            }
        } else if (pLinkRef != null) {
            String pLinkName = ResolverUtility.getNameByRef(pLinkRef);
            if (pLinkName != null) {
                result.append(pLinkName);
            }
        }
        //
        return result.toString();
    }
    
    protected String getImplHtmlDisplayName() {
        Copy ref = getReference();
        if (ref == null) {
            return super.getImplHtmlDisplayName();
        }
        
        String stringFrom = null;
        String stringTo = null;
        
        From from = ref.getFrom();
        To to = ref.getTo();
        
        if (from == null || to == null) {
            return "";
        }
        
        stringFrom =  getFromLabel(from);
        stringTo = getToLabel(to);
        if (stringFrom == null && stringTo == null) {
            return "";
        } else {
            stringFrom = stringFrom == null ? "" : stringFrom;
            stringTo = stringTo == null ? "" : stringTo;
        }
        return NbBundle.getMessage(CopyNode.class,"LBL_Copy",stringTo,stringFrom); // NOI18N
    }
    
    private String getFromLabel(From from) {
        String stringFrom = null;
        
        stringFrom =  from.getVariable() == null ? null : from.getVariable().getRefString();
        
        stringFrom = stringFrom != null ? stringFrom :
            from.getPartnerLink() != null ? from.getPartnerLink().getRefString()
            : null;
        
        stringFrom = stringFrom != null ? stringFrom :
            from.getContent() == null ? null
                : from.getContent().length() < 10 ? from.getContent() : EXP_LABEL;
        
        return stringFrom;
    }
    
    private String getToLabel(To to) {
        String stringTo = null;
        
        stringTo = to.getVariable() == null ? null : to.getVariable().getRefString();
        stringTo = stringTo != null ? stringTo
                : to.getPartnerLink() == null ? null : to.getPartnerLink().getRefString();
        
        stringTo = stringTo != null ? stringTo :
            to.getContent() == null ? null
                : to.getContent().length() < 10 ? to.getContent() : QUERY_LABEL;
        return stringTo;
    }
    
    private String getEndpointReferenceLabelPart(From from) {
        StringBuffer labelStr = new StringBuffer();
        Roles roles = from.getEndpointReference();
        if (roles != null) {
            labelStr.append(" ").append(ENDPOINT_REFERENCE).append(EQUAL_SIGN);
            labelStr.append(roles.toString());
        }
        
        return labelStr.toString();
    }
    
//    protected String getImplShortDescription() {
//        Copy ref = getReference();
//        if (ref == null) {
//            return super.getImplShortDescription();
//        }
//        
//        String stringFrom = null;
//        String stringTo = null;
//        
//        From from = ref.getFrom();
//        To to = ref.getTo();
//        
//        if (from == null || to == null) {
//            return "";
//        }
//        
//        stringFrom =  getFromLabel(from);
//        String endpointStr = getEndpointReferenceLabelPart(from);
//        if(stringFrom != null && endpointStr != null
//                && endpointStr.length() >0) {
//            stringFrom = "("+stringFrom+endpointStr+")";
//        }
//        
//        stringTo = getToLabel(to);
//        if (stringFrom == null && stringTo == null) {
//            return "";
//        } else {
//            stringFrom = stringFrom == null ? "" : stringFrom;
//            stringTo = stringTo == null ? "" : stringTo;
//        }
//        return NbBundle.getMessage(CopyNode.class,"LBL_COPY_TOOLTIP",stringTo,stringFrom); // NOI18N
//    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.MOVE_COPY_UP,
            ActionType.MOVE_COPY_DOWN,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
}
