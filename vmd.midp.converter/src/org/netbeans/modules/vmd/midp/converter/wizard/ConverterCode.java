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

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.modules.vmd.api.codegen.JavaCodeGenerator;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.listeners.CommandListenerCD;
import org.netbeans.modules.vmd.midp.components.listeners.ItemCommandListenerCD;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
public class ConverterCode {

    public static void convertCode (List<ConverterItem> items, StyledDocument styledDocument, StyledDocument outputStyledDocument, DesignDocument document) throws BadLocationException {
        HashMap<String, String> replaceTable = createUserCodeConvertTable (items);
        GuardedSectionManager manager = GuardedSectionManager.getInstance (styledDocument);

        for (GuardedSection section : manager.getGuardedSections ()) {
            if ("MVDInitBegin".equals (section.getName ())) { // NOI18N
                long rootID = document.getRootComponent ().getComponentID ();
                putUserCode (outputStyledDocument, rootID + "-initialize", rootID + "-preInitialize", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N
            } else if ("MVDInitEnd".equals (section.getName ())) { // NOI18N
                long rootID = document.getRootComponent ().getComponentID ();
                putUserCode (outputStyledDocument, rootID + "-initialize", rootID + "-postInitialize", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if ("MVDGetDisplay".equals (section.getName ())) { // NOI18N
                // TODO - set to method body
            } else if ("MVDExitMidlet".equals (section.getName ())) { // NOI18N
                // TODO - set to method body

            } else if (section.getName ().startsWith ("MVDGetBegin")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDGetBegin".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                putUserCode (outputStyledDocument, id + "-getter", id + "-preInit", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDGetEnd")) { // NOI18N
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDGetEnd".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                putUserCode (outputStyledDocument, id + "-getter", id + "-postInit", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDCABegin")) {
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", listener.getComponentID () + "-preCommandAction", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDCAEnd")) {
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", listener.getComponentID () + "-postCommandAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDCAAction")) {
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDCAAction".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", id + "-preAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDCACase")) {
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDCACase".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, CommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-commandAction", id + "-postAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N

            } else if (section.getName ().startsWith ("MVDICABegin")) {
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", listener.getComponentID () + "-preItemCommandAction", replaceTable, getUserCodeAfterSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDICAEnd")) {
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", listener.getComponentID () + "-postItemCommandAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
                
            } else if (section.getName ().startsWith ("MVDICAAction")) {
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDICAAction".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", id + "-preAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
            } else if (section.getName ().startsWith ("MVDICACase")) {
                ConverterItem item = findItemWithUID (items, section.getName ().substring ("MVDICACase".length ())); // NOI18N
                long id = item.getRelatedComponent ().getComponentID ();
                DesignComponent listener = MidpDocumentSupport.getCommandListener (document, ItemCommandListenerCD.TYPEID);
                putUserCode (outputStyledDocument, listener.getComponentID () + "-itemCommandAction", id + "-postAction", replaceTable, getUserCodeBeforeSection (styledDocument, section)); // NOI18N
            }
        }

        // TODO - convert class begining (before guarded-blocks), class end (after guarded-blocks), between guarded blocks
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
        for (Map.Entry<String, String> entry : replaceTable.entrySet ())
            userCode = userCode.replaceAll (entry.getKey (), entry.getValue ());
        JavaCodeGenerator.getDefault ().putUserCode (styledDocument, guardedID, editableID, userCode);
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
