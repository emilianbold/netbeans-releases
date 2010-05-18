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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab.util;

import org.netbeans.lib.collab.util.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.regex.*;

/**
 *
 */
public class HtmlUtility{
    public static final String HTML_BEGIN = "<html><body>";
    public static final String HTML_END = "</body></html>";

    private static final String ID_HTTP  = "http://";
    private static final String ID_WWW   = "www.";
    private static final char   ID_TAGSTART = '<';
    private static final char   ID_TAGEND   = '>';
    
    public static final String BODY_BEGIN = "<body";
    
    public static final String ID_COLOR_BLACK = "color=\"#000000\"";
    public static final String ID_COLOR_BLUE  = "color=\"#0000ff\"";
    public static final String ID_COLOR_GREEN = "color=\"#00ff00\"";
    public static final String ID_COLOR_RED   = "color=\"#ff0000\"";
    public static final String ID_COLOR_WHITE = "color=\"#ffffff\"";
    public static final String ID_COLOR_PINK  = "color=\"#ff00ff\"";
    
    public static final int FONT_COLOR_BLACK = 0;
    public static final int FONT_COLOR_BLUE  = 1;
    public static final int FONT_COLOR_RED   = 2;
    public static final int FONT_COLOR_WHITE = 3;
    public static final int FONT_COLOR_GREEN = 4;
    public static final int FONT_COLOR_PINK  = 5;
    
    private static final Pattern PATTERN_MIDDLE_SPACE = 
                Pattern.compile("([^\u0020])[\u0020][\u0020]+([^\u0020])");
    private static final Pattern PATTERN_START_SPACE = 
                                            Pattern.compile("\\A[\u0020]+");
    private static final Pattern PATTERN_END_SPACE = 
                                            Pattern.compile("[\u0020]+\\z");
     
    //given font type and size returns font tag in format of
    //"<font color=\"#0000ff\" size=2> ";
    //if size = null no size is entered
    final static public String getFontColor(int color, String size){
        StringBuffer buf = new StringBuffer("<font ");
        
        switch(color){
            case FONT_COLOR_BLACK:
                buf.append(ID_COLOR_BLACK);
                break;
            case FONT_COLOR_BLUE:
                buf.append(ID_COLOR_BLUE);
                break;
            case FONT_COLOR_RED:
                buf.append(ID_COLOR_RED);
                break;
            case FONT_COLOR_WHITE:
                buf.append(ID_COLOR_WHITE);
                break;
            case FONT_COLOR_GREEN:
                buf.append(ID_COLOR_GREEN);
                break;
            case FONT_COLOR_PINK:
                buf.append(ID_COLOR_PINK);
                break;
            default:
                buf.append(ID_COLOR_BLACK);
                break;
        }
        if(size != null){
            buf.append(" size=");
            buf.append(size);
        }
        buf.append(">");
        return buf.toString();
    }
    
    
    /**
     *
     *
     * @param
     */
    //Convert a path to an Html Image Tag
    final static public String createImageTag(String src, String alt){
        //<IMG SRC="http://"
        //WIDTH=450 HEIGHT=284 VSPACE=2 BORDER=0 ALT="Asheville, NC">
        String s = "<IMG SRC=\"" + src + "\"" + " ALT=\"" + alt + "\">" ;
        return s;
    }
    
    
    /*
     * Returns the content of the first encountered HTML tag received as a parameter
     * @param s The source string to extract the result from
     * @param tag The HTML tag
     * @return the content of the first encountered <I>tag</I>, the original string if an error occured
     */
    final static public String getTagContent(String s, HTML.Tag tag) {
        //System.out.println("HTMLUtility.getTagContent():   s: "+s);
        //System.out.println("HTMLUtility.getTagContent(): tag: "+tag);
        String tmpStr = s.toLowerCase();
        int startOpeningTag = tmpStr.indexOf("<"+tag);
        if(startOpeningTag < 0) return s;
        
        int endOpeningTag = startOpeningTag+tmpStr.substring(startOpeningTag).indexOf(">");
        
        int startClosingTag = endOpeningTag+tmpStr.substring(endOpeningTag+1).lastIndexOf("</"+tag);
        if(startClosingTag < 0) return s;
        
        //System.out.println("HTMLUtility.getTagContent(): returning: "+s.substring(endOpeningTag+1, startClosingTag));
        return s.substring(endOpeningTag+1, startClosingTag+1).trim();
    }
    
