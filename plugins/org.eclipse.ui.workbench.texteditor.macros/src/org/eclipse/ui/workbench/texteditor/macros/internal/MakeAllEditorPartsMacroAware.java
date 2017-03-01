/*******************************************************************************
 * Copyright (c) 2017 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation - http://eclip.se/8519
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.macros.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Used to listen to all the parts/editors available during macro recording to
 * make them macro-aware.
 */
public class MakeAllEditorPartsMacroAware {

	/**
	 * When a new window is opened/activated, make sure its editors are
	 * macro-aware.
	 */
	private class WindowsListener implements IWindowListener {
		@Override
		public void windowOpened(IWorkbenchWindow window) {
			addListeners(window);
		}

		@Override
		public void windowClosed(IWorkbenchWindow window) {
		}

		@Override
		public void windowActivated(IWorkbenchWindow window) {
			addListeners(window);
		}

		@Override
		public void windowDeactivated(IWorkbenchWindow window) {
		}
	}

	/**
	 * When a new part is made visible or is opened, make sure it's made
	 * macro-aware.
	 */
	private class MacroPartListener implements IPartListener2 {

		private Map<StyledText, MacroStyledTextInstaller> fRegisteredIn = new HashMap<>();

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			enableMacroInPart(partRef);
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			enableMacroInPart(partRef);
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			enableMacroInPart(partRef);
		}

		protected void enableMacroInPart(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof IEditorPart) {
				IEditorPart iEditorPart = (IEditorPart) part;
				Control control = iEditorPart.getAdapter(Control.class);
				if (control != null && control instanceof StyledText) {
					StyledText styledText = (StyledText) control;
					if (!styledText.isDisposed()) {
						if (!fRegisteredIn.containsKey(styledText)) {
							MacroStyledTextInstaller macroStyledTextInstaller = new MacroStyledTextInstaller(
									iEditorPart, styledText);
							fMacroService.addMacroStateListener(macroStyledTextInstaller);
							// Make sure its initial state is Correct
							macroStyledTextInstaller.macroStateChanged(fMacroService);
							fRegisteredIn.put(styledText, macroStyledTextInstaller);
						}
					}
				}
			}
		}

		protected void disableMacroInRegisteredParts() {
			Set<Entry<StyledText, MacroStyledTextInstaller>> entrySet = fRegisteredIn.entrySet();
			while (!entrySet.isEmpty()) {
				Iterator<Entry<StyledText, MacroStyledTextInstaller>> iterator = entrySet.iterator();
				Entry<StyledText, MacroStyledTextInstaller> item = iterator.next();
				MacroStyledTextInstaller macroStyledTextInstaller = item.getValue();
				macroStyledTextInstaller.restoreNoMacroState();
				fMacroService.removeMacroStateListener(macroStyledTextInstaller);
				iterator.remove();
			}
		}
	}

	/**
	 * A part listener responsible for making editors macro aware.
	 */
	private MacroPartListener fPartListener = new MacroPartListener();

	/**
	 * A windows listener to make new windows macro aware.
	 */
	private WindowsListener fWindowsListener = new WindowsListener();

	/**
	 * The macro service to be listened to.
	 */
	private EMacroService fMacroService;

	/**
	 * @param macroService
	 *            the macro service.
	 */
	public MakeAllEditorPartsMacroAware(EMacroService macroService) {
		this.fMacroService = macroService;
	}

	private void addListeners(IWorkbenchWindow window) {
		window.getPartService().addPartListener(fPartListener);
	}

	private void removeListeners(IWorkbenchWindow window) {
		window.getPartService().removePartListener(fPartListener);
	}

	/**
	 * Make all the current editors macro-aware (and register for future editors
	 * too).
	 */
	public void install() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			addListeners(window);
		}

		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference ref : page.getEditorReferences()) {
					IEditorPart part = ref.getEditor(false);
					if (part != null) {
						fPartListener.partOpened(ref);
					}
				}
			}
		}
		PlatformUI.getWorkbench().addWindowListener(fWindowsListener);
	}

	/**
	 * Make all the current editors macro-unaware (and remove all registered
	 * listeners).
	 */
	public void uninstall() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			removeListeners(window);
		}
		fPartListener.disableMacroInRegisteredParts();
		PlatformUI.getWorkbench().removeWindowListener(fWindowsListener);
	}

}
