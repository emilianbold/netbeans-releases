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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.features;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.languages.ASTEvaluator;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;


/**
 *
 * @author Jan Jancura
 */
class SemanticHighlightsLayer extends AbstractHighlightsContainer {

    private static Map<Document,List<WeakReference<SemanticHighlightsLayer>>> cache = new HashMap<Document,List<WeakReference<SemanticHighlightsLayer>>> ();
            
    private Document            document;
    private OffsetsBag          offsetsBag;
    
    SemanticHighlightsLayer (Document document) {
        this.document = document;
        offsetsBag = new OffsetsBag (document);
        List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
        if (layers == null) {
            new Listener (document);
            layers = new ArrayList<WeakReference<SemanticHighlightsLayer>> ();
            cache.put (document, layers);
        }
        layers.add (new WeakReference<SemanticHighlightsLayer> (this));
    }

    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
                                                                                //S ystem.out.println("SemanticHighlightsLayer.getHighlights " + startOffset + " : " + endOffset);
        return offsetsBag.getHighlights (startOffset, endOffset);
    }
    
    private void setOffsetsBag (OffsetsBag offsetsBag) {
        this.offsetsBag = offsetsBag;
        fireHighlightsChange (0, document.getLength ());
    }

    private static class Listener extends ASTEvaluator {
        
        private Document document;
        private OffsetsBag offsetsBag;

        Listener (Document document) {
            this.document = document;
            ParserManager.get (document).addASTEvaluator (this);
        }
        
        public String getFeatureName () {
            return null; //"COLOR";
        }
        
        public void beforeEvaluation (State state, ASTNode root) {
            if (state == State.PARSING) return;
            offsetsBag = new OffsetsBag (document, true);
        }

        public void afterEvaluation (State state, ASTNode root) {
            if (state == State.PARSING) return;
            List<WeakReference<SemanticHighlightsLayer>> layers = cache.get (document);
            List<WeakReference<SemanticHighlightsLayer>> newLayers = new ArrayList<WeakReference<SemanticHighlightsLayer>> ();
            boolean remove = true;
            Iterator<WeakReference<SemanticHighlightsLayer>> it = layers.iterator ();
            while (it.hasNext()) {
                WeakReference<SemanticHighlightsLayer> weakReference = it.next ();
                SemanticHighlightsLayer layer = weakReference.get ();
                if (layer == null) continue;
                remove = false;
                layer.setOffsetsBag (offsetsBag);
                newLayers.add (weakReference);
            }
            if (remove) {
                cache.remove (document);
                ParserManagerImpl.get (document).removeASTEvaluator (this);
            } else
                cache.put (document, newLayers);
        }

        public void evaluate (State state, List<ASTItem> path, Feature feature) {
            if (state == State.PARSING) return;
            AttributeSet attributeSet = null;
            ASTItem leaf = path.get (path.size () - 1);
            SyntaxContext context = SyntaxContext.create (document, ASTPath.create (path));
            if (feature.getBoolean ("condition", context, true))
                attributeSet = ColorsManager.createColoring (feature, null);
            
            ASTNode rootNode = (ASTNode) path.get (0);
            DatabaseContext root = DatabaseManager.getRoot (rootNode);
            DatabaseItem i = root.getDatabaseItem (leaf.getOffset ());
            if (i != null && i.getEndOffset () == leaf.getEndOffset ()) {
                AttributeSet as = getAttributes (i);
                if (as != null)
                    if (attributeSet != null) 
                        ((SimpleAttributeSet) attributeSet).addAttributes (as);
                    else
                        attributeSet = as;
            }
            
            offsetsBag.addHighlight (leaf.getOffset (), leaf.getEndOffset (), attributeSet);
            
        }
    
        private static AttributeSet getAttributes (DatabaseItem item) {
            if (item instanceof DatabaseDefinition) {
                DatabaseDefinition definition = (DatabaseDefinition) item;
                if ("global_variable".equals (definition.getName ()))
                    System.out.println("");
                if (definition.getUsages ().isEmpty ()) {
                    if ("parameter".equals (definition.getType ()))
                        return getUnusedParameterAttributes ();
                    if ("variable".equals (definition.getType ()))
                        return getUnusedLocalVariableAttributes ();
                    if ("field".equals (definition.getType ()))
                        return getUnusedFieldAttributes ();
                } else {
                    if ("parameter".equals (definition.getType ()))
                        return getParameterAttributes ();
                    if ("variable".equals (definition.getType ()))
                        return getLocalVariableAttributes ();
                    if ("field".equals (definition.getType ()))
                        return getFieldAttributes ();
                }
            }
            if (item instanceof DatabaseUsage) {
                DatabaseUsage usage = (DatabaseUsage) item;
                DatabaseDefinition definition = usage.getDefinition ();
                if ("parameter".equals (definition.getType ()))
                    return getParameterAttributes ();
                if ("local".equals (definition.getType ()))
                    return getLocalVariableAttributes ();
                if ("field".equals (definition.getType ()))
                    return getFieldAttributes ();
            }
            return null;
        }
    }
    
    private static AttributeSet unusedParameterAttributeSet;
    
    private static AttributeSet getUnusedParameterAttributes () {
        if (unusedParameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (115, 115, 115));
            unusedParameterAttributeSet = sas;
        }
        return unusedParameterAttributeSet;
    }
    
    private static AttributeSet parameterAttributeSet;
    
    private static AttributeSet getParameterAttributes () {
        if (parameterAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (160, 96, 1));
            parameterAttributeSet = sas;
        }
        return parameterAttributeSet;
    }
    
    private static AttributeSet unusedLocalVariableAttributeSet;
    
    private static AttributeSet getUnusedLocalVariableAttributes () {
        if (unusedLocalVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (115, 115, 115));
            unusedLocalVariableAttributeSet = sas;
        }
        return unusedLocalVariableAttributeSet;
    }
    
    private static AttributeSet localVariableAttributeSet;
    
    private static AttributeSet getLocalVariableAttributes () {
        if (localVariableAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            localVariableAttributeSet = sas;
        }
        return localVariableAttributeSet;
    }
    
    private static AttributeSet unusedFieldAttributeSet;
    
    private static AttributeSet getUnusedFieldAttributes () {
        if (unusedFieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (115, 115, 115));
            StyleConstants.setBold (sas, true);
            unusedFieldAttributeSet = sas;
        }
        return unusedFieldAttributeSet;
    }
    
    private static AttributeSet fieldAttributeSet;
    
    private static AttributeSet getFieldAttributes () {
        if (fieldAttributeSet == null) {
            SimpleAttributeSet sas = new SimpleAttributeSet ();
            StyleConstants.setForeground (sas, new Color (9, 134, 24));
            StyleConstants.setBold (sas, true);
            fieldAttributeSet = sas;
        }
        return fieldAttributeSet;
    }
}
