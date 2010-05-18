/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.search;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openidex.search.SearchType;


/**
 * Panel which shows to user one search type allowing it to customize.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see SearchPanel
 */
public final class SearchTypePanel extends JPanel
                                   implements PropertyChangeListener,
                                              ActionListener,
                                              DialogLifetime {

    /** Name of customized property. */
    public static final String PROP_CUSTOMIZED = "customized"; // NOI18N
    /** Modificator suffix.  */
    private static final String MODIFICATOR_SUFFIX = " *"; // NOI18N
    /** Customized property. Indicates this criterion model 
     * was customized by user. */
    private boolean customized;
    /** Search type this model is customized by. */
    private SearchType searchType;
    /**
     * has this panel's customizer been initialized with
     * <code>setObject(...)</code>?
     *
     * @see  #initializeWithObject()
     */
    private boolean initialized = false;
    /** Customizer for search type. */
    final Customizer customizer;
    /** Customizer component. */
    final Component customizerComponent;

    private String lastSavedName;
    
    
    /** Creates new form <code>SearchTypePanel</code>. */
    public SearchTypePanel(SearchType searchType) {
        initComponents();
        initAccessibility();
                
        this.searchType = searchType;

        customizer = createCustomizer(this.searchType);
        if (customizer != null) {
            customizerComponent = (Component) customizer;
        } else {
            customizerComponent = null;
            
            initialized = true;            //cannot initialize <null> customizer
            
            // PENDING use property sheet as it will implement Customizer
            // allow hiding tabs, ....
            System.err.println("No customizer for "                     //NOI18N
                               + this.searchType.getName()
                               + ", skipping...");                      //NOI18N
        }

        customizer.setObject(this.searchType);
        this.searchType.addPropertyChangeListener(this);
        
        ResourceBundle bundle = NbBundle.getBundle(SearchTypePanel.class);
        Mnemonics.setLocalizedText(
                applyCheckBox,
                bundle.getString("TEXT_BUTTON_APPLY"));                 //NOI18N
        
        customizerPanel.add(customizerComponent, BorderLayout.CENTER);

        setCustomized(this.searchType.isValid());
        
        // obtain tab label string & icon
        setName(createName());
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(SearchTypePanel.class);
        this.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_DIALOG_DESC"));                   //NOI18N        
        applyCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_TEXT_BUTTON_APPLY"));             //NOI18N        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        customizerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        customizerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(customizerPanel, gridBagConstraints);

        applyCheckBox.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 11, 0, 11);
        add(applyCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JCheckBox applyCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JPanel customizerPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Called when the <em>Save Settings as...</em> or <em>Restore Saved...</em>
     * button is pressed or when the <em>Use This Criterion for Search</em>
     * checkbox is (de)selected.
     */
    public void actionPerformed(ActionEvent e) {
        final Object source = e.getSource();
        
        if (source == applyCheckBox) {
            
            /* PENDING: Some better solution of valid / customized needed. */
            boolean selected = applyCheckBox.isSelected();
            setCustomized(selected);
            searchType.setValid(selected);
            
        } else {
            
            /* this should never happen */
            assert false;
        }
    }
    
    // PENDING Better solution for these properties are needed.
    /** Listens on search type PROP_VALID property change and sets
     * customized property accordingly. */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == searchType) {

            // if Type fires valid property change listens for
            // its invalidity -> mark itself as unmodified
            if (SearchType.PROP_VALID.equals(evt.getPropertyName()) ) {
                
                if (evt.getNewValue().equals(Boolean.FALSE)) {
                    setCustomized (false);
                    return;
                } else {
                    setCustomized (true);
                }
            }
        }
    }
        
    public void onOk() {
        if (customizer instanceof DialogLifetime) {
            ((DialogLifetime)customizer).onOk();            
        }
    }
    
    public void onCancel() {
        if (customizer instanceof DialogLifetime) {
            ((DialogLifetime)customizer).onCancel();            
        }
    }
    
    /**
     * Creates name used as tab name, 
     * @return name. */
    private String createName() {
        String name = searchType.getName();

        if(customized) {        
            return  name + MODIFICATOR_SUFFIX;
        } else {
            return  name;
        }
    }

    /**
     * Creates a customizer for a given search type. 
     *
     * @param  searchTypeClass  class of the search type
     * @return  customizer object for the search type,
     *          or <code>null</code> if the customizer could not be created
     */
    private static Customizer createCustomizer(final SearchType searchType) {
        final Class searchTypeClass = searchType.getClass();
        Class clazz = null;
        
        if (isDefaultSearchType(searchTypeClass)) {
            String typeClassName = searchType.getClass().getName();
            assert typeClassName.endsWith("Type");                      //NOI18N
            
            int typeNameLen = typeClassName.length();
            String customizerClassName
                    = new StringBuffer(typeNameLen + 6)
                      .append(typeClassName.substring(0, typeNameLen - 4))
                      .append("Customizer")                             //NOI18N
                      .toString();
            try {
                clazz = Class.forName(customizerClassName);
            } catch (Exception ex) {
                assert false;
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        }
        
        if (clazz == null) {
            final BeanInfo beanInfo;
            try {
                beanInfo = Utilities.getBeanInfo(searchTypeClass);
            } catch (IntrospectionException ie) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ie);
                return null;
            }

            clazz = beanInfo.getBeanDescriptor ().getCustomizerClass ();
        }
        if (clazz == null) return null;

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }

        if (!(o instanceof Component) ||
                !(o instanceof Customizer)) return null;
        return (Customizer) o;
    }
    
    /**
     * Checks whether the given <code>SearchType</code> class represents
     * a default search type.
     * (Default search type is such a search type that is defined
     * in the Utilities module.)
     *
     * @param  searchTypeClass  <code>SearchType</code> class to check
     * @return  <code>true</code> if the given <code>Class</code> object
     *          represents a default search type; <code>false</code> if not
     */
    private static boolean isDefaultSearchType(Class searchTypeClass) {
        assert SearchType.class.isAssignableFrom(searchTypeClass);

        String mandatoryPackage = "org.netbeans.modules.search.types";  //NOI18N
        
        String className = searchTypeClass.getName();
        return className.startsWith(mandatoryPackage)
               && (className.lastIndexOf('.') == mandatoryPackage.length());
    }
    
    /**
     * Sets customized property.
     *
     * @param cust value to which customized property to set.
     */
    private void setCustomized(boolean cust) {
        customized = cust;

        applyCheckBox.setSelected(customized);

        setName(createName());

        firePropertyChange(PROP_CUSTOMIZED, !cust, cust);
    }

    /** Tests whether this panel is customized. */
    public boolean isCustomized() {
        return customized;
    }

    /** Restores the search type. */
    private void restoreSearchType(SearchType searchType) {
        this.searchType.removePropertyChangeListener(this);

        this.searchType = (SearchType) searchType.clone();
        initialized = false;
        initializeWithObject();
        this.searchType.addPropertyChangeListener(this);

        setCustomized(true);
    }    
    
    /**
     * Initializes this panel's customizer using <code>setObject(...)</code>,
     * if it has not been initialized yet.
     */
    final void initializeWithObject() {
        if (!initialized) {
            customizer.setObject(this.searchType);
            initialized = true;
        }
    }

    /** Return currently hold bean. */
    public SearchType getSearchType() {
        return searchType;
    }
    
    /**
     * Class equality
     *
     * @return this.bean.getClass().equals(bean.getClass());
     */
    public boolean equals(Object obj) {
        try {
            return searchType.getClass().equals(
                    ((SearchTypePanel) obj).getSearchType().getClass());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /** Gets help context. */
    public HelpCtx getHelpCtx() {
        return searchType.getHelpCtx();
    }
    
}
