/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.codegen;

import java.io.IOException;
import java.util.Map;
import javax.swing.text.JTextComponent;

/**
 *
 * @author gpatil
 */
public abstract class BaseCodegenerator {
    public static final String BEGIN_EDITOR_FOLD = 
            "<editor-fold defaultstate=\"collapsed\" desc=\"Generated JCA support code. Click on the + sign on the left to edit the code.\">"; // NOI18N
    public static final String END_EDITOR_FOLD = "</editor-fold>"; // NOI18N
    public static String METHOD_NAME = "method.name";//NOI18N
    public abstract void generateCode(JTextComponent doc, Map<String, Object> input) throws IOException;
}
