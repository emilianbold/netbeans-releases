/*
 * SingleElementCustomizationPanel.java
 *
 * Created on March 28, 2008, 11:24 AM
 */
package org.netbeans.modules.uml.diagrams.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.options.ColorComboBox;
import org.netbeans.modules.uml.diagrams.options.PreviewScene;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author  treyspiva
 */
public class SingleElementCustomizationPanel extends JPanel implements ItemListener
{
    private Widget activeWidget = null;
    private IPresentationElement element = null;
    private PreviewScene scene = new PreviewScene();
    private CustomizableListModel widgetModel = null;
    private Widget old = null;
    
    /** Creates new form SingleElementCustomizationPanel */
    public SingleElementCustomizationPanel()
    {
        initComponents();
        
        previewPane.setViewportView(scene.createView());
        ColorComboBox.init(fgComboBox);
        ColorComboBox.init(bgComboBox);
        
    }

    public void setWidget(IPresentationElement element)
    {
        setWidget(scene.addNode(element), element); 
    }
    
    public void setWidget(Widget widget, IPresentationElement element)
    {
        activeWidget = widget;
        this.element = element;
        
        scene.initializeResources(widget.getResourceTable()); 
        
        Widget node = scene.addNode(element);
        node.setPreferredLocation(new Point(50, 50));
        
        updateWidgetList(widget);

    }
    
    public void save()
    {
        widgetModel.save();
    }
    
    private void updateWidgetList(Widget w)
    {
        widgetModel = new CustomizableListModel();
        
        widgetList.setModel(widgetModel);
        if (widgetModel.getSize() == 0)
        {
            setResourceValues(null);
        }
        else
        {
            widgetList.setSelectedIndex(0);
            setResourceValues((Customizable)widgetList.getSelectedValue());
        }
    }
    
    private void setResourceValues(Customizable cw)
    {
        enableResourceFields(cw);
        
        ResourceValue value = widgetModel.getResource(cw);
        if (value == null)
        {
            fontField.setText("");
            ColorComboBox.init(fgComboBox);
            ColorComboBox.init(bgComboBox);
            return;
        }
        
        fgComboBox.removeItemListener(this);
        bgComboBox.removeItemListener(this); 
       
        fontField.setText(fontField.isEnabled() ? fontToString(value.getFont()) : "");

        if (fgComboBox.isEnabled())
        {
            Color fg = value.getFGColor();
            if (fg == null)
            {
                ColorComboBox.setInheritedColor(fgComboBox, null);
                fgComboBox.setSelectedIndex(fgComboBox.getItemCount() - 1);
            } else
            {
                fgComboBox.setSelectedItem(new ColorValue(fg)); 
            }
        } else
        {
            ColorComboBox.init(fgComboBox);
        }
        if (bgComboBox.isEnabled())
        {
            Color bg = value.getBGColor();
            if (bg == null)
            {
                ColorComboBox.setInheritedColor(bgComboBox, null);
                bgComboBox.setSelectedIndex(bgComboBox.getItemCount() - 1);
            } else
            {
                bgComboBox.setSelectedItem(new ColorValue(bg)); 
            }
        } else
        {
            ColorComboBox.init(bgComboBox);
        }
        fgComboBox.addItemListener(this); 
        bgComboBox.addItemListener(this); 
    }
    
    private void enableResourceFields(Customizable c)
    {
        List<ResourceType> list = new ArrayList<ResourceType>();
        
        if (c!=null)
        {
            ResourceType[] types = c.getCustomizableResourceTypes();
            list = Arrays.asList(types); 
        }
        this.bgComboBox.setEnabled(list.contains(ResourceType.BACKGROUND)); 
        this.bgcolorLbl.setEnabled(list.contains(ResourceType.BACKGROUND)); 
        this.fgComboBox.setEnabled(list.contains(ResourceType.FOREGROUND)); 
        this.fgcolorLbl.setEnabled(list.contains(ResourceType.FOREGROUND)); 
        this.fontField.setEnabled(list.contains(ResourceType.FONT)); 
        this.fontBtn.setEnabled(list.contains(ResourceType.FONT)); 
        this.fontLbl.setEnabled(list.contains(ResourceType.FONT)); 
    }
    
