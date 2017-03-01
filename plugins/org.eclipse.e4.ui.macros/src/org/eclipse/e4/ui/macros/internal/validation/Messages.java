package org.eclipse.e4.ui.macros.internal.validation;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.macros.internal.validation.messages"; //$NON-NLS-1$
	public static String CurrentEditorValidationInstaller_MacroEditorChangedMessage;
	public static String CurrentEditorValidationInstaller_MacroRecordingIssue;
	public static String CurrentEditorValidationInstaller_ProceedWithMacroRecording;
	public static String CurrentEditorValidationInstaller_StopMacroRecording;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
