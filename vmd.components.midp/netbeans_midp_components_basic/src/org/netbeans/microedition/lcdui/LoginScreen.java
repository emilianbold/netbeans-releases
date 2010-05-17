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

package org.netbeans.microedition.lcdui;

import javax.microedition.lcdui.*;
import org.netbeans.microedition.lcdui.laf.ColorSchema;
import org.netbeans.microedition.lcdui.laf.DefaultColorSchema;

/**
 * The <code>LoginScreen</code> custom component provides usefully UI with standard
 * elements like Username Filed, Password Field and Login Button 
 *
 * @author breh
 */
public class LoginScreen extends Canvas implements CommandListener {

    /*
     * Command fired when login process is started.
     */
    public static Command LOGIN_COMMAND = new Command("Login", Command.OK, 1);

    private static final int ACTIVE_USERNAME = 1;
    private static final int ACTIVE_PASSWORD = 2;
    private static final int ACTIVE_LOGIN_BUTTON = 3;

    private int activeField = ACTIVE_USERNAME;

    private String loginButtonText;
    private boolean useLoginButton = true;

    private int backgroundImageAnchorPoint;

    private InputTextBox inputTextBox;

    private boolean useTextBoxForInput = true;
    private boolean inputTextIsActive = false;

    private int borderStyle;
    private int hiBorderStyle;

    private Font titleFont;
    private Font inputFont;
    private Font loginButtonFont;

    private int visibleInputFieldLength = 12;
    private int maximumInputSize;
    private static final int borderPadding = 2;
    private static final int labelPadding = 4;

    private int loginTitleY;
    private int usernameY;
    private int passwordY;
    private int usernameX;
    private int passwordX;

    private int inputFieldsWidth;
    private int inputFieldsHeight;
    private int loginButtonWidth;
    private int loginButtonHeight;
    private int loginButtonY;
    private int loginButtonX;

    private int usernameLabelWidth;
    private int passwordLabelWidth;


    private String usernameLabel;
    private String passwordLabel;
    private String username = "";
    private String password = "";
    private String shownPassword = "";
    private static final char PASSWORD_CHAR = '*';
    private String loginScreenTitle;
    private Display display;
    private CommandListener l;
    private ColorSchema colorSchema;

    /**
     * Creates a new instance of <code>LoginScreen</code> for given <code>Display</code> object.
     * @param display a non-null <code>Display</code> object.
     */
    public LoginScreen(Display display) {
        this.display = display;
        colorSchema = new DefaultColorSchema();
        setDefaulBorderStyles();
        setDefaultFonts();

        titleFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
        loginButtonFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

        // default values
        this.usernameLabel = "Username:";
        this.passwordLabel = "Password:";
        this.loginButtonText = "Login";
        this.maximumInputSize = 20;

        setUseLoginButton(useLoginButton);
        super.setCommandListener(this);
    }

    /**
     * Sets predefined login username.
     * @param username predifined login username
     */
    public void setUsername(String username) {
        if (username == null) {
            username = "";
        }
        this.username = username;
    }
    
    /**
     * Returns login username
     * @return non null <code>String</code> value
     */
    public String getUsername() {
        return username;
    }
    
     /**
     * Sets predefined login buttin text.
     * @param username predifined login username
     */
    public void setLoginButtonText(String loginButtonText) {
        if (loginButtonText == null) {
            username = "";
        }
        this.loginButtonText = loginButtonText;
    }

    /**
     * Returns login button text
     * @return non null <code>String</code> value
     */
    public String getLoginButtonText() {
        return loginButtonText;
    }

    /**
     * Sets predefined login password.
     * @param password predefined login password
     */
    public void setPassword(String password) {
        if (password == null) {
            password = "";
        }
        this.password = password;
        char[] shownPwd = new char[password.length()];
        for (int i = 0; i < shownPwd.length; i++) {
            shownPwd[i] = PASSWORD_CHAR;
        }
        this.shownPassword = new String(shownPwd, 0, shownPwd.length);
    }

    /**
     * Returns login password
     * @return non null <code>String</code> value
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets login title text.
     * @param loginTitleText login title text
     */
    public void setLoginTitle(String loginTitleText) {
        this.loginScreenTitle = loginTitleText;
    }

