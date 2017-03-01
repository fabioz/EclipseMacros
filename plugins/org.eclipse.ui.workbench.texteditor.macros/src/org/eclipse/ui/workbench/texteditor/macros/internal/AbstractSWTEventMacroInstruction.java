package org.eclipse.ui.workbench.texteditor.macros.internal;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.macros.IMacroInstruction;
import org.eclipse.swt.widgets.Event;

/**
 * Base classe for a macro instruction based on events from a given type.
 *
 */
public abstract class AbstractSWTEventMacroInstruction implements IMacroInstruction {


	private static final String CHARACTER = "character"; //$NON-NLS-1$

	private static final String STATE_MASK = "stateMask"; //$NON-NLS-1$

	private static final String KEY_CODE = "keyCode"; //$NON-NLS-1$

	private static final String DETAIL = "detail"; //$NON-NLS-1$

	protected final Event fEvent;

	/**
	 * @param event
	 *            the event for which the macro instruction is being created.
	 */
	public AbstractSWTEventMacroInstruction(Event event) {
		// Create a new event (we want to make sure that only the given info is
		// really needed on playback and don't want to keep a reference to the
		// original widget).
		Assert.isTrue(event.type == getEventType());
		Event newEvent = copyEvent(event);

		this.fEvent = newEvent;
	}

	/**
	 *
	 * @return the event type of the events that this macro instruction is
	 *         related to.
	 */
	protected abstract int getEventType();

	/**
	 * Helper to create a copy of some event.
	 *
	 * @param event
	 *            the event to be copied.
	 * @return a copy of the passed event.
	 */
	protected static Event copyEvent(Event event) {
		Event newEvent = new Event();
		newEvent.keyCode = event.keyCode;
		newEvent.stateMask = event.stateMask;
		newEvent.type = event.type;
		newEvent.character = event.character;
		newEvent.detail = event.detail;
		return newEvent;
	}

	protected static Event createEventFromMap(Map<String, String> map, int eventType) {
		Event event = new Event();
		event.type = eventType;

		String keyCode = map.get(KEY_CODE);
		if (keyCode != null) {
			event.keyCode = Integer.parseInt(keyCode);
		} else {
			event.keyCode = 0;
		}

		String stateMask = map.get(STATE_MASK);
		if (stateMask != null) {
			event.stateMask = Integer.parseInt(stateMask);
		} else {
			event.stateMask = 0;
		}

		String character = map.get(CHARACTER);
		if (character != null) {
			event.character = character.charAt(0);
		} else {
			event.character = '\0';
		}

		String detail = map.get(DETAIL);
		if (detail != null) {
			event.detail = Integer.parseInt(detail);
		} else {
			event.detail = 0;
		}
		return event;
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();

		// Only save non-default values.

		if (fEvent.keyCode != 0) {
			map.put(KEY_CODE, Integer.toString(fEvent.keyCode));
		}

		if (fEvent.stateMask != 0) {
			map.put(STATE_MASK, Integer.toString(fEvent.stateMask));
		}

		if (fEvent.character != '\0') {
			map.put(CHARACTER, Character.toString(fEvent.character));
		}

		if (fEvent.detail != 0) {
			map.put(DETAIL, Integer.toString(fEvent.detail));
		}
		return map;
	}

}
