package org.overture.interpreter.eval;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AClassInvariantDefinition;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.lex.Dialect;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.patterns.ASetBind;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.patterns.PMultipleBind;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.AAlwaysStm;
import org.overture.ast.statements.AApplyObjectDesignator;
import org.overture.ast.statements.AAssignmentStm;
import org.overture.ast.statements.AAtomicStm;
import org.overture.ast.statements.ABlockSimpleBlockStm;
import org.overture.ast.statements.ACallObjectStm;
import org.overture.ast.statements.ACallStm;
import org.overture.ast.statements.ACaseAlternativeStm;
import org.overture.ast.statements.ACasesStm;
import org.overture.ast.statements.AClassInvariantStm;
import org.overture.ast.statements.ACyclesStm;
import org.overture.ast.statements.ADefLetDefStm;
import org.overture.ast.statements.ADurationStm;
import org.overture.ast.statements.AElseIfStm;
import org.overture.ast.statements.AErrorStm;
import org.overture.ast.statements.AExitStm;
import org.overture.ast.statements.AFieldObjectDesignator;
import org.overture.ast.statements.AFieldStateDesignator;
import org.overture.ast.statements.AForAllStm;
import org.overture.ast.statements.AForIndexStm;
import org.overture.ast.statements.AForPatternBindStm;
import org.overture.ast.statements.AIdentifierObjectDesignator;
import org.overture.ast.statements.AIdentifierStateDesignator;
import org.overture.ast.statements.AIfStm;
import org.overture.ast.statements.ALetBeStStm;
import org.overture.ast.statements.AMapSeqStateDesignator;
import org.overture.ast.statements.ANewObjectDesignator;
import org.overture.ast.statements.ANonDeterministicSimpleBlockStm;
import org.overture.ast.statements.ANotYetSpecifiedStm;
import org.overture.ast.statements.APeriodicStm;
import org.overture.ast.statements.AReturnStm;
import org.overture.ast.statements.ASelfObjectDesignator;
import org.overture.ast.statements.ASkipStm;
import org.overture.ast.statements.ASpecificationStm;
import org.overture.ast.statements.AStartStm;
import org.overture.ast.statements.ASubclassResponsibilityStm;
import org.overture.ast.statements.ATixeStm;
import org.overture.ast.statements.ATixeStmtAlternative;
import org.overture.ast.statements.ATrapStm;
import org.overture.ast.statements.AWhileStm;
import org.overture.config.Settings;
import org.overture.interpreter.assistant.definition.PDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.PMultipleBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.PPatternAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ACaseAlternativeStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AStartStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ATixeStmtAlternativeAssistantInterpreter;
import org.overture.interpreter.assistant.statement.SSimpleBlockStmAssistantInterpreter;
import org.overture.interpreter.debug.BreakpointManager;
import org.overture.interpreter.messages.rtlog.RTExtendedTextMessage;
import org.overture.interpreter.messages.rtlog.RTLogger;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ExitException;
import org.overture.interpreter.runtime.PatternMatchException;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.runtime.VdmRuntime;
import org.overture.interpreter.runtime.VdmRuntimeError;
import org.overture.interpreter.scheduler.BasicSchedulableThread;
import org.overture.interpreter.scheduler.ISchedulableThread;
import org.overture.interpreter.scheduler.SharedStateListner;
import org.overture.interpreter.values.BooleanValue;
import org.overture.interpreter.values.FunctionValue;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.MapValue;
import org.overture.interpreter.values.NameValuePair;
import org.overture.interpreter.values.NameValuePairList;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.OperationValue;
import org.overture.interpreter.values.Quantifier;
import org.overture.interpreter.values.QuantifierList;
import org.overture.interpreter.values.RecordValue;
import org.overture.interpreter.values.SeqValue;
import org.overture.interpreter.values.SetValue;
import org.overture.interpreter.values.UndefinedValue;
import org.overture.interpreter.values.UpdatableValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueList;
import org.overture.interpreter.values.ValueListenerList;
import org.overture.interpreter.values.ValueMap;
import org.overture.interpreter.values.ValueSet;
import org.overture.interpreter.values.VoidReturnValue;
import org.overture.interpreter.values.VoidValue;
import org.overture.parser.config.Properties;

