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
package org.eclipse.e4.ui.macros.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.macros.Activator;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Helper class to show notifications to the user.
 */
public class UserNotifications {

	/**
	 * Sets a given message to be shown to the user.
	 *
	 * @param message
	 *            the message to be shown or null to clear it.
	 */
	public static void setMessage(String message) {
		IStatusLineManager statusLineManager = UserNotifications.getStatusLineManager();
		if (statusLineManager != null) {
			statusLineManager.setMessage(message);
			if (message == null) {
				// Also clear any previous error message we might have set.
				statusLineManager.setErrorMessage(null);
			}
		}
	}

	/**
	 * Shows some error message related to the macro to the user.
	 *
	 * @param message
	 *            the error message to be shown (cannot be null).
	 */
	public static void showErrorMessage(String message) {
		Activator plugin = Activator.getDefault();
		if (plugin != null) {
			// Log it
			plugin.getLog().log(new Status(IStatus.INFO, plugin.getBundle().getSymbolicName(), message));
		}

		// Make it visible to the user.
		IStatusLineManager statusLineManager = UserNotifications.getStatusLineManager();
		if (statusLineManager == null) {
			MessageDialog.openWarning(UserNotifications.getParent(), Messages.Activator_ErrorMacroRecording, message);
		} else {
			statusLineManager.setErrorMessage(message);
			Display current = Display.getCurrent();
			if (current != null) {
				// Also beep to say something strange happened.
				current.beep();
			}
		}
	}

	/**
	 * @return a shell to be used as a dialog's parent.
	 */
	private static Shell getParent() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		return activeWorkbenchWindow.getShell();
	}

	/**
	 * @return the available status line manager for the current editor.
	 */
	private static IStatusLineManager getStatusLineManager() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return null;
		}
		IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor == null) {
			return null;
		}
		IEditorSite editorSite = activeEditor.getEditorSite();
		if (editorSite == null) {
			return null;
		}
		return editorSite.getActionBars().getStatusLineManager();
	}

}
