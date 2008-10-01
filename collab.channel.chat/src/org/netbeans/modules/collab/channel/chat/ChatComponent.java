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
package org.netbeans.modules.collab.channel.chat;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabMessage;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.ContentTypes;
import com.sun.collablet.Conversation;
import com.sun.collablet.ConversationPrivilege;
import com.sun.collablet.UserInterface;
import com.sun.collablet.chat.ChatCollablet;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import org.openide.ErrorManager;

import org.openide.awt.*;
import org.openide.util.*;
import org.openide.windows.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.datatransfer.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.lang.reflect.*;

import java.net.URL;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.text.*;
import javax.swing.text.html.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.channel.chat.messagetype.CollabContentType;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public class ChatComponent extends JPanel implements HyperlinkListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L;
    private static final String END_ELEMENT_ID = "END";
    private static final String MESSAGE_TEMPLATE_ELEMENT_ID = "MESSAGE_TEMPLATE";
    private static final String SYSTEM_MESSAGE_TEMPLATE_ELEMENT_ID = "SYSTEM_MESSAGE_TEMPLATE";
    private static final String CHAT_MESSAGE_TEMPLATE_ELEMENT_ID = "CHAT_MESSAGE_TEMPLATE";
    private static final String CHAT_TEMPLATE_RESOURCE = "/org/netbeans/modules/collab/channel/chat/chat_template.html";
    private static String[][] smiles = new String[][] {
        { ":-)", "emo_smiley16.png" },
        { ":)",  "emo_smiley16.png" },
        { ":-(", "emo_sad16.png" },
        { ":(",  "emo_sad16.png" },
        { ";-)", "emo_wink16.png" },
        { ";)",  "emo_wink16.png" },
        { ":=)", "emo_laughing16.png" },
        { "8-)", "emo_cool16.png" },
        { ":-D", "emo_grin16.png" }};

    // NOTE: These constants must be kept in sync with the values in the
    // HTML template file!
    private static final String TOKEN_SENDER_CLASS = "__SENDER_CLASS__";
    private static final String TOKEN_SENDER = "__SENDER__";
    private static final String TOKEN_MESSAGE_CLASS = "__MESSAGE_CLASS__";
    private static final String TOKEN_MESSAGE = "__MESSAGE__";
    private static final String TOKEN_MESSAGE_TEXT = "__MESSAGE_TEXT__";
    private static final String TOKEN_TIMESTAMP = "__TIMESTAMP__";
    private static final String TOKEN_CONTENT_TYPE = "__CONTENT_TYPE__";
    private static final String MESSAGE_CLASS_CHAT = "message-chat";
    private static final String MESSAGE_CLASS_TEXT = "message-text";
    private static final String MESSAGE_CLASS_HTML = "message-html";
    private static final String MESSAGE_CLASS_SYSTEM = "message-system";
    private static final String MESSAGE_CLASS_OTHER = "message-other";
    private static final String MESSAGE_CLASS_XML = "message-xml";
    private static final String MESSAGE_CLASS_JAVA = "message-java";
    private static final String DEFAULT_SENDER_CLASS = "sender-default";
    private static final int MAX_SENDER_CLASSES = 9;
    private static final String TOKEN_BASE_FONT_SIZE = "@BASE_FONT_SIZE@";
    private static final String TOKEN_SMALL_FONT_SIZE = "@SMALL_FONT_SIZE@";

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private ChatCollablet channel;
    private JEditorPane transcriptPane;
    private ChatInputPane inputPane;
    private JButton sendButton;
    private JLabel typingMessageLabel;
    private String inputContentType = ContentTypes.UNKNOWN_TEXT;
    private String previouslySelectedContentType;
    private Element endElement;
    private String messageTemplate;
    private String chatMessageTemplate;
    private String systemMessageTemplate;
    private Map senderStyles = new HashMap();
    private Map contentTypeToButton = new HashMap();
    private int lastSenderStyleIndex;
    private ConversationChangeListener conversationListener;
    private javax.swing.Timer typingTimer = null;
    private int TYPE_TIMER_INTERVAL = 2000;
    private Set typingParticipants = Collections.synchronizedSet(new HashSet());
    private JPopupMenu popupMenu;
    private JPopupMenu newMenu;
    private JButton smileyButton;
    private JToggleButton chatFocusButton;

    /**
     *
     *
     */
    public ChatComponent(ChatCollablet channel) {
        super();
        this.channel = channel;

        initialize();
        initTypingTimer();

        // Listen to conversation changes
        conversationListener = new ConversationChangeListener();
        channel.addPropertyChangeListener(conversationListener);
        channel.getConversation().addPropertyChangeListener(conversationListener);

        // To update Find, Copy, etc. actions, add to constructor:
        //		ActionMap map = getActionMap();
        //		Action findBinding = new MyFindAction(this);
        //		map.put(((CallbackSystemAction)SystemAction.get(
        //			FindAction.class)).getActionMapKey(), findBinding);
        // Ensure that the first sender style is reserved for us
        lastSenderStyleIndex++;
        allocateNewSenderStyleClass(channel.getConversation().getCollabSession().getUserPrincipal().getDisplayName());

        if (chatFocusButton != null) {
            chatFocusButton.setSelected(true);
        }

        setHelpCtx();
    }

    /**
     *set help ctx map id for context sensitive help
     *
     */
    private void setHelpCtx() {
        HelpCtx.setHelpIDString(this, "collab_chat_overview"); //NOI18n
    }

    /**
     *
     *
     */
    private void initialize() {
        // Set our layout--if we don't do this, nothing will appear in
        // the component
        setLayout(new BorderLayout());

        JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        splitPanel.setResizeWeight(0.85f);
        splitPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // We must set the editor kit explicitly in order to keep NetBeans
        // from installing its own document type
        transcriptPane = new JEditorPane();
        transcriptPane.setEditable(false);

        // Install our own editor kit in order to ensure rendering of the
        // message template element is no-op'd
        transcriptPane.setEditorKit(
            new HTMLEditorKit() {
                public ViewFactory getViewFactory() {
                    return new ChatHTMLFactory();
                }
            }
        );

        createPopupMenu();
        createSmileMenu();

        initializeTranscriptTemplate();

        // Listen to clicked links
        transcriptPane.addHyperlinkListener(this);

        // Create a scroll pane to contain the conversation transcript
        JScrollPane transcriptScrollPane1 = new JScrollPane(transcriptPane);
        transcriptScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel transcriptPanel = new JPanel(new BorderLayout());
        transcriptPanel.add(transcriptScrollPane1, BorderLayout.CENTER);
        splitPanel.add(transcriptPanel, JSplitPane.TOP);

        // Panel to hold the various input components
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        inputPanel.setLayout(new GridBagLayout());

        boolean canSendMessages = false;

        try {
            if (
                (getChannel().getConversation().getPrivilege() == ConversationPrivilege.MANAGE) ||
                    (getChannel().getConversation().getPrivilege() == ConversationPrivilege.WRITE)
            ) {
                canSendMessages = true;
            }
        } catch (CollabException e) {
            canSendMessages = false;
        }

        if (canSendMessages) {
            // Create a send button
            sendButton = new JButton() {
                public void transferFocusBackward() {
                    // Make sure the prior component is always the input pane
                    getInputPane().requestFocus();
                }
            };
            Mnemonics.setLocalizedText(sendButton, 
                    NbBundle.getMessage(ChatComponent.class, "LBL_ChatComponent_SendButton"));
            sendButton.addActionListener(new SendButtonActionListener());

            // Add the toolbar
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = gbc.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            inputPanel.add(initializeToolbar(), gbc);
            gbc = new GridBagConstraints();
            gbc.fill = gbc.NONE;
            gbc.anchor = gbc.EAST;
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            inputPanel.add(sendButton, gbc);

            // Create an input pane
            inputPane = new ChatInputPane(this);
            inputPane.setEditable(true);
            inputPane._setContentType(inputContentType);
            inputPane.setTransferHandler(new InputPaneTransferHandler(inputPane.getTransferHandler()));
            
            KeyStroke snd = KeyStroke.getKeyStroke("control ENTER"); // NOI18N
            inputPane.getInputMap().put(snd, "sendAction"); // NOI18N
            inputPane.getActionMap().put("sendAction", new SendButtonActionListener());

            // registers itself
            InputPaneDocumentListener lst = new InputPaneDocumentListener();

            // Create a scroll pane to contain the input pane
            JScrollPane inputScrollPane = new JScrollPane(inputPane);
            inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            gbc = new GridBagConstraints();
            gbc.fill = gbc.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            inputPanel.add(inputScrollPane, gbc);

            typingMessageLabel = new JLabel();
            gbc = new GridBagConstraints();
            gbc.fill = gbc.HORIZONTAL;
            gbc.anchor = gbc.CENTER;
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            inputPanel.add(typingMessageLabel, gbc);
        } else {
            // Instead, add a label that indicates that the user cannot
            // send messages in this conversation
            JLabel messageLabel = new JLabel(
                    NbBundle.getMessage(ChatComponent.class, "LBL_ChatComponent_NoPrivilege"), // NOI18N
                    SwingConstants.CENTER
                );
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = gbc.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            inputPanel.add(messageLabel, gbc);
            typingMessageLabel = new JLabel();

            typingMessageLabel.setOpaque(true);
            gbc.fill = gbc.HORIZONTAL;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            inputPanel.add(typingMessageLabel, gbc);
        }

        splitPanel.add(inputPanel, JSplitPane.BOTTOM);

        add(splitPanel, BorderLayout.CENTER);
    }

    /**
     *
     *
     */
    private void initializeTranscriptTemplate() {
        // Initialize the chat transcript template
        try {
            // Read the template file into memory.  Note, it doesn't seem to
            // work to get the resource as a URL and call setPage() on the
            // editor pane.
            InputStream is = ChatComponent.class.getResourceAsStream(CHAT_TEMPLATE_RESOURCE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuffer buffer = new StringBuffer();
            String line = null;

            while ((line = reader.readLine()) != null)
                buffer.append(line).append("\n");

            // Determine the current font size in the IDE
            int baseFontSize = Math.max(new JLabel().getFont().getSize(), 12);
            int smallFontSize = baseFontSize - 1;

            // Replace global tokens
            String templateContent = buffer.toString();
            templateContent = StringTokenizer2.replace(templateContent, TOKEN_BASE_FONT_SIZE, "" + baseFontSize);
            templateContent = StringTokenizer2.replace(templateContent, TOKEN_SMALL_FONT_SIZE, "" + smallFontSize);

            // Set the template content
            transcriptPane.setText(templateContent);

            // Find and cache the element at which we will insert additional
            // elements
            HTMLDocument document = (HTMLDocument) transcriptPane.getDocument();
            endElement = document.getElement(END_ELEMENT_ID);

            // Get the message template HTML from the document itself
            Element templateParent = document.getElement(MESSAGE_TEMPLATE_ELEMENT_ID);

            // Switch to the implied <p> inside the <div>
            templateParent = templateParent.getElement(0);

            // Find the first comment element of the template parent element.
            // This will be our template string.
            for (int i = 0; i < templateParent.getElementCount(); i++) {
                Element element = templateParent.getElement(i);

                if (element.getAttributes().isDefined(HTML.Attribute.COMMENT)) {
                    messageTemplate = (String) element.getAttributes().getAttribute(HTML.Attribute.COMMENT);

                    break;
                }
            }

            // Get the system message template
            Element systemMsgTemplateParent = document.getElement(SYSTEM_MESSAGE_TEMPLATE_ELEMENT_ID);

            // Switch to the implied <p> inside the <div>
            systemMsgTemplateParent = systemMsgTemplateParent.getElement(0);

            // Find the first comment element of the template parent element.
            // This will be our template string.
            for (int i = 0; i < systemMsgTemplateParent.getElementCount(); i++) {
                Element element = systemMsgTemplateParent.getElement(i);

                if (element.getAttributes().isDefined(HTML.Attribute.COMMENT)) {
                    systemMessageTemplate = (String) element.getAttributes().getAttribute(HTML.Attribute.COMMENT);

                    break;
                }
            }

            // Get the chat message template
            Element chatMessageTemplateParent = document.getElement(CHAT_MESSAGE_TEMPLATE_ELEMENT_ID);

            // Switch to the implied <p> inside the <div>
            chatMessageTemplateParent = chatMessageTemplateParent.getElement(0);

            // Find the first comment element of the template parent element.
            // This will be our template string.
            for (int i = 0; i < chatMessageTemplateParent.getElementCount(); i++) {
                Element element = chatMessageTemplateParent.getElement(i);

                if (element.getAttributes().isDefined(HTML.Attribute.COMMENT)) {
                    chatMessageTemplate = (String) element.getAttributes().getAttribute(HTML.Attribute.COMMENT);

                    break;
                }
            }
        } catch (IOException e) {
            // Shouldn't happen
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    private void initTypingTimer() {
        TypingTimerActionListener actionListener = new TypingTimerActionListener();
        typingTimer = new javax.swing.Timer(TYPE_TIMER_INTERVAL, actionListener);
        typingTimer.setRepeats(false);
    }

    /**
     * Code to dynamically add the buttons by using the lookup api of the netbeans
     *
     */
    private JToolBar initializeToolbar() {
        JToolBar inputToolbar = new JToolBar(JToolBar.HORIZONTAL);
        inputToolbar.setFloatable(false);
        inputToolbar.setBorder(new CompoundBorder(new EmptyBorder(3, 0, 0, 0), inputToolbar.getBorder()));

        ContentTypeActionListener actionListener = new ContentTypeActionListener();

        ButtonGroup group = new ButtonGroup();

        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(CollabContentType.class));
        Collection messageTypeCollection = result.allInstances();
        
        int i=1;
        for (Iterator it = messageTypeCollection.iterator(); it.hasNext(); i++) {
            CollabContentType messageType = (CollabContentType)it.next();
            Image icon = messageType.getIcon();
            String displayName = messageType.getDisplayName();
            String contentType = messageType.getContentType();
            JToggleButton button = new JToggleButton(new ImageIcon(icon));
            button.putClientProperty("contentType", contentType); //NOI18N

            if (i < 9) button.setMnemonic((char)('0' + i));
            button.setToolTipText(displayName);
            if (messageType.getContentType().equals(ContentTypes.UNKNOWN_TEXT)) {
                chatFocusButton = button;
            }
            button.addActionListener(actionListener);

            group.add(button);
            inputToolbar.add(button);
            contentTypeToButton.put(contentType, button);
        }

        inputToolbar.addSeparator();

        Image smileyButtonIcon = ImageUtilities.loadImage(
                "org/netbeans/modules/collab/channel/chat/" + "resources/emoticons/emo_smiley16.png"
            ); // NOI18N        
        smileyButton = new JButton(new ImageIcon(smileyButtonIcon));
        smileyButton.setMnemonic(NbBundle.getMessage(ChatComponent.class,
                "MNE_ChatComponent_MIMEType_Smileys").charAt(0));
        smileyButton.setToolTipText(NbBundle.getMessage(ChatComponent.class, "LBL_ChatComponent_MIMEType_Smileys"));
        smileyButton.setRequestFocusEnabled(false);
        smileyButton.addActionListener(new SmileListener());
        inputToolbar.add(smileyButton);

        return inputToolbar;
    }

    /*author Smitha Krishna Nagesh*/
    public JPopupMenu createSmileMenu() {
        newMenu = new JPopupMenu();
        newMenu.setLayout(new GridLayout(3, 2));
        newMenu.setPopupSize(50, 50);
        ButtonClickedListener listener = new ButtonClickedListener();

        newMenu.add(createSmileButton(":-)", "emo_smiley", "Smile", listener));
        newMenu.add(createSmileButton(":-(", "emo_sad", "Frown", listener));
        newMenu.add(createSmileButton(";-)", "emo_wink", "Wink", listener));
        newMenu.add(createSmileButton(":=)", "emo_laughing", "Laugh", listener));
        newMenu.add(createSmileButton("8-)", "emo_cool", "Cool", listener));
        newMenu.add(createSmileButton(":-D", "emo_grin", "Grin", listener));

        newMenu.pack();

        return newMenu;
    }

    private JButton createSmileButton(String smile, String icon, String key, ActionListener listener) {
        Image img = ImageUtilities.loadImage(
                "org/netbeans/modules/collab/channel/chat/resources/emoticons/" + icon + "16.png"); // NOI18N
        JButton button = new JButton(new ImageIcon(img));
        button.setActionCommand(smile.concat(" "));
        String tip = NbBundle.getMessage(ChatComponent.class, "LBL_Emoticon_" + key);
        button.setToolTipText(tip);
        button.setMnemonic(tip.charAt(0));
        button.addActionListener(listener);

        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        button.setBorderPainted(false);
        button.setRequestFocusEnabled(false);
        return button;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accessors
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public ChatCollablet getChannel() {
        return channel;
    }

    /**
     *
     *
     */
    public JEditorPane getTranscriptPane() {
        return transcriptPane;
    }

    /**
     *
     *
     */
    public ChatInputPane getInputPane() {
        return inputPane;
    }

    /**
     *
     *
     */
    public JButton getSendButton() {
        return sendButton;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event notifications
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (getInputPane() != null) {
                        getInputPane().requestFocus();
                    }
                }
            }
        );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Input support methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public String getInputContentType() {
        return inputContentType;
    }

    /**
     *
     *
     */
    public void setInputContentType(String contentType, boolean syncUI, boolean autoselected) {
        if (contentType == null) {
            throw new IllegalArgumentException("contentType cannot be null");
        }

        // Remember what the content type was if this is happening due to
        // auto-selection during a paste.  Always remember the first content
        // type that user had manually selected.
        if (autoselected) {
            if (previouslySelectedContentType == null) {
                previouslySelectedContentType = inputContentType;
            }
        }

        //		else
        //		{
        //			previouslySelectedContentType=null;
        //		}
        inputContentType = contentType;

        // Sync the input pane; remember the text and the selection
        String text = getInputPane().getText();
        int selStart = getInputPane().getSelectionStart();
        int selEnd = getInputPane().getSelectionEnd();
        int caretPos = getInputPane().getCaretPosition();

        getInputPane()._setContentType(contentType);
        getInputPane().popUpMenu();
        getInputPane().setText(text);
        getInputPane().setCaretPosition(caretPos);
        getInputPane().setSelectionStart(selStart);
        getInputPane().setSelectionEnd(selEnd);
        getInputPane().requestFocus();

        if (syncUI) {
            SwingUtilities.invokeLater(new ContentTypeSynchronizer());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message management
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected void sendInput() {
        // Send a message to the conference
        try {
            CollabMessage message = getChannel().getConversation().createMessage();

            // Set the message content
            String content = getInputPane().getText();
            message.setContent(content);

            // Set the actual content type to a different, chat-specific header.
            // The message content type will always be text/plain
            message.setHeader(ChatCollablet.DISPLAY_CONTENT_TYPE_HEADER, getInputContentType());

            // TODO: Tag the message as belonging to the chat channel
            // Is this necessary if we are sending SOAP?
            message.setHeader("x-channel", "chat"); // NOI18N

            // Send the message
            getChannel().getConversation().sendMessage(message);
        } catch (CollabException e) {
            Debug.errorManager.notify(e);
        }

        // Clear the message text
        getInputPane().setText("");
        getInputPane().requestFocus();

        // Restore the content type if auto-selection occured
        if (previouslySelectedContentType != null) {
            setInputContentType(previouslySelectedContentType, true, false);
            previouslySelectedContentType = null;
        }
    }

    private String replaceAll(String origStr, String targetStr, String replacedStr) {
        StringBuffer sb = new StringBuffer(origStr);
        int idx;
        while((idx = sb.indexOf(targetStr)) != -1) {
            sb.replace(idx, idx+targetStr.length(), replacedStr);
        }
        return sb.toString();
    }

    private boolean strContains(String str, String pattern) {
        return str.indexOf(pattern) != -1;
    }


    /**
     *
     *
     */
    public void appendMessage(CollabMessage message) {
        try {
            // Convert message to HTML
            String html = convertToHtml(message);

            // fixme: pattern ") is first expanded to &quot;) which will then find a smile there.
            // Replace smiles only in plain text
            String contentType = message.getHeader(ChatCollablet.DISPLAY_CONTENT_TYPE_HEADER);
            if ((contentType == null) || contentType.equals(ContentTypes.UNKNOWN_TEXT)) {
                for (int i=0; i<smiles.length; i++) {
                    if (strContains(html, smiles[i][0])) {
                        URL smileUrl = ChatComponent.class.getResource(
                            "resources/emoticons/" + smiles[i][1]);
                        String tag = "<img align=\"center\" src=" + smileUrl + "></img>";
                        html = replaceAll(html, smiles[i][0], tag);
                    }
                }
            }
            
            // Insert the message text before the start of the epilogue
            HTMLDocument document = (HTMLDocument) getTranscriptPane().getDocument();
            document.insertBeforeStart(getEndElement(), html);

            //Debug.out.println("Appended:\n"+html);
            // Scroll to bottom of document
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        scrollToLastMessage();
                    }
                }
            );
        } catch (Exception e) {
            // TODO: Do something appropriate here
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void appendSystemMessage(String message) {
        try {
            String content = SyntaxColoring.convertToHTML(message, ContentTypes.TEXT);

            String html = getSystemMessageTemplate();
            html = StringTokenizer2.replace(html, TOKEN_MESSAGE_CLASS, MESSAGE_CLASS_SYSTEM);
            html = StringTokenizer2.replace(
                    html, TOKEN_TIMESTAMP, SimpleDateFormat.getTimeInstance().format(new Date())
                );
            html = StringTokenizer2.replace(html, TOKEN_MESSAGE, content);

            // Insert the message text before the start of the
            // epilogue
            HTMLDocument document = (HTMLDocument) getTranscriptPane().getDocument();
            document.insertBeforeStart(getEndElement(), html);

            // Scroll to bottom of document
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        scrollToLastMessage();
                    }
                }
            );
        } catch (Exception e) {
            // TODO: Do something appropriate here
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void scrollToLastMessage() {
        try {
            // Find the parent of the end element
            Element parentElement = getEndElement().getParentElement();

            // Figure out the last element in the parent container (besides
            // the end element)
            Element lastElement = parentElement.getElement(parentElement.getElementCount() - 2);

            // Get the element rectangle
            Rectangle rect = getTranscriptPane().modelToView(lastElement.getStartOffset());

            if (rect != null) {
                // Make the height of the element rectangle the same as
                // the visible size of the component
                Rectangle visRect = getTranscriptPane().getVisibleRect();
                rect.height = visRect.height;

                // Scroll to the last element
                getTranscriptPane().scrollRectToVisible(rect);
            }
        } catch (Exception e) {
            Debug.debugNotify(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Formatting methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the element before which new messages should be inserted
     *
     */
    protected Element getEndElement() {
        return endElement;
    }

    /**
     * Returns the message template string
     *
     */
    protected String getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * Returns the message template string
     *
     */
    protected String getChatMessageTemplate() {
        return chatMessageTemplate;
    }

    /**
     * Returns the system message template string
     *
     */
    protected String getSystemMessageTemplate() {
        return systemMessageTemplate;
    }

    /**
     *
     *
     */
    private Map getSenderStyles() {
        return senderStyles;
    }

    /**
     *
     *
     */
    protected String convertToHtml(CollabMessage message) {
        String content = null;
        String plainContent = null;
        String messageClass = null;

        String contentType = message.getHeader(ChatCollablet.DISPLAY_CONTENT_TYPE_HEADER);

        try {
            content = message.getContent();
            assert content != null : "Content was null";
            plainContent = content;

            if ((contentType == null) || contentType.equals(ContentTypes.UNKNOWN_TEXT)) {
                // Trim the content, with the assumption it is coming from
                // a plain IM client
                content = SyntaxColoring.convertToHTML(content.trim(), ContentTypes.UNKNOWN_TEXT);
                messageClass = MESSAGE_CLASS_CHAT;
                contentType = ContentTypes.UNKNOWN_TEXT;

                // Escape any hyperlinks
                content = replaceHyperlinks(content);
            } else if (contentType.equals(ContentTypes.HTML)) {
                content = SyntaxColoring.convertToHTML(content, ContentTypes.HTML);
                messageClass = MESSAGE_CLASS_HTML;

                // Escape any hyperlinks
                content = replaceHyperlinks(content);
            } else if (contentType.equals(ContentTypes.XML)) {
                content = SyntaxColoring.convertToHTML(content, ContentTypes.XML);
                messageClass = MESSAGE_CLASS_XML;

                // Escape any hyperlinks
                content = replaceHyperlinks(content);
            } else if (contentType.equals(ContentTypes.JAVA)) {
                content = SyntaxColoring.convertToHTML(content, ContentTypes.JAVA);
                messageClass = MESSAGE_CLASS_JAVA;
            } else {
                content = SyntaxColoring.convertToHTML(content, ContentTypes.TEXT);
                messageClass = MESSAGE_CLASS_TEXT;

                // Escape any hyperlinks
                content = replaceHyperlinks(content);
            }
        } catch (CollabException e) {
            // TODO: Proper error handling here
            content = e.getMessage();
            Debug.debugNotify(e);
        }

        // Determine the style for the sender, based on whether we've seen
        // this sender before or not
        String sender = message.getOriginator().getDisplayName();
        String senderClass = DEFAULT_SENDER_CLASS;

        if (getSenderStyles().containsKey(sender)) {
            senderClass = (String) getSenderStyles().get(sender);
        } else {
            // Allocate a new sender style.  Styles for senders are embedded
            // in the document and referenced numerically from here.
            if ((++lastSenderStyleIndex) <= MAX_SENDER_CLASSES) {
                senderClass = allocateNewSenderStyleClass(sender);
            } else {
                // Do nothing, leave as default
            }
        }

        // Fill in the message template.  Use the chat template if the content
        // type is simple/unknown text.
        String result = (contentType == ContentTypes.UNKNOWN_TEXT) ? getChatMessageTemplate() : getMessageTemplate();

        //		String result=getMessageTemplate();
        result = StringTokenizer2.replace(result, TOKEN_SENDER_CLASS, senderClass);
        result = StringTokenizer2.replace(result, TOKEN_MESSAGE_CLASS, messageClass);
        result = StringTokenizer2.replace(result, TOKEN_SENDER, sender);
        result = StringTokenizer2.replace(result, TOKEN_MESSAGE, content);
        result = StringTokenizer2.replace(result, TOKEN_MESSAGE_TEXT, plainContent);
        result = StringTokenizer2.replace(
                result, TOKEN_TIMESTAMP, SimpleDateFormat.getTimeInstance().format(new Date())
            );
        result = StringTokenizer2.replace(result, TOKEN_CONTENT_TYPE, contentType);

        return result;
    }

    /**
     *
     *
     */
    private String replaceHyperlinks(String content) {
        String originalContent = content;

        try {
            int index = content.indexOf("http://"); // NOI18N

            while (index != -1) {
                int startIndex = index;
                int endIndex = startIndex;


// Find the end of the URL
LOOP: 
                for (; endIndex < content.length(); endIndex++) {
                    switch (content.charAt(endIndex)) {
                    case ',':
                    case '\'':
                    case '!':
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\t':
                        break LOOP;

                    case '&': // Check for entities

                        if (
                            content.startsWith("&quot;", endIndex) || // NOI18N
                                content.startsWith("&lt;", endIndex) || // NOI18N
                                content.startsWith("&gt;", endIndex) || // NOI18N
                                content.startsWith("&nbsp;", endIndex)
                        ) // NOI18N
                         {
                            break LOOP;
                        }

                    default:

                        continue;
                    }
                }

                String link = content.substring(startIndex, endIndex);

                // Strip any trailing periods
                if (link.lastIndexOf(".") == (link.length() - 1)) // NOI18N
                 {
                    link = link.substring(0, link.length() - 1);
                    endIndex--;
                }

                try {
                    java.net.URL url = new java.net.URL(link);

                    // If we got here, the link is valid
                    link = "<a href='" + link + "'>" + link + "</a>"; // NOI18N
                    index += link.length();

                    // Replace the link with the HTML markup
                    StringBuffer buffer = new StringBuffer(content);
                    buffer.replace(startIndex, endIndex, link);
                    content = buffer.toString();
                } catch (Exception e) {
                    // Ignore this link
                }

                index = content.indexOf("http://", index); // NOI18N
            }

            return content;
        } catch (Exception e) {
            Debug.debugNotify(e);

            return originalContent;
        }
    }

    /**
     *
     *
     */
    private String allocateNewSenderStyleClass(String sender) {
        String senderClass = "sender" + lastSenderStyleIndex; // NOI18N
        getSenderStyles().put(sender, senderClass);

        return senderClass;
    }

    /**
     * send user typing status to participants
     *
     */
    private void sendTypingStatus(boolean typing) {
        try {
            CollabMessage message = getChannel().getConversation().createMessage();

            int status = typing ? CollabMessage.TYPING_ON : CollabMessage.TYPING_OFF;
            message.sendStatus(status);
        } catch (CollabException e) {
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void updateStatusMessage(CollabPrincipal principal, boolean typing) {
        // Effectively, we manage a reference count for the set of principals
        // currently typing
        if (typing) {
            if (!typingParticipants.contains(principal)) {
                typingParticipants.add(principal);
            }
        } else {
            typingParticipants.remove(principal);
        }

        // Determine what text to show
        String text = "";

        if (typingParticipants.size() == 1) {
            // Single user, show their name
            CollabPrincipal participant = (CollabPrincipal) typingParticipants.iterator().next();
            text = " " +
                NbBundle.getMessage(
                    ChatComponent.class, "LBL_ChatComponent_USER_TYPING", // NOI18N
                    participant.getDisplayName()
                );
        } else if (typingParticipants.size() > 1) {
            // Multiple users, show general message
            text = " " + NbBundle.getMessage(ChatComponent.class, "LBL_ChatComponent_SERVERAL_USERS_TYPING"); // NOI18N
        }

        typingMessageLabel.setText(text);
    }

    /**
     *
     *
     */
    private javax.swing.Timer getTypingTimer() {
        return typingTimer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                java.net.URL url = event.getURL();
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } catch (Exception e) {
                // Ignore
                Debug.debugNotify(e);
            }
        }
    }

    /*Creates the popup menu
    *author Smitha Krishna Nagesh
    */
    private void createPopupMenu() {
        JMenuItem menuItem;
        popupMenu = new JPopupMenu();

        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, false));
        menuItem.setText("Copy");
        popupMenu.add(menuItem);
        popupMenu.addSeparator();

        menuItem = new JMenuItem("Save");
        popupMenu.add(menuItem);
        menuItem.addActionListener(new FileChooserActionListener());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false));

        MouseListener popupListener = new PopupListener();
        transcriptPane.addMouseListener(popupListener);
    }

    /*author Smitha Krishna Nagesh*/
    protected class SmileListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            newMenu.show(smileyButton, 10, 10);
            inputPane.grabFocus();
        }
    }

    /*author Smitha Krishna Nagesh*/
    protected class ButtonClickedListener implements ActionListener {
        public void actionPerformed(ActionEvent menuEvent) {
            JButton button = (JButton) menuEvent.getSource();
            String str = button.getActionCommand();

            try {
                int caretPos = inputPane.getCaretPosition();
                inputPane.getDocument().insertString(caretPos, str, null);
                inputPane.setCaretPosition(caretPos + str.length());
                newMenu.setVisible(false);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class ChatHTMLFactory extends HTMLEditorKit.HTMLFactory {
        /**
         *
         *
         */
        public View create(Element element) {
            // Unfortunately, the Swing HTML parser doesn't respect the
            // display: none style, so we no-op the rendering of the message
            // template element here.
            if (
                element.getAttributes().isDefined(HTML.Attribute.ID) &&
                    element.getAttributes().getAttribute(HTML.Attribute.ID).equals(MESSAGE_TEMPLATE_ELEMENT_ID)
            ) {
                return new BlockView(element, View.Y_AXIS) {
                        public void paint(Graphics g, Shape allocation) {
                            // Do nothing
                        }
                    };
            } else {
                return super.create(element);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class ConversationChangeListener extends Object implements PropertyChangeListener {
        /**
         *
         *
         */
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(ChatCollablet.PROP_TRANSCRIPT)) {
                try {
                    CollabMessage message = (CollabMessage) event.getNewValue();
                    appendMessage(message);
                    CollabManager.getDefault().getUserInterface().notifyConversationEvent(
                        ChatComponent.this.getChannel().getConversation(), UserInterface.NOTIFY_CHAT_MESSAGE_RECEIVED
                    );
                } catch (Exception e) {
                    // TODO: Do something appropriate here
                    Debug.errorManager.notify(e);
                }
            } else if (event.getPropertyName().equals(Conversation.PROP_PARTICIPANTS)) {
                boolean joined = event.getNewValue() != null;
                CollabPrincipal principal = joined ? (CollabPrincipal) event.getNewValue()
                                                   : (CollabPrincipal) event.getOldValue();

                try {
                    String key = joined ? "MSG_ChatComponent_UserJoined" // NOI18N
                                        : "MSG_ChatComponent_UserLeft"; // NOI18N

                    String message = NbBundle.getMessage(
                            ChatComponent.class, key, principal.getDisplayName(),
                            SimpleDateFormat.getTimeInstance().format(new Date())
                        );
                    appendSystemMessage(message);
                } catch (Exception e) {
                    // TODO: Do something appropriate here
                    Debug.errorManager.notify(e);
                }
            } else if (event.getPropertyName().equals(Conversation.USER_TYPING_ON)) {
                CollabPrincipal principal = (CollabPrincipal) event.getNewValue();
                updateStatusMessage(principal, true);
            } else if (event.getPropertyName().equals(Conversation.USER_TYPING_OFF)) {
                CollabPrincipal principal = (CollabPrincipal) event.getNewValue();
                updateStatusMessage(principal, false);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    private class ContentTypeActionListener extends Object implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // This will be invoked only when the user selects the button.
            // If the user explicitly selects a button, cancel the previous
            // selection
            JToggleButton source = (JToggleButton) event.getSource();
            String type = (String)source.getClientProperty("contentType"); //NOI18N 
            smileyButton.setEnabled(type.equals(ContentTypes.UNKNOWN_TEXT));

            previouslySelectedContentType = null;

            setInputContentType(type, false, false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This part of the code has been changed inorder to suit the needs of the dynamic addition of the button
     *
     */
    protected class ContentTypeSynchronizer extends Object implements Runnable {
        /**
         *
         *
         */
        public void run() {
            JToggleButton button = (JToggleButton) contentTypeToButton.get(inputContentType);
            button.setSelected(true);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class TypingTimerActionListener extends AbstractAction implements ActionListener {
        /**
         *
         *
         */
        public void actionPerformed(ActionEvent event) {
            getTypingTimer().stop();
            sendTypingStatus(false);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class InputPaneDocumentListener implements DocumentListener, PropertyChangeListener {
        private Document oldDoc;
        
        public InputPaneDocumentListener() {
            getInputPane().addPropertyChangeListener(this);
            updateDocument();
        }
        
        public void updateTimer() {
            if (getTypingTimer() != null) {
                if (getTypingTimer().isRunning()) {
                    getTypingTimer().restart();
                } else {
                    if (getInputPane().getDocument().getLength() > 0) {
                        getTypingTimer().start();
                        sendTypingStatus(true);
                    }
                }
            }
        }

        public void changedUpdate(DocumentEvent e) {
            updateTimer();
        }

        public void insertUpdate(DocumentEvent e) {
            updateTimer();
        }

        public void removeUpdate(DocumentEvent e) {
            updateTimer();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("document")) {
                updateDocument();
            }
        }
        private void updateDocument() {
            if (oldDoc != null) oldDoc.removeDocumentListener(this);
            oldDoc = getInputPane().getDocument();
            oldDoc.addDocumentListener(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class SendButtonActionListener extends AbstractAction implements ActionListener {
        /**
         *
         *
         */
        public void actionPerformed(ActionEvent event) {
            getTypingTimer().stop();
            sendTypingStatus(false);

            sendInput();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * HACK: This class implements a proxy pattern in order to allow us to
     * "sniff" pasted content.  Unfortunately, because TransferHandler has
     * protected methods, it requires us to use AccessibleObject to invoke
     * certain methods.
     *
     */
    public class InputPaneTransferHandler extends TransferHandler {
        private TransferHandler delegate;

        /**
         *
         *
         */
        public InputPaneTransferHandler(TransferHandler delegate) {
            super();
            this.delegate = delegate;
        }

        /**
         *
         *
         */
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return delegate.canImport(comp, transferFlavors);
        }

        /**
         *
         *
         */
        protected Transferable createTransferable(JComponent c) {
            Method method = null;

            try {
                method = getClass().getMethod("createTransferable", // NOI18N
                        new Class[] { JComponent.class }
                    );
                method.setAccessible(true);
            } catch (Exception e) {
                Debug.debugNotify(e);

                // Cannot happen
                assert false : e.getMessage();
            }

            try {
                return (Transferable) method.invoke(delegate, new Object[] { c });
            } catch (Exception e) {
                // Shouldn't happen
                Debug.debugNotify(e);
            }

            // Fallback, just in case
            return super.createTransferable(c);
        }

        /**
         *
         *
         */
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            delegate.exportAsDrag(comp, e, action);
        }

        /**
         *
         *
         */
        protected void exportDone(JComponent source, Transferable data, int action) {
            Method method = null;

            try {
                method = getClass().getMethod(
                        "exportDone", // NOI18N
                        new Class[] { JComponent.class, Transferable.class, Integer.TYPE }
                    );
                method.setAccessible(true);
            } catch (Exception e) {
                Debug.debugNotify(e);

                // Cannot happen
                assert false : e.getMessage();
            }

            try {
                method.invoke(delegate, new Object[] { source, data, new Integer(action) });
            } catch (Exception e) {
                // Shouldn't happen
                Debug.debugNotify(e);

                // Fallback, just in case
                super.exportDone(source, data, action);
            }
        }

        /**
         *
         *
         */
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            delegate.exportToClipboard(comp, clip, action);
        }

        /**
         *
         *
         */
        public int getSourceActions(JComponent c) {
            return delegate.getSourceActions(c);
        }

        /**
         *
         *
         */
        public Icon getVisualRepresentation(Transferable t) {
            return delegate.getVisualRepresentation(t);
        }

        /**
         *
         *
         */
        public boolean importData(JComponent component, Transferable transferable) {
            String content = getInputPane().getText();

            // Go ahead and import the data
            boolean result = delegate.importData(component, transferable);

            if (result && ((content == null) || (content.length() == 0))) {
                // Figure out what the best representation of the just-imported
                // data is, and set the current input content type accordingly
                DataFlavor[] flavors = transferable.getTransferDataFlavors();
                DataFlavor flavor = null;

                if ((flavor = containsContentType(flavors, ContentTypes.JAVA)) != null) {
                    autoselectContentType(ContentTypes.JAVA);
                } else if ((flavor = containsContentType(flavors, ContentTypes.XML)) != null) {
                    autoselectContentType(ContentTypes.XML);
                } else if ((flavor = containsContentType(flavors, ContentTypes.HTML)) != null) {
                    // See if this is actually HTML or just plain text encoded
                    // as HTML
                    String htmlText = getContent(transferable, flavor);
                    String plainText = getContent(transferable, containsContentType(flavors, ContentTypes.TEXT));

                    if ((htmlText != null) && (plainText != null)) {
                        if (htmlText.equals(plainText)) {
                            // This is actually HTML
                            autoselectContentType(ContentTypes.HTML);
                        } else {
                            // This is actually just plain text encoded as HTML
                            setTextContentTypeConditionally();
                        }
                    } else {
                        // If the HTML string was here, it's definitely HTML.
                        // I don't know that this condition is actually ever
                        // possible in any case; I think in all cases where
                        // text/html is available, text/plain will also be
                        // available.
                        if (htmlText != null) {
                            autoselectContentType(ContentTypes.HTML);
                        } else {
                            setTextContentTypeConditionally();
                        }
                    }
                } else if ((flavor = containsContentType(flavors, ContentTypes.TEXT)) != null) {
                    setTextContentTypeConditionally();
                } else {
                    // Do nothing
                }
            }

            return result;
        }

        /**
         *
         *
         */
        private String getContent(Transferable transferable, DataFlavor flavor) {
            if (flavor == null) {
                return null;
            }

            // Get the data so we can look at it
            Object data = null;
            String dataString = null;

            try {
                data = transferable.getTransferData(flavor);
            } catch (Exception e) {
                Debug.debugNotify(e);
                autoselectContentType(ContentTypes.HTML);
            }

            if (data != null) {
                // Convert to a String
                if (data instanceof String) {
                    dataString = (String) data;
                } else if (data instanceof Reader) {
                    try {
                        BufferedReader reader = new BufferedReader((Reader) data);
                        StringBuffer buffer = new StringBuffer();
                        String line = null;

                        while ((line = reader.readLine()) != null)
                            buffer.append(line).append("\n"); // NOI18N

                        dataString = buffer.toString();
                    } catch (Exception e) {
                        Debug.debugNotify(e);
                    }
                } else if (data instanceof InputStream) {
                    try {
                        String charset = flavor.getParameter("charset");
                        BufferedReader reader = null;

                        if (charset != null) {
                            reader = new BufferedReader(new InputStreamReader((InputStream) data, charset));
                        } else {
                            reader = new BufferedReader(new InputStreamReader((InputStream) data));
                        }

                        StringBuffer buffer = new StringBuffer();
                        String line = null;

                        while ((line = reader.readLine()) != null)
                            buffer.append(line).append("\n"); // NOI18N

                        dataString = buffer.toString();
                    } catch (Exception e) {
                        Debug.debugNotify(e);
                    }
                }
            }

            // We have to normalize line endings in order to compare strings
            // of different content types
            dataString = StringTokenizer2.replace(dataString, "\r\n", "\n");

            return dataString;
        }

        /**
         *
         *
         */
        private void setTextContentTypeConditionally() {
            // If it's not already text, set it to text. Don't
            // distinguish between formatted and plain text
            if (
                !getInputContentType().equals(ContentTypes.UNKNOWN_TEXT) &&
                    !getInputContentType().equals(ContentTypes.TEXT)
            ) {
                autoselectContentType(ContentTypes.TEXT);
            }
        }

        /**
         *
         *
         */
        private void autoselectContentType(String contentType) {
            setInputContentType(contentType, true, true);
        }

        /**
         *
         *
         */
        public DataFlavor containsContentType(DataFlavor[] flavors, String contentType) {
            //			Debug.out.println("----------------------------");
            //			for (int i=0; i<flavors.length; i++)
            //				Debug.out.println(flavors[i].getMimeType());
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].isMimeTypeEqual(contentType)) {
                    return flavors[i];
                }
            }

            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /* author Smitha Krishna Nagesh*/
    protected class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent mouseEvt) {
            showPopup(mouseEvt);
        }

        public void mouseReleased(MouseEvent mouseEvt) {
            showPopup(mouseEvt);
        }

        private void showPopup(MouseEvent mouseEvt) {
            if (mouseEvt.isPopupTrigger()) {
                popupMenu.show(mouseEvt.getComponent(), mouseEvt.getX(), mouseEvt.getY());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /* author Smitha Krishna Nagesh
    Pops up a FileChooser to choose a directory to save the file.*/
    protected class FileChooserActionListener implements ActionListener {
        JFileChooser chooseFile = new JFileChooser();

        public void actionPerformed(ActionEvent saveEvt) {
            chooseFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int returnVal = chooseFile.showSaveDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooseFile.getSelectedFile();

                if (file.exists()) {
                    int option = JOptionPane.showOptionDialog(
                            null, NbBundle.getMessage(ChatComponent.class, "MSG_ChatComponent_FileChoosing"), //NOI18N
                            NbBundle.getMessage(ChatComponent.class, "TITLE_Confirmation"), //NOI18N
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null
                        );

                    if ((option == JOptionPane.CANCEL_OPTION) || (option == JOptionPane.CLOSED_OPTION)) {
                        return;
                    }
                }

                String fileStr = file.toString();
                file = new File(fileStr);

                HTMLDocument document = (HTMLDocument) transcriptPane.getDocument();

                try {
                    String string = document.getText(0, document.getLength());
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(string, 0, string.length());
                    fileWriter.flush();
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
