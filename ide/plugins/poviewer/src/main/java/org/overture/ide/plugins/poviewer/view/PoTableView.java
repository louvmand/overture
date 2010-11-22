package org.overture.ide.plugins.poviewer.view;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.overture.ide.core.resources.IVdmProject;
import org.overturetool.vdmj.pog.ProofObligation;

public class PoTableView extends ViewPart implements ISelectionListener
{
	private Text viewer;
	final Display display = Display.getCurrent();
	private Font font = null;

	/**
	 * The constructor.
	 */
	public PoTableView()
	{
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();

		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		font = fontRegistry.get(JFaceResources.TEXT_FONT);

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		viewer = new Text(parent, SWT.MULTI);
		viewer.setFont(font);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		viewer.setFocus();
	}

	public void setDataList(final IVdmProject project,
			final ProofObligation data)
	{
		display.asyncExec(new Runnable()
		{

			public void run()
			{

				viewer.setText(data.getValue());
			}

		});
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{

	}
}
