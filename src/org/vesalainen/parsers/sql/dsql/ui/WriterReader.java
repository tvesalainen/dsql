/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.parsers.sql.dsql.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Semaphore;

/**
 * @author Timo Vesalainen
 */
public class WriterReader 
{
    private Writer writer = new W();
    private Reader reader = new R();
    private char[] buffer;
    private int offset;
    private Semaphore toWrite = new Semaphore(0);
    private Semaphore toRead = new Semaphore(0);
    private int flushed;

    public Reader getReader()
    {
        return reader;
    }

    public Writer getWriter()
    {
        return writer;
    }
    
    public class W extends Writer
    {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException
        {
            try
            {
                while (true)
                {
                    toWrite.acquire(1);
                    int length = toWrite.availablePermits()+1;
                    if (length > len)
                    {
                        System.arraycopy(cbuf, off, buffer, offset, len);
                        toWrite.acquire(len-1);
                        offset += len;
                        toRead.release(len);
                        return;
                    }
                    else
                    {
                        System.arraycopy(cbuf, off, buffer, offset, length);
                        toWrite.acquire(length-1);
                        len -= length;
                        off += length;
                        toRead.release(length);
                    }
                }
                
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
        }

        @Override
        public void flush() throws IOException
        {
            flushed = toRead.availablePermits();
            toRead.release(flushed);
        }

        @Override
        public void close() throws IOException
        {
            flush();
        }
        
    }
    public class R extends Reader
    {

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
            buffer = cbuf;
            offset = off;
            toWrite.release(len);
            try
            {
                toRead.acquire(len);
                if (flushed > 0)
                {
                    return flushed;
                }
                return len;
            }
            catch (InterruptedException ex)
            {
                throw new IOException(ex);
            }
        }

        @Override
        public void close() throws IOException
        {
        }

        @Override
        public boolean ready() throws IOException
        {
            return toRead.availablePermits() > 0;
        }
        

    }
    public static class Re implements Runnable
    {
        private WriterReader wr;

        public Re(WriterReader wr)
        {
            this.wr = wr;
        }

        @Override
        public void run()
        {
            try
            {
                Reader r = wr.getReader();
                char[] buf = new char[3];
                int cc = r.read(buf);
                while (cc != -1)
                {
                    System.err.print((char)buf[0]);
                    System.err.print((char)buf[1]);
                    System.err.print((char)buf[2]);
                    cc = r.read(buf);
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

    }
    public static void main(String... args)
    {
        try
        {
            WriterReader wr = new WriterReader();
            Thread thr = new Thread(new Re(wr));
            thr.start();
            Writer w = wr.getWriter();
            w.append("hello world!\n");
            Thread.sleep(500);
            thr.interrupt();
        }
        catch (Exception ex)
        {
            
        }
    }
}
