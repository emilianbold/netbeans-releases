/*
 * ElementCustomizationPanel.java
 *
 * Created on February 27, 2008, 1:44 PM
 */
package org.netbeans.modules.uml.diagrams.options;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Sheryl Su
 */
public class ElementCustomizationPanel extends JPanel implements ItemListener
{

    private Map<Customizable, ResourceValue> map = new HashMap<Customizable, ResourceValue>();
    private Widget old = null;
    private PreviewScene scene;
    private IPresentationElement[] elements;
    private int selectedWidgetIndex = 0;
    private int selectedTypeIndex = 0;
    private Widget blinkWidget ;

    public ElementCustomizationPanel()
    {
        initComponents();

        ColorComboBox.init(fgComboBox);
        ColorComboBox.init(bgComboBox);
        fgComboBox.addItemListener(this);
        bgComboBox.addItemListener(this);

        typeComboBox.setRenderer(new ElementTypeWidgetComboBoxRenderer());
        widgetList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
        widgetList.setVisibleRowCount(3);
        widgetList.setCellRenderer(new CompartmentRenderer());

        scene = new PreviewScene();
        previewPane.setViewportView(scene.createView());
        blinkWidget = new Widget(scene);
        scene.addChild(blinkWidget);
    }

    public void clear()
    {
        map.clear();
        selectedWidgetIndex = widgetList.getSelectedIndex();
        selectedTypeIndex = typeComboBox.getSelectedIndex();
        Collection<IPresentationElement> nodes = scene.getNodes();
        IPresentationElement[] a = new IPresentationElement[nodes.size()];
        nodes.toArray(a);
        for (IPresentationElement node : a)
        {
            scene.removeNodeWithEdges(node);
        }
        blinkWidget.setBorder(BorderFactory.createEmptyBorder());
    }

    public void save()
    {
        for (Customizable v : map.keySet())
        {
            map.get(v).save(v.getID());
        }
        ResourceValue.save();
        clear();
    }

    public Scene getScene()
    {
        return scene;
    }

    public  void init(IPresentationElement[] elements)
    {
        init(elements, false);
    }
    
