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
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
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
    
    public CopyNode(Copy copy, Lookup lookup) {
        super(copy, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.COPY;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        PropertyUtils propUtil = PropertyUtils.getInstance();
        //
        propUtil.registerElementProperty(this, null, mainPropertySet,
                From.class, COPY_FROM, "getFrom", null, null); // NOI18N
        propUtil.registerElementProperty(this, null, mainPropertySet,
                To.class, COPY_TO, "getTo", null, null); // NOI18N
        //
        propUtil.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
// TODO add after the runtime will supported it
//        PropertyUtils.registerProperty(this, mainPropertySet,
//                KEEP_SRC_ELEMENT_NAME, "getKeepSrcElementName", "setKeepSrcElementName", "removeKeepSrcElementName"); // NOI18N
//        //
        propUtil.registerProperty(this, mainPropertySet,
                IGNORE_MISSING_FROM_DATA, "getIgnoreMissingFromData", "setIgnoreMissingFromData", "removeIgnoreMissingFromData"); // NOI18N
        //
        propUtil.registerProperty(this, mainPropertySet,
                BINARY_COPY, "getBinaryCopy", "setBinaryCopy", "removeBinaryCopy"); // NOI18N
        //
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
        From from = (ref == null) ? null : ref.getFrom();
        
        if (from != null && from.equals(component)) {
            return true;
        }
        
        To to = (ref == null) ? null : ref.getTo();
        if (to != null && to.equals(component)) {
            return true;
        }
        
        return false;
    }

    @Override
    protected boolean isComplexValidationStatus() {
        return true;
    }
    
    public static String serializeFrom(From from) {
        if (from == null) {
            return null;
        }
        //
        String fromContent = from.getContent();
        if (fromContent != null && fromContent.length() != 0) {
            return fromContent;
        }
        //
        FromChild child = from.getFromChild();
        if (child != null) {
            if (child instanceof Literal) {
                Literal literal = (Literal)child;
                String literalText = literal.getContent();
                if (literalText != null && literalText.length() != 0) {
                    return literal.getContent();
                }
            }
        }
        //
        BpelReference<VariableDeclaration> varRef = from.getVariable();
        BpelReference<PartnerLink> pLinkRef = from.getPartnerLink();
        //
        StringBuilder result = new StringBuilder(200);
        //
        if (varRef != null) {
            String varName = ResolverUtility.getNameByRef(varRef);
            if (varName != null) {
                result.append(varName);
                //
                WSDLReference<CorrelationProperty> propRef = from.getProperty();
                if (propRef != null) {
                    String propName = ResolverUtility.getNameByRef(propRef);
                    if (propName != null && propName.length() != 0) {
                        result.append(DELIMITER).append(propName);
                    }
                } 
                //
                WSDLReference<Part> partRef = from.getPart();
                if (partRef != null) {
                    String partName = ResolverUtility.getNameByRef(partRef);
                    if (partName != null && partName.length() != 0) {
                        result.append(".").append(partName);
                    }
                }
                //
                if (child != null && child instanceof Query) {
                    String queryText = ((Query)child).getContent();
                    if (queryText != null && queryText.length() != 0) {
                        result.append(DELIMITER).append(queryText);
                    }
                }
            }
        } else if (pLinkRef != null) {
            String pLinkName = ResolverUtility.getNameByRef(pLinkRef);
            if (pLinkName != null) {
                Roles role = from.getEndpointReference();
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
    
    public static String serializeTo(To to) {
        if (to == null) {
            return null;
        }
        //
        String toContent = to.getContent();
        if (toContent != null && toContent.length() != 0) {
            return toContent;
        }
        //
        StringBuilder result = new StringBuilder(100);
        //
        BpelReference<VariableDeclaration> varRef = to.getVariable();
        BpelReference<PartnerLink> pLinkRef = to.getPartnerLink();
        if (varRef != null) {
            String varName = ResolverUtility.getNameByRef(varRef);
            if (varName != null) {
                result.append(varName);
                //
                WSDLReference<CorrelationProperty> propRef = to.getProperty();
                if (propRef != null) {
                    String propName = ResolverUtility.getNameByRef(propRef);
                    if (propName != null && propName.length() != 0) {
                        result.append(DELIMITER).append(propName);
                    }
                } 
                //
                WSDLReference<Part> partRef = to.getPart();
                if (partRef != null) {
                    String partName = ResolverUtility.getNameByRef(partRef);
                    if (partName != null && partName.length() != 0) {
                        result.append(".").append(partName);
                    }
                }
                //
                Query query = to.getQuery();
                if (query != null) {
                    String queryText = query.getContent();
                    if (queryText != null && queryText.length() != 0) {
                        result.append(queryText);
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
        stringTo = DecorationProvider.Util.getToLabel(to);
        if (stringFrom == null && stringTo == null) {
            return "";
        } else {
            stringFrom = stringFrom == null ? "" : stringFrom;
            stringTo = stringTo == null ? "" : stringTo;
        }
        return org.netbeans.modules.bpel.editors.api.EditorUtil.getCorrectedHtmlRenderedString(
                    NbBundle.getMessage(CopyNode.class,"LBL_Copy",stringTo,stringFrom)); // NOI18N
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
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
}
