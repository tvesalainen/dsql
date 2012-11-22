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
import java.awt.Color;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.swing.undo.UndoManager;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.parser.util.OffsetLocatorException;
import org.vesalainen.parsers.sql.Engine;
import org.vesalainen.parsers.sql.ErrorReporter;
import org.vesalainen.parsers.sql.FetchResult;
import org.vesalainen.parsers.sql.Placeholder;
import org.vesalainen.parsers.sql.SQLLocator;
import org.vesalainen.parsers.sql.SelectStatement;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.UpdateableFetchResult;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;

/**
 * @author Timo Vesalainen
 */
public class WorkBench extends WindowAdapter implements DocumentListener, SQLLocator, ErrorReporter
{
    static final String TITLE = "Datastore Query 1.0";
    final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    final static Cursor defaultCursor = Cursor.getDefaultCursor();
    Engine engine;
    JFrame frame;
    private JMenuBar menuBar;
    private final JScrollPane sqlPane;
    private final JScrollPane lowerPane;
    private final JSplitPane splitPane;
    final JTextPane sqlArea;
    private FetchResultTableModel tableModel;
    private InputReader reader;
    private Statement statement;
    private final JButton executeButton;
    private final JButton selectButton;
    private final JButton selectAndUpdateButton;
    private final Action redAction;
    private final Action blueAction;
    private final Action blackAction;
    private final Timer timer;
    private final Action grayAction;
    private final ForegroundAction orangeAction;
    private final UndoableEditListenerSwitch undoSwitch;
    final String storedStatementsKind;
    private final JButton commitButton;
    private final JButton rollbackButton;
    private UpdateableFetchResult updateableFetchResult;
    private DSJTable table;
    private final JButton deleteRowButton;
    private final JButton insertRowButton;
    private Level errorLevel;
    
    public WorkBench(final DSQLEngine engine, String storedStatementsKind)
    {
        this.storedStatementsKind = storedStatementsKind;
        this.engine = engine;
        frame = new JFrame();
        frame.addWindowListener(this);
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new ExceptionHandler());

        timer = new Timer(500, new AL(this));
        timer.stop();
        timer.setRepeats(false);
        
        sqlArea = new JTextPane();
        sqlArea.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        sqlArea.setPreferredSize(new Dimension(700, 200));
        Map<Object,Action> actions = new HashMap<>();
        for (Action action : sqlArea.getActions())
        {
            actions.put(action.getValue(Action.NAME), action);
        }
        blackAction = new StyledEditorKit.ForegroundAction("Black", Color.BLACK);
        redAction = new StyledEditorKit.ForegroundAction("Red", Color.red);
        blueAction = new StyledEditorKit.ForegroundAction("Blue", Color.BLUE);
        grayAction = new StyledEditorKit.ForegroundAction("Green", Color.LIGHT_GRAY);
        orangeAction = new StyledEditorKit.ForegroundAction("Orange", Color.ORANGE);
        
        Document document = sqlArea.getDocument();
        document.addDocumentListener(this);
        UndoManager undo = new UndoManager();
        undoSwitch = new UndoableEditListenerSwitch(undo);
        document.addUndoableEditListener(undoSwitch);
        
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
        editMenu.add(new UndoAction("Undo", undo)).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(new RedoAction("Redo", undo)).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(actions.get(DefaultEditorKit.cutAction));
        editMenu.add(actions.get(DefaultEditorKit.copyAction));
        editMenu.add(actions.get(DefaultEditorKit.pasteAction));
        
        sqlPane = new JScrollPane(sqlArea);
        
        JMenu sourceMenu = new JMenu("Source");
        menuBar.add(sourceMenu);
        
