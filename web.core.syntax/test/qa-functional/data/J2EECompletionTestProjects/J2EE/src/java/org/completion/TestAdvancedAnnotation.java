package org.completion;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FieldResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Temporal;
public class TestAdvancedAnnotation{
    public @interface RequestForEnhancement {
        int    id();
        String synopsis();
        String engineer() default "[unassigned]";
        String date() default "[unimplemented]";
    }
    
/** Instant substitution in case of one annotation name. */
/**CC
@|
Basic
@Basic
*/

/** Instant substitution in case of one annotation attribute. */
/**CC
@FieldResult(|
String name
@FieldResult(name=
*/
    
/** Instant substitution in case of one annotation value. */
/**
@Enumerated(v|
EnumType value = javax.persistence.EnumType.ORDINAL
@Enumerated(value=
 */

/** Completion of annotation value in case of just one attribute. */
/**CC
@Temporal(|
TemporalType value
@Temporal(value=
*/

/** Completion of Java class for resultClass attribute. */
/**CC
@NamedNativeQuery(resultClass=java.util.C|
Calendar
@NamedNativeQuery(resultClass=java.util.Calendar
*/

/** Completion of Boolean value. */
/**CC
@Column(unique=|
false
@Column(unique=false
*/

/** Completion user defined annotation. */
/**CC
@|
RequestForEnhancement
@RequestForEnhancement
*/
    
/**CC
@RequestForEnhancement(|)
int id
@RequestForEnhancement(id=)
*/
    
}
