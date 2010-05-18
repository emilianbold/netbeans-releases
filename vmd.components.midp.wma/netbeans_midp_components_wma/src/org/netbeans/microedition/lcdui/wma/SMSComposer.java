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

package org.netbeans.microedition.lcdui.wma;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import org.netbeans.microedition.lcdui.laf.ColorSchema;
import org.netbeans.microedition.lcdui.laf.DefaultColorSchema;

/**
 * The <code>SMSComposer</code> provides an easy and convenient way to send SMS messages
 *  directly from JavaME application.
 * @author
 */
public class SMSComposer extends Canvas implements CommandListener {

    private static String sendCommandName = "Send";
    /**
     * Command fired when SMS sent.
     */
    public static Command SEND_COMMAND = new Command(sendCommandName, Command.OK, 1);
    private String phoneNumber = "";
    private String message = "";
    private Display display;
    private int portNum = 50000;
    private boolean useTextBoxForInput = true;
    private boolean inputTextIsActive = false;
    private int borderStyle;
    private int hiBorderStyle;
    private Font inputFont;
    private static final int borderPadding = 2;
    private static final int labelPadding = 2;
    private int phoneNumberY;
    private int messageY;
    private int phoneNumberX;
    private int messageX;
    private int inputFieldsWidth;
    private int inputFieldsHeight;
    private int phoneNumberLabelWidth;
    private int messageLabelWidth;
    private static final int ACTIVE_PHONE_NUMBER = 1;
    private static final int ACTIVE_MESSAGE = 2;
    private int activeField = ACTIVE_PHONE_NUMBER;
    private String phoneNumberLabel;
    private String messageLabel;
    private CommandListener l;
    private ColorSchema colorSchema = new DefaultColorSchema();
    private InputTextBox phoneBox;
    private InputTextBox msgBox;
    private InputTextBox current = null;
    private boolean sendAutomatically = true;

    /**
     * Creates a new instance of SMSComposer for given <code>Display</code> object.
     * @param display A non-null display object.
     */
    public SMSComposer(Display display) {
        this.display = display;
        setDefaulBorderStyles();
        setDefaultFonts();
        addCommand(SEND_COMMAND);
        super.setCommandListener(this);

        // default values
        this.phoneNumberLabel = "Phone Number:";
        this.messageLabel = "Message:";
        phoneBox = new InputTextBox(phoneNumberLabel, 20, TextField.PHONENUMBER);
        msgBox = new InputTextBox(messageLabel, 160, TextField.ANY);
    }

    /**
     * Sets the phone number.
     * @param phoneNumber phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            phoneNumber = "";
        }
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets the phone number label.
     * @param phoneNumberLabel phone number label
     */
    public void setPhoneNumberLabel(String phoneNumberLabel) {
        if (phoneNumberLabel == null) {
            phoneNumberLabel = "";
        }
        this.phoneNumberLabel = phoneNumberLabel;
        phoneBox.setTitle(phoneNumberLabel);
    }

    /**
     * Sets the message text.
     * @param message message text
     */
    public void setMessage(String message) {
        if (message == null) {
            message = "";
        }
        this.message = message;
    }

    /**
     * Sets the message text label.
     * @param messageLabel message text label
     */
    public void setMessageLabel(String messageLabel) {
        if (messageLabel == null) {
            messageLabel = "";
        }
        this.messageLabel = messageLabel;
        msgBox.setTitle(messageLabel);
    }

    /**
     * Sets the port number
     * @param portNum port number
     */
    public void setPort(int portNum) {
        this.portNum = portNum;
    }

    /**
     * Controlls execution of sendSMS method. Default value of sendAutomaticly argument is Boolean.TRUE which means that method <code>sendSMS</code> is
     * invoked when command Send pressed. If sendAutomaticly is Boolean.FALSE then <code>sendSMS</code> method is not invoked after Send command is pressed.
     * @param sendAutomatically Boolean.TRUE sendSMS method is executed, Boolean.FALSE sendSMS method is NOT executed
     */
    public void setSendAutomatically(boolean sendAutomatically) {
        this.sendAutomatically = sendAutomatically;
    }

    /**
     * Sends the message based on given non null phone number and port number.
     * @throws IOException if the message could not be sent or because 
     * of network failure or if the connection is not available
     */
    public void sendSMS() throws IOException {
        if (phoneNumber == null) {
            throw new IllegalArgumentException();
        }
        String address = "sms://" + phoneNumber + ":" + portNum;
        MessageConnection smsconn = null;
        /** Open the message connection. */
        smsconn = (MessageConnection) Connector.open(address);
        TextMessage txtmessage = (TextMessage) smsconn.newMessage(MessageConnection.TEXT_MESSAGE);
        txtmessage.setAddress(address);
        txtmessage.setPayloadText(message);
        smsconn.send(txtmessage);
        smsconn.close();
    }

