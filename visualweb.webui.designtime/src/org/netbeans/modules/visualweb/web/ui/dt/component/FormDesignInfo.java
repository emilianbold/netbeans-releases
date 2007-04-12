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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.ext.componentgroup.ColorWrapper;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroup;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupHolder;
import com.sun.rave.designtime.ext.componentgroup.ComponentSubset;
import com.sun.rave.designtime.ext.componentgroup.util.ComponentGroupHelper;
import com.sun.rave.designtime.ext.componentgroup.impl.ColorWrapperImpl;
import com.sun.rave.designtime.ext.componentgroup.impl.ComponentGroupImpl;
import com.sun.rave.designtime.ext.componentgroup.impl.ComponentSubsetImpl;
import com.sun.rave.web.ui.component.Form;
import com.sun.rave.web.ui.component.Form.VirtualFormDescriptor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import org.netbeans.modules.visualweb.web.ui.dt.component.vforms.VirtualFormsHelper;

/**
 * DesignInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Form} component.
 *
 * @author mbohm
 * @author gjmurphy
 */
public class FormDesignInfo extends AbstractDesignInfo {

    public static final String VIRTUAL_FORM_HOLDER_NAME = Form.class.getName();   //NOI18N
    private static final String ID_SEP = String.valueOf(NamingContainer.SEPARATOR_CHAR);
    private static final String[] SUBSET_PROPERTY_NAMES = {"participants", "submitters"}; // NOI18N
    private static final ComponentSubset.LineType[] SUBSET_LINE_TYPES = {ComponentSubset.LineType.SOLID, ComponentSubset.LineType.DASHED};
    


    /** Creates a new instance of FormDesignInfo */
    public FormDesignInfo() {
        super(Form.class);
    }

    /**
     * Allow form anywhere, so long as parent is not a form and the parent has
     * no form ancestor.
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        DesignBean thisBean = parentBean;
        while (thisBean.getBeanParent() != null) {
            if (thisBean.getInstance() instanceof Form)
                return false;
            thisBean = thisBean.getBeanParent();
        }
        return super.isSunWebUIContext(parentBean);
    }

    /**
     * <p>Designtime version of
     * <code>Form.getFullyQualifiedId(UIComponent)</code> for webui.
     */
    /*
     * Be sure to keep this method in sync with the versions in
     * <code>org.netbeans.modules.visualweb.web.ui.dt.component.Form</code> (in webui) and
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code>
     * (in jsfcl).</p>
     */
    public static String getFullyQualifiedId(DesignBean bean) {
        if (bean == null) {
            return null;
        }
        Object beanInstance = bean.getInstance();
        if (! (beanInstance instanceof UIComponent)) {
            return null;
        }
        if (beanInstance instanceof Form) {
            return ID_SEP;
        }
        String compId = bean.getInstanceName();
        if (compId == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(compId);
        DesignBean currentBean = bean.getBeanParent();
        boolean formEncountered = false;
        while (currentBean != null) {
            sb.insert(0, ID_SEP);
            if (currentBean.getInstance() instanceof Form) {
                formEncountered = true;
                break;
            }
            else {
                String currentCompId = currentBean.getInstanceName();
                if (currentCompId == null) {
                    return null;
                }
                sb.insert(0, currentCompId);
            }
            currentBean = currentBean.getBeanParent();
        }
        if (formEncountered) {
            return sb.toString();
        }
        else {
            return null;
        }
    }
    
    private void registerComponentGroupHolderIfNecessary(DesignBean bean) {
        DesignContext dcontext = bean.getDesignContext();
        ComponentGroupHolder[] holders = null;
        Object dcontextData = dcontext.getContextData(ComponentGroupHolder.CONTEXT_DATA_KEY);
        if (dcontextData instanceof ComponentGroupHolder[]) {
            holders = (ComponentGroupHolder[])dcontextData;
        }
                
        boolean foundSelf = false;
        if (holders != null) {
            for (int i = 0; i < holders.length; i++) {
                if (VIRTUAL_FORM_HOLDER_NAME.equals(holders[i].getName())) {
                    foundSelf = true;
                    break;
                }
            }
        }
        if (!foundSelf) {
            ComponentGroupHolder[] revisedHolders;
            if (holders == null) {
                revisedHolders = new ComponentGroupHolder[]{new VirtualFormHolder()};
            }
            else {
                revisedHolders = new ComponentGroupHolder[holders.length + 1];
                System.arraycopy(holders, 0, revisedHolders, 0, holders.length);
                revisedHolders[holders.length] = new VirtualFormHolder();
            }
            dcontext.setContextData(ComponentGroupHolder.CONTEXT_DATA_KEY, revisedHolders);
        }
    }
    
    public Result beanCreatedSetup(DesignBean bean) {
        registerComponentGroupHolderIfNecessary(bean);
        return Result.SUCCESS;
    }
    
    public void beanContextActivated(DesignBean bean) {
        registerComponentGroupHolderIfNecessary(bean);
    }
    
    private static class VirtualFormHolder implements ComponentGroupHolder {
       private static ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-DT",
                               Locale.getDefault(),
                               VirtualFormHolder.class.getClassLoader());
       
       public String getName() {
           return VIRTUAL_FORM_HOLDER_NAME;
       }
        
       public ComponentGroup[] getComponentGroups(DesignContext dcontext) {
           DesignBean[] formBeans = dcontext.getBeansOfType(Form.class);
           if (formBeans == null) {
               return new ComponentGroup[0];
           }

           List<ComponentGroup> groupList = new ArrayList<ComponentGroup>();
           for (int i = 0; i < formBeans.length; i++) {
               DesignBean formBean = formBeans[i];
               
               if (formBean == null) {
                   continue;
               }
               
               Object formObj = formBean.getInstance();
               if (! (formObj instanceof Form)) {
                   continue;
               }
               Form form = (Form)formObj;
               
               VirtualFormDescriptor[] vds = form.getVirtualForms();

               if ((vds == null) || (vds.length == 0)) {
                    continue;
               }
               
               //get form name
               DesignProperty idProp = formBean.getProperty("id"); //NOI18N
               
               String formName = idProp == null ? "" : (String)idProp.getValue(); //NOI18N

               for (int v = 0; v < vds.length; v++) {
                    //get group name
                    String vfName = vds[v].getName();
                    if (vfName == null) {
                       continue;
                    }

                    String name = formName + "." + vfName;  // name like .virtualForm1 would be ok, but unlikely

                    //get explictly assigned color, if any
                    Color color = null;
                    String holderName = VIRTUAL_FORM_HOLDER_NAME;
                    String key = ComponentGroupHelper.getComponentGroupColorKey(holderName, name);
                    Object o = dcontext.getContextData(key);
                    String vkey = ComponentGroupHolder.VIRTUAL_FORM_COLOR_KEY_PREFIX + name;
                    boolean attemptLegacyKeyConversion = false;
                    if (o == null) {
                        //see if there's an entry using the old "virtualFormColor:" prefix
                        attemptLegacyKeyConversion = true;
                        o = dcontext.getContextData(vkey);
                    }
                    //now retest o
                    if (o instanceof ColorWrapper) {
                        color = ((ColorWrapper)o).getColor();
                        if (color != null) {
                            //o is good, so attempt legacy conversion if appropriate
                            if (attemptLegacyKeyConversion) {
                                dcontext.setContextData(vkey, null);
                                dcontext.setContextData(key, o);
                            }
                        }
                    } else if (o instanceof String) {
                        ColorWrapper cw = new ColorWrapperImpl((String)o);
                        color = cw.getColor();
                        if (color != null) {
                            dcontext.setContextData(key, cw);
                            //o is good, so attempt legacy conversion if appropriate
                            if (attemptLegacyKeyConversion) {
                                dcontext.setContextData(vkey, null);
                            }
                        }
                    }

                    //get subsets
                    String[] participantArr = vds[v].getParticipatingIds();
                    String[] submitterArr = vds[v].getSubmittingIds();

                    String[][] subsetArrs = {participantArr, submitterArr};

                    ComponentSubset[] componentSubsets = new ComponentSubset[subsetArrs.length];
                    for (int s = 0; s < subsetArrs.length; s++) {
                       String[] subsetArr = subsetArrs[s];
                       componentSubsets[s] = new ComponentSubsetImpl(SUBSET_PROPERTY_NAMES[s], subsetArr, SUBSET_LINE_TYPES[s]);
                    }

                    ComponentGroup group = new VirtualFormGroup(name, color, componentSubsets, vfName);
                    groupList.add(group);
               }
           }
           return groupList.toArray(new ComponentGroup[groupList.size()]);
       }

       public String getToolTip() {
           return bundle.getString("Form.ComponentGroupHolder.tooltip"); //NOI18N
       }
       public String getLegendLabel() {
           return bundle.getString("Form.ComponentGroupHolder.legendLabel"); //NOI18N
       }

        //public Class getAssociatedBeanType() {
        //    return AjaxTransaction.class;
        //}

        public DisplayAction[] getDisplayActions(DesignContext dcontext, DesignBean[] dbeans) {
            DisplayAction virtualFormDisplayAction = null;
            if (dbeans != null && dbeans.length > 0) {
                virtualFormDisplayAction = VirtualFormsHelper.getContextItem(dbeans);
            }
            if (virtualFormDisplayAction == null && dcontext != null) {
                virtualFormDisplayAction = VirtualFormsHelper.getContextItem(dcontext);
            }
            if (virtualFormDisplayAction != null) {
                return new DisplayAction[]{virtualFormDisplayAction};
            }
            return new DisplayAction[0];
        }
    }
    
