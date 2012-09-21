/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2.options;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import org.netbeans.core.output2.Controller;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColors;

/**
 *
 * @author jhavlin
 */
public class OutputOptions {

    public enum LinkStyle {
        NONE, UNDERLINE
    }

    private static OutputOptions DEFAULT = null;
    private boolean initialized = false;
    private static final Logger LOG = Logger.getLogger(
            OutputOptions.class.getName());
    private static final String PREFIX = "output.settings.";            //NOI18N
    public static final String PROP_FONT = "font";                      //NOI18N
    private static final String PROP_FONT_FAMILY = "font.family";       //NOI18N
    private static final String PROP_FONT_SIZE = "font.size";           //NOI18N
    private static final String PROP_FONT_STYLE = "font.style";         //NOI18N
    public static final String PROP_COLOR_STANDARD = "color.standard";  //NOI18N
    public static final String PROP_COLOR_ERROR = "color.error";        //NOI18N
    public static final String PROP_COLOR_LINK = "color.link";          //NOI18N
    public static final String PROP_COLOR_LINK_IMPORTANT =
            "color.link.important";                                     //NOI18N
    public static final String PROP_COLOR_BACKGROUND =
            "color.backgorund";                                         //NOI18N
    public static final String PROP_STYLE_LINK = "style.link";          //NOI18N
    private static final String PROP_INITIALIZED = "initialized";       //NOI18N
    private static final int MIN_FONT_SIZE = 3;
    private static Font defaultFont = null;
    private Font font = null;
    private Color colorStandard;
    private Color colorError;
    private Color colorLink;
    private Color colorLinkImportant;
    private Color colorBackground;
    private LinkStyle linkStyle = LinkStyle.UNDERLINE;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private OutputOptions(boolean initFromDisk) {
        resetToDefault();
        if (!initFromDisk) {
            return;
        }
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                loadFrom(NbPreferences.forModule(Controller.class));
            }
        });
    }

    final void resetToDefault() {
        setDefaultFont();
        setDefaultColors();
        setLinkStyle(LinkStyle.UNDERLINE);
    }

    public void loadFrom(Preferences preferences) {
        assert !EventQueue.isDispatchThread();
        final OutputOptions diskData = new OutputOptions(false);
        String fontFamily = preferences.get(PREFIX + PROP_FONT_FAMILY,
                getDefaultFont().getFamily());
        int fontSize = preferences.getInt(PREFIX + PROP_FONT_SIZE,
                getDefaultFont().getSize());
        int fontStyle = preferences.getInt(PREFIX + PROP_FONT_STYLE,
                getDefaultFont().getStyle());
        diskData.setFont(new Font(fontFamily, fontStyle, fontSize));
        int rgbStandard = preferences.getInt(PREFIX + PROP_COLOR_STANDARD,
                getDefaultColorStandard().getRGB());
        diskData.setColorStandard(new Color(rgbStandard));
        int rgbError = preferences.getInt(PREFIX + PROP_COLOR_ERROR,
                getDefaultColorError().getRGB());
        diskData.setColorError(new Color(rgbError));
        int rgbBackground = preferences.getInt(PREFIX + PROP_COLOR_BACKGROUND,
                getDefaultColorBackground().getRGB());
        diskData.setColorBackground(new Color(rgbBackground));
        int rgbLink = preferences.getInt(PREFIX + PROP_COLOR_LINK,
                getDefaultColorLink().getRGB());
        diskData.setColorLink(new Color(rgbLink));
        int rgbLinkImportant = preferences.getInt(
                PREFIX + PROP_COLOR_LINK_IMPORTANT,
                getDefaultColorLinkImportant().getRGB());
        String linkStyleStr = preferences.get(PREFIX + PROP_STYLE_LINK,
                "UNDERLINE");                                           //NOI18N
        try {
            diskData.setLinkStyle(LinkStyle.valueOf(linkStyleStr));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Invalid link style {0}", linkStyleStr);//NOI18N
        }
        diskData.setColorLinkImportant(new Color(rgbLinkImportant));
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                assign(diskData);
                synchronized (OutputOptions.this) {
                    initialized = true;
                }
                pcs.firePropertyChange(PROP_INITIALIZED, false, true);
            }
        });
    }

    public void saveTo(Preferences preferences) {
        assert !EventQueue.isDispatchThread();
        preferences.putInt(PREFIX + PROP_COLOR_STANDARD,
                getColorStandard().getRGB());
        preferences.putInt(PREFIX + PROP_COLOR_ERROR,
                getColorError().getRGB());
        preferences.putInt(PREFIX + PROP_COLOR_BACKGROUND,
                getColorBackground().getRGB());
        preferences.putInt(PREFIX + PROP_COLOR_LINK,
                getColorLink().getRGB());
        preferences.putInt(PREFIX + PROP_COLOR_LINK_IMPORTANT,
                getColorLinkImportant().getRGB());
        preferences.putInt(PREFIX + PROP_FONT_SIZE, getFont().getSize());
        preferences.putInt(PREFIX + PROP_FONT_STYLE, getFont().getStyle());
        preferences.put(PREFIX + PROP_FONT_FAMILY, getFont().getFamily());
        preferences.put(PREFIX + PROP_STYLE_LINK, getLinkStyle().name());
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    private void setDefaultColors() {
        setColorStandard(getDefaultColorStandard());
        setColorError(getDefaultColorError());
        setColorLink(getDefaultColorLink());
        setColorLinkImportant(getDefaultColorLinkImportant());
        setColorBackground(getDefaultColorBackground());
    }

    private void setDefaultFont() {
        setFont(getDefaultFont());
    }

    public static Font getDefaultFont() {
        if (defaultFont == null) {
            int size = UIManager.getInt("uiFontSize");                  //NOI18N
            if (size < MIN_FONT_SIZE) {
                size = UIManager.getInt("customFontSize");              //NOI18N
            }
            if (size < MIN_FONT_SIZE) {
                Font f = (Font) UIManager.get("controlFont");           //NOI18N
                if (f != null) {
                    size = f.getSize();
                }
            }
            if (size < MIN_FONT_SIZE) {
                size = 11;
            }
            defaultFont = new Font("Monospaced", Font.PLAIN, size);     //NOI18N
        }
        return defaultFont;
    }

    public Font getFont() {
        return font;
    }

    public Color getColorStandard() {
        return colorStandard;
    }

    public Color getColorError() {
        return colorError;
    }

    public Color getColorLink() {
        return colorLink;
    }

    public Color getColorLinkImportant() {
        return colorLinkImportant;
    }

    public Color getColorBackground() {
        return colorBackground;
    }

    public LinkStyle getLinkStyle() {
        return linkStyle;
    }

    public void setFont(Font font) {
        Font fontToSet = font == null ? getDefaultFont() : font;
        if (!fontToSet.equals(this.font)) {
            Font oldFont = this.font;
            this.font = fontToSet;
            pcs.firePropertyChange(PROP_FONT, oldFont, fontToSet);
        }
    }

    public void setColorStandard(Color colorStandard) {
        Parameters.notNull("colorStandard", colorStandard);             //NOI18N
        if (!colorStandard.equals(this.colorStandard)) {
            Color oldColorStandard = this.colorStandard;
            this.colorStandard = colorStandard;
            pcs.firePropertyChange(PROP_COLOR_STANDARD, oldColorStandard,
                    colorStandard);
        }
    }

    public void setColorError(Color colorError) {
        Parameters.notNull("colorError", colorError);                   //NOI18N
        if (!colorError.equals(this.colorError)) {
            Color oldColorError = this.colorError;
            this.colorError = colorError;
            pcs.firePropertyChange(PROP_COLOR_ERROR, oldColorError, colorError);
        }
    }

    public void setColorLink(Color colorLink) {
        Parameters.notNull("colorLink", colorLink);                     //NOI18N
        if (!colorLink.equals(this.colorLink)) {
            Color oldColorLink = this.colorLink;
            this.colorLink = colorLink;
            pcs.firePropertyChange(PROP_COLOR_LINK, oldColorLink, colorLink);
        }
    }

    public void setColorLinkImportant(Color colorLinkImportant) {
        Parameters.notNull("colorLinkImportant", colorLinkImportant);   //NOI18N
        if (!colorLinkImportant.equals(this.colorLinkImportant)) {
            Color oldColorLinkImportant = this.colorLinkImportant;
            this.colorLinkImportant = colorLinkImportant;
            pcs.firePropertyChange(PROP_COLOR_LINK_IMPORTANT,
                    oldColorLinkImportant, colorLinkImportant);
        }
    }

    public void setColorBackground(Color colorBackground) {
        Parameters.notNull("colorBackground", colorBackground);         //NOI18N
        if (!colorBackground.equals(this.colorBackground)) {
            Color oldColorBackground = this.colorBackground;
            this.colorBackground = colorBackground;
            pcs.firePropertyChange(PROP_COLOR_BACKGROUND, oldColorBackground,
                    colorBackground);
        }
    }

    public void setLinkStyle(LinkStyle linkStyle) {
        Parameters.notNull("linkStyle", linkStyle);                     //NOI18N
        if (!linkStyle.equals(this.linkStyle)) {
            LinkStyle oldLinkStyle = this.linkStyle;
            this.linkStyle = linkStyle;
            pcs.firePropertyChange(PROP_STYLE_LINK, oldLinkStyle, linkStyle);
        }
    }

    public static synchronized OutputOptions getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new OutputOptions(true);
        }
        return DEFAULT;
    }

    public void addPropertyChangeListener(
            PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(
            PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Create a copy of this object, with the same options values, but with
     * separate set of listeners.
     */
    public OutputOptions makeCopy() {
        final OutputOptions copy = new OutputOptions(false);
        copy.font = font;
        copy.colorStandard = this.colorStandard;
        copy.colorError = this.colorError;
        copy.colorBackground = this.colorBackground;
        copy.colorLink = this.colorLink;
        copy.colorLinkImportant = this.colorLinkImportant;
        copy.initialized = initialized;
        copy.linkStyle = linkStyle;
        if (!initialized) {
            PropertyChangeListener l = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(PROP_INITIALIZED)) {
                        copy.assign(OutputOptions.this);
                        copy.initialized = true;
                        copy.pcs.firePropertyChange(PROP_INITIALIZED,
                                false, true);
                        OutputOptions.this.removePropertyChangeListener(this);
                    }
                }
            };
            OutputOptions.this.addPropertyChangeListener(l);
        }
        return copy;
    }

    /**
     * Assign values from another object.
     */
    public void assign(OutputOptions outputOptions) {
        this.setFont(outputOptions.getFont());
        this.setColorStandard(outputOptions.getColorStandard());
        this.setColorError(outputOptions.getColorError());
        this.setColorLink(outputOptions.getColorLink());
        this.setColorLinkImportant(outputOptions.getColorLinkImportant());
        this.setColorBackground(outputOptions.getColorBackground());
        this.setLinkStyle(outputOptions.getLinkStyle());
    }

    static Color getDefaultColorStandard() {
        Color out = UIManager.getColor("nb.output.foreground");         //NOI18N
        if (out == null) {
            out = UIManager.getColor("textText");                       //NOI18N
            if (out == null) {
                out = Color.BLACK;
            }
        }
        return out;
    }

    static Color getDefaultColorBackground() {
        Color back = UIManager.getColor("nb.output.backgorund");        //NOI18N
        if (back == null) {
            back = UIManager.getColor("text");                          //NOI18N
            if (back == null) {
                back = Color.WHITE;
            }
        }
        return back;
    }

    static Color getDefaultColorError() {
        Color err = UIManager.getColor("nb.output.err.foreground");     //NOI18N
        if (err == null) {
            err = LFCustoms.shiftColor(Color.red);
        }
        return err;
    }

    static Color getDefaultColorLink() {
        Color hyperlink = UIManager.getColor(
                "nb.output.link.foreground");                           //NOI18N
        if (hyperlink == null) {
            hyperlink = LFCustoms.shiftColor(Color.blue);
        }
        return hyperlink;
    }

    static Color getDefaultColorLinkImportant() {
        Color hyperlinkImp = UIManager.getColor(
                "nb.output.link.foreground.important");                 //NOI18N
        if (hyperlinkImp == null) {
            return getDefaultColorLink();
        } else {
            return hyperlinkImp;
        }
    }

    public Color getColorForType(IOColors.OutputType type) {
        switch (type) {
            case OUTPUT:
                return getColorStandard();
            case ERROR:
                return getColorError();
            case HYPERLINK:
                return getColorLink();
            case HYPERLINK_IMPORTANT:
                return getColorLinkImportant();
            default:
                return getColorStandard();
        }
    }
}
