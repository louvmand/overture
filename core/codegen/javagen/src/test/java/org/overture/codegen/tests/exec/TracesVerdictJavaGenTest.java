package org.overture.codegen.tests.exec;

import java.io.File;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.overture.ast.lex.Dialect;
import org.overture.codegen.execution.tests.CommonJavaGenCheckerTest;
import org.overture.codegen.tests.TracesVerdictTest;
import org.overture.config.Release;

@RunWith(value = Parameterized.class)
public class TracesVerdictJavaGenTest extends CommonJavaGenCheckerTest
{

	public TracesVerdictJavaGenTest(String name,File vdmSpec, File javaGeneratedFiles,
			TestHandler testHandler, boolean printInput, String rootPackage)
	{
		super(vdmSpec, javaGeneratedFiles, testHandler, printInput, rootPackage);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> getData()
	{
		return collectTests(new File(TracesVerdictTest.ROOT),new TraceHandler(Release.VDM_10, Dialect.VDM_PP));
	}

	@Override
	protected String getPropertyId()
	{
		return "tracesverdict";
	}

}
