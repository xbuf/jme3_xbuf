package jme3_ext_xbuf;

import java.util.LinkedList
import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.helpers.FormattingTuple
import org.slf4j.helpers.MarkerIgnoringBase
import org.slf4j.helpers.MessageFormatter

@SuppressWarnings("serial")
@FinalFieldsConstructor
class LoggerCollector extends MarkerIgnoringBase {
	public enum Level {
		TRACE, DEBUG,INFO,WARN,ERROR;
	}

	@Data
	static class Event {
		long timestamp = System.currentTimeMillis()
		Level level
		FormattingTuple args
	}

	public val String name;
	public val List<Event> events = new LinkedList<Event>();

	def dumpTo(Logger dst) {
		for(Event e: events) {
			switch(e.level) {
			case TRACE : dst.trace(e.args.getMessage(), e.args.getThrowable())
			case DEBUG : dst.debug(e.args.getMessage(), e.args.getThrowable())
			case INFO : dst.info(e.args.getMessage(), e.args.getThrowable())
			case WARN : dst.warn(e.args.getMessage(), e.args.getThrowable())
			case ERROR : dst.error(e.args.getMessage(), e.args.getThrowable())
			}
		}
	}

	def int countOf(Level filter) {
		var b = 0;
		for(Event e: events) {
			if (e.level == filter) b++;
		}
		b
	}

	override String getName() {
		return name;
	}
	override boolean isDebugEnabled() {
		return debugEnabled;
	}
	override boolean isDebugEnabled(Marker arg0) {
		return false;
	}
	override boolean isErrorEnabled() {
		return errorEnabled;
	}
	override boolean isErrorEnabled(Marker arg0) {
		return false;
	}
	override boolean isInfoEnabled() {
		return infoEnabled;
	}
	override boolean isInfoEnabled(Marker arg0) {
		return false;
	}
	override boolean isTraceEnabled() {
		return traceEnabled;
	}
	override boolean isTraceEnabled(Marker arg0) {
		return false;
	}
	override boolean isWarnEnabled() {
		return warnEnabled;
	}
	override boolean isWarnEnabled(Marker arg0) {
		return false;
	}

	// -- debug
	boolean debugEnabled = false;

	override debug(String arg0) {
		if (debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0, null)));
	}
	override debug(String arg0, Object arg1) {
		if (debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0, arg1)));
	}
	override debug(String arg0, Object... arg1) {
		if (debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.arrayFormat(arg0, arg1)));
	}
	override debug(String arg0, Throwable arg1) {
		if (debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0, arg1)));
	}
	override debug(String arg0, Object arg1, Object arg2) {
		if (debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- error
	boolean errorEnabled = true;

	override error(String arg0) {
		if (errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0, null)));
	}
	override error(String arg0, Object arg1) {
		if (errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0, arg1)));
	}
	override error(String arg0, Object... arg1) {
		if (errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.arrayFormat(arg0, arg1)));
	}
	override error(String arg0, Throwable arg1) {
		if (errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0, arg1)));
	}
	override error(String arg0, Object arg1, Object arg2) {
		if (errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- info
	boolean infoEnabled = true;

	override info(String arg0) {
		if (infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0, null)));
	}
	override info(String arg0, Object arg1) {
		if (infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0, arg1)));
	}
	override info(String arg0, Object... arg1) {
		if (infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.arrayFormat(arg0, arg1)));
	}
	override info(String arg0, Throwable arg1) {
		if (infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0, arg1)));
	}
	override info(String arg0, Object arg1, Object arg2) {
		if (infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- warn
	boolean warnEnabled = true;

	override warn(String arg0) {
		if (warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0, null)));
	}
	override warn(String arg0, Object arg1) {
		if (warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0, arg1)));
	}
	override warn(String arg0, Object... arg1) {
		if (warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.arrayFormat(arg0, arg1)));
	}
	override warn(String arg0, Throwable arg1) {
		if (warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0, arg1)));
	}
	override warn(String arg0, Object arg1, Object arg2) {
		if (warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0, arg1, arg2)));
	}

	// -- trace
	boolean traceEnabled = false;

	override trace(String arg0) {
		if (traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0, null)));
	}
	override trace(String arg0, Object arg1) {
		if (traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0, arg1)));
	}
	override trace(String arg0, Object... arg1) {
		if (traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.arrayFormat(arg0, arg1)));
	}
	override trace(String arg0, Throwable arg1) {
		if (traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0, arg1)));
	}
	override trace(String arg0, Object arg1, Object arg2) {
		if (traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0, arg1, arg2)));
	}

}
