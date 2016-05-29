package jme3_ext_xbuf;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
@RequiredArgsConstructor
public class LoggerCollector extends MarkerIgnoringBase {
	public enum Level {
		TRACE,
		DEBUG,
		INFO,
		WARN,
		ERROR
		;
	}

	@Data
	static class Event {
		long timestamp = System.currentTimeMillis();
		final Level level;
		final FormattingTuple args;
	}

	public final String name;
	public final List<Event> events = new LinkedList<Event>();

	public void dumpTo(Logger dst) {
		for (Event e : events) {
			switch (e.level) {
				case TRACE: dst.trace(e.args.getMessage(), e.args.getThrowable()); break;
				case DEBUG: dst.debug(e.args.getMessage(), e.args.getThrowable()); break;
				case INFO: dst.info(e.args.getMessage(), e.args.getThrowable()); break;
				case WARN: dst.warn(e.args.getMessage(), e.args.getThrowable()); break;
				case ERROR: dst.error(e.args.getMessage(), e.args.getThrowable()); break;
			}
		}
	}

	public int countOf(Level filter) {
		int b = 0;
		for (Event e : events) {
			if(e.level == filter) b++;
		}
		return b;
	}

	@Override public String getName() {
		return name;
	}

	@Override public boolean isDebugEnabled() {
		return debugEnabled;
	}

	@Override public boolean isDebugEnabled(Marker arg0) {
		return false;
	}

	@Override public boolean isErrorEnabled() {
		return errorEnabled;
	}

	@Override public boolean isErrorEnabled(Marker arg0) {
		return false;
	}

	@Override public boolean isInfoEnabled() {
		return infoEnabled;
	}

	@Override public boolean isInfoEnabled(Marker arg0) {
		return false;
	}

	@Override public boolean isTraceEnabled() {
		return traceEnabled;
	}

	@Override public boolean isTraceEnabled(Marker arg0) {
		return false;
	}

	@Override public boolean isWarnEnabled() {
		return warnEnabled;
	}

	@Override public boolean isWarnEnabled(Marker arg0) {
		return false;
	}

	// -- debug
	boolean debugEnabled = false;

	@Override public void debug(String arg0) {
		if(debugEnabled) events.add(new Event(Level.DEBUG, MessageFormatter.format(arg0, null)));
	}

	@Override public void debug(String arg0, Object arg1) {
		if(debugEnabled) events.add(new Event(Level.DEBUG, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void debug(String arg0, Object... arg1) {
		if(debugEnabled) events.add(new Event(Level.DEBUG, MessageFormatter.arrayFormat(arg0, arg1)));
	}

	@Override public void debug(String arg0, Throwable arg1) {
		if(debugEnabled) events.add(new Event(Level.DEBUG, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void debug(String arg0, Object arg1, Object arg2) {
		if(debugEnabled) events.add(new Event(Level.DEBUG, MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- error
	boolean errorEnabled = true;

	@Override public void error(String arg0) {
		if(errorEnabled) events.add(new Event(Level.ERROR, MessageFormatter.format(arg0, null)));
	}

	@Override public void error(String arg0, Object arg1) {
		if(errorEnabled) events.add(new Event(Level.ERROR, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void error(String arg0, Object... arg1) {
		if(errorEnabled) events.add(new Event(Level.ERROR, MessageFormatter.arrayFormat(arg0, arg1)));
	}

	@Override public void error(String arg0, Throwable arg1) {
		if(errorEnabled) events.add(new Event(Level.ERROR, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void error(String arg0, Object arg1, Object arg2) {
		if(errorEnabled) events.add(new Event(Level.ERROR, MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- info
	boolean infoEnabled = true;

	@Override public void info(String arg0) {
		if(infoEnabled) events.add(new Event(Level.INFO, MessageFormatter.format(arg0, null)));
	}

	@Override public void info(String arg0, Object arg1) {
		if(infoEnabled) events.add(new Event(Level.INFO, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void info(String arg0, Object... arg1) {
		if(infoEnabled) events.add(new Event(Level.INFO, MessageFormatter.arrayFormat(arg0, arg1)));
	}

	@Override public void info(String arg0, Throwable arg1) {
		if(infoEnabled) events.add(new Event(Level.INFO, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void info(String arg0, Object arg1, Object arg2) {
		if(infoEnabled) events.add(new Event(Level.INFO, MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- warn
	boolean warnEnabled = true;

	@Override public void warn(String arg0) {
		if(warnEnabled) events.add(new Event(Level.WARN, MessageFormatter.format(arg0, null)));
	}

	@Override public void warn(String arg0, Object arg1) {
		if(warnEnabled) events.add(new Event(Level.WARN, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void warn(String arg0, Object... arg1) {
		if(warnEnabled) events.add(new Event(Level.WARN, MessageFormatter.arrayFormat(arg0, arg1)));
	}

	@Override public void warn(String arg0, Throwable arg1) {
		if(warnEnabled) events.add(new Event(Level.WARN, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void warn(String arg0, Object arg1, Object arg2) {
		if(warnEnabled) events.add(new Event(Level.WARN, MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- trace
	boolean traceEnabled = false;

	@Override public void trace(String arg0) {
		if(traceEnabled) events.add(new Event(Level.TRACE, MessageFormatter.format(arg0, null)));
	}

	@Override public void trace(String arg0, Object arg1) {
		if(traceEnabled) events.add(new Event(Level.TRACE, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void trace(String arg0, Object... arg1) {
		if(traceEnabled) events.add(new Event(Level.TRACE, MessageFormatter.arrayFormat(arg0, arg1)));
	}

	@Override public void trace(String arg0, Throwable arg1) {
		if(traceEnabled) events.add(new Event(Level.TRACE, MessageFormatter.format(arg0, arg1)));
	}

	@Override public void trace(String arg0, Object arg1, Object arg2) {
		if(traceEnabled) events.add(new Event(Level.TRACE, MessageFormatter.format(arg0, arg1, arg2)));
	}

}
