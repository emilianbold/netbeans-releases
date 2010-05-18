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

package movieslib;

import java.io.Serializable;

public class Movie implements Serializable {
    
    private static int nextId = 1;
    
    /** 
     * Creates a new instance of Movie. 
     */
    public Movie() {
        synchronized(Movie.class) {
            this.id = new Integer(nextId);
            nextId++;
        }
    }

    /**
     * Holds value of property title.
     */
    private String title;

    /**
     * Getter for property title.
     * @return Value of property title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for property title.
     * @param title New value of property title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Holds value of property year.
     */
    private Integer year;

    /**
     * Getter for property year.
     * @return Value of property year.
     */
    public Integer getYear() {
        return this.year;
    }

    /**
     * Setter for property year.
     * @param year New value of property year.
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * Holds value of property length.
     */
    private Integer length;

    /**
     * Getter for property length.
     * @return Value of property length.
     */
    public Integer getLength() {
        return this.length;
    }

    /**
     * Setter for property length.
     * @param length New value of property length.
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * Holds value of property rating.
     */
    private String rating;

    /**
     * Getter for property rating.
     * @return Value of property rating.
     */
    public String getRating() {
        return this.rating;
    }

    /**
     * Setter for property rating.
     * @param rating New value of property rating.
     */
    public void setRating(String rating) {
        this.rating = rating;
    }

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Holds value of property image.
     */
    private String image;

    /**
     * Getter for property image.
     * @return Value of property image.
     */
    public String getImage() {
        return this.image;
    }

    /**
     * Setter for property image.
     * @param image New value of property image.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Holds value of property id.
     */
    private Integer id;

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Integer getId() {
        return this.id;
    }    

    /**
     * Holds value of property genre.
     */
    private String genre;

    /**
     * Getter for property genre.
     * @return Value of property genre.
     */
    public String getGenre() {
        return this.genre;
    }

    /**
     * Setter for property genre.
     * @param genre New value of property genre.
     */
    public void setGenre(String genre) {

        this.genre = genre;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("<movie>");
        sb.append("id=");
        sb.append(id);
        sb.append(", title=");
        sb.append(title);
        sb.append(", year=");
        sb.append(year);
        sb.append(", genre=");
        sb.append(genre);
        sb.append(", length=");
        sb.append(length);
        sb.append(", rating=");
        sb.append(rating);
        sb.append(", image=");
        sb.append(image);
        sb.append(", description=");
        sb.append(description);
        sb.append("</movie>");
        return sb.toString();
    }
}
