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


package org.netbeans.modules.iep.editor.tcg.ps;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.model.TcgPropertyType;
import org.netbeans.modules.iep.editor.tcg.util.ArrayHashMap;

public class TcgComponentNode extends AbstractNode implements SharedConstants {
    /**
     * The logger.
     */
    private static final Logger mLogger = Logger.getLogger(TcgComponentNode.class.getName());
    
    private TcgComponent mComponent;
    private DisplayNameHelper mHelper;
    private TcgComponent mDoc;
    private TcgComponentNodeView mView;
    
    /**
     * Sheet holding various Sheet.Sets
     *
     */
    private Sheet mSheet = null;
    
    public TcgComponentNode(TcgComponent component, DisplayNameHelper helper, TcgComponent doc, TcgComponentNodeView view) {
        super(Children.LEAF);
        mComponent = component;
        if (helper == null) {
            mHelper = new DefaultDisplayNameHelper(component);
        } else {
            mHelper = helper;
        }
        mDoc = doc;
        mView = view;
    }
    
    public TcgComponentNode(TcgComponent component, TcgComponent doc, TcgComponentNodeView view) {
        this(component, null, doc, view);
    }
    
    public String getName() {
        return mComponent.getName();
    }
    
    public String getDisplayName() {
        return mHelper.getDisplayName();
    }
    
    public String getTypeDisplayName() {
        return mHelper.getTypeDisplayName();
    }
    
    public String getShortDescription() {
        return TcgPsI18n.getToolTip(mComponent);
    }
    
    public TcgComponent getDoc() {
        return mDoc;
    }
    
    public TcgComponent getComponent() {
        return mComponent;
    }
    
    public TcgComponentNodeView getView() {
        return mView;
    }
    
    protected Sheet createSheet() {
        mSheet = new Sheet();
        addToSheet(mSheet);
        //RA do not show alert and logger properties
        //addLoggerAlertProps();
        return mSheet;
    }
    
    private void addToSheet(Sheet sheet) {
        ArrayHashMap ssTable = new ArrayHashMap();
        try {
            List propList = mComponent.getPropertyList();
            Sheet.Set ss = null;
            for (int i = 0, I = propList.size(); i < I; i++) {
                TcgProperty prop = (TcgProperty)propList.get(i);
                TcgPropertyType pt = prop.getType();
                if (!pt.isReadable()) {
                    continue;
                }
                String category = pt.getCategory();
                if (ssTable.containsKey(category)) {
                    ss = (Sheet.Set)ssTable.get(category);
                } else {
                    ss = new Sheet.Set();
                    ss.setName(category);
                    ss.setDisplayName(TcgPsI18n.getCatetoryDisplayName(pt));
                    ssTable.put(category, ss);
                    ss.setExpert(true);
                }
                ss.put(TcgComponentNodeProperty.newInstance(prop, this));
            }
            // if component defines property categoryOrder, add categories mentioned 
            // first, then add the cagegories not mentioned in the order added to ssTable
            if (mComponent.hasProperty(CATEGORY_ORDER_KEY)) {
                List catList = mComponent.getProperty(CATEGORY_ORDER_KEY).getListValue();
                for (int i = 0, I = catList.size(); i < I; i++) {
                    String key = (String)catList.get(i);
                    if (ssTable.containsKey(key)) {
                        sheet.put((Sheet.Set)ssTable.get(key));
                        ssTable.remove(key);
                    }
                }
            } 
            List ssList = ssTable.getValueList();
            for (int i = 0, I = ssList.size(); i < I; i++) {
                sheet.put((Sheet.Set)ssList.get(i));
            }
        } catch (Exception ex) {
            mLogger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public static interface DisplayNameHelper {
        public String getDisplayName();
        public String getTypeDisplayName();
    }
    
    public static class DefaultDisplayNameHelper implements DisplayNameHelper {
        private TcgComponent mComponent;
        public DefaultDisplayNameHelper(TcgComponent component) {
            mComponent = component;
        }
        
        public String getDisplayName() {
            return TcgPsI18n.getDisplayName(mComponent);
        }

        public String getTypeDisplayName() {
            return TcgPsI18n.getDisplayName(mComponent.getType());
        }
    }
}    