    /**
     * Sets use of the login button.
     * @param useLoginButton <code>Boolean.TRUE</code> login buton in used, <code>Boolean.FALSE</code>
     * login button is NOT used
     */
    public void setUseLoginButton(boolean useLoginButton) {
        this.useLoginButton = useLoginButton;
        if (!useLoginButton) {
            addCommand(LOGIN_COMMAND);
        } else {
            removeCommand(LOGIN_COMMAND);
        }
    }

    /**
     * Sets username and password label.
     * @param usernameLabel username label
     * @param passwordLabel password label
     */
    public void setLabelTexts(String usernameLabel, String passwordLabel) {
        this.usernameLabel = usernameLabel;
        this.passwordLabel = passwordLabel;
    }

    /**
     * Returns dispaly object.
     * @return non null Display object
     */
    public Display getDisplay() {
        return display;
    }

    /**
     * Sets default border styles.
     */
    public void setDefaulBorderStyles() {
        borderStyle = getDisplay().getBorderStyle(false);
        hiBorderStyle = getDisplay().getBorderStyle(true);
    }

    /**
     * Default border styles.
     */
    public void setDefaultFonts() {
        titleFont = Font.getFont(Font.FONT_STATIC_TEXT);
        inputFont = Font.getFont(Font.FONT_INPUT_TEXT);
        loginButtonFont = titleFont;
    }

    /**
     * Sets login screen fonts.
     * @param titleFont fonts used in title
     * @param inputFont font used in username and password text fields
     * @param loginButtonFont fonts used in login button
     */
    public void setFonts(Font titleFont, Font inputFont, Font loginButtonFont) {
        this.titleFont = titleFont;
        this.inputFont = inputFont;
        this.loginButtonFont = loginButtonFont;
    }

    /**
     * Sets backbround image anchor point.
     * @param anchorPoint image anchor paint
     */
    public void setBackgroundImageAnchorPoint(int anchorPoint) {
        this.backgroundImageAnchorPoint = anchorPoint;
    }

    /**
     * The implementation calls showNotify()  immediately prior to this <code>Canvas</code>
     * being made visible on the display. <code>Canvas</code> subclasses may override this method
     * to perform tasks before being shown, such as setting up animations, starting
     * timers, etc.
     */
    protected void showNotify() {
        computeMetrics();
    }

    /**
     * Called when the drawable area of the <code>Canvas</code> has been changed. This method
     * has augmented semantics compared to Displayable.sizeChanged.
     * In addition to the causes listed in Displayable.sizeChanged, a size
     * change can occur on a <code>Canvas</code> because of a change between normal and full-screen modes.
     * If the size of a <code>Canvas</code> changes while it is actually visible on the display,
     * it may trigger an automatic repaint request. If this occurs, the call to size  Changed will
     * occur prior to the call to paint. If the <code>Canvas</code> has become smaller, the
     * implementation may choose not to trigger a repaint request if the remaining
     * contents of the <code>Canvas</code> have been preserved. Similarly, if the <code>Canvas</code> has become
     * larger, the implementation may choose to trigger a repaint only for the new region.
     * In both cases, the preserved contents must remain stationary with respect to the origin
     * of the <code>Canvas</code>. If the size change is significant to the contents of the <code>Canvas</code>,
     * the application must explicitly issue a repaint request for the changed areas.
     * Note that the application's repaint request should not cause multiple repaints,
     * since it can be coalesced with repaint requests that are already pending.
     * If the size of a <code>Canvas</code> changes while it is not visible, the implementation
     * may choose to delay calls to sizeChanged until immediately prior to the call
     * to showNotify. In that case, there will be only one call to sizeChanged,
     * regardless of the number of size changes.
     * An application that is sensitive to size changes can update instance
     * variables in its implementation of sizeChanged. These updated values
     * will be available to the code in the showNotify, hideNotify, and paint methods.
     * @param w the new width in pixels of the drawable area of the <code>Canvas</code>
     * @param h the new height in pixels of the drawable area of the <code>Canvas</code>
     */
    protected void sizeChanged(int w, int h) {
        computeMetrics();
    }


