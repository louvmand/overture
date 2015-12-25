package org.overture.codegen.rt2rmi;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AClassClassDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.lex.Dialect;
import org.overture.ast.util.Utils;
import org.overture.codegen.cgast.declarations.ADefaultClassDeclCG;
import org.overture.codegen.cgast.declarations.ARemoteContractDeclCG;
import org.overture.codegen.cgast.declarations.ARemoteContractImplDeclCG;
import org.overture.codegen.ir.IRInfo;
import org.overture.codegen.logging.Logger;
import org.overture.codegen.merging.MergeVisitor;
import org.overture.codegen.rt2rmi.systemanalysis.DistributionMapping;
import org.overture.codegen.utils.GeneralCodeGenUtils;
import org.overture.codegen.utils.GeneralUtils;
import org.overture.codegen.utils.GeneratedData;
import org.overture.codegen.utils.GeneratedModule;
import org.overture.codegen.vdm2java.JavaCodeGenMain;
import org.overture.codegen.vdm2java.JavaCodeGenUtil;
import org.overture.codegen.vdm2java.JavaFormat;
import org.overture.codegen.vdm2java.JavaVarPrefixManager;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

public class RmiGeneratorCommandLine
{
	private static final String FOLDER_ARG = "-folder";
	private static final String PRINT_ARG = "-print";
	public static final String OUTPUT_ARG = "-output";

	public static void main(String[] args) throws org.overture.codegen.cgast.analysis.AnalysisException, IOException
	{
		if (args == null || args.length < 1)
		{
			usage("Expected one or more arguments");
			return;
		}

		Settings.release = Release.VDM_10;
		Settings.dialect = Dialect.VDM_RT;
		
		List<String> listArgs = Arrays.asList(args);
		List<File> files = new LinkedList<File>();
		File outputDir = null;
		boolean print = false;
		
		for (Iterator<String> i = listArgs.iterator(); i.hasNext();)
		{
			String arg = i.next();
			
			if (arg.equals(OUTPUT_ARG))
			{
				if (i.hasNext())
				{
					outputDir = new File(i.next());
					outputDir.mkdirs();

					if (!outputDir.isDirectory())
					{
						usage(outputDir + " is not a directory");
					}

				} else
				{
					usage(OUTPUT_ARG + " requires a directory");
				}
			}
			else if (arg.equals(PRINT_ARG))
			{
				print = true;
			} 
			else if (arg.equals(FOLDER_ARG))
			{
				if (i.hasNext())
				{
					File path = new File(i.next());

					if (path.isDirectory())
					{
						files.addAll(filterFiles(GeneralUtils.getFiles(path)));
					} else
					{
						usage("Could not find path: " + path);
					}
				} else
				{
					usage(FOLDER_ARG + " requires a directory");
				}
			}
			else
			{
				// It's a file or a directory
				File file = new File(arg);

				if (file.isFile())
				{
					if (isRtFile(file))
					{
						files.add(file);
					}
				} else
				{
					usage("Not a file: " + file);
				}
			}
		}
		
		try
		{
			TypeCheckResult<List<SClassDefinition>> tcResult = TypeCheckerUtil.typeCheckRt(files);
			
			/**********Analyse System class**********/
			// Now the architecture of the VDM-RT model is analysed
			// in order to extract the Connection map, Distribution map,
			// number of deployed objects and number of CPUs
			
			DistributionMapping mapping = new DistributionMapping(tcResult.result);
			mapping.run();
			
			int deployedObjCounter = mapping.getDeployedObjCounter();
			
			System.out.println("Number of deployed objects in the system class is: " 
								+ deployedObjCounter);
			
			Set<AClassClassDefinition> deployedClasses = mapping
					.getDeployedClasses();

			Set<AVariableExp> deployedObjects = mapping.getDeployedObjects();
			
			/******************************************/
			if (!GeneralCodeGenUtils.hasErrors(tcResult))
			{
				String systemClassName = mapping.getSystemName();
				
				RmiGenerator rmiGen = new RmiGenerator(systemClassName);
				GeneratedData data = rmiGen.generate(tcResult.result);
				
				IRInfo info = new IRInfo();
				JavaFormat javaFormat = rmiGen.getJavaGen().getJavaFormat();
				
				List<ADefaultClassDeclCG> irClasses = Util.getClasses(data.getClasses());
				
				//**********************************************************************//
				// Generate the remote contracts
				RemoteContractGenerator contractGenerator = new RemoteContractGenerator(
						irClasses);
				Set<ARemoteContractDeclCG> remoteContracts = contractGenerator
						.run();

				MergeVisitor printer = javaFormat.getMergeVisitor();


				System.out.println("**********************Remote contracts**********************");
				for (ARemoteContractDeclCG conract : remoteContracts) {
					StringWriter writer = new StringWriter();
					conract.apply(printer, writer);

					System.out.println(JavaCodeGenUtil.formatJavaCode(writer
							.toString()));
				}
				
				//**********************************************************************//
				// Generate the remote contract implementations
				RemoteImplGenerator implsGen = new RemoteImplGenerator(irClasses);
				List<ARemoteContractImplDeclCG> remoteImpls = implsGen.run();

				System.out.println("**********************Remote contracts implementation**********************");
				
				for (ARemoteContractImplDeclCG impl : remoteImpls) {
					StringWriter writer = new StringWriter();
					impl.apply(printer, writer);

					System.out.println(JavaCodeGenUtil.formatJavaCode(writer
							.toString()));
				}
				
				
				System.out.println("**********************CPU deployment**********************");

				Map<String, Set<AVariableExp>> CpuToDeployedObject = mapping.getCpuToDeployedObject();

				Map<String, Set<String>> cpuToConnectedCPUs = mapping.cpuToConnectedCPUs();
				//CPUdeploymentGenerator cpuDep = new CPUdeploymentGenerator(CpuToDeployedObject);

				Map<String, Set<AClassClassDefinition>> cpuToDeployedClasses = mapping.cpuToDeployedClasses();

				// Distributed the generate remote contracts and their implementation
				rmiGen.processData(remoteContracts, cpuToDeployedClasses, cpuToConnectedCPUs, remoteImpls);
				
				
				
				
				JavaCodeGenMain.processData(print, outputDir, rmiGen.getJavaGen(), data, false);
			} else
			{
				Logger.getLog().printErrorln("Could not parse/type check VDM model:\n"
						+ GeneralCodeGenUtils.errorStr(tcResult));
			}
		} catch (AnalysisException e)
		{
			Logger.getLog().println("Could not code generate model: " + e.getMessage());
		}
	}
	
	public static List<File> filterFiles(List<File> files)
	{
		List<File> filtered = new LinkedList<File>();
		
		for(File f : files)
		{
			if(isRtFile(f))
			{
				filtered.add(f);
			}
		}
		
		return filtered;
	}

	private static boolean isRtFile(File f)
	{
		return f.getName().endsWith(".vdmrt") || f.getName().endsWith(".vrt");
	}
	
	private static void usage(String msg)
	{
		Logger.getLog().printErrorln("VDM-RT to Java RMI generator: " + msg + "\n");
		Logger.getLog().printErrorln("Usage: rt2rmi [<options>] [<VDM-RT files>]");
		Logger.getLog().printErrorln(PRINT_ARG + ": print the generated code to the console");
		Logger.getLog().printErrorln(OUTPUT_ARG + " <folder path>: the output folder of the generated code");
		Logger.getLog().printErrorln(FOLDER_ARG + " <folder path>: a folder containing input .vdmrt files");

		System.exit(1);
	}
}
