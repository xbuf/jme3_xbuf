package jme3_ext_assettools;

import java.io.File;
import java.io.FileInputStream;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import xbuf.Datas.Data;

public class JsonTools {
	public static void main(String[] args) throws Exception {
		dump(new File(args[0]));
	}

	public static void dump(File src) throws Exception {
		try(FileInputStream f = new FileInputStream(src)) {
			Data data = Data.parseFrom(f);
			JsonFormat.Printer printer = JsonFormat.printer().usingTypeRegistry(TypeRegistry.getEmptyTypeRegistry());
			System.out.println(printer.print(data));
		}
	}
}
