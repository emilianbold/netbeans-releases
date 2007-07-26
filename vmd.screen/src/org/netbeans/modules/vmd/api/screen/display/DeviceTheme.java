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
package org.netbeans.modules.vmd.api.screen.display;

import java.awt.Color;
import java.awt.Font;
import java.util.EnumSet;

/**
 * Describes basic device resource.
 *
 * @author breh
 */
public abstract class DeviceTheme {
    
    public enum FontFace {MONOSPACE, PROPORTIONAL, SYSTEM}
    public enum FontSize {SMALL, MEDIUM, LARGE}
    public enum FontStyle {PLAIN, BOLD, ITALIC, UNDERLINED}
    public enum FontType {DEFAULT, INPUT_TEXT, STATIC_TEXT, CUSTOM}
 
    public enum Colors {BACKGROUND, FOREGROUND, HIGHLIGHTED}
    
    public abstract Font getFont(FontType type);
    
    public abstract Font getFont(FontFace face, EnumSet<FontStyle> style, FontSize size);
    
    public abstract Color getColor(Colors color);
    
}
