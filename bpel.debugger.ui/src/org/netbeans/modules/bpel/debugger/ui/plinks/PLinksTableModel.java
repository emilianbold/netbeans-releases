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

package org.netbeans.modules.bpel.debugger.ui.plinks;

import java.util.Vector;
import javax.swing.JToolTip;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.EndpointWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.PartnerLinkWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.RoleRefWrapper;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Table model supporting the BPEL Partner Links view.
 * 
 * @author Kirill Sorokin
 */
public class PLinksTableModel implements TableModel, Constants {
    
    public static final String NAME_COLUMN_ID = 
            "NameColumn"; // NOI18N
    public static final String TYPE_COLUMN_ID = 
            "TypeColumn"; // NOI18N
    public static final String VALUE_COLUMN_ID = 
            "ValueColumn"; // NOI18N
    
    private BpelDebugger myDebugger;
    
    private Vector myListeners = new Vector();
    
    /**{@inheritDoc}*/
    public PLinksTableModel(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return "";
        }
        
        if (object instanceof PLinksTreeModel.Dummy) {
            return "";
        }
        
        if (object instanceof JToolTip) {
            final Object realObject = ((JToolTip) object).
                    getClientProperty("getShortDescription"); // NOI18N
                    
            return getValueAt(realObject, column);
        }
        
        if (column.equals(TYPE_COLUMN_ID)) {
            if (object instanceof PartnerLinkWrapper) {
                final PartnerLinkWrapper pLink = (PartnerLinkWrapper) object;
                final QName qName = pLink.getPartnerLinkTypeRef().getQName();
                
                return qName.getPrefix() + ":" + qName.getLocalPart(); // NOI18N
            }
            
            if (object instanceof RoleRefWrapper) {
                final RoleRefWrapper rWrapper = (RoleRefWrapper) object;
                final QName qName = 
                        rWrapper.getRoleRef().get().getPortType().getQName();
                
                return qName.getPrefix() + ":" + qName.getLocalPart(); // NOI18N
            }
            
            if (object instanceof EndpointWrapper) {
                final EndpointWrapper epWrapper = (EndpointWrapper) object;
                
                final String value = epWrapper.getSerializedValue();
                if (value != null) {
                    return "";
                } else {
                    return NbBundle.getMessage(
                            PLinksNodeModel.class, "CTL_EndpointMissing");
                }
            }
            
            if (object instanceof Node) {
                return "";
            }
        }
        
        if (column.equals(VALUE_COLUMN_ID)) {
            if (object instanceof PartnerLinkWrapper) {
                return ((PartnerLinkWrapper) object).isDynamic() ?
                    NbBundle.getMessage(PLinksNodeModel.class, "CTL_DynamicPL") :
                    NbBundle.getMessage(PLinksNodeModel.class, "CTL_StaticPL");
            }
            
            if (object instanceof RoleRefWrapper) {
                final RoleRefWrapper rWrapper = (RoleRefWrapper) object;
                final QName qName = rWrapper.getRoleRef().getQName();
                
                return qName.getLocalPart();
            }
            
            if (object instanceof EndpointWrapper) {
                final EndpointWrapper epWrapper = (EndpointWrapper) object;
                
                final String value = epWrapper.getSerializedValue();
                if (value != null) {
                    return value;
                } else {
                    return NbBundle.getMessage(
                            PLinksNodeModel.class, "CTL_EndpointMissing");
                }
            }
            
            if (object instanceof Node) {
                final Node node = (Node) object;
                
                if (object instanceof Element) {
                    final Element element = (Element) object;
                    final NodeList childNodes = element.getChildNodes();
                    
                    if ((childNodes.getLength() == 1) && 
                            (childNodes.item(0).getNodeType() == Node.TEXT_NODE)) {
                        return childNodes.item(0).getNodeValue();
                    }
                    
                    return "";
                }
                
                return node.getNodeValue();
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (column.equals(TYPE_COLUMN_ID)) {
            return true;
        }
        
        if (column.equals(VALUE_COLUMN_ID)) {
            return true;
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
    }
    
    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        
        myListeners.remove(listener);
    }
}
