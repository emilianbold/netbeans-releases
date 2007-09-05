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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromChild;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.nodes.validation.ValidationProxyListener;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class CopyNode extends BpelNode<Copy> {
    
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
    
    @Override
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        assert event != null;
        
        boolean isUpdate = false;
        isUpdate = super.isEventRequreUpdate(event);
        if (isUpdate) {
            return isUpdate;
        }
        
        BpelEntity entity = event.getParent();
        if (entity == null) {
            return false;
        }
        Copy ref = getReference();
        return  
                ref != null && ref == entity.getParent() && (
                entity.getElementType() == From.class
// TODO r issue 84631                || entity.getElementType() == FaultHandlers.class
                || entity.getElementType() == To.class
                );
    }

    @Override
    protected boolean isRequireNameUpdate(ChangeEvent event) {
        assert event != null;
        if (super.isRequireNameUpdate(event)) {
            return true;
        }
        
        BpelEntity entity = event.getParent();
        Copy ref = getReference();
        
        if (entity == null || ref == null || ref != entity) {
            return false;
        }

        return event instanceof EntityRemoveEvent 
                || event instanceof EntityInsertEvent;
    }

    @Override
    protected ResultType getValidationStatus(ValidationProxyListener vpl) {
        ResultType resultType = super.getValidationStatus(vpl);
        if (resultType != null) {
            return resultType;
        }
        
        Copy ref = getReference();
        From from = ref.getFrom();
        To to = ref.getTo();
        
        ResultType fromResType = vpl
                .getValidationStatusForElement(from);
        ResultType toResType = vpl
                .getValidationStatusForElement(to);
        
        resultType = ValidationProxyListener.getPriorytestType(fromResType, toResType);
        return resultType;
    }

    @Override
    protected boolean isValidationAnnotatedEntity(Component component) {
        boolean isSupportedEntity = 
                super.isValidationAnnotatedEntity(component);
        if (isSupportedEntity) {
            return true;
        }

        Copy ref = getReference();
        From from = ref.getFrom();
        
        if (from != null && from.equals(component)) {
            return true;
        }
        
        To to = ref.getTo();
        if (to != null && to.equals(component)) {
            return true;
        }
        
        return false;
    }

    @Override
    protected boolean isComplexValidationStatus() {
        return true;
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
        FromChild child = from.getFromChild();
        //
        if ( !(child instanceof Literal)) {
          return result.toString();
        }
        Literal literal = (Literal) child;
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
        
        stringFrom =  DecorationProvider.Util.getFromLabel(from);
        stringFrom = stringFrom.replace(">", "&gt;");
        stringFrom = stringFrom.replace("<", "&lt;");
        
        stringTo = DecorationProvider.Util.getToLabel(to);
        if (stringFrom == null && stringTo == null) {
            return "";
        } else {
            stringFrom = stringFrom == null ? "" : stringFrom;
            stringTo = stringTo == null ? "" : stringTo;
        }
        return NbBundle.getMessage(CopyNode.class,"LBL_Copy",stringTo,stringFrom); // NOI18N
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
//            return EMPTY_STRING;
//        }
//        
//        stringFrom =  DecorationProvider.Util.getFromLabel(from);
//        String endpointStr = DecorationProvider.Util.getEndpointReferenceLabelPart(from);
//        if(stringFrom != null && endpointStr != null
//                && endpointStr.length() >0) {
//            stringFrom = "("+stringFrom+endpointStr+")";
//        }
//        
//        stringTo = DecorationProvider.Util.getToLabel(to);
//        if (stringFrom == null && stringTo == null) {
//            return "";
//        } else {
//            stringFrom = stringFrom == null ? "" : stringFrom;
//            stringTo = stringTo == null ? "" : stringTo;
//        }
//        return NbBundle.getMessage(CopyNode.class,"LBL_COPY_HTML_TOOLTIP",stringTo,stringFrom); // NOI18N
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
