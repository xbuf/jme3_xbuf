package jme3_ext_remote_editor;

class Protocol {
	static class Kind {
		public static val pingpong = 0x01 as byte
		public static val logs = 0x02 as byte
		public static val askScreenshot = 0x03 as byte
		public static val rawScreenshot = 0x04 as byte
		public static val msgpack = 0x05 as byte
		public static val xbufCmd = 0x06 as byte
	}
}
