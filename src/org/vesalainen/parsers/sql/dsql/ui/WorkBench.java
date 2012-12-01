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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import org.vesalainen.parsers.sql.dsql.ui.action.MetadataTreeAction;
import org.vesalainen.parsers.sql.dsql.ui.action.RedoAction;
import org.vesalainen.parsers.sql.dsql.ui.action.UndoAction;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import org.vesalainen.parsers.sql.dsql.DSQLEngine;
import org.vesalainen.parsers.sql.dsql.ui.action.AboutAction;
import org.vesalainen.parsers.sql.dsql.ui.action.DSqlParseAction;
import org.vesalainen.parsers.sql.dsql.ui.action.ExecuteAction;
import org.vesalainen.parsers.sql.dsql.ui.action.ExportCVSAction;
import org.vesalainen.parsers.sql.dsql.ui.action.FetchResultHandler;
import org.vesalainen.parsers.sql.dsql.ui.action.OpenSQLFileAction;
import org.vesalainen.parsers.sql.dsql.ui.action.PersistenceHandler;
import org.vesalainen.parsers.sql.dsql.ui.action.PrintAction;
import org.vesalainen.parsers.sql.dsql.ui.action.SaveSQLFileAction;
import org.vesalainen.parsers.sql.dsql.ui.action.SelectForUpdateAction;
import org.vesalainen.parsers.sql.dsql.ui.plugin.MailPlugin;

/**
 * @author Timo Vesalainen
 */
public class WorkBench extends WindowAdapter implements VetoableChangeListener
{
    static final String TITLE = I18n.get("DATASTORE QUERY 1.0");
    static final String SqlProperty = WorkBench.class.getName()+".sql";
    final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    final static Cursor defaultCursor = Cursor.getDefaultCursor();
    public static final List<Image> icons = new ArrayList<>();
    static
    {
        icons.add(new ImageIcon(WorkBench.class.getResource("images/dsql16.png")).getImage());
        icons.add(new ImageIcon(WorkBench.class.getResource("images/dsql32.png")).getImage());
        icons.add(new ImageIcon(WorkBench.class.getResource("images/dsql48.png")).getImage());
        icons.add(new ImageIcon(WorkBench.class.getResource("images/dsql64.png")).getImage());
    }
    private DSQLEngine engine;
    private JFrame frame;
    private JMenuBar menuBar;
    private final JScrollPane upperPane;
    private final JScrollPane resultPane;
    private final JSplitPane splitPane;
    final JTextPane sqlPane;
    private JButton executeButton;
    private JButton selectAndUpdateButton;
    final String storedStatementsKind;
    private JButton commitButton;
    private JButton rollbackButton;
    private JButton deleteRowButton;
    private DSqlParseAction parseAction;
    private JPanel buttonPanel;
    private JMenu sourceMenu;
    private JMenu actionMenu;
    private FetchResultHandler fetchResultHandler;
    private ExecuteAction executeAction;
    private PersistenceHandler persistenceHandler;
    private JButton printButton;
    private JMenu helpMenu;
    private String title;
    private ExportCVSAction exportCVSAction;
    private boolean embed;
    private boolean readonly;
    
    public WorkBench(Properties properties) throws IOException, InterruptedException
    {
        this(properties, false, true);
    }
    public WorkBench(Properties properties, boolean embed, boolean readonly) throws IOException, InterruptedException
    {
        this.storedStatementsKind = properties.getProperty("stored-statements-kind", "DSQLStatements");
        this.embed = embed;
        this.readonly = readonly;
        engine = DSQLEngine.getProxyInstance(
                properties.getProperty("remoteserver"), 
                properties.getProperty("remoteuser"), 
                properties.getProperty("remotepassword")
                    );
        title = TITLE+" - "+properties.getProperty("remoteserver");
        frame = new JFrame(title);
        frame.addWindowListener(this);
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new ExceptionHandler());
        frame.setIconImages(icons);

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
        parseAction = new DSqlParseAction(this, undoManager, readonly);
        document.addDocumentListener(parseAction);
        document.addUndoableEditListener(parseAction);
        
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu(I18n.get("FILE"));
        menuBar.add(fileMenu);

        persistenceHandler = new PersistenceHandler(this, storedStatementsKind);
        
        OpenSQLFileAction openFileStatementAction = new OpenSQLFileAction(this, persistenceHandler);
        fileMenu.add(openFileStatementAction);
        SaveSQLFileAction saveSQLFileAction = new SaveSQLFileAction(this);
        fileMenu.add(saveSQLFileAction);
        persistenceHandler.addVetoableChangeListener(saveSQLFileAction);
        
        fileMenu.addSeparator();
        persistenceHandler.addVetoableChangeListener(this);
        for (Action action : persistenceHandler.getActions())
        {
            fileMenu.add(action);
        }
        fileMenu.addSeparator();

        JMenu editMenu = new JMenu(I18n.get("EDIT"));
        menuBar.add(editMenu);
        editMenu.add(new UndoAction(I18n.get("UNDO"), undoManager)).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(new RedoAction(I18n.get("REDO"), undoManager)).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(actions.get(DefaultEditorKit.cutAction));
        editMenu.add(actions.get(DefaultEditorKit.copyAction));
        editMenu.add(actions.get(DefaultEditorKit.pasteAction));
        
