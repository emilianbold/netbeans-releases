/*
 * AdvancedPanel.java
 *
 * Created on August 20, 2008, 11:16 AM
 */

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.awt.event.FocusEvent;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jms.JMSAddress;
import org.netbeans.modules.wsdlextensions.jms.JMSJCAOptions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.util.NbBundle;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author  jalmero
 */
public class JMSAdvancedPanel extends javax.swing.JPanel {

    private WSDLComponent mComponent;

    /** QName **/
    private QName mQName;

    /** resource bundle for file bc **/
    private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.jms.resources.Bundle");

    private static final Logger mLogger = Logger.
            getLogger(JMSAdvancedPanel.class.getName());
    
    private DescriptionPanel descPanel = null;
    
    /** Creates new form JMSBindingConfigurationPanel */
    public JMSAdvancedPanel(QName qName, WSDLComponent component) {
        initComponents();
        populateView(qName, component);
        setAccessibility();
    }    

    /**
     * Sets the content of the text area
     * @param txt
     */
    public void setText(String txt) {
        mTextArea.setText(txt);
        if ((txt != null) && (txt.length() > 0)) {
            mTextArea.setCaretPosition(0);
        }
    }
    
    /**
     * Return the content of the text area
     */
    public String getText() {
        return mTextArea.getText();
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(JMSAdvancedPanel.class, 
                "JMSAdvancedPanel.panelTitle");
    }
    
    /**
     * Populate the view with the given the model component
     * @param qName
     * @param component
     */
    public void populateView(QName qName, WSDLComponent component) {
        mQName = qName;
        mComponent = component;
        resetView();
        populateView(mComponent);
    }    
    
    private void populateView(WSDLComponent component) {
        if (component != null) {
            if (component instanceof JMSAddress) {
                populateAddress((JMSAddress) component);            
            } else if (component instanceof Port) {
                Collection<JMSAddress> address = ((Port) component).
                        getExtensibilityElements(JMSAddress.class);
                if (!address.isEmpty()) {
                    populateAddress(address.iterator().next());
                }
            }
        }
    }
    
    private void populateAddress(JMSAddress jmsAddress) {
        if (jmsAddress != null) {
            List<JMSJCAOptions> jmsjcaOptions =
                    jmsAddress.getExtensibilityElements(JMSJCAOptions.class);
            String optionsStr = null;
            if (jmsjcaOptions.size() > 0) {
                JMSJCAOptions options = jmsjcaOptions.get(0);
                Element e = options.getPeer();
                NodeList nl = e.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node node1 = nl.item(i);
                    if (node1 instanceof CDATASection) {
                        optionsStr = node1.getNodeValue();
                        setText(trimCDataTag(optionsStr));
                        break;
                    }
                }
            }
        }
    }
    
    private void resetView() {
        // uncomment once we have the correct description
        mTextArea.setText("");
    }
    
    private void updateDescriptionArea(FocusEvent evt) {
        if (descPanel != null) {
            descPanel.setText("");
        }

        String[] desc = null;

        if (evt.getSource() == mTextArea) {
            desc = new String[]{"Advanced Options \n\n",
                   mBundle.getString("DESC_Attribute_jndiInfo")}; //NOI18N 
        }
        if (desc != null) {
            if (descPanel != null) {
                descPanel.setText(desc[0], desc[1]);
            }
            return;
        }
    }         

    /**
     * Trims the '<![CDATA[' and ']]>' if exists.  Returns null if blank.
     *
     * @param text
     * @return trimmed text, if blank returns null.
     */
    private String trimCDataTag(String text) {
        if (text == null) {
            return text;
        }
        if ((text.startsWith("<![CDATA[")) && //NOI18N
            (text.endsWith("]]>"))) {  //NOI18N
            int length = text.length();
            return text.substring(9, length - 3);// "hamburger".substring(4, 8) returns "urge"
        }
        return text;
    }
    
    private void setAccessibility() {
        this.getAccessibleContext().setAccessibleName(getName());
        this.getAccessibleContext().setAccessibleDescription(getName());
        mTextArea.getAccessibleContext().setAccessibleName(mBundle.getString("DESC_Attribute_jndiInfo")); // NOI18N
        mTextArea.getAccessibleContext().setAccessibleDescription(mBundle.getString("DESC_Attribute_jndiInfo")); // NOI18N                 
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        mTextArea = new javax.swing.JTextArea();
        descriptionPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout(0, 10));

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 10, 10));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout(0, 10));

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setLabelFor(mTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JMSAdvancedPanel.class, "JMSAdvancedPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 120, 0, 0);
        jPanel1.add(jSeparator1, gridBagConstraints);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        mTextArea.setColumns(20);
        mTextArea.setLineWrap(true);
        mTextArea.setRows(5);
        mTextArea.setName("mTextArea"); // NOI18N
        mTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                mTextAreaFocusGained(evt);
            }
        });
        jScrollPane1.setViewportView(mTextArea);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setTopComponent(jPanel2);

        descriptionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        descriptionPanel.setName("descriptionPanel"); // NOI18N
        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 75));
        descriptionPanel.setLayout(new java.awt.BorderLayout());
        descPanel = new DescriptionPanel();
        descriptionPanel.add(descPanel, java.awt.BorderLayout.CENTER);
        jSplitPane1.setBottomComponent(descriptionPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void mTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mTextAreaFocusGained
// TODO add your handling code here:
    updateDescriptionArea(evt);
}//GEN-LAST:event_mTextAreaFocusGained

private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
// TODO add your handling code here:
    if (mTextArea != null) {
        mTextArea.requestFocusInWindow();
    }
}//GEN-LAST:event_formComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea mTextArea;
    // End of variables declaration//GEN-END:variables

}
