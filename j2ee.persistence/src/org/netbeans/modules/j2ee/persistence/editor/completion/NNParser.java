///*
// * The contents of this file are subject to the terms of the Common Development
// * and Distribution License (the License). You may not use this file except in
// * compliance with the License.
// *
// * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
// * or http://www.netbeans.org/cddl.txt.
// *
// * When distributing Covered Code, include this CDDL Header Notice in each file
// * and include the License file at http://www.netbeans.org/cddl.txt.
// * If applicable, add the following below the CDDL Header, with the fields
// * enclosed by brackets [] replaced by your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// * Microsystems, Inc. All Rights Reserved.
// */
//package org.netbeans.modules.j2ee.persistence.editor.completion;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import javax.swing.text.BadLocationException;
//import org.netbeans.editor.BaseDocument;
//import org.netbeans.editor.SyntaxSupport;
//import org.netbeans.editor.TokenItem;
//import org.netbeans.editor.ext.java.JavaSyntaxSupport;
//import org.netbeans.editor.ext.java.JavaTokenContext;
//import org.openide.ErrorManager;
//
///**
// * Builds an annotations tree containg NN name and attribs map. Supports nested annotations.
// *
// * @author Marek Fukala
// */
// TODO: RETOUCHE
//public class NNParser {
//    
//    //parser states
//    private static final int INIT = 0;
//    private static final int NN = 1; //@
//    private static final int ERROR = 2;
//    private static final int NNNAME = 3; //@Table
//    private static final int INNN = 4; //@Table(
//    private static final int ATTRNAME = 5; //@Table(name
//    private static final int EQ = 6; //@Table(name=
//    private static final int ATTRVALUE = 7; //@Table(name="hello" || @Table(name=@
//    
//    private JavaSyntaxSupport sup;
//    
//    public NNParser(BaseDocument bdoc) {
//        SyntaxSupport ssup = bdoc.getSyntaxSupport();
//        if(!(ssup instanceof JavaSyntaxSupport)) throw new IllegalArgumentException("Only java files are supported!");
//        sup = (JavaSyntaxSupport)ssup;
//    }
//    
//    public NN parseAnnotation(int offset) {
//        int nnStart = findAnnotationStart(offset);
//        if(nnStart == -1) {
//            return null;
//        } else {
//            return parseAnnotationOnOffset(nnStart);
//        }
//    }
//    
//    /** very simple annotations parser */
//    private NN parseAnnotationOnOffset(int offset) {
//        try {
//            int parentCount = -1;
//            int state = INIT;
//            TokenItem ti = sup.getTokenChain(offset, offset+1);
//            
//            assert ti.getTokenID() == JavaTokenContext.ANNOTATION;
//            
//            int nnstart = offset;
//            int nnend = -1;
//            String nnName = null;
//            String currAttrName = null;
//            String currAttrValue = null;
//            List<NNAttr> attrs = new ArrayList<NNAttr>(5);
//            //helper var
//            int eqOffset = -1;
//            
//            do {
//                int tid = ti.getTokenID().getNumericID();
//                //ignore whitespaces
//                if(tid == JavaTokenContext.WHITESPACE_ID) {
//                    ti = ti.getNext();
//                    continue;
//                }
//                
//                switch(state) {
//                    case INIT:
//                        switch(tid) {
//                            case JavaTokenContext.ANNOTATION_ID:
//                                state = NN;
//                                break;
//                            default:
//                                state = ERROR;
//                        }
//                        break;
//                    case NN:
//                        switch(tid) {
//                            case JavaTokenContext.IDENTIFIER_ID:
//                                state = NNNAME;
//                                nnName = ti.getImage();
////                                debug("parsing annotation " + nnName);
//                                break;
//                            default:
//                                state = ERROR;
//                        }
//                        break;
//                    case NNNAME:
//                        switch(tid) {
//                            case JavaTokenContext.LPAREN_ID:
//                                state = INNN;
//                                break;
//                            case JavaTokenContext.DOT_ID:
//                            case JavaTokenContext.IDENTIFIER_ID:
//                                //add the token image to the NN name
//                                nnName += ti.getImage();
//                                break;
//                            default:
//                                //we are in NN name, but no parenthesis came
//                                //this mean either error or annotation without parenthesis like @Id
//                                nnend = nnstart + "@".length() + nnName.length();
//                                NN newNN = new NN(nnName, attrs, nnstart, nnend);
//                                return newNN;
//                        }
//                        break;
//                    case INNN:
//                        switch(tid) {
//                            case JavaTokenContext.IDENTIFIER_ID:
//                                currAttrName = ti.getImage();
////                                debug("parsing attribute " + currAttrName);
//                                state = ATTRNAME;
//                                break;
//                                //case JavaTokenContext.RPAREN_ID:
//                            case JavaTokenContext.COMMA_ID:
//                                //just consume, still in INNN
//                                break;
//                            default:
//                                //we reached end of the annotation, or error
//                                state = ERROR;
//                                break;
//                        }
//                        break;
//                    case ATTRNAME:
//                        switch(tid) {
//                            case JavaTokenContext.EQ_ID:
//                                state = EQ;
//                                eqOffset = ti.getOffset();
//                                break;
//                            default:
//                                state = ERROR;
//                        }
//                        break;
//                    case EQ:
//                        switch(tid) {
//                            case JavaTokenContext.STRING_LITERAL_ID:
//                                state = INNN;
//                                currAttrValue = Utils.unquote(ti.getImage());
//                                attrs.add(new NNAttr(currAttrName, currAttrValue, ti.getOffset(), true));
//                                break;
//                            case JavaTokenContext.IDENTIFIER_ID:
//                                state = INNN;
//                                currAttrValue = ti.getImage();
//                                attrs.add(new NNAttr(currAttrName, currAttrValue, ti.getOffset(), false));
//                                break;
//                            case JavaTokenContext.ANNOTATION_ID:
//                                //nested annotation
//                                NN nestedNN = parseAnnotationOnOffset(ti.getOffset());
//                                attrs.add(new NNAttr(currAttrName, nestedNN, ti.getOffset(), false));
//                                state = INNN;
//                                //I need to skip what was parsed in the nested annotation in this parser
//                                ti = sup.getTokenChain(nestedNN.getEndOffset(), nestedNN.getEndOffset()+1);
//                                continue; //next loop
//                            default:
//                                //ERROR => recover
////                                debug("found uncompleted attribute " + currAttrName);
//                                //set the start offset of the value to the offset of the equator + 1
//                                attrs.add(new NNAttr(currAttrName, "", eqOffset + 1, false));
//                                state = INNN;
//                                break;
//                        }
//                }
//                
//                //if(state == ERROR) return null;
//                if(state == ERROR) {
//                    //return what we parser so far to be error recovery as much as possible
//                    nnend = ti.getOffset() + ti.getImage().length();
//                    NN newNN = new NN(nnName, attrs, nnstart, nnend);
//                    return newNN;
//                }
//                ti = ti.getNext();//get next token
//                
//            } while(ti != null);
//            
//        }catch(BadLocationException e) {
//            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
//        }
//        
//        return null;
//    }
//    
//    
//    private int  findAnnotationStart(int offset) {
//        int parentCount = -100;
//        try {
//            TokenItem ti = sup.getTokenChain(offset - 1, offset);
//            while(ti != null) {
////                debug(ti);
//                if(ti.getTokenID() == JavaTokenContext.RPAREN) {
//                    if(parentCount == -100) parentCount = 0;
//                    parentCount++;
//                } else if(ti.getTokenID() == JavaTokenContext.LPAREN) {
//                    if(parentCount == -100) parentCount = 0;
//                    parentCount--;
//                } else if(ti.getTokenID() == JavaTokenContext.ANNOTATION) {
//                    if(parentCount == -1 || parentCount == -100) { //needed if offset is not within annotation content
////                        debug("found outer annotation: " + ti.getImage());
//                        return ti.getOffset();
//                    }
//                }
//                ti = ti.getPrevious();
//            }
//            
//        }catch(BadLocationException e) {
//            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
//        }
//        
//        return -1;
//    }
//    
////    private static void debug(Object message) {
////        System.out.println(message.toString());
////    }
//    
//    public class NNAttr {
//        private String name;
//        private Object value;
//        private int valueOffset;
//        private boolean quoted;
//        
//        NNAttr(String name, Object value, int valueOffset, boolean quoted) {
//            this.name = name;
//            this.value = value;
//            this.valueOffset = valueOffset;
//            this.quoted = quoted;
//        }
//        
//        public String getName() {
//            return name;
//        }
//        
//        public Object getValue() {
//            return value;
//        }
//        
//        public int getValueOffset() {
//            return valueOffset;
//        }
//        
//        public boolean isValueQuoted() {
//            return quoted;
//        }
//        
//    }
//    
//    public class NN {
//        
//        private String name;
//        private List<NNAttr> attributes;
//        private int startOffset, endOffset;
//        
//        public NN(String name, List<NNAttr> attributes, int startOffset, int endOffset) {
//            this.name = name;
//            this.attributes = attributes;
//            this.startOffset = startOffset;
//            this.endOffset = endOffset;
//        }
//        
//        public String getName() {
//            return name;
//        }
//        
//        public List<NNAttr> getAttributesList() {
//            return attributes;
//        }
//        
//        public Map<String,Object> getAttributes() {
//            HashMap<String,Object> map = new HashMap<String,Object>();
//            for(NNAttr nnattr : getAttributesList()) {
//                map.put(nnattr.getName(), nnattr.getValue());
//            }
//            return map;
//        }
//        
//        public NNAttr getAttributeForOffset(int offset) {
//            NNAttr prevnn = null;
//            for(NNAttr nnattr : getAttributesList()) {
//                if(nnattr.getValueOffset() >= offset) {
//                    prevnn = nnattr;
//                    break;
//                }
//                prevnn = nnattr;
//            }
//            
//            if(prevnn == null) return null;
//            
//            int nnEndOffset = prevnn.getValueOffset() + prevnn.getValue().toString().length() + (prevnn.isValueQuoted() ? 2 : 0);
//            if(nnEndOffset >= offset && prevnn.getValueOffset() <= offset) {
//                return prevnn;
//            } else {
//                return null;
//            }
//        }
//        
//        public int getStartOffset() {
//            return startOffset;
//        }
//        
//        public int getEndOffset() {
//            return endOffset;
//        }
//        
//        public String toString() {
//            //just debug purposes -> no need for superb performance
//            String text = "@" + getName() + " [" + getStartOffset() + " - " + getEndOffset() + "](";
//            for(NNAttr nnattr : getAttributesList()) {
//                String key = nnattr.getName();
//                String value = nnattr.getValue().toString();
//                text+=key+"="+value+ " (" + nnattr.getValueOffset() + ") ,";
//            }
//            text = text.substring(0, text.length() -1);
//            text+=")";
//            return text;
//        }
//        
//    }
//    
//}
