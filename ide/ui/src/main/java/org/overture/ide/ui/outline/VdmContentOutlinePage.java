package org.overture.ide.ui.outline;

import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.overture.ide.core.ElementChangedEvent;
import org.overture.ide.core.IElementChangedListener;
import org.overture.ide.core.IVdmElement;
import org.overture.ide.core.IVdmElementDelta;
import org.overture.ide.core.VdmCore;
import org.overture.ide.core.resources.IVdmSourceUnit;
import org.overture.ide.ui.editor.core.VdmEditor;
import org.overture.ide.ui.internal.viewsupport.DecorationgVdmLabelProvider;
import org.overture.ide.ui.internal.viewsupport.VdmUILabelProvider;
import org.overturetool.vdmj.ast.IAstNode;
import org.overturetool.vdmj.definitions.Definition;
import org.overturetool.vdmj.types.Type;

@SuppressWarnings("deprecation")
public class VdmContentOutlinePage extends ContentOutlinePage implements
		IContentOutlinePage
{
	/**
	 * The element change listener of the java outline viewer.
	 * 
	 * @see IElementChangedListener
	 */
	protected class ElementChangedListener implements IElementChangedListener
	{

		public void elementChanged(final ElementChangedEvent e)
		{

			if (getControl() == null || getControl().isDisposed())
				return;

			Display d = getControl().getDisplay();
			if (d != null)
			{
				d.asyncExec(new Runnable()
				{
					public void run()
					{
						if (fOutlineViewer != null
								&& !fOutlineViewer.getControl().isDisposed())
						{
							fOutlineViewer.refresh(true);
							fOutlineViewer.expandToLevel(AUTO_EXPAND_LEVEL);
						}
					}
				});
			}
		}

		private boolean isPossibleStructuralChange(IVdmElementDelta cuDelta)
		{
			if (cuDelta.getKind() != IVdmElementDelta.CHANGED)
			{
				return true; // add or remove
			}
			// int flags= cuDelta.getFlags();
			// if ((flags & IVdmElementDelta.F_CHILDREN) != 0) {
			// return true;
			// }
			// return (flags & (IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_FINE_GRAINED)) ==
			// IJavaElementDelta.F_CONTENT;
			return true;
		}

		protected IVdmElementDelta findElement(IVdmSourceUnit unit,
				IVdmElementDelta delta)
		{

			if (delta == null || unit == null)
				return null;

			if (delta.getElement() instanceof IVdmSourceUnit)
			{
				IVdmSourceUnit element = (IVdmSourceUnit) delta.getElement();

				if (unit.equals(element))
				{
					if (isPossibleStructuralChange(delta))
					{
						return delta;
					}
					return null;
				}
			}

			// if (element.getElementType() > IVdmElement.CLASS_FILE)
			// return null;
			//
			// IJavaElementDelta[] children= delta.getAffectedChildren();
			// if (children == null || children.length == 0)
			// return null;
			//
			// for (int i= 0; i < children.length; i++) {
			// IJavaElementDelta d= findElement(unit, children[i]);
			// if (d != null)
			// return d;
			// }

			return null;
		}
	}

	public class VdmOutlineViewer extends TreeViewer
	{
		private class OutlineSorter extends ViewerSorter
		{
			private final static int TYPES = 1;
			private final static int VALUES = 0;

			// private final static int INSTANCEVARIABLES = 2;
			// private final static int OPERATIONS = 3;
			// private final static int FUNCTIONS = 4;
			// private final static int THREADS = 5;
			// private final static int SYN = 6;
			// private final static int TRACES = 7;

			@Override
			public int category(Object element)
			{
				if (element instanceof Type)
				{
					return TYPES;
				} else if (element instanceof Definition)
				{
					return VALUES;
				} else
					return super.category(element);
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				int cat1 = category(e1);
				int cat2 = category(e2);
				if (cat1 != cat2)
				{
					return cat1 - cat2;
				}
				
				if(e1 instanceof IAstNode && e2 instanceof IAstNode)
				{
					return collator.compare(((IAstNode)e1).getName(), ((IAstNode)e2).getName());
				}else
				return super.compare(viewer, e1, e2);
			}
		}

		public VdmOutlineViewer(Composite parent)
		{
			super(parent);
			setAutoExpandLevel(2);
			setUseHashlookup(true);

			setSorter(new OutlineSorter());
		}

		@Override
		protected void fireSelectionChanged(SelectionChangedEvent event)
		{
			if (!inExternalSelectionMode)
			{
				super.fireSelectionChanged(event);
			}
		}

		public void dispose()
		{

			Tree t = getTree();
			if (t != null && !t.isDisposed())
			{
				if (t.getChildren() != null)
				{
					for (Control c : t.getChildren())
					{
						c.dispose();
					}
				}
				getLabelProvider().dispose();
				t.dispose();
			}

		}

	}

	/**
	 * Constant indicating that all levels of the tree should be expanded or collapsed.
	 * 
	 * @see #expandToLevel(int)
	 * @see #collapseToLevel(Object, int)
	 */
	public static final int ALL_LEVELS = -1;

	private VdmEditor vdmEditor;

	private VdmOutlineViewer fOutlineViewer;
	private IVdmElement fInput;
	private ElementChangedListener fListener;

	private ListenerList fSelectionChangedListeners = new ListenerList(ListenerList.IDENTITY);

	private boolean inExternalSelectionMode = false;

	private static final int AUTO_EXPAND_LEVEL = 2;

	private MemberFilterActionGroup fMemberFilterActionGroup;

	private VdmUILabelProvider uiLabelProvider;

	public VdmContentOutlinePage(VdmEditor vdmEditor)
	{
		this.vdmEditor = vdmEditor;
	}

	@Override
	public void createControl(Composite parent)
	{

		fOutlineViewer = new VdmOutlineViewer(parent);
		fOutlineViewer.setAutoExpandLevel(AUTO_EXPAND_LEVEL);
		fOutlineViewer.setContentProvider(new VdmOutlineTreeContentProvider());
		// fOutlineViewer.setLabelProvider(new VdmOutlineLabelProvider());
		uiLabelProvider = new VdmUILabelProvider();
		fOutlineViewer.setLabelProvider(new DecorationgVdmLabelProvider(uiLabelProvider));
		fOutlineViewer.addSelectionChangedListener(this);

		Object[] listeners = fSelectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; i++)
		{
			fSelectionChangedListeners.remove(listeners[i]);
			fOutlineViewer.addSelectionChangedListener((ISelectionChangedListener) listeners[i]);
		}

		// addSelectionChangedListener(new VdmSelectionListener());

		registerToolBarActions();

		fOutlineViewer.setInput(fInput);
	}

	private void registerToolBarActions()
	{

		IPageSite site = getSite();
		IActionBars actionBars = site.getActionBars();

		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		// toolBarManager.add(new LexicalSortingAction());

		fMemberFilterActionGroup = new MemberFilterActionGroup(fOutlineViewer, "org.overture.ide.ui.VdmOutlinePage"); //$NON-NLS-1$
		fMemberFilterActionGroup.contributeToToolBar(toolBarManager);

		// fCustomFiltersActionGroup.fillActionBars(actionBars);
		//
		// IMenuManager viewMenuManager= actionBars.getMenuManager();
		//			viewMenuManager.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
		//
		// fToggleLinkingAction= new ToggleLinkingAction();
		// fToggleLinkingAction.setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
		// viewMenuManager.add(new ClassOnlyAction());
		// viewMenuManager.add(fToggleLinkingAction);
		//
		//			fCategoryFilterActionGroup= new CategoryFilterActionGroup(fOutlineViewer, "org.eclipse.jdt.ui.JavaOutlinePage", new IJavaElement[] {fInput}); //$NON-NLS-1$
		// fCategoryFilterActionGroup.contributeToViewMenu(viewMenuManager);

	}

	@Override
	public void dispose()
	{
		if (vdmEditor != null)
		{

			vdmEditor.outlinePageClosed();
			vdmEditor = null;
		}
		// fListener.clear();
		// fListener = null;

		// fPostSelectionChangedListeners.clear();
		// fPostSelectionChangedListeners= null;

		// if (fPropertyChangeListener != null) {
		// JavaPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		// fPropertyChangeListener= null;
		// }

		// if (fMenu != null && !fMenu.isDisposed()) {
		// fMenu.dispose();
		// fMenu= null;
		// }

		// if (fActionGroups != null)
		// fActionGroups.dispose();

		// fTogglePresentation.setEditor(null);

		if (fOutlineViewer != null)
		{
			fOutlineViewer.dispose();
		}

		if (fMemberFilterActionGroup != null)
		{
			fMemberFilterActionGroup.dispose();
		}

		if (uiLabelProvider != null)
		{
			uiLabelProvider.dispose();
		}

		fOutlineViewer = null;

		super.dispose();

	}

	@Override
	public Control getControl()
	{
		if (fOutlineViewer != null)
			return fOutlineViewer.getControl();
		return null;
	}

	@Override
	public void setActionBars(IActionBars actionBars)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		if (fOutlineViewer != null)
			fOutlineViewer.addSelectionChangedListener(listener);
		else
			fSelectionChangedListeners.add(listener);
	}

	/*
	 * @see ISelectionProvider#setSelection(ISelection)
	 */
	@Override
	public void setSelection(ISelection selection)
	{
		if (fOutlineViewer != null)
			fOutlineViewer.setSelection(selection);
	}

	/*
	 * @see ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection()
	{
		if (fOutlineViewer == null)
			return StructuredSelection.EMPTY;
		return fOutlineViewer.getSelection();
	}

	/*
	 * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener)
	{
		if (fOutlineViewer != null)
			fOutlineViewer.removeSelectionChangedListener(listener);
		else
			fSelectionChangedListeners.remove(listener);
	}

	public void setInput(IVdmElement je)
	{
		this.fInput = je;
		if (fOutlineViewer != null)
		{
			fOutlineViewer.setInput(fInput);
			fOutlineViewer.expandToLevel(AUTO_EXPAND_LEVEL);

		}
		if (fListener != null)
			VdmCore.removeElementChangedListener(fListener);
		fListener = new ElementChangedListener();
		VdmCore.addElementChangedListener(fListener);
	}

	public void select(IAstNode reference)
	{

		if (fOutlineViewer != null)
		{

			inExternalSelectionMode = true;
			ISelection s = fOutlineViewer.getSelection();
			if (s instanceof IStructuredSelection)
			{
				IStructuredSelection ss = (IStructuredSelection) s;
				@SuppressWarnings("rawtypes")
				List elements = ss.toList();
				if (!elements.contains(reference))
				{
					s = (reference == null ? StructuredSelection.EMPTY
							: new StructuredSelection(reference));
					fOutlineViewer.setSelection(s, true);
				}
			}
			inExternalSelectionMode = false;
		}
	}

}
