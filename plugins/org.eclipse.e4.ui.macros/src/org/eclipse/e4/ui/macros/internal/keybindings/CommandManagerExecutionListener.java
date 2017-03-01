package org.eclipse.e4.ui.macros.internal.keybindings;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.e4.core.macros.EMacroService;

/**
 *
 */
public class CommandManagerExecutionListener implements IExecutionListener {

	private CommandManager fCommandManager;
	private EMacroService fMacroService;

	/**
	 * @param commandManager
	 * @param macroService
	 */
	public CommandManagerExecutionListener(CommandManager commandManager, EMacroService macroService) {
		this.fCommandManager = commandManager;
		this.fMacroService = macroService;
	}

	@Override
	public void notHandled(String commandId, NotHandledException exception) {

	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {

	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		if (fMacroService.isRecording() && !fMacroService.isCommandWhitelisted(commandId)) {
			System.out.println(fCommandManager);
			System.out.println("Non-whitelisted command got through (TODO: warn user)."); //$NON-NLS-1$
		}
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {

	}

}
