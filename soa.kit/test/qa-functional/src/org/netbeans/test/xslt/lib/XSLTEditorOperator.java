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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.xslt.lib;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 *
 * @author ca@netbeans.org
 */

public class XSLTEditorOperator {
    private SourceTreeOperator m_opSourceTree;
    private TargetTreeOperator m_opTargetTree;
    private CanvasOperator m_opCanvas;
    private PaletteOperator m_opPalette;

    private JToggleButtonOperator m_opDesignButton;
    private JToggleButtonOperator m_opSourceButton;
    
    private JComponentOperator m_opEditor;
    private MouseRobotDriver m_mouseDriver;
    private KeyRobotDriver m_keyDriver;
    
    /**
     * Creates a new instance of XSLTEditorOperator 
     */
    public XSLTEditorOperator(String strEditorName) {
        JComponentOperator opComponent = Helpers.getComponentOperator(MainWindowOperator.getDefault(), "org.netbeans.core.multiview.MultiViewCloneableTopComponent", strEditorName);
        
        m_opEditor = Helpers.getComponentOperator(opComponent, "org.netbeans.modules.xslt.mapper.view.XsltMapper");
        
        m_opCanvas = new CanvasOperator(m_opEditor);
        m_opSourceTree = new SourceTreeOperator(m_opEditor);
        m_opTargetTree = new TargetTreeOperator(m_opEditor);
        m_opPalette = new PaletteOperator();
        
        JComponentOperator opToolbar = Helpers.getComponentOperator(opComponent, "javax.swing.JToolBar");
        
        m_opDesignButton = new JToggleButtonOperator(opToolbar, "Design");
        m_opSourceButton = new JToggleButtonOperator(opToolbar, "Source");
        
        m_mouseDriver = new MouseRobotDriver(new Timeout("", 500));
        m_keyDriver = new KeyRobotDriver(new Timeout("", 50));
    }
    
    public CanvasOperator getCanvasOperator() {
        return m_opCanvas;
    }
    
    public void bindSourceToTarget(String sourceTreePath, String targetTreePath) {
        Point startPoint = m_opSourceTree.prepareNodeForClick(sourceTreePath);
        Point endPoint = m_opTargetTree.prepareNodeForClick(targetTreePath);
        
        performDragAndDrop(m_opSourceTree, startPoint, m_opTargetTree, endPoint);
    }
    
    public void bindSourceToMethoid(String sourceTreePath, String strMethoidTitle, int methoidIndex, int methiodPortIndex) {
        Point startPoint = m_opSourceTree.prepareNodeForClick(sourceTreePath);
        
        MethoidOperator opMethoid = m_opCanvas.findMethoid(strMethoidTitle, methoidIndex);
        Point endPoint = opMethoid.getPortPoint(methiodPortIndex);
        
        performDragAndDrop(m_opSourceTree, startPoint, m_opCanvas, endPoint);
    }
    
    public void bindMethoidToTarget(String strMethoidTitle, int methoidIndex, String targetTreePath ) {
        MethoidOperator opMethoid = m_opCanvas.findMethoid(strMethoidTitle, methoidIndex);
        Point startPoint = opMethoid.getPortPoint(opMethoid.getPortCount()-1);
        
        Point endPoint = m_opTargetTree.prepareNodeForClick(targetTreePath);
        
        performDragAndDrop(m_opCanvas, startPoint, m_opTargetTree, endPoint);
    }
    
    
    public void bindMethoidToMethiod(String strSrcMethoidTitle, int srcMethoidIndex, int srcMethiodPortIndex,
                                    String strDstMethoidTitle, int dstMethoidIndex, int dstMethiodPortIndex) {
        MethoidOperator opSrcMethoid = m_opCanvas.findMethoid(strSrcMethoidTitle, srcMethoidIndex);
        Point startPoint = opSrcMethoid.getPortPoint(srcMethiodPortIndex);
        
        MethoidOperator opDstMethoid = m_opCanvas.findMethoid(strDstMethoidTitle, dstMethoidIndex);
        Point endPoint = opDstMethoid.getPortPoint(dstMethiodPortIndex);
        
        performDragAndDrop(m_opCanvas, startPoint, m_opCanvas, endPoint);
    }
    
    public void dropPaletteItemOnCanvas(PaletteOperator.Groups group, String strItem, Point canvasPoint) {
        Point palettePoint = m_opPalette.prepareNodeForClick(group, strItem);
        
        performDragAndDrop(m_opPalette, palettePoint, m_opCanvas, canvasPoint);
        Helpers.waitNoEvent();
    }
    
    public void removeMethoid(String strTitle, int index) {
        MethoidOperator opMethoid = m_opCanvas.findMethoid(strTitle, index);
        Rectangle rect = opMethoid.getBoundings();
        Point clickPoint = new Point(rect.x + 1, rect.y + 1);
        m_mouseDriver.clickMouse(m_opCanvas, clickPoint.x, clickPoint.y, 1, InputEvent.BUTTON1_MASK, 0, new Timeout("t1", 50));
        
        m_keyDriver.pushKey(m_opCanvas, KeyEvent.VK_DELETE, 0, new Timeout("t1", 50));
        Helpers.waitNoEvent();
    }
    
    private void performDragAndDrop(JComponentOperator opFrom, Point pointFrom, JComponentOperator opTo, Point pointTo) {
        
//        Doesn't work on Solaris
//        Point p1 = Helpers.getContainerPoint(opFrom, pointFrom, m_opEditor);
//        Point p2 = Helpers.getContainerPoint(opTo, pointTo, m_opEditor);
//        m_mouseDriver.dragNDrop(m_opEditor, p1.x, p1.y, p2.x, p2.y, InputEvent.BUTTON1_MASK, 0, new Timeout("t1", 50), new Timeout("t2", 50));
        
        m_mouseDriver.moveMouse(opFrom, pointFrom.x, pointFrom.y);
        m_mouseDriver.pressMouse(InputEvent.BUTTON1_MASK, 0);
        m_mouseDriver.enterMouse(opTo);
        m_mouseDriver.dragMouse(opTo, pointTo.x, pointTo.y, InputEvent.BUTTON1_MASK, 0);
        m_mouseDriver.releaseMouse(InputEvent.BUTTON1_MASK, 0);
    }
    
    public void switchToDesign() {
        m_opDesignButton.push();
        Helpers.waitNoEvent();
    }
    
    public void switchToSource() {
        m_opSourceButton.push();
        Helpers.waitNoEvent();
    }
    
    public void removeAllTargetNodes(String strRoot) {
        m_opTargetTree.ivokeDeleteOnPath(strRoot);
    }
}
