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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.vesalainen.parsers.sql.SQLLocator;
import org.vesalainen.parsers.sql.SelectStatement;
import org.vesalainen.parsers.sql.Statement;
import org.vesalainen.parsers.sql.UpdateableFetchResult;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.DataStoreEngineProxy;

/**
 * @author Timo Vesalainen
 */
public class WorkBench extends WindowAdapter implements DocumentListener, SQLLocator, ErrorReporter
{
    public final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    public final static Cursor defaultCursor = Cursor.getDefaultCursor();
    private Engine engine;
    private JFrame frame;
    private JMenuBar menuBar;
    private final JScrollPane sqlPane;
    private final JScrollPane lowerPane;
    private final JSplitPane splitPane;
    private final JTextPane sqlArea;
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
    
    public WorkBench(DSQLEngine engine)
    {
        this.engine = engine;
        frame = new JFrame();
        frame.addWindowListener(this);

        timer = new Timer(500, new AL(this));
        timer.stop();
        timer.setRepeats(false);
        
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

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

        frame.setContentPane(contentPane);
        
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
    }

    private void execute()
    {
        statement.execute();
        if (tableModel != null)
        {
            tableModel.clear();
        }
    }

    private void select()
    {
        SelectStatement select = (SelectStatement) statement;
        FetchResult result = select.execute();
        if (tableModel == null)
        {
            tableModel = new FetchResultTableModel(result);
            JTable table = new DSJTable(tableModel);
            lowerPane.setViewportView(table);
        }
        else
        {
            tableModel.updateDate(result);
        }
    }
    private void selectAndUpdate()
    {
        SelectStatement select = (SelectStatement) statement;
        UpdateableFetchResult result = select.selectForUpdate();
        if (tableModel == null)
        {
            tableModel = new FetchResultTableModel(result);
            JTable table = new DSJTable(tableModel);
            lowerPane.setViewportView(table);
        }
        else
        {
            tableModel.updateDate(result);
        }
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            String server = "";
            String email = "";
            String password = "";
            if (args.length > 0)
            {
                server = args[0];
            }
            if (args.length > 1)
            {
                email = args[1];
            }
            if (args.length > 2)
            {
                password = args[2];
            }
            CredentialsDialog dia = new CredentialsDialog(server, email, password);
            if (dia.input())
            {
                DSQLEngine engine = DSQLEngine.getProxyInstance(dia.getServer(), dia.getEmail(), new String(dia.getPassword()));
                new WorkBench(engine);
            }
            else
            {
                System.exit(-1);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
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
            color(blackAction, 0, sqlArea.getDocument().getLength());
            executeButton.setEnabled(false);
            selectButton.setEnabled(false);
            selectAndUpdateButton.setEnabled(false);
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
                    engine.check(reader, parent);
                    statement = engine.prepare(sql);
                    statement.check(engine, parent);
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
                catch (OffsetLocatorException ex)
                {
                    color(redAction, ex.getStart(), ex.getEnd());
                }
            }
        }
        
    }
}
