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
package org.netbeans.lib.terminalemulator.support;

import java.awt.Font;
import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.UIManager;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * Singleton "bean" to hold Term option properties.
 * 
 * Use TermOptions.getDefault() to get at the singleton.
 *
 * Uses Preferences as a backing store.
 * Uses "term." to distinguish keys from other properties.
 * The values are recovered from backing store on initial creation of
 * the singleton.
 */

public final class TermOptions {

    private static TermOptions DEFAULT;

    // In case settings get shared uniqueify the key names with a prefix:
    private static final String PREFIX = "term.";	// NOI18N

    private boolean dirty = false;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private TermOptions() {
	resetToDefault();
    }

    // Copy constructor
    private TermOptions(TermOptions orig) {
        assign(orig);
    }

    public final void resetToDefault() {
        final Font controlFont = UIManager.getFont("controlFont"); //NOI18N
        fontSize = (controlFont == null) ? 12 : controlFont.getSize();
        font = new Font("monospaced", Font.PLAIN, fontSize); //NOI18N
        foreground = getDefaultColorStandard();
        background = getDefaultColorBackground();
        selectionBackground = getDefaultSelectionBackground();
	historySize = 5000;
	tabSize = 8;
	clickToType = true;
	scrollOnInput = true;
	scrollOnOutput = true;
	lineWrap = true;
        ignoreKeymap = false;
        markDirty();
    }

    private static Color getDefaultColorStandard() {
        Color out = UIManager.getColor("nb.output.foreground"); //NOI18N
        if (out == null) {
            out = UIManager.getColor("TextField.foreground"); //NOI18N
            if (out == null) {
                out = Color.BLACK;
            }
        }
        return out;
    }

    private static Color getDefaultColorBackground() {
        Color back = UIManager.getColor("nb.output.backgorund"); //NOI18N
        if (back == null) {
            back = UIManager.getColor("TextField.background"); //NOI18N
            if (back == null) {
                back = Color.WHITE;
            } else if ("Nimbus".equals( //NOI18N
                    UIManager.getLookAndFeel().getName())) {
                back = new Color(back.getRGB()); // #225829
            }
        }
        return back;
    }

    private static Color getDefaultSelectionBackground() {
	Color color = UIManager.getColor("TextArea.selectionBackground");// NOI18N
	if (color == null) {
	    // bug #185154
	    // Nimbus L&F doesn't define "TextArea.selectionBackground"
	    color = UIManager.getColor("textHighlight");// NOI18N
	}
        return color;
    }

    public synchronized static TermOptions getDefault(Preferences prefs) {
	if (DEFAULT == null) {
	    DEFAULT = new TermOptions();
	    DEFAULT.loadFrom(prefs);
	}
	return DEFAULT;
    }

    /**
     * Make a copy of 'this'.
     */
    public TermOptions makeCopy() {
        return new TermOptions(this);
    }

    /**
     * Assign the values in 'that' to 'this'.
     */
    public void assign (TermOptions that) {
	this.font= that.font;
	this.fontSize = that.fontSize;
	this.tabSize = that.tabSize;
	this.historySize = that.historySize;
	this.foreground = that.foreground;
	this.background = that.background;
	this.selectionBackground = that.selectionBackground;
	this.clickToType = that.clickToType;
	this.scrollOnInput = that.scrollOnInput;
	this.scrollOnOutput = that.scrollOnOutput;
	this.lineWrap = that.lineWrap;
        this.ignoreKeymap = that.ignoreKeymap;
	this.dirty = false;
	pcs.firePropertyChange(null, null, null);
    }

