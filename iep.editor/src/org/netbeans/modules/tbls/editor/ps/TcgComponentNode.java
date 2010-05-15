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


package org.netbeans.modules.tbls.editor.ps;
import org.netbeans.modules.iep.model.share.SharedConstants;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Iterator;
import java.util.List;

import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.tbls.model.ArrayHashMap;
import org.netbeans.modules.tbls.model.TcgComponent;
import org.netbeans.modules.tbls.model.TcgComponentType;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;


public class TcgComponentNode extends AbstractNode implements SharedConstants {
    /**
     * The logger.
     */
    private static final Logger mLogger = Logger.getLogger(TcgComponentNode.class.getName());
    
    private DisplayNameHelper mHelper;
    private TcgComponentNodeView mView;
    
    private OperatorComponent mComponent;
    
    private IEPModel mModel;
    
    
    /**
     * Sheet holding various Sheet.Sets
     *
     */
    private Sheet mSheet = null;
    
//    public TcgComponentNode(TcgComponent component, DisplayNameHelper helper, TcgComponent doc, TcgComponentNodeView view) {
//        super(Children.LEAF);
//        //ritmComponent = component;
//        if (helper == null) {
//            mHelper = new DefaultDisplayNameHelper(component);
//        } else {
//            mHelper = helper;
//        }
//        //ritmDoc = doc;
//        mView = view;
//    }
    
//    public TcgComponentNode(TcgComponent component, TcgComponent doc, TcgComponentNodeView view) {
//        this(component, null, doc, view);
//    }
    
    public TcgComponentNode(OperatorComponent component, 
                            IEPModel model, 
                            TcgComponentNodeView view) {
        super(Children.LEAF, createLookup(model));
        this.mComponent = component;
    }
    
    public String getName() {
        return mComponent.getName();
    }
    
    public String getDisplayName() {
        //ritreturn mHelper.getDisplayName();
        return mComponent.getString(SharedConstants.PROP_NAME);
    }
    
    public String getTypeDisplayName() {
        //ritreturn mHelper.getTypeDisplayName();
        return TcgPsI18n.getDisplayName(mComponent.getComponentType());
    }
    
    public String getShortDescription() {
        //ritreturn TcgPsI18n.getToolTip(mComponent);
        return TcgPsI18n.getToolTip(mComponent.getComponentType());
    }
    
    public TcgComponent getDoc() {
        return null;
    }
    
    public TcgComponent getComponent() {
        return null;
    }
    
  
    
    public IEPModel getModel() {
        return this.mModel;
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
//            List<org.netbeans.modules.iep.model.Property> propList = mComp.getProperties();
//            Sheet.Set ss = null;
//            for (int i = 0, I = propList.size(); i < I; i++) {
//                org.netbeans.modules.iep.model.Property prop = (org.netbeans.modules.iep.model.Property)propList.get(i);
//                TcgPropertyType pt = prop.getPropertyType();
//                if (!pt.isReadable()) {
//                    continue;
//                }
//                String category = pt.getCategory();
//                if (ssTable.containsKey(category)) {
//                    ss = (Sheet.Set)ssTable.get(category);
//                } else {
//                    ss = new Sheet.Set();
//                    ss.setName(category);
//                    ss.setDisplayName(TcgPsI18n.getCatetoryDisplayName(pt));
//                    ssTable.put(category, ss);
//                    ss.setExpert(true);
//                }
//                ss.put(TcgComponentNodeProperty.newInstance(prop, mComp, mComp.getModel()));
//                
//            }
            
            Sheet.Set ss = null;
            Sheet.Set firstSheet = null;
            TcgComponentType componentType = mComponent.getComponentType();
            List properties = componentType.getPropertyTypeList();
            Iterator it = properties.iterator();
            while(it.hasNext()) {
                TcgPropertyType pt = (TcgPropertyType) it.next();
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
                  
                  if(firstSheet == null) {
                      firstSheet = ss;
                  }
              }
              Node.Property property = NodePropertyFactory.getInstance().getProperty(pt, mComponent);
              if(property != null) {
                  ss.put(property);
              }
              //ss.put(TcgComponentNodeProperty.newInstance(prop, mComp, mComp.getModel()));
              
                
            }
            
            //add documentation to first sheet;
            if(firstSheet != null) {
                DocumentationProperty docProp = new DocumentationProperty(String.class, mComponent);
                firstSheet.put(docProp);
            }
            
            
            // if component defines property categoryOrder, add categories mentioned 
            // first, then add the cagegories not mentioned in the order added to ssTable
            if (componentType.getPropertyType(PROP_CATEGORY_ORDER) != null) {
                List<String> catList = (List<String>) componentType.getPropertyType(PROP_CATEGORY_ORDER).getDefaultValue();
                for (int i = 0, I = catList.size(); i < I; i++) {
                    String key = catList.get(i);
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
    
    private static Lookup createLookup(IEPModel model) {
        // Include the data object in order for the Navigator to
        // show the structure of the current document.
        //and when carent in source view appropriate properties are displayed
        //and right click actions on source
        DataObject dobj = model.getModelSource().getLookup().lookup(DataObject.class);
        if (dobj != null) {
            //We want to pass common cookies like validate, check etc
            return new ProxyLookup(new Lookup[] {
                    Lookups.exclude(dobj.getNodeDelegate().getLookup(), new Class[] {
                        Node.class,
                        DataObject.class
                    }),
                    Lookups.singleton(dobj),
            });
        }
        //should not happen
        return new ProxyLookup(new AbstractLookup(new InstanceContent()));
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