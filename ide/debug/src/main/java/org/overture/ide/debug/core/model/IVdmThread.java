/*******************************************************************************
 * Copyright (c) 2009, 2011 Overture Team and others.
 *
 * Overture is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Overture is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Overture.  If not, see <http://www.gnu.org/licenses/>.
 * 	
 * The Overture Tool web-site: http://overturetool.org/
 *******************************************************************************/
package org.overture.ide.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.overture.ide.debug.core.dbgp.IDbgpSession;
import org.overture.ide.debug.core.dbgp.IDbgpStatusInterpreterThreadState;
import org.overture.ide.debug.core.dbgp.breakpoints.IDbgpBreakpoint;
import org.overture.ide.debug.core.model.eval.IVdmEvaluationEngine;
import org.overture.ide.debug.core.model.internal.IVdmStreamProxy;

public interface IVdmThread extends IThread /* , IFilteredStep */
{
	int ERR_THREAD_NOT_SUSPENDED = -3;

	IDbgpSession getDbgpSession();

	IDbgpBreakpoint getDbgpBreakpoint(String id);

	IVdmStreamProxy getStreamProxy();

	IVdmEvaluationEngine getEvaluationEngine();

	int getModificationsCount();

	void sendTerminationRequest() throws DebugException;

	int getPropertyPageSize();

	boolean retrieveGlobalVariables();

	boolean retrieveClassVariables();

	boolean retrieveLocalVariables();

	void updateStackFrames();

	IDbgpStatusInterpreterThreadState getInterpreterState();
}