    private String fontToString(Font f)
    {
        if (f == null)
        {
            return "Inherited";
        }
        String strStyle;

        if (f.isBold())
        {
            strStyle = f.isItalic() ? "bolditalic" : "bold";
        } else
        {
            strStyle = f.isItalic() ? "italic" : "plain";
        }
        return f.getName() + " " + strStyle + " " + f.getSize() + " ";
    }

    private String loc(String key)
    {
        return NbBundle.getMessage(SingleElementCustomizationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        previewPane = new javax.swing.JScrollPane();
        widgetLbl = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        widgetList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        fontLbl = new javax.swing.JLabel();
        fontField = new javax.swing.JTextField();
        fontBtn = new javax.swing.JButton();
        fgcolorLbl = new javax.swing.JLabel();
        fgComboBox = new javax.swing.JComboBox();
        bgcolorLbl = new javax.swing.JLabel();
        bgComboBox = new javax.swing.JComboBox();

        widgetLbl.setLabelFor(widgetList);
        org.openide.awt.Mnemonics.setLocalizedText(widgetLbl, org.openide.util.NbBundle.getMessage(SingleElementCustomizationPanel.class, "SingleElementCustomizationPanel.widgetLbl.text")); // NOI18N

        widgetList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        widgetList.setCellRenderer(new org.netbeans.modules.uml.diagrams.options.CompartmentRenderer());
        widgetList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                widgetListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(widgetList);

        jLabel1.setLabelFor(previewPane);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(SingleElementCustomizationPanel.class, "SingleElementCustomizationPanel.jLabel1.text_1")); // NOI18N

        fontLbl.setLabelFor(fontField);
        org.openide.awt.Mnemonics.setLocalizedText(fontLbl, org.openide.util.NbBundle.getMessage(SingleElementCustomizationPanel.class, "SingleElementCustomizationPanel.fontLbl.text")); // NOI18N

        fontField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontFieldActionPerformed(evt);
            }
        });

        fontBtn.setText(org.openide.util.NbBundle.getMessage(SingleElementCustomizationPanel.class, "SingleElementCustomizationPanel.fontBtn.text")); // NOI18N
        fontBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontBtnActionPerformed(evt);
            }
        });

        fgcolorLbl.setLabelFor(fgComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(fgcolorLbl, org.openide.util.NbBundle.getMessage(SingleElementCustomizationPanel.class, "SingleElementCustomizationPanel.fgcolorLbl.text")); // NOI18N

        bgcolorLbl.setLabelFor(bgComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(bgcolorLbl, org.openide.util.NbBundle.getMessage(SingleElementCustomizationPanel.class, "SingleElementCustomizationPanel.bgcolorLbl.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(previewPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fgcolorLbl)
                            .add(fontLbl)
                            .add(bgcolorLbl))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(fgComboBox, 0, 148, Short.MAX_VALUE)
                            .add(fontField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                            .add(bgComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fontBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(widgetLbl)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(widgetLbl)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fontLbl)
                            .add(fontBtn)
                            .add(fontField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fgcolorLbl)
                            .add(fgComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(bgcolorLbl)
                            .add(bgComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(previewPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    ///////////////////////////////////////////////////////////////////////////
    // Listener Methods
    
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getSource() == bgComboBox)
        {
            Color bgcolor = ColorComboBox.getColor(bgComboBox);
            Widget widget = (Widget) widgetList.getSelectedValue();
            if (widget != null)
            {
                if (widget instanceof Customizable)
                {
                    Customizable c = (Customizable) widget;
                    String id = c.getID();
                    scene.getResourceTable().addProperty(id + "." + ResourceValue.BGCOLOR, bgcolor);
                }
                else
                {
                    widget.setBackground(bgcolor);
                }

                scene.validate();
                ResourceValue v = widgetModel.getResource(widget);
                if (v != null)
                {
                    v.setBGColor(bgcolor);
                }
            }
        }
        else if (e.getSource() == fgComboBox)
        {
            Color fgcolor = ColorComboBox.getColor(fgComboBox);
            Widget widget = (Widget) widgetList.getSelectedValue();
            if (widget != null)
            {
                if (widget instanceof Customizable)
                {
                    Customizable c = (Customizable) widget;
                    String id = c.getID();
                    scene.getResourceTable().addProperty(id + "." + ResourceValue.FGCOLOR, fgcolor);
                }
                else
                {
                    widget.setForeground(fgcolor);
                }

                scene.validate();
                ResourceValue v = widgetModel.getResource(widget);
                if (v != null)
                {
                    v.setFGColor(fgcolor);
                }
            }
        }
    }
    
private void widgetListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_widgetListValueChanged
    Widget widget = (Widget) widgetList.getSelectedValue();
    if (widget == old || widget == null)
    {
        return;
    }

    old = widget;
//    if (!blink)
//    {
//        return;
//    }
//
//    startBlinking(widget);
    setResourceValues((Customizable) widget);
}//GEN-LAST:event_widgetListValueChanged

private void fontFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontFieldActionPerformed
//    Widget widget = (Widget) widgetList.getSelectedValue();
//    if (fontField.getText().trim().equals(""))
//    {
//        if (widget instanceof Customizable)
//        {
//            Customizable c = (Customizable) widget;
//            String id = c.getID();
//            scene.getResourceTable().addProperty(id + ResourceValue.FONT, null);
//        }
//        else
//        {
//            widget.setFont((Font) null);
//        }
//
//        ResourceValue v = map.get(widget);
//        if (v != null)
//        {
//            v.setFont(null);
//            fontField.setText(fontToString(null));
//        }
//    }
}//GEN-LAST:event_fontFieldActionPerformed

private void fontBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontBtnActionPerformed
    PropertyEditor pe = PropertyEditorManager.findEditor(Font.class);
    Widget widget = (Widget) widgetList.getSelectedValue();
    Font f = widget.getFont();
    pe.setValue(f);
    
    DialogDescriptor dd = new DialogDescriptor(pe.getCustomEditor(), loc("CTL_Font_Chooser"));
    DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    if (dd.getValue() == DialogDescriptor.OK_OPTION)
    {
        f = (Font) pe.getValue();
        fontField.setText(fontToString(f));

        if (widget instanceof Customizable)
        {
            Customizable c = (Customizable) widget;
            String id = c.getID();
            scene.getResourceTable().addProperty(id + "." + ResourceValue.FONT, f);
        }
        else
        {
            widget.setFont(f);
        }

        scene.validate();
        ResourceValue v = widgetModel.getResource(widget);
        if (v != null)
        {
            v.setFont(f);
        }
    }
}//GEN-LAST:event_fontBtnActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bgComboBox;
    private javax.swing.JLabel bgcolorLbl;
    private javax.swing.JComboBox fgComboBox;
    private javax.swing.JLabel fgcolorLbl;
    private javax.swing.JButton fontBtn;
    private javax.swing.JTextField fontField;
    private javax.swing.JLabel fontLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane previewPane;
    private javax.swing.JLabel widgetLbl;
    private javax.swing.JList widgetList;
    // End of variables declaration//GEN-END:variables

    public class CustomizableListModel extends AbstractListModel
    {
        List < Customizable > widgets = new ArrayList < Customizable >();
        List < ResourceValue > resources = new ArrayList < ResourceValue >();
        
        public CustomizableListModel()
        {
            gatherCustomizableChildren(activeWidget);
        }

        public int getSize()
        {
            return widgets.size();
        }

        public Object getElementAt(int index)
        {
            return widgets.get(index);
        }
        
        public ResourceValue getResource(int index)
        {
            return resources.get(index);
        }
        
        public ResourceValue getResource(Customizable widget)
        {
            int index = widgets.indexOf(widget);
            return getResource(index);
        }
        
        public ResourceValue getResource(Widget widget)
        {
            ResourceValue retVal = null;
            
            if (widget instanceof Customizable)
            {
                Customizable c = (Customizable) widget;
                retVal = getResource(c);
            }
            
            return retVal;
        }
        
        private void gatherCustomizableChildren(Widget w)
        {
            for (Widget child : w.getChildren())
            {
                if (child instanceof Customizable)
                {
                    Customizable c = (Customizable)child;
                    widgets.add(c);
                    resources.add(ResourceValue.getResources(c.getID(), child.getResourceTable()));
                }
                
                gatherCustomizableChildren(child);
            }
        }
        
        public void save()
        {
            for(Customizable customizable : widgets)
            {
                if (customizable instanceof Widget)
                {
                    Widget widget = (Widget) customizable;
                    ResourceTable table = widget.getResourceTable();
                    
                    ResourceValue value = getResource(customizable);
                    value.save(customizable.getID(), table);
                }

            }
        }
    }
}
