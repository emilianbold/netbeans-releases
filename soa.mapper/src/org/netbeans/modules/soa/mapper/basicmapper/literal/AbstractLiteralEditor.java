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

package org.netbeans.modules.soa.mapper.basicmapper.literal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openide.windows.WindowManager;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperLiteralUpdateEventInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;

/**
 * Handles all generic literal editor behavior. Care must be taken to
 * handle disposal and focus events correctly. The dispose() method must
 * always be final, because subclasses are thereby required to use
 * diposeAndUpdateLiteral to dispose the window.
 * 
 * A literal editor is simply a window that is opened up on top of
 * a methoid so that it appears as if the user is editing the methoid.
 * The editor automatically closes when focus is lost, or when the
 * ENTER or ESC key is hit.
 * 
 * An update listener can be registered with a literal. This provides
 * custom handling for when the editor is closed and the change needs
 * to be committed.
 * 
 * @author Josh Sandusky
 */
public abstract class AbstractLiteralEditor 
extends JWindow 
implements ILiteralEditor {

    protected IMethoidNode mMethoidNode;
    protected IFieldNode mFieldNode;
    protected ICanvasFieldNode mCanvasFieldNode;
    protected ICanvasMethoidNode mCanvasMethoidNode;
    protected boolean isIgnoringFocusLostUpdate = false;
    protected ILiteralUpdater mUpdateListener;
    private ComponentListener mComponentListener;
    private boolean mClosed = false;
    private IBasicMapper mBasicMapper;
    private JComponent mEditorComponent;
    protected boolean mIsLiteralMethoid = false;
    

    public AbstractLiteralEditor(Window owner,
                                 IBasicMapper basicMapper, 
                                 IFieldNode fieldNode,
                                 ILiteralUpdater updateListener) {
        super(owner);
        
        mBasicMapper = basicMapper;
        mFieldNode = fieldNode;
        mUpdateListener = updateListener;
        mMethoidNode = (IMethoidNode) fieldNode.getGroupNode();
        IMethoid methoid = (IMethoid) mMethoidNode.getMethoidObject();
        mIsLiteralMethoid = methoid.isLiteral();
        
        IMapperCanvasView canvasView = mBasicMapper.getMapperViewManager().getCanvasView();

        mCanvasMethoidNode = canvasView.getCanvas().findCanvasMethoidNode(mMethoidNode);
        mCanvasFieldNode   = canvasView.getCanvas().findCanvasFieldNode(mFieldNode);

        Rectangle rect = mCanvasFieldNode.getBounding();
        Point canvasLocation = canvasView.getCanvasComponent().getLocationOnScreen();
        Point fieldLocation = new Point(rect.x, rect.y);
        canvasView.getCanvas().convertDocToView(fieldLocation);
        fieldLocation.x += canvasLocation.x + 1;
        fieldLocation.y += canvasLocation.y + 1;
        setBackground(canvasView.getViewComponent().getBackground());
        JPanel panel = new JPanel();
        panel.setBackground(canvasView.getViewComponent().getBackground());
        panel.setLayout(new BorderLayout());
        setContentPane(panel);
        setLocation(fieldLocation);
        setSize(getInitialSize());
        initWindowListeners();
    }

    public void show() {
        super.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // ah well
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // Ensure our editor component has focus.
                        // This is because a literal methoid that is added to the canvas
                        // will cause the canvas to request focus away from us.
                        toFront();
                        mEditorComponent.requestFocus();
                        
                        // By this time we have focus and should expect nobody to
                        // request focus away from us.
                        mEditorComponent.addFocusListener(new FocusListener() {
                            public void focusGained(FocusEvent e) {
                            }
                            public void focusLost(FocusEvent e) {
                                if (!isIgnoringFocusLostUpdate) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                if (!isIgnoringFocusLostUpdate) { // double check
                                                    diposeAndUpdateLiteral();
                                                }
                                            }
                                        });
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }
    
    protected final boolean isClosed() {
        return mClosed;
    }

    private void disposeMe() {
        setVisible(false);
        mClosed = true;
        WindowManager.getDefault().getMainWindow().removeComponentListener(mComponentListener);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dispose();
            }
        });
    }

    // Do not allow subclasses to dispose directly, they must
    // use diposeAndUpdateLiteral which ensures focus is handled correctly.
    public final void dispose() {
        super.dispose();
    }
    
    private void initWindowListeners() {

        mComponentListener = new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
                isIgnoringFocusLostUpdate = true;
                diposeAndUpdateLiteral();
            }

            public void componentMoved(ComponentEvent e) {
                isIgnoringFocusLostUpdate = true;
                diposeAndUpdateLiteral();
            }

            public void componentResized(ComponentEvent e) {
                isIgnoringFocusLostUpdate = true;
                diposeAndUpdateLiteral();
            }

            public void componentShown(ComponentEvent e) {
            }
        };

        WindowManager.getDefault().getMainWindow().addComponentListener(mComponentListener);
    }

    protected void initializeLiteralComponent(
                                              IMapperCanvasView canvasView, 
                                              JComponent literalComponent, 
                                              JComponent editorComponent) {
        mEditorComponent = editorComponent;
        
        Color bgColor = canvasView.getViewComponent().getBackground();

        editorComponent.setBorder(null);
        literalComponent.setBackground(bgColor);
        editorComponent.setBackground(bgColor);

        editorComponent.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

        editorComponent.getActionMap().put("escape", 
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    isIgnoringFocusLostUpdate = true;
                    disposeMe();
                }
            });

        editorComponent.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "return");
        editorComponent.getActionMap().put("return",
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    commitEdit();
                }
            });
    }
    
    protected void diposeAndUpdateLiteral() {
        // Dispose FIRST!!!
        disposeMe();
        // Ensure canvas focus LAST!!!
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Ensure the canvas has focus because otherwise 
                    // its key events don't get heard.
                    // Then updateLiteral.
                    String newValue = updateLiteral();
                    if (newValue != null) {
                        fireLiteralUpdated(newValue);
                    }
                    
                    IMapperCanvasView canvasView = 
                        mBasicMapper.getMapperViewManager().getCanvasView();
                    canvasView.getCanvasComponent().requestFocus();
                }
            });
    }

    protected abstract Dimension getInitialSize();
    
    protected abstract String updateLiteral();

    protected void commitEdit() {
        isIgnoringFocusLostUpdate = true;
        diposeAndUpdateLiteral();
    }
    
    public ILiteralUpdater getUpdateListener() {
        return mUpdateListener;
    }
    
    private void fireLiteralUpdated(final String newValue) {
        Object ob = mMethoidNode.getNodeObject();
        final IMethoid methoid = (IMethoid) mMethoidNode.getMethoidObject();

        // set the expression
        mBasicMapper.updateFieldLiteral(new IBasicMapperLiteralUpdateEventInfo() {
            public ILiteralUpdater getLiteralUpdater() {
                return mUpdateListener;
            }
            public IMethoidNode getMethoidNode() {
                return mMethoidNode;
            }
            public IFieldNode getFieldNode() {
                return mFieldNode;
            }
            public String getNewValue() {
                return newValue;
            }
            public boolean isLiteralMethoid() {
                return methoid.isLiteral();
            }
        });
    }
}
