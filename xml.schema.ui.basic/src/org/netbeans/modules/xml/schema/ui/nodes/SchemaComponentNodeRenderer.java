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

package org.netbeans.modules.xml.schema.ui.nodes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.AncestorListener;
import org.openide.explorer.view.NodeRenderer;

/**
 * Leave this class alone for now--I may come back to it later to spruce up
 * the rendering of each tree node
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SchemaComponentNodeRenderer extends NodeRenderer
{
    /**
     *
     *
     */
    public SchemaComponentNodeRenderer()
    {
        super();
    }


	/**
	 *
	 *
	 */
    public Component getTreeCellRendererComponent(JTree tree, Object value, 
		boolean selected, boolean expanded, boolean leaf, int row, 
		boolean hasFocus)
	{
		JComponent component=(JComponent)
			super.getTreeCellRendererComponent(tree,value, 
				selected,expanded,leaf,row,hasFocus);
		return new WrapperComponent(tree,component,true);
	}




	////////////////////////////////////////////////////////////////////////////
	// Inner class
	////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 *
	 */
	protected class WrapperComponent extends JComponent
	{
            static final long serialVersionUID = 1L;
		/**
		 *
		 *
		 */
		public WrapperComponent(JTree tree, JComponent target, 
			boolean hasDetail)
		{
			super();
			this.tree=tree;
			this.target=target;
			this.hasDetail=hasDetail;
		}


		/**
		 *
		 *
		 */
		public void paint(Graphics g)
		{
			target.paint(g);

			Graphics2D g2d=(Graphics2D)g;

			if (!hasDetail)
				return;		

			Rectangle clip=g2d.getClipBounds();

			int newWidth = tree.getWidth() - (int) clip.getX();

			g2d.setClip(0 /*(int)clip.getX()*/,(int)clip.getY(),
				newWidth /*(int)clip.getWidth()+10*/,(int)clip.getHeight());

			// Draw the "has detail" triangle widget
			final int INSET=6;
			final int WIDTH=newWidth; //getWidth()+8;
			final int HEIGHT=getHeight();
			final int HEIGHT_CORRECTION=HEIGHT % 2 + 1;
			final int SIZE=HEIGHT-2*INSET;
			final int X=WIDTH-SIZE-INSET;

			// This value must be odd
			final int STEPS=HEIGHT - 2*INSET + HEIGHT_CORRECTION; 

			// Draw the shadow with 33% opacity
			g2d.setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.33f));

			// Draw the shadow
			int right=0;
			for (int i=0; i<STEPS+1; i++)
			{
				final int X_OFFSET=-1;
				final int Y_OFFSET=-1;

				if (i==0)
				{
					g.drawLine(X+X_OFFSET,INSET+i+Y_OFFSET-1,
						X+right+X_OFFSET,INSET+i+Y_OFFSET-1);
				}

				right+=(i <= STEPS/2) ? 2 : -2;
				g.drawLine(X+X_OFFSET,INSET+i+Y_OFFSET,
					X+right+X_OFFSET,INSET+i+Y_OFFSET);

				if (i==STEPS)
				{
					g.drawLine(X+X_OFFSET,INSET+i+Y_OFFSET+1,
						X+right+X_OFFSET,INSET+i+Y_OFFSET+1);
				}
			}

			// Draw widget with 100% opacity
			g2d.setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));

			int color=255;
			final int COLOR_INC=color/6/STEPS;

			// Draw the triangle
			right=-1;
			for (int i=0; i<STEPS; i++)
			{
				g.setColor(new Color(color,color,color));
				color-=COLOR_INC;

				right+=(i <= STEPS/2) ? 2 : -2;
				g.drawLine(X,INSET+i,X+right,INSET+i);
			}
		}

		public void addNotify() {
			target.addNotify();
		}

		public void removeNotify() {
			target.removeNotify();
		}

		public Dimension getPreferredSize() {
			return target.getPreferredSize();
		}

//	    public String getText() {
//			return target.getText();
//		}

		public Border getBorder() {
			return target.getBorder();
		}

		public void setBorder(Border b) {
			target.setBorder(b);
		}

		public Insets getInsets() {
			return target.getInsets();
		}

		public void setEnabled(boolean b) {
			target.setEnabled(b);
		}

		public boolean isEnabled() {
			return target.isEnabled();
		}

		public void updateUI() {
			target.updateUI();
		}

		public Graphics getGraphics() {
			return target.getGraphics();
		}

		public Rectangle getBounds() {
			return target.getBounds();
		}

		public void setBounds(int x, int y, int w, int h) {
			target.setBounds(x,y,w,h);
		}

                @SuppressWarnings("deprecation")
		public void reshape(int x, int y, int w, int h) {
			target.reshape(x,y,w,h);
		}

		public int getWidth() {
			return target.getWidth();
		}

		public int getHeight() {
			return target.getHeight();
		}

		public Point getLocation() {
			return target.getLocation();
		}

		public void validate() {
			target.validate();
		}

//		public void repaint(long tm, int x, int y, int w, int h) {
//			target.repaint(tm,x,y,w,h);
//		}
//
//		public void repaint() {
//			target.repaint();
//		}

		public void invalidate() {
			target.invalidate();
		}

		public void revalidate() {
			target.revalidate();
		}

		public void addAncestorListener(AncestorListener l) {
			target.addAncestorListener(l);
		}

		public void addComponentListener(ComponentListener l) {
			target.addComponentListener(l);
		}

		public void addContainerListener(ContainerListener l) {
			target.addContainerListener(l);
		}

		public void addHierarchyListener(HierarchyListener l) {
			target.addHierarchyListener(l);
		}

		public void addHierarchyBoundsListener(HierarchyBoundsListener l) {
			target.addHierarchyBoundsListener(l);
		}

		public void addInputMethodListener(InputMethodListener l) {
			target.addInputMethodListener(l);
		}

		public void addFocusListener(FocusListener fl) {
			target.addFocusListener(fl);
		}

		public void addMouseListener(MouseListener ml) {
			target.addMouseListener(ml);
		}

		public void addMouseWheelListener(MouseWheelListener ml) {
			target.addMouseWheelListener(ml);
		}

		public void addMouseMotionListener(MouseMotionListener ml) {
			target.addMouseMotionListener(ml);
		}

		public void addVetoableChangeListener(VetoableChangeListener vl) {
			target.addVetoableChangeListener(vl);
		}

		public void addPropertyChangeListener(String s, PropertyChangeListener l) {
			target.addPropertyChangeListener(s,l);
		}

		public void addPropertyChangeListener(PropertyChangeListener l) {
			target.addPropertyChangeListener(l);
		}

		private JTree tree;
		private JComponent target;
		private boolean hasDetail;
	}
}
