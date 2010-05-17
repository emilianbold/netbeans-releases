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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