    private int computeYMetrics(int baseY) {
        loginTitleY = baseY + 2 * labelPadding;
        usernameY = loginTitleY + titleFont.getHeight() + 2 * labelPadding;
        passwordY = usernameY + inputFieldsHeight + labelPadding;
        loginButtonY = passwordY + inputFieldsHeight + 2 * labelPadding;
        return loginButtonY + loginButtonHeight;
    }


    private void computeMetrics() {
        final int width = getWidth();
        final int height = getHeight();
        final int centerY = height / 2;
        final int centerX = width / 2;

        inputFieldsWidth = inputFont.charWidth('X') * visibleInputFieldLength + 2 * borderPadding;
        if (inputFieldsWidth > width) {
            inputFieldsWidth = width - 2;
        }
        inputFieldsHeight = inputFont.getHeight() + 2 * borderPadding;

        int componentsHeight = computeYMetrics(0);

        int newbaseY = (height - componentsHeight) / 4;
        if (newbaseY > 0) {
            computeYMetrics(newbaseY);
        }

        usernameLabelWidth = inputFont.stringWidth(usernameLabel);
        passwordLabelWidth = inputFont.stringWidth(passwordLabel);
        final int labelWidth = Math.max(usernameLabelWidth, passwordLabelWidth);
        usernameLabelWidth = labelWidth;
        passwordLabelWidth = labelWidth;

        usernameX = centerX - (usernameLabelWidth + labelPadding + inputFieldsWidth) / 2;
        if (usernameX < 0) {
            usernameX = 0;
            inputFieldsWidth = width - labelWidth - labelPadding - 1;
        }
        passwordX = centerX - (passwordLabelWidth + labelPadding + inputFieldsWidth) / 2;
        if (passwordX < 0) {
            passwordY = 0;
            inputFieldsWidth = width - labelWidth - labelPadding - 1;
        }

        loginButtonHeight = loginButtonFont.getHeight() + borderPadding * 2;
        loginButtonWidth = loginButtonFont.stringWidth("X" + loginButtonText + "X") + borderPadding * 2;
        loginButtonX = centerX - loginButtonWidth / 2;
    }


    private static void setColorByState(Graphics g, int baseColor, int hiColor, boolean active) {
        if (active) {
            g.setColor(hiColor);
        } else {
            g.setColor(baseColor);
        }
    }


    private static void setStyleByState(Graphics g, int baseStyle, int hiStyle, boolean active) {
        if (active) {
            g.setStrokeStyle(baseStyle);
        } else {
            g.setStrokeStyle(hiStyle);
        }
    }

    /**
     * Returns component's color schema.
     * @return component's color schema
     */
    public ColorSchema getColorSchema() {
        return colorSchema;
    }

