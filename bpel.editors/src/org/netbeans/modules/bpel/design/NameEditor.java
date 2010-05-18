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

package org.netbeans.modules.bpel.design;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;

import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.ProcessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.DiagramView;
import org.openide.util.NbBundle;

public class NameEditor extends JTextField 
        implements FocusListener, ActionListener, DocumentListener
{

    private DiagramView DiagramView;
    private VisualElement textElement;
    
    private int startWidth;
    private int startX;
    private static final long serialVersionUID = 1;
    
    
    public NameEditor(DiagramView DiagramView) {
        this.DiagramView = DiagramView;
        
        final Object esc = "cancel-name-editing"; // NOI18N
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), esc);
        getActionMap().put(esc, new CancelAction());
        
        addFocusListener(this);
        addActionListener(this);
    }
    

    public void startEdit(Point p) {
        FPoint fp = getDiagramView().convertScreenToDiagram(p);
        startEdit(fp.x, fp.y);
    }
    
    
    public void startEdit(double x, double y) {
        VisualElement element = getDiagramView().findElement(x, y);
        if (element == null) return;
        if (!element.textContains(x, y)) return;
        startEdit(element);
    }
    
    
    public void startEdit(Pattern pattern) {
        if (pattern == null) return;
        startEdit(pattern.getTextElement());
    }
    
    
    public void startEdit(VisualElement element) {
        if (getDiagramView().getDesignView().getModel().isReadOnly()) return;
        
        if (element == null) return;
        if (!element.getPattern().isTextElement(element)) return;
        //if (element.isEmptyText()) return;

        String text = element.getText();
        
        setText(text == null ? "" : text); // NOI18N
//        setFont(getDiagramView().getZoomedDiagramFont());
//        
//        FRectangle bounds = element.getTextBounds();
//
//        if (bounds == null) {
//            JLabel measurer = new JLabel("        "); // NOI18N
//            measurer.setFont(getDiagramView().getZoomedDiagramFont());
//        
//            Dimension size = measurer.getPreferredSize();
//            
//            float correctedZoom = getDiagramView().getCorrectedZoom();
//
//            float w = ((float) size.width) / correctedZoom;
//            float h = ((float) size.height) / correctedZoom;
//            
//            float x;
//            float y;
//            
//            if (element instanceof ProcessBorder) {
//                x = element.getCenterX() - 0.5f * w;
//                y = element.getY() + 16 - 0.5f * h;
//            } else if (element instanceof BorderElement) {
//                x = element.getX() + 6;
//                y = element.getY() + 1;
//            } else {
//                x = element.getCenterX() - 0.5f * w;
//                y = element.getY() + element.getHeight();
//            }
//            
//            bounds = new FRectangle(x, y, w, h);
//        }
//        
//        float zoom = getDiagramView().getCorrectedZoom();
//
//        Dimension size = getPreferredSize();
//        Insets insets = getInsets();
//
//        int width = Math.round(Math.max(60, Math.max(
//                zoom * bounds.getWidth() + insets.left + insets.right,
//                size.width) + 10 * zoom));
//
//        int height = size.height;
//        
//        Point topLeft = DiagramView.convertDiagramToScreen(
//                new FPoint(bounds.x, bounds.y));
//        
//        Point center = DiagramView.convertDiagramToScreen(
//                new FPoint(bounds.getCenterX(), bounds.getCenterY()));
//        
//        int x1;
//        int y1 = topLeft.y - insets.top;
//
//        if (element instanceof ProcessBorder) {
//            x1 = center.x - width / 2;
//            setHorizontalAlignment(CENTER);
//        } else if (element instanceof BorderElement) {
//            x1 = topLeft.x - insets.left;
//            setHorizontalAlignment(LEFT);
//        } else {
//            x1 = center.x - width / 2;
//            setHorizontalAlignment(CENTER);
//        }
//        
//        
//        if (x1 < 0) {
//            x1 = 0;
//        }
//        
//        if (x1 + width > getDiagramView().getWidth()) {
//            width = getDiagramView().getWidth() - x1;
//        }
//        
//        
//        startWidth = width;
//        startX = x1;
//        
//
//        setBounds(x1, y1, width, height);
        
        textElement = element;
        
        getDiagramView().add(this);
        selectAll();
        requestFocusInWindow();
        
        getDocument().addDocumentListener(this);

        updateBounds();
    }
    
    
    public boolean isActive() {
        return textElement != null;
    }
    
    
