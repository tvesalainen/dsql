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

import org.vesalainen.parsers.sql.dsql.ui.action.MetadataTreeAction;
import org.vesalainen.parsers.sql.dsql.ui.action.RedoAction;
import org.vesalainen.parsers.sql.dsql.ui.action.OpenStatementAction;
import org.vesalainen.parsers.sql.dsql.ui.action.UndoAction;
import org.vesalainen.parsers.sql.dsql.ui.action.SaveStatementAction;
import org.vesalainen.parsers.sql.dsql.ui.action.SaveAsStatementAction;
import org.vesalainen.parsers.sql.dsql.ui.action.RemoveStatementAction;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.UpdateableFetchResult;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.action.DSqlParseAction;
import org.vesalainen.parsers.sql.dsql.ui.action.ExecuteAction;
import org.vesalainen.parsers.sql.dsql.ui.action.FetchResultHandler;
import org.vesalainen.parsers.sql.dsql.ui.action.SelectForUpdateAction;

/**
 * @author Timo Vesalainen
 */
public class WorkBench extends WindowAdapter
{
    static final String TITLE = "Datastore Query 1.0";
    final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    final static Cursor defaultCursor = Cursor.getDefaultCursor();
    Engine engine;
    JFrame frame;
    private JMenuBar menuBar;
    private final JScrollPane upperPane;
    private final JScrollPane resultPane;
    private final JSplitPane splitPane;
    final JTextPane sqlPane;
    private final JButton executeButton;
    private final JButton selectAndUpdateButton;
    final String storedStatementsKind;
    private final JButton commitButton;
    private final JButton rollbackButton;
    private final JButton deleteRowButton;
    private final DSqlParseAction parseAction;
    
    public WorkBench(final DSQLEngine engine, String storedStatementsKind)
    {
        this.storedStatementsKind = storedStatementsKind;
        this.engine = engine;
        frame = new JFrame(TITLE);
        frame.addWindowListener(this);
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new ExceptionHandler());

        sqlPane = new JTextPane();
        sqlPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        sqlPane.setPreferredSize(new Dimension(700, 200));
        Map<Object,Action> actions = new HashMap<>();
        for (Action action : sqlPane.getActions())
        {
            actions.put(action.getValue(Action.NAME), action);
        }
        Document document = sqlPane.getDocument();
        UndoManager undoManager = new UndoManager();
        parseAction = new DSqlParseAction(this, undoManager);
        document.addDocumentListener(parseAction);
        document.addUndoableEditListener(parseAction);
        
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new OpenStatementAction("Open Statement", this, storedStatementsKind));
        fileMenu.add(new SaveStatementAction("Save Statement", this, storedStatementsKind));
        fileMenu.add(new SaveAsStatementAction("Save As Statement", this, storedStatementsKind));
        fileMenu.add(new RemoveStatementAction("Remove Statement", this, storedStatementsKind));
        
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        editMenu.add(new UndoAction("Undo", undoManager)).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(new RedoAction("Redo", undoManager)).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(actions.get(DefaultEditorKit.cutAction));
        editMenu.add(actions.get(DefaultEditorKit.copyAction));
        editMenu.add(actions.get(DefaultEditorKit.pasteAction));
        
        upperPane = new JScrollPane(sqlPane);
        
        JMenu sourceMenu = new JMenu("Source");
        menuBar.add(sourceMenu);
        
        InsertPropertiesHandler insertPropertiesHandler = new InsertPropertiesHandler(sqlPane);
        MetadataTreeAction insertPropertiesAction = new MetadataTreeAction("Insert Properties", engine.getStatistics(), insertPropertiesHandler);
        sourceMenu.add(insertPropertiesAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.ALT_DOWN_MASK));

        GenerateSelectHandler generateSelectHandler = new GenerateSelectHandler(sqlPane);
        MetadataTreeAction generateSelectAction = new MetadataTreeAction("Generate Select", engine.getStatistics(), generateSelectHandler);
        sourceMenu.add(generateSelectAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_DOWN_MASK));

        resultPane = new JScrollPane();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane, resultPane);
        splitPane.setDividerLocation(0.5);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        JMenu actionMenu = new JMenu("Actions");
        menuBar.add(actionMenu);
        
        ExecuteAction executeAction = new ExecuteAction(frame);
        parseAction.addPropertyChangeListener(executeAction);
        executeButton = new JButton(executeAction);
        buttonPanel.add(executeButton);
        actionMenu.add(executeButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));;
        
        SelectForUpdateAction selectForUpdateAction = new SelectForUpdateAction(frame);
        parseAction.addPropertyChangeListener(selectForUpdateAction);
        selectAndUpdateButton = new JButton(selectForUpdateAction);
        buttonPanel.add(selectAndUpdateButton);
        actionMenu.add(selectAndUpdateButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));;;
        
        FetchResultHandler fetchResultHandler = new FetchResultHandler(frame, resultPane);
        executeAction.addPropertyChangeListener(fetchResultHandler);
        selectForUpdateAction.addPropertyChangeListener(fetchResultHandler);
                
        deleteRowButton = new JButton(fetchResultHandler.getDeleteRowAction());
        buttonPanel.add(deleteRowButton);
        actionMenu.add(deleteRowButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.ALT_DOWN_MASK));;;

        commitButton = new JButton(fetchResultHandler.getCommitAction());
        buttonPanel.add(commitButton);
        actionMenu.add(commitButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));;;

        rollbackButton = new JButton(fetchResultHandler.getRollbackAction());
        buttonPanel.add(rollbackButton);
        actionMenu.add(rollbackButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));;;

        frame.setContentPane(contentPane);
        
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
    }

    public JTextPane getActiveTextPane()
    {
        return sqlPane;
    }
    
    public ActionListener createActionListener(final Component component, final ActionListener actionListener)
    {
        ActionListener al = new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    component.setCursor(busyCursor);
                    component.repaint();
                    actionListener.actionPerformed(e);
                }
                finally
                {
                    component.setCursor(defaultCursor);
                }
            }
        };
        return al;
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        engine.exit();
        System.exit(0);
    }
    
    public String getOpenStatement()
    {
        String title = frame.getTitle();
        if (TITLE.equals(title))
        {
            return null;
        }
        else
        {
            return title.substring(0, title.length()-TITLE.length()-3);
        }
    }
    public void setOpenStatement(String name, String sql)
    {
        frame.setTitle(name+" - "+TITLE);
        sqlPane.setText(sql);
    }
    public void setOpenStatement(String name)
    {
        frame.setTitle(name+" - "+TITLE);
    }

    public Engine getEngine()
    {
        return engine;
    }

    public JFrame getFrame()
    {
        return frame;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            if (args.length != 1)
            {
                System.err.println("usage: java ... <properties file>");
                System.exit(-1);
            }
            final Properties properties = new Properties();
            try (FileInputStream pFile = new FileInputStream(args[0]);)
            {
                properties.load(pFile);
            }
            CredentialsDialog dia = new CredentialsDialog(
                    properties.getProperty("remoteserver"), 
                    properties.getProperty("remoteuser"), 
                    properties.getProperty("remotepassword")
                    );
            if (dia.input())
            {
                DSQLEngine engine = DSQLEngine.getProxyInstance(dia.getServer(), dia.getEmail(), new String(dia.getPassword()));
                new WorkBench(engine, properties.getProperty("stored-statements-kind", "DSQLStatements"));
            }
            else
            {
                System.exit(-1);
            }
        }
        catch (Throwable ex)
        {
            ex = ex.getCause() == null ? ex : ex.getCause();
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            System.exit(-1);
        }
    }

}