    /*
     * Returns the s string after removing the content of the encountered tag and the tags itself
     * @param s The source string to extract the result from
     * @param tag The HTML tag
     */
    final static private String removeTagAndContent(String s, HTML.Tag tag) {
        String tmpStr       = s.toLowerCase();
        StringBuffer buf    = new StringBuffer();
        int startOpeningTag = tmpStr.indexOf("<"+tag);
        if(startOpeningTag < 0) return s;
        
        buf.append(s.substring(0, startOpeningTag+1));
        
        int endOpeningTag   = startOpeningTag+tmpStr.substring(startOpeningTag).indexOf(">");
        int startClosingTag = endOpeningTag+tmpStr.substring(endOpeningTag+1).lastIndexOf("</"+tag);
        int endClosingTag   = startClosingTag+tmpStr.substring(startClosingTag).indexOf(">");
        if(endClosingTag < 0) return s;
        buf.append(s.substring(endClosingTag+1));
        
        //System.out.println("HTMLUtility.getTagContent(): returning: "+s.substring(endOpeningTag+1, startClosingTag));
        return buf.toString().trim();
    }

    /**
     * This method removes the tag argument passed to the method from the string
     * It keeps the content of the tag intact. 
     *
     */
    final static private String removeFirstTagOnly(String s, HTML.Tag tag) {
        //Thread.dumpStack();
        String tmpStr       = s.toLowerCase();        
        StringBuffer buf    = new StringBuffer();
        int startOpeningTag = tmpStr.indexOf("<"+tag);
        if(startOpeningTag < 0) return s;
                
        buf.append(s.substring(0, startOpeningTag));
        
        int endOpeningTag   = startOpeningTag+tmpStr.substring(startOpeningTag).indexOf(">");
        int startClosingTag = endOpeningTag+tmpStr.substring(endOpeningTag+1).indexOf("</"+tag);
        if (startClosingTag < 0) return s;
       
        buf.append(s.substring(endOpeningTag + 1, startClosingTag - 1));        
        int endClosingTag   = startClosingTag+tmpStr.substring(startClosingTag).indexOf(">");
        if(endClosingTag < 0) return s;
       
        buf.append(s.substring(endClosingTag+1));
                
        //System.out.println("HTMLUtility.getTagContent(): returning: "+s.substring(endOpeningTag+1, startClosingTag));
        return buf.toString().trim();
    }

    
    
    /*
     * Returns the s string after having removed the unsupported tags and their content
     * @param s The source string to extract the result from
     * @param tag The HTML tag
     */
    final static private String stripUnsupportedTags(String s) {
        HTML.Tag [] unsupportedTags = {HTML.Tag.SCRIPT};
        String tmpStr = s;
        for (int i = 0; i < unsupportedTags.length; i++) {
            while (tmpStr.toLowerCase().indexOf(unsupportedTags[i].toString()) > -1) {
                tmpStr = removeTagAndContent(tmpStr, unsupportedTags[i]);
            }
        }
        return tmpStr;
    }
    
    /*
     * returns the content of an HTML document get rid of the head part, body tags and if there first p tag
     * @param String s
     */
    final static public String getContent(String s) {
        //System.out.println("HTMLUtility.getContent: s: \n"+s);
        String content = getTagContent(s, HTML.Tag.BODY);
        //System.out.println("HTMLUtility.getContent: content: \n"+content);
        return stripUnsupportedTags(content);
    }

    /*
     * Removes the first tag <I>tag</I> encountered and i
     */
    final static public String[] splitFirstParagraph(String s, HTML.Tag tag) {
        //if the string does not begin by the specified tag then exit
        if(s != null) {
            int tagLen = tag.toString().length();
            if(s.length() > tagLen) {
                if (s.substring(0, tagLen + 1).equalsIgnoreCase("<"+tag)) {
                    String[] parts = new String[2];
                    parts[0] = getTagContent(s, tag);
                    parts[1] = removeTagAndContent(s, tag);
                    return parts;
                }
            }
        }
        return null;
    }
    