//    public void stopEdit() {
//        if (!isActive()) return;
//        
//        removeActionListener(this);
//        removeFocusListener(this);
//        
//        getDiagramView().remove(this);
//        getDiagramView().revalidate();
//        getDiagramView().repaint();
//        
//        textElement = null;
//        
//        getDocument().removeDocumentListener(this);
//    }
    
    
    
    public void cancelEdit() {
        if (!isActive()) return;
        
        getDiagramView().remove(this);
        getDiagramView().revalidate();
        getDiagramView().repaint();
        
        textElement = null;
        
        getDocument().removeDocumentListener(this);
    }
    
    
    
//    private void saveChanges() {
//        if (!isActive()) return true;
//        
//        String oldValue = textElement.getText();
//        
//        if (oldValue == null) { 
//            oldValue = ""; // NOI18N
//        }
//        
//        String newValue = getNewName();
//        
//        if (newValue.equals(oldValue)) {
//            stopEdit();
//            return true;
//        }
//
//        NamedElement namedElement = (NamedElement) textElement.getPattern()
//                .getOMReference();
//        
//        try {
//            if ("".equals(newValue)) { // NOI18N
//                if (namedElement instanceof Activity) {
//                    ((Activity) namedElement).removeName();
//                } else {
//                    stopEdit();
//                    return true;
//                }
//            } else {
//                namedElement.setName(newValue);
//            }
//            
//            stopEdit();
//            return true;
//        } catch (Exception ex) {
////              ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            
//            JOptionPane.showMessageDialog(SwingUtilities.getRootPane(
//                    getDiagramView()), ex.getMessage(), 
//                    "Invalid Name", JOptionPane.ERROR_MESSAGE);
//
//            setText(oldValue);
//            selectAll();
//            requestFocusInWindow();
//            
//            return false;
//        }
//    }
    
    
    public DiagramView getDiagramView() {
        return DiagramView;
    }
    

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2);
    }

    
    public void focusGained(FocusEvent e) {
    }

    
    public void focusLost(FocusEvent e) {
        if (!isActive()) return;

        removeFocusListener(this);
        removeActionListener(this);
        
        
        String oldValue = getOldName();
        String newValue = getNewName();
        
        NamedElement namedElement = (NamedElement) textElement.getPattern()
                .getOMReference();
        
        if (!newValue.equals(oldValue)) {
            if (getDiagramView().getDesignView().getModel().isReadOnly()) {
                showErrorMessage(NbBundle.getMessage(getClass(), 
                        "LBL_RenameReadOnlyMessage")); // NOI18N
                cancelEdit();
                addFocusListener(this);
                addActionListener(this);
                return;
            }
            
            try {
                if ("".equals(newValue) && (namedElement instanceof Activity)) { // NOI18N
                    ((Activity) namedElement).removeName();
                } else {
                    namedElement.setName(newValue);
                }
            } catch (Exception ex) {
                showErrorMessage(ex.getLocalizedMessage());
            }
        }

        cancelEdit();

        addFocusListener(this);
        addActionListener(this);
    }
    
    
    public void actionPerformed(ActionEvent e) {
        if (!isActive()) return;

        removeFocusListener(this);
        removeActionListener(this);
        
        String oldValue = getOldName();
        String newValue = getNewName();
        
        NamedElement namedElement = (NamedElement) textElement.getPattern()
                .getOMReference();
        
        if (!newValue.equals(oldValue)) {
            if (getDiagramView().getDesignView().getModel().isReadOnly()) {
                showErrorMessage(NbBundle.getMessage(getClass(), 
                        "LBL_RenameReadOnlyMessage")); // NOI18N
                cancelEdit();
                addFocusListener(this);
                addActionListener(this);
                getDiagramView().requestFocusInWindow();
                return;
            }
            
            try {
                if ("".equals(newValue) && (namedElement instanceof Activity)) { // NOI18N
                    ((Activity) namedElement).removeName();
                } else {
                    namedElement.setName(newValue);
                }
            } catch (Exception ex) {
                showErrorMessage(ex.getLocalizedMessage());
                
                setText(oldValue);
                selectAll();

                addFocusListener(this);
                addActionListener(this);
                
                return;
            }
        }

        cancelEdit();

        addFocusListener(this);
        addActionListener(this);
        
        getDiagramView().requestFocusInWindow();
    }
    

    private String getOldName() {
        String result = textElement.getText();
        return (result == null) ? "" : result; // NOI18N
    }
    

    private String getNewName() {
        return getText().trim();
    }

    private DesignView getDesignView(){
        return getDiagramView().getDesignView();
    }
    public void updateBounds() {
        if (!isActive()) return;
        
        setFont(getDesignView().getZoomedDiagramFont());
        
        FBounds bounds = textElement.getTextBounds();

        if (bounds == null) {
            JLabel measurer = new JLabel("        "); // NOI18N
            measurer.setFont(getDesignView().getZoomedDiagramFont());
        
            Dimension size = measurer.getPreferredSize();
            
            double correctedZoom = getDesignView().getCorrectedZoom();

            double w = ((double) size.width) / correctedZoom;
            double h = ((double) size.height) / correctedZoom;
            
            double x;
            double y;
            
            if (textElement instanceof ProcessBorder) {
                x = textElement.getCenterX() - 0.5 * w;
                y = textElement.getY() + 16 - 0.5 * h;
            } else if (textElement instanceof BorderElement) {
                x = textElement.getX() + 6;
                y = textElement.getY() + 1;
            } else {
                x = textElement.getCenterX() - 0.5 * w;
                y = textElement.getY() + textElement.getHeight();
            }
            
            bounds = new FBounds(x, y, w, h);
        }
        
        double zoom = getDesignView().getCorrectedZoom();

        Dimension size = getPreferredSize();
        Insets insets = getInsets();

        int width = (int) Math.round(Math.max(60, Math.max(
                zoom * bounds.getWidth() + insets.left + insets.right,
                size.width) + 10 * zoom));

        int height = size.height;
        
        Point topLeft = DiagramView.convertDiagramToScreen(
                new FPoint(bounds.x, bounds.y));
        
        Point center = DiagramView.convertDiagramToScreen(
                new FPoint(bounds.getCenterX(), bounds.getCenterY()));
        
        int x1;
        int y1 = topLeft.y - insets.top;

        if (textElement instanceof ProcessBorder) {
            x1 = center.x - width / 2;
            setHorizontalAlignment(CENTER);
        } else if (textElement instanceof BorderElement) {
            x1 = topLeft.x - insets.left;
            setHorizontalAlignment(LEFT);
        } else {
            x1 = center.x - width / 2;
            setHorizontalAlignment(CENTER);
        }
        
        
        if (x1 < 0) {
            x1 = 0;
        }
        
        if (x1 + width > getDiagramView().getWidth()) {
            width = getDiagramView().getWidth() - x1;
        }
        
        startWidth = width;
        startX = x1;

        setBounds(x1, y1, width, height);
        scrollToBeVisible();
        getDiagramView().repaint();

        
//        Dimension size = getPreferredSize();
//        int oldX = getX();
//        int oldY = getY();
//        int oldWidth = getWidth();
//        int oldHeight = getHeight();
//        
//        int newWidth = Math.max(size.width + 8, startWidth);
//        
//        int newX = (getHorizontalAlignment() == LEFT) ? startX
//                : startX + (startWidth - newWidth) / 2;
//
//        if (newX < 0) {
//            newX = 0;
//        }
//        
//        if (newX + newWidth > getDiagramView().getWidth()) {
//            newWidth = getDiagramView().getWidth() - newX;
//        }
//        
//        setBounds(newX, oldY, newWidth, oldHeight);
//        
//        scrollToBeVisible();
    }
    
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(SwingUtilities.getRootPane(
                getDiagramView()), message, 
                NbBundle.getMessage(getClass(), "LBL_NameEditorInvalidName"), // NOI18N
                JOptionPane.ERROR_MESSAGE);
    }
    
    
    public void insertUpdate(DocumentEvent e) {
        updateBounds();
    }

    
    public void removeUpdate(DocumentEvent e) {
        updateBounds();
    }

    
    public void changedUpdate(DocumentEvent e) {
        updateBounds();
    }
    
    
    private void scrollToBeVisible() {
        JViewport port = (JViewport) getDiagramView().getParent();
        
        Rectangle vr = port.getViewRect();
        Rectangle er = getBounds();

//        System.out.println("View Rect: " + vr);
//        System.out.println("Edir Rect: " + er);
        
        int tx1 = er.x - vr.x;
        int ty1 = er.y - vr.y;
        
        int tx2 = (er.x + er.width) - (vr.x + vr.width);
        int ty2 = (er.y + er.height) - (vr.y + vr.height);

        int tx = 0;
        int ty = 0;
        
        if (er.width > vr.width) {
            tx = tx1;
        } else if (tx2 > 0) {
            tx = tx2;
        } else if (tx1 < 0) {
            tx  = tx1;
        }
        
        if (er.height > vr.height) {
            ty = ty1;
        } else if (ty2 > 0) {
            ty = ty2;
        } else if (ty1 < 0) {
            ty  = ty1;
        }
        
        if (tx == 0 && ty == 0) return;
        
//        System.out.println("tx = " + tx);        
//        System.out.println("ty = " + ty);
        port.setViewPosition(new Point(vr.x + tx, vr.y + ty));
        getDiagramView().repaint();
    }
    
    
    private class CancelAction extends AbstractAction {
        private static final long serialVersionUID = 1;
        public void actionPerformed(ActionEvent e) {
            cancelEdit();
            getDiagramView().requestFocus();
        }
    }
}
