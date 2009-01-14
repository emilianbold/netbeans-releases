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

package org.netbeans.modules.xslt.mapper.methoid;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicField;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class is intended to load meta-information from the layer.xml
 * The only public method returns the IMethoid object which contains all 
 * meta-information. The object of this type is required by Mapper Core 
 * as a transferable data for DnD operation 
 * (dragging an element from the palette to canvas view).
 *
 * @author nk160297
 */

public class MethoidLoader {
    
    public static IMethoid loadMethoid(FileObject fileObject) {
        IMethoid methoid = generateMethoid(fileObject);
        return methoid;
    }
    
    public static IMethoid loadMethoid(String xpathOperator){
        FileObject metainfoFo = FileUtil.getConfigFile(Constants.XSLT_PALETTE_METAINFO);
        for(FileObject subfolder: metainfoFo.getChildren()){
            for (FileObject methoidfile: subfolder.getChildren()){
                if (xpathOperator.equals(methoidfile.getName())){
                    return MethoidLoader.loadMethoid(methoidfile);
                }
            } 
            
        }
        System.out.println("The methoid with the name \"" + xpathOperator + "\" can't be found"); // NOI18N
        return null;
        
    }
    
    private static IField generateField(FileObject fo,
            String attrName,
            String attrType,
            String attrToolTip,
            boolean isInput,
            boolean isOutput,
            ResourceBundle bundle,
            boolean isLiteral,
            ILiteralUpdaterFactory literalUpdaterFactory) {
        String fieldName = "";
        String fieldTooltip = "";
        String fieldType = "";
        String tooltipKey = "";
        
        if ((fieldName = (String) fo.getAttribute(attrName)) != null) {
            if (
                    (fieldName == null) ||
                    (fieldName.length() == 0) ||
                    // java expression, no "this"
                    (attrName.equals("Class") && fieldName.equals("javaExp"))) {
                return null;
            }
            
            fieldType = (String) fo.getAttribute(attrType);
            tooltipKey = (String) fo.getAttribute(attrToolTip);
            
            if (tooltipKey == null) {
                ErrorManager.getDefault().log(
                        "Unable to find tooltip name:[" + attrToolTip + "]"); // NOi18N
            } else {
                try {
                    fieldTooltip = bundle.getString(tooltipKey);
                } catch (Exception e) {
                }
            }
            
            ILiteralUpdater literalUpdater = null;
            if ((isInput || isLiteral) && literalUpdaterFactory != null) {
                // literal updater will be non-null if field type supports literals
                literalUpdater = literalUpdaterFactory.createLiteralUpdater(fieldType);
            }
            
            IField field = new BasicField(
                    fieldName,
                    fieldType,
                    fieldTooltip,
                    null,
                    isInput,
                    isOutput,
                    literalUpdater);
            
            return field;
        }
        
        return null;
    }
    
    private static IMethoid generateMethoid(FileObject fo) {
        
        boolean isEditableLiteral = false;
        Object literalObj = fo.getAttribute(Constants.LITERAL_FLAG);
        if (literalObj != null && literalObj instanceof Boolean) {
            isEditableLiteral = ((Boolean) literalObj).booleanValue();
        }
        
        String bundleRef = (String)fo.getAttribute(Constants.BUNDLE_CLASS);
        ResourceBundle bundle = ResourceBundle.getBundle(bundleRef);
        
        // creating funtoid namespace (this) field
        String fieldName = null;
        String fieldTooltip = null;
        String fieldType = null;
        IField thisField = generateField(
                fo,
                Constants.INPUT_THIS,
                Constants.THIS_CLASS,
                Constants.THIS_TOOLTIP,
                true,
                false,
                bundle,
                isEditableLiteral,
                null);
        
        // creating input fields
        List fieldList = new ArrayList();
        IField field = null;
        int i = 1;
        int inputNum = 0;
        
        try {
            inputNum = Integer.parseInt((String) fo.getAttribute("InputNum"));
        } catch (java.lang.Throwable t) {
            t.printStackTrace(System.err);
        }
        
        for (; i <= inputNum; i++) {
            field = generateField(
                    fo,
                    Constants.INPUT_PARAM + i,
                    Constants.INPUT_TYPE + i,
                    Constants.INPUT_TOOLTIP + i,
                    true,
                    false,
                    bundle,
                    isEditableLiteral,
                    null);
            
            if (field != null) {
                fieldList.add(field);
            }
        }
        
        List input = new ArrayList(fieldList);
        fieldList.clear();
        
        // creating output fields
        i = 1;
        
        int outputNum = 0;
        
        try {
            outputNum = Integer.parseInt(
                    (String)fo.getAttribute(Constants.OUTPUT_NUM));
        } catch (java.lang.Throwable t) {
            t.printStackTrace(System.err);
        }
        
        for (; i <= outputNum; i++) {
            field = generateField(
                    fo,
                    Constants.OUTPUT_PARAM + i,
                    Constants.OUTPUT_TYPE + i,
                    Constants.OUTPUT_TOOLTIP + i,
                    false,
                    true,
                    bundle,
                    isEditableLiteral,
                    null); // new BpelLiteralHandler()
            
            if (field != null) {
                fieldList.add(field);
            }
        }
        
        List output = new ArrayList(fieldList);
        
        boolean isAccumulative = false;
        Object accumObj = fo.getAttribute(Constants.ACCUMULATIVE);
        if (accumObj != null && accumObj instanceof Boolean) {
            isAccumulative = ((Boolean) accumObj).booleanValue();
        }
        //
        String tooltip = (String)fo.getAttribute(Constants.TOOLTIP);
        tooltip = bundle.getString(tooltip);
        //
        String name = (String)fo.getAttribute(Constants.LOCAL_NAME);
        name = bundle.getString(name);
        //
        URL iconUrl = (URL)fo.getAttribute(Constants.FILE_ICON);
        Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
        Icon icon = new ImageIcon(img);
        //
        return new BasicMethoid(icon,
                MapperUtilities.cutAmpersand(name),
                tooltip, fo, thisField, input, output,
                isAccumulative, isEditableLiteral);
    }
}