    private static class VirtualFormGroup extends ComponentGroupImpl {
        private String legendEntryLabel;
        public VirtualFormGroup(String name, Color color, ComponentSubset[] componentSubsets, String legendEntryLabel) {
            super(name, color, componentSubsets);
            this.legendEntryLabel = legendEntryLabel;
        }
        public String getLegendEntryLabel() {
            return this.legendEntryLabel;
        }
    }
    
    /*
    private static class VirtualFormGroup implements ComponentGroup {
        private String name;
        private Color color;
        private ComponentSubset[] componentSubsets;
        //private DesignBean associatedBean;
        
        public VirtualFormGroup(String name, Color color, ComponentSubset[] componentSubsets) {
            this.name = name;
            this.color = color;
            this.componentSubsets = componentSubsets;
            //this.associatedBean = associatedBean;
        }
        
        public String getName() {
            return this.name;
        }
        public Color getColor() {
            return this.color;
        }
        public ComponentSubset[] getComponentSubsets() {
            return this.componentSubsets;
        }
        //public DesignBean getAssociatedBean() {
        //    return this.associatedBean;
        //}
    }
    
    private static class VirtualFormSubset implements ComponentSubset {
        private String name;
        private String[] members;
        private ComponentSubset.LineType lineType;
        public VirtualFormSubset(String name, String[] members, LineType lineType) {
            this.name = name;
            this.members = members;
            this.lineType = lineType;
        }
        
        //e.g., "participants", "submitters", "inputs", "execute", "render"
        public String getName() {
            return this.name;
        }
        
        //e.g., contains participants or submitters in a particular virtual form. contains inputs, executes, or renders of a particular ajax transaction.
        public String[] getMembers() {
            return this.members;
        } 

        //SOLID OR DASHED
        public LineType getLineType() {
            return this.lineType;
        }
    }
     * */
}
