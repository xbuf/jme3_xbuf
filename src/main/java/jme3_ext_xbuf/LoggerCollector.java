package jme3_ext_xbuf;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@SuppressWarnings("serial")
@RequiredArgsConstructor
public class LoggerCollector extends MarkerIgnoringBase{
	public enum Level{
		TRACE,DEBUG,INFO,WARN,ERROR;
	}

	@Data
	public static class Event{
		@NonNull Level level;
		@NonNull FormattingTuple args;
		long timestamp=System.currentTimeMillis();
	}

	protected final @Getter String name;
	protected final List<Event> events=new LinkedList<Event>();
	protected @Getter @Setter boolean debugEnabled=false,errorEnabled=true,infoEnabled=true,traceEnabled=false,warnEnabled=true;

	public void dumpTo(Logger dst) {
		for(Event e:events){
			switch(e.level){
				case TRACE:
					dst.trace(e.args.getMessage(),e.args.getThrowable());
				case DEBUG:
					dst.debug(e.args.getMessage(),e.args.getThrowable());
				case INFO:
					dst.info(e.args.getMessage(),e.args.getThrowable());
				case WARN:
					dst.warn(e.args.getMessage(),e.args.getThrowable());
				case ERROR:
					dst.error(e.args.getMessage(),e.args.getThrowable());
			}
		}
	}

	public int countOf(Level filter) {
		return (int)events.stream().filter(e -> e.level==filter).count();
	}

	@Override
	public boolean isDebugEnabled(Marker arg0) {
		return false;
	}

	@Override
	public boolean isErrorEnabled(Marker arg0) {
		return false;
	}

	@Override
	public boolean isInfoEnabled(Marker arg0) {
		return false;
	}

	@Override
	public boolean isTraceEnabled(Marker arg0) {
		return false;
	}

	@Override
	public boolean isWarnEnabled(Marker arg0) {
		return false;
	}

	@Override
	public void debug(String arg0) {
		if(debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0,null)));
	}

	@Override
	public void debug(String arg0, Object arg1) {
		if(debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void debug(String arg0, Object... arg1) { 
		if(debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.arrayFormat(arg0,arg1)));
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		if(debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
		if(debugEnabled) events.add(new Event(Level.DEBUG,MessageFormatter.format(arg0,arg1,arg2)));
	}

	@Override
	public void error(String arg0) {
		if(errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0,null)));
	}

	@Override
	public void error(String arg0, Object arg1) {
		if(errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void error(String arg0, Object... arg1) {
		if(errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.arrayFormat(arg0,arg1)));
	}

	@Override
	public void error(String arg0, Throwable arg1) {
		if(errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2) {
		if(errorEnabled) events.add(new Event(Level.ERROR,MessageFormatter.format(arg0,arg1,arg2)));
	}

	// -- info

	@Override
	public void info(String arg0) {
		if(infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0,null)));
	}

	@Override
	public void info(String arg0, Object arg1) {
		if(infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void info(String arg0, Object... arg1) {
		if(infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.arrayFormat(arg0,arg1)));
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		if(infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2) {
		if(infoEnabled) events.add(new Event(Level.INFO,MessageFormatter.format(arg0,arg1,arg2)));
	}

	// -- warn

	@Override
	public void warn(String arg0) {
		if(warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0,null)));
	}

	@Override
	public void warn(String arg0, Object arg1) {
		if(warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		if(warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.arrayFormat(arg0,arg1)));
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		if(warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
		if(warnEnabled) events.add(new Event(Level.WARN,MessageFormatter.format(arg0,arg1,arg2)));
	}

	// -- trace

	@Override
	public void trace(String arg0) {
		if(traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0,null)));
	}

	@Override
	public void trace(String arg0, Object arg1) {
		if(traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void trace(String arg0, Object... arg1) {
		if(traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.arrayFormat(arg0,arg1)));
	}

	@Override
	public void trace(String arg0, Throwable arg1) {
		if(traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0,arg1)));
	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2) {
		if(traceEnabled) events.add(new Event(Level.TRACE,MessageFormatter.format(arg0,arg1,arg2)));
	}
}
