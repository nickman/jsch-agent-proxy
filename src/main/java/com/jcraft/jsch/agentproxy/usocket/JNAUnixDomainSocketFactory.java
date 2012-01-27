/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package com.jcraft.jsch.agentproxy.usocket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Properties;

import com.jcraft.jsch.agentproxy.USocketFactory;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * <p>Title: JNAUnixDomainSocketFactory</p>
 * <p>Description: JNA based analog for JUnixDomainSocketFactory</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>com.jcraft.jsch.agentproxy.usocket.JNAUnixDomainSocketFactory</code></p>
 */
public class JNAUnixDomainSocketFactory implements USocketFactory {
	/** The native library to load  */
	public static final String LIBRARY = "c";

	public static final int AF_UNIX = 1;
	public static final int SOCK_STREAM = 1;
	public static final int PROTOCOL = 0;
	public static final String UNIX_SOCKET_PATH_PROPERTY_NAME = "unixSocketPath";
	public static final String LOCALHOST = "localhost";

	protected java.net.Socket socket = null;
	
	/**
	 * <p>Title: SockAddr</p>
	 * <p>Description: Defines a Unix Socket Address</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>com.jcraft.jsch.agentproxy.usocket.JNAUnixDomainSocketFactory.SockAddr</code></p>
	 */
	protected static class SockAddr extends Structure {
        public final static int SUN_PATH_SIZE = 108;
        public final static byte[] ZERO_BYTE = new byte[] { 0 };
        
        public short sun_family = 1;
        public byte[] sun_path = new byte[SUN_PATH_SIZE];
        
        
        
        public void setSunPath(String sunPath) {
                System.arraycopy(sunPath.getBytes(),0,this.sun_path,0,sunPath.length());
                System.arraycopy(ZERO_BYTE,0,this.sun_path,sunPath.length(),1);
        }
	}
	
	static {
		Native.register(LIBRARY);
	}
	
    public static native int socket(int domain, int type, int protocol);
    public static native int connect(int sockfd, SockAddr sockaddr, int addrlen);
	public static native int read(int fd, ByteBuffer buffer, int count);
	public static native int write(int fd, ByteBuffer buffer, int count);
	public static native int close(int fd);
	public static native String strerror(int errno);