    public boolean isDirty() {
	return dirty;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    void loadFrom(Preferences prefs) {
        if (prefs == null)
            return;
	String fontFamily = prefs.get(PREFIX + PROP_FONT_FAMILY, font.getFamily());
	int fontStyle = prefs.getInt(PREFIX + PROP_FONT_STYLE, font.getStyle());
	fontSize = prefs.getInt(PREFIX + PROP_FONT_SIZE, fontSize);

	tabSize = prefs.getInt(PREFIX + PROP_TAB_SIZE, tabSize);
	historySize = prefs.getInt(PREFIX + PROP_HISTORY_SIZE, historySize);

	int foregroundRGB = prefs.getInt(PREFIX + PROP_FOREGROUND,
					 foreground.getRGB());
	foreground = new Color(foregroundRGB);
	int backgroundRGB = prefs.getInt(PREFIX + PROP_BACKGROUND,
					 background.getRGB());
	background = new Color(backgroundRGB);
	int selectionRGB = prefs.getInt(PREFIX + PROP_SELECTION_BACKGROUND,
						  selectionBackground.getRGB());
	selectionBackground = new Color(selectionRGB);
	clickToType = prefs.getBoolean(PREFIX + PROP_CLICK_TO_TYPE,
				       clickToType);
	scrollOnInput = prefs.getBoolean(PREFIX + PROP_SCROLL_ON_INPUT,
					 scrollOnInput);
	scrollOnOutput = prefs.getBoolean(PREFIX + PROP_SCROLL_ON_OUTPUT,
					  scrollOnOutput);
	lineWrap = prefs.getBoolean(PREFIX + PROP_LINE_WRAP,
				    lineWrap);
        
        ignoreKeymap = prefs.getBoolean(PREFIX + PROP_IGNORE_KEYMAP,
				    ignoreKeymap);

	font = new Font(fontFamily, fontStyle, fontSize);

	// If 'fontfamily' isn't recognized Font.<init> will return
	// a "Dialog" font, per javadoc, which isn't fixed-width so
	// we need to fall back on Monospaced.
	if ("Dialog".equals(font.getFamily()))			// NOI18N
	    font = new Font("Monospaced", fontStyle, fontSize);// NOI18N
    }

    public void storeTo(Preferences prefs) {
        if (prefs == null)
            return;
	prefs.put(PREFIX + PROP_FONT_FAMILY, font.getFamily());
	prefs.putInt(PREFIX + PROP_FONT_STYLE, font.getStyle());
	prefs.putInt(PREFIX + PROP_FONT_SIZE, fontSize);
	prefs.putInt(PREFIX + PROP_TAB_SIZE, tabSize);
	prefs.putInt(PREFIX + PROP_HISTORY_SIZE, historySize);
	prefs.putInt(PREFIX + PROP_FOREGROUND, foreground.getRGB());
	prefs.putInt(PREFIX + PROP_BACKGROUND, background.getRGB());
	prefs.putInt(PREFIX + PROP_SELECTION_BACKGROUND,
		     selectionBackground.getRGB());
	prefs.putBoolean(PREFIX + PROP_CLICK_TO_TYPE, clickToType);
	prefs.putBoolean(PREFIX + PROP_SCROLL_ON_INPUT, scrollOnInput);
	prefs.putBoolean(PREFIX + PROP_SCROLL_ON_OUTPUT, scrollOnOutput);
	prefs.putBoolean(PREFIX + PROP_LINE_WRAP, lineWrap);
        prefs.putBoolean(PREFIX + PROP_IGNORE_KEYMAP, ignoreKeymap);
    }


    /*
     * Font property
     */
    public static final String PROP_FONT = "font"; // NOI18N

    // we use PROP_FONT_SIZE and these two when we persist PROP_FONT
    public static final String PROP_FONT_STYLE = "fontStyle"; // NOI18N
    public static final String PROP_FONT_FAMILY = "fontFamily"; // NOI18N

    private Font font;

    public Font getFont() {
	return font;
    }

    public void setFont(Font font) {
        this.font = font;
        // recalculate fontSize as well.
        fontSize = this.font.getSize();
        markDirty();
    }

    /*
     * Font size property.
     */
    public static final String PROP_FONT_SIZE = "fontSize"; // NOI18N

    private int fontSize;

    public int getFontSize() {
	return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;

        // recalculate font as well.
        font = new Font(font.getFamily(),
                        font.getStyle(),
                        this.fontSize);
        markDirty();
    }

    /*
     * Foreground color property.
     */
    public static final String PROP_FOREGROUND = "foreground"; // NOI18N

    private Color foreground;

    public Color getForeground() {
	return foreground;
    } 
    public void setForeground(Color foreground) {
	this.foreground = foreground;
        markDirty();
    } 

    /*
     * Background color property.
     */
    public static final String PROP_BACKGROUND = "background"; // NOI18N

    private Color background;

    public Color getBackground() {
	return background;
    } 
    public void setBackground(Color background) {
	this.background = background;
        markDirty();
    } 

    /*
     * Selection background color property.
     */
    public static final String PROP_SELECTION_BACKGROUND =
	"selectionBackground"; // NOI18N

    private Color selectionBackground;

    public Color getSelectionBackground() {
	return selectionBackground;
    } 
    public void setSelectionBackground(Color selectionBackground) {
	this.selectionBackground = selectionBackground;
        markDirty();
    } 

    /*
     * History Size property.
     */
    public static final String PROP_HISTORY_SIZE = "historySize"; // NOI18N

    private int historySize;

    public int getHistorySize() {
	return historySize;
    } 
    public void setHistorySize(int historySize) {
        this.historySize = historySize;
        markDirty();
    } 

    /*
     * Tab Size property.
     */
    public static final String PROP_TAB_SIZE = "tabSize"; // NOI18N

    private int tabSize;

    public int getTabSize() {
	return tabSize;
    } 
    public void setTabSize(int tabSize) {
	this.tabSize = tabSize;
        markDirty();
    } 

    /*
     * Click-to-type property.
     */
    public static final String PROP_CLICK_TO_TYPE = "clickToType"; // NOI18N

    private boolean clickToType;

    public boolean getClickToType() {
	return clickToType;
    } 
    public void setClickToType(boolean clickToType) {
	this.clickToType = clickToType;
        markDirty();
    } 

    /*
     * Scroll on input property.
     */
    public static final String PROP_SCROLL_ON_INPUT =
	"scrollOnInput"; // NOI18N

    private boolean scrollOnInput;

    public boolean getScrollOnInput() {
	return scrollOnInput;
    } 
    public void setScrollOnInput(boolean scrollOnInput) {
	this.scrollOnInput = scrollOnInput;
        markDirty();
    } 


    /*
     * Scroll on output property.
     */
    public static final String PROP_SCROLL_ON_OUTPUT =
	"scrollOnOutput"; // NOI18N

    private boolean scrollOnOutput;

    public boolean getScrollOnOutput() {
	return scrollOnOutput;
    } 
    public void setScrollOnOutput(boolean scrollOnOutput) {
	this.scrollOnOutput = scrollOnOutput;
        markDirty();
    } 

    /*
     * Line wrap property.
     */
    public static final String PROP_LINE_WRAP = "lineWrap"; // NOI18N

    private boolean lineWrap;

    public boolean getLineWrap() {
	return lineWrap;
    } 
    public void setLineWrap(boolean lineWrap) {
	this.lineWrap = lineWrap;
        markDirty();
    } 
    
    /*
     * Ignore keymap property.
     */
    public static final String PROP_IGNORE_KEYMAP = "ignoreKeymap"; // NOI18N

    private boolean ignoreKeymap;

    public boolean getIgnoreKeymap() {
	return ignoreKeymap;
    } 
    public void setIgnoreKeymap(boolean ignoreKeymap) {
	this.ignoreKeymap = ignoreKeymap;
        markDirty();
    } 

    private void markDirty() {
        this.dirty = true;
        pcs.firePropertyChange(null, null, null);
    }
}
