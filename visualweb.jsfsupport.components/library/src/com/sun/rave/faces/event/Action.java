/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.faces.event;

import javax.faces.event.FacesListener;

/**
 * @author cquinn
 */
public interface Action extends FacesListener {
    public String action();
}
