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

/*
 * TranslucentLabel.java
 *
 * Created on June 21, 2006, 6:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *
 * @author girix
 */
public class TranslucentLabel extends JLabel{
    private static final long serialVersionUID = 7526472295622776147L;
    
    public TranslucentLabel(Icon icon){
        super(icon);
    }
    
    public TranslucentLabel(Icon icon, int allignment){
        super(icon, allignment);
    }
    
    public TranslucentLabel(String str, int allignment){
        super(str, allignment);
    }
    
    public void paint(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        Rectangle rect = g2d.getClipBounds();
        
        int rule = AlphaComposite.SRC_OVER;
        float alpha = 1.0f;
        g2d.setComposite(AlphaComposite.getInstance(rule, alpha));
        
        GradientPaint fill=new GradientPaint(
                (float)rect.x,(float)rect.y,getBackground() ,
                (float)rect.x,(float)rect.height,getBackground());
        
        
        g2d.setPaint(fill);
        
        g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        super.paint(g2d);
    }
    
}
