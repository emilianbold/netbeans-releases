/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

import java.io.*;
import org.w3c.dom.*;

public class XMLUtil {
    private XMLUtil() {}

    /**
     * Takes some text to be printed into an XML stream and escapes any
     * characters that might make it invalid XML (like '<').
     */
    public static void printXML(StringBuffer out, String msg) {
        printXML(out, msg, true);
    }

    public static void printXML(StringBuffer out, String msg, boolean attribute) {
        if (msg == null)
            return;
        int msgLength = msg.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            printXML(out, c, attribute);
        }
    }

    public static void printXML(StringBuffer out, char msg, boolean attribute) {
        if (msg == '&')
            out.append("&amp;");
        else if (msg == '<')
            out.append("&lt;");
        else if (msg == '>')
            out.append("&gt;");
        else if (attribute && msg == '"')
            out.append("&quot;");
        else if (attribute && msg == '\'')
            out.append("&apos;");
        else if (attribute && msg == '\n')
            out.append("&#xA");
        else if (attribute && msg == '\t')
            out.append("&#x9");
        else
            out.append(msg);
    }

    public static boolean shouldEscape(char c) {
        if (c == '&')
            return true;
        else if (c == '<')
            return true;
        else if (c == '>')
            return true;
        return false;
    }

    public static boolean shouldEscape(String s) {
        if (s == null)
            return false;
        int msgLength = s.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = s.charAt(i);
            if (shouldEscape(c))
                return true;
        }
        return false;
    }

    /**
     * Takes some text to be printed into an XML stream and escapes any
     * characters that might make it invalid XML (like '<').
     */
    public static void printXML(java.io.Writer out, String msg) throws java.io.IOException {
        printXML(out, msg, true);
    }

    public static void printXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
        if (msg == null)
            return;
        int msgLength = msg.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            printXML(out, c, attribute);
        }
    }

    public static void printXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
        if (msg == '&')
            out.write("&amp;");
        else if (msg == '<')
            out.write("&lt;");
        else if (msg == '>')
            out.write("&gt;");
        else if (attribute && msg == '"')
            out.write("&quot;");
        else if (attribute && msg == '\'')
            out.write("&apos;");
        else if (attribute && msg == '\n')
            out.write("&#xA;");
        else if (attribute && msg == '\t')
            out.write("&#x9;");
        else
            out.write(msg);
    }

    /**
     * Reformat the DOM graph to make it look like pretty XML.
     *
     * @param doc The Document to create new TextNodes from.
     * @param indent The String used to indent per level
     */
    public static void reindent(Document doc, String indent) {
        reindent(doc, doc, -1, indent);
    }
    
    /**
     * Reformat the DOM graph to make it look like pretty XML.
     *
     * @param doc The Document to create new TextNodes from.
     * @param node The top of the tree to reindent from.
     * @param indent The String used to indent per level
     * @param level How far in to reindent
     * @return true if node is a Text node that has only whitespace
     */
    public static boolean reindent(Document doc, Node node,
                                   int level, String indent) {
        String nodeValue = node.getNodeValue();

        boolean hasOnlyWhitespaceTextChildren = true;
        NodeList children = node.getChildNodes();
        int length = children.getLength();
        for (int i = 0; i < length; ++i) {
            if (!reindent(doc, children.item(i), level+1, indent))
                hasOnlyWhitespaceTextChildren = false;
        }

        /*
        try {
            printLevel(System.out, level, indent,
                       node.getNodeName()+": \""+nodeValue+"\"\n");
            printLevel(System.out, level, indent,
                       "hasOnlyWhitespaceTextChildren="+hasOnlyWhitespaceTextChildren+"\n");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        */

        if (hasOnlyWhitespaceTextChildren && level >= 0  && length > 0) {
            // We can reindent this one.  So, go thru each child node
            // and make sure it's intendation is where we want it.
            
            StringBuffer idealWhitespaceBuf = new StringBuffer();
            printLevel(idealWhitespaceBuf, level, indent);
            String idealFinalWhitespace = "\n" + idealWhitespaceBuf.toString().intern();
            printLevel(idealWhitespaceBuf, 1, indent);
            String idealChildWhitespace = "\n"+idealWhitespaceBuf.toString().intern();
            //System.out.println("idealChildWhitespace='"+idealChildWhitespace+"'");
            //
            // Check to make sure the last child node is a text node.
            // If not, insert the correct spacing at the end.
            //
            if (length > 1 && !(children.item(length-1) instanceof Text)) {
                //System.out.println("Inserting additional whitespace at end of child list.");
                node.appendChild(doc.createTextNode(idealFinalWhitespace));
                ++length;
            }
            //System.out.println("node.getNodeName="+node.getNodeName()+" children.length="+length);
            
            boolean shouldBeTextNode = true;  // This alternates
            Text textNode;
            for (int i = 0; i < length; ++i) {
                Node childNode = children.item(i);
                boolean isTextNode = (childNode instanceof Text);
                //System.out.println("shouldBeTextNode="+shouldBeTextNode+" isTextNode="+isTextNode+" "+childNode.getNodeName());
                if (shouldBeTextNode) {
                    if (isTextNode) {
                        String childNodeValue = childNode.getNodeValue().intern();
                        if (length == 1) {
                            // We have a single text child, don't mess with
                            // it's contents.
                            continue;
                        }
                        
                        textNode = (Text) childNode;
                        // Need to make sure it has the correct whitespace
                        if (i == length-1) {
                            if (idealFinalWhitespace != childNodeValue) {
                                //System.out.println("!Incorrect whitespace on final!");
                                if (textNode.getLength() > 0)
                                    textNode.deleteData(0, textNode.getLength());
                                textNode.appendData(idealFinalWhitespace);
                            }
                            
                        } else {
                            if (idealChildWhitespace != childNodeValue) {
                                //System.out.println("!Incorrect whitespace: '"+childNodeValue+"' versus ideal of '"+idealChildWhitespace+"'");
                                textNode.deleteData(0, textNode.getLength());
                                textNode.appendData(idealChildWhitespace);
                            }
                        }
                        shouldBeTextNode ^= true;
                    } else {
                        // Need to insert a whitespace node
                        //System.out.println("Need to insert a whitespace node before "+childNode.getNodeName()+": "+childNode.getNodeValue());
                        if (i == length-1) {
                            //System.out.println("It's a final one!");
                            node.insertBefore(doc.createTextNode(idealChildWhitespace), childNode);
                            node.appendChild(doc.createTextNode(idealFinalWhitespace));
                            ++length;
                        } else {
                            //System.out.println("Not final.");
                            node.insertBefore(doc.createTextNode(idealChildWhitespace), childNode);
                        }
                        //
                        // We updated our list while going thru it at the same
                        // time, so update our indices to account for the
                        // new growth.
                        //
                        ++i;  
                        ++length;
                    }
                } else {
                    if (isTextNode) {
                        // The last whitespace node is correct, so this one
                        // must be extra.
                        //System.out.println("Extra unneeded whitespace");
                        node.removeChild(childNode);
                        --i;
                        --length;
                        if (i == length-1 && i >= 0) {
                            //System.out.println("It's a final one!");
                            // Go back and fix up the last node.
                            childNode = children.item(i);
                            String childNodeValue = childNode.getNodeValue().intern();
                            if (idealFinalWhitespace != childNodeValue) {
                                textNode = (Text) childNode;
                                //System.out.println("!Incorrect whitespace on final!");
                                if (textNode.getLength() > 0)
                                    textNode.deleteData(0, textNode.getLength());
                                textNode.appendData(idealFinalWhitespace);
                            }
                        }
                    } else {
                        // This is just right.
                        //System.out.println("This is just right.");
                        shouldBeTextNode ^= true;
                    }
                }
            }
        }

        // Let my caller know if I'm a Text node that has only whitespace
        // or not.
        if (node instanceof Text) {
            if (nodeValue == null)
                return true;
            return (nodeValue.trim().equals(""));
        }
        return true;
    }

    protected static void printLevel(StringBuffer out, int level, String indent) {
        for (int i = 0; i < level; ++i) {
            out.append(indent);
        }
    }
}