    /**
     * Paints this canvas.
     * @param graphics the <code>Graphics</code> object to be used for rendering the <code>Canvas</code>
     */
    protected void paint(Graphics graphics) {
        //System.out.println("CLIPX: "+g.getClipX()+","+g.getClipY()+","+g.getClipWidth()+","+g.getClipHeight());
        int width = getWidth();
        int height = getHeight();
        //System.out.println("WIdth = "+width+", hei="+height);
        int centerX = width / 2;

        getColorSchema().paintBackground(graphics, false);
        // draw login screen title
        if (loginScreenTitle != null) {
            graphics.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
            graphics.setFont(titleFont);
            graphics.drawString(loginScreenTitle, centerX, loginTitleY, Graphics.HCENTER | Graphics.TOP);
        }

        // draw username
        graphics.setFont(inputFont);
        //g.setColor(textboxBackgroundColor);
        int x;
        int y;
        int w;
        int h;
        //x = center - inputFieldsWidth/2;
        x = usernameX + usernameLabelWidth + labelPadding;
        y = usernameY; // - inputFieldsHeight + borderPadding;
        w = inputFieldsWidth;
        h = inputFieldsHeight;
        graphics.setColor(0xffffff);
        graphics.fillRoundRect(x, y, w, h, 6, 6);
        boolean usernameActive = activeField == ACTIVE_USERNAME;
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BORDER), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BORDER), usernameActive);
        setStyleByState(graphics, borderStyle, hiBorderStyle, usernameActive);
        graphics.drawRoundRect(x, y, w, h, 6, 6);
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_FOREGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND), usernameActive);
        graphics.setClip(x + borderPadding, y + borderPadding, w - 2 * borderPadding, h - 2 * borderPadding);
        graphics.drawString(username, x + borderPadding, usernameY + borderPadding, Graphics.LEFT | Graphics.TOP);
        graphics.setClip(0, 0, width, height);
        graphics.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
        graphics.drawString(usernameLabel, usernameX, usernameY + borderPadding, Graphics.LEFT | Graphics.TOP);
        // draw password
        boolean passwordActive = activeField == ACTIVE_PASSWORD;
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BACKGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND), passwordActive);
        //x = center - inputFieldsWidth/2;
        x = passwordX + passwordLabelWidth + labelPadding;
        y = passwordY; // - inputFieldsHeight + borderPadding;
        w = inputFieldsWidth;
        h = inputFieldsHeight;
        graphics.setColor(0xffffff);
        graphics.fillRoundRect(x, y, w, h, 6, 6);
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BORDER), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BORDER), passwordActive);
        setStyleByState(graphics, borderStyle, hiBorderStyle, passwordActive);
        graphics.drawRoundRect(x, y, w, h, 6, 6);
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_FOREGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND), passwordActive);
        graphics.setClip(x + borderPadding, y + borderPadding, w - 2 * borderPadding, h - 2 * borderPadding);
        graphics.drawString(shownPassword, x + borderPadding, y + borderPadding, Graphics.LEFT | Graphics.TOP);
        graphics.setClip(0, 0, width, height);
        graphics.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
        graphics.drawString(passwordLabel, passwordX, passwordY + borderPadding, Graphics.LEFT | Graphics.TOP);

        // draw login button
        if (useLoginButton) {
            boolean loginButtonActive = activeField == ACTIVE_LOGIN_BUTTON;
            setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BACKGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND), loginButtonActive);
            x = loginButtonX;
            y = loginButtonY;
            w = loginButtonWidth;
            h = loginButtonHeight;
            graphics.fillRoundRect(x, y, w, h, 6, 6);
            setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BORDER), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BORDER), loginButtonActive);
            setStyleByState(graphics, borderStyle, hiBorderStyle, loginButtonActive);
            graphics.drawRoundRect(x, y, w, h, 6, 6);
            setColorByState(graphics, getColorSchema().getColor(Display.COLOR_FOREGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND), loginButtonActive);
            //g.setClip(x+borderPadding,y+borderPadding,w-2*borderPadding,h-2*borderPadding);
            graphics.setFont(loginButtonFont);
            graphics.drawString(loginButtonText, centerX, y + borderPadding, Graphics.HCENTER | Graphics.TOP);
            graphics.setClip(0, 0, width, height);
        }
    }

    private int getLastActiveItem() {
        return useLoginButton ? ACTIVE_LOGIN_BUTTON : ACTIVE_PASSWORD;
    }


    private void moveActiveField(boolean down) {
        int add = down ? 1 : -1;
        activeField += add;
        if (activeField > getLastActiveItem()) {
            activeField = ACTIVE_USERNAME;
        } else if (activeField < ACTIVE_USERNAME) {
            activeField = getLastActiveItem();
        }
        repaint();
    }


    private void fireLoginEvent() {
        CommandListener l = getCommandListener();
        if (l != null) {
            l.commandAction(LOGIN_COMMAND, this);
        }
    }

    /**
     * Called when a key is released.
     * @param keyCode the key code of the key that was released
     */
    protected void keyReleased(int keyCode) {
        final int gameAction = getGameAction(keyCode);
        if (inputTextIsActive) {
        } else {
            switch (gameAction) {
                case FIRE:
                    if (activeField == ACTIVE_LOGIN_BUTTON) {
                        fireLoginEvent();
                    } else {
                        startEditingInputText();
                    }
                    return;
            }
        }
    }

    /**
     * Called when a key is pressed.
     * @param keyCode the key code of the key that was pressed
     */
    protected void keyPressed(int keyCode) {
        final int gameAction = getGameAction(keyCode);
        if (inputTextIsActive) {
        } else {
            switch (gameAction) {
                case LEFT:
                case DOWN:
                    moveActiveField(true);
                    return;
                case RIGHT:
                case UP:
                    moveActiveField(false);
                    return;
                    /*case FIRE:
                    startEditingInputText();
                    return;
                     */
            }
        }
    }


    private InputTextBox getInputTextBox() {
        if (inputTextBox == null) {
            inputTextBox = new InputTextBox();
        }
        return inputTextBox;
    }

    private void startEditingInputText() {
        inputTextIsActive = true;
        if (useTextBoxForInput) {
            getInputTextBox().setTextBoxMode(activeField);
            if (activeField == ACTIVE_USERNAME) {
                getInputTextBox().setString(username);
            } else if (activeField == ACTIVE_PASSWORD) {
                getInputTextBox().setString(password);
            }
            //System.out.println("inputtextbox: "+getInputTextBox());
            getDisplay().callSerially(new Runnable() {

                public void run() {
                    getDisplay().setCurrent(getInputTextBox());
                }
            });
        }
    }

    private void stopEditingInputText(boolean confirmChanges) {
        inputTextIsActive = false;
        if (useTextBoxForInput) {
            if (confirmChanges) {
                if (activeField == ACTIVE_USERNAME) {
                    setUsername(getInputTextBox().getString());
                } else if (activeField == ACTIVE_PASSWORD) {
                    setPassword(getInputTextBox().getString());
                }
            }
            getDisplay().setCurrent(this);
        }
    }

    /**
     * Sets component's command listener.
     * @param l CommandListener
     */
    public void setCommandListener(CommandListener l) {
        this.l = l;
    }

    /**
     * Returns command listener.
     * @return commandListener non null CommandListener object
     */
    public CommandListener getCommandListener() {
        return l;
    }

    /**
     * Sets component's background color.
     * @param color background color
     */
    public void setBGColor(int color) {
        ((DefaultColorSchema) colorSchema).setBGColor(color);
    }

    /**
     * Sets component's foreground color.
     * @param color foreground color
     */
    public void setFGColor(int color) {
        ((DefaultColorSchema) colorSchema).setFGColor(color);
    }

    /**
     * Indicates that a command event has occurred on Displayable d.
     * @param c a <code>Command</code> object identifying the command. This is either
     * one of the applications have been added to <code>Displayable</code> with addCommand(Command)
     * or is the implicit <code>SELECT_COMMAND</code> of List.
     * @param d the <code>Displayable</code> on which this event has occurred
     */
    public void commandAction(Command c, Displayable d) {
        if (d == this) {
            if (c.equals(LOGIN_COMMAND)) {
                fireLoginEvent();
            } else if (l != null) {
                l.commandAction(c, d);
            }
        }
    }

    private class InputTextBox extends TextBox implements CommandListener {

        private final Command CONFIRM_COMMAND = new Command("OK", Command.ITEM, 1);
        private final Command CANCEL_COMMAND = new Command("Cancel", Command.CANCEL, 1);

        public InputTextBox() {
            super(null, null, maximumInputSize, 0);
            InputTextBox.this.setCommandListener(this);
            addCommand(CONFIRM_COMMAND);
            addCommand(CANCEL_COMMAND);
        }

        public void setTextBoxMode(int mode) {
            if (mode == ACTIVE_USERNAME) {
                setConstraints(TextField.NON_PREDICTIVE);
                setTitle(usernameLabel);
            } else if (mode == ACTIVE_PASSWORD) {
                setConstraints(TextField.NON_PREDICTIVE | TextField.PASSWORD);
                setTitle(passwordLabel);
            } else {
                // else something wrong has happened
                throw new IllegalArgumentException("Wrong mode: " + mode);
            }
        }

        /**
         * Indicates that a command event has occurred on Displayable d.
         * @param c a <code>Command</code> object identifying the command. This is either
         * one of the applications have been added to <code>Displayable</code> with addCommand(Command)
         * or is the implicit <code>SELECT_COMMAND</code> of List.
         * @param d the <code>Displayable</code> on which this event has occurred
         */
        public void commandAction(Command c, Displayable d) {

            if (d == this) {
                if (c == CONFIRM_COMMAND) {
                    // confirm
                    stopEditingInputText(true);
                } else if (c == CANCEL_COMMAND) {
                    // cancel
                    stopEditingInputText(false);
                }
            }
        }
    }
}
