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

import java.util.Map;
import org.eclipse.e4.core.macros.IMacroPlaybackContext;
import org.eclipse.e4.ui.macros.macroinstructions.AbstractSWTEventMacroInstruction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A macro instruction to replay a key down (always followed by a key up).
 */
/* default */ class StyledTextKeyDownMacroInstruction extends AbstractSWTEventMacroInstruction {

	private static final String ID = "org.eclipse.ui.texteditor.macro.styledTextKeyDownMacroInstruction"; //$NON-NLS-1$

	public StyledTextKeyDownMacroInstruction(Event event) {
		super(event);
	}

	@Override
	public void execute(IMacroPlaybackContext macroPlaybackContext) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return;
		}
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return;
		}
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			return;
		}
		Control control = activeEditor.getAdapter(Control.class);
		if (control instanceof StyledText) {
			StyledText styledText = (StyledText) control;
			styledText.notifyListeners(SWT.KeyDown, fEvent);

			// Key up is also needed to update the clipboard.
			Event keyUpEvent = copyEvent(fEvent);
			keyUpEvent.type = SWT.KeyUp;
			styledText.notifyListeners(SWT.KeyUp, keyUpEvent);
		}
	}

	@Override
	protected int getEventType() {
		return SWT.KeyDown;
	}

	@Override
	public String getId() {
		return ID;
	}

	/* default */ static StyledTextKeyDownMacroInstruction fromMap(Map<String, String> map) {
		Event event = createEventFromMap(map, SWT.KeyDown);
		return new StyledTextKeyDownMacroInstruction(event);
	}

}