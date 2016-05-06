package jme3_ext_xbuf;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;

import xbuf.Meshes;
import xbuf.Meshes.FloatBuffer;
import xbuf.Meshes.UintBuffer;
import xbuf.Meshes.VertexArray;
import xbuf.Primitives;
import xbuf.Primitives.Mat4;

public class Converters{

	public static Vector2f cnv(Primitives.Vec2 src, Vector2f dst) {
		dst.set(src.getX(),src.getY());
		return dst;
	}

	public static Vector3f cnv(Primitives.Vec3 src, Vector3f dst) {
		dst.set(src.getX(),src.getY(),src.getZ());
		return dst;
	}

	public static Vector4f cnv(Primitives.Vec4 src, Vector4f dst) {
		dst.set(src.getX(),src.getY(),src.getZ(),src.getW());
		return dst;
	}

	public static Quaternion cnv(Primitives.Quaternion src, Quaternion dst) {
		dst.set(src.getX(),src.getY(),src.getZ(),src.getW());
		return dst;
	}

	public static Vector4f cnv(Primitives.Quaternion src, Vector4f dst) {
		dst.set(src.getX(),src.getY(),src.getZ(),src.getW());
		return dst;
	}

	public static ColorRGBA cnv(Primitives.Color src, ColorRGBA dst) {
		dst.set(src.getR(),src.getG(),src.getB(),src.getA());
		return dst;
	}

	public static Matrix4f cnv(Mat4 src, Matrix4f dst) {
		dst.m00=src.getC00();
		dst.m10=src.getC10();
		dst.m20=src.getC20();
		dst.m30=src.getC30();
		dst.m01=src.getC01();
		dst.m11=src.getC11();
		dst.m21=src.getC21();
		dst.m31=src.getC31();
		dst.m02=src.getC02();
		dst.m12=src.getC12();
		dst.m22=src.getC22();
		dst.m32=src.getC32();
		dst.m03=src.getC03();
		dst.m13=src.getC13();
		dst.m23=src.getC23();
		dst.m33=src.getC33();
		return dst;
	}

	public static Mesh.Mode cnv(Meshes.Mesh.Primitive v) {
		switch(v){
			case line_strip:
				return Mode.LineStrip;
			case lines:
				return Mode.Lines;
			case points:
				return Mode.Points;
			case triangle_strip:
				return Mode.TriangleStrip;
			case triangles:
				return Mode.Triangles;
			default:
				throw new IllegalArgumentException(String.format("doesn't support %s : %s",v==null?"?":v.getClass(),v));
		}
	}

	public static VertexBuffer.Type cnv(VertexArray.Attrib v) {
		switch(v){
			case position:
				return Type.Position;
			case normal:
				return Type.Normal;
			case bitangent:
				return Type.Binormal;
			case tangent:
				return Type.Tangent;
			case color:
				return Type.Color;
			case texcoord:
				return Type.TexCoord;
			case texcoord2:
				return Type.TexCoord2;
			case texcoord3:
				return Type.TexCoord3;
			case texcoord4:
				return Type.TexCoord4;
			case texcoord5:
				return Type.TexCoord5;
			case texcoord6:
				return Type.TexCoord6;
			case texcoord7:
				return Type.TexCoord7;
			case texcoord8:
				return Type.TexCoord8;
			default:
				throw new IllegalArgumentException(String.format("doesn't support %s : %s",v==null?"?":v.getClass(),v));
		}
	}

	//TODO use an optim version: including a patch for no autoboxing : https://code.google.com/p/protobuf/issues/detail?id=464
	public static float[] hack_cnv(FloatBuffer src) {
		return Floats.toArray(src.getValuesList());
	}

	//TODO use an optim version: including a patch for no autoboxing : https://code.google.com/p/protobuf/issues/detail?id=464
	public static int[] hack_cnv(UintBuffer src) {
		return Ints.toArray(src.getValuesList());
	}
}
