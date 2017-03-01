package org.eclipse.e4.ui.macros.internal.actions;

import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.core.macros.IMacroStateListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * Make sure that the toolbar elements are kept properly updated even if the
 * macro is programatically stopped.
 */
public class KeepRecordMacroToggleActionStateUpdated implements IMacroStateListener {

	@Override
	public void macroStateChanged(EMacroService macroService) {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(ToggleMacroRecordAction.COMMAND_ID, null);
	}
}
