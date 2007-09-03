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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.util;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * An empty icon
 * @author Vladimir Kvashin
 */
public class EmptyIcon implements Icon {
    
    private int width;
    private int height;

    public EmptyIcon(int size) {
	this(size, size);
    }
    
    public EmptyIcon(int width, int height) {
	this.width = width;
	this.height = height;
    }

    public int getIconHeight() {
	return height;
    }

    public int getIconWidth() {
	return width;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
    }
}
