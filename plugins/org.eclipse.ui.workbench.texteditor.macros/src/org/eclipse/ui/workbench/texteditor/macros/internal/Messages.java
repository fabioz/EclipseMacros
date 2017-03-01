package org.eclipse.ui.workbench.texteditor.macros.internal;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.workbench.texteditor.macros.internal.messages"; //$NON-NLS-1$
	public static String StyledTextKeyDownMacroInstruction_KeyDown;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
