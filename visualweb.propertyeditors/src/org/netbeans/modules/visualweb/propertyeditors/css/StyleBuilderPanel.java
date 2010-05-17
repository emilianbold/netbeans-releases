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
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleParser;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignContext;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Style Builder main panel
 * @author  Winston Prakash
 */
public class StyleBuilderPanel extends JPanel implements PropertyChangeListener{
    JPanel currentEditor;
    String currentStyle = null;
    private CssStyleData cssStyleData = new CssStyleData();
    private PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);
    
    DesignProperty designProperty = null;
    Image previewImage = null;
    
    StyleEditorListPanel styleEditorListPanel = null;
    
    List styleEditorList = new ArrayList();
    String noPreviewLabel = NbBundle.getMessage(StyleBuilderPanel.class, "NO_PREVIEW");
    
    /** Creates new form StyleBuilderPanel */
    public StyleBuilderPanel() {
        initComponents();
        initialize();
    }
    
    public StyleBuilderPanel(String styleString, DesignProperty liveProperty) {
        initComponents();
        this.designProperty = liveProperty;
        CssStyleParser styleParser = new CssStyleParser(cssStyleData,liveProperty);
        cssStyleData = styleParser.parse(styleString);
        currentStyle = cssStyleData.getStyleValue();
        initialize();
        styleTextArea.setText(currentStyle);
        displayPreviewImage();
    }
    
    public StyleBuilderPanel(String styleString) {
        this(styleString,null);
    }
    
    private void initialize(){
        styleEditorListPanel = new StyleEditorListPanel(this, cssStyleData);
        
        styleEditorList.add(new FontStyleEditor(cssStyleData));
        BackgroundStyleEditor bgStyleEditor = new BackgroundStyleEditor(cssStyleData);
        bgStyleEditor.setDesignProperty(designProperty);
        styleEditorList.add(bgStyleEditor);
        styleEditorList.add(new TextBlockStyleEditor(cssStyleData));
        styleEditorList.add(new BorderStyleEditor(cssStyleData));
        styleEditorList.add(new MarginStyleEditor(cssStyleData));
        //styleEditorList.add(new ListStyleEditor());
        styleEditorList.add(new PositionStyleEditor(cssStyleData));
        //styleEditorList.add(new OtherStyleEditor());
        
        for(int i=0; i< styleEditorList.size(); i++){
            StyleEditor styleEitor = (StyleEditor)styleEditorList.get(i);
            styleEditorListPanel.addEditor(styleEitor);
        }
        styleEditorListPanel.setSelectedEditor((StyleEditor)styleEditorList.get(0));
        setEditorListPanel(styleEditorListPanel);
        cssStyleData.addCssPropertyChangeListener(this);
        /*previewImage = new BufferedImage(200,50,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D)previewImage.getGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(5,5,190,40);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("This is a test Text",(float)50.,(float)30.);
        if(previewImage != null){
            previewPanel.setPreferredSize(new Dimension(previewImage.getWidth(this)+10,previewImage.getHeight(this)+10));
        }*/
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        styleTextArea.setText(cssStyleData.getStyleValue());
        propertyChangeSupport.firePropertyChange("style", currentStyle, cssStyleData.getStyleValue()); //NOI18N
        currentStyle = cssStyleData.getStyleValue();
        displayPreviewImage();
    }
    
    private void displayPreviewImage(){
        if(designProperty != null){
            try{
                MarkupDesignBean liveBean = (MarkupDesignBean)designProperty.getDesignBean();
                MarkupDesignContext liveContext = (MarkupDesignContext) liveBean.getDesignContext();
                Dimension viewportDim = previewScrollPane.getViewport().getViewSize();
                previewImage = liveContext.getCssPreviewImage(currentStyle,null,liveBean,(int)viewportDim.getWidth(),(int)viewportDim.getHeight());
                if(previewImage != null){
                    previewPanel.setPreferredSize(new Dimension(previewImage.getWidth(this)+10,previewImage.getHeight(this)+10));
                }
                repaint();
            }catch(Exception exc){
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
            }
        }else{
            
        }
    }
    /**
     * Adds a PropertyChangeListener to the listener list.
     * The property change listener is added to the CssStyleData
     * @param listener The listener to add.
     */
    public void addCssPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * The property change listener is removed from the CssStyleData
     * @param listener The listener to remove.
     */
    public void removeCssPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Get the Style property string.
     * Constructed from the CssStyleData Structure
     */
    public String getStyleString(){
        return cssStyleData.toString();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainSplitPane = new javax.swing.JSplitPane();
        styleStringPanel = new javax.swing.JPanel();
        styleStringScroll = new javax.swing.JScrollPane();
        styleTextArea = new javax.swing.JTextArea();
        styleLabel = new javax.swing.JLabel();
        styleEditorSplitPane = new javax.swing.JSplitPane();
        editorListPanel = new javax.swing.JPanel();
        editorSplitPane = new javax.swing.JSplitPane();
        previewScrollPane = new javax.swing.JScrollPane();
        previewPanel = new PreviewPanel();
        previewScrollPane1 = new javax.swing.JScrollPane();
        editorPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(1.0);
        mainSplitPane.setPreferredSize(new java.awt.Dimension(400, 650));

        styleStringPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        styleStringPanel.setPreferredSize(new java.awt.Dimension(375, 100));
        styleStringPanel.setLayout(new java.awt.GridBagLayout());

        styleStringScroll.setPreferredSize(new java.awt.Dimension(300, 100));

        styleTextArea.setColumns(50);
        styleTextArea.setLineWrap(true);
        styleTextArea.setRows(3);
        styleTextArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
        styleTextArea.setMinimumSize(new java.awt.Dimension(110, 75));
        styleTextArea.setPreferredSize(new java.awt.Dimension(410, 75));
        styleTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                styleTextAreaFocusLost(evt);
            }
        });
        styleStringScroll.setViewportView(styleTextArea);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/css/Bundle"); // NOI18N
        styleTextArea.getAccessibleContext().setAccessibleName(bundle.getString("STYLE_EDITOR_STYLE_TEXT_ACCESSIBLE_NAME")); // NOI18N
        styleTextArea.getAccessibleContext().setAccessibleDescription(bundle.getString("STYLE_EDITOR_STYLE_TEXT_ACCESSIBLE_DESC")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        styleStringPanel.add(styleStringScroll, gridBagConstraints);

        styleLabel.setLabelFor(styleTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(styleLabel, org.openide.util.NbBundle.getMessage(StyleBuilderPanel.class, "CSS_STYLE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        styleStringPanel.add(styleLabel, gridBagConstraints);

        mainSplitPane.setBottomComponent(styleStringPanel);

        editorListPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        editorListPanel.setLayout(new java.awt.BorderLayout());
        styleEditorSplitPane.setLeftComponent(editorListPanel);

        editorSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        editorSplitPane.setResizeWeight(1.0);

        previewScrollPane.setAutoscrolls(true);
        previewScrollPane.setDoubleBuffered(true);
        previewScrollPane.setPreferredSize(new java.awt.Dimension(400, 150));

        previewPanel.setLayout(new java.awt.GridBagLayout());
        previewScrollPane.setViewportView(previewPanel);

        editorSplitPane.setBottomComponent(previewScrollPane);

        previewScrollPane1.setAutoscrolls(true);
        previewScrollPane1.setDoubleBuffered(true);
        previewScrollPane1.setPreferredSize(new java.awt.Dimension(400, 350));

        editorPanel.setLayout(new java.awt.BorderLayout());
        previewScrollPane1.setViewportView(editorPanel);

        editorSplitPane.setTopComponent(previewScrollPane1);

        styleEditorSplitPane.setRightComponent(editorSplitPane);

        mainSplitPane.setTopComponent(styleEditorSplitPane);

        add(mainSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void styleTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_styleTextAreaFocusLost
        propertyChangeSupport.firePropertyChange("style", currentStyle, styleTextArea.getText()); //NOI18N
    }//GEN-LAST:event_styleTextAreaFocusLost
    
    /**
     * Set the editor List Panel
     **/
    public void setEditorListPanel(JPanel panel){
        editorListPanel.add(panel,BorderLayout.CENTER);
        repaint();
    }
    
    /**
     * Set the Editor Panel. It is the responsibility of
     * the editor list panel to set the editor when the
     * corresponding editor is selected from its list
     **/
    public void setEditorPanel(JPanel panel){
        //editorPanel.removeAll();
        if(currentEditor != null) {
            editorPanel.remove(currentEditor);
        }
        currentEditor = panel;
        editorPanel.add(currentEditor,BorderLayout.CENTER);
        validate();
        repaint();
    }
    
    class PreviewPanel extends JPanel{
        public PreviewPanel(){
            
        }
        
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2d = (Graphics2D) graphics;
            if(previewImage != null){
                int imgX = 3;
                int imgY = 3;
                int imgWidth = previewImage.getWidth(this);
                int imgHeight = previewImage.getHeight(this);
                if(imgWidth < this.getWidth()){
                    imgX =  (getWidth() - imgWidth)/2;
                    imgY =  (getHeight() - imgHeight)/2;
                }
                g2d.drawImage(previewImage, imgX, imgY,this);
            }else{
                FontRenderContext frc = g2d.getFontRenderContext();
                Rectangle2D bounds = g2d.getFont().getStringBounds(noPreviewLabel,frc);
                int labelX =  (getWidth() - (int)bounds.getWidth())/2;
                int labelY =  (getHeight() - (int)bounds.getHeight())/2;
                g2d.drawString(noPreviewLabel,labelX,labelY);
            }
        }
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editorListPanel;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JSplitPane editorSplitPane;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JScrollPane previewScrollPane;
    private javax.swing.JScrollPane previewScrollPane1;
    private javax.swing.JSplitPane styleEditorSplitPane;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JPanel styleStringPanel;
    private javax.swing.JScrollPane styleStringScroll;
    private javax.swing.JTextArea styleTextArea;
    // End of variables declaration//GEN-END:variables
    
}
