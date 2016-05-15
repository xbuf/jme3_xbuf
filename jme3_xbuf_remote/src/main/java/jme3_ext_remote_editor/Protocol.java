package jme3_ext_remote_editor;

class Protocol {
	static class Kind {
		public static final byte pingpong = 0x01;
		public static final byte logs = 0x02;
		public static final byte askScreenshot = 0x03;
		public static final byte rawScreenshot = 0x04;
		public static final byte msgpack = 0x05;
		public static final byte xbufCmd = 0x06;
	}
}