	/**
	 * {@inheritDoc}
	 * @see com.jcraft.jsch.agentproxy.USocketFactory#open(java.lang.String)
	 */
	@Override
	public com.jcraft.jsch.agentproxy.USocketFactory.Socket open(String path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	class UnixSocket extends java.net.Socket {
		int sockfd = -1;
		
		InputStream is = null;
		OutputStream os = null;
		
		UnixSocket(SockAddr sockAddr) throws SocketException {
			this.sockfd = socket(AF_UNIX,SOCK_STREAM,PROTOCOL);
			
			try {
				int i = connect(sockAddr,sockAddr.size());
				
				if (i != 0) {
					new SocketException("UnixSocket(..): could not connect to socket");					
				}
				
			} catch (LastErrorException lee) {
				throwSocketException("UnixSocket(..): could not connect to socket",lee);
			}
			
			this.is = new BufferedInputStream(new UnixSocketInputStream(this));
			this.os = new BufferedOutputStream(new UnixSocketOutputStream(this));
		}

		protected void throwIOException(String prefixMessage, LastErrorException lee) throws IOException {
			String strerror = strerror(lee.getErrorCode());
			
			throw new IOException(prefixMessage + ": " + strerror);
		}

		protected void throwSocketException(String prefixMessage, LastErrorException lee) throws SocketException {
			String strerror = strerror(lee.getErrorCode());
			
			throw new SocketException(prefixMessage + ": " + strerror);
		}

		protected int socket(int domain, int type, int protocol) throws SocketException {
	    	try {
	    		int sockfd = JNAUnixDomainSocketFactory.socket(domain,type,protocol);
	    		
	    		return sockfd;
	    		
	    	} catch (LastErrorException lee) {
	    		throwSocketException("socket(..): could not open socket",lee);
	    		return -1;
	    	}
	    }

	    public int connect(SockAddr sockaddr, int addrlen) throws SocketException {
	    	try {
	    		int result = JNAUnixDomainSocketFactory.connect(this.sockfd,sockaddr,addrlen);
	    		
	    		return result;
	    		
	    	} catch (LastErrorException lee) {
	    		throwSocketException("connect(..): could not connect to socket",lee);
	    		return -1;
	    	}	    	
	    }
	    
	    public int read(byte[] buf, int count) throws IOException {
	    	try {
	    		ByteBuffer buffer = ByteBuffer.wrap(buf);
	    		
	    		int length = JNAUnixDomainSocketFactory.read(this.sockfd,buffer,count);
	    		
	    		return length;
	    		
	    	} catch (LastErrorException lee) {
	    		throwIOException("read(..): could not read from socket",lee);
	    		return -1;
	    	}	    	
	    }
	    
	    public int write(byte[] buf, int count) throws IOException {
	    	try {
	    		ByteBuffer buffer = ByteBuffer.wrap(buf);
	    		
	    		int length = JNAUnixDomainSocketFactory.write(this.sockfd,buffer,count);
	    		
	    		return length;
	    		
	    	} catch (LastErrorException lee) {
	    		throwIOException("write(..): could not write to socket",lee);
	    		return -1;
	    	}	    		    	
	    }
	    
		@Override
		public InputStream getInputStream() throws IOException {
			return is;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return os;
		}

		@Override
		public void shutdownInput() throws IOException {
			is = null;
		}

		@Override
		public void shutdownOutput() throws IOException {
			os = null;
		}

		@Override
		public synchronized void close() throws IOException {
	    	try {
	    		shutdownInput();
	    		shutdownOutput();
	    		JNAUnixDomainSocketFactory.close(this.sockfd);
	    		
	    	} catch (LastErrorException lee) {
	    		throwIOException("close(..): could not close socket",lee);
	    	}	    		    		    	
	    }
	}
	
	
	class UnixSocketInputStream extends InputStream {
		UnixSocket unixSocket = null;
		
		UnixSocketInputStream(UnixSocket unixSocket) {
			this.unixSocket = unixSocket;
		}
		
		@Override
		public long skip(long n) throws IOException {
			return -1;
		}

		@Override
		public synchronized void mark(int readlimit) {
			//
		}

		@Override
		public synchronized void reset() throws IOException {
			//
		}

		@Override
		public boolean markSupported() {
			return false;
		}

		@Override
		public int available() throws IOException {
			return -1;
		}

		@Override
		public int read() throws IOException {
			byte[] data = new byte[1];
			
			int read = read(data);
			
			if (read != 1) {
				throw new IOException("read(..): could not read one byte");
			}
			
			return (int) data[0];
		}		
		
		@Override
		public int read(byte[] data) throws IOException {
			return read(data,0,data.length);
		}

		@Override
		public int read(byte[] data, int offset, int length) throws IOException {
			int readLength = 0;
			
			if (offset == 0) {
				readLength = this.unixSocket.read(data,length);
//				System.out.println("Read " + readLength + ": " + new String(data,0,readLength));
								
			} else {
				throw new IOException("read(..): offset not supported");				
			}
			
			return readLength;
		}

		@Override
		public void close() throws IOException {
			//
		}
	}

	class UnixSocketOutputStream extends OutputStream {
		UnixSocket unixSocket = null;
		
		UnixSocketOutputStream(UnixSocket unixSocket) {
			this.unixSocket = unixSocket;
		}

		@Override
		public void flush() throws IOException {
			//
		}

		@Override
		public void write(byte[] data) throws IOException {
			write(data,0,data.length);
		}

		@Override
		public void write(int data) throws IOException {
			write(new byte[] { (byte) data },0,1);
		}

		@Override
		public void write(byte[] data, int offset, int length) throws IOException {
			if (offset == 0) {
				int writtenLength = this.unixSocket.write(data,length);
//				System.out.println("Wrote " + writtenLength + "/" + length + ": " + new String(data,0,length));
				
				if (writtenLength != length) {
					throw new IOException("write(..): length is " + length + " but only wrote " + writtenLength);
				}
				
			} else {
				throw new IOException("write(..): offset not supported");
			}
		}

		@Override
		public void close() throws IOException {
			//
		}
	}

	
	public java.net.Socket afterHandshake() throws SocketException, IOException {
		return this.socket;
	}

	
	public java.net.Socket beforeHandshake() throws SocketException, IOException {
		return this.socket;
	}
	
	
	
	public java.net.Socket connect(String host, int portNumber, Properties props) throws SocketException, IOException {
		if (!LOCALHOST.equalsIgnoreCase(host)) {
			throw new SocketException("connect(..): must specify a hostname of \"" + LOCALHOST + "\" in the JDBC URL");
		}
		
		String unixSocketPath = props == null ? null : props.getProperty(UNIX_SOCKET_PATH_PROPERTY_NAME);
		
		if (unixSocketPath != null && !"".equals(unixSocketPath.trim())) {
			File unixSocketFile = new File(unixSocketPath);
			
			if (!unixSocketFile.exists()) {
				throw new SocketException("connect(..): could not find Unix socket at \"" + unixSocketPath + "\"; please re-specify \"" + UNIX_SOCKET_PATH_PROPERTY_NAME + "\" property in JDBC URL (or remove the property and typical locations will be tried)");
			}
			
		} else {
			

			if (unixSocketPath == null) {
				throw new SocketException("connect(..): could not find Unix socket after searching typical locations; please specify \"" + UNIX_SOCKET_PATH_PROPERTY_NAME + "\" property in the JDBC URL");			
			}
		}
		
		SockAddr sockAddr = new SockAddr();			
		sockAddr.setSunPath(unixSocketPath);

		this.socket = new UnixSocket(sockAddr);
	
		return this.socket;	
	}
}
	


