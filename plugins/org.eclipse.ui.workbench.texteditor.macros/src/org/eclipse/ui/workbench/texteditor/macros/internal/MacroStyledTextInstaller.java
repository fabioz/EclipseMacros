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

import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.core.macros.IMacroStateListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextOperationTargetExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * Helper class to deal with entering/exiting macro record/playback.
 */
public class MacroStyledTextInstaller implements IMacroStateListener {

	/**
	 * null when not in macro record/playback. Used to check the current state
	 * and store information to be restored when exiting macro mode.
	 *
	 */
	private IMemento fMementoStateBeforeMacro;

	/**
	 * Helper class to record keystrokes on the StyledText.
	 */
	private StyledTextMacroRecorder fStyledTextMacroRecorder;

	/**
	 * The editor part where the macro recording should be installed.
	 */
	private IEditorPart fEditorPart;

	/**
	 * The styled text where we should record key strokes.
	 */
	private StyledText fStyledText;

	/**
	 * Constant used to save whether the content assist was enabled before being
	 * disabled in disableCodeCompletion.
	 */
	private static final String CONTENT_ASSIST_ENABLED = "contentAssistEnabled";//$NON-NLS-1$

	/**
	 * @param editorPart
	 * @param styledText
	 */
	public MacroStyledTextInstaller(IEditorPart editorPart, StyledText styledText) {
		this.fEditorPart = editorPart;
		this.fStyledText = styledText;
	}

	/**
	 * Re-enables the content assist based on the state of the key
	 * {@link #CONTENT_ASSIST_ENABLED} in the passed memento.
	 *
	 * @param memento
	 *            the memento where a key with {@link #CONTENT_ASSIST_ENABLED}
	 *            with the enabled state of the content assist to be restored.
	 */
	private void restoreContentAssist(IMemento memento) {
		ITextOperationTarget textOperationTarget = this.fEditorPart.getAdapter(ITextOperationTarget.class);
		if (textOperationTarget instanceof ITextOperationTargetExtension) {
			ITextOperationTargetExtension targetExtension = (ITextOperationTargetExtension) textOperationTarget;
			if (textOperationTarget instanceof ITextOperationTargetExtension) {
				Boolean contentAssistProposalsBeforMacroMode = memento.getBoolean(CONTENT_ASSIST_ENABLED);
				if (contentAssistProposalsBeforMacroMode != null) {
					if ((contentAssistProposalsBeforMacroMode).booleanValue()) {
						targetExtension.enableOperation(ISourceViewer.CONTENTASSIST_PROPOSALS, true);
					} else {
						targetExtension.enableOperation(ISourceViewer.CONTENTASSIST_PROPOSALS, false);
					}
				}
			}
		}
	}

	/**
	 * Disables the content assist and saves the previous state on the passed
	 * memento (note that it's only saved if it is actually disabled here).
	 *
	 * @param memento
	 *            memento where the previous state should be saved, to be
	 *            properly restored later on in
	 *            {@link #restoreContentAssist(IMemento)}.
	 */
	private void disableContentAssist(IMemento memento) {
		ITextOperationTarget textOperationTarget = this.fEditorPart.getAdapter(ITextOperationTarget.class);
		if (textOperationTarget instanceof ITextOperationTargetExtension) {
			ITextOperationTargetExtension targetExtension = (ITextOperationTargetExtension) textOperationTarget;

			// Disable content assist and mark it to be restored
			// later on
			if (textOperationTarget.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
				memento.putBoolean(CONTENT_ASSIST_ENABLED, true);
				targetExtension.enableOperation(ISourceViewer.CONTENTASSIST_PROPOSALS, false);
			}
		}
	}

	/**
	 * Implemented to properly deal with macro recording/playback (i.e.: the
	 * editor may need to disable content assist during macro recording and it
	 * needs to record keystrokes to be played back afterwards).
	 *
	 * @see org.eclipse.e4.core.macros.IMacroStateListener#macroStateChanged(org.eclipse.e4.core.macros.EMacroService)
	 */
	@Override
	public void macroStateChanged(EMacroService macroService) {
		if (macroService.isPlayingBack() || (macroService.isRecording())) {
			// We always need to disable content assist on playback and we
			// selectively disable it on record based on
			// the return of getDisableContentAssistOnMacroRecord(), which
			// subclasses may override.
			if (fMementoStateBeforeMacro == null) {
				fMementoStateBeforeMacro = XMLMemento.createWriteRoot("AbstractTextEditorXmlMemento"); //$NON-NLS-1$
				disableContentAssist(fMementoStateBeforeMacro);
			}
		} else {
			restoreContentAssist();
		}

		// When recording install a recorder for key events (and uninstall
		// if not recording).
		if (macroService.isRecording()) {
			if (fStyledTextMacroRecorder == null) {
				if (macroService.isRecording()) {
					if (fStyledText != null && !fStyledText.isDisposed()) {
						fStyledTextMacroRecorder = new StyledTextMacroRecorder(macroService);
						fStyledTextMacroRecorder.install(fStyledText);
					}
				}
			}
		} else { // !macroService.isRecording()
			disableRecording();
		}
	}

	/**
	 * Restores the state of the editor to have no macro functions.
	 */
	public void restoreNoMacroState() {
		restoreContentAssist();
		disableRecording();
	}

	private void restoreContentAssist() {
		if (fMementoStateBeforeMacro != null) {
			// Restores content assist if it was disabled (based on the
			// memento)
			restoreContentAssist(fMementoStateBeforeMacro);
			fMementoStateBeforeMacro = null;
		}
	}

	private void disableRecording() {
		if (fStyledTextMacroRecorder != null) {
			if (fStyledText != null && !fStyledText.isDisposed()) {
				fStyledTextMacroRecorder.uninstall(fStyledText);
				fStyledTextMacroRecorder = null;
			}
		}
	}

}
