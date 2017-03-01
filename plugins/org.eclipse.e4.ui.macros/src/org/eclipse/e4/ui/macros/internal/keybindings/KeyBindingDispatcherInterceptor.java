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

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.ui.bindings.keys.IKeyBindingInterceptor;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.macros.internal.UserNotifications;
import org.eclipse.swt.widgets.Event;

/***
 * An interceptor for the KeyBindingsDispatcher which will create
 * macro*instructions after a command is successfully executed and to stop
 * actions*which aren'twhitelisted during a macro record/playback session.
 */
public class KeyBindingDispatcherInterceptor implements IKeyBindingInterceptor {

	/**
	 * The KeyBindingDispatcherInterceptor has to know about the macro service
	 * so that it can:
	 *
	 * <p>
	 * <ul>
	 * <li>Skip running commands which aren't accepted when recording or playing
	 * back a macro.</li>
	 * <li>Actually record macro instructions for commands which are run.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * It's <strong>not</strong> responsible for recording keystrokes which
	 * don't map to commands. For this, the actual control or part has to do
	 * that. I.e.: the text editor is responsible for recording key events in
	 * the text editor (this is done so that each part takes full responsibility
	 * for what it's doing related to the macro and there's no need to make
	 * hacks related to it, as each part should know what it expects).
	 * </p>
	 */
	private EMacroService fMacroService;

	/**
	 * The keybinding dispatcher which we're intercepting.
	 */
	private KeyBindingDispatcher fKeybindingDispatcher;

	/**
	 * The id of the last command id we checked for execution.
	 */
	private String fLastCheckedCommandId;

	/**
	 * @param macroService
	 *            the macro service to which this KeyBindingDispatcher is
	 *            related.
	 * @param dispatcher
	 *            the dispatcher which we're intercepting.
	 */
	public KeyBindingDispatcherInterceptor(EMacroService macroService, KeyBindingDispatcher dispatcher) {
		this.fMacroService = macroService;
		this.fKeybindingDispatcher = dispatcher;
	}

	@Override
	public void postExecuteCommand(ParameterizedCommand parameterizedCommand, Event trigger, boolean commandDefined,
			boolean commandHandled) {
		boolean executedCommand = commandDefined && commandHandled;
		if (executedCommand) {
			// Properly record it in the macro.
			if (fMacroService.isRecording()) {
				if (fMacroService.getRecordMacroInstruction(parameterizedCommand.getId())) {
					MacroInstructionForParameterizedCommand macroInstruction = new MacroInstructionForParameterizedCommand(
							parameterizedCommand, trigger, fKeybindingDispatcher);
					fMacroService.addMacroInstruction(macroInstruction, trigger, EMacroService.PRIORITY_HIGH);
				}
			}
		}
	}

	@Override
	public boolean executeCommand(ParameterizedCommand cmd, Event event) {
		this.fLastCheckedCommandId = cmd.getId();
		// We can only run the command if we're not recording/playing
		// back or if it's accepted.
		if (fMacroService == null || (!fMacroService.isRecording() && !fMacroService.isPlayingBack())
				|| fMacroService.isCommandWhitelisted(cmd.getId())) {
			// Ok, allow it to go through.
			return false;
		}
		UserNotifications.showErrorMessage(
				String.format(Messages.KeyBindingDispatcherInterceptor_SkipExecutionOfCommand, cmd.getId()));
		return true;
	}

	/**
	 * @return the command id of the last checked command (being whitelisted or
	 *         not). Note that this is set prior to actually executing the
	 *         command.
	 */
	public String getLastCheckedCommandId() {
		return fLastCheckedCommandId;
	}

	/**
	 * Clear the last checked command id.
	 */
	public void clearLastCheckedCommand() {
		fLastCheckedCommandId = null;
	}
}
