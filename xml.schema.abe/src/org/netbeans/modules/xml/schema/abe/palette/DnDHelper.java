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



/*
 * DnDHelper.java
 *
 * Created on April 12, 2006, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import org.openide.nodes.Node;

/**
 *
 * @author girix
 */
public class DnDHelper {
    
    
    public static enum PaletteItem{
        ELEMENT,
        ATTRIBUTE,
        SEQUENCE,
        CHOICE,
        ALL,
        COMPLEXTYPE,
        UNKNOWN
    }
    
    /** Creates a new instance of DnDHelper */
    public DnDHelper() {
    }
    
    
    public static PaletteItem getDraggedPaletteItem(Transferable trans){
        for(DataFlavor flav: trans.getTransferDataFlavors()){
            Class repClass = flav.getRepresentationClass();
            Object data = null;
            try {
                data = trans.getTransferData(flav);
            } catch (UnsupportedFlavorException ex) {
                continue;
            } catch (IOException ex) {
                continue;
            }
            if (Node.class.isAssignableFrom(repClass)){
                String name = ((Node)data).getName();
                return getPaletteItem(name);
            }
        }
        return PaletteItem.UNKNOWN;
    }
    
    public static PaletteItem getDraggedPaletteItem(DropTargetDragEvent event) {
        return getDraggedPaletteItem(event.getTransferable());
    }
    
    public static PaletteItem getDraggedPaletteItem(DropTargetDropEvent event){
        return getDraggedPaletteItem(event.getTransferable());
    }
    
    private static DnDHelper.PaletteItem getPaletteItem(String name) {
        if(name.equalsIgnoreCase("attribute"))
            return PaletteItem.ATTRIBUTE;
        if(name.equalsIgnoreCase("element"))
            return PaletteItem.ELEMENT;
        if(name.equalsIgnoreCase("sequence"))
            return PaletteItem.SEQUENCE;
        if(name.equalsIgnoreCase("choice"))
            return PaletteItem.CHOICE;
        if(name.equalsIgnoreCase("all"))
            return PaletteItem.ALL;
        if(name.equalsIgnoreCase("complextype"))
            return PaletteItem.COMPLEXTYPE;
        return PaletteItem.UNKNOWN;
    }
    
    public static boolean isCompositor(DnDHelper.PaletteItem paletteItem){
        switch(paletteItem){
            case SEQUENCE:
            case CHOICE:
            case ALL:
                return true;
            default:
                return false;
        }
    }
    
}
