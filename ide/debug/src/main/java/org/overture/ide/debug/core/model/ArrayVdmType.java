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
import org.eclipse.debug.core.model.IVariable;

/**
 * Represents an 'array' script type
 */
public class ArrayVdmType extends CollectionVdmType
{

	private static String ARRAY = "sequence"; //$NON-NLS-1$

	public ArrayVdmType()
	{
		super(ARRAY);
	}

	/**
	 * @see org.eclipse.dltk.debug.core.model.CollectionScriptType#buildDetailString(org.eclipse.debug.core.model.IVariable)
	 */
	protected String buildDetailString(IVariable variable)
			throws DebugException
	{
		String name = variable.getName();
		if (name != null && name.length() > 0)
		{
			int counter = 0;
			if (name.startsWith("-"))
			{
				counter++;
			}
			while (counter < name.length())
			{
				if (!Character.isDigit(name.charAt(counter++)))
				{
					return name + "=" + super.buildDetailString(variable);
				}
			}
		}
		return super.buildDetailString(variable);
	}
}
