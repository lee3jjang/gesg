package com.gof.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggingOutputStream  extends OutputStream{
	
	private static final int DeFAULT_BUFFER_LENGTH =2048;
	private boolean hasBeedClosed=false;
	private byte[] buf;
	private int count;
	private int curBufLength;
	private Logger log;
	private Level level;
	
	public LoggingOutputStream( Logger log, Level level)  throws IllegalArgumentException{
		if(log== null || level==null) {
			throw new IllegalArgumentException("Logger or log Level must bu not null");
			
		}
		this.log = log;
		this.level = level;
		curBufLength = DeFAULT_BUFFER_LENGTH;
		buf = new byte[curBufLength];
		count =0;
	}

	@Override
	public void write(int b) throws IOException {
		if(hasBeedClosed) {
			throw new IOException("stream has been closed");
			
		}
		if(b==0) {
			return;
		}
		if(count == curBufLength){
			final int newBufLength = curBufLength + DeFAULT_BUFFER_LENGTH;
			final byte[] newBuf = new byte[newBufLength];
			System.arraycopy(buf, 0, newBuf, 0, curBufLength);
			buf = newBuf;
			curBufLength = newBufLength;
		}
		
		buf[count] =(byte) b;
		count++;
		
		
	}
	
	public void flush() {
		if(count ==0) {
			return;
		}
		
		final byte[] bytes = new byte[count];
		System.arraycopy(buf, 0, bytes, 0, count);
		String str = new String(bytes);
		
		log.log(level, str);
		count=0;
		
	}
	
	public void close() {
		flush();
		hasBeedClosed =true;
	}
	
	
	
}
