package org.eclipse.ui.workbench.texteditor.macros.internal;

import org.eclipse.e4.core.macros.EMacroService;
import org.eclipse.e4.core.macros.IMacroStateListener;

/**
 * A listener to the macro context which will make editors macro-aware (to
 * record key strokes and disable code-completion).
 */
public class EditorPartMacroInstaller implements IMacroStateListener {

	private MakeAllEditorPartsMacroAware fMakeEditorPartsMacroAware;

	@Override
	public void macroStateChanged(EMacroService macroService) {
		if (macroService.isRecording() || macroService.isPlayingBack()) {
			if (fMakeEditorPartsMacroAware == null) {
				fMakeEditorPartsMacroAware = new MakeAllEditorPartsMacroAware(macroService);
				fMakeEditorPartsMacroAware.install();
			}
		} else {
			if (fMakeEditorPartsMacroAware != null) {
				fMakeEditorPartsMacroAware.uninstall();
				fMakeEditorPartsMacroAware = null;
			}
		}
	}
}
