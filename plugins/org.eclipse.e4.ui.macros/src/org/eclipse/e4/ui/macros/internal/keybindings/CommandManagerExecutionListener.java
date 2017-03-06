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
package org.eclipse.e4.ui.macros.internal.keybindings;

import java.util.Stack;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.macros.Activator;
import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.ui.macros.internal.UserNotifications;
import org.eclipse.e4.ui.macros.internal.actions.ToggleMacroRecordAction;
import org.eclipse.swt.widgets.Event;

/**
 * Used to give notifications to the user in case some command goes through and
 * it didn't go through the keybinding dispatcher (the user is meant to use only
 * the keyboard to execute commands, not activate other UI elements during macro
 * recording, so, if he fails to do so, let him know about it).
 */
public class CommandManagerExecutionListener implements IExecutionListener {

	private EMacroService fMacroService;

	private static class CommandAndTrigger {

		private ParameterizedCommand command;
		private Object trigger;

		/**
		 * @param command
		 * @param trigger
		 */
		public CommandAndTrigger(ParameterizedCommand command, Object trigger) {
			this.command = command;
			this.trigger = trigger;
		}

	}

	private Stack<CommandAndTrigger> fCommandsStack = new Stack<>();
	private EHandlerService fHandlerService;

	/**
	 * @param macroService
	 *            the macro service
	 * @param interceptor
	 *            the interceptor which is used to actually record commands.
	 */
	public CommandManagerExecutionListener(EMacroService macroService, EHandlerService handlerService) {
		this.fMacroService = macroService;
		this.fHandlerService = handlerService;
	}

	@Override
	public void notHandled(String commandId, NotHandledException exception) {
		popCommand(commandId);
	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
		popCommand(commandId);
	}

	private CommandAndTrigger popCommand(String commandId) {
		if (!fCommandsStack.empty()) {
			CommandAndTrigger commandAndTrigger = fCommandsStack.peek();
			if (commandId.equals(commandAndTrigger.command.getCommand().getId())) {
				return commandAndTrigger;
			}
			Activator.log(new RuntimeException(
					String.format("Expected to find %s in command stack. Found: %s", commandId, //$NON-NLS-1$
							commandAndTrigger.command.getId())));
			fCommandsStack.clear();
		}
		return null;
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		CommandAndTrigger commandAndTrigger = popCommand(commandId);
		if (fMacroService.isRecording()) {
			if (ToggleMacroRecordAction.COMMAND_ID.equals(commandId)) {
				// It's an exception because it's the command that starts it all
				// and thus isn't initially checked in the keybindings
				// interceptor.
				return;
			}
			if (!fMacroService.isCommandWhitelisted(commandId)) {
				// If we got to post execute something not whitelisted, it means
				// it wasn't executed through the keybindings (otherwise we
				// could've blacklisted it), but through some other way.
				String message = String.format(Messages.CommandManagerExecutionListener_CommandNotRecorded, commandId);
				UserNotifications.showErrorMessage(message);
			} else {
				// Ok, it's a whitelisted command. Let's check if it should
				// actually be recorded
				if (fMacroService.getRecordMacroInstruction(commandId)) {
					if (commandAndTrigger.trigger instanceof Event) {
						fMacroService.addMacroInstruction(new MacroInstructionForParameterizedCommand(
								commandAndTrigger.command, (Event) commandAndTrigger.trigger, this.fHandlerService));
					} else {
						fMacroService.addMacroInstruction(new MacroInstructionForParameterizedCommand(
								commandAndTrigger.command, this.fHandlerService));
					}
				}
			}
		}
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {

		if (!fMacroService.isCommandWhitelisted(commandId)) {
			// If we got to post execute something not whitelisted, it means
			// it wasn't executed through the keybindings (otherwise we
			// could've blacklisted it), but through some other way.
			String message = String.format(Messages.CommandManagerExecutionListener_CommandBlacklisted, commandId);
			UserNotifications.showErrorMessage(message);

			throw new RuntimeException(message);

		} else {
			// Ok, it's a whitelisted command. Let's check if it should
			// actually be recorded
			if (fMacroService.getRecordMacroInstruction(commandId)) {
				ParameterizedCommand command = ParameterizedCommand.generateCommand(event.getCommand(),
						event.getParameters());
				fCommandsStack.add(new CommandAndTrigger(command, event.getTrigger()));
			}
		}
	}
}