    protected void init(IPresentationElement[] elements, boolean markAsDirty)
    {
        if (elements.length == 0)
        {
            return;
        }
        this.elements = elements;
        Widget[] widgets = new Widget[elements.length + 1];
        int i = 1;
        widgets[0] = scene;
        for (IPresentationElement e : elements)
        {
            Widget w = scene.addNode(e);
//            if(w != null)
//            {
                widgets[i++] = w;
//            }
//            else
//            {
//                scene.removeNode(e);
//            }
        }       
        Arrays.sort(widgets, new WidgetComparator());
        DefaultComboBoxModel model = new DefaultComboBoxModel(widgets);
        typeComboBox.setModel(model);

        List<Customizable> list = new ArrayList<Customizable>();
        getAllCustomizableChildren(list, scene);
        for (Customizable c : list)
        {
            ResourceValue value = ResourceValue.getResources(c.getID(), scene.getResourceTable());
            value.setDirty(markAsDirty);
            map.put(c, value);
        }
        
        ResourceValue sceneValue = ResourceValue.getResources(scene.getID(), scene.getResourceTable());
        sceneValue.setDirty(markAsDirty);
        map.put(scene, sceneValue);
                
        scene.validate();
        widgetList.setSelectedIndex(selectedWidgetIndex);
        typeComboBox.setSelectedIndex(selectedTypeIndex);
        setResourceValues((Customizable)widgetList.getSelectedValue());
        updateWidgetList((Widget) typeComboBox.getSelectedItem());

        blinkWidget.bringToFront();
        // display all the customizable widgets on a scene
        if (selectedTypeIndex == 0)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    GraphLayout gLayout = GraphLayoutFactory.createHierarchicalGraphLayout(scene, true);
                    gLayout.layoutGraph(scene);
                }
            });
        }
    }

    private void hideNodes()
    {
        for (IPresentationElement child : scene.getNodes())
        {
            Widget widget = scene.findWidget(child);
            if (widget != null)
            {
                widget.setVisible(false);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeComboBox = new javax.swing.JComboBox();
        typeLbl = new javax.swing.JLabel();
        widgetLbl = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        widgetList = new javax.swing.JList();
        previewLbl = new javax.swing.JLabel();
        previewPane = new javax.swing.JScrollPane();
        restoreBtn = new javax.swing.JButton();
        fontLbl = new javax.swing.JLabel();
        fontField = new javax.swing.JTextField();
        fontBtn = new javax.swing.JButton();
        fgcolorLbl = new javax.swing.JLabel();
        fgComboBox = new javax.swing.JComboBox();
        bgcolorLbl = new javax.swing.JLabel();
        bgComboBox = new javax.swing.JComboBox();

        typeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                typeComboBoxItemStateChanged(evt);
            }
        });

        typeLbl.setLabelFor(typeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(typeLbl, org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.typeLbl.text")); // NOI18N

        widgetLbl.setLabelFor(widgetList);
        org.openide.awt.Mnemonics.setLocalizedText(widgetLbl, org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.widgetLbl.text")); // NOI18N

        widgetList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        widgetList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                widgetListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(widgetList);

        org.openide.awt.Mnemonics.setLocalizedText(previewLbl, org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.previewLbl.text")); // NOI18N

        restoreBtn.setText(org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.restoreBtn.text")); // NOI18N
        restoreBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreBtnActionPerformed(evt);
            }
        });

        fontLbl.setLabelFor(fontBtn);
        org.openide.awt.Mnemonics.setLocalizedText(fontLbl, org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.fontLbl.text")); // NOI18N

        fontField.setText(org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.fontField.text")); // NOI18N
        fontField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontFieldActionPerformed(evt);
            }
        });

        fontBtn.setText(org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.fontBtn.text")); // NOI18N
        fontBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontBtnActionPerformed(evt);
            }
        });

        fgcolorLbl.setLabelFor(fgComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(fgcolorLbl, org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.fgcolorLbl.text")); // NOI18N

        bgcolorLbl.setLabelFor(bgComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(bgcolorLbl, org.openide.util.NbBundle.getMessage(ElementCustomizationPanel.class, "ElementCustomizationPanel.bgcolorLbl.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, previewLbl)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(typeLbl)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(typeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 106, Short.MAX_VALUE)
                        .add(restoreBtn))
                    .add(layout.createSequentialGroup()
                        .add(previewPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(layout.createSequentialGroup()
                                        .add(bgcolorLbl)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(bgComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(fgcolorLbl)
                                            .add(fontLbl))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(fontField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(fgComboBox, 0, 122, Short.MAX_VALUE))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fontBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(widgetLbl))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLbl)
                    .add(restoreBtn)
                    .add(typeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previewLbl)
                    .add(widgetLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fontLbl)
                            .add(fontBtn)
                            .add(fontField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fgcolorLbl)
                            .add(fgComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(bgcolorLbl)
                            .add(bgComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, previewPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void typeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_typeComboBoxItemStateChanged
        Widget w = (Widget) typeComboBox.getSelectedItem();
        prepareDisplay(w);
}//GEN-LAST:event_typeComboBoxItemStateChanged

    private void prepareDisplay(Widget w)
    {
        if (w instanceof Scene)
        {
            for (IPresentationElement child : scene.getNodes())
            {
                child.getFirstSubjectsType();
                Widget widget = scene.findWidget(child);
                if (widget != null)
                {
                    widget.setVisible(true);
                }
            } 
            GraphLayout gLayout = GraphLayoutFactory.createHierarchicalGraphLayout(scene, true);
            gLayout.layoutGraph(scene);
        } else
        {
            // hide other node widgets
            hideNodes();           
            w.setPreferredLocation(new Point(50, 50));
        }
        w.setVisible(true);
        scene.validate();
        updateWidgetList(w);
    }

    private void widgetListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_widgetListValueChanged

        Widget widget = (Widget) widgetList.getSelectedValue();
        if (widget == old || widget == null)
        {
            return;
        }

        old = widget;
        if (!blink)
        {
            return;
        }
        
        startBlinking(widget);
        setResourceValues((Customizable)widget);
}//GEN-LAST:event_widgetListValueChanged

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
            
            if(widget instanceof Customizable)
            {
                Customizable c = (Customizable)widget;
                String id = c.getID();
                scene.getResourceTable().addProperty(id + "." + ResourceValue.FONT, f);
            }
            else
            {
                widget.setFont(f);
            }
            
            scene.validate();
            ResourceValue v = map.get(widget);
            if (v != null)
            {
                v.setFont(f);
            }
        }
}//GEN-LAST:event_fontBtnActionPerformed

    private void fontFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontFieldActionPerformed
        Widget widget = (Widget) widgetList.getSelectedValue();
        if (fontField.getText().trim().equals(""))
        {
            if(widget instanceof Customizable)
            {
                Customizable c = (Customizable)widget;
                String id = c.getID();
                scene.getResourceTable().addProperty(id + ResourceValue.FONT, null);
            }
            else
            {
                widget.setFont((Font) null);
            }
            
            ResourceValue v = map.get(widget);
            if (v != null)
            {
                v.setFont(null);
                fontField.setText(fontToString(null));
            }
        }
    }//GEN-LAST:event_fontFieldActionPerformed

private void restoreBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreBtnActionPerformed

        ResourceValue.load(scene.getResourceTable());
        clear();
        init(elements, true);
        
//        Widget w = (Widget) typeComboBox.getSelectedItem();
//        prepareDisplay(w);  
}//GEN-LAST:event_restoreBtnActionPerformed

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getSource() == bgComboBox)
        {
            Color bgcolor = ColorComboBox.getColor(bgComboBox);
            Widget widget = (Widget) widgetList.getSelectedValue();
            if (widget != null)
            {
                if(widget instanceof Customizable)
                {
                    Customizable c = (Customizable)widget;
                    String id = c.getID();
                    scene.getResourceTable().addProperty(id + "." + ResourceValue.BGCOLOR, bgcolor);
                }
                else
                {
                    widget.setBackground(bgcolor);
                }
                
                scene.validate();
                ResourceValue v = map.get(widget);
                if (v != null)
                {
                    v.setBGColor(bgcolor);
                }
            }
        } else if (e.getSource() == fgComboBox)
        {
            Color fgcolor = ColorComboBox.getColor(fgComboBox);
            Widget widget = (Widget) widgetList.getSelectedValue();
            if (widget != null)
            {
                if(widget instanceof Customizable)
                {
                    Customizable c = (Customizable)widget;
                    String id = c.getID();
                    scene.getResourceTable().addProperty(id + "." + ResourceValue.FGCOLOR, fgcolor);
                }
                else
                {
                    widget.setForeground(fgcolor);
                }
                
                scene.validate();
                ResourceValue v = map.get(widget);
                if (v != null)
                {
                    v.setFGColor(fgcolor);
                }
            }
        }
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
    
    private void setResourceValues(Customizable cw)
    {
        enableResourceFields(cw);
        
        ResourceValue value = map.get(cw);
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

    

    private void updateWidgetList(Widget w)
    {
        DefaultListModel model = new DefaultListModel();
        List<Customizable> children = new ArrayList<Customizable>();
        if (w instanceof PreviewScene)
        {
            model.addElement(scene);
        } else
        {
            getAllCustomizableChildren(children, w);

            for (Customizable c : children)
            {
                model.addElement(c);
            }
        }
        widgetList.setModel(model);
        if (model.isEmpty())
            setResourceValues(null);
        else
            widgetList.setSelectedIndex(0);
    }

    private List<Customizable> getAllCustomizableChildren(List<Customizable> l, Widget w)
    {
        for (Widget c : w.getChildren())
        {
            if (c instanceof Customizable)
            {
                l.add((Customizable) c);
            }
            getAllCustomizableChildren(l, c);
        }
        return l;
    }
    // utility to highlight the selected customizable widget
    private boolean blink = true;
    private int blinkSequence = 0;
    private RequestProcessor.Task task = new RequestProcessor(
            "ElementCustomizationPanel").create(new Runnable()
    {
        public void run()
        {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {//required to be called in awt thread (at least scene::validate())

                    public void run() {
                        if ((blinkSequence % 2) == 1) {
                            blinkWidget.setBorder(BorderFactory.createLineBorder(2, Color.RED));
                        } else {
                            blinkWidget.setBorder(BorderFactory.createEmptyBorder());
                        }
                        scene.validate();

                        blinkSequence--;
                        task.schedule(250);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    });

    private void startBlinking(Widget w)
    {
        task.cancel();
        blinkWidget.setBorder(BorderFactory.createEmptyBorder());

        Point location = w.convertLocalToScene(new Point());
        Rectangle rec = w.getClientArea();
        if(rec != null)
        {
            blinkWidget.setPreferredLocation(location);
            Insets insets = w.getBorder().getInsets();
            blinkWidget.setPreferredBounds(new Rectangle((rec.x - insets.left),
                    rec.y - insets.top, rec.width + insets.left + insets.right,
                    rec.height + insets.bottom + insets.top));

            blinkSequence = 5;
            task.schedule(0);
        }
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
        return NbBundle.getMessage(ElementCustomizationPanel.class, key);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox bgComboBox;
    private javax.swing.JLabel bgcolorLbl;
    private javax.swing.JComboBox fgComboBox;
    private javax.swing.JLabel fgcolorLbl;
    private javax.swing.JButton fontBtn;
    private javax.swing.JTextField fontField;
    private javax.swing.JLabel fontLbl;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel previewLbl;
    private javax.swing.JScrollPane previewPane;
    private javax.swing.JButton restoreBtn;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLbl;
    private javax.swing.JLabel widgetLbl;
    private javax.swing.JList widgetList;
    // End of variables declaration//GEN-END:variables

    private static class ElementTypeWidgetComboBoxRenderer extends DefaultListCellRenderer
    {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (value == null)
            {
                return new JLabel("");
            }
            setComponentOrientation(list.getComponentOrientation());
            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            Widget w = (Widget) value;
            if (w instanceof Scene)
            {
                setText(NbBundle.getMessage(ElementCustomizationPanel.class, "DiagramScene"));
                setIcon(ImageUtil.instance().getIcon("diagram.png"));
            } else
            {
                IPresentationElement element = ((IPresentationElement) ((ObjectScene) w.getScene()).findObject(w));
                String elementName = element.getFirstSubject().getExpandedElementType();
                String displayName = element.getFirstSubject().getDisplayElementType();
                
                setText(displayName);
                String imageName = CommonResourceManager.instance().getIconDetailsForElementType(elementName);
                imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
                setIcon(ImageUtil.instance().getIcon(imageName));
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setBorder(cellHasFocus ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return this;
        }
    }

    private static class WidgetComparator implements Comparator<Widget>
    {

        public WidgetComparator()
        {
        }

        public int compare(Widget widget1, Widget widget2)
        {
            int retVal = 0;
            // Put all NULLs to the back of the list.  We should never have
            // a null widget, but just in case.
            if ((widget1 == null) || (widget2 != null))
            {
                return retVal = -1;
            }
            else if ((widget1 != null) || (widget2 == null))
            {
                return retVal = 1;
            }

            // Always make the scene the first item in the list.
            if (widget1 instanceof Scene)
            {
                retVal = 1;
            }
            else if (widget2 instanceof Scene)
            {
                retVal = -1;
            }
            else
            {
                Object data1 = ((ObjectScene) widget1.getScene()).findObject(widget1);
                String widget1Name = "";
                if (data1 instanceof IPresentationElement)
                {
                    IPresentationElement presentation = (IPresentationElement) data1;
                    IElement subject = presentation.getFirstSubject();
                    if (subject != null)
                    {
                        widget1Name = subject.getDisplayElementType();
                    }
                }

                Object data2 = ((ObjectScene) widget2.getScene()).findObject(widget2);
                String widget2Name = "";
                if (data2 instanceof IPresentationElement)
                {
                    IPresentationElement presentation = (IPresentationElement) data2;
                    IElement subject = presentation.getFirstSubject();
                    if (subject != null)
                    {
                        widget2Name = subject.getDisplayElementType();
                    }
                }
                retVal = widget2Name.compareTo(widget1Name);
            }


            return retVal;
        }
    }
}
