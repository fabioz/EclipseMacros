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
import org.eclipse.swt.widgets.Event;

/**
 * Used to blacklist and record commands being executed and record whitelisted commands.
 */
public class CommandManagerExecutionListener implements IExecutionListener {

	private EMacroService fMacroService;

	private static class ParameterizedCommandAndTrigger {

		private ParameterizedCommand parameterizedCommand;
		private Object trigger;

		private ParameterizedCommandAndTrigger(ParameterizedCommand parameterizedCommand, Object trigger) {
			this.parameterizedCommand = parameterizedCommand;
			this.trigger = trigger;
		}

	}

	/**
	 * A stack to keep information on the parameterized commands and what
	 * triggered it.
	 */
	private Stack<ParameterizedCommandAndTrigger> fParameterizedCommandsAndTriggerStack = new Stack<>();

	/**
	 * The handler service.
	 */
	private EHandlerService fHandlerService;

	/**
	 * @param macroService
	 *            the macro service
	 * @param handlerService
	 *            the handler service (used to execute actions).
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

	private ParameterizedCommandAndTrigger popCommand(String commandId) {
		if (!fParameterizedCommandsAndTriggerStack.empty()) {
			ParameterizedCommandAndTrigger commandAndTrigger = fParameterizedCommandsAndTriggerStack.peek();
			if (commandId.equals(commandAndTrigger.parameterizedCommand.getCommand().getId())) {
				return commandAndTrigger;
			}
			Activator.log(new RuntimeException(
					String.format("Expected to find %s in parameterizedCommand stack. Found: %s", commandId, //$NON-NLS-1$
							commandAndTrigger.parameterizedCommand.getId())));
			fParameterizedCommandsAndTriggerStack.clear();
		}
		return null;
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		ParameterizedCommandAndTrigger commandAndTrigger = popCommand(commandId);
		if (commandAndTrigger == null) {
			// Can happen if we didn't get the preExecute (i.e.: the toggle
			// macro record is executed and post executed only (the pre execute
			// is skipped because recording still wasn't in place).
			return;
		}
		if (fMacroService.isRecording()) {
			// Record it if needed.
			if (fMacroService.getRecordMacroInstruction(commandId)) {
				if (commandAndTrigger.trigger instanceof Event) {
					fMacroService.addMacroInstruction(
							new MacroInstructionForParameterizedCommand(commandAndTrigger.parameterizedCommand,
									(Event) commandAndTrigger.trigger, this.fHandlerService));
				} else {
					fMacroService.addMacroInstruction(new MacroInstructionForParameterizedCommand(
							commandAndTrigger.parameterizedCommand, this.fHandlerService));
				}
			}
		}
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
		// Let's check if it should actually be recorded.
		if (fMacroService.getRecordMacroInstruction(commandId)) {
			ParameterizedCommand command = ParameterizedCommand.generateCommand(event.getCommand(),
					event.getParameters());
			fParameterizedCommandsAndTriggerStack.add(new ParameterizedCommandAndTrigger(command, event.getTrigger()));
		}
	}
}
