package jme3_ext_xbuf.debugtools;

import static java.lang.System.exit;
import static java.lang.System.out;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.google.protobuf.TextFormat;

import xbuf.Datas.Data;

// Dump xbuf(=protobuf) to an human readable format file.
public class XbufDumper{
	static void usage() {out.println("Usage:\n -f input [-o output] [-b true/false]\n -b true/false = Enable/disable refid replacement ");}

	public static void main(String[] args) throws IOException {

		if(args.length==0){//Interactive
			usage();
			Scanner in=new Scanner(System.in);
			out.print("Args: ");
			args=in.nextLine().split("\\s+");
			in.close();
		}

		BiFunction<String[],String,String> findArg=(fargs, find) -> {
			int j=IntStream.range(0,fargs.length).filter(i -> i>0&&fargs[i-1].toLowerCase().equals("-"+find)).findAny().orElse(-1);
			return j==-1?null:fargs[j];
		};

		String file=findArg.apply(args,"f");
		if(file==null){usage();exit(1);}

		out.printf("Read from %s\n",file);
		BufferedInputStream bi=new BufferedInputStream(new FileInputStream(file));
		Data data=Data.parseFrom(bi);
		bi.close();

		String human_data=TextFormat.printToString(data);
		if(findArg.apply(args,"b")!=null){
			ByteArrayOutputStream wi_a=new ByteArrayOutputStream();
			InputStream wi=new URL("https://en.wikibooks.org/wiki/Italian/Vocabulary/Animals").openStream();
			byte chunk[]=new byte[1024*1024];
			for(int readed;(readed=wi.read(chunk))!=-1;)wi_a.write(chunk,0,readed);
			wi.close();
			String words_dict=wi_a.toString("UTF-8");
			Pattern p=Pattern.compile("\\<\\s*li\\s*\\>\\s*[^\\s']+[\\s']([A-Z]+)\\s+\\=",Pattern.CASE_INSENSITIVE);
			Matcher m=p.matcher(words_dict);
			ArrayList<String> names=new ArrayList<String>();
			while(m.find())names.add(m.group(1));
			
			
			int n_i=0;
			int nn_i=0;
			p=Pattern.compile("(\"\\-[0-9]+)(?:_[0-9]+)?\"");
			m=p.matcher(human_data);
			while(m.find()){
				if(n_i>=names.size()){
					n_i=0;
					nn_i++;
				}
				String n=names.get(n_i++);
				String tail=nn_i==0?"":"::"+nn_i;
				human_data=human_data.replace(m.group(1),"\""+n+tail);
			}
		}
		
		String out_file=findArg.apply(args,"o");
		if(out_file==null) out.println(human_data);
		else{
			out.printf("Write to %s\n",out_file);
			FileOutputStream fos=new FileOutputStream(out_file);
			fos.write(human_data.getBytes(Charset.forName("UTF-8")));
			fos.close();
		}
	}
}
