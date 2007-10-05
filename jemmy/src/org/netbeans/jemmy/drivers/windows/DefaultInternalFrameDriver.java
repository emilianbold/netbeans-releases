/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.drivers.windows;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.WindowEvent;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;

import org.netbeans.jemmy.drivers.FrameDriver;
import org.netbeans.jemmy.drivers.InternalFrameDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.WindowDriver;

import org.netbeans.jemmy.drivers.input.EventDriver;

public class DefaultInternalFrameDriver extends LightSupportiveDriver 
    implements WindowDriver, FrameDriver, InternalFrameDriver {
    public DefaultInternalFrameDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.JInternalFrameOperator"});
    }
    public void activate(ComponentOperator oper) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).moveToFront();
	((JInternalFrameOperator)oper).getTitleOperator().clickMouse();
    }
    public void close(ComponentOperator oper) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).moveToFront();
	((JButtonOperator)((JInternalFrameOperator)oper).getCloseButton()).push();
    }
    public void move(ComponentOperator oper, int x, int y) {
	checkSupported(oper);
	ComponentOperator titleOperator = ((JInternalFrameOperator)oper).getTitleOperator();
	titleOperator.dragNDrop(titleOperator.getCenterY(),
				titleOperator.getCenterY(),
				x - ((JInternalFrameOperator)oper).getX() + titleOperator.getCenterY(),
				y - ((JInternalFrameOperator)oper).getY() + titleOperator.getCenterY());
    }
    public void resize(ComponentOperator oper, int width, int height) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).
	    dragNDrop(((JInternalFrameOperator)oper).getWidth() - 1,
		      ((JInternalFrameOperator)oper).getHeight() - 1,
		      width - 1,
		      height - 1);
    }
    public void iconify(ComponentOperator oper) {
	checkSupported(oper);
	((JButtonOperator)((JInternalFrameOperator)oper).getMinimizeButton()).clickMouse();
    }
    public void deiconify(ComponentOperator oper) {
	checkSupported(oper);
	((JInternalFrameOperator)oper).getIconOperator().pushButton();
    }
    public void maximize(ComponentOperator oper) {
	checkSupported(oper);
	if(!((JInternalFrameOperator)oper).isMaximum()) {
	    if(!((JInternalFrameOperator)oper).isSelected()) {
		activate(oper);
	    }
	    ((JInternalFrameOperator)oper).getMaximizeButton().push();
	}
    }
    public void demaximize(ComponentOperator oper) {
	checkSupported(oper);
	if(((JInternalFrameOperator)oper).isMaximum()) {
	    if(!((JInternalFrameOperator)oper).isSelected()) {
		activate(oper);
	    }
	    ((JInternalFrameOperator)oper).getMaximizeButton().push();
	}
    }
    public Component getTitlePane(ComponentOperator operator) {
	ComponentSearcher cs = new ComponentSearcher((Container)operator.getSource());
	cs.setOutput(operator.getOutput().createErrorOutput());
	return(cs.findComponent(new ComponentChooser() {
					public boolean checkComponent(Component comp) {
					    if(System.getProperty("java.version").startsWith("1.2")) {
						return(comp.getClass().getName().endsWith("InternalFrameTitlePane"));
					    } else {
						return(comp instanceof BasicInternalFrameTitlePane);
					    }
					}
					public String getDescription() {
					    return("Title pane");
					}
				    }));
    }
}
