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

    /**
     * <p>The name for any <code>ComponentGroupHolder</code> that holds virtual forms.</p>
     */ 
    public static final String VIRTUAL_FORM_HOLDER_NAME = Form.class.getName();   //NOI18N
    /**
     * <p><code>String</code> version of the <code>NamingContainer.SEPARATOR_CHAR</code>, used in qualified ids.</p>
     */ 
    private static final String ID_SEP = String.valueOf(NamingContainer.SEPARATOR_CHAR);
    /**
     * <p>The component subset names in a component group representing a virtual form.</p>
     */ 
    private static final String[] SUBSET_NAMES = {"participants", "submitters"}; // NOI18N
    /**
     * <p>The line types corresponding to the subset names.</p>
     */ 
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
            Object currentBeanInstance = currentBean.getInstance();
            if (currentBeanInstance instanceof UIComponent) {
                sb.insert(0, ID_SEP);
                if (currentBeanInstance instanceof Form) {
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
    
    /**
     * <p>Add to the design context data a <code>ComponentGroupHolder</code> 
     * representing the virtual forms on the page, if one is not already 
     * present.</p> 
     */ 
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
    
    /**
     * <p>Setup logic when a <code>Form</code> is dragged from the palette.</p>
     */ 
    public Result beanCreatedSetup(DesignBean bean) {
        registerComponentGroupHolderIfNecessary(bean);
        return Result.SUCCESS;
    }
    
    /**
     * <p>Logic invoked when the designer view of the page is activated.</p>
     */ 
    public void beanContextActivated(DesignBean bean) {
        registerComponentGroupHolderIfNecessary(bean);
    }
    
    /**
     * <p>A <code>ComponentGroupHolder</code> implementation representing 
     * the virtual forms on the page. The registered instance will be used
     * by the designer when painting component group borders and legends.</p>
     */ 
    private static class VirtualFormHolder implements ComponentGroupHolder {
       private static ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-DT",
                               Locale.getDefault(),
                               VirtualFormHolder.class.getClassLoader());
       
       /**
        * <p>Get the name of the holder.</p>
        */ 
       public String getName() {
           return VIRTUAL_FORM_HOLDER_NAME;
       }

       /**
        * <p>Create and return <code>ComponentGroup</code> instances 
        * representing the virtual forms on the page. First, find all 
        * <code>Form</code> components on the page and their respective 
        * virtual form descriptors.
        * For each descriptor, retrieve the color associated with the virtual
        * form in question, if one was stored in the context data. Then create
        * <code>ComponentSubsetImpl</code> instances for the participants and
        * submitters of the virtual form in question. Finally, use the subset
        * instances, color, and other information to create a 
        * <code>ComponentGroup</code> for the virtual form in question.</p>
        */ 
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
                       componentSubsets[s] = new ComponentSubsetImpl(SUBSET_NAMES[s], subsetArr, SUBSET_LINE_TYPES[s]);
                    }

                    ComponentGroup group = new VirtualFormGroup(name, color, componentSubsets, vfName);
                    groupList.add(group);
               }
           }
           return groupList.toArray(new ComponentGroup[groupList.size()]);
       }

       /**
        * <p>Get the tooltip for the virtual forms toolbar button in the 
        * designer.</p>
        */ 
       public String getToolTip() {
           return bundle.getString("Form.ComponentGroupHolder.tooltip"); //NOI18N
       }

       /**
        * <p>Get the label for the virtual forms legend.</p>
        */ 
       public String getLegendLabel() {
           return bundle.getString("Form.ComponentGroupHolder.legendLabel"); //NOI18N
       }

       /**
        * Get the virtual forms context menu item as a <code>DisplayAction[]</code>
        * with at most one member.</p>
        */ 
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
    
    /**
     * <p>Extends <code>ComponentGroupImpl</code> to override 
     * <code>getLegendEntryLabel</code>, since the name of a <code>VirtualFormGroup</code>
     * is <code>formName.vfName</code>, while the legend entry label is simply
     * the virtual form name.</p>
     */ 
    private static class VirtualFormGroup extends ComponentGroupImpl {
        private String legendEntryLabel;
        
        /**
         * <p>Constructor that accepts an explicit <code>legendEntryLabel</code>.</p>
         */ 
        public VirtualFormGroup(String name, Color color, ComponentSubset[] componentSubsets, String legendEntryLabel) {
            super(name, color, componentSubsets);
            this.legendEntryLabel = legendEntryLabel;
        }
        
        /**
         * <p>Get the legend entry label. This will simply be the virtual form name.</p>
         */ 
        public String getLegendEntryLabel() {
            return this.legendEntryLabel;
        }
    }
}