        upperPane = new JScrollPane(sqlPane);
        
        sourceMenu = new JMenu(I18n.get("SOURCE"));
        menuBar.add(sourceMenu);
        
        InsertPropertiesHandler insertPropertiesHandler = new InsertPropertiesHandler(sqlPane);
        MetadataTreeAction insertPropertiesAction = new MetadataTreeAction(
                this,
                I18n.get("INSERT PROPERTIES"), 
                I18n.get("INSERT PROPERTIES AT THE CURSOR POSITION"),
                engine.getStatistics(), 
                insertPropertiesHandler
                );
        sourceMenu.add(insertPropertiesAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.ALT_DOWN_MASK));

        GenerateSelectHandler generateSelectHandler = new GenerateSelectHandler(sqlPane);
        MetadataTreeAction generateSelectAction = new MetadataTreeAction(
                this,
                I18n.get("GENERATE SELECT"), 
                I18n.get("GENERATE A SELECT STATEMENT"),
                engine.getStatistics(), 
                generateSelectHandler
                );
        sourceMenu.add(generateSelectAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.ALT_DOWN_MASK));

        resultPane = new JScrollPane();

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane, resultPane);
        splitPane.setDividerLocation(0.5);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(splitPane, BorderLayout.CENTER);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        actionMenu = new JMenu(I18n.get("ACTIONS"));
        menuBar.add(actionMenu);
        
        executeAction = new ExecuteAction(frame);
        parseAction.addPropertyChangeListener(executeAction);
        executeButton = new JButton(executeAction);
        buttonPanel.add(executeButton);
        actionMenu.add(executeButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));;
        
        SelectForUpdateAction selectForUpdateAction = new SelectForUpdateAction(frame);
        parseAction.addPropertyChangeListener(selectForUpdateAction);
        if (!readonly)
        {
            selectAndUpdateButton = new JButton(selectForUpdateAction);
            buttonPanel.add(selectAndUpdateButton);
            actionMenu.add(selectAndUpdateButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));;;
        }
        
        fetchResultHandler = new FetchResultHandler(frame, resultPane);
        executeAction.addPropertyChangeListener(fetchResultHandler);
        selectForUpdateAction.addPropertyChangeListener(fetchResultHandler);
                
        deleteRowButton = new JButton(fetchResultHandler.getDeleteRowAction());
        buttonPanel.add(deleteRowButton);
        actionMenu.add(deleteRowButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.ALT_DOWN_MASK));

        commitButton = new JButton(fetchResultHandler.getCommitAction());
        buttonPanel.add(commitButton);
        actionMenu.add(commitButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));

        rollbackButton = new JButton(fetchResultHandler.getRollbackAction());
        buttonPanel.add(rollbackButton);
        actionMenu.add(rollbackButton.getAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        
        PrintAction printAction = new PrintAction();
        fetchResultHandler.addPropertyChangeListener(printAction);
        printButton = new JButton(printAction);
        buttonPanel.add(printButton);

        frame.setContentPane(contentPane);

        helpMenu = new JMenu(I18n.get("HELP"));
        menuBar.add(helpMenu);
        
        helpMenu.add(new AboutAction());
        
        exportCVSAction = new ExportCVSAction();
        fetchResultHandler.addPropertyChangeListener(exportCVSAction);
        persistenceHandler.addVetoableChangeListener(exportCVSAction);
        fileMenu.add(exportCVSAction);
        
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setSize(800, 580);
        
        addFetchResultPlugin(new MailPlugin(engine));
    }
    /**
     * Adds a FetchResultHandler.
     * @param plugin 
     */
    public void addFetchResultPlugin(FetchResultPlugin plugin)
    {
        Action action = (Action) plugin;
        JButton button = new JButton(action);
        buttonPanel.add(button);
        actionMenu.add(action);
        plugin.setFrame(frame);
        fetchResultHandler.addPropertyChangeListener(plugin);
        persistenceHandler.addVetoableChangeListener(plugin);
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
    {
        Entity entity = (Entity) evt.getNewValue();
        String name = "";
        switch (evt.getPropertyName())
        {
            case PersistenceHandler.OPEN:
                if (entity != null)
                {
                    // Open
                    Text sql = (Text) entity.getProperty(SqlProperty);
                    if (sql != null)
                    {
                        sqlPane.setText(sql.getValue());
                    }
                    else
                    {
                        sqlPane.setText(null);
                    }
                    name = entity.getKey().getName();
                }
                else
                {
                    // New
                    sqlPane.setText(null);
                }
                break;
            case PersistenceHandler.SAVE:
                if (entity != null)
                {
                    // Save
                    Text sql = new Text(sqlPane.getText());
                    entity.setUnindexedProperty(SqlProperty, sql);
                    name = entity.getKey().getName();
                }
                else
                {
                    // Remove
                    sqlPane.setText(null);
                }
                break;
        }
        frame.setTitle(name+" - "+title);
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
        if (!embed)
        {
            System.exit(0);
        }
    }
    
    public DSQLEngine getEngine()
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
            File file = new File(args[0]);
            CredentialsDialog dia = new CredentialsDialog(file);
            if (dia.input())
            {
                WorkBench workBench = new WorkBench(dia.getProperties());
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
