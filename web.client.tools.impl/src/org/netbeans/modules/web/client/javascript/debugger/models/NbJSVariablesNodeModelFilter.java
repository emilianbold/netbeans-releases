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

package org.netbeans.modules.web.client.javascript.debugger.models;

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

public class NbJSVariablesNodeModelFilter implements ExtendedNodeModelFilter  {
	
    public static String toHTML(String text, boolean bold,  boolean italics, Color color) {
        if (text == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<html>"); // NOI18N
        if (bold) {
            sb.append("<b>"); // NOI18N
        }
        if (italics) {
            sb.append("<i>"); // NOI18N
        }
        if (color != null) {
            sb.append("<font color="); // NOI18N
            sb.append(Integer.toHexString ((color.getRGB () & 0xffffff)));
            sb.append(">"); // NOI18N
        }
        text = text.replaceAll("&", "&amp;"); // NOI18N
        text = text.replaceAll("<", "&lt;"); // NOI18N
        text = text.replaceAll(">", "&gt;"); // NOI18N
        sb.append(text);
        if (color != null) {
            sb.append("</font>"); // NOI18N
        }
        if (italics) {
            sb.append("</i>"); // NOI18N
        }
        if (bold) {
            sb.append("</b>"); // NOI18N
        }
        sb.append("</html>"); // NOI18N
        return sb.toString();
    }


    private String toStaleText(String text ){
    	return toHTML(text, false, false, Color.LIGHT_GRAY);
    }


	public String getDisplayName(NodeModel original, Object node)
			throws UnknownTypeException {
		
		String strOriginal= original.getDisplayName(node);
		if( original instanceof NbJSVariablesModel ){
			if ( ((NbJSVariablesModel)original).isStaleState() ) {
				return toStaleText(strOriginal);
			}
		}
		
		return strOriginal;
		
	}

	public String getIconBase(NodeModel original, Object node)
			throws UnknownTypeException { 
		return original.getIconBase(node);
	}

	public String getShortDescription(NodeModel original, Object node)
			throws UnknownTypeException {

		return original.getShortDescription(node);
	}


    public boolean canCopy(ExtendedNodeModel original, Object node)
            throws UnknownTypeException {
        return original.canCopy(node);
    }


    public boolean canCut(ExtendedNodeModel original, Object node)
            throws UnknownTypeException {
        return original.canCut(node);
    }


    public boolean canRename(ExtendedNodeModel original, Object node)
            throws UnknownTypeException {
        return original.canRename(node);
    }


    public Transferable clipboardCopy(ExtendedNodeModel original, Object node)
            throws IOException, UnknownTypeException {
        // TODO Auto-generated method stub
        return original.clipboardCopy(node);
    }


    public Transferable clipboardCut(ExtendedNodeModel original, Object node)
            throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }


    public String getIconBaseWithExtension(ExtendedNodeModel original,
            Object node) throws UnknownTypeException {
        return original.getIconBaseWithExtension(node);
    }


    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node,
            Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }


    public void setName(ExtendedNodeModel original, Object node, String name)
            throws UnknownTypeException {
       original.setName(node, name);
        
    }


    public void addModelListener(ModelListener l) {
    }


    public void removeModelListener(ModelListener l) {
    }


}
