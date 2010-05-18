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
package org.netbeans.modules.vmd.midp.screen.display;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
abstract class WrappedLabel extends JPanel {

    private static final long serialVersionUID = 1123746185724284730L;

    enum Mode {
        WRAP_ON(2),
        WRAP_OFF(1),
        DEFAULT(0);
        
        Mode(int mode ){
            myMode = mode;
        }
        
        public static Mode forInt( int mode ){
            for ( Mode md : values() ){
                if ( md.myMode == mode ){
                    return md;
                }
            }
            return DEFAULT;
        }
        
        private final int myMode;
    }
    
    /*WrappedLabel(){
        setMinimumSize( new JLabel().getMinimumSize());
    }*/

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paintComponent( Graphics g ) {
        fillWrapList(g);
        
        int height = g.getFontMetrics().getHeight();
        int i=1;
        for (String string : myList) {
            g.drawString( string, 0, (i-1)*height +g.getFontMetrics().getAscent() );
            i++;
        }
        int componentHeight  = myList.size()*height;
        setPreferredSize( new Dimension( getPreferedWidth() , componentHeight));
        setMaximumSize( getPreferredSize() );
        setMinimumSize( getPreferredSize() );
        revalidate();
    }
    
    public String getText(){
        return myText;
    }
    
    public void setText(String text ){
        if ( text == null || text.length() ==0 ){
            myText = " ";
        }
        else {
            myText = text;
        }
    }
   
    protected Mode getMode(){
        return myMode;
    }

    protected int getLabelHeight(){
        return -1;
    }

    protected int getPreferedWidth(){
        if ( myWidth == -1 ){
            return getLabelWidth();
        }
        else {
            return myWidth;
        }
    }

    protected void setPreferedWidth(int width ){
        myWidth = width;
    }

    abstract protected int getLabelWidth();

    protected void setMode(Mode mode) {
        myMode = mode;
    }
    
    protected String getSuffix() {
        if ( getMode() == Mode.WRAP_ON ){
            return NbBundle.getMessage(WrappedLabel.class,
                    "TXT_NoWrapSign");
        }
        else if ( getMode() == Mode.DEFAULT ){
            return "";
        }
        return null;
    }

    private Dimension getLabelSize(){
        int height = getLabelHeight();
        if ( height == -1 ){
            height = (int)super.getSize().getHeight();
        }
        return new Dimension( getPreferedWidth() , height );
    }

    private void fillWrapList( Graphics g ) {
        if ( myList == null ){
            myList = new LinkedList<String>();
        }
        String str = getText();

        int width  = getPreferedWidth();

        double empiricLetterWidth = g.getFontMetrics().getStringBounds("a", // NOI18N
                g).getWidth();
        int empiricSize = (int)(width/empiricLetterWidth);

        int indx;
        myList.clear();
        boolean noWrap= getSuffix()!= null;
        while ( (indx = getWrapIndex(str, g, width, empiricSize)) != str.length() ){
            if ( indx == 0){
                return;
            }
            String start = str.substring( 0, indx );
            if ( noWrap ){
                start =start + getSuffix();
            }
            myList.add( start );
            if ( noWrap ){
                str = "";
                break;
            }
            str = str.substring( indx);
        }
        if ( str.length() > 0 ){
            myList.add( str );
        }
    }

    private int getWrapIndex( String str, Graphics g, double width,
            int empiricSize )
    {
        if (g.getFontMetrics().getStringBounds(str, g).getWidth() <= width) {
            return str.length();
        }
        if (str.length() == 0) {
            return str.length();
        }
        
        if ( empiricSize >= str.length() ){
            empiricSize = str.length();
        }
        double startWidth = g.getFontMetrics().getStringBounds(
                getSubstring( str, empiricSize), g).getWidth();
        if (startWidth <= width) {
            for (int i = empiricSize; i <= str.length(); i++) {
                double stringWidth = g.getFontMetrics().getStringBounds(
                        getSubstring(str, i), g).getWidth();
                if (stringWidth > width) {
                    return i-1;
                }
            }
            return str.length();
        }
        else {
            for (int i = empiricSize; i >= 0; i--) {
                double stringWidth = g.getFontMetrics().getStringBounds(
                        getSubstring( str , i), g).getWidth();
                if (stringWidth <= width) {
                    return i;
                }
            }
        }
        return str.length();
        
    }
    
    private String getSubstring( String main, int index  ){
        if ( getSuffix() == null){
            return main.substring( 0 , index );
        }
        else{
            StringBuilder builder = new StringBuilder( main);
            return builder.delete( index , main.length()).append( getSuffix() ).
                toString();
        }
    }
    
    private String myText;
    private Mode myMode;
    private List<String> myList;
    private int myWidth =-1;
}