    /**
     * Sets Default border styles.
     */
    public void setDefaulBorderStyles() {
        borderStyle = getDisplay().getBorderStyle(false);
        hiBorderStyle = getDisplay().getBorderStyle(true);
    }

    /**
     * Sets Default fonts.
     */
    public void setDefaultFonts() {
        inputFont = Font.getFont(Font.FONT_INPUT_TEXT);
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
        phoneNumberY = baseY + labelPadding;
        messageY = phoneNumberY + inputFieldsHeight + labelPadding;
        return messageY + inputFieldsHeight;
    }

    private void computeMetrics() {

        final int width = getWidth();
        final int height = getHeight();
        final int centerY = height / 2;
        final int centerX = width / 2;
        int visibleInputFieldLength = 12;
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

        phoneNumberLabelWidth = inputFont.stringWidth(phoneNumberLabel);
        messageLabelWidth = inputFont.stringWidth(messageLabel);
        final int labelWidth = Math.max(phoneNumberLabelWidth, messageLabelWidth);
        phoneNumberLabelWidth = labelWidth;
        messageLabelWidth = labelWidth;

        phoneNumberX = centerX - (phoneNumberLabelWidth + labelPadding + inputFieldsWidth) / 2;
        if (phoneNumberX < 0) {
            phoneNumberX = 0;
            inputFieldsWidth = width - labelWidth - labelPadding - 1;
        }
        messageX = centerX - (messageLabelWidth + labelPadding + inputFieldsWidth) / 2;
        if (messageX < 0) {
            messageY = 0;
            inputFieldsWidth = width - labelWidth - labelPadding - 1;
        }
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
     * Returnd component's color schema.
     * @return colorSchema
     */
    public ColorSchema getColorSchema() {
        return colorSchema;
    }

    /**
     * Paints this canvas.
     * @param graphics the <code>Graphic</code> object to be used for rendering the <code>Canvas</code>
     */
    protected void paint(Graphics graphics) {
        //System.out.println("CLIPX: "+g.getClipX()+","+g.getClipY()+","+g.getClipWidth()+","+g.getClipHeight());
        int width = getWidth();
        int height = getHeight();
        //System.out.println("WIdth = "+width+", hei="+height);
        int centerX = width / 2;

        getColorSchema().paintBackground(graphics, false);

        // draw phoneNumber
        graphics.setFont(inputFont);
        //g.setColor(textboxBackgroundColor);
        int x;
        int y;
        int w;
        int h;
        //x = center - inputFieldsWidth/2;
        x = phoneNumberX + phoneNumberLabelWidth + labelPadding;
        y = phoneNumberY; // - inputFieldsHeight + borderPadding;
        w = inputFieldsWidth;
        h = inputFieldsHeight;
        graphics.setColor(0xffffff);
        graphics.fillRoundRect(x, y, w, h, 6, 6);
        boolean phoneNumberActive = activeField == ACTIVE_PHONE_NUMBER;
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BORDER), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BORDER), phoneNumberActive);
        setStyleByState(graphics, borderStyle, hiBorderStyle, phoneNumberActive);
        graphics.drawRoundRect(x, y, w, h, 6, 6);
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_FOREGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND), phoneNumberActive);
        graphics.setClip(x + borderPadding, y + borderPadding, w - 2 * borderPadding, h - 2 * borderPadding);
        graphics.drawString(phoneNumber, x + borderPadding, phoneNumberY + borderPadding, Graphics.LEFT | Graphics.TOP);
        graphics.setClip(0, 0, width, height);
        graphics.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
        graphics.drawString(phoneNumberLabel, phoneNumberX, phoneNumberY + borderPadding, Graphics.LEFT | Graphics.TOP);
        // draw message
        boolean messageActive = activeField == ACTIVE_MESSAGE;
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BACKGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND), messageActive);
        //x = center - inputFieldsWidth/2;
        x = messageX + messageLabelWidth + labelPadding;
        y = messageY; // - inputFieldsHeight + borderPadding;
        w = inputFieldsWidth;
        h = inputFieldsHeight;
        graphics.setColor(0xffffff);
        graphics.fillRoundRect(borderPadding, y + inputFieldsHeight + labelPadding, width - 2 * borderPadding, height - (y + inputFieldsHeight + labelPadding) - 2 * borderPadding, 6, 6);
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_BORDER), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_BORDER), messageActive);
        setStyleByState(graphics, borderStyle, hiBorderStyle, messageActive);
        graphics.drawRoundRect(borderPadding, y + inputFieldsHeight + labelPadding, width - 2 * borderPadding, height - (y + inputFieldsHeight + labelPadding) - 2 * borderPadding, 6, 6);
        setColorByState(graphics, getColorSchema().getColor(Display.COLOR_FOREGROUND), getColorSchema().getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND), messageActive);

        graphics.setClip(borderPadding, y + inputFieldsHeight + labelPadding, width - 2 * borderPadding, height - (y + inputFieldsHeight + labelPadding) - 2 * borderPadding);

        int messageLength = message.length();
        int currentWidth = 0;
        int startPoint = 0;
        int line = 1;

        for (int i = 0; i < messageLength; i++) {
            char c = message.charAt(i);
            int cWidth = inputFont.stringWidth("" + c);
            currentWidth += cWidth;
            if (currentWidth >= width - 3 * borderPadding) {
                //go back one char
                i--;
                String subMsg = message.substring(startPoint, i);
                graphics.drawString(subMsg, 2 * borderPadding, y + line * (inputFieldsHeight) + labelPadding + borderPadding, Graphics.LEFT | Graphics.TOP);
                startPoint = i;
                currentWidth = 0;
                line++;
            }
        }
        String subMsg = message.substring(startPoint, messageLength);
        graphics.drawString(subMsg, 2 * borderPadding, y + line * (inputFieldsHeight) + labelPadding + borderPadding, Graphics.LEFT | Graphics.TOP);


        graphics.setClip(0, 0, width, height);
        graphics.setColor(getColorSchema().getColor(Display.COLOR_FOREGROUND));
        graphics.drawString(messageLabel, messageX, messageY + borderPadding, Graphics.LEFT | Graphics.TOP);
    }

    private void moveActiveField(boolean down) {
        int add = down ? 1 : -1;
        activeField += add;
        if (activeField > ACTIVE_MESSAGE) {
            activeField = ACTIVE_PHONE_NUMBER;
        } else if (activeField < ACTIVE_PHONE_NUMBER) {
            activeField = ACTIVE_MESSAGE;
        }
        repaint();
    }

    private void fireSendEvent() {
        CommandListener l = getCommandListener();
        if (l != null) {
            l.commandAction(SEND_COMMAND, this);
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
                    startEditingInputText();
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
                    /*
                    case FIRE:
                    startEditingInputText();
                    return;
                     */
            }
        }
    }

    private void startEditingInputText() {
        inputTextIsActive = true;
        if (useTextBoxForInput) {
            if (activeField == ACTIVE_PHONE_NUMBER) {
                phoneBox.setString(phoneNumber);
                current = phoneBox;
            } else if (activeField == ACTIVE_MESSAGE) {
                msgBox.setString(message);
                current = msgBox;
            }
            //System.out.println("inputtextbox: "+getInputTextBox());
            getDisplay().callSerially(new Runnable() {

                public void run() {
                    getDisplay().setCurrent(current);
                }
            });
        }
    }

    private void stopEditingInputText(boolean confirmChanges) {
        inputTextIsActive = false;
        if (useTextBoxForInput) {
            if (confirmChanges) {
                if (activeField == ACTIVE_PHONE_NUMBER) {
                    setPhoneNumber(current.getString());
                } else if (activeField == ACTIVE_MESSAGE) {
                    setMessage(current.getString());
                }
            }
            getDisplay().setCurrent(this);
        }
    }

    /**
     * Sets component's command listener.
     * @param listener CommandListener
     */
    public void setCommandListener(CommandListener listener) {
        this.l = listener;
    }

    /**
     * Returns component's command listener.
     * @return CommandListener
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
     * Returns dispaly.
     * @return Display
     */
    private Display getDisplay() {
        return display;
    }

    /**
     * Indicates that a command event has occurred on Displayable d.
     * @param c a Command object identifying the command. This is either
     * one of the applications have been added to Displayable with addCommand(Command)
     * or is the implicit SELECT_COMMAND of List.
     * @param d the Displayable on which this event has occurred
     */
    public void commandAction(Command c, Displayable d) {
        if (c.equals(SEND_COMMAND)) {
            if (sendAutomatically) {
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            sendSMS();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        if (l != null) {
            l.commandAction(c, d);
        }
    }

    private class InputTextBox extends TextBox implements CommandListener {

        private final Command CONFIRM_COMMAND = new Command("OK", Command.OK, 1);
        private final Command CANCEL_COMMAND = new Command("Cancel", Command.CANCEL, 1);

        public InputTextBox(String title, int maximumChars, int constraints) {
            super(title, null, maximumChars, constraints);
            setCommandListener(this);
            addCommand(CONFIRM_COMMAND);
            addCommand(CANCEL_COMMAND);
        }

        /**
         * Indicates that a command event has occurred on Displayable d.
         * @param c a <code>Command</code> object identifying the command. This is either
         * one of the applications have been added to <code>Displayable</code> with addCommand(Command)
         * or is the implicit <code>SELECT_COMMAND</code> of List.
         * @param d the <code>Displayable</code> on which this event has occurred
         */
        public void commandAction(Command c, Displayable d) {
            /*
            System.out.println("Command axtion from input text box: command="+c.getLabel()+", d="+d);
            try {
            throw new RuntimeException("test");
            } catch (RuntimeException e) {
            e.printStackTrace();
            }
             */
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
