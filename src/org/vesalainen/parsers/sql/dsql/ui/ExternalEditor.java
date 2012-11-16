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

import java.awt.Dialog;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Timo Vesalainen
 */
public class ExternalEditor extends OkCancelDialog
{
    private Path path;
    private Thread processThread;

    public ExternalEditor(Path path)
    {
        this.path = path;
    }

    @Override
    public boolean input()
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/C", path.toString());   // TODO 
            Process process = pb.start();
            processThread = new Thread(new ProcessWaiter(process));
            processThread.start();
            
            boolean result = super.input();
            interrupt();
            return result;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private synchronized void interrupt()
    {
        if (processThread != null)
        {
            processThread.interrupt();
            processThread = null;
        }
        setVisible(false);
    }
    @Override
    protected void init()
    {
        super.init();
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
    }

    private class ProcessWaiter implements Runnable
    {
        private Process process;

        public ProcessWaiter(Process process)
        {
            this.process = process;
        }
        
        @Override
        public void run()
        {
            try
            {
                process.waitFor();
                interrupt();
            }
            catch (InterruptedException ex)
            {
            }
        }
        
    }
}
