package org.eclipse.e4.core.macros;

/**
 * A listener for the macro instructions being added.
 */
public interface IMacroInstructionsListener {

	/**
	 * The macro instruction which was added to the macro service.
	 * 
	 * @param macroInstruction
	 *            the macro instruction added.
	 * @throws CancelMacroRecordingException
	 *             whether the recording should stop.
	 */
	void beforeMacroInstructionAdded(IMacroInstruction macroInstruction) throws CancelMacroRecordingException;
}