    /**
     * returns just the body portion of html text between P tags
     * leaves out <body> and <p> tags
     * @param String s
     */
    final static public String getPBody(String s){
        return getTagContent(s, HTML.Tag.P);
    }
    
    /**
     * returns just the body portion of html text
     * leaves out <body> tags
     * @param String s
     */
    protected static Pattern popen = Pattern.compile("\\A<(\\w)[^>]*>(.*)</p>(.*)\\Z", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
    protected static Pattern pclose = Pattern.compile(".*</p>.*", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
    final static public String getBody(String s){
       // fix bug id 6176277       
       // just strip the first paragraph.
        String body = getTagContent(s, HTML.Tag.BODY);
        Matcher m = popen.matcher(body);
        String cnt;
        if(m.matches() && m.group(1).equalsIgnoreCase("p") && m.group(3).length() == 0){
            cnt = m.group(2);
            Matcher mclose = pclose.matcher(cnt);
            if(!mclose.matches()){
                return cnt;
            }
        }
        return body;

    }
    
    
    /**
     *
     *
     * @param
     */
    //TODO make sure that the http:// is not already a html link tag
    //if there is already a formated <href> tag, this will not skip over it
    //converts any instance of a url to a html link
    final static public String convertLinks(String body){
         final String originalBody = body;
        StringBuffer buf    = new StringBuffer();
        String       tmpStr = null;
        int a = 0;
        int b = 0;
        
        while (a < body.length()) {
            tmpStr = body.substring(a);
            if (tmpStr.charAt(0) == ID_TAGSTART) { 
                //If we're entering an HTML tag 
                //Then jump to the end of the tag
                if ((b = tmpStr.indexOf(ID_TAGEND)) == -1) { //Illegal HTML: tag not closed
                    return originalBody;
                } else {
                    b += (a +1);
                    buf.append(body.substring(a,b));
                }
            } else {
                if (tmpStr.startsWith(ID_HTTP)) {                    
                    if ((b = getEndOfURL(tmpStr)) == -1) { //Unknown error
                        return originalBody;
                    } else {
                        b += a;
                        buf.append(createLink(body.substring(a, b), body.substring(a, b)));
                    }
                } else if(tmpStr.startsWith(ID_WWW)) {                    
                    if ((b = getEndOfURL(tmpStr)) == -1) { //Unknown error
                        return originalBody;
                    } else {
                        b += a;
                        buf.append(createLink(ID_HTTP + body.substring(a, b), body.substring(a, b)));
                    }
                } else {
                    b++; //Move pointer b to b+1
                    buf.append(body.substring(a,b));
                }
            }
            a = b; //Move pointer a to b
        }
        return buf.toString();
    }
    
    private static int getEndOfURL(String str) {
        //End of URL is either a space or the beginning of a tag
        int i = str.indexOf(ID_TAGSTART);
        int j = str.indexOf(' ');
        if ((i == -1) && (j > -1)) {
            return j;
        } else if ((i > -1) && (j == -1)) {
            return i;
        } else if ((i > -1) && (j > -1)) {
            return Math.min(i, j);
        } else {
            return -1;
        }
    }
    
    
    /**
     *
     *
     * @param
     */
    //TODO make sure that the http:// is not already a html link tag
    //if there is already a formated <href> tag, this will not skip over it
    //converts any instance of a url to a html link
    final static public String convertLinks2(String body){
        int tmp = 0;
        
        
        while(true){
            tmp = body.indexOf("http://", tmp);//7
            if(tmp < 0) break;
            int tmp2 = body.indexOf(" ", tmp);
            String link = body.substring(tmp, tmp2);
            tmp2 = link.indexOf("<");
            if(!(tmp2 < 0)) link = link.substring(0, tmp2);            
            body = StringUtility.replaceString(link, createLink(link, link), body);
            tmp += (2 * link.length()) + 8;//size of link + html tags
        }
        return body;
    }
    
    
    /**
     *
     *
     * @param
     */
    final static public String convertFont(String s){
        //s = StringUtility.replaceString("size=\"8\"", "size=\"0\"", s);
        //s = StringUtility.replaceString("size=\"10\"", "size=\"1\"", s);
        //s = StringUtility.replaceString("size=\"12\"", "size=\"2\"", s);
        //s = StringUtility.replaceString("size=\"14\"", "size=\"3\"", s);
        //s = StringUtility.replaceString("size=\"18\"", "size=\"4\"", s);
        //s = StringUtility.replaceString("size=\"24\"", "size=\"5\"", s);
        //s = StringUtility.replaceString("size=\"32\"", "size=\"6\"", s);
        //if(s.equals("8")) return "0\" ";
        //else if(s.equals("10")) return "1\"";
        if(s.equals("12")) return "2\"";
        else if(s.equals("14")) return "3\"";
        else if(s.equals("18")) return "4\"";
        else if(s.equals("24")) return "5\"";
        else if(s.equals("32")) return "6\"";
        else return "3\"";
    }
    
    
    
    /**
     *
     *
     * @param
     */
    final static public String convertFontSize(boolean[] list, String s){
        //for(int n = 0; n< list.length; n++){
        //    if(list[n])
        //}
        //if(list[0])
        //    s = StringUtility.replaceString("size=\"8\"", "size=\"0\"", s);
        //if(list[1])
        //    s = StringUtility.replaceString("size=\"10\"", "size=\"1\"", s);
        if(list[0])
            s = StringUtility.replaceString("size=\"12\"", "size=\"2\"", s);
        if(list[1])
            s = StringUtility.replaceString("size=\"14\"", "size=\"3\"", s);
        if(list[2])
            s = StringUtility.replaceString("size=\"18\"", "size=\"4\"", s);
        if(list[3])
            s = StringUtility.replaceString("size=\"24\"", "size=\"5\"", s);
        if(list[4])
            s = StringUtility.replaceString("size=\"32\"", "size=\"6\"", s);
        return s;
    }
    
    
    
    /**
     * //Inserts image tag into a given Document at cursor pos
     *
     * @param
     */
    final static public void insertImage(Document d, int pos, String src, String alt, int width,  int height, int  vspace, int border){
        MutableAttributeSet a = new SimpleAttributeSet();
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
        a.addAttribute(HTML.Attribute.SRC, src);
        a.addAttribute(HTML.Attribute.ALT , alt);
        try{
            d.insertString(pos, "i", a);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    final static public void insertImage(Document d, int pos, String src, String alt) {
        insertImage(d, pos, src, alt, 0, 0, 0, 0);
    }
    
    
    /**
     * Create a html link from a given url and display string
     * @param String link, String name
     */
    final static public String createLink(String link, String name){
        StringBuffer buf = new StringBuffer("<a href=");
        buf.append("\"");
        buf.append(link);
        buf.append("\"");
        buf.append(">");
        buf.append(name.trim());
        buf.append("</a>");
        return buf.toString();
        //String text = " <a href=" +link+ " target=install> ";
        //return text +name+ " </a>";
    }
    
    
    /**
     * Used in cases where a real link can't be inserted
     * because 1.2.2 has problems inserting tags, may want to create fake looking link
     * I think most problems have been fixed but I'll leave for now
     * @param String lnk
     */
    final static public String createFakeLink(String lnk){
        return "<u><font color=\""
        +HtmlUtility.getFontColor(FONT_COLOR_BLUE, null)
        + "\">"+lnk+"</font></u>";
    }
    
    
    /**
     * takes a url string and adds http:// on end if needed
     * @param String url
     */
    final static public String formatUrl(String url){
        if(url.equals(""))
            return "";
        else if(url.startsWith("http://"))
            return url;
        else if(url.startsWith("file:///"))
            return url;
        else
            return "http://" + url;
        //else
        //    return url;
    }
    
    final static public int getPosAfterBody(String s) {
        int startPos = s.indexOf(BODY_BEGIN);
        if (startPos == -1) return -1;
        int endPos = s.substring(startPos).indexOf(">");
        if (endPos == -1) return -1;
        return startPos+endPos+1;
    }

    //Ampersand has to be the last, because it's treated separatly from the others
    private static final char[]   unescapedChars = { '&',  '<',    '>',     '\"' };
    private static final String[] escapedStrings = { "&amp;", "&lt;", "&gt;", "&quot;"};    
    
    public static String escape(String s){
        for (int i = 0; i<unescapedChars.length; i++) {            
            s = StringUtility.substitute(s, unescapedChars[i], escapedStrings[i]);
        }
        return s;
    }
    
    /**
     * Html unescape a string
     */
    public static String unescape(String s){
        if (s == null || s.length() <= 0) return s;        
        for (int i=0; i<escapedStrings.length-1; i++) {            
            s = StringUtility.substitute(s, escapedStrings[i], unescapedChars[i]);
        }
        return s;
    }
    
    
    /**
     * Substitutes all instances of a pattern within a HTML String, by another
     * String but only outside of HTML tags. 
     * It recognizes escaped characters as one character to be replaced.
     * @param to input HTML string
     * @param from pattern to replace must not contains escaped characters
     * @param to String to replace instances of the pattern with
     * @return new String in which all instances of swapOut have been replaced
     * substituted with swapIn
     */
    final public static String substitute(final String s, final String from, final String to) {
        if (s    == null || s.length()    <= 0) return s;
        if (from == null || from.length() <= 0) return s;
        if (to   == null)                       return s;
        if (from == to)                         return s;
        
        int a = 0; //points to the beginning of the chunck to process
        int b = 0; //points to the end of the chunck to process
        int c = 0; //temporary pointer
        final int    maxA         = s.length() - 1;
        StringBuffer buf          = new StringBuffer();
        String       strToProcess = s.trim();
        String       unescapedStr = null;
        //System.out.println("HtmlUtility.substitute(): s: "+s);
        while (strToProcess.length() > 0) {
            //System.out.println("HtmlUtility.substitute(): strToProcess: "+strToProcess);
            if ((b = strToProcess.indexOf('<')) >= 0) { //if there is HTML tag(s) in the original string
                unescapedStr = unescape(strToProcess.substring(0,b));
                while ((c = unescapedStr.indexOf(from)) >= 0) {
		    //System.out.println("HtmlUtility.substitute(): unescapedStr.substring(0,c): "+ unescapedStr.substring(0,c));
                    //if the string to replace is present then put what's before in the buffer
                    //put the replacement string in the buffer and loop on the next part of the string
                    buf.append(escape(unescapedStr.substring(0,c)));
                    buf.append(to);
                    unescapedStr = unescapedStr.substring(c+from.length());
                }
                buf.append(escape(unescapedStr));
                //append the html tag to the buffer
                a = b;
                //indexOf from b since becuase you can have 
                //unescaped '>' in content
                b = strToProcess.indexOf('>',a);
                
                if (b < 0) {
                    //System.err.println("HtmlUtility.substitute(): Malformed HTML");
                    return s;
                } 
                buf.append(strToProcess.substring(a,b+1));
            } else { //if there is no HTML tag(s) in the original string
                unescapedStr = unescape(strToProcess);
                while ((c = unescapedStr.indexOf(from)) >= 0) {
		    //System.out.println("HtmlUtility.substitute(): unescapedStr.substring(0,c): "+ unescapedStr.substring(0,c));
                    buf.append(escape(unescapedStr.substring(0,c)));
                    buf.append(to);
                    unescapedStr = unescapedStr.substring(c+from.length());
                }
                buf.append(escape(unescapedStr));
                b = strToProcess.length() - 1;
            }
            strToProcess = strToProcess.substring(b+1);
            a = b = 0;
        }
        //System.out.println("HtmlUtility.substitute(): returning: "+buf.toString());
        return buf.toString();
    }

    
    private static String replaceSpace(String s) {
        Pattern p = PATTERN_MIDDLE_SPACE;
        Matcher m = p.matcher(s);
        StringBuffer out = new StringBuffer();
        int prev = 0;
        while(m.find(prev)) {
            out.append(s.substring(prev,m.start()));
            int len = m.end() - m.start();
            StringBuffer temp = new StringBuffer();
            for(int i = 1; i < len - 2; i++) {
                temp.append("&nbsp;");
            }
            temp.append(" ");
            out.append(m.group(1));
            out.append(temp);
            //out.append(m.group(2));
            prev = m.end() - 1;
        }
        out.append(s.substring(prev));
        p = PATTERN_START_SPACE;
        m = p.matcher(out.toString());
        if(m.find()) {
            int len = m.end() - m.start();
            StringBuffer temp = new StringBuffer();
            for(int i = 0; i < len; i++) {
                temp.append("&nbsp;");
            }
            out.replace(m.start(), m.end(), temp.toString());
        }

        p = PATTERN_END_SPACE;
        m = p.matcher(out.toString());
        if(m.find()) {
            int len = m.end() - m.start();
            StringBuffer temp = new StringBuffer();
            for(int i = 0; i < len; i++) {
                temp.append("&nbsp;");
            }
            out.replace(m.start(), m.end(), temp.toString());
        }
        return out.toString();

    }
    
    final public static String substituteSpace(final String s) {
        if (s    == null || s.length()    <= 0) return s;
        
        int a = 0; //points to the beginning of the chunck to process
        int b = 0; //points to the end of the chunck to process
        int c = 0; //temporary pointer
        final int    maxA         = s.length() - 1;
        StringBuffer buf          = new StringBuffer();
        String       strToProcess = s.trim();
        String       unescapedStr = null;
        //System.out.println("HtmlUtility.substitute(): s: "+s);
        while (strToProcess.length() > 0) {
            //System.out.println("HtmlUtility.substitute(): strToProcess: "+strToProcess);
               if ((b = strToProcess.indexOf('<')) >= 0) { //if there is HTML tag(s) in the original string
                unescapedStr = unescape(strToProcess.substring(0,b));
                /*while ((c = unescapedStr.indexOf(from)) >= 0) {
		    //System.out.println("HtmlUtility.substitute(): unescapedStr.substring(0,c): "+ unescapedStr.substring(0,c));
                    //if the string to replace is present then put what's before in the buffer
                    //put the replacement string in the buffer and loop on the next part of the string
                    buf.append(escape(unescapedStr.substring(0,c)));
                    buf.append(to);
                    unescapedStr = unescapedStr.substring(c+from.length());
                }*/
                buf.append(escape(replaceSpace(unescapedStr)));
                //append the html tag to the buffer
                a = b;
                //indexOf from b since becuase you can have 
                //unescaped '>' in content
                b = strToProcess.indexOf('>',a);
                
                if (b < 0) {
                    //System.err.println("HtmlUtility.substitute(): Malformed HTML");
                    return s;
                } 
                buf.append(strToProcess.substring(a,b+1));
            } else { //if there is no HTML tag(s) in the original string
                unescapedStr = unescape(strToProcess);
                /*while ((c = unescapedStr.indexOf(from)) >= 0) {
		    //System.out.println("HtmlUtility.substitute(): unescapedStr.substring(0,c): "+ unescapedStr.substring(0,c));
                    buf.append(escape(unescapedStr.substring(0,c)));
                    buf.append(to);
                    unescapedStr = unescapedStr.substring(c+from.length());
                }*/
                buf.append(escape(replaceSpace(unescapedStr)));
                b = strToProcess.length() - 1;
            }
            strToProcess = strToProcess.substring(b+1);
            a = b = 0;
        }
        //System.out.println("HtmlUtility.substitute(): returning: "+buf.toString());
        return buf.toString();
    }
    
    /*
    public static void main(String[] arg)
    {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(arg[0]));
	    String s = null;
	    while ((s = reader.readLine()) != null) {
		s = substitute(s, ":))", "<img src=\"lol.gif\">");
		s = substitute(s, ":-)", "<img src=\"smile.gif\">");
		s = substitute(s, ";-)", "<img src=\"wink.gif\">");
		s = substitute(s, ":)", "<img src=\"smile.gif\">");
		s = substitute(s, ";)", "<img src=\"wink.gif\">");
		
		System.out.println(s);
	    }
	} catch (Exception e) {	    
	    e.printStackTrace();
	}	
    }
    */

    public static boolean containsPre(String body) {
        return body.contains("</pre>");
    }
}


