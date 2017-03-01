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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.ui.macros.internal.UserNotifications;
import org.eclipse.e4.ui.macros.internal.actions.ToggleMacroRecordAction;

/**
 * Used to give notifications to the user in case some command goes through and
 * it didn't go through the keybinding dispatcher (the user is meant to use only
 * the keyboard to execute commands, not activate other UI elements during macro
 * recording, so, if he fails to do so, let him know about it).
 */
public class CommandManagerExecutionListener implements IExecutionListener {

	private EMacroService fMacroService;
	private KeyBindingDispatcherInterceptor fInterceptor;

	/**
	 * @param macroService
	 * @param interceptor
	 */
	public CommandManagerExecutionListener(EMacroService macroService, KeyBindingDispatcherInterceptor interceptor) {
		this.fMacroService = macroService;
		this.fInterceptor = interceptor;
	}

	@Override
	public void notHandled(String commandId, NotHandledException exception) {
		fInterceptor.clearLastCheckedCommand();
	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
		fInterceptor.clearLastCheckedCommand();
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
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
				// Ok, it's a whitelisted command. Let's check if it was
				// actually recorded in the keybindings interceptor.
				if (!commandId.equals(fInterceptor.getLastCheckedCommandId())) {
					String message = String.format(Messages.CommandManagerExecutionListener_CommandNotRecorded,
							commandId);
					UserNotifications.showErrorMessage(message);
				}
			}
		}
		fInterceptor.clearLastCheckedCommand();
	}


	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
	}

}
