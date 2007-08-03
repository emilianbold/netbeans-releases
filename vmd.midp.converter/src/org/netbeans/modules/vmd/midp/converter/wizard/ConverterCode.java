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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.vmd.api.codegen.JavaCodeGenerator;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.listeners.CommandListenerCD;
import org.netbeans.modules.vmd.midp.components.listeners.ItemCommandListenerCD;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
public class ConverterCode {

    public static void convertCode (List<ConverterItem> items, StyledDocument styledDocument, final StyledDocument outputStyledDocument, DesignDocument document, String oldDesignName, String newDesignName) throws BadLocationException {
        HashMap<String, String> replaceTable = createUserCodeConvertTable (items);
        replaceTable.put (oldDesignName, newDesignName);
        GuardedSectionManager manager = GuardedSectionManager.getInstance (styledDocument);

        final StringBuffer globalCode = new StringBuffer ();
        String getDisplayMethodBody = ""; // NOI18N
        String exitMIDletMethodBody = ""; // NOI18N
        String getDisplayBeforeCode = null;
        String exitMIDletBeforeCode = null;

        for (GuardedSection section : manager.getGuardedSections ()) {
            if ("MVDFields".equals (section.getName ())) { // NOI18N
                String before = getUserCodeBeforeSection (styledDocument, section);
                if (before != null)
                    globalCode.append (processUserCodeByTable (replaceTable, before)).append ('\n'); // NOI18N
            } else if ("MVDMethods".equals (section.getName ())) { // NOI18N
                String before = getUserCodeBeforeSection (styledDocument, section);
                if (before != null)
                    globalCode.append (processUserCodeByTable (replaceTable, before)).append ('\n'); // NOI18N

            } else if ("MVDInitBegin".equals (section.getName ())) { // NOI18N
                long rootID = document.getRootComponent ().getComponentID ();
                putUserCode (outputStyledDocument, rootID + "-initialize", rootID + "-preInitialize", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N

                String before = getUserCodeBeforeSection (styledDocument, section);
                if (before != null)
                    globalCode.append (processUserCodeByTable (replaceTable, before)).append ('\n'); // NOI18N
            } else if ("MVDInitEnd".equals (section.getName ())) { // NOI18N
                long rootID = document.getRootComponent ().getComponentID ();
                putUserCode (outputStyledDocument, rootID + "-initialize", rootID + "-postInitialize", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if ("MVDGetDisplay".equals (section.getName ())) { // NOI18N
                getDisplayBeforeCode = getUserCodeBeforeSection (styledDocument, section);

                InteriorSection is = (InteriorSection) section;
                getDisplayMethodBody = is.getBody ();
            } else if ("MVDExitMidlet".equals (section.getName ())) { // NOI18N
                exitMIDletBeforeCode = getUserCodeBeforeSection (styledDocument, section);

                InteriorSection is = (InteriorSection) section;
                exitMIDletMethodBody = is.getBody ();

            } else if (section.getName ().startsWith ("MVDGetBegin")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDGetBegin".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                putUserCode (outputStyledDocument, id + "-getter", id + "-preInit", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N

                String before = getUserCodeBeforeSection (styledDocument, section);
                if (before != null)
                    globalCode.append (processUserCodeByTable (replaceTable, before)).append ('\n'); // NOI18N
            } else if (section.getName ().startsWith ("MVDGetEnd")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDGetEnd".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                putUserCode (outputStyledDocument, id + "-getter", id + "-postInit", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDCABegin")) { // NOI18N
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", listener.getComponentID () + "-preCommandAction", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N

                String before = getUserCodeBeforeSection (styledDocument, section);
                if (before != null)
                    globalCode.append (processUserCodeByTable (replaceTable, before)).append ('\n'); // NOI18N
            } else if (section.getName ().startsWith ("MVDCAEnd")) { // NOI18N
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", listener.getComponentID () + "-postCommandAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDCAAction")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDCAAction".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", id + "-preAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDCACase")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDCACase".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", id + "-postAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDICABegin")) { // NOI18N
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", listener.getComponentID () + "-preItemCommandAction", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N

                String before = getUserCodeBeforeSection (styledDocument, section);
                if (before != null)
                    globalCode.append (processUserCodeByTable (replaceTable, before)).append ('\n'); // NOI18N
            } else if (section.getName ().startsWith ("MVDICAEnd")) { // NOI18N
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", listener.getComponentID () + "-postItemCommandAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDICAAction")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDICAAction".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", id + "-preAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDICACase")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDICACase".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", id + "-postAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
            }
        }

        if (getDisplayBeforeCode != null)
            globalCode.append (processUserCodeByTable (replaceTable, getDisplayBeforeCode)).append ('\n'); // NOI18N
        getDisplayMethodBody += "\n        // return Display.getDisplay (this);\n"; // NOI18N
        globalCode.append ("    /**\n     * Returns a display instance.\n     * @return the display instance.\n     */\n"); // NOI18N
        globalCode.append ("public Display getDisplay () {\n").append (processUserCodeByTable (replaceTable, getDisplayMethodBody)).append ("}\n"); // NOI18N
        globalCode.append ('\n'); // NOI18N

        if (exitMIDletBeforeCode != null)
            globalCode.append (processUserCodeByTable (replaceTable, exitMIDletBeforeCode)).append ('\n'); // NOI18N
        exitMIDletMethodBody += "\n        // switchDisplayable (null, null);\n        // destroyApp(true);\n        // notifyDestroyed();\n"; // NOI18N
        globalCode.append ("    /**\n     * Exits MIDlet.\n     */\n"); // NOI18N
        globalCode.append ("public void exitMIDlet() {\n").append (processUserCodeByTable (replaceTable, exitMIDletMethodBody)).append ("}\n"); // NOI18N
        globalCode.append ('\n'); // NOI18N

        final int[] indices = new int[] { -1, -1 };
        findIndices (styledDocument, indices);

        if (indices[0] >= 0) {
            int i = getFirstGuardedSectionOffset (styledDocument);
            if (i != Integer.MAX_VALUE  &&  indices[0] < i) {
                globalCode.append (processUserCodeByTable (replaceTable, styledDocument.getText (indices[0], i - indices[0])));
                globalCode.append ('\n'); // NOI18N
            }
        }

        if (indices[1] >= 0) {
            int i = getLastGuardedSectionOffset (styledDocument);
            if (i != Integer.MIN_VALUE  &&  i < indices[1]) {
                globalCode.append (processUserCodeByTable (replaceTable, styledDocument.getText (i, indices[1] - i)));
                globalCode.append ('\n'); // NOI18N
            }
        }

        NbDocument.runAtomic (outputStyledDocument, new Runnable() {
            public void run () {
                int outputOffset = getLastGuardedSectionOffset (outputStyledDocument);
                assert outputOffset != Integer.MIN_VALUE;
                try {
                    outputStyledDocument.insertString (outputOffset, globalCode.toString (), null);
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace (e);
                }
            }
        });

        // TODO - imports, extends, implements sections
        // TODO - other classes in the file
    }

    private static void findIndices (StyledDocument styledDocument, final int[] indices) {
        JavaSource source = JavaSource.forDocument (styledDocument);
        try {
            source.runUserActionTask (new CancellableTask<CompilationController>() {
                public void cancel () {
                }

                public void run (CompilationController controller) throws Exception {
                    controller.toPhase (JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassTree mainClass = findMainClass (controller);
                    if (mainClass == null)
                        return;

                    int start = (int) controller.getTrees ().getSourcePositions ().getStartPosition (controller.getCompilationUnit (), mainClass);
                    int end = (int) controller.getTrees ().getSourcePositions ().getEndPosition (controller.getCompilationUnit (), mainClass);

                    if (start < 0  ||  end < 0  ||  start >= end)
                        return;

                    int bracket = controller.getText ().indexOf ('{', start);

                    if (bracket < 0 || bracket > end)
                        return;
                    if (controller.getText ().charAt (end - 1) == '}')
                        end --;
                    indices[0] = bracket + 1;
                    indices[1] = end;
                }
            }, true);
        } catch (IOException e) {
            throw Debug.error (e);
        }
    }

    static ClassTree findMainClass (CompilationController controller) {
        for (Tree decl : controller.getCompilationUnit ().getTypeDecls ()) {
            if (decl.getKind () == Tree.Kind.CLASS) {
                ClassTree ct = (ClassTree) decl;
                if (ct.getModifiers ().getFlags ().contains (Modifier.PUBLIC))
                    return ct;
            }
        }
        return null;
    }

    private static int getFirstGuardedSectionOffset (StyledDocument styledDocument) {
        GuardedSectionManager manager = GuardedSectionManager.getInstance (styledDocument);
        int offset = Integer.MAX_VALUE;
        for (GuardedSection section : manager.getGuardedSections ())
            offset = Math.min (offset, section.getStartPosition ().getOffset ());
        return offset;
    }

    private static int getLastGuardedSectionOffset (StyledDocument styledDocument) {
        GuardedSectionManager manager = GuardedSectionManager.getInstance (styledDocument);
        int offset = Integer.MIN_VALUE;
        for (GuardedSection section : manager.getGuardedSections ())
            offset = Math.max (offset, section.getEndPosition ().getOffset () + 1);
        return offset;
    }

    private static String getUserCodeBeforeSection (StyledDocument styledDocument, GuardedSection section) throws BadLocationException {
        GuardedSectionManager manager = GuardedSectionManager.getInstance (styledDocument);
        int offset = section.getStartPosition ().getOffset ();
        GuardedSection best = null;
        int bestOffset = 0;

        for (GuardedSection s : manager.getGuardedSections ()) {
            int o = s.getStartPosition ().getOffset ();
            if (offset <= o)
                continue;
            if (best == null  ||  bestOffset < o) {
                best = s;
                bestOffset = o;
            }
        }
        if (best == null)
            return null;
        int start = best.getEndPosition ().getOffset () + 1;
        int end = section.getStartPosition ().getOffset ();
        return styledDocument.getText (start, end - start);
    }

    private static String getUserCodeAfterSection (StyledDocument styledDocument, GuardedSection section) throws BadLocationException {
        GuardedSectionManager manager = GuardedSectionManager.getInstance (styledDocument);
        int offset = section.getStartPosition ().getOffset ();
        GuardedSection best = null;
        int bestOffset = 0;

        for (GuardedSection s : manager.getGuardedSections ()) {
            int o = s.getStartPosition ().getOffset ();
            if (o <= offset)
                continue;
            if (best == null  ||  o < bestOffset) {
                best = s;
                bestOffset = o;
            }
        }
        if (best == null)
            return null;
        int start = section.getEndPosition ().getOffset () + 1;
        int end = best.getStartPosition ().getOffset ();
        return styledDocument.getText (start, end - start);
    }

    private static ConverterItem findItemWithUID (List<ConverterItem> items, String itemUID) {
        for (ConverterItem item : items)
            if (item.getUID ().equals (itemUID))
                return item;
        return null;
    }

    private static void putUserCode (StyledDocument styledDocument, String guardedID, String editableID, HashMap<String, String> replaceTable, String userCode) {
        userCode = processUserCodeByTable (replaceTable, userCode);
        JavaCodeGenerator.getDefault ().putUserCode (styledDocument, guardedID, editableID, userCode);
    }

    private static String processUserCodeByTable (HashMap<String, String> replaceTable, String userCode) {
        for (Map.Entry<String, String> entry : replaceTable.entrySet ()) {
            String key = entry.getKey ();
            int len = key.length ();
            if (len < 0)
                continue;
            int i = 0;
            for (;;) {
                i = userCode.indexOf (key, i);
                if (i < 0)
                    break;
                if (i > 0  &&  Character.isJavaIdentifierPart (userCode.charAt (i - 1))) {
                    i += len;
                    continue;
                }
                if (i + len < userCode.length ()  &&  Character.isJavaIdentifierPart (userCode.charAt (i + len))) {
                    i += len;
                    continue;
                }
                String value = entry.getValue ();
                userCode = userCode.substring (0, i) + value + userCode.substring (i + len);
                i += value.length ();
            }
        }
        return userCode;
    }

    private static HashMap<String,String> createUserCodeConvertTable (List<ConverterItem> items) {
        HashMap<String, String> table = new HashMap<String, String> ();
        for (ConverterItem item : items) {
            if (! item.isClass ())
                continue;
            String name = item.getID ();
            table.put ("get_" + name, "get" + Character.toUpperCase (name.charAt (0)) + name.substring (1)); // NOI18N
        }
        return table;
    }

}
