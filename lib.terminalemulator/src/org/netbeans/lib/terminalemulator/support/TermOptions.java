/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

public class TermOptions {

    private static TermOptions DEFAULT;

    // In case settings get shared uniqueify the key names with a prefix:
    private static final String PREFIX = "term.";	// NOI18N

    private boolean dirty = false;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private TermOptions() {
	resetToDefault();
    }

    public final void resetToDefault() {
	final Font controlFont = UIManager.getFont("controlFont");// NOI18N
	fontSize = (controlFont == null)? 12: controlFont.getSize();
        font = new Font("monospaced", Font.PLAIN, fontSize);	// NOI18N
	foreground = Color.black;
	background = Color.white;
	selectionBackground =
	    UIManager.getColor("TextArea.selectionBackground");	// NOI18N
	historySize = 5000;
	tabSize = 8;
	clickToType = true;
	scrollOnInput = true;
	scrollOnOutput = true;
	lineWrap = true;
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
	TermOptions copy = new TermOptions();
	copy.font= this.font;
	copy.fontSize = this.fontSize;
	copy.tabSize = this.tabSize;
	copy.historySize = this.historySize;
	copy.foreground = new Color(this.foreground.getRGB());
	copy.background = new Color(this.background.getRGB());
	copy.selectionBackground = new Color(this.selectionBackground.getRGB());
	copy.clickToType = this.clickToType;
	copy.scrollOnInput = this.scrollOnInput;
	copy.scrollOnOutput = this.scrollOnOutput;
	copy.lineWrap = this.lineWrap;
	return copy;
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
    }

    public void storeTo(Preferences prefs) {
        if (prefs == null)
            return;
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
    }


    /*
     * Font property
     */
    public static final String PROP_FONT = "font"; // NOI18N

    private Font font;

    public Font getFont() {
	return font;
    }
    public void setFont(Font font) {
	if (this.font!= font) {
	    this.font= font;
	    dirty = true;
	    pcs.firePropertyChange(null, null, null);
	}
    }

    /*
     * Font size property.
     */
    public static final String PROP_FONT_SIZE = "fontSize"; // NOI18N

    private int fontSize;

    /*
     TMP
    public int getFontSize() {
	return fontSize;
    } 
     */
    public void setFontSize(int fontSize) {
	if (this.fontSize != fontSize) {
	    this.fontSize = fontSize;
	    dirty = true;
	    pcs.firePropertyChange(null, null, null);
	} 
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	if (this.historySize != historySize) {
	    dirty = true;
	    this.historySize = historySize;
	    pcs.firePropertyChange(null, null, null);
	} 
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
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
	dirty = true;
	pcs.firePropertyChange(null, null, null);
    } 
}
