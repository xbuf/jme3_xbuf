package jme3_ext_xbuf.ext;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;

import xbuf.Meshes;
import xbuf.Primitives;
import xbuf.Meshes.VertexArray;
import xbuf.Primitives.Mat4;

public class PrimitiveExt{

	public static Vector2f toJME(Primitives.Vec2 src) {
		return new Vector2f(src.getX(),src.getY());
	}

	public static Vector3f toJME(Primitives.Vec3 src) {
		return new Vector3f(src.getX(),src.getY(),src.getZ());
	}

	public static Vector4f toJME(Primitives.Vec4 src) {
		return new Vector4f(src.getX(),src.getY(),src.getZ(),src.getW());
	}

	public static Quaternion toJME(Primitives.Quaternion src) {
		return new Quaternion(src.getX(),src.getY(),src.getZ(),src.getW());
	}

	public static ColorRGBA toJME(Primitives.Color src) {
		return new ColorRGBA(src.getR(),src.getG(),src.getB(),src.getA());
	}

	public static Matrix4f toJME(Mat4 src) {
		Matrix4f dst=new Matrix4f();
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

	public static Mesh.Mode toJME(Meshes.Mesh.Primitive v) {
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

	public static VertexBuffer.Type toJME(VertexArray.Attrib v) {
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

}
