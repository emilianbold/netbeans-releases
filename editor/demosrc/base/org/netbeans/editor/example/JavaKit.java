/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.example;

import java.io.File;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;

import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import javax.swing.KeyStroke;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.java.*;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaKit extends ExtKit {

    public static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N

    private static final String[] getSetIsPrefixes = new String[] {
                "get", "set", "is"
            };

    /** Switch first letter of word to capital and insert 'get'
    * at word begining.
    */
    public static final String makeGetterAction = "make-getter"; // NOI18N

    /** Switch first letter of word to capital and insert 'set'
    * at word begining.
    */
    public static final String makeSetterAction = "make-setter"; // NOI18N

    /** Switch first letter of word to capital and insert 'is'
    * at word begining.
    */
    public static final String makeIsAction = "make-is"; // NOI18N

    /** Debug source and line number */
    public static final String abbrevDebugLineAction = "abbrev-debug-line"; // NOI18N

    static final long serialVersionUID =-5445829962533684922L;

    static {
        Settings.addInitializer( new JavaSettingsInitializer( JavaKit.class ) );
        Settings.addInitializer( new SaJavaSettingsInitializer() );
        Settings.reset();

        ResourceBundle settings = ResourceBundle.getBundle( "settings" );
        String jcPath = null;
        try {
            jcPath = settings.getString( "Java_Completion" );
        } catch( MissingResourceException exc ) {}

        if( jcPath != null ) {
            JCBaseFinder finder = new JCBaseFinder();
            JCFileProvider provider = new JCFileProvider( jcPath );
            finder.append( provider );
            JavaCompletion.setFinder( finder );
        }
    }

    public String getContentType() {
        return JAVA_MIME_TYPE;
    }

    /** Create new instance of syntax coloring scanner
    * @param doc document to operate on. It can be null in the cases the syntax
    *   creation is not related to the particular document
    */
    public Syntax createSyntax(Document doc) {
        return new JavaSyntax();
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new JavaSyntaxSupport(doc);
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return new JavaCompletion(extEditorUI);
    }

    /** Create the formatter appropriate for this kit */
    public Formatter createFormatter() {
        return new JavaFormatter(this.getClass());
    }
    
    protected EditorUI createEditorUI() {
        return new ExtEditorUI();
    }

    protected void initDocument(BaseDocument doc) {
        doc.addLayer(new JavaDrawLayerFactory.JavaLayer(),
                JavaDrawLayerFactory.JAVA_LAYER_VISIBILITY);
        doc.addDocumentListener(new JavaDrawLayerFactory.LParenWatcher());
    }

    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
                                   new JavaDefaultKeyTypedAction(),
                                   new PrefixMakerAction(makeGetterAction, "get", getSetIsPrefixes),
                                   new PrefixMakerAction(makeSetterAction, "set", getSetIsPrefixes),
                                   new PrefixMakerAction(makeIsAction, "is", getSetIsPrefixes),
                                   new AbbrevDebugLineAction(),
                               };
        return TextAction.augmentList(super.createActions(), javaActions);
    }


    public static class JavaDefaultKeyTypedAction extends ExtDefaultKeyTypedAction {

        protected void checkIndentHotChars(JTextComponent target, String typedText) {
            boolean reindent = false;

            BaseDocument doc = Utilities.getDocument(target);
            int dotPos = target.getCaret().getDot();
            if (doc != null) {
                /* Check whether the user has written the ending 'e'
                 * of the first 'else' on the line.
                 */
                if ("e".equals(typedText)) {
                    try {
                        int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                        if (fnw >= 0 && fnw + 4 == dotPos
                            && "else".equals(doc.getText(fnw, 4))
                        ) {
                            reindent = true;
                        }
                    } catch (BadLocationException e) {
                    }

                } else if (":".equals(typedText)) {
                    try {
                        int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                        if (fnw >= 0 && fnw + 4 <= doc.getLength()
                            && "case".equals(doc.getText(fnw, 4))
                        ) {
                            reindent = true;
                        }
                    } catch (BadLocationException e) {
                    }
                }

                // Reindent the line if necessary
                if (reindent) {
                    try {
                        Utilities.reformatLine(doc, dotPos);
                    } catch (BadLocationException e) {
                    }
                }
            }

            super.checkIndentHotChars(target, typedText);
        }

    }



    public static class AbbrevDebugLineAction extends BaseAction {

        public AbbrevDebugLineAction() {
            super(abbrevDebugLineAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                BaseDocument doc = (BaseDocument)target.getDocument();
                StringBuffer sb = new StringBuffer("System.err.println(\""); // NOI18N
                File file = (File)doc.getProperty( "file" );
                if (file != null) {
                    sb.append( file.getAbsolutePath() );
                    sb.append(':');
                }
                try {
                    sb.append(Utilities.getLineOffset(doc, target.getCaret().getDot()) + 1);
                } catch (BadLocationException e) {
                }
                sb.append(' ');

                BaseKit kit = Utilities.getKit(target);
                Action a = kit.getActionByName(BaseKit.insertContentAction);
                if (a != null) {
                    Utilities.performAction(
                        a,
                        new ActionEvent(target, ActionEvent.ACTION_PERFORMED, sb.toString()),
                        target
                    );
                }
            }
        }
    }
    
    
    private static class SaJavaSettingsInitializer extends Settings.AbstractInitializer {
        public SaJavaSettingsInitializer() {
            super( "sa-java-settings-initializer" );
        }
        
        
        
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            if (kitClass == JavaKit.class) {
                SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST, getJavaKeyBindings());
            }

        }

        public MultiKeyBinding[] getJavaKeyBindings() {
            return new MultiKeyBinding[] {
               new MultiKeyBinding(
                   new KeyStroke[] {
                       KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                       KeyStroke.getKeyStroke(KeyEvent.VK_G, 0)
                   },
                   JavaKit.makeGetterAction
               ),
               new MultiKeyBinding(
                   new KeyStroke[] {
                       KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                       KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)
                   },
                   JavaKit.makeSetterAction
               ),
               new MultiKeyBinding(
                   new KeyStroke[] {
                       KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK),
                       KeyStroke.getKeyStroke(KeyEvent.VK_I, 0)
                   },
                   JavaKit.makeIsAction
               ),
               new MultiKeyBinding(
                   KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK),
                   JavaKit.abbrevDebugLineAction
               )
            };
        }
    }

}
