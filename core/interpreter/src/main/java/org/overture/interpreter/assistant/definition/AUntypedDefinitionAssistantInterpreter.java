package org.overture.interpreter.assistant.definition;

import org.overture.ast.definitions.AUntypedDefinition;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.values.NameValuePairList;

public class AUntypedDefinitionAssistantInterpreter
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public AUntypedDefinitionAssistantInterpreter(
			IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

	public static NameValuePairList getNamedValues(AUntypedDefinition d,
			Context initialContext)
	{
		assert false : "Can't get name/values of untyped definition?";
		return null;
	}

}
