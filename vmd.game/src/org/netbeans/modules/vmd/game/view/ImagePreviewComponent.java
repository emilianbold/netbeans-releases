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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.game.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import org.netbeans.modules.vmd.game.model.Previewable;


public class ImagePreviewComponent extends JComponent {

	public static final boolean DEBUG = false;

	private boolean centeredH = true;
	private boolean centeredV = true;
	
	private Previewable previewable;
	
	public ImagePreviewComponent (boolean scaled, boolean centeredHor, boolean centeredVer) {
		this(scaled);
		this.centeredH = centeredHor;
		this.centeredV = centeredVer;
	}

	public ImagePreviewComponent (boolean scaled) {
		this.setDoubleBuffered(true);
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (DEBUG) System.out.println("resized - updating preview"); // NOI18N
				ImagePreviewComponent.this.repaint();
			}
		});
	}
	
	public void setPreviewable(Previewable previewable) {
		this.previewable = previewable;
		Dimension d = new Dimension(previewable.getWidth(), previewable.getHeight());
		this.setMinimumSize(d);
		this.setPreferredSize(d);
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		int offX = 0;
		int offY = 0;
		if (this.previewable != null) {
			if (this.centeredH) {
				offX = (this.getWidth() - this.previewable.getWidth()) / 2;
			}
			if (this.centeredV) {
				offY = (this.getHeight() - this.previewable.getHeight()) / 2;
			}
			this.previewable.paint((Graphics2D) g, offX, offY);
		}
	}
	
}
