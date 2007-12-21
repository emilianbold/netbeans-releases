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
package org.netbeans.modules.bpel.nodes.navigator;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.DecorationProvider;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TooltipManager {
    boolean accept(NodeType nodeType, Object reference);
    String getTooltip(NodeType nodeType, Object reference);
    
    class ShortTooltipManager implements TooltipManager {
        public boolean accept(NodeType nodeType, Object reference) {
            return reference instanceof Component;
        }

        public String getTooltip(NodeType nodeType, Object reference) {
            if (!accept(nodeType, reference)) {
                return null;
            }
            
            
            String docsStr = null;
            if (reference instanceof ExtensibleElements) {
                docsStr = Util.getDocumentations((ExtensibleElements)reference);
            }
            
            String refName = reference instanceof Named 
                    ? ((Named)reference).getName() 
                    : BpelNode.EMPTY_STRING; 
            refName = refName == null ? BpelNode.EMPTY_STRING : refName;
            
        return docsStr == null || BpelNode.EMPTY_STRING.equals(docsStr) ? 
            NbBundle.getMessage(BpelNode.class,
                "LBL_SHORT_TOOLTIP_HTML_TEMPLATE", // NOI18N
                nodeType.getDisplayName(), 
                refName)
                : NbBundle.getMessage(BpelNode.class,
                "LBL_SHORT_TOOLTIP_WITHDOCS_HTML_TEMPLATE", // NOI18N
                nodeType.getDisplayName(), 
                refName, docsStr); // NOI18N
        }
    }
    
    class LongTooltipManager implements TooltipManager {
        public boolean accept(NodeType nodeType, Object reference) {
            if (!(reference instanceof BpelEntity)) {
                return false;
            }
            
            switch (nodeType) {
                case INVOKE:
                case IMPORT:
                case IMPORT_WSDL:
                case IMPORT_SCHEMA:
                case FROM_PART:
                case TO_PART:
                case ON_EVENT:
                case MESSAGE_HANDLER:
                case PARTNER_LINK:
                case RECEIVE:
                case REPLY:
                    return true;
            }
            
            return false;
        }

        public String getTooltip(NodeType nodeType, Object reference) {
            if (!accept(nodeType, reference)) {
                return null;
            }
            
            String docsStr = null;
            if (reference instanceof ExtensibleElements) {
                docsStr = Util.getDocumentations((ExtensibleElements)reference);
            }
            
            String refName = reference instanceof Named 
                    ? ((Named)reference).getName() 
                    : BpelNode.EMPTY_STRING; 
            refName = refName == null ? BpelNode.EMPTY_STRING : refName;
            
        return docsStr == null || BpelNode.EMPTY_STRING.equals(docsStr) ? 
            NbBundle.getMessage(BpelNode.class,
                "LBL_LONG_TOOLTIP_HTML_TEMPLATE" , // NOI18N
                nodeType.getDisplayName(), 
                refName,
                Util.getAttributesTooltip(nodeType,reference)
                ) 
                : NbBundle.getMessage(BpelNode.class,
                "LBL_LONG_TOOLTIP_WITHDOCS_HTML_TEMPLATE" , // NOI18N
                new String[] {nodeType.getDisplayName(), 
                refName,
                docsStr,
                Util.getAttributesTooltip(nodeType,reference)}
                ); 
        }
    }    
    
    class CopyTooltipManager implements TooltipManager {
        public boolean accept(NodeType nodeType, Object reference) {
            if (!(reference instanceof Copy)) {
                return false;
            }
            
            return NodeType.COPY == nodeType;
        }

        public String getTooltip(NodeType nodeType, Object reference) {
            if (!accept(nodeType, reference)) {
                return null;
            }
            
            Copy copyRef = (Copy)reference;
            
        String stringFrom = null;
        String stringTo = null;
        
        From from = copyRef.getFrom();
        To to = copyRef.getTo();
        
        if (from == null || to == null) {
            return BpelNode.EMPTY_STRING;
        }
        
        stringFrom =  DecorationProvider.Util.getFromLabel(from);
        String endpointStr = DecorationProvider.Util.getEndpointReferenceLabelPart(from);
        if(stringFrom != null && endpointStr != null
                && endpointStr.length() >0) {
            stringFrom = "("+stringFrom+endpointStr+")";
        }
        
        stringTo = DecorationProvider.Util.getToLabel(to);
        if (stringFrom == null && stringTo == null) {
            return "";
        } else {
            stringFrom = stringFrom == null ? BpelNode.EMPTY_STRING : stringFrom;
            stringTo = stringTo == null ? BpelNode.EMPTY_STRING : stringTo;
        }
        
        
        return NbBundle.getMessage(BpelNode.class,
                "LBL_COPY_HTML_TOOLTIP",// NOI18N
                stringTo,stringFrom); 
        }
        
        

    }    

    class Util {
        private Util() {
        }
        
        public static String getDocumentations(ExtensibleElements entity) {
            Documentation[] docs = entity.getDocumentations();
            if (docs == null) {
                return null;
            }
            StringBuffer docsStr = new StringBuffer();
            for (int i = 0; i < docs.length; i++) {
                String content = docs[i].getContent();
                if (content != null) {
                    docsStr.append(content);
                    if (i < docs.length -1) {
                        docsStr.append("<br>");
                    }
                }
            }
            
            return docsStr == null ? null : docsStr.toString();
        }

        public static String getLocalizedAttribute(Reference attributeRef, String attributeName) {
            if (attributeRef == null) {
                return BpelNode.EMPTY_STRING;
            }
            
            attributeName = attributeName == null ? "" : attributeName;
            return NbBundle.getMessage(
                    BpelNode.class,
                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
                    attributeName,
                    attributeRef.getRefString()
                    );
        }
        
        public static String getLocalizedAttribute(String attributeValue, String attributeName) {
            if (attributeValue == null) {
                return BpelNode.EMPTY_STRING;
            }
            
            attributeName = attributeName == null ? BpelNode.EMPTY_STRING : attributeName;
            attributeValue = attributeValue == null ? BpelNode.EMPTY_STRING : attributeValue;
            return NbBundle.getMessage(
                    BpelNode.class,
                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
                    attributeName,
                    attributeValue
                    );
        }
        
        public static String getAttributesTooltip(NodeType nodeType, Object component) {
            StringBuffer attributesTooltip = new StringBuffer();
            BpelReference tmpAttrRef = null;
            switch (nodeType) {
                case INVOKE:
                    assert component instanceof Invoke;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Invoke)component).getOutputVariable(),
                                Invoke.OUTPUT_VARIABLE)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Invoke)component).getInputVariable(),
                                Invoke.INPUT_VARIABLE)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Invoke)component).getPartnerLink(),
                                Invoke.PARTNER_LINK)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Invoke)component).getOperation(),
                                Invoke.OPERATION)
                            );
                    break;
                case IMPORT:
                case IMPORT_WSDL:
                case IMPORT_SCHEMA:
                    assert component instanceof Import;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Import)component).getNamespace(),
                                Import.NAMESPACE)
                            );
                    break;
                case FROM_PART:
                    assert component instanceof FromPart;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((FromPart)component).getToVariable(),
                                FromPart.TO_VARIABLE)
                            );
                    break;
                case TO_PART:
                    assert component instanceof ToPart;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((ToPart)component).getFromVariable(),
                                ToPart.FROM_VARIABLE)
                            );
                    break;
                case ON_EVENT:
                    assert component instanceof OnEvent;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((OnEvent)component).getMessageExchange(),
                                OnEvent.MESSAGE_EXCHANGE)
                            );
                    break;
                case MESSAGE_HANDLER:
                    assert component instanceof OnMessage;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((OnMessage)component).getMessageExchange(),
                                OnMessage.MESSAGE_EXCHANGE)
                            );
                    break;
                case PARTNER_LINK:
                    assert component instanceof PartnerLink;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((PartnerLink)component).getMyRole(),
                                PartnerLink.MY_ROLE)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((PartnerLink)component).getPartnerRole(),
                                PartnerLink.PARTNER_ROLE)
                            );
                    break;
                case RECEIVE:
                    assert component instanceof Receive;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Receive)component).getVariable(),
                                Receive.VARIABLE)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Receive)component).getMessageExchange(),
                                Receive.MESSAGE_EXCHANGE)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Receive)component).getPartnerLink(),
                                Receive.PARTNER_LINK)
                            );
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Receive)component).getOperation(),
                                Receive.OPERATION)
                            );
                    break;
                case REPLY:
                    assert component instanceof Reply;
                    attributesTooltip.append(Util.getLocalizedAttribute(
                                ((Reply)component).getMessageExchange(),
                                Reply.MESSAGE_EXCHANGE)
                            );
                    break;
            }
            return attributesTooltip.toString();
        }
    }
    
}
