package org.overture.ide.vdmpp.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class VdmPpUIPlugin extends AbstractUIPlugin {

	private static final boolean DEBUG = true;
	
	
	
	
	public static void println(String s)
	{
		if(DEBUG)
			System.out.println(s);
	}
	public static void printe(Exception e) {
		println(e.getStackTrace().toString());
		println(e.getMessage());
		
	}

}
