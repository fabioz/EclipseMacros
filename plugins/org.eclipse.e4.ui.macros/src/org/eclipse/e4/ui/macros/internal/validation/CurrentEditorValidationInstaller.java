package org.eclipse.e4.ui.macros.internal.validation;

import org.eclipse.e4.core.macros.CancelMacroRecordingException;
import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.core.macros.IMacroInstruction;
import org.eclipse.e4.core.macros.IMacroInstructionsListener;
import org.eclipse.e4.core.macros.IMacroStateListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A validator which will check that when some macro instruction is added, the
 * same editor is still kept.
 */
public class CurrentEditorValidationInstaller implements IMacroStateListener {

	private static class CurrentEditorValidator implements IMacroInstructionsListener {

		private IEditorPart fEditorPart;

		@Override
		public void beforeMacroInstructionAdded(IMacroInstruction macroInstruction)
				throws CancelMacroRecordingException {
			if (fEditorPart == null) {
				fEditorPart = getCurrentEditorPart();
			} else {
				IEditorPart currentEditorPart = getCurrentEditorPart();
				if (currentEditorPart != fEditorPart) {
					// Warn the user and ask him if the macro recording should
					// be aborted.
					IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow == null) {
						throw new CancelMacroRecordingException();
					}
					MessageDialog dialog = new MessageDialog(activeWorkbenchWindow.getShell(),
							Messages.CurrentEditorValidationInstaller_MacroRecordingIssue, null,
							Messages.CurrentEditorValidationInstaller_MacroEditorChangedMessage, MessageDialog.QUESTION,
							0, new String[] { Messages.CurrentEditorValidationInstaller_ProceedWithMacroRecording,
									Messages.CurrentEditorValidationInstaller_StopMacroRecording });
					if (dialog.open() != 0) {
						throw new CancelMacroRecordingException();
					}
					fEditorPart = currentEditorPart;
				}
			}
		}

		private IEditorPart getCurrentEditorPart() {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow == null) {
				return null;
			}
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage == null) {
				return null;
			}
			return activePage.getActiveEditor();
		}
	}

	private CurrentEditorValidator fCurrentEditorValidator;

	@Override
	public void macroStateChanged(EMacroService macroService) {
		if (macroService.isRecording()) {
			if (fCurrentEditorValidator == null) {
				fCurrentEditorValidator = new CurrentEditorValidator();
				macroService.addMacroInstructionsListener(fCurrentEditorValidator);
			}
		} else {
			if (fCurrentEditorValidator != null) {
				macroService.removeMacroInstructionsListener(fCurrentEditorValidator);
				fCurrentEditorValidator = null;
			}
		}
	}

}
