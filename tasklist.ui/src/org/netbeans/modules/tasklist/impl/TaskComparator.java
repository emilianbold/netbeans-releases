/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.tasklist.impl;

import java.util.Comparator;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;

/**
 * @author S. Aubrecht
 */
public class TaskComparator {
    
    private static Comparator<Task> DEFAULT_COMPARATOR;
    
    /** Creates a new instance of DefaultTaskComparator */
    private TaskComparator() {
    }
    
    public static Comparator<Task> getDefault() {
        if( null == DEFAULT_COMPARATOR ) {
            DEFAULT_COMPARATOR = new Comparator<Task>() {
                public int compare( Task t1, Task t2 ) {
                    int result = 0;
                    //compare groups
                    result = Accessor.getGroup( t1 ).compareTo( Accessor.getGroup( t2 ) );

                    //compare file
                    if( 0 == result ) {
                        FileObject f1 = Accessor.getResource(t1);
                        FileObject f2 = Accessor.getResource(t2);
                        if( null == f1 && null != f2 )
                            result = -1;
                        else if( null != f1 && null == f2 ) 
                            result = 1;
                        else if( null != f1 && null != f2 ) 
                            result = f1.getPath().compareTo( f2.getPath() );
                    }

                    //compare line number
                    if( 0 == result ) {
                        if( Accessor.getLine(t1) <= 0 && Accessor.getLine(t2) > 0 )
                            result = -1;
                        else if( Accessor.getLine(t1) > 0 && Accessor.getLine(t2) <= 0 )
                            result = 1;
                        else if( Accessor.getLine(t1) > 0 && Accessor.getLine(t2) > 0 )
                            result = Accessor.getLine(t1)- Accessor.getLine(t2);
                    }

                    //compare description
                    if( 0 == result ) {
                        result = Accessor.getDescription(t1).compareTo( Accessor.getDescription(t2) );
                    }
                    return result;
                }
            };
        }
        return DEFAULT_COMPARATOR;
    }
    
    public static Comparator<Task> getDescriptionComparator( boolean asc ) {
        return new DescriptionComparator( asc );
    }
    
    public static Comparator<Task> getLocationComparator( boolean asc ) {
        return new LocationComparator( asc );
    }
    
    public static Comparator<Task> getLineComparator( boolean asc ) {
        return new LineComparator( asc );
    }
    
    public static Comparator<Task> getFileComparator( boolean asc ) {
        return new FileComparator( asc );
    }
    
    private static class DescriptionComparator implements Comparator<Task> {
        private boolean asc;
        public DescriptionComparator( boolean asc ) {
            this.asc = asc;
        }

        public int compare( Task t1, Task t2 ) {
            int result = Accessor.getDescription(t1).compareTo( Accessor.getDescription(t2) );
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            return result;
        }
    
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final DescriptionComparator test = (DescriptionComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 69 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
    
    private static class LineComparator implements Comparator<Task> {
        private boolean asc;
        public LineComparator( boolean asc ) {
            this.asc = asc;
        }

        public int compare( Task t1, Task t2 ) {
            int result = 0;
            if( Accessor.getLine(t1) <= 0 && Accessor.getLine(t2) > 0 )
                result = -1;
            else if( Accessor.getLine(t1) > 0 && Accessor.getLine(t2) <= 0 )
                result = 1;
            else if( Accessor.getLine(t1) > 0 && Accessor.getLine(t2) > 0 )
                result = Accessor.getLine(t1)- Accessor.getLine(t2);
            
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            
            return result;
        }
    
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final LineComparator test = (LineComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
    
    private static class FileComparator implements Comparator<Task> {
        private boolean asc;
        public FileComparator( boolean asc ) {
            this.asc = asc;
        }

        public int compare( Task t1, Task t2 ) {
            int result = 0;
            
            FileObject f1 = Accessor.getResource(t1);
            FileObject f2 = Accessor.getResource(t2);
            if( null == f1 && null != f2 )
                result = -1;
            else if( null != f1 && null == f2 ) 
                result = 1;
            else if( null != f1 && null != f2 ) {
                result = f1.getNameExt().compareTo( f2.getNameExt() );
            }
            
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            
            return result;
        }
    
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final FileComparator test = (FileComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
    
    private static class LocationComparator implements Comparator<Task> {
        private boolean asc;
        public LocationComparator( boolean asc ) {
            this.asc = asc;
        }

        public int compare( Task t1, Task t2 ) {
            int result = 0;
            
            FileObject f1 = Accessor.getResource(t1);
            FileObject f2 = Accessor.getResource(t2);
            if( null == f1 && null != f2 )
                result = -1;
            else if( null != f1 && null == f2 ) 
                result = 1;
            else if( null != f1 && null != f2 ) {
                result = f1.getPath().compareTo( f2.getPath() );
            }
            
            if( 0 == result )
                result = getDefault().compare( t1, t2 );
            else if( !asc )
                result *= -1;
            
            return result;
        }
    
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            final LocationComparator test = (LocationComparator) o;

            if (this.asc != test.asc)
                return false;
            return true;
        }

        public int hashCode() {
            int hash = 7;
            hash = 99 * hash + (this.asc ? 1 : 0);
            return hash;
        }
    }
}
