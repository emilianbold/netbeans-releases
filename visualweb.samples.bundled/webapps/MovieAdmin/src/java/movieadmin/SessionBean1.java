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

package movieadmin;

import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.appbase.AbstractSessionBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import movieslib.Movie;
import movieslib.MovieListDataProvider;

/**
 * <p>Session scope data bean for your application.  Create properties
 *  here to represent cached data that should be made available across
 *  multiple HTTP requests for an individual user.</p>
 *
 * <p>An instance of this class will be created for you automatically,
 * the first time your application evaluates a value binding expression
 * or method binding expression that references a managed bean using
 * this class.</p>
 */
public class SessionBean1 extends AbstractSessionBean {
    // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }
    // </editor-fold>

    /**
     * <p>Construct a new session data bean instance.</p>
     */
    public SessionBean1() {
    }

    /**
     * <p>This method is called when this bean is initially added to
     * session scope.  Typically, this occurs as a result of evaluating
     * a value binding or method binding expression, which utilizes the
     * managed bean facility to instantiate this bean and store it into
     * session scope.</p>
     * 
     * <p>You may customize this method to initialize and cache data values
     * or resources that are required for the lifetime of a particular
     * user session.</p>
     */
    @SuppressWarnings(value="unchecked")
    @Override
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here
        
        // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("SessionBean1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
        
        List movies = this.movieListDataProvider.getList();
        Movie dukesMovie = (Movie) movies.get(0);
        String dukesGenre = dukesMovie.getGenre();
        this.moviesByGenre.put(dukesGenre, movies);
        this.currentGenre = dukesGenre;
    }
    
    private Map moviesByGenre = new HashMap();

    /**
     * <p>This method is called when the session containing it is about to be
     * passivated.  Typically, this occurs in a distributed servlet container
     * when the session is about to be transferred to a different
     * container instance, after which the <code>activate()</code> method
     * will be called to indicate that the transfer is complete.</p>
     * 
     * <p>You may customize this method to release references to session data
     * or resources that can not be serialized with the session itself.</p>
     */
    @Override
    public void passivate() {
    }

    /**
     * <p>This method is called when the session containing it was
     * reactivated.</p>
     * 
     * <p>You may customize this method to reacquire references to session
     * data or resources that could not be serialized with the
     * session itself.</p>
     */
    @Override
    public void activate() {
    }

    /**
     * <p>This method is called when this bean is removed from
     * session scope.  Typically, this occurs as a result of
     * the session timing out or being terminated by the application.</p>
     * 
     * <p>You may customize this method to clean up resources allocated
     * during the execution of the <code>init()</code> method, or
     * at any later time during the lifetime of the application.</p>
     */
    @Override
    public void destroy() {
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean1 getApplicationBean1() {
        return (ApplicationBean1) getBean("ApplicationBean1");
    }
    
    private String currentGenre;

    public String getCurrentGenre() {
        return currentGenre;
    }

    public void setCurrentGenre(String currentGenre) {
        this.currentGenre = currentGenre;
        List moviesInCurrentGenre = null;
        if (currentGenre != null) {
            moviesInCurrentGenre = (List) moviesByGenre.get(this.currentGenre);
            if (moviesInCurrentGenre == null) {
                moviesInCurrentGenre = new ArrayList();
            }
        }
        movieListDataProvider.setList(moviesInCurrentGenre);
    }

    @SuppressWarnings(value="unchecked")
    public void addMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Could not add null movie");
        }
        String genre = movie.getGenre();
        if ( genre == null ) {
            throw new IllegalArgumentException("Could not add movie, because its genre was null: " + movie);
        }
        // If movie is in current genre, then just add to movieListDp directly.
        // Otherwise, need to add to moviesInGenre.
        if ( genre.equals(this.currentGenre) ) {
            try {
                movieListDataProvider.appendRow(movie);
                movieListDataProvider.commitChanges();
            } catch (DataProviderException e) {
                movieListDataProvider.revertChanges();
                //rethrow e, so it is caught and logged by callers
                throw e;
            }
        } else {
            List moviesInGenre = (List) moviesByGenre.get(genre);
            if ( moviesInGenre == null ) {
                moviesInGenre = new ArrayList<Movie>();
                moviesByGenre.put(genre, moviesInGenre);
            }
            moviesInGenre.add(movie);
        }
    }
    
    public void reviseGenre(Movie movie, String newGenre) {
        if (movie == null) {
            throw new IllegalArgumentException("movie was null");
        }
        if (newGenre == null) {
            throw new IllegalArgumentException("Could not revise movie genre because new genre was null: " + movie);
        }
        String oldGenre = movie.getGenre();
        if (oldGenre == null) {
            throw new IllegalArgumentException("Could not revise movie genre because old genre was null: " + movie);
        }
        //we need to remove the movie from the old genre.
        //movieListDp contains the list of movies for the old genre.
        //it is bad practice to modify the underlying list without movieListDp's knowledge,
        //so temporarily set the current genre to null, which will set MovieListDp's list to null.
        setCurrentGenre(null);
        List moviesInOldGenre = (List) moviesByGenre.get(oldGenre);  //shouldn't be null, because movie is in the old genre
        moviesInOldGenre.remove(movie);
        movie.setGenre(newGenre);
        addMovie(movie);    //will add movie to moviesByGenre
        setCurrentGenre(newGenre);  //resets list inside movieListDp
        //set movieListDp cursor onto the movie
        RowKey row = movieListDataProvider.findFirst("id", movie.getId());
        if (row == null) {
            throw new RuntimeException("could not find movie " + movie + " after revising its genre");
        }
        movieListDataProvider.setCursorRow(row);
    }

    private MovieListDataProvider movieListDataProvider = new MovieListDataProvider();

    public MovieListDataProvider getMovieListDataProvider() {
        return movieListDataProvider;
    }
}