public class StatementEvaluator extends DelegateExpressionEvaluator
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -8502928171757367740L;

	
	@Override
	public Value caseAAlwaysStm(AAlwaysStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		Value rv = null;
		ExitException bodyRaised = null;

		try
		{
			rv = node.getBody().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
		catch (ExitException e)
		{
			// Finally clause executes the "always" statement, but we
			// re-throw this exception, unless the always clause raises one.

			bodyRaised = e;
		}
		finally
		{
			node.getAlways().apply(VdmRuntime.getStatementEvaluator(),ctxt);

			if (bodyRaised != null)
			{
				throw bodyRaised;
			}
		}

		return rv;
	}
	
	
	@Override
	public Value caseAAssignmentStm(AAssignmentStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		Value newval = node.getExp().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		Value oldval = node.getTarget().apply(VdmRuntime.getStatementEvaluator(),ctxt);

		//Experimental hood added for DESTECS
		if(Settings.dialect == Dialect.VDM_RT)
		{
			SharedStateListner.beforeAssignmentSet(node,oldval,newval);
		}
		
		try
		{
			oldval.set(node.getLocation(), newval.convertTo(node.getTargetType(), ctxt), ctxt);
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		if (Settings.dialect == Dialect.VDM_RT &&
			Properties.rt_log_instvarchanges)
		{
			ObjectValue self = ctxt.getSelf();	// May be a static

			// The showtrace plugin does not like "quotes", nor does it
			// have a \" type convention, so we substitute for apostrophes.
			String noquotes = newval.toString().replaceAll("\\\"", "\'");

			if (self == null)
			{
    			RTLogger.log(new RTExtendedTextMessage(
    				"InstVarChange -> instnm: \"" + node.getTarget().toString() + "\"" +
    				" val: \"" + noquotes + "\"" +
    				" objref: nil" +
    				" id: " + BasicSchedulableThread.getThread(Thread.currentThread()).getId()));
			}
			else
			{
    			RTLogger.log(new RTExtendedTextMessage(
    				"InstVarChange -> instnm: \"" + node.getTarget().toString() + "\"" +
    				" val: \"" + noquotes + "\"" +
    				" objref: " + self.objectReference +
    				" id: " + BasicSchedulableThread.getThread(Thread.currentThread()).getId()));
			}
		}

		return new VoidValue();
	}
	
	@Override
	public Value caseAAtomicStm(AAtomicStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		
		int size = node.getAssignments().size();
		ValueList targets = new ValueList(size);
		ValueList values = new ValueList(size);
		
		// Rather than execute the assignment statements directly, we calculate the
		// Updateable values that would be affected, and the new values to put in them.
		// Note that this does not provoke any invariant checks (other than those that
		// may be present in the RHS expression of each assignment).
		
		for (AAssignmentStm stmt: node.getAssignments())
		{
			try
			{
				stmt.getLocation().hit();
				targets.add(stmt.getTarget().apply(VdmRuntime.getStatementEvaluator(), ctxt));				
				values.add(stmt.getExp().apply(VdmRuntime.getStatementEvaluator(), ctxt).convertTo(stmt.getTargetType(), ctxt));
			}
			catch (ValueException e)
			{
				VdmRuntimeError.abort(node.getLocation(), e);
			}
		}
		
		// We make the assignments atomically by turning off thread swaps and time
		// then temporarily removing the listener lists from each Updateable target.
		// Then, when all assignments have been made, we check the invariants by
		// passing the updated values to each listener list, as the assignment would have.
		// Finally, we re-enable the thread swapping and time stepping, before returning
		// a void value.
		
		ctxt.threadState.setAtomic(true);
		List<ValueListenerList> listenerLists = new Vector<ValueListenerList>(size);

		for (int i = 0; i < size; i++)
		{
			UpdatableValue target = (UpdatableValue) targets.get(i);
			listenerLists.add(target.listeners);
			target.listeners = null;
			target.set(node.getLocation(), values.get(i), ctxt);	// No invariant listeners
			target.listeners = listenerLists.get(i);
		}
		
		for (int i = 0; i < size; i++)
		{
			ValueListenerList listeners = listenerLists.get(i);
			
			if (listeners != null)
			{
				listeners.changedValue(node.getLocation(), values.get(i), ctxt);
			}
		}

		ctxt.threadState.setAtomic(false);

		return new VoidValue();
	}
	
	@Override
	public Value caseACallObjectStm(ACallObjectStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		node.getField().getLocation().hit();

		// The check above increments the hit counter for the call, but so
		// do the evaluations of the designator below, so we correct the
		// hit count here...

		node.getLocation().setHits(node.getLocation().getHits()-1);
		//node.getLocation().hits--;

		try
		{
			ObjectValue obj = node.getDesignator().apply(VdmRuntime.getStatementEvaluator(),ctxt).objectValue(ctxt);
			Value v = obj.get(node.getField(), node.getExplicit());

			if (v == null)
			{
    			VdmRuntimeError.abort(node.getField().getLocation(),4035, "Object has no field: " + node.getField().getName(), ctxt);
			}

			v = v.deref();

			if (v instanceof OperationValue)
			{
    			OperationValue op = v.operationValue(ctxt);
    			ValueList argValues = new ValueList();

    			for (PExp arg: node.getArgs())
    			{
    				argValues.add(arg.apply(VdmRuntime.getStatementEvaluator(),ctxt));
    			}

    			return op.eval(node.getLocation(), argValues, ctxt);
			}
			else
			{
    			FunctionValue op = v.functionValue(ctxt);
    			ValueList argValues = new ValueList();

    			for (PExp arg: node.getArgs())
    			{
    				argValues.add(arg.apply(VdmRuntime.getStatementEvaluator(),ctxt));
    			}

    			return op.eval(node.getLocation(), argValues, ctxt);
			}
		}
		catch (ValueException e)
		{
			return VdmRuntimeError.abort(node.getLocation(),e);
		}
	}
	
	@Override
	public Value caseACallStm(ACallStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			Value v = ctxt.lookup(node.getName()).deref();

			if (v instanceof OperationValue)
			{
    			OperationValue op = v.operationValue(ctxt);
    			ValueList argValues = new ValueList();

    			for (PExp arg: node.getArgs())
    			{
    				argValues.add(arg.apply(VdmRuntime.getStatementEvaluator(),ctxt));
    			}

    			return op.eval(node.getLocation(), argValues, ctxt);
			}
			else
			{
    			FunctionValue fn = v.functionValue(ctxt);
    			ValueList argValues = new ValueList();

    			for (PExp arg: node.getArgs())
    			{
    				argValues.add(arg.apply(VdmRuntime.getStatementEvaluator(),ctxt));
    			}

    			return fn.eval(node.getLocation(), argValues, ctxt);
			}
		}
		catch (ValueException e)
		{
			return VdmRuntimeError.abort(node.getLocation(),e);
		}
	}
	
	@Override
	public Value caseACasesStm(ACasesStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		Value val = node.getExp().apply(VdmRuntime.getStatementEvaluator(),ctxt);

		for (ACaseAlternativeStm c: node.getCases())
		{
			Value rv = ACaseAlternativeStmAssistantInterpreter.eval(c,val, ctxt);
			if (rv != null) return rv;
		}

		if (node.getOthers() != null)
		{
			return node.getOthers().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}

		return new VoidValue();
	}
	
	
	@Override
	public Value caseAClassInvariantStm(AClassInvariantStm node,
			Context ctxt) throws AnalysisException
	{
		for (PDefinition d: node.getInvDefs())
		{
			AClassInvariantDefinition invdef = (AClassInvariantDefinition)d;

			try
			{
				if (!invdef.getExpression().apply(VdmRuntime.getStatementEvaluator(),ctxt).boolValue(ctxt))
				{
					return new BooleanValue(false);
				}
			}
			catch (ValueException e)
			{
				VdmRuntimeError.abort(node.getLocation(),e);
			}
		}

		return new BooleanValue(true);
	}
	
	@Override
	public Value caseACyclesStm(ACyclesStm node, Context ctxt)
			throws AnalysisException
	{
		node.getLocation().hit();
		node.getCycles().getLocation().hit();

		ISchedulableThread me = BasicSchedulableThread.getThread(Thread.currentThread());

		if (me.inOuterTimestep())
		{
			// Already in a timed step, so ignore nesting
			return node.getStatement().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
		else
		{
			me.inOuterTimestep(true);
			Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),ctxt);
			me.inOuterTimestep(false);
			me.duration(ctxt.threadState.CPU.getDuration(node.getValue()), ctxt, node.getLocation());
			return rv;
		}
	}
	
	@Override
	public Value caseADurationStm(ADurationStm node, Context ctxt)
			throws AnalysisException
	{
		node.getLocation().hit();
		node.getDuration().getLocation().hit();

		ISchedulableThread me = BasicSchedulableThread.getThread(Thread.currentThread());

		if (me.inOuterTimestep())
		{
			// Already in a timed step, so ignore nesting
			return node.getStatement().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
		else
		{
			// We disable the swapping and time (RT) as duration evaluation should be "free".
		    ctxt.threadState.setAtomic(true);
		    long step = node.getDuration().apply(VdmRuntime.getStatementEvaluator(),ctxt).intValue(ctxt);
		    ctxt.threadState.setAtomic(false);

		    me.inOuterTimestep(true);
		    Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		    me.inOuterTimestep(false);
		    me.duration(step, ctxt, node.getLocation());
		    return rv;
		}
	}
	
	@Override
	public Value caseAElseIfStm(AElseIfStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			return node.getElseIf().apply(VdmRuntime.getStatementEvaluator(),ctxt).boolValue(ctxt) ? node.getThenStm().apply(VdmRuntime.getStatementEvaluator(),ctxt) : null;
		}
        catch (ValueException e)
        {
        	return VdmRuntimeError.abort(node.getLocation(),e);
        }
	}
	
	@Override
	public Value caseAErrorStm(AErrorStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		return VdmRuntimeError.abort(node.getLocation(),4036, "ERROR statement reached", ctxt);
	}
	
	@Override
	public Value caseAExitStm(AExitStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		Value v = null;

		if (node.getExpression() != null)
		{
			v = node.getExpression().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
		else
		{
			v = new UndefinedValue();
		}

		throw new ExitException(v, node.getLocation(), ctxt);			// BANG!!
	}
	
	@Override
	public Value caseAForAllStm(AForAllStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			ValueSet values = node.getSet().apply(VdmRuntime.getStatementEvaluator(),ctxt).setValue(ctxt);

			for (Value val: values)
			{
				try
				{
					Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "for all", ctxt);
					evalContext.putList(PPatternAssistantInterpreter.getNamedValues(node.getPattern(),val, ctxt));
					Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);

					if (!rv.isVoid())
					{
						return rv;
					}
				}
				catch (PatternMatchException e)
				{
					// Ignore and try others
				}
			}
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		return new VoidValue();
	}
	
	@Override
	public Value caseAForIndexStm(AForIndexStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			long fval = node.getFrom().apply(VdmRuntime.getStatementEvaluator(),ctxt).intValue(ctxt);
			long tval = node.getTo().apply(VdmRuntime.getStatementEvaluator(),ctxt).intValue(ctxt);
			long bval = (node.getBy() == null) ? 1 : node.getBy().apply(VdmRuntime.getStatementEvaluator(),ctxt).intValue(ctxt);

			if (bval == 0)
			{
				VdmRuntimeError.abort(node.getLocation(),4038, "Loop, from " + fval + " to " + tval + " by " + bval +
						" will never terminate", ctxt);
			}

			for (long value = fval;
				 (bval > 0 && value <= tval) || (bval < 0 && value >= tval);
				 value += bval)
			{
				Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "for index", ctxt);
				evalContext.put(node.getVar(), new IntegerValue(value));
				Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);

				if (!rv.isVoid())
				{
					return rv;
				}
			}
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		return new VoidValue();
	}
	
	@Override
	public Value caseAForPatternBindStm(AForPatternBindStm node,
			Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			ValueList values = node.getExp().apply(VdmRuntime.getStatementEvaluator(),ctxt).seqValue(ctxt);

			if (node.getReverse())
			{
				ListIterator<Value> li = values.listIterator(values.size());
				ValueList backwards = new ValueList();

				while (li.hasPrevious())
				{
					backwards.add(li.previous());
				}

				values = backwards;
			}

			if (node.getPatternBind().getPattern() != null)
			{
				for (Value val: values)
				{
					try
					{
						Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "for pattern", ctxt);
						evalContext.putList(PPatternAssistantInterpreter.getNamedValues(node.getPatternBind().getPattern(),val, ctxt));
						Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);

						if (!rv.isVoid())
						{
							return rv;
						}
					}
					catch (PatternMatchException e)
					{
						// Ignore mismatches
					}
				}
			}
			else if (node.getPatternBind().getBind() instanceof ASetBind)
			{
				ASetBind setbind = (ASetBind)node.getPatternBind().getBind();
				ValueSet set = setbind.getSet().apply(VdmRuntime.getStatementEvaluator(),ctxt).setValue(ctxt);

				for (Value val: values)
				{
					try
					{
						if (!set.contains(val))
						{
							VdmRuntimeError.abort(node.getLocation(),4039, "Set bind does not contain value " + val, ctxt);
						}

						Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "for set bind", ctxt);
						evalContext.putList(PPatternAssistantInterpreter.getNamedValues(setbind.getPattern(),val, ctxt));
						Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);

						if (!rv.isVoid())
						{
							return rv;
						}
					}
					catch (PatternMatchException e)
					{
						// Ignore mismatches
					}
				}
			}
			else
			{
				ATypeBind typebind = (ATypeBind)node.getPatternBind().getBind();

				for (Value val: values)
				{
					try
					{
						Value converted = val.convertTo(typebind.getType(), ctxt);

						Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "for type bind", ctxt);
						evalContext.putList(PPatternAssistantInterpreter.getNamedValues(typebind.getPattern(),converted, ctxt));
						Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);

						if (!rv.isVoid())
						{
							return rv;
						}
					}
					catch (PatternMatchException e)
					{
						// Ignore mismatches
					}
				}
			}
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		return new VoidValue();
	}
	
	@Override
	public Value caseAIfStm(AIfStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
    		if (node.getIfExp().apply(VdmRuntime.getStatementEvaluator(),ctxt).boolValue(ctxt))
    		{
    			return node.getThenStm().apply(VdmRuntime.getStatementEvaluator(),ctxt);
    		}
    		else
    		{
    			for (AElseIfStm elseif: node.getElseIf())
    			{
    				Value r = elseif.apply(VdmRuntime.getStatementEvaluator(),ctxt);
    				if (r != null) return r;
    			}

    			if (node.getElseStm() != null)
    			{
    				return node.getElseStm().apply(VdmRuntime.getStatementEvaluator(),ctxt);
    			}

    			return new VoidValue();
    		}
        }
        catch (ValueException e)
        {
        	return VdmRuntimeError.abort(node.getLocation(),e);
        }
	}
	
	@Override
	public Value caseALetBeStStm(ALetBeStStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		try
		{
			QuantifierList quantifiers = new QuantifierList();

			for (PMultipleBind mb: node.getDef().getBindings())
			{
				ValueList bvals =PMultipleBindAssistantInterpreter.getBindValues( mb,ctxt);

				for (PPattern p: mb.getPlist())
				{
					Quantifier q = new Quantifier(p, bvals);
					quantifiers.add(q);
				}
			}

			quantifiers.init(ctxt, true);

			while (quantifiers.hasNext())
			{
				Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "let be st statement", ctxt);
				NameValuePairList nvpl = quantifiers.next();
				boolean matches = true;

				for (NameValuePair nvp: nvpl)
				{
					Value v = evalContext.get(nvp.name);

					if (v == null)
					{
						evalContext.put(nvp.name, nvp.value);
					}
					else
					{
						if (!v.equals(nvp.value))
						{
							matches = false;
							break;	// This quantifier set does not match
						}
					}
				}

				if (matches &&
					(node.getSuchThat() == null || node.getSuchThat().apply(VdmRuntime.getStatementEvaluator(),evalContext).boolValue(ctxt)))
				{
					return node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);
				}
			}
		}
        catch (ValueException e)
        {
        	VdmRuntimeError.abort(node.getLocation(),e);
        }

		return VdmRuntimeError.abort(node.getLocation(),4040, "Let be st found no applicable bindings", ctxt);
	}
	
	@Override
	public Value caseADefLetDefStm(ADefLetDefStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "let statement", ctxt);

		LexNameToken sname = new LexNameToken(node.getLocation().getModule(), "self", node.getLocation());
		ObjectValue self = (ObjectValue)ctxt.check(sname);

		for (PDefinition d: node.getLocalDefs())
		{
			NameValuePairList values =PDefinitionAssistantInterpreter.getNamedValues(d,evalContext);

			if (self != null && d instanceof AExplicitFunctionDefinition)
			{
				for (NameValuePair nvp: values)
				{
					if (nvp.value instanceof FunctionValue)
					{
						FunctionValue fv = (FunctionValue)nvp.value;
						fv.setSelf(self);
					}
				}
			}

			evalContext.putList(values);
		}

		return node.getStatement().apply(VdmRuntime.getStatementEvaluator(),evalContext);
	}
	
	@Override
	public Value caseANotYetSpecifiedStm(ANotYetSpecifiedStm node,
			Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		return VdmRuntimeError.abort(node.getLocation(),4041, "'is not yet specified' statement reached", ctxt);
	}
	
	@Override
	public Value caseAReturnStm(AReturnStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		if (node.getExpression() == null)
		{
			return new VoidReturnValue();
		}
		else
		{
			return node.getExpression().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
	}
	
	@Override
	public Value caseABlockSimpleBlockStm(ABlockSimpleBlockStm node,
			Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "block statement", ctxt);

		for (PDefinition d: node.getAssignmentDefs())
		{
			evalContext.putList(PDefinitionAssistantInterpreter.getNamedValues(d,evalContext));
		}

		return SSimpleBlockStmAssistantInterpreter.evalBlock(node,evalContext);
	}
	
	@Override
	public Value caseANonDeterministicSimpleBlockStm(
			ANonDeterministicSimpleBlockStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		return SSimpleBlockStmAssistantInterpreter.evalBlock(node,ctxt);
	}
	
	@Override
	public Value caseASkipStm(ASkipStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		return new VoidValue();
	}
	
	@Override
	public Value caseASpecificationStm(ASpecificationStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		return VdmRuntimeError.abort(node.getLocation(),4047, "Cannot execute specification statement", ctxt);
	}
	
	
	@Override
	public Value caseAStartStm(AStartStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			Value value = node.getObj().apply(VdmRuntime.getStatementEvaluator(),ctxt);

			if (value.isType(SetValue.class))
			{
				ValueSet set = value.setValue(ctxt);

				for (Value v: set)
				{
					ObjectValue target = v.objectValue(ctxt);
					OperationValue op = target.getThreadOperation(ctxt);

					AStartStmAssistantInterpreter.start(node,target, op, ctxt);
				}
			}
			else
			{
				ObjectValue target = value.objectValue(ctxt);
				OperationValue op = target.getThreadOperation(ctxt);

				AStartStmAssistantInterpreter.start(node,target, op, ctxt);
			}

			return new VoidValue();
		}
		catch (ValueException e)
		{
			return VdmRuntimeError.abort(node.getLocation(),e);
		}
	}
	
	@Override
	public Value caseASubclassResponsibilityStm(
			ASubclassResponsibilityStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		return VdmRuntimeError.abort(node.getLocation(),4048, "'is subclass responsibility' statement reached", ctxt);
	}
	
	@Override
	public Value caseATixeStm(ATixeStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		Value rv = null;

		try
		{
			rv = node.getBody().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
		catch (ExitException original)
		{
			ExitException last = original;

			while (true)
			{
				Value exval = last.value;

				try
    			{
    				for (ATixeStmtAlternative tsa: node.getTraps())
    				{
    					rv = ATixeStmtAlternativeAssistantInterpreter.eval(tsa,node.getLocation(), exval, ctxt);

    					if (rv != null)  // Statement was executed
    					{
    						return rv;
    					}
    				}
    			}
    			catch (ExitException ex)
    			{
    				last = ex;
    				continue;
    			}

				throw last;
			}
		}

		return rv;
	}
	
	@Override
	public Value caseATrapStm(ATrapStm node, Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);
		Value rv = null;

		try
		{
			rv = node.getBody().apply(VdmRuntime.getStatementEvaluator(),ctxt);
		}
		catch (ExitException e)
		{
			Value exval = e.value;

			try
			{
    			if (node.getPatternBind().getPattern() != null)
    			{
    				Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "trap pattern", ctxt);
    				evalContext.putList(PPatternAssistantInterpreter.getNamedValues(node.getPatternBind().getPattern(),exval, ctxt));
    				rv = node.getWith().apply(VdmRuntime.getStatementEvaluator(),evalContext);
    			}
    			else if (node.getPatternBind().getBind() instanceof ASetBind)
    			{
    				ASetBind setbind = (ASetBind)node.getPatternBind().getBind();
    				ValueSet set = setbind.getSet().apply(VdmRuntime.getStatementEvaluator(),ctxt).setValue(ctxt);

    				if (set.contains(exval))
    				{
    					Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "trap set", ctxt);
    					evalContext.putList(PPatternAssistantInterpreter.getNamedValues(setbind.getPattern(),exval, ctxt));
    					rv = node.getWith().apply(VdmRuntime.getStatementEvaluator(),evalContext);
    				}
    				else
    				{
    					VdmRuntimeError.abort(node.getLocation(),4050, "Value " + exval + " is not in set bind", ctxt);
    				}
    			}
    			else
    			{
    				ATypeBind typebind = (ATypeBind)node.getPatternBind().getBind();
    				Value converted = exval.convertTo(typebind.getType(), ctxt);
    				Context evalContext = new Context(ctxt.assistantFactory,node.getLocation(), "trap type", ctxt);
    				evalContext.putList(PPatternAssistantInterpreter.getNamedValues(typebind.getPattern(),converted, ctxt));
    				rv = node.getWith().apply(VdmRuntime.getStatementEvaluator(),evalContext);
    			}
			}
			catch (ValueException ve)
			{
				VdmRuntimeError.abort(node.getLocation(),ve);
			}
			catch (PatternMatchException pe)
			{
				throw e;
			}
		}

		return rv;
	}
	
	@Override
	public Value caseAWhileStm(AWhileStm node, Context ctxt)
			throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		try
		{
			while (node.getExp().apply(VdmRuntime.getStatementEvaluator(),ctxt).boolValue(ctxt))
			{
				Value rv = node.getStatement().apply(VdmRuntime.getStatementEvaluator(),ctxt);

				if (!rv.isVoid())
				{
					return rv;
				}
			}
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		return new VoidValue();
	}
	
	@Override
	public Value caseAPeriodicStm(APeriodicStm node, Context ctxt)
			throws AnalysisException
	{
		final int PERIODIC = 0;
		final int JITTER = 1;
		final int DELAY = 2;
		final int OFFSET = 3;
		
		node.setPeriod(0L);
		node.setJitter(0L);
		node.setDelay(0L);
		node.setOffset(0L);
		
		
		for(int i = 0; i < node.getArgs().size(); i++)
		{
			PExp arg = node.getArgs().get(i);
			long value = -1;
			
			try{
				
				arg.getLocation().hit();
				value = arg.apply(VdmRuntime.getExpressionEvaluator(), ctxt).intValue(ctxt);
				
				if(value < 0)
				{
					VdmRuntimeError.abort(node.getLocation(), 4157,  "Expecting +ive integer in periodic argument " + (i+1) + ", was " + value, ctxt);
				}
			
				if(i == PERIODIC)
					node.setPeriod(value);
				else if (i == JITTER)
					node.setJitter(value);
				else if(i == DELAY)
					node.setDelay(value);
				else if(i == OFFSET)
					node.setOffset(value);
			}
			catch(ValueException e)
			{
				VdmRuntimeError.abort(node.getLocation(), 4157, "Expecting +ive integer in periodic argument " + (i+1) + ", was " + value, ctxt);
			}
		}
		
		if (node.getPeriod() == 0)
		{
			VdmRuntimeError.abort(node.getLocation(), 4158, "Period argument must be non-zero, was " + node.getPeriod(), ctxt);
		}

		if (node.getArgs().size() == 4)
		{
			if (node.getDelay() >= node.getPeriod())
			{
				VdmRuntimeError.abort(node.getLocation(), 4159, "Delay argument (" + node.getDelay() + ") must be less than the period (" + node.getPeriod() + ")", ctxt);
			}
		}
		
		
		return null;	// Not actually used - see StartStatement
	}
	
	
	
	@Override
	public Value caseAIdentifierStateDesignator(
			AIdentifierStateDesignator node, Context ctxt) throws AnalysisException
	{
		// We lookup the name in a context comprising only state...
				// return ctxt.getUpdateable().lookup(name.getExplicit(true));
				return ctxt.lookup(node.getName().getExplicit(true));
	}
	
	@Override
	public Value caseAFieldStateDesignator(AFieldStateDesignator node,
			Context ctxt) throws AnalysisException
	{
		Value result = null;

		try
		{
			result = node.getObject().apply(VdmRuntime.getStatementEvaluator(),ctxt).deref();

			if (result instanceof ObjectValue && node.getObjectfield() != null)
			{
    			ObjectValue ov = result.objectValue(ctxt);
    			Value rv = ov.get(node.getObjectfield(), false);

    			if (rv == null)
    			{
    				VdmRuntimeError.abort(node.getLocation(),4045, "Object does not contain value for field: " + node.getField(), ctxt);
    			}

    			return rv;
			}
			else if (result instanceof RecordValue)
			{
    			RecordValue rec = result.recordValue(ctxt);
    			result = rec.fieldmap.get(node.getField().getName());

    			if (result == null)
    			{
    				VdmRuntimeError.abort(node.getField().getLocation(),4037, "No such field: " + node.getField(), ctxt);
    			}

    			return result;
			}
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		return result;
	}
	
	@Override
	public Value caseAFieldObjectDesignator(AFieldObjectDesignator node,
			Context ctxt) throws AnalysisException
	{
		try
		{
			Value val = node.getObject().apply(VdmRuntime.getStatementEvaluator(),ctxt).deref();

			if (val instanceof ObjectValue && node.getField() != null)
			{
    			ObjectValue ov = val.objectValue(ctxt);
    			Value rv = ov.get(node.getField(), (node.getClassName() != null));

    			if (rv == null)
    			{
    				VdmRuntimeError.abort(node.getLocation(),4045, "Object does not contain value for field: " + node.getField(), ctxt);
    			}

    			return rv;
			}
			else if (val instanceof RecordValue)
			{
				RecordValue rec = val.recordValue(ctxt);
				Value result = rec.fieldmap.get(node.getFieldName().getName());

				if (result == null)
				{
					VdmRuntimeError.abort(node.getLocation(),4046, "No such field: " + node.getFieldName(), ctxt);
				}

				return result;
			}
			else
			{
				return VdmRuntimeError.abort(node.getLocation(),4020,
					"State value is neither a record nor an object", ctxt);
			}
		}
		catch (ValueException e)
		{
			return VdmRuntimeError.abort(node.getLocation(),e);
		}
	}
	
	@Override
	public Value caseAMapSeqStateDesignator(AMapSeqStateDesignator node,
			Context ctxt) throws AnalysisException
	{
		Value result = null;

		try
		{
			Value root = node.getMapseq().apply(VdmRuntime.getStatementEvaluator(),ctxt);
			Value index = node.getExp().apply(VdmRuntime.getExpressionEvaluator(), ctxt);

			if (root.isType(MapValue.class))
			{
				index = index.convertTo(node.getMapType().getFrom(), ctxt);
				ValueMap map = root.mapValue(ctxt);
				result = map.get(index);

				if (result == null && root instanceof UpdatableValue)
				{
					// Assignment to a non-existent map key creates the value
					// in order to have it updated.

					UpdatableValue ur = (UpdatableValue)root;
					result = UpdatableValue.factory(ur.listeners);
					map.put(index, result);
				}
			}
			else if (root.isType(SeqValue.class))
			{
				ValueList seq = root.seqValue(ctxt);
				int i = (int)index.intValue(ctxt)-1;

				if (!seq.inbounds(i))
				{
					if (i == seq.size())
					{
						// Assignment to an index one greater than the length
						// creates the value in order to have it updated.

						UpdatableValue ur = (UpdatableValue)root;
						seq.add(UpdatableValue.factory(ur.listeners));
					}
					else
					{
						VdmRuntimeError.abort(node.getExp().getLocation(),4019, "Sequence cannot extend to key: " + index, ctxt);
					}
				}

				result = seq.get(i);
			}
			else
			{
				VdmRuntimeError.abort(node.getLocation(),4020, "State value is neither a sequence nor a map", ctxt);
			}
		}
		catch (ValueException e)
		{
			VdmRuntimeError.abort(node.getLocation(),e);
		}

		return result;
	}
	
	@Override
	public Value caseAApplyObjectDesignator(AApplyObjectDesignator node,
			Context ctxt) throws AnalysisException
	{
		try
		{
			Value uv = node.getObject().apply(VdmRuntime.getStatementEvaluator(), ctxt);
			Value v = uv.deref();

			if (v instanceof MapValue)
			{
				ValueMap mv = v.mapValue(ctxt);
				Value a = node.getArgs().get(0).apply(VdmRuntime.getExpressionEvaluator(),ctxt);
				Value rv = mv.get(a);

				if (rv == null && uv instanceof UpdatableValue)
				{
					// Not already in map - get listener from root object
					UpdatableValue ur = (UpdatableValue)uv;
					rv = UpdatableValue.factory(ur.listeners);
					mv.put(a, rv);
				}

				return rv;
			}
			else if (v instanceof SeqValue)
			{
				ValueList seq = v.seqValue(ctxt);
				Value a = node.getArgs().get(0).apply(VdmRuntime.getExpressionEvaluator(),ctxt);
				int i = (int)a.intValue(ctxt)-1;

				if (!seq.inbounds(i))
				{
					VdmRuntimeError.abort(node.getLocation(),4042, "Sequence does not contain key: " + a, ctxt);
				}

				return seq.get(i);
			}
			else if (v instanceof FunctionValue)
			{
				ValueList argvals = new ValueList();

				for (PExp arg: node.getArgs())
				{
					argvals.add(arg.apply(VdmRuntime.getExpressionEvaluator(),ctxt));
				}

				FunctionValue fv = v.functionValue(ctxt);
				return fv.eval(node.getLocation(), argvals, ctxt);
			}
			else if (v instanceof OperationValue)
			{
				ValueList argvals = new ValueList();

				for (PExp arg: node.getArgs())
				{
					argvals.add(arg.apply(VdmRuntime.getExpressionEvaluator(),ctxt));
				}

				OperationValue ov = v.operationValue(ctxt);
				return ov.eval(node.getLocation(), argvals, ctxt);
			}
			else
			{
				return VdmRuntimeError.abort(node.getLocation(),4043,
					"Object designator is not a map, sequence, operation or function", ctxt);
			}
		}
		catch (ValueException e)
		{
			return VdmRuntimeError.abort(node.getLocation(),e);
		}
	}
	
	@Override
	public Value caseAIdentifierObjectDesignator(
			AIdentifierObjectDesignator node, Context ctxt)
			throws AnalysisException
	{
		return node.getExpression().apply(VdmRuntime.getExpressionEvaluator(),ctxt);
	}
	
	@Override
	public Value caseANewObjectDesignator(ANewObjectDesignator node,
			Context ctxt) throws AnalysisException
	{
		return node.getExpression().apply(VdmRuntime.getExpressionEvaluator(),ctxt);
	}
	
	@Override
	public Value caseASelfObjectDesignator(ASelfObjectDesignator node,
			Context ctxt) throws AnalysisException
	{
		return  ctxt.lookup(node.getSelf());
	}
}
