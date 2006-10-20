/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.design.model.elements;



import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.TextstyleDescriptor;
import org.netbeans.modules.bpel.design.geom.FDimension;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.geom.FShape;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.connections.Connection;


public abstract class VisualElement {
    
    private FShape shape;
    
    private FRectangle textBounds;
    
    private List<Connection> inputConnections  = new ArrayList<Connection>();
    private List<Connection> outputConnections = new ArrayList<Connection>();

    private String text;
    
//    protected TextDesignElement label;
//    protected RectDesignElement labelMouseCatcher;
    

    private Pattern pattern;
    
    
    public VisualElement(FShape shape) {
        this.shape = shape.moveTo(0, 0);
    }
    
    
    public void addInputConnection(Connection c) {
        inputConnections.add(c);
    }
    
    
    public void setLabelText(String text){
        this.text = text;
//        label.clear();
//        label.addText(text);
//        updateLabelPosition();
    }
    
    
    public String getLabelText(){
        return text;
//        String[] lines = label.getStringArray();
//        return lines[0];
    }
    
    
    public void removeInputConnection(Connection c) {
        inputConnections.remove(c);
    }
    
    
    public void addOutputConnection(Connection c) {
        outputConnections.add(c);
    }
    
    
    public void removeOutputConnection(Connection c) {
        outputConnections.remove(c);
    }
    
    
    public List<Connection> getIncomingConnections(){
        return inputConnections;
    }
    
    
    public List<Connection> getOutcomingConnections(){
        return outputConnections;
    }
    
    
    public List<Connection> getAllConnections(){
        ArrayList<Connection> result = new ArrayList<Connection>(inputConnections);
        result.addAll(outputConnections);
        return result;
    }
    
    
    public FShape getShape() {
        return shape;
    }
    
    
    public FRectangle getTextBounds() {
        if (isEmptyText()) return null;
        return textBounds;
    }


    protected void setTextBounds(FRectangle textBounds) {
        this.textBounds = textBounds;
    }
    
    
    public void setLocation(FPoint pos) {
        setLocation(pos.x, pos.y);
    }
    
    
    public void setLocation(float x, float y) {
        shape = shape.moveTo(x, y);
    }
    
    
    public void translate(float dx, float dy) {
        shape = shape.translate(dx, dy);
    }
    
    
    public void setCenter(float cx, float cy) {
        shape = shape.centerTo(cx, cy);
    }
    
    
    public void setSize(FDimension size) {
        shape = shape.resize(size);
    }

    
    public void setSize(float width, float height) {
        shape = shape.resize(width, height);
    }
    

    public void setBounds(float x, float y, float width, float height) {
        shape = shape.rebound(x, y, width, height);
    }
    
    
    public float getX() { return shape.getX(); }
    public float getY() { return shape.getY(); }
    public float getCenterX() { return shape.getCenterX(); }
    public float getCenterY() { return shape.getCenterY(); }
    public float getWidth() { return shape.getWidth(); }
    public float getHeight() { return shape.getHeight(); }
    

    public FRectangle getBounds() { return shape.getBounds(); }
    
    
    public boolean textContains(float x, float y) {
        FRectangle textBounds = getTextBounds();
        return (textBounds != null) && (textBounds.position(x, y) >= 0);
    }
    
    
    public boolean contains(float x, float y) {
        return textContains(x, y) || (shape.position(x, y) >= 0);
    }
    
    
    public void setPattern(Pattern newPattern) {
        pattern = newPattern;
    }
    
    
    public Pattern getPattern() {
        return pattern;
    }
    
    
    public abstract void paint(Graphics2D g);


    public String getText() {
        return text;
    }

    
    public void setText(String newText) {
        text = (newText != null) ? newText.trim() : null;
    }

    
    public boolean isEmptyText() {
        return (text == null) || (text.length() == 0);
    }

    
    public boolean isTextElement() {
        return (getPattern() == null) ? false : getPattern().isTextElement(this);
    }
    
    
    public boolean isPaintText() {
//        if (getPattern() != null) {
//            TextElement textElement = getPattern().getTextElement();
//            return (textElement == null || textElement.isEmptyText()) 
//                    && !isEmptyText();
//        }
        return !isEmptyText();
    }

    
    
    
    
    public Color getTextColor() {
        
        Pattern p = getPattern();
        Decoration decoration = p.getModel().getView().getDecoration(p);
        
        
        
        TextstyleDescriptor textStyle = (decoration == null) ? null 
                : decoration.getTextstyle();
        
        boolean editable = isTextElement();
        
        if (textStyle == null) {
            return (editable) 
                    ? ViewProperties.EDITABLE_TEXT_COLOR 
                    : ViewProperties.UNEDITABLE_TEXT_COLOR;
        }
        
        return (editable) 
                ? textStyle.getEditableTextColor() 
                : textStyle.getNotEditableTextColor();
    }
    
    
    public static final int BASELINE = 4;
    public static final int TEXT_PADDING = 6;
    public static final Font FONT = new Font("sans-serif", Font.PLAIN, 10);
}
