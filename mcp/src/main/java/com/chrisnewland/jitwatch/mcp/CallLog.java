package com.chrisnewland.jitwatch.mcp;

import java.io.PrintStream;

/**
 * One line when a tool call starts, one when it ends, on stderr because stdout carries
 * the protocol under stdio. The trace therefore lands in whatever console started the
 * server, and a host recording the child's stderr keeps it.
 *
 * Deliberately not JITWatch's own logger, which prints a line per javap invocation, per
 * unhandled tag and per unresolvable class, burying a call in hundreds of lines. These
 * always print; that one is quiet unless asked.
 */
public final class CallLog {

	private static final String PREFIX = "[jitwatch-mcp] ";

	private static final int MAX_DETAIL = 300;

	private final PrintStream out;

	private final boolean stackTraces;

	public CallLog(PrintStream out, boolean stackTraces) {
		this.out = out;
		this.stackTraces = stackTraces;
	}

	public void note(String message) {
		out.println(PREFIX + message);
	}

	public void started(String tool, Object arguments) {
		String rendered = arguments == null ? "" : " " + abbreviate(String.valueOf(arguments));

		out.println(PREFIX + "-> " + tool + rendered);
	}

	public void succeeded(String tool, long millis, int chars) {
		out.println(PREFIX + "<- " + tool + " ok " + millis + "ms " + chars + " chars");
	}

	public void rejected(String tool, long millis, String message) {
		finished(tool, "bad-argument", millis, message);
	}

	/** A stack trace follows when the log level asked for one. */
	public void failed(String tool, long millis, String message, Throwable cause) {
		finished(tool, "failed", millis, message);

		if (stackTraces && cause != null) {
			cause.printStackTrace(out);
		}
	}

	private void finished(String tool, String outcome, long millis, String message) {
		StringBuilder builder = new StringBuilder(PREFIX).append("<- ")
			.append(tool)
			.append(' ')
			.append(outcome)
			.append(' ')
			.append(millis)
			.append("ms");

		if (message != null) {
			builder.append(" : ").append(abbreviate(message));
		}

		out.println(builder);
	}

	private static String abbreviate(String text) {
		String oneLine = text.replace('\n', ' ');

		return oneLine.length() <= MAX_DETAIL ? oneLine : oneLine.substring(0, MAX_DETAIL) + "…";
	}

}
