/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
 */

package com.oracle.solaris.vp.panel.common.control;

import com.oracle.solaris.vp.util.misc.event.*;

public class NavigationListeners extends EventListeners<NavigationListener>
    implements NavigationListener {

    //
    // Static data
    //

    private static final EventDispatcher<NavigationStartEvent,
	NavigationListener> aDispatcher =
	new EventDispatcher<NavigationStartEvent, NavigationListener>() {
	    public void dispatch(NavigationListener listener,
		NavigationStartEvent event) {
		listener.navigationStarted(event);
	    }
	};

    private static final EventDispatcher<NavigationStopEvent,
	NavigationListener> zDispatcher =
	new EventDispatcher<NavigationStopEvent, NavigationListener>() {
	    public void dispatch(NavigationListener listener,
		NavigationStopEvent event) {
		listener.navigationStopped(event);
	    }
	};

    //
    // NavigationListener methods
    //

    @Override
    public void navigationStarted(NavigationStartEvent e) {
	dispatch(aDispatcher, e);
    }

    @Override
    public void navigationStopped(NavigationStopEvent e) {
	dispatch(zDispatcher, e);
    }
}
