package org.overture.typechecker.assistant.definition;

import java.util.List;
import java.util.Vector;

import org.overture.ast.definitions.AExternalDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexNameList;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;

public class AExternalDefinitionAssistantTC {

	public static PDefinition findName(AExternalDefinition d,
			ILexNameToken sought, NameScope scope) {
		
		if (sought.getOld())
		{
			return (sought.equals(d.getOldname())) ? d : null;
		}

		return (sought.equals(d.getState().getName())) ? d : null;
	}

	public static void markUsed(AExternalDefinition d) {
		d.setUsed(true);
		PDefinitionAssistantTC.markUsed(d.getState());
		
	}

	public static List<PDefinition> getDefinitions(AExternalDefinition d) {
		
		List<PDefinition> result =  new Vector<PDefinition>();
		result.add(d.getState());
	
		return result;
	}

	public static LexNameList getVariableNames(AExternalDefinition d) {
		return PDefinitionAssistantTC.getVariableNames(d.getState());
	}

	public static PType getType(AExternalDefinition def) {
		return PDefinitionAssistantTC.getType(def.getState());
	}

	public static boolean isUsed(AExternalDefinition u) {
		return PDefinitionAssistantTC.isUsed(u.getState());
		
	}

}