        InsertPropertiesHandler insertPropertiesHandler = new InsertPropertiesHandler(sqlArea);
        MetadataTreeAction insertPropertiesAction = new MetadataTreeAction("Insert Properties", engine.getStatistics(), insertPropertiesHandler);
        sourceMenu.add(insertPropertiesAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.ALT_DOWN_MASK));

        GenerateSelectHandler generateSelectHandler = new GenerateSelectHandler(sqlArea);
        MetadataTreeAction generateSelectAction = new MetadataTreeAction("Generate Select", engine.getStatistics(), generateSelectHandler);
        sourceMenu.add(generateSelectAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_DOWN_MASK));

        lowerPane = new JScrollPane();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sqlPane, lowerPane);
        splitPane.setDividerLocation(0.5);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        executeButton = new JButton("Execute");
        executeButton.setEnabled(false);
        ActionListener executeAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                execute();
            }
        };
        executeAction = createActionListener(frame, executeAction);
        executeButton.addActionListener(executeAction);
        buttonPanel.add(executeButton);

        selectButton = new JButton("Select");
        selectButton.setEnabled(false);
        ActionListener selectAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                select();
            }
        };
        selectAction = createActionListener(frame, selectAction);
        selectButton.addActionListener(selectAction);
        buttonPanel.add(selectButton);

        selectAndUpdateButton = new JButton("Select&Update");
        selectAndUpdateButton.setEnabled(false);
        ActionListener selectAndUpdateAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                selectAndUpdate();
            }
        };
        selectAndUpdateAction = createActionListener(frame, selectAndUpdateAction);
        selectAndUpdateButton.addActionListener(selectAndUpdateAction);
        buttonPanel.add(selectAndUpdateButton);

        deleteRowButton = new JButton("Delete Row");
        deleteRowButton.setEnabled(false);
        ActionListener deleteRowAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int rowNum = table.getSelectedRow();
                while (rowNum != -1)
                {
                    tableModel.deleteRow(rowNum);
                    rowNum = table.getSelectedRow();
                }
            }
        };
        deleteRowAction = createActionListener(frame, deleteRowAction);
        deleteRowButton.addActionListener(deleteRowAction);
        buttonPanel.add(deleteRowButton);

        insertRowButton = new JButton("Insert Row");
        insertRowButton.setEnabled(false);
        ActionListener insertRowAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        };
        insertRowAction = createActionListener(frame, insertRowAction);
        insertRowButton.addActionListener(insertRowAction);
        buttonPanel.add(insertRowButton);

        commitButton = new JButton("Commit");
        commitButton.setEnabled(false);
        ActionListener commitAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellEditor cellEditor = table.getCellEditor();
                if (cellEditor != null)
                {
                    cellEditor.stopCellEditing();
                }
                updateableFetchResult.update();
                updateableFetchResult = null;
                engine.commitTransaction();
                commitButton.setEnabled(false);
                rollbackButton.setEnabled(false);
                insertRowButton.setEnabled(false);
                deleteRowButton.setEnabled(false);
            }
        };
        commitAction = createActionListener(frame, commitAction);
        commitButton.addActionListener(commitAction);
        buttonPanel.add(commitButton);

        rollbackButton = new JButton("Rollback");
        rollbackButton.setEnabled(false);
        ActionListener rollbackAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateableFetchResult = null;
                engine.rollbackTransaction();
                commitButton.setEnabled(false);
                rollbackButton.setEnabled(false);
                insertRowButton.setEnabled(false);
                deleteRowButton.setEnabled(false);
            }
        };
        rollbackAction = createActionListener(frame, rollbackAction);
        rollbackButton.addActionListener(rollbackAction);
        buttonPanel.add(rollbackButton);

        frame.setContentPane(contentPane);
        
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
    }

    private void execute()
    {
        if (enterPlaceHolders(statement))
        {
            statement.execute();
            if (tableModel != null)
            {
                tableModel.clear();
            }
        }
    }

    private void select()
    {
        if (enterPlaceHolders(statement))
        {
            SelectStatement select = (SelectStatement) statement;
            FetchResult result = select.execute();
            if (tableModel == null)
            {
                tableModel = new FetchResultTableModel(result);
                table = new DSJTable(tableModel);
                table.setFrame(frame);
                lowerPane.setViewportView(table);
            }
            else
            {
                tableModel.updateData(result);
            }
        }
    }
    private void selectAndUpdate()
    {
        if (enterPlaceHolders(statement))
        {
            SelectStatement select = (SelectStatement) statement;
            engine.beginTransaction();
            updateableFetchResult = select.selectForUpdate();
            if (tableModel == null)
            {
                tableModel = new FetchResultTableModel(updateableFetchResult);
                table = new DSJTable(tableModel);
                table.setFrame(frame);
                lowerPane.setViewportView(table);
            }
            else
            {
                tableModel.updateData(updateableFetchResult);
            }
            commitButton.setEnabled(true);
            rollbackButton.setEnabled(true);
            insertRowButton.setEnabled(true);
            deleteRowButton.setEnabled(true);
        }
    }
    
    private boolean enterPlaceHolders(Statement statement)
    {
        LinkedHashMap<String,Placeholder> placeholderMap = statement.getPlaceholderMap();
        if (!placeholderMap.isEmpty())
        {
            
            InputDialog inputDialog = new InputDialog(frame, "Enter Placeholder Values");
            for (Entry<String,Placeholder> entry : placeholderMap.entrySet())
            {
                Placeholder ph = entry.getValue();
                inputDialog.add(ph.getName(), ph.getValue(), ph.getType());
            }
            if (inputDialog.input())
            {
                int row = 0;
                for (Entry<String,Placeholder> entry : placeholderMap.entrySet())
                {
                    Placeholder ph = entry.getValue();
                    statement.bindValue(ph.getName(), inputDialog.get(row++));
                }
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    
    private void changed()
    {
        timer.restart();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        changed();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        changed();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        //changed();
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
    
    private void color(Action action, int start, int end)
    {
        try
        {
            undoSwitch.setOff();
            int save = sqlArea.getCaretPosition();
            sqlArea.setCaretPosition(start);
            sqlArea.moveCaretPosition(end);
            action.actionPerformed(null);
            sqlArea.setCaretPosition(save);
            undoSwitch.setOn();
        }
        catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void locate(int start, int end, Type type)
    {
        switch (type)
        {
            case COMMENT:
                color(grayAction, start, end);
                break;
            case RESERVED_WORD:
                color(blueAction, start, end);
                break;
            default:
                assert false;
        }
    }

    @Override
    public void report(String message, Level level, String source, int start, int end)
    {
        switch (level)
        {
            case Fatal:
                color(redAction, start, end);
                break;
            case Hint:
                color(orangeAction, start, end);
                break;
            default:
                assert false;
        }
        sqlArea.setToolTipText(message);
        if (level.ordinal() > errorLevel.ordinal())
        {
            errorLevel = level;
        }
    }

    @Override
    public void replace(String newText, int start, int end)
    {
        undoSwitch.setOff();
        int save = sqlArea.getCaretPosition();
        sqlArea.setCaretPosition(start);
        sqlArea.moveCaretPosition(end);
        sqlArea.replaceSelection(newText);
        sqlArea.setCaretPosition(save);
        undoSwitch.setOn();
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
        sqlArea.setText(sql);
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

    private class AL implements ActionListener
    {
        WorkBench parent;

        public AL(WorkBench parent)
        {
            this.parent = parent;
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            sqlArea.setToolTipText("");
            color(blackAction, 0, sqlArea.getDocument().getLength());
            executeButton.setEnabled(false);
            selectButton.setEnabled(false);
            selectAndUpdateButton.setEnabled(false);
            commitButton.setEnabled(false);
            rollbackButton.setEnabled(false);
            String sql = sqlArea.getText();
            if (!sql.isEmpty())
            {
                if (reader == null)
                {
                    reader = new InputReader(sql);
                }
                else
                {
                    reader.reuse(sql);
                }
                try
                {
                    parent.errorLevel = Level.Ok;
                    engine.check(reader, parent);
                    statement = engine.prepare(sql);
                    statement.check(engine, parent);
                    if (parent.errorLevel != Level.Fatal)
                    {
                        if (statement instanceof SelectStatement)
                        {
                            selectButton.setEnabled(true);
                            selectAndUpdateButton.setEnabled(true);
                        }
                        else
                        {
                            executeButton.setEnabled(true);
                        }
                    }
                }
                catch (OffsetLocatorException ex)
                {
                    color(redAction, ex.getStart(), ex.getEnd());
                }
            }
        }
        
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